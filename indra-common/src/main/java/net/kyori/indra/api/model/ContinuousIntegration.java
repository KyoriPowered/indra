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
 * A simplified description of a continuous integration system.
 *
 * @since 2.0.0
 */
@Value.Immutable
public interface ContinuousIntegration {
  /**
   * Create a new CI builder.
   *
   * @return the builder
   * @since 2.0.0
   */
  static @NonNull Builder builder() {
    return new ContinuousIntegrationImpl.BuilderImpl();
  }

  /**
   * The name of the continuous integration system used.
   *
   * @return the CI system name
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
   * A builder for new continuous integration instances.
   *
   * @since 2.0.0
   */
  interface Builder {
    /**
     * Fill a builder with attribute values from the provided {@link ContinuousIntegration} instance.
     *
     * @param instance The instance from which to copy values
     * @return this builder
     * @since 2.0.0
     */
    @NonNull Builder from(final ContinuousIntegration instance);

    @NonNull Builder system(final @NonNull String system);

    @NonNull Builder url(final @NonNull String url);

    @NonNull ContinuousIntegration build();

  }
}
