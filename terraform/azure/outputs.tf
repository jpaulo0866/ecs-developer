output "storage_container_name" {
  description = "The name of the created Azure Storage Container."
  value       = var.create_resources ? azurerm_storage_container.container[0].name : "Resources not created."
}

output "azure_connection_string" {
  description = "The primary connection string for the storage account."
  value       = var.create_resources ? azurerm_storage_account.storage[0].primary_connection_string : "Resources not created."
  sensitive   = true
}