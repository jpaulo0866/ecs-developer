# --- AWS Outputs ---
output "aws_s3_bucket_name" {
  description = "The name of the created S3 bucket."
  value       = module.aws.s3_bucket_name
}

output "aws_access_key_id" {
  description = "The access key ID for the IAM user."
  value       = module.aws.aws_access_key_id
  sensitive   = true
}

output "aws_secret_access_key" {
  description = "The secret access key for the IAM user."
  value       = module.aws.aws_secret_access_key
  sensitive   = true
}

# --- GCP Outputs ---
output "gcp_gcs_bucket_name" {
  description = "The name of the created GCS bucket."
  value       = module.gcp.gcs_bucket_name
}

output "gcp_service_account_email" {
  description = "The email of the created service account."
  value       = module.gcp.gcp_service_account_email
}

output "gcp_credentials_base64" {
  description = "The base64-encoded service account key JSON file."
  value       = module.gcp.gcp_credentials_base64
  sensitive   = true
}

# --- Azure Outputs ---
output "azure_storage_container_name" {
  description = "The name of the created Azure Storage Container."
  value       = module.azure.storage_container_name
}

output "azure_connection_string" {
  description = "The primary connection string for the storage account."
  value       = module.azure.azure_connection_string
  sensitive   = true
}