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

### Documentation

https://cloud.google.com/dataflow/docs/quickstarts/create-pipeline-java

### create a Google Cloud environment

environment variables 

See here for valid dataflow locations
https://cloud.google.com/dataflow/docs/resources/locations

```sh
export GCP_PROJECT_ID=$(gcloud config list core/project --format="value(core.project)")
export GCP_PROJECT_NUM=$(gcloud projects describe $GCP_PROJECT_ID --format="value(projectNumber)")
export CURRENT_USER=$(gcloud config list account --format "value(core.account)")
export GCP_BUCKET_REGION="US"
export GCP_DATAFLOW_REGION="us-east4"
export PROJECT_NAME="pubsubchronicle"
export GCS_BUCKET=gs://${GCP_PROJECT_ID}-${PROJECT_NAME}
export GCS_BUCKET_TMP=${GCS_BUCKET}/tmp/
export GCS_BUCKET_INPUT=${GCS_BUCKET}/input
export GCS_BUCKET_OUTPUT=${GCS_BUCKET}/output
export GCS_BUCKET_PUBSUB_INPUT=${GCS_BUCKET}/pubsubinput/
export PUB_SUB_TOPIC=projects/${GCP_PROJECT_ID}/topics/${PROJECT_NAME}
export PUB_SUB_SUBSCRIPTION=${PROJECT_NAME}-sub
export SUBSCRIPTION_ID=projects/${GCP_PROJECT_ID}/subscriptions/${PUB_SUB_SUBSCRIPTION}
```

enable apis
```shell
gcloud services enable dataflow compute_component logging storage_component storage_api bigquery pubsub datastore.googleapis.com cloudresourcemanager.googleapis.com
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
--pubSubSubscription=${SUBSCRIPTION_ID} \
--runner='DataflowRunner' \
--project=${GCP_PROJECT_ID} \
--region=${GCP_DATAFLOW_REGION} \
--tempLocation=${GCS_BUCKET_TMP}"
```

## Creating synthetic pub/sub messages

Create a pub/sub topic to contain our synthetic input data
```shell
gcloud pubsub topics create ${PUB_SUB_TOPIC}
```

Create a subscription to this topic
```shell
gcloud pubsub subscriptions create ${PUB_SUB_SUBSCRIPTION} --topic=${PUB_SUB_TOPIC}
```


this google provided template is used to create fake data

https://cloud.google.com/dataflow/docs/guides/templates/provided/streaming-data-generator#gcloud

the template requires that a schema for the synthetic data be provided

```shell
{
  "weatherSensorId": "{{uuid()}}",
  "sensorHealthy": {{bool()}},
  "temperatureCelcius": {{integer(-10,40)}},
  "location": {{random("Belfast","Edinburgh","Cardiff", "London")}},
  "weatherSensorIpAddress": {{ipv6()}}
}
```
upload schema to Google Cloud Storage

```shell
gcloud storage cp pubsubinput/synth_data_schema.json ${GCS_BUCKET_PUBSUB_INPUT}

```

kick off template
```shell
gcloud dataflow flex-template run gen-synth-weather-readings \
    --project=${GCP_PROJECT_ID} \
    --region=${GCP_DATAFLOW_REGION} \
    --template-file-gcs-location=gs://dataflow-templates-${GCP_DATAFLOW_REGION}/latest/flex/Streaming_Data_Generator \
    --parameters \
schemaLocation=${GCS_BUCKET_PUBSUB_INPUT}/syth_data_schema.json,\
qps=1,\
sinkType=PUBSUB,\
outputType=JSON,\
messagesLimit=10,\
topic=${PUB_SUB_TOPIC}
```