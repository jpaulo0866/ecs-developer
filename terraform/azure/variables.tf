variable "azure_location" {
  description = "The Azure location to create resources in."
  type        = string
  default     = "East US"
}

variable "resource_group_name" {
  description = "The name of the Azure Resource Group."
  type        = string
  default     = "bff-log-service-rg"
}

variable "storage_account_name" {
  description = "The name of the Azure Storage Account. Must be globally unique and use only lowercase letters and numbers."
  type        = string
  default     = "bfflogstorageaccunique"
}

variable "container_name" {
  description = "The name of the Blob Storage container."
  type        = string
  default     = "logs"
}

variable "create_resources" {
  description = "Set to true to create Azure resources, false to skip."
  type        = bool
  default     = true
}