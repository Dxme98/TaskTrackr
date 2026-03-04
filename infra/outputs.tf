# --- Networking Outputs ---
output "vpc_id" {
  description = "Die ID des VPC"
  value       = module.vpc.vpc_id
}

output "private_subnets" {
  description = "Liste der privaten Subnetz-IDs (wo die App läuft)"
  value       = module.vpc.private_subnets
}

# --- Database Outputs ---
output "rds_endpoint" {
  description = "Die Adresse der Datenbank (Host)"
  value       = aws_db_instance.main.address
}

output "rds_port" {
  description = "Der Port der Datenbank"
  value       = aws_db_instance.main.port
}

output "rds_db_name" {
  description = "Der Name der Initial-Datenbank"
  value       = aws_db_instance.main.db_name
}

output "app_url" {
  value       = "http://${aws_lb.main.dns_name}"
  description = "Hier klicken um die App zu sehen"
}

output "keycloak_url" {
  description = "Die Adresse zur Keycloak Admin-Konsole (Pfad-Routing)"
  value       = "http://${aws_lb.main.dns_name}/auth"
}

output "cloudfront_url" {
  description = "Die Haupt-URL des Projekts (HTTPS via CloudFront)"
  value       = "https://${aws_cloudfront_distribution.main.domain_name}"
}