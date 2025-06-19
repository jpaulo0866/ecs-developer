output "gcs_bucket_name" {
  description = "The name of the created GCS bucket."
  value       = var.create_resources ? google_storage_bucket.log_bucket[0].name : "Resources not created."
}

output "gcp_service_account_email" {
  description = "The email of the created service account."
  value       = var.create_resources ? google_service_account.app_sa[0].email : "Resources not created."
}

output "gcp_credentials_base64" {
  description = "The base64-encoded service account key JSON file."
  value       = var.create_resources ? google_service_account_key.sa_key[0].private_key : "Resources not created."
  sensitive   = true
}