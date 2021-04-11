/*
 * This file is part of indra, licensed under the MIT License.
 *
 * Copyright (c) 2020-2021 KyoriPowered
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
import org.checkerframework.checker.nullness.qual.Nullable;
import org.immutables.value.Value;

/**
 * License information for the current project.
 *
 * @since 2.0.0
 */
@Value.Immutable
public interface License {
  /**
   * Create a builder to construct a custom license.
   *
   * @return the license builder
   * @since 2.0.0
   */
  static @NonNull Builder builder() {
    return new LicenseImpl.BuilderImpl();
  }

  /**
   * Get an Apache 2.0 license instance.
   *
   * @return the Apache 2.0 license
   * @since 2.0.0
   */
  static @NonNull License apache2() {
    return License.builder()
      .spdx("Apache-2.0")
      .name("Apache License, Version 2.0")
      .url("https://opensource.org/licenses/Apache-2.0")
      .build();
  }

  /**
   * Get a GPL-3.0-only license instance.
   *
   * @return the GPL-3.0-only license
   * @since 2.0.0
   */
  static @NonNull License gpl3Only() {
    return License.builder()
      .spdx("GPL-3.0-only")
      .name("GNU General Public License version 3")
      .url("https://opensource.org/licenses/GPL-3.0")
      .build();
  }

  /**
   * Get a MIT license instance.
   *
   * @return the MIT license
   * @since 2.0.0
   */
  static @NonNull License mit() {
    return License.builder()
      .spdx("MIT")
      .name("The MIT License")
      .url("https://opensource.org/licenses/MIT")
      .build();
  }

  /**
   * Get the <a href="https://spdx.org/licenses/">SPDX</a> identifier that describes this license.
   *
   * @return spdx the license identifier
   * @since 2.0.0
   */
  @SuppressWarnings("SpellCheckingInspection")
  @Nullable String spdx();

  /**
   * Get the display name of this license.
   *
   * @return the license display name
   * @since 2.0.0
   */
  @NonNull String name();

  /**
   * Get a URL to the text of this license.
   *
   * @return a URL pointing to this license text
   * @since 2.0.0
   */
  @NonNull String url();

  /**
   * A builder for {@link License}s.
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
    @NonNull Builder from(final License instance);

    /**
     * Set the <a href="https://spdx.org/licenses/">SPDX</a> identifier that describes this license.
     *
     * <p>This field is optional.</p>
     *
     * @param spdx the license identifier
     * @return this builder
     * @since 2.0.0
     */
    @NonNull Builder spdx(final @NonNull String spdx);

    /**
     * Set the display name of this license.
     *
     * @param name the license name
     * @return this builder
     * @since 2.0.0
     */
    @NonNull Builder name(final @NonNull String name);

    /**
     * Set the URL of this license.
     *
     * @param url the license URL
     * @return this builder
     * @since 2.0.0
     */
    @NonNull Builder url(final @NonNull String url);

    /**
     * Create a new license.
     *
     * <p>All fields but {@link #spdx(String)} must be set.</p>
     *
     * @return a new {@link License}
     * @since 2.0.0
     */
    @NonNull License build();
  }
}
