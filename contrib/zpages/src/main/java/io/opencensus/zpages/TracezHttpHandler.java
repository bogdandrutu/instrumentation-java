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

package io.opencensus.zpages;

import com.google.common.collect.ImmutableMap;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import io.opencensus.common.Scope;
import io.opencensus.trace.AttributeValue;
import io.opencensus.trace.EndSpanOptions;
import io.opencensus.trace.NetworkEvent;
import io.opencensus.trace.NetworkEvent.Type;
import io.opencensus.trace.Sampler;
import io.opencensus.trace.Status.CanonicalCode;
import io.opencensus.trace.Tracer;
import io.opencensus.trace.Tracing;
import io.opencensus.trace.export.ExportComponent;
import io.opencensus.trace.samplers.Samplers;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/** TODO(bdrutu): Document the page. */
public final class TracezHttpHandler implements HttpHandler {
  private static final Tracer tracer = Tracing.getTracer();
  private static final String HTTP_SERVER_SPAN_NAME = "HttpServer/tracez";
  private final TracezPageFormatter pageFormatter;
  private final Sampler sampler = Samplers.probabilitySampler(0.1);

  /** Constructs a new {@code TracezHttpHandler}. */
  public TracezHttpHandler() {
    ExportComponent exportComponent = Tracing.getExportComponent();
    this.pageFormatter =
        new TracezPageFormatter(
            exportComponent.getRunningSpanStore(), exportComponent.getSampledSpanStore());
    Tracing.getExportComponent()
        .getSampledSpanStore()
        .registerSpanNamesForCollection(Arrays.asList(HTTP_SERVER_SPAN_NAME));
  }

  @Override
  public void handle(HttpExchange httpExchange) throws IOException {
    long nanos = System.nanoTime();
    try (Scope ss =
        tracer
            .spanBuilderWithExplicitParent(HTTP_SERVER_SPAN_NAME, null)
            .setRecordEvents(true)
            .setSampler(sampler)
            .startScopedSpan()) {
      long nanos2 = System.nanoTime();
      tracer
          .getCurrentSpan()
          .addNetworkEvent(
              NetworkEvent.builder(Type.RECV, 1)
                  .setMessageSize(httpExchange.getRequestBody().available())
                  .build());
      long nanos3 = System.nanoTime();
      tracer
          .getCurrentSpan()
          .addAnnotation(
              "Finished adding NE",
              ImmutableMap.<String, AttributeValue>builder()
                  .put("CreateSpan", AttributeValue.longAttributeValue(nanos2 - nanos))
                  .put("AddNE", AttributeValue.longAttributeValue(nanos3 - nanos2))
                  .put("AddThis", AttributeValue.longAttributeValue(System.nanoTime() - nanos3))
                  .build());
      tracer
          .getCurrentSpan()
          .addAttributes(
              ImmutableMap.<String, AttributeValue>builder()
                  .put(
                      "RequestMethod",
                      AttributeValue.stringAttributeValue(httpExchange.getRequestMethod()))
                  .build());
      httpExchange.sendResponseHeaders(200, 0);
      pageFormatter.emitHTML(queryToMap(httpExchange), httpExchange.getResponseBody());
      httpExchange.close();
      tracer.getCurrentSpan().addNetworkEvent(NetworkEvent.builder(Type.SENT, 1).build());
      if (nanos % 27 == 0) {
        tracer
            .getCurrentSpan()
            .end(
                EndSpanOptions.builder()
                    .setStatus(CanonicalCode.values()[(int) (nanos % 15)].toStatus())
                    .build());
      }
    }
  }

  private static Map<String, String> queryToMap(HttpExchange httpExchange) {
    String query = httpExchange.getRequestURI().getQuery();
    if (query == null) {
      return Collections.emptyMap();
    }
    Map<String, String> result = new HashMap<String, String>();
    for (String param : query.split("&")) {
      String[] pair = param.split("=");
      if (pair.length > 1) {
        result.put(pair[0], pair[1]);
      } else {
        result.put(pair[0], "");
      }
    }
    tracer.getCurrentSpan().addAnnotation("Finish to parse query components.");
    return result;
  }
}
