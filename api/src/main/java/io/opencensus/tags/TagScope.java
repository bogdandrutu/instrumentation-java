/*
 * Copyright 2019, OpenCensus Authors
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

package io.opencensus.tags;

import javax.annotation.concurrent.Immutable;

/**
 * TagScope is used to determine the scope of a Tag.
 *
 * @since 0.19
 */
@Immutable
public enum TagScope {

  /**
   * Tag with Local scope are used within the process it created.
   *
   * @since 0.19
   */
  LOCAL,

  /**
   * A tag is created with the Request scope then it is propagated across process boundaries subject
   * to outgoing and incoming (on remote side) filter criteria
   *
   * @since 0.19
   */
  REQUEST,
}
