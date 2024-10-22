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

import java.util.Arrays;
import java.util.List;

import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.coders.StringUtf8Coder;
import org.apache.beam.sdk.options.Default;
import org.apache.beam.sdk.options.Description;
import org.apache.beam.sdk.options.PipelineOptionsFactory;
import org.apache.beam.sdk.options.StreamingOptions;
import org.apache.beam.sdk.options.PipelineOptions;
import org.apache.beam.sdk.transforms.Create;
import org.apache.beam.sdk.transforms.DoFn;
import org.apache.beam.sdk.transforms.MapElements;
import org.apache.beam.sdk.transforms.ParDo;
import org.apache.beam.sdk.values.PCollection;
import org.apache.beam.sdk.values.TypeDescriptors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class App {
  // https://cloud.google.com/dataflow/docs/guides/logging
  // Instantiate Logger
  private static final Logger LOG = LoggerFactory.getLogger(App.class);

  // For custom command line options
  public interface MyAppOptions extends PipelineOptions {
    @Description("Input text")
    String getInputText();
    void setInputText(String value);
  }

  static class ComputeWordLengthFn extends DoFn<String, Integer> {
    @ProcessElement
    public void processElement(@Element String word, OutputReceiver<Integer> out) {
      // "@Element" is used by BeamSDK to pass each member of input PCollection to param "word"
      // "OutputReceiver" is from BeamSDK and is what we populate with DoFns output, per element
      out.output(word.length());

      // Demo of using Log
      LOG.info("Found word = " + word);
    }
  }

  public static void main(String[] args) {
    // Initialize the pipeline options
    PipelineOptionsFactory.register(MyAppOptions.class);
    MyAppOptions myOptions = PipelineOptionsFactory
        .fromArgs(args)
        .withValidation()
        .as(MyAppOptions.class);

    // create the main pipeline
    Pipeline pipeline = Pipeline.create(myOptions);

    // create an input PCollection
    PCollection<String> words = pipeline.apply(
        "Create words for input",
        Create.of(Arrays.asList("Hello", "World!", myOptions.getInputText()))
    );

    // calculate the length of each word
    words.apply(ParDo.of(new ComputeWordLengthFn()));

    //TODO::ReadInFromPub/Sub

    //TODO::TransformPub/Sub content into Chronicle format

    //TODO::output#1 - Write out to Pub/Sub with push subscription into Chronicle

    //TODO::output#2 - Write to Chronicle API

    //execute the pipeline
    pipeline.run().waitUntilFinish();
  }
}