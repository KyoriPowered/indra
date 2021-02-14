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
import org.checkerframework.checker.nullness.qual.Nullable;
import org.immutables.value.Value;

/**
 * License information for the current project.
 */
@Value.Immutable
public interface License {
  static Builder builder() {
    return new Builder();
  }

  static License apache2() {
    return License.builder()
      .spdx("Apache-2.0")
      .name("Apache License, Version 2.0")
      .url("https://opensource.org/licenses/Apache-2.0")
      .build();
  }

  static License gpl3Only() {
    return License.builder()
      .spdx("GPL-3.0-only")
      .name("GNU General Public License version 3")
      .url("https://opensource.org/licenses/GPL-3.0")
      .bintray("GPL-3.0")
      .build();
  }

  static License mit() {
    return License.builder()
      .spdx("MIT")
      .name("The MIT License")
      .url("https://opensource.org/licenses/MIT")
      .build();
  }

  @SuppressWarnings("SpellCheckingInspection")
  @Nullable String spdx(); // https://spdx.org/licenses/

  @NonNull String name();

  @NonNull String url();

  @Value.Default
  default @Nullable String bintray() {
    return this.spdx();
  }

  final class Builder extends LicenseImpl.Builder {
    // generated based on class spec
  }
}
