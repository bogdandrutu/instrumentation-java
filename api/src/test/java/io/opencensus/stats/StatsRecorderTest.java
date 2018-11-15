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

import static com.google.common.truth.Truth.assertThat;
import static io.opencensus.stats.MeasureMap.newNoopMeasureMap;
import static io.opencensus.stats.StatsRecorder.getNoopStatsRecorder;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Unit tests for {@link StatsRecorder}. */
@RunWith(JUnit4.class)
public final class StatsRecorderTest {

  @Test
  public void noopStatsRecorder_ReturnsNoopMeasureMap() {
    assertThat(getNoopStatsRecorder().newMeasureMap()).isInstanceOf(newNoopMeasureMap().getClass());
  }
}
