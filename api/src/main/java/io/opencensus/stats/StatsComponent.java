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

import static io.opencensus.stats.StatsRecorder.getNoopStatsRecorder;

import javax.annotation.concurrent.ThreadSafe;

/**
 * Class that holds the implementations for {@link StatsRecorder}.
 *
 * <p>All objects returned by methods on {@code StatsComponent} are cacheable.
 *
 * @since 0.8
 */
public abstract class StatsComponent {
  /**
   * Returns the default {@link StatsRecorder}.
   *
   * @since 0.8
   */
  public abstract StatsRecorder getStatsRecorder();

  /**
   * Returns a {@code StatsComponent} that has a no-op implementation for {@link StatsRecorder}.
   *
   * @return a {@code StatsComponent} that has a no-op implementation for {@code StatsRecorder}.
   */
  protected static StatsComponent newNoopStatsComponent() {
    return new NoopStatsComponent();
  }

  @ThreadSafe
  private static final class NoopStatsComponent extends StatsComponent {
    @Override
    public StatsRecorder getStatsRecorder() {
      return getNoopStatsRecorder();
    }
  }
}
