provider "google" {
  project = var.gcp_project_id
  region  = var.gcp_region
}

resource "google_storage_bucket" "log_bucket" {
  count = var.create_resources ? 1 : 0
  name  = var.bucket_name
  location = var.gcp_region
  
  force_destroy = true # Only for testing purposes
  uniform_bucket_level_access = true
}

resource "google_service_account" "app_sa" {
  count        = var.create_resources ? 1 : 0
  account_id   = "bff-log-service-sa"
  display_name = "BFF Log Service Account"
}

resource "google_storage_bucket_iam_member" "bucket_reader" {
  count  = var.create_resources ? 1 : 0
  bucket = google_storage_bucket.log_bucket[0].name
  role   = "roles/storage.legacyBucketReader"
  member = "serviceAccount:${google_service_account.app_sa[0].email}"
}

resource "google_storage_bucket_iam_member" "object_admin" {
  count  = var.create_resources ? 1 : 0
  bucket = google_storage_bucket.log_bucket[0].name
  role   = "roles/storage.objectAdmin"
  member = "serviceAccount:${google_service_account.app_sa[0].email}"
}

resource "google_service_account_key" "sa_key" {
  count              = var.create_resources ? 1 : 0
  service_account_id = google_service_account.app_sa[0].name
}