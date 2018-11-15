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

package io.opencensus.stats;

import static io.opencensus.stats.MeasureMap.newNoopMeasureMap;

import javax.annotation.concurrent.Immutable;

/**
 * Provides methods to record stats against tags.
 *
 * @since 0.8
 */
public abstract class StatsRecorder {
  // TODO(sebright): Should we provide convenience methods for only recording one measure?

  /**
   * Returns an object for recording multiple measurements.
   *
   * @return an object for recording multiple measurements.
   * @since 0.8
   */
  public abstract MeasureMap newMeasureMap();

  /**
   * Returns a {@code StatsRecorder} that does not record any data.
   *
   * @return a {@code StatsRecorder} that does not record any data.
   */
  static StatsRecorder getNoopStatsRecorder() {
    return NoopStatsRecorder.INSTANCE;
  }

  @Immutable
  private static final class NoopStatsRecorder extends StatsRecorder {
    static final StatsRecorder INSTANCE = new NoopStatsRecorder();

    @Override
    public MeasureMap newMeasureMap() {
      return newNoopMeasureMap();
    }
  }
}
