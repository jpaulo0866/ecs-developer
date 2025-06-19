# 0002. Modular Terraform Structure per Cloud Provider

- **Date**: 2025-06-20
- **Status**: Accepted

## Context

The project requires infrastructure on multiple cloud platforms (AWS, GCP, and Azure) to manage storage buckets and access permissions. We need a way to define and manage this infrastructure that is version-controlled, repeatable, and easy to maintain. A single, monolithic set of Terraform files for all three providers would become complex and difficult to navigate. Furthermore, developers may need to deploy resources for only a subset of these providers during local development or testing, requiring a flexible configuration.

## Decision

We will structure our Terraform configuration using a modular approach, with a dedicated sub-module for each cloud provider.

1.  **Root Module**: A main `terraform/` directory will serve as the root module. Its `main.tf` will be responsible for configuring the required providers (AWS, GCP, Azure) and conditionally invoking the sub-modules.

2.  **Provider Sub-modules**: We will create separate directories for each provider (`./aws`, `./gcp`, `./azure`). Each directory will contain all the Terraform resources specific to that provider (e.g., S3 buckets and IAM users for AWS, GCS buckets and service accounts for GCP).

3.  **Conditional Creation**: The root module will use boolean input variables (e.g., `create_aws_resources`, `create_gcp_resources`) to control whether a provider's sub-module is invoked. This allows developers to easily enable or disable the creation of resources for any given cloud by setting a flag in the `terraform.tfvars` file.

This structure isolates provider-specific logic, making the infrastructure codebase clean, maintainable, and flexible.

## Consequences

### Positive:
- **Modularity and Clarity**: Each provider's infrastructure is self-contained, making it easy to understand, manage, and modify without affecting the others.
- **Flexibility**: Developers can easily deploy infrastructure for one, two, or all three clouds by simply changing variables. This is highly beneficial for focused development and testing.
- **Maintainability**: The separation of concerns simplifies debugging and updates. Changes to Azure resources, for example, are confined to the `azure/` module.
- **Scalability**: Adding a new cloud provider in the future is straightforward—it only requires creating a new module and adding a corresponding invocation in the root module.

### Negative:
- **Increased Boilerplate**: This approach requires passing variables from the root module down to the sub-modules, which can introduce a small amount of repetitive code.
- **Slightly Higher Complexity**: For those unfamiliar with Terraform modules, this structure is slightly more complex than a single flat file, though the benefits in organization outweigh this for a multi-cloud setup.
