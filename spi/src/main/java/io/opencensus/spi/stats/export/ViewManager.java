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

package io.opencensus.spi.stats.export;

import io.opencensus.common.Functions;
import io.opencensus.common.Timestamp;
import io.opencensus.internal.Utils;
import io.opencensus.spi.stats.export.View.Name;
import io.opencensus.tags.TagValue;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nullable;
import javax.annotation.concurrent.GuardedBy;
import javax.annotation.concurrent.ThreadSafe;

/**
 * Provides facilities to register {@link View}s for collecting stats and retrieving stats data as a
 * {@link ViewData}.
 *
 * @since 0.8
 */
public abstract class ViewManager {
  /**
   * Pull model for stats. Registers a {@link View} that will collect data to be accessed via {@link
   * #getView(View.Name)}.
   *
   * @param view the {@code View} to be registered.
   * @since 0.8
   */
  public abstract void registerView(View view);

  /**
   * Returns the current stats data, {@link ViewData}, associated with the given view name.
   *
   * <p>Returns {@code null} if the {@code View} is not registered.
   *
   * @param view the name of {@code View} for the current stats.
   * @return {@code ViewData} for the {@code View}, or {@code null} if the {@code View} is not
   *     registered.
   * @since 0.8
   */
  @Nullable
  public abstract ViewData getView(View.Name view);

  /**
   * Returns all registered views that should be exported.
   *
   * <p>This method should be used by any stats exporter that automatically exports data for views
   * registered with the {@link ViewManager}.
   *
   * @return all registered views that should be exported.
   * @since 0.9
   */
  public abstract Set<View> getAllExportedViews();

  /**
   * Returns a {@code ViewManager} that maintains a map of views, but always returns empty {@link
   * ViewData}s.
   *
   * @return a {@code ViewManager} that maintains a map of views, but always returns empty {@code
   *     ViewData}s.
   */
  static ViewManager newNoopViewManager() {
    return new NoopViewManager();
  }

  @ThreadSafe
  private static final class NoopViewManager extends ViewManager {
    private static final Timestamp ZERO_TIMESTAMP = Timestamp.create(0, 0);

    @GuardedBy("registeredViews")
    private final Map<Name, View> registeredViews = new HashMap<Name, View>();

    // Cached set of exported views. It must be set to null whenever a view is registered or
    // unregistered.
    @javax.annotation.Nullable private volatile Set<View> exportedViews;

    @Override
    public void registerView(View newView) {
      Utils.checkNotNull(newView, "newView");
      synchronized (registeredViews) {
        exportedViews = null;
        View existing = registeredViews.get(newView.getName());
        Utils.checkArgument(
            existing == null || newView.equals(existing),
            "A different view with the same name already exists.");
        if (existing == null) {
          registeredViews.put(newView.getName(), newView);
        }
      }
    }

    @Override
    @javax.annotation.Nullable
    @SuppressWarnings("deprecation")
    public ViewData getView(View.Name name) {
      Utils.checkNotNull(name, "name");
      synchronized (registeredViews) {
        View view = registeredViews.get(name);
        if (view == null) {
          return null;
        } else {
          return ViewData.create(
              view,
              Collections.<List</*@Nullable*/ TagValue>, AggregationData>emptyMap(),
              view.getWindow()
                  .match(
                      Functions.<ViewData.AggregationWindowData>returnConstant(
                          ViewData.AggregationWindowData.CumulativeData.create(
                              ZERO_TIMESTAMP, ZERO_TIMESTAMP)),
                      Functions.<ViewData.AggregationWindowData>returnConstant(
                          ViewData.AggregationWindowData.IntervalData.create(ZERO_TIMESTAMP)),
                      Functions.<ViewData.AggregationWindowData>throwAssertionError()));
        }
      }
    }

    @Override
    public Set<View> getAllExportedViews() {
      Set<View> views = exportedViews;
      if (views == null) {
        synchronized (registeredViews) {
          exportedViews = views = filterExportedViews(registeredViews.values());
        }
      }
      return views;
    }

    // Returns the subset of the given views that should be exported
    @SuppressWarnings("deprecation")
    private static Set<View> filterExportedViews(Collection<View> allViews) {
      Set<View> views = new HashSet<View>();
      for (View view : allViews) {
        if (view.getWindow() instanceof View.AggregationWindow.Interval) {
          continue;
        }
        views.add(view);
      }
      return Collections.unmodifiableSet(views);
    }
  }
}
