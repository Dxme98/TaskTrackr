variable "aws_region" {
  description = "Die AWS Region für das Deployment"
  type        = string
  default     = "eu-central-1" # Frankfurt
}

variable "project_name" {
  description = "Name des Projekts"
  type        = string
  default     = "tasktrackr"
}