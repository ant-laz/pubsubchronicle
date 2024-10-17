## Pipeline overview

source = Pub/Sub

sink = Chronicle

## Setup development environment

### install java

```shell
# Install sdkman.
curl -s "https://get.sdkman.io" | bash

# Make sure you have Java 17 installed.
sdk install java 17.0.5-tem
```

## Local development with beam runner

To run passing command line arguments.

```shell
gradle run --args="\
--inputText='some demo text' \
--runner='DirectRunner'"
```

## Deploying to Dataflow

### create a Google Cloud environment

environment variables 

```sh
export GCP_PROJECT_ID=$(gcloud config list core/project --format="value(core.project)")
export GCP_PROJECT_NUM=$(gcloud projects describe $GCP_PROJECT_ID --format="value(projectNumber)")
export GCP_BUCKET_REGION="US"
export GCP_DATAFLOW_REGION="us-east4"
export GCS_BUCKET=gs://${GCP_PROJECT_ID}-pubsubchronicle
export GCS_BUCKET_TMP=${GCS_BUCKET}/tmp/
export GCS_BUCKET_INPUT=${GCS_BUCKET}/input
export GCS_BUCKET_OUTPUT=${GCS_BUCKET}/output
export EMAIL_ADDRESS=your_email@domain.com
```

create a bucket
```shell
gcloud storage buckets create ${GCS_BUCKET} \
  --project=${GCP_PROJECT_ID} \
  --location=${GCP_BUCKET_REGION} \
  --uniform-bucket-level-access

gcloud storage buckets add-iam-policy-binding ${GCS_BUCKET} \
--member=allUsers \
--role=roles/storage.objectViewer

```

```shell
gradle run --args="\
--inputText='some demo text' \
--runner='DataflowRunner' \
--project=<YOUR_GCP_PROJECT_ID> \-
-region=<GCP_REGION> \
--tempLocation=gs://<YOUR_GCS_BUCKET>/temp/ \
--output=gs://<YOUR_GCS_BUCKET>/output"
```
