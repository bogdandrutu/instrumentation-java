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

import static com.google.common.base.Preconditions.checkArgument;

import com.google.auto.value.AutoValue;
import com.google.common.collect.Maps;
import io.opencensus.common.Function;
import io.opencensus.common.Functions;
import io.opencensus.common.Timestamp;
import io.opencensus.stats.Aggregation;
import io.opencensus.stats.Aggregation.Count;
import io.opencensus.stats.Aggregation.Distribution;
import io.opencensus.stats.Aggregation.Mean;
import io.opencensus.stats.Aggregation.Sum;
import io.opencensus.stats.AggregationData;
import io.opencensus.stats.AggregationData.CountData;
import io.opencensus.stats.AggregationData.DistributionData;
import io.opencensus.stats.AggregationData.MeanData;
import io.opencensus.stats.AggregationData.SumDataDouble;
import io.opencensus.stats.AggregationData.SumDataLong;
import io.opencensus.stats.Measure;
import io.opencensus.stats.Measure.MeasureDouble;
import io.opencensus.stats.Measure.MeasureLong;
import io.opencensus.tags.TagValue;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import javax.annotation.concurrent.Immutable;

/*>>>
import org.checkerframework.checker.nullness.qual.Nullable;
*/

/**
 * The aggregated data for a particular {@link ViewDescription}.
 *
 * @since 0.8
 */
@Immutable
@AutoValue
// Suppress Checker Framework warning about missing @Nullable in generated equals method.
@AutoValue.CopyAnnotations
@SuppressWarnings("nullness")
public abstract class ViewData {

  // Prevents this class from being subclassed anywhere else.
  ViewData() {}

  /**
   * Returns the {@code ViewDescription}.
   *
   * @return the {@code ViewDescription}.
   * @since 0.8
   */
  public abstract ViewDescription getViewDescription();

  /**
   * Returns the start {@code Timestamp}.
   *
   * @return the start {@code Timestamp}.
   * @since 0.8
   */
  public abstract Timestamp getStartTime();

  /**
   * Returns the end {@code Timestamp}.
   *
   * @return the end {@code Timestamp}.
   * @since 0.8
   */
  public abstract Timestamp getEndTime();

  /**
   * The {@link AggregationData} grouped by combination of tag values, associated with this {@link
   * ViewData}.
   *
   * @since 0.8
   */
  public abstract Map<List</*@Nullable*/ TagValue>, AggregationData> getAggregationMap();

  /**
   * Constructs a new {@link ViewData}.
   *
   * @param viewDescription the {@link ViewDescription} associated with this {@link
   *     io.opencensus.stats.ViewData}.
   * @param starTime the start time for these data.
   * @param endTime the end time for these data.
   * @param map the mapping from {@link TagValue} list to {@link AggregationData}.
   * @return a {@code ViewData}.
   * @throws IllegalArgumentException if the types of {@code Aggregation} and {@code
   *     AggregationData} don't match, or the types of {@code Window} and {@code WindowData} don't
   *     match.
   * @since 0.13
   */
  public static ViewData create(
      ViewDescription viewDescription,
      Timestamp starTime,
      Timestamp endTime,
      Map<? extends List</*@Nullable*/ TagValue>, ? extends AggregationData> map) {
    Map<List<TagValue>, AggregationData> deepCopy = Maps.newHashMap();
    for (Entry<? extends List<TagValue>, ? extends AggregationData> entry : map.entrySet()) {
      checkAggregation(
          viewDescription.getAggregation(), entry.getValue(), viewDescription.getMeasure());
      deepCopy.put(
          Collections.unmodifiableList(new ArrayList</*@Nullable*/ TagValue>(entry.getKey())),
          entry.getValue());
    }

    return new AutoValue_ViewData(
        viewDescription, starTime, endTime, Collections.unmodifiableMap(deepCopy));
  }

  private static void checkAggregation(
      final Aggregation aggregation, final AggregationData aggregationData, final Measure measure) {
    aggregation.match(
        new Function<Sum, Void>() {
          @Override
          public Void apply(Sum arg) {
            measure.match(
                new Function<MeasureDouble, Void>() {
                  @Override
                  public Void apply(MeasureDouble arg) {
                    checkArgument(
                        aggregationData instanceof SumDataDouble,
                        createErrorMessageForAggregation(aggregation, aggregationData));
                    return null;
                  }
                },
                new Function<MeasureLong, Void>() {
                  @Override
                  public Void apply(MeasureLong arg) {
                    checkArgument(
                        aggregationData instanceof SumDataLong,
                        createErrorMessageForAggregation(aggregation, aggregationData));
                    return null;
                  }
                },
                Functions.<Void>throwAssertionError());
            return null;
          }
        },
        new Function<Count, Void>() {
          @Override
          public Void apply(Count arg) {
            checkArgument(
                aggregationData instanceof CountData,
                createErrorMessageForAggregation(aggregation, aggregationData));
            return null;
          }
        },
        new Function<Mean, Void>() {
          @Override
          public Void apply(Mean arg) {
            checkArgument(
                aggregationData instanceof MeanData,
                createErrorMessageForAggregation(aggregation, aggregationData));
            return null;
          }
        },
        new Function<Distribution, Void>() {
          @Override
          public Void apply(Distribution arg) {
            checkArgument(
                aggregationData instanceof DistributionData,
                createErrorMessageForAggregation(aggregation, aggregationData));
            return null;
          }
        },
        Functions.<Void>throwAssertionError());
  }

  private static String createErrorMessageForAggregation(
      Aggregation aggregation, AggregationData aggregationData) {
    return "Aggregation and AggregationData types mismatch. "
        + "Aggregation: "
        + aggregation
        + " AggregationData: "
        + aggregationData;
  }
}
