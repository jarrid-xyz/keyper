---
title: AWS
hide:
---

## Keyper Configuration

To deploy to the GCP stack, first create GCP provider configuration in `app.<env>.yaml`:

```yaml
provider:
  aws:
    accountId: <aws account id>
    region: <region>
    assume_role_arn: <assume role arn to use for tf aws provider (optional)>
```

Read more about Keyper Configuration [here](../configuration/index.md).

## Create Resource Admin IAM Role

### Pre-requisite

Companies or organizations usually has pre-configured aws profile setup. If you are running this locally, you can follow the https://docs.aws.amazon.com/IAM/latest/UserGuide/security-creds.html

1. Create IAM Role for Terraform

    You can easily add relevant resources and permissions via Terraform following this [spacelift guide](https://spacelift.io/blog/terraform-iam-role). However, here's a quick CLI way without Terraform.

    Create an `assume-role-policy.json`:

    ```json
    {
        "Version": "2012-10-17",
        "Statement": [
            {
            "Effect": "Allow",
            "Principal": {
                "Service": "ec2.amazonaws.com"
            },
            "Action": "sts:AssumeRole"
            }
        ]
    }
    ```


    ```bash
    SERVICE=keyper
    aws iam create-role \
        --role-name $SERVICE-cdktf-role \
        --assume-role-policy-document file://assume-role-policy.json
    ```

    Add assume role policy to current user. ***Note: this setup is for demo purpose. Your company or organization should have default CI/CD setup.***

    ```json
    {
        "Version": "2012-10-17",
        "Statement": [
            {
            "Effect": "Allow",
            "Principal": {
                "AWS": "arn:aws:iam::<ACCOUNT_ID>:root"
            },
            "Action": "sts:AssumeRole",
            "Condition": {
                "ArnLike": {
                    "aws:PrincipalArn": [
                        "arn:aws:iam::<ACCOUNT_ID>:user/<USER>",
                    ]
                }
            }
            }
        ]
    }
    ```


2. Add KMS Admin Permission

    Create a `kms-policy.json`:

    ```json
    {
        "Version": "2012-10-17",
        "Statement": [
            {
                "Effect": "Allow",
                "Action": [
                    "kms:*"
                ],
                "Resource": "*"
            }
        ]
    }
    ```


    ```bash
    SERVICE=keyper
    aws iam put-role-policy \
        --role-name $SERVICE-cdktf-role \
        --policy-name $SERVICE-cdktf-kms-policy \
        --policy-document file://kms-policy.json
    ```

3. Add IAM Admin Permission

    ```bash
    SERVICE=keyper
    aws iam attach-role-policy \
        --role-name $SERVICE-cdktf-role \
        --policy-arn arn:aws:iam::aws:policy/IAMFullAccess
    ```



## Add S3 Permission

See [here](https://developer.hashicorp.com/terraform/language/settings/backends/s3#s3-bucket-permissions){target=_blank} for more details. Create an IAM Policy: `s3-policy.json`:

```json
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Effect": "Allow",
      "Action": "s3:ListBucket",
      "Resource": "arn:aws:s3:::keyper-tf-state"
    },
    {
      "Effect": "Allow",
      "Action": ["s3:GetObject", "s3:PutObject"],
      "Resource": "arn:aws:s3:::keyper-tf-state/*"
    }
  ]
}
```

Grab the policy ARN and attach to the role.

```bash
SERVICE=keyper
aws iam put-role-policy \
    --role-name $SERVICE-cdktf-role \
    --policy-name $SERVICE-cdktf-backend-policy \
    --policy-document file://s3-policy.json
```

If the bucket you plan on using for remote Terraform state doesn't exists yet, create it as well.

```bash
aws s3api create-bucket \
    --bucket keyper-tf-state \
    --region us-east-1 \
    --object-ownership BucketOwnerEnforced
```
