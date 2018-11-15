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

package io.opencensus.implcore.stats;

import com.google.common.base.Preconditions;
import io.opencensus.implcore.internal.CurrentState.State;
import io.opencensus.metrics.Metrics;
import io.opencensus.metrics.export.MetricProducer;
import io.opencensus.spi.stats.SpiStatsComponent;
import io.opencensus.spi.stats.StatsCollectionState;
import io.opencensus.spi.stats.export.ExportComponent;
import io.opencensus.spi.stats.export.ViewManager;

/** Base implementation of {@link SpiStatsComponent}. */
public class SpiStatsComponentImplBase extends SpiStatsComponent {
  private final StatsManager statsManager;
  private final ViewManagerImpl viewManager;
  private final ExportComponent exportComponent;

  /** Creates a new {@code SpiStatsComponentImplBase}. */
  public SpiStatsComponentImplBase(StatsManager statsManager) {
    this.statsManager = statsManager;
    this.viewManager = new ViewManagerImpl(statsManager);
    exportComponent =
        new ExportComponent() {
          @Override
          public ViewManager getViewManager() {
            return viewManager;
          }
        };

    // Create a new MetricProducerImpl and register it to MetricProducerManager when
    // SpiStatsComponentImplBase is initialized.
    MetricProducer metricProducer = new MetricProducerImpl(statsManager);
    Metrics.getExportComponent().getMetricProducerManager().add(metricProducer);
  }

  @Override
  public ExportComponent getExportComponent() {
    return exportComponent;
  }

  @Override
  public StatsCollectionState getState() {
    return stateToStatsState(statsManager.getCurrentState().get());
  }

  @Override
  @SuppressWarnings("deprecation")
  public synchronized void setState(StatsCollectionState newState) {
    boolean stateChanged =
        statsManager
            .getCurrentState()
            .set(statsStateToState(Preconditions.checkNotNull(newState, "newState")));
    if (stateChanged) {
      if (newState == StatsCollectionState.DISABLED) {
        viewManager.clearStats();
      } else {
        viewManager.resumeStatsCollection();
      }
    }
  }

  private static State statsStateToState(StatsCollectionState statsCollectionState) {
    return statsCollectionState == StatsCollectionState.ENABLED ? State.ENABLED : State.DISABLED;
  }

  private static StatsCollectionState stateToStatsState(State state) {
    return state == State.ENABLED ? StatsCollectionState.ENABLED : StatsCollectionState.DISABLED;
  }
}
