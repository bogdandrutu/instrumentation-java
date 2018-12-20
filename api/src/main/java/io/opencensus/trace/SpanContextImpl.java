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

// NOTE: This would be moved to impl_core.
@Immutable
public final class SpanContextImpl extends SpanContext {
  private static final Tracestate TRACESTATE_DEFAULT = Tracestate.builder().build();
  private final TraceId traceId;
  private final SpanId spanId;

  /**
   * The invalid {@code SpanContext}.
   *
   * @since 0.5
   */
  public static final SpanContext INVALID =
      new SpanContextImpl(TraceId.INVALID, SpanId.INVALID, TraceOptions.DEFAULT, TRACESTATE_DEFAULT);

  /**
   * Creates a new {@code SpanContext} with the given identifiers and options.
   *
   * @param traceId the trace identifier of the span context.
   * @param spanId the span identifier of the span context.
   * @param traceOptions the trace options for the span context.
   * @return a new {@code SpanContext} with the given identifiers and options.
   * @deprecated use {@link #create(TraceId, SpanId, TraceOptions, Tracestate)}.
   */
  @Deprecated
  public static SpanContext create(TraceId traceId, SpanId spanId, TraceOptions traceOptions) {
    return create(traceId, spanId, traceOptions, TRACESTATE_DEFAULT);
  }

  /**
   * Creates a new {@code SpanContext} with the given identifiers and options.
   *
   * @param traceId the trace identifier of the span context.
   * @param spanId the span identifier of the span context.
   * @param traceOptions the trace options for the span context.
   * @param tracestate the trace state for the span context.
   * @return a new {@code SpanContext} with the given identifiers and options.
   * @since 0.16
   */
  public static SpanContext create(
      TraceId traceId, SpanId spanId, TraceOptions traceOptions, Tracestate tracestate) {
    return new SpanContextImpl(traceId, spanId, traceOptions, tracestate);
  }

  @Override
  public String toTraceId() {
      return traceId.toString(); // TODO: use a better value
  }

  @Override
  public String toSpanId() {
      return spanId.toString(); // TODO: same as above
  }

  /**
   * Returns the trace identifier associated with this {@code SpanContext}.
   *
   * @return the trace identifier associated with this {@code SpanContext}.
   * @since 0.5
   */
  public TraceId getTraceId() {
    return traceId;
  }

  /**
   * Returns the span identifier associated with this {@code SpanContext}.
   *
   * @return the span identifier associated with this {@code SpanContext}.
   * @since 0.5
   */
  public SpanId getSpanId() {
    return spanId;
  }

  /**
   * Returns true if this {@code SpanContext} is valid.
   *
   * @return true if this {@code SpanContext} is valid.
   * @since 0.5
   */
  @Override
  public boolean isValid() {
    return traceId.isValid() && spanId.isValid();
  }

  @Override
  public boolean equals(@Nullable Object obj) {
    if (obj == this) {
      return true;
    }

    if (!(obj instanceof SpanContextImpl)) {
      return false;
    }

    SpanContextImpl that = (SpanContextImpl) obj;
    return traceId.equals(that.traceId)
        && spanId.equals(that.spanId)
        && traceOptions.equals(that.traceOptions);
  }

  @Override
  public int hashCode() {
    return Arrays.hashCode(new Object[] {traceId, spanId, traceOptions});
  }

  @Override
  public String toString() {
    return "SpanContext{traceId="
        + traceId
        + ", spanId="
        + spanId
        + ", traceOptions="
        + traceOptions
        + "}";
  }

  private SpanContextImpl(
      TraceId traceId, SpanId spanId, TraceOptions traceOptions, Tracestate tracestate) {
    super(traceOptions, tracestate);
    this.traceId = traceId;
    this.spanId = spanId;
  }
}
