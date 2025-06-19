# Bucket Manager Infrastructure Setup

## Overview

This project is a backend-for-frontend (BFF) service designed to provide secure, controlled access to log files stored in object storage across multiple cloud providers: AWS S3, Google Cloud Storage (GCS), and Azure Blob Storage.

It exposes a RESTful API for listing and downloading log files, with a focus on security and modularity. The cloud infrastructure (buckets, service accounts, permissions) is managed entirely through Terraform, allowing for repeatable and version-controlled deployments.

## Prerequisites

Before you begin, ensure you have the following tools installed on your local machine:

* [Terraform](https://learn.hashicorp.com/tutorials/terraform/install-cli) (v1.0.0 or newer)
* [AWS CLI](https://aws.amazon.com/cli/) (v2 or newer)
* [Google Cloud CLI (gcloud)](https://cloud.google.com/sdk/docs/install)
* [Azure CLI (az)](https://docs.microsoft.com/cli/azure/install-azure-cli)
* Java JDK 21+

## Infrastructure Setup with Terraform

The entire cloud infrastructure is managed via Terraform. The following steps will guide you through authenticating with each cloud provider and deploying the necessary resources.

### 1. Cloud Authentication Setup

Terraform needs to authenticate with each cloud provider to manage resources. You must log in using the respective CLI for each cloud you intend to use. These commands link your local machine to your cloud accounts.

#### AWS CLI

The AWS CLI uses long-lived credentials from an IAM user.

1. Log in to your AWS Console and create an IAM user with administrative permissions.
2. Generate an Access Key and Secret Access Key for that user.
3. Configure the AWS CLI with these credentials by running:

    ```bash
    aws configure
    ```

4. You will be prompted to enter your keys, a default region, and output format.

    ```
    AWS Access Key ID [None]: xxxxx
    AWS Secret Access Key [None]: xxxxx
    Default region name [None]: us-east-1
    Default output format [None]: json
    ```

    Terraform will automatically use these credentials.

#### Google Cloud CLI

The gcloud CLI uses your personal Google account for authentication, which is ideal for local development.

1. Run the following command:

    ```bash
    gcloud auth application-default login
    ```

2. A browser window will open. Log in to the Google account that has permissions for your GCP project.
3. Terraform will automatically use these credentials.

#### Azure CLI

The Azure CLI also uses a browser-based login.

1. Run the following command:

    ```bash
    az login
    ```

2. A browser window will open. Log in to your Microsoft account associated with your Azure subscription.
3. If you have multiple subscriptions, ensure you select the correct one:

    ```bash
    # List all subscriptions
    az account list --output table

    # Set the active subscription
    az account set --subscription="YOUR_SUBSCRIPTION_ID"
    ```

    Terraform will automatically use your active login session.

### 2. Terraform Configuration

All Terraform commands should be run from the `terraform/` directory of this project.

#### Create the `terraform.tfvars` file

Terraform uses a `terraform.tfvars` file to set variable values, especially secrets or project-specific identifiers that should not be committed to version control.

1. Navigate to the `terraform/` directory.
2. Create a new file named `terraform.tfvars`.
3. Copy the content below into your new file and **replace the placeholder values** with your own.

    ```hcl
    # ----------------- REQUIRED -----------------
    # You MUST provide your GCP Project ID here.
    gcp_project_id = "your-gcp-project-id-here"


    # ----------------- OPTIONAL -----------------
    # You can override the default bucket names or regions here if you wish.
    # The default names are designed to be unique, but you can customize them.

    # aws_bucket_name = "my-custom-aws-bucket-12345"
    # gcp_bucket_name = "my-custom-gcp-bucket-12345"
    # azure_storage_account_name = "mycustomstorageacc12345"
    ```

#### Enabling and Disabling Providers

You can control which cloud providers' resources are created by setting flags in your `terraform.tfvars` file. By default, all are enabled (`true`).

To disable the creation of resources for a specific provider, set its `create_*_resources` variable to `false`.

**Example:** To deploy resources only for AWS and disable GCP and Azure, your `terraform.tfvars` file would look like this:

```hcl
gcp_project_id = "your-gcp-project-id-here"

# Disable resource creation for GCP and Azure
create_gcp_resources   = false
create_azure_resources = false
```

### 3. Deploying the Infrastructure

With authentication and configuration complete, you can now deploy the infrastructure.

1. **Initialize Terraform:**
    This command downloads the necessary provider plugins. Run it once per project.

    ```bash
    cd terraform/
    terraform init
    ```

2. **Plan the Deployment:**
    This command shows you a "dry run" of all the resources Terraform will create, change, or destroy. It's a good practice to review the plan before applying it.

    ```bash
    terraform plan
    ```

3. **Apply the Deployment:**
    This command executes the plan and creates the resources in your cloud accounts.

    ```bash
    terraform apply
    ```

    Terraform will show you the plan again and ask for confirmation. Type `yes` and press Enter to proceed.

### 4. How to Obtain the Credentials

Once `terraform apply` completes successfully, it will print all the output values to your terminal. These are the credentials and resource names you need for your Spring Boot application.

If you need to view the outputs again later, you can run:

```bash
terraform output
```

To view a specific sensitive value, such as a key, you can target it directly:

```bash
# Example for the AWS secret key
terraform output -raw aws_secret_access_key
```

The outputs will be used to populate the `application.yml` or environment variables for the Java application.

## 5. Configuring the Spring Boot Application

Take the credentials from the Terraform output and use them to configure the `src/main/resources/application.yml` file. 

## 6. Cleaning Up

To avoid incurring costs, you should destroy all the created cloud resources when you are finished.

1. Navigate to the `terraform/` directory.
2. Run the destroy command:

    ```bash
    terraform destroy
    ```

3. Terraform will show you all the resources that will be deleted. Type `yes` to confirm and permanently remove the infrastructure.
