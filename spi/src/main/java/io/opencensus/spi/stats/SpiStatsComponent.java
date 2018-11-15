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

import io.opencensus.internal.Utils;
import io.opencensus.spi.stats.export.ExportComponent;
import io.opencensus.spi.stats.export.ViewManager;
import javax.annotation.concurrent.ThreadSafe;

/**
 * Class that holds the implementations for {@link ViewManager}.
 *
 * <p>All objects returned by methods on {@code StatsComponent} are cacheable.
 *
 * @since 0.8
 */
public abstract class SpiStatsComponent {

  public abstract ExportComponent getExportComponent();

  /**
   * Returns the current {@code StatsCollectionState}.
   *
   * <p>When no implementation is available, {@code getState} always returns {@link
   * StatsCollectionState#DISABLED}.
   *
   * <p>Once {@link #getState()} is called, subsequent calls to {@link
   * #setState(StatsCollectionState)} will throw an {@code IllegalStateException}.
   *
   * @return the current {@code StatsCollectionState}.
   * @since 0.8
   */
  public abstract StatsCollectionState getState();

  /**
   * Sets the current {@code StatsCollectionState}.
   *
   * <p>When no implementation is available, {@code setState} does not change the state.
   *
   * <p>If state is set to {@link StatsCollectionState#DISABLED}, all stats that are previously
   * recorded will be cleared.
   *
   * @param state the new {@code StatsCollectionState}.
   * @throws IllegalStateException if {@link #getState()} was previously called.
   * @deprecated This method is deprecated because other libraries could cache the result of {@link
   *     #getState()}, use a stale value, and behave incorrectly. It is only safe to call early in
   *     initialization. This method throws {@link IllegalStateException} after {@code getState()}
   *     has been called, in order to limit changes to the result of {@code getState()}.
   * @since 0.8
   */
  @Deprecated
  public abstract void setState(StatsCollectionState state);

  /**
   * Returns a {@code StatsComponent} that has a no-op implementation for {@link ExportComponent}.
   *
   * @return a {@code StatsComponent} that has a no-op implementation for {@code ExportComponent}.
   */
  static SpiStatsComponent newNoopSpiStatsComponent() {
    return new NoopSpiStatsComponent();
  }

  @ThreadSafe
  private static final class NoopSpiStatsComponent extends SpiStatsComponent {
    private final ExportComponent exportComponent = ExportComponent.newNoopExportComponent();
    private volatile boolean isRead;

    @Override
    public ExportComponent getExportComponent() {
      return exportComponent;
    }

    @Override
    public StatsCollectionState getState() {
      isRead = true;
      return StatsCollectionState.DISABLED;
    }

    @Override
    @Deprecated
    public void setState(StatsCollectionState state) {
      Utils.checkNotNull(state, "state");
      Utils.checkState(!isRead, "State was already read, cannot set state.");
    }
  }
}
