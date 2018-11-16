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

package io.opencensus.spi.trace;

import static com.google.common.truth.Truth.assertThat;

import io.opencensus.spi.trace.config.TraceConfig;
import io.opencensus.spi.trace.export.ExportComponent;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Unit tests for {@link SpiTracing}. */
@RunWith(JUnit4.class)
public class SpiTracingTest {
  @Rule public ExpectedException thrown = ExpectedException.none();

  @Test
  public void loadTraceComponent_UsesProvidedClassLoader() {
    final RuntimeException toThrow = new RuntimeException("UseClassLoader");
    thrown.expect(RuntimeException.class);
    thrown.expectMessage("UseClassLoader");
    SpiTracing.loadTraceComponent(
        new ClassLoader() {
          @Override
          public Class<?> loadClass(String name) {
            throw toThrow;
          }
        });
  }

  @Test
  public void loadTraceComponent_IgnoresMissingClasses() {
    ClassLoader classLoader =
        new ClassLoader() {
          @Override
          public Class<?> loadClass(String name) throws ClassNotFoundException {
            throw new ClassNotFoundException();
          }
        };
    assertThat(SpiTracing.loadTraceComponent(classLoader).getClass().getName())
        .isEqualTo("io.opencensus.spi.trace.SpiTraceComponent$NoopSpiTraceComponent");
  }

  @Test
  public void defaultTraceExporter() {
    assertThat(SpiTracing.getExportComponent())
        .isInstanceOf(ExportComponent.newNoopExportComponent().getClass());
  }

  @Test
  public void defaultTraceConfig() {
    assertThat(SpiTracing.getTraceConfig()).isSameAs(TraceConfig.getNoopTraceConfig());
  }
}
