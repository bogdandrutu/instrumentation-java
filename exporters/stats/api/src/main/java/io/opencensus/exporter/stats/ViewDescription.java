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

import com.google.auto.value.AutoValue;
import io.opencensus.stats.Aggregation;
import io.opencensus.stats.Measure;
import io.opencensus.stats.View;
import io.opencensus.stats.View.Name;
import io.opencensus.tags.TagKey;
import java.util.List;
import javax.annotation.concurrent.Immutable;

/*>>>
import org.checkerframework.checker.nullness.qual.Nullable;
*/

/**
 * The aggregated data for a particular {@link View}.
 *
 * @since 0.8
 */
@Immutable
@AutoValue
// Suppress Checker Framework warning about missing @Nullable in generated equals method.
@AutoValue.CopyAnnotations
@SuppressWarnings("nullness")
public abstract class ViewDescription {
  /**
   * Name of the view.
   *
   * @since 0.8
   */
  public abstract View.Name getName();

  /**
   * More detailed description, for documentation purposes.
   *
   * @since 0.8
   */
  public abstract String getDescription();

  /**
   * Measure type of this view.
   *
   * @since 0.8
   */
  // TODO(bdrutu): Check if this is really needed.
  public abstract Measure getMeasure();

  /**
   * The {@link Aggregation} associated with this {@link View}.
   *
   * @since 0.8
   */
  // TODO(bdrutu): Check if this is really needed.
  public abstract Aggregation getAggregation();

  /**
   * Columns (a.k.a Tag Keys) to match with the associated {@link Measure}.
   *
   * <p>{@link Measure} will be recorded in a "greedy" way. That is, every view aggregates every
   * measure. This is similar to doing a GROUPBY on view’s columns. Columns must be unique.
   *
   * @since 0.8
   */
  public abstract List<TagKey> getColumns();

  /**
   * @param name the {@link Name} of view. Must be unique.
   * @param description the description of view.
   * @param measure the {@link Measure} to be aggregated by this view.
   * @param aggregation the basic {@link Aggregation} that this view will support.
   * @param columns the {@link TagKey}s that this view will aggregate on. Columns should not contain
   *     duplicates.
   * @return a {@code ViewDescription}.
   * @throws IllegalArgumentException if the types of {@code Aggregation} and {@code
   *     AggregationData} don't match, or the types of {@code Window} and {@code WindowData} don't
   *     match.
   * @since 0.13
   */
  public static ViewDescription create(
      View.Name name,
      String description,
      Measure measure,
      Aggregation aggregation,
      List<TagKey> columns) {

    return new AutoValue_ViewDescription(name, description, measure, aggregation, columns);
  }

  /**
   * @param view the {@link View}.
   * @return a {@code ViewDescription}.
   * @throws IllegalArgumentException if the types of {@code Aggregation} and {@code
   *     AggregationData} don't match, or the types of {@code Window} and {@code WindowData} don't
   *     match.
   * @since 0.13
   */
  public static ViewDescription create(View view) {

    return new AutoValue_ViewDescription(
        view.getName(),
        view.getDescription(),
        view.getMeasure(),
        view.getAggregation(),
        view.getColumns());
  }
}
