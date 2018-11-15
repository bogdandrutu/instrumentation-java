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

package io.opencensus.spi.stats.export;

import static com.google.common.truth.Truth.assertThat;
import static io.opencensus.spi.stats.export.ViewManager.newNoopViewManager;

import io.opencensus.common.Duration;
import io.opencensus.common.Timestamp;
import io.opencensus.spi.stats.export.Aggregation.Sum;
import io.opencensus.spi.stats.export.View.AggregationWindow.Cumulative;
import io.opencensus.spi.stats.export.View.AggregationWindow.Interval;
import io.opencensus.spi.stats.export.View.Name;
import io.opencensus.spi.stats.export.ViewData.AggregationWindowData.CumulativeData;
import io.opencensus.spi.stats.export.ViewData.AggregationWindowData.IntervalData;
import io.opencensus.stats.Measure;
import io.opencensus.stats.Measure.MeasureDouble;
import io.opencensus.tags.TagKey;
import java.util.Arrays;
import java.util.Set;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Unit tests for {@link io.opencensus.spi.stats.export.ViewManager}. */
@RunWith(JUnit4.class)
public final class ViewManagerTest {
  private static final MeasureDouble MEASURE =
      Measure.MeasureDouble.create("my measure", "description", "s");
  private static final TagKey KEY = TagKey.create("KEY");
  private static final Name VIEW_NAME = Name.create("my view");
  private static final String VIEW_DESCRIPTION = "view description";
  private static final Sum AGGREGATION = Sum.create();
  private static final Cumulative CUMULATIVE = Cumulative.create();
  private static final Duration TEN_SECONDS = Duration.create(10, 0);
  private static final Interval INTERVAL = Interval.create(TEN_SECONDS);

  @Rule public final ExpectedException thrown = ExpectedException.none();

  @Test
  public void registerView_DisallowRegisteringDifferentViewWithSameName() {
    final View view1 =
        View.create(
            VIEW_NAME, "description 1", MEASURE, AGGREGATION, Arrays.asList(KEY), CUMULATIVE);
    final View view2 =
        View.create(
            VIEW_NAME, "description 2", MEASURE, AGGREGATION, Arrays.asList(KEY), CUMULATIVE);
    ViewManager viewManager = newNoopViewManager();
    viewManager.registerView(view1);

    try {
      thrown.expect(IllegalArgumentException.class);
      thrown.expectMessage("A different view with the same name already exists.");
      viewManager.registerView(view2);
    } finally {
      assertThat(viewManager.getView(VIEW_NAME).getView()).isEqualTo(view1);
    }
  }

  @Test
  public void registerView_AllowRegisteringSameViewTwice() {
    View view =
        View.create(
            VIEW_NAME, VIEW_DESCRIPTION, MEASURE, AGGREGATION, Arrays.asList(KEY), CUMULATIVE);
    ViewManager viewManager = newNoopViewManager();
    viewManager.registerView(view);
    viewManager.registerView(view);
  }

  @Test
  public void registerView_DisallowNull() {
    ViewManager viewManager = newNoopViewManager();
    thrown.expect(NullPointerException.class);
    viewManager.registerView(null);
  }

  @Test
  public void getView_GettingNonExistentViewReturnsNull() {
    ViewManager viewManager = newNoopViewManager();
    assertThat(viewManager.getView(VIEW_NAME)).isNull();
  }

  @Test
  public void getView_Cumulative() {
    View view =
        View.create(
            VIEW_NAME, VIEW_DESCRIPTION, MEASURE, AGGREGATION, Arrays.asList(KEY), CUMULATIVE);
    ViewManager viewManager = newNoopViewManager();
    viewManager.registerView(view);

    ViewData viewData = viewManager.getView(VIEW_NAME);
    assertThat(viewData.getView()).isEqualTo(view);
    assertThat(viewData.getAggregationMap()).isEmpty();
    assertThat(viewData.getStart()).isEqualTo(Timestamp.create(0, 0));
    assertThat(viewData.getEnd()).isEqualTo(Timestamp.create(0, 0));
    assertThat(viewData.getWindowData())
        .isEqualTo(CumulativeData.create(Timestamp.create(0, 0), Timestamp.create(0, 0)));
  }

  @Test
  public void getView_Interval() {
    View view =
        View.create(
            VIEW_NAME, VIEW_DESCRIPTION, MEASURE, AGGREGATION, Arrays.asList(KEY), INTERVAL);
    ViewManager viewManager = newNoopViewManager();
    viewManager.registerView(view);

    ViewData viewData = viewManager.getView(VIEW_NAME);
    assertThat(viewData.getView()).isEqualTo(view);
    assertThat(viewData.getAggregationMap()).isEmpty();
    assertThat(viewData.getWindowData()).isEqualTo(IntervalData.create(Timestamp.create(0, 0)));
  }

  @Test
  public void getView_DisallowNull() {
    ViewManager viewManager = newNoopViewManager();
    thrown.expect(NullPointerException.class);
    viewManager.getView(null);
  }

  @Test
  public void getAllExportedViews() {
    ViewManager viewManager = newNoopViewManager();
    assertThat(viewManager.getAllExportedViews()).isEmpty();
    View cumulativeView1 =
        View.create(
            View.Name.create("View 1"),
            VIEW_DESCRIPTION,
            MEASURE,
            AGGREGATION,
            Arrays.asList(KEY),
            CUMULATIVE);
    View cumulativeView2 =
        View.create(
            View.Name.create("View 2"),
            VIEW_DESCRIPTION,
            MEASURE,
            AGGREGATION,
            Arrays.asList(KEY),
            CUMULATIVE);
    View intervalView =
        View.create(
            View.Name.create("View 3"),
            VIEW_DESCRIPTION,
            MEASURE,
            AGGREGATION,
            Arrays.asList(KEY),
            INTERVAL);
    viewManager.registerView(cumulativeView1);
    viewManager.registerView(cumulativeView2);
    viewManager.registerView(intervalView);

    // Only cumulative views should be exported.
    assertThat(viewManager.getAllExportedViews()).containsExactly(cumulativeView1, cumulativeView2);
  }

  @Test
  public void getAllExportedViews_ResultIsUnmodifiable() {
    ViewManager viewManager = newNoopViewManager();
    View view1 =
        View.create(
            View.Name.create("View 1"), VIEW_DESCRIPTION, MEASURE, AGGREGATION, Arrays.asList(KEY));
    viewManager.registerView(view1);
    Set<View> exported = viewManager.getAllExportedViews();

    View view2 =
        View.create(
            View.Name.create("View 2"), VIEW_DESCRIPTION, MEASURE, AGGREGATION, Arrays.asList(KEY));
    thrown.expect(UnsupportedOperationException.class);
    exported.add(view2);
  }
}
