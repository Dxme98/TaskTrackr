resource "aws_db_instance" "main" {
  identifier        = "${var.project_name}-db"
  engine            = "postgres"
  engine_version    = "16.6"
  instance_class    = "db.t4g.micro"
  allocated_storage = 20

  db_name  = "tasktrackrapp"
  username = "dbadmin"

  manage_master_user_password = true

  db_subnet_group_name   = module.vpc.database_subnet_group_name
  vpc_security_group_ids = [aws_security_group.db_sg.id]

  publicly_accessible = false
  skip_final_snapshot = true
  delete_automated_backups = true
  deletion_protection = false
}