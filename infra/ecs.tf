resource "aws_ecr_repository" "app_repo" {
  name                 = "${var.project_name}-repo"
  image_tag_mutability = "MUTABLE"

  image_scanning_configuration {
    scan_on_push = true
  }

  force_delete = true
}

resource "aws_ecs_cluster" "main" {
  name = "${var.project_name}-cluster"
}


resource "aws_iam_role" "ecs_execution_role" {
  name = "${var.project_name}-execution-role"

  assume_role_policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Action = "sts:AssumeRole"
        Effect = "Allow"
        Principal = {
          Service = "ecs-tasks.amazonaws.com"
        }
      }
    ]
  })
}

resource "aws_iam_role" "ecs_task_role" {
  name = "${var.project_name}-task-role"

  assume_role_policy = jsonencode({
    Version = "2012-10-17"
    Statement = [{
      Action    = "sts:AssumeRole"
      Effect    = "Allow"
      Principal = { Service = "ecs-tasks.amazonaws.com" }
    }]
  }
  )
}


resource "aws_iam_policy" "secrets_policy" {
  name        = "${var.project_name}-secrets-policy"
  description = "Erlaubt den Zugriff auf das RDS Passwort"

  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Effect   = "Allow"
        Action   = ["secretsmanager:GetSecretValue"]
        Resource = [aws_db_instance.main.master_user_secret[0].secret_arn, aws_secretsmanager_secret.keycloak_creds.arn]
      }
    ]
  })
}

resource "aws_iam_role_policy_attachment" "secrets_attach" {
  role       = aws_iam_role.ecs_execution_role.name
  policy_arn = aws_iam_policy.secrets_policy.arn
}

resource "aws_iam_role_policy_attachment" "ecs_standard_attach" {
  role       = aws_iam_role.ecs_execution_role.name
  policy_arn = "arn:aws:iam::aws:policy/service-role/AmazonECSTaskExecutionRolePolicy"
}

resource "aws_ecs_task_definition" "app_task" {
  family                   = "${var.project_name}-task"
  network_mode             = "awsvpc"
  requires_compatibilities = ["FARGATE"]
  cpu                      = 512
  memory                   = 1024

  execution_role_arn = aws_iam_role.ecs_execution_role.arn
  task_role_arn      = aws_iam_role.ecs_task_role.arn

  container_definitions = jsonencode([
    {
      name = "spring-app"
      image     = aws_ecr_repository.app_repo.repository_url  # Hier nimmt terraform standard :latest, es wird solang fehlschlagen bis ein image im ecr ist
      essential = true

      portMappings = [
        {
          containerPort = 8080
          hostPort      = 8080
        }
      ]

      secrets = [
        {
          name = "SPRING_DATASOURCE_PASSWORD"
          valueFrom = "${aws_db_instance.main.master_user_secret[0].secret_arn}:password::"
        },
        {
          name      = "SPRING_DATASOURCE_USERNAME"
          valueFrom = "${aws_db_instance.main.master_user_secret[0].secret_arn}:username::"
        },
        {
          name      = "KEYCLOAK_CLIENT_SECRET"
          valueFrom = "${aws_secretsmanager_secret.keycloak_creds.arn}:client_secret::"
        }
      ]

      environment = [
        {
          name  = "SPRING_PROFILES_ACTIVE"
          value = "prod"
        },
        {
          name  = "SPRING_DATASOURCE_URL"
          value = "jdbc:postgresql://${aws_db_instance.main.address}:${aws_db_instance.main.port}/${aws_db_instance.main.db_name}"
        },
        {
          name  = "KEYCLOAK_ISSUER_URI"
          value = "http://${aws_lb.main.dns_name}/auth/realms/tasktrackr-realm"
        },
        {
          name  = "KEYCLOAK_INTERNAL_URL"
          value = "http://auth.tasktrackr.local:8080/auth/realms/tasktrackr-realm/protocol/openid-connect"
        }
      ]


      logConfiguration = {
        logDriver = "awslogs"
        options = {
          "awslogs-group" = aws_cloudwatch_log_group.app_logs.name
          "awslogs-region" = var.aws_region
          "awslogs-stream-prefix" = "ecs"
        } }
    }
  ])
}

resource "aws_secretsmanager_secret" "keycloak_creds" {
  name        = "${var.project_name}-keycloak-client-secret"
  description = "Manually managed secret for Keycloak client"

  recovery_window_in_days = 0
}

resource "aws_ecs_service" "app_service" {
  name            = "${var.project_name}-service"
  cluster         = aws_ecs_cluster.main.id
  task_definition = aws_ecs_task_definition.app_task.arn
  launch_type     = "FARGATE"

  desired_count = 2

  health_check_grace_period_seconds = 120

  network_configuration {
    subnets          = module.vpc.private_subnets
    security_groups = [aws_security_group.app_sg.id]
    assign_public_ip = false
  }

  load_balancer {
    target_group_arn = aws_lb_target_group.app_tg.arn
    container_name   = "spring-app"
    container_port   = 8080
  }
  depends_on = [aws_lb_listener.http]

}

resource "aws_ecs_task_definition" "keycloak_task" {
  family                   = "${var.project_name}-keycloak"
  network_mode             = "awsvpc"
  requires_compatibilities = ["FARGATE"]
  cpu                      = 512
  memory                   = 1024

  execution_role_arn = aws_iam_role.ecs_execution_role.arn
  task_role_arn      = aws_iam_role.ecs_task_role.arn

  container_definitions = jsonencode([
    {
      name  = "keycloak"
      image = "quay.io/keycloak/keycloak:26.2.5"
      entryPoint = ["/opt/keycloak/bin/kc.sh"]
      command  = ["start-dev"] # später start
      essential = true

      portMappings = [
        {
          containerPort = 8080
          hostPort      = 8080
        }
      ]

      environment = [
        { name = "KC_DB", value = "postgres" },
        # Wir nutzen hier die Variablen der RDS Instanz direkt:
        {
          name  = "KC_DB_URL",
          value = "jdbc:postgresql://${aws_db_instance.main.address}:${aws_db_instance.main.port}/${aws_db_instance.main.db_name}"
        },
        { name = "KC_HEALTH_ENABLED", value = "true" },
        { name = "KEYCLOAK_ADMIN", value = "admin" },
        { name = "KC_HTTP_RELATIVE_PATH", value = "/auth" }
      ]

      secrets = [
        {
          name      = "KC_DB_USERNAME"
          valueFrom = "${aws_db_instance.main.master_user_secret[0].secret_arn}:username::"
        },
        {
          name      = "KC_DB_PASSWORD"
          valueFrom = "${aws_db_instance.main.master_user_secret[0].secret_arn}:password::"
        },
        {
          name      = "KEYCLOAK_ADMIN_PASSWORD"
          valueFrom = "${aws_db_instance.main.master_user_secret[0].secret_arn}:password::"
        }
      ]


      logConfiguration = {
        logDriver = "awslogs"
        options = {
          "awslogs-group"         = aws_cloudwatch_log_group.app_logs.name
          "awslogs-region"        = var.aws_region
          "awslogs-stream-prefix" = "keycloak"
        }
      }
    }
  ])
}

resource "aws_ecs_service" "keycloak_service" {
  name            = "${var.project_name}-keycloak-service"
  cluster         = aws_ecs_cluster.main.id
  task_definition = aws_ecs_task_definition.keycloak_task.arn
  launch_type     = "FARGATE"
  desired_count   = 1

  network_configuration {
    subnets         = module.vpc.private_subnets
    security_groups = [aws_security_group.keycloak_sg.id]
    assign_public_ip = false
  }

  service_registries {
    registry_arn = aws_service_discovery_service.auth.arn
  }

  load_balancer {
    target_group_arn = aws_lb_target_group.keycloak_tg.arn
    container_name   = "keycloak"
    container_port   = 8080
  }
}


resource "aws_cloudwatch_log_group" "app_logs" {
  name = "/ecs/${var.project_name}-logs"
  retention_in_days = 1
}
