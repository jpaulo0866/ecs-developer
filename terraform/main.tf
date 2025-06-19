terraform {
  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 5.0"
    }
    google = {
      source  = "hashicorp/google"
      version = "~> 5.0"
    }
    azurerm = {
      source  = "hashicorp/azurerm"
      version = "~> 3.0"
    }
  }
}

module "aws" {
  source = "./aws"

  create_resources = var.create_aws_resources
  aws_region       = var.aws_region
  bucket_name      = var.aws_bucket_name
}

module "gcp" {
  source = "./gcp"

  create_resources = var.create_gcp_resources
  gcp_project_id   = var.gcp_project_id
  gcp_region       = var.gcp_region
  bucket_name      = var.gcp_bucket_name
}

module "azure" {
  source = "./azure"

  create_resources         = var.create_azure_resources
  azure_location           = var.azure_location
  resource_group_name      = var.azure_resource_group_name
  storage_account_name     = var.azure_storage_account_name
  container_name           = var.azure_container_name
}