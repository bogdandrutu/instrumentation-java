/*
 * Copyright 2017, OpenCensus Authors
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

package io.opencensus.impllite.trace;

import io.opencensus.implcore.trace.TraceComponentImplBase;
import io.opencensus.spi.trace.SpiTraceComponent;
import io.opencensus.spi.trace.config.TraceConfig;
import io.opencensus.spi.trace.export.ExportComponent;

/** Android-compatible implementation of the {@link SpiTraceComponent}. */
public final class SpiTraceComponentImplLite extends SpiTraceComponent {
  private final TraceComponentImplBase traceComponentImplBase =
      TraceComponentImplBaseInstance.getInstance();

  /** Public constructor to be used with reflection loading. */
  public SpiTraceComponentImplLite() {}

  @Override
  public ExportComponent getExportComponent() {
    return traceComponentImplBase.getExportComponent();
  }

  @Override
  public TraceConfig getTraceConfig() {
    return traceComponentImplBase.getTraceConfig();
  }
}
