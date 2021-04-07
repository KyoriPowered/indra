/*
 * This file is part of indra, licensed under the MIT License.
 *
 * Copyright (c) 2020 KyoriPowered
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package net.kyori.indra.api.model;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.immutables.value.Value;

/**
 * Project issue tracker information.
 *
 * @since 2.0.0
 */
@Value.Immutable
public interface Issues {

  /**
   * Create a new builder for a {@link Issues} instance.
   *
   * @return a new builder
   * @since 2.0.0
   */
  static @NonNull Builder builder() {
    return new IssuesImpl.BuilderImpl();
  }

  /**
   * The name of the issue tracking system used.
   *
   * @return the issue tracking system name
   * @since 2.0.0
   */
  @NonNull String system();

  /**
   * The URL pointing to a web interface for the CI system.
   *
   * @return the URL
   * @since 2.0.0
   */
  @NonNull String url();

  /**
   * A builder for new issue tracker instances.
   *
   * @since 2.0.0
   */
  interface Builder {

    /**
     * Fill a builder with attribute values from the provided {@link Issues} instance.
     *
     * @param instance The instance from which to copy values
     * @return this builder
     * @since 2.0.0
     */
    @NonNull Builder from(final Issues instance);

    @NonNull Builder system(final @NonNull String system);

    @NonNull Builder url(final @NonNull String url);

    Issues build();

  }
}
