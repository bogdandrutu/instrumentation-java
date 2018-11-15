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

package io.opencensus.spi.stats;

import static com.google.common.truth.Truth.assertThat;
import static io.opencensus.spi.stats.SpiStatsComponent.newNoopSpiStatsComponent;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Unit tests for {@link SpiStatsComponent}. */
@RunWith(JUnit4.class)
public final class SpiStatsComponentTest {
  @Rule public final ExpectedException thrown = ExpectedException.none();

  @Test
  public void getState() {
    assertThat(newNoopSpiStatsComponent().getState()).isEqualTo(StatsCollectionState.DISABLED);
  }

  @Test
  @SuppressWarnings("deprecation")
  public void setState_IgnoresInput() {
    SpiStatsComponent noopStatsComponent = newNoopSpiStatsComponent();
    noopStatsComponent.setState(StatsCollectionState.ENABLED);
    assertThat(noopStatsComponent.getState()).isEqualTo(StatsCollectionState.DISABLED);
  }

  @Test
  @SuppressWarnings("deprecation")
  public void setState_DisallowsNull() {
    SpiStatsComponent noopStatsComponent = newNoopSpiStatsComponent();
    thrown.expect(NullPointerException.class);
    noopStatsComponent.setState(null);
  }

  @Test
  @SuppressWarnings("deprecation")
  public void disallowsSetStateAfterGetState() {
    SpiStatsComponent noopStatsComponent = newNoopSpiStatsComponent();
    noopStatsComponent.setState(StatsCollectionState.DISABLED);
    noopStatsComponent.getState();
    thrown.expect(IllegalStateException.class);
    thrown.expectMessage("State was already read, cannot set state.");
    noopStatsComponent.setState(StatsCollectionState.ENABLED);
  }
}
