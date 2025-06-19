variable "gcp_project_id" {
  description = "The GCP project ID to create resources in."
  type        = string
}

variable "gcp_region" {
  description = "The GCP region for the bucket."
  type        = string
  default     = "us-central1"
}

variable "bucket_name" {
  description = "The name of the GCS bucket. Must be globally unique."
  type        = string
  default     = "bff-log-storage-bucket-gcp-unique"
}

variable "create_resources" {
  description = "Set to true to create GCP resources, false to skip."
  type        = bool
  default     = true
}