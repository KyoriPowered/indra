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
 * A representation of services that can be configured for a specific forge site (GitHub, GitLab, etc).
 *
 * <p>{@code ApplyTo} instances are mutable and must not be shared.</p>
 *
 * @since 1.0.0
 */
@Value.Modifiable
@Value.Style(set = "*", create = "new", defaultAsDefault = true, visibility = Value.Style.ImplementationVisibility.PACKAGE)
public interface ApplyTo {

  /**
   * Create a new instance with default values.
   *
   * @return the new instance
   * @since 2.0.0
   */
  static @NonNull ApplyTo defaults() {
    return new ModifiableApplyTo();
  }

  /**
   * Get whether CI configuration will be applied.
   *
   * @return whether to apply CI configuration
   * @since 1.2.0
   */
  default boolean ci() {
    return false;
  }

  /**
   * Set whether continuous integration configuration will be applied.
   *
   * @param ci whether to apply continuous integration configuration
   * @return this instance
   * @since 1.2.0
   */
  ApplyTo ci(final boolean ci);

  /**
   * Get whether issues configuration will be applied.
   *
   * @return whether to apply issues configuration
   * @since 1.0.0
   */
  default boolean issues() {
    return true;
  }

  /**
   * Set whether issue tracker configuration will be applied.
   *
   * @param issues whether to apply issue tracker configuration
   * @return this instance
   * @since 1.0.0
   */
  ApplyTo issues(final boolean issues);

  /**
   * Get whether scm configuration will be applied.
   *
   * @return whether to apply scm
   * @since 1.0.0
   */
  default boolean scm() {
    return true;
  }

  /**
   * Set whether scm will be applied.
   *
   * @param scm whether to apply scm
   * @return this instance
   * @since 1.0.0
   */
  ApplyTo scm(final boolean scm);

  /**
   * Get whether publishing configuration will be applied.
   *
   * @return whether to apply publishing
   * @since 1.0.0
   */
  default boolean publishing() {
    return false;
  }

  /**
   * Set whether publishing will be applied.
   *
   * @param publishing whether to apply publishing
   * @return this instance
   * @since 1.0.0
   */
  ApplyTo publishing(final boolean publishing);

}
