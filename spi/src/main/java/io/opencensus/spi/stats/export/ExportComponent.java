/*
 * Copyright 2018, OpenCensus Authors
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

package io.opencensus.spi.stats.export;

import static io.opencensus.spi.stats.export.ViewManager.newNoopViewManager;

import javax.annotation.concurrent.ThreadSafe;

public abstract class ExportComponent {
  /**
   * Returns the default {@link ViewManager}.
   *
   * @since 0.8
   */
  public abstract ViewManager getViewManager();

  /**
   * Returns a {@code StatsComponent} that has a no-op implementation for {@link ViewManager}.
   *
   * @return a {@code StatsComponent} that has a no-op implementation for {@code ViewManager}.
   */
  public static ExportComponent newNoopExportComponent() {
    return new NoopExportComponent();
  }

  @ThreadSafe
  private static final class NoopExportComponent extends ExportComponent {
    private final ViewManager viewManager = newNoopViewManager();

    @Override
    public ViewManager getViewManager() {
      return viewManager;
    }
  }
}
