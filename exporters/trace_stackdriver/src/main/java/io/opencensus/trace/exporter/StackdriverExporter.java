/*
 * Copyright 2017, Google Inc.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.opencensus.trace.exporter;

import com.google.auth.Credentials;
import com.google.devtools.cloudtrace.v1.PatchTracesRequest;
import com.google.devtools.cloudtrace.v1.TraceServiceGrpc;
import com.google.devtools.cloudtrace.v1.TraceServiceGrpc.TraceServiceBlockingStub;
import com.google.devtools.cloudtrace.v1.Traces;
import io.grpc.Channel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.auth.MoreCallCredentials;
import io.opencensus.trace.export.SpanData;
import io.opencensus.trace.export.SpanExporter;
import io.opencensus.trace.export.SpanExporter.Handler;
import java.util.Collection;

/**
 * Implementation of the {@link SpanExporter.Handler} which exports all the exported {@link
 * SpanData} to Stackdriver Trace using the gRPC client.
 *
 * <p>Example of usage on Google Cloud VMs:
 *
 * <pre><code>
 *   public static void main(String[] args) {
 *     StackdriverExporter stackdriverExporter =
 *         StackdriverExporter.create(
 *             GoogleCredentials.getApplicationDefault(), "MyGoogleCloudProjectId");
 *     stackdriverExporter.register(Tracing.getExportComponent().getSpanExporter());
 *     ... // Do work.
 *   }
 * </code></pre>
 */
public final class StackdriverExporter extends Handler {
  private static final String REGISTER_NAME = StackdriverExporter.class.getName();
  private static final String API_HOST = "cloudtrace.googleapis.com";

  private final String projectId;
  private final TraceBuilderHelper traceBuilderHelper;
  private final TraceServiceBlockingStub traceServiceBlockingStub;

  /**
   * Returns a {@code StackdriverExporter} that sends sampled spans to the Stackdriver Trace API via
   * gRPC.
   *
   * @param credentials a credentials used to authenticate API calls.
   * @param projectId the cloud project id.
   * @return a {@code StackdriverExporter} that sends sampled spans to the Stackdriver Trace API via
   *     gRPC.
   */
  public static StackdriverExporter create(Credentials credentials, String projectId) {
    Channel channel = ManagedChannelBuilder.forTarget(API_HOST).build();
    TraceServiceBlockingStub traceServiceBlockingStub =
        TraceServiceGrpc.newBlockingStub(channel)
            .withCallCredentials(MoreCallCredentials.from(credentials));
    return new StackdriverExporter(traceServiceBlockingStub, projectId);
  }

  /**
   * Registers the {@code LoggingHandler} to the {@code SpanExporter}.
   *
   * @param spanExporter the instance of the {@code SpanExporter} where this service is registered.
   */
  public void register(SpanExporter spanExporter) {
    spanExporter.registerHandler(REGISTER_NAME, this);
  }

  /**
   * Unregisters the {@code LoggingHandler} from the {@code SpanExporter}.
   *
   * @param spanExporter the instance of the {@code SpanExporter} from where this service is
   *     unregistered.
   */
  public void unregister(SpanExporter spanExporter) {
    spanExporter.unregisterHandler(REGISTER_NAME);
  }

  @Override
  public void export(Collection<SpanData> spanDataList) {
    Traces.Builder tracesBuilder = Traces.newBuilder();
    for (SpanData spanData : spanDataList) {
      tracesBuilder.addTraces(traceBuilderHelper.generateTrace(spanData));
    }
    traceServiceBlockingStub.patchTraces(
        PatchTracesRequest.newBuilder()
            .setProjectId(projectId)
            .setTraces(tracesBuilder.build())
            .build());
  }

  private StackdriverExporter(TraceServiceBlockingStub traceServiceBlockingStub, String projectId) {
    this.projectId = projectId;
    this.traceBuilderHelper = new TraceBuilderHelper(projectId);
    this.traceServiceBlockingStub = traceServiceBlockingStub;
  }
}
