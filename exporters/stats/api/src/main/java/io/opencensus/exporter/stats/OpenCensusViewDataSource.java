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

package io.opencensus.exporter.stats;

import com.google.common.collect.Lists;
import io.opencensus.common.Function;
import io.opencensus.common.Functions;
import io.opencensus.common.Timestamp;
import io.opencensus.stats.View;
import io.opencensus.stats.View.AggregationWindow.Cumulative;
import io.opencensus.stats.ViewData.AggregationWindowData;
import io.opencensus.stats.ViewData.AggregationWindowData.CumulativeData;
import io.opencensus.stats.ViewData.AggregationWindowData.IntervalData;
import io.opencensus.stats.ViewManager;
import java.util.Collection;
import java.util.List;

public final class OpenCensusViewDataSource extends ViewDataSource {
  private static final String OPEN_CENSUS_NAME = "opencensus.io";
  private final ViewManager viewManager;

  public OpenCensusViewDataSource(ViewManager viewManager) {
    this.viewManager = viewManager;
  }

  @Override
  public String getName() {
    return OPEN_CENSUS_NAME;
  }

  @Override
  public Collection<ViewData> getViewData() {
    List</*@Nullable*/ ViewData> viewDataList = Lists.newArrayList();
    for (View view : viewManager.getAllExportedViews()) {
      if (isCumulativeView(view)) {
        io.opencensus.stats.ViewData viewData = viewManager.getView(view.getName());
        if (viewData != null) {
          Timestamp startTime = getStartTime(viewData.getWindowData());
          Timestamp endTime = getEndTime(viewData.getWindowData());
          // Only upload stats for valid views.
          viewDataList.add(
              ViewData.create(
                  ViewDescription.create(view), startTime, endTime, viewData.getAggregationMap()));
        }
      }
    }
    return viewDataList;
  }

  private static Timestamp getStartTime(AggregationWindowData aggregationWindowData) {
    return aggregationWindowData.match(
        new Function<CumulativeData, Timestamp>() {
          @Override
          public Timestamp apply(CumulativeData arg) {
            return arg.getStart();
          }
        },
        new Function<IntervalData, Timestamp>() {
          @Override
          public Timestamp apply(IntervalData arg) {
            // TODO(songya): we don't export IntervalData in this version.
            throw new IllegalArgumentException("IntervalData not supported");
          }
        },
        Functions.</*@Nullable*/ Timestamp>throwIllegalArgumentException());
  }

  private static Timestamp getEndTime(AggregationWindowData aggregationWindowData) {
    return aggregationWindowData.match(
        new Function<CumulativeData, Timestamp>() {
          @Override
          public Timestamp apply(CumulativeData arg) {
            return arg.getEnd();
          }
        },
        new Function<IntervalData, Timestamp>() {
          @Override
          public Timestamp apply(IntervalData arg) {
            // TODO(songya): we don't export IntervalData in this version.
            throw new IllegalArgumentException("IntervalData not supported");
          }
        },
        Functions.</*@Nullable*/ Timestamp>throwIllegalArgumentException());
  }

  private static boolean isCumulativeView(View view) {
    return view.getWindow() instanceof Cumulative;
  }
}
