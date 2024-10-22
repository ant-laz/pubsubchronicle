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
import org.apache.beam.sdk.transforms.MapElements;
import org.apache.beam.sdk.values.PCollection;
import org.apache.beam.sdk.values.TypeDescriptors;

public class App {
  public interface MyOptions extends PipelineOptions {
    @Description("Input text to print.")
    @Default.String("My input text")
    String getInputText();
    void setInputText(String value);
  }

  public static PCollection<String> buildPipeline(Pipeline pipeline, String inputText) {
    return pipeline
        .apply("Create elements", Create.of(Arrays.asList("Hello", "World!", inputText)))
        .apply("Print elements",
            MapElements.into(TypeDescriptors.strings()).via(x -> {
              System.out.println(x);
              return x;
            }));
  }

  public static void main(String[] args) {
    PipelineOptions options = PipelineOptionsFactory.fromArgs(args).withValidation().as(MyOptions.class);
    Pipeline pipeline = Pipeline.create(options);
    App.buildPipeline(pipeline, "test");
    pipeline.run().waitUntilFinish();
  }
}