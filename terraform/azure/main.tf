provider "azurerm" {
  features {}
}

resource "azurerm_resource_group" "rg" {
  count    = var.create_resources ? 1 : 0
  name     = var.resource_group_name
  location = var.azure_location
}

resource "azurerm_storage_account" "storage" {
  count                    = var.create_resources ? 1 : 0
  name                     = var.storage_account_name
  resource_group_name      = azurerm_resource_group.rg[0].name
  location                 = azurerm_resource_group.rg[0].location
  account_tier             = "Standard"
  account_replication_type = "LRS"
}

resource "azurerm_storage_container" "container" {
  count                 = var.create_resources ? 1 : 0
  name                  = var.container_name
  storage_account_name  = azurerm_storage_account.storage[0].name
  container_access_type = "private"
}