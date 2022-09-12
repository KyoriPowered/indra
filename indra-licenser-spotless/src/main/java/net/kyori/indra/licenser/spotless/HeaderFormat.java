/*
 * This file is part of indra, licensed under the MIT License.
 *
 * Copyright (c) 2020-2022 KyoriPowered
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
package net.kyori.indra.licenser.spotless;

import org.jetbrains.annotations.Nullable;

/**
 * A description of a license header format.
 *
 * @since 2.2.0
 */
public class HeaderFormat {
  private final @Nullable String begin;
  private final @Nullable String linePrefix;
  private final @Nullable String lineSuffix;
  private final @Nullable String end;

  HeaderFormat(
    final @Nullable String begin,
    final @Nullable String linePrefix,
    final @Nullable String lineSuffix,
    final @Nullable String end
  ) {
    this.begin = begin;
    this.linePrefix = linePrefix;
    this.lineSuffix = lineSuffix;
    this.end = end;
  }

  public static HeaderFormat headerFormat(final @Nullable String begin, final @Nullable String linePrefix, final @Nullable String lineSuffix, final @Nullable String end) {
    return new HeaderFormat(begin, linePrefix, lineSuffix, end);
  }

  public static HeaderFormat starSlash() {
    return new HeaderFormat("/* ", " * ", null, " */");
  }

  public static HeaderFormat doubleSlash() {
    return prefix("// ");
  }

  public static HeaderFormat prefix(final String prefix) {
    return new HeaderFormat(null, prefix, null, null);
  }

  public @Nullable String begin() {
    return this.begin;
  }

  public @Nullable String linePrefix() {
    return this.linePrefix;
  }

  public @Nullable String lineSuffix() {
    return this.lineSuffix;
  }

  public @Nullable String end() {
    return this.end;
  }
}
