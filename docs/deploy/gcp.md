---
title: GCP
hide:
---

## Keyper Configuration

To deploy to the GCP stack, first create GCP provider configuration in `lib/src/main/resources/app.yaml`:

```yaml
provider:
  gcp:
    accountId: "<projectId>"
    region: "<region>"
```

## Create Resource Admin Service Account

1. Create Service Account for Terraform:

    ```bash
    SERVICE=keyper
    PROJECT_ID=$(gcloud config get-value project)
    gcloud iam service-accounts create $SERVICE-cdktf-sa
    ```

2. Add `roles/cloudkms.admin` to the service account:

    ```bash
    SERVICE=keyper
    PROJECT_ID=$(gcloud config get-value project)
    gcloud projects add-iam-policy-binding $PROJECT_ID \
    --member "serviceAccount:$SERVICE-cdktf-sa@$PROJECT_ID.iam.gserviceaccount.com" \
    --role "roles/cloudkms.admin"
    ```

    You can also verify it by running:

    ```bash
    gcloud projects get-iam-policy $PROJECT_ID \
       --flatten="bindings[].members" \
       --filter="bindings.members:serviceAccount:$SERVICE-cdktf-sa@$PROJECT_ID.iam.gserviceaccount.com"
    ```

3. Add `roles/iam.serviceAccountAdmin` to the service account:

    ```bash
    SERVICE=keyper
    PROJECT_ID=$(gcloud config get-value project)
    gcloud projects add-iam-policy-binding $PROJECT_ID \
    --member "serviceAccount:$SERVICE-cdktf-sa@$PROJECT_ID.iam.gserviceaccount.com" \
    --role "roles/iam.serviceAccountAdmin"
    ```

4. Create and download the key:

    ```bash
    gcloud iam service-accounts keys create .cdktf-sa-key.json \
       --iam-account "$SERVICE-cdktf-sa@$PROJECT_ID.iam.gserviceaccount.com"
    ```
    **Make sure you don't commit `.cdktf-sa-key.json` to github.**

5. Set ENV `GOOGLE_CLOUD_KEYFILE_JSON` to path

    Your CI/CD pipeline will be able to use this role to create/delete GCP KMS resources.

## Add GCS Permission

If you are using [remote Terraform state backend](https://developer.hashicorp.com/terraform/tutorials/cloud/cloud-migrate), you will also need to add GCS permission to the role

```bash
SERVICE=keyper
PROJECT_ID=$(gcloud config get-value project)
gcloud projects add-iam-policy-binding $PROJECT_ID \
--member "serviceAccount:$SERVICE-cdktf-sa@$PROJECT_ID.iam.gserviceaccount.com" \
--role "roles/storage.objectUser"
```

If the bucket you plan on using for remote Terraform state doesn't exist yet, create it as well.

```bash
gcloud storage buckets create gs://keyper-tf-state --location=us-east1
```
