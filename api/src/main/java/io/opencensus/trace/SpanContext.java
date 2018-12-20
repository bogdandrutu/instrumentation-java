/*
 * Copyright 2016-17, OpenCensus Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.opencensus.trace;

import java.util.Arrays;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

/**
 * A class that represents a span context. A span context contains the state that must propagate to
 * child {@link Span}s and across process boundaries. It contains the identifiers (a {@link TraceId
 * trace_id} and {@link SpanId span_id}) associated with the {@link Span} and a set of {@link
 * TraceOptions options}.
 *
 * @since 0.5
 */
@Immutable
public abstract class SpanContext {
  protected final TraceOptions traceOptions;
  protected final Tracestate tracestate;

  public abstract String toTraceId();

  public abstract String toSpanId();

  public abstract boolean isValid();

  /**
   * Returns the {@code TraceOptions} associated with this {@code SpanContext}.
   *
   * @return the {@code TraceOptions} associated with this {@code SpanContext}.
   * @since 0.5
   */
  public TraceOptions getTraceOptions() {
    return traceOptions;
  }

  /**
   * Returns the {@code Tracestate} associated with this {@code SpanContext}.
   *
   * @return the {@code Tracestate} associated with this {@code SpanContext}.
   * @since 0.5
   */
  public Tracestate getTracestate() {
    return tracestate;
  }

  protected SpanContext(TraceOptions traceOptions, Tracestate tracestate)
  {
      this.traceOptions = traceOptions;
      this.tracestate = tracestate;
  }
}
