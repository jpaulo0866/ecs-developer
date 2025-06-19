output "s3_bucket_name" {
  description = "The name of the created S3 bucket."
  value       = var.create_resources ? aws_s3_bucket.log_bucket[0].id : "Resources not created."
}

output "aws_access_key_id" {
  description = "The access key ID for the IAM user."
  value       = var.create_resources ? aws_iam_access_key.app_user_key[0].id : "Resources not created."
  sensitive   = true
}

output "aws_secret_access_key" {
  description = "The secret access key for the IAM user."
  value       = var.create_resources ? aws_iam_access_key.app_user_key[0].secret : "Resources not created."
  sensitive   = true
}