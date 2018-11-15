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

package io.opencensus.stats;

import static io.opencensus.stats.MeasureMap.newNoopMeasureMap;

import io.opencensus.stats.Measure.MeasureDouble;
import io.opencensus.tags.Tag;
import io.opencensus.tags.TagContext;
import io.opencensus.tags.TagKey;
import io.opencensus.tags.TagValue;
import java.util.Collections;
import java.util.Iterator;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Unit tests for {@link MeasureMap}. */
@RunWith(JUnit4.class)
public final class MeasureMapTest {
  private static final Tag TAG = Tag.create(TagKey.create("key"), TagValue.create("value"));
  private static final MeasureDouble MEASURE =
      MeasureDouble.create("my measure", "description", "s");

  private final TagContext tagContext =
      new TagContext() {

        @Override
        protected Iterator<Tag> getIterator() {
          return Collections.<Tag>singleton(TAG).iterator();
        }
      };

  @Rule public final ExpectedException thrown = ExpectedException.none();

  @Test
  public void noopMeasureMap_PutAttachmentNullKey() {
    MeasureMap measureMap = newNoopMeasureMap();
    thrown.expect(NullPointerException.class);
    thrown.expectMessage("key");
    measureMap.putAttachment(null, "value");
  }

  @Test
  public void noopMeasureMap_PutAttachmentNullValue() {
    MeasureMap measureMap = newNoopMeasureMap();
    thrown.expect(NullPointerException.class);
    thrown.expectMessage("value");
    measureMap.putAttachment("key", null);
  }

  @Test
  public void noopMeasureMap_PutNegativeValue() {
    newNoopMeasureMap().put(MEASURE, -5).record(tagContext);
  }

  // The NoopStatsRecorder should do nothing, so this test just checks that record doesn't throw an
  // exception.
  @Test
  public void noopMeasureMap_Record() {
    newNoopMeasureMap().put(MEASURE, 5).record(tagContext);
  }

  // The NoopStatsRecorder should do nothing, so this test just checks that record doesn't throw an
  // exception.
  @Test
  public void noopMeasureMap_RecordWithCurrentContext() {
    newNoopMeasureMap().put(MEASURE, 6).record();
  }

  @Test
  public void noopMeasureMap_Record_DisallowNullTagContext() {
    MeasureMap measureMap = newNoopMeasureMap();
    thrown.expect(NullPointerException.class);
    thrown.expectMessage("tags");
    measureMap.record(null);
  }
}
