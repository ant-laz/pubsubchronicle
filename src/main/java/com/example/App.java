//  Copyright 2024 Google LLC
//
//  Licensed under the Apache License, Version 2.0 (the "License");
//  you may not use this file except in compliance with the License.
//  You may obtain a copy of the License at
//
//  http://www.apache.org/licenses/LICENSE-2.0
//
//  Unless required by applicable law or agreed to in writing, software
//  distributed under the License is distributed on an "AS IS" BASIS,
//  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//  See the License for the specific language governing permissions and
//  limitations under the License.

package com.example;

import com.google.cloud.pubsublite.proto.PubSubMessage;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.io.gcp.pubsub.PubsubIO;
import org.apache.beam.sdk.io.gcp.pubsub.PubsubMessage;
import org.apache.beam.sdk.options.Description;
import org.apache.beam.sdk.options.PipelineOptionsFactory;
import org.apache.beam.sdk.options.PipelineOptions;
import org.apache.beam.sdk.transforms.Create;
import org.apache.beam.sdk.transforms.DoFn;
import org.apache.beam.sdk.transforms.ParDo;
import org.apache.beam.sdk.values.PCollection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class App {

  // ---------   LOGGER ----------------------------------------------------------------------------
  // https://cloud.google.com/dataflow/docs/guides/logging
  // Instantiate Logger
  private static final Logger LOG = LoggerFactory.getLogger(App.class);

  // ---------   COMMAND LINE OPTIONS --------------------------------------------------------------
  // For custom command line options
  public interface MyAppOptions extends PipelineOptions {

    @Description("Input Pub/Sub subscription to read from")
    String getPubSubSubscription();

    void setPubSubSubscription(String subSubscription);
  }

  // ---------   DoFn ------------------------------------------------------------------------------
  static class TransformPubSubToChonicle extends DoFn<PubsubMessage, String> {

    @ProcessElement
    public void processElement(@Element PubsubMessage msg, OutputReceiver<String> out){
      // "@Element" is used by BeamSDK to pass each member of input PCollection to param "msg"
      // "OutputReceiver" is from BeamSDK and is what we populate with DoFns output, per element
      out.output(msg.toString());

      // Log
      //LOG.info("String representation of message = " + msg.toString());
      LOG.info("Payload of message = " + new String(msg.getPayload(), StandardCharsets.UTF_8 ));

    }
  }

  // ---------   Pipeline---------------------------------------------------------------------------
  public static void main(String[] args) {
    // Initialize the pipeline options
    PipelineOptionsFactory.register(MyAppOptions.class);
    MyAppOptions myOptions = PipelineOptionsFactory
        .fromArgs(args)
        .withValidation()
        .as(MyAppOptions.class);

    // create the main pipeline
    Pipeline pipeline = Pipeline.create(myOptions);


    // Read messages from Pub/Sub Subscription
    // As per https://cloud.google.com/pubsub/docs/publish-message-overview#about-messages
    // 1 Message in Pub/Sub has
    //   1 x message data
    //   1 x ordering key
    //   * x attributes
    //   1 x message ID
    //   1 x timestamp
    PCollection<PubsubMessage> msgs = pipeline.apply("ReadFromPubSubSubscription",
        PubsubIO.readMessagesWithAttributes().fromSubscription(myOptions.getPubSubSubscription()));

    // Get the payload of message from Pub/Sub
    PCollection<String> payloads = msgs.apply(ParDo.of(new TransformPubSubToChonicle()));

    //TODO::Transform Pub/Sub content into Chronicle format

    //TODO::output#1 - Write out to Pub/Sub with push subscription into Chronicle

    //TODO::output#2 - Write to Chronicle API

    //execute the pipeline
    pipeline.run().waitUntilFinish();
  }
}