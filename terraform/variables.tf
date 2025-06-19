#------------------------------------------------------------------------------
# AWS Variables
#------------------------------------------------------------------------------
variable "create_aws_resources" {
  description = "Set to true to create AWS resources."
  type        = bool
  default     = true
}

variable "aws_region" {
  description = "The AWS region to create resources in."
  type        = string
  default     = "us-east-1"
}

variable "aws_bucket_name" {
  description = "The name of the S3 bucket. Must be globally unique."
  type        = string
  default     = "bff-log-storage-bucket-unique-aws"
}

#------------------------------------------------------------------------------
# GCP Variables
#------------------------------------------------------------------------------
variable "create_gcp_resources" {
  description = "Set to true to create GCP resources."
  type        = bool
  default     = true
}

variable "gcp_project_id" {
  description = "The GCP project ID to create resources in."
  type        = string
}

variable "gcp_region" {
  description = "The GCP region for the bucket."
  type        = string
  default     = "us-central1"
}

variable "gcp_bucket_name" {
  description = "The name of the GCS bucket. Must be globally unique."
  type        = string
  default     = "bff-log-storage-bucket-unique-gcp"
}

#------------------------------------------------------------------------------
# Azure Variables
#------------------------------------------------------------------------------
variable "create_azure_resources" {
  description = "Set to true to create Azure resources."
  type        = bool
  default     = true
}

variable "azure_location" {
  description = "The Azure location to create resources in."
  type        = string
  default     = "East US"
}

variable "azure_resource_group_name" {
  description = "The name of the Azure Resource Group."
  type        = string
  default     = "bff-log-service-rg"
}

variable "azure_storage_account_name" {
  description = "The name of the Azure Storage Account. Must be globally unique and use only lowercase letters and numbers."
  type        = string
  default     = "bfflogstorageaccunique"
}

variable "azure_container_name" {
  description = "The name of the Blob Storage container."
  type        = string
  default     = "logs"
}