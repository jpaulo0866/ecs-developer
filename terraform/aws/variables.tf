variable "aws_region" {
  description = "The AWS region to create resources in."
  type        = string
  default     = "us-east-1"
}

variable "bucket_name" {
  description = "The name of the S3 bucket. Must be globally unique."
  type        = string
  default     = "bff-log-storage-bucket-unique-name"
}

variable "create_resources" {
  description = "Set to true to create AWS resources, false to skip."
  type        = bool
  default     = true
}