provider "aws" {
  region = var.aws_region
}

resource "aws_s3_bucket" "log_bucket" {
  count  = var.create_resources ? 1 : 0
  bucket = var.bucket_name
}

resource "aws_s3_bucket_public_access_block" "log_bucket_pab" {
  count  = var.create_resources ? 1 : 0
  bucket = aws_s3_bucket.log_bucket[0].id

  block_public_acls       = true
  block_public_policy     = true
  ignore_public_acls      = true
  restrict_public_buckets = true
}

resource "aws_iam_user" "app_user" {
  count = var.create_resources ? 1 : 0
  name  = "bff-log-service-user"
}

resource "aws_iam_access_key" "app_user_key" {
  count = var.create_resources ? 1 : 0
  user  = aws_iam_user.app_user[0].name
}

resource "aws_iam_user_policy" "app_user_policy" {
  count = var.create_resources ? 1 : 0
  name  = "bff-log-service-s3-rw-policy"
  user  = aws_iam_user.app_user[0].name

  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Effect = "Allow",
        Action = [
          "s3:ListBucket"
        ],
        Resource = [
          aws_s3_bucket.log_bucket[0].arn
        ]
      },
      {
        Effect = "Allow",
        Action = [
          "s3:GetObject",
          "s3:PutObject",
          "s3:DeleteObject"
        ],
        Resource = [
          "${aws_s3_bucket.log_bucket[0].arn}/*"
        ]
      }
    ]
  })
}