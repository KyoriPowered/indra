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

import org.jetbrains.annotations.NotNull;
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

  /**
   * Create a fully custom header format.
   *
   * @param begin text to put at the beginning of the header block
   * @param linePrefix text to put at the beginning of every line
   * @param lineSuffix text to put at the end of every line
   * @param end text to put at the end of the header block
   * @return a new header format object
   * @since 2.2.0
   */
  public static @NotNull HeaderFormat headerFormat(final @Nullable String begin, final @Nullable String linePrefix, final @Nullable String lineSuffix, final @Nullable String end) {
    return new HeaderFormat(begin, linePrefix, lineSuffix, end);
  }

  /**
   * Create a header format using star-slash, or C-style format.
   *
   * @return a star-slash header format instance
   * @since 2.2.0
   */
  public static @NotNull HeaderFormat starSlash() {
    return new HeaderFormat("/*", " * ", null, " */");
  }

  /**
   * Create a header format using a double-slash prefix.
   *
   * @return the header prefix to use
   * @since 2.2.0
   */
  public static @NotNull HeaderFormat doubleSlash() {
    return prefix("// ");
  }

  /**
   * Create a header format applying a custom prefix to each body line.
   *
   * @param prefix the body line prefix
   * @return the created header format
   * @since 2.2.0
   */
  public static @NotNull HeaderFormat prefix(final String prefix) {
    return new HeaderFormat(null, prefix, null, null);
  }

  /**
   * Get text to put at the beginning of an entire header block.
   *
   * @return the beginning content
   * @since 2.2.0
   */
  public @Nullable String begin() {
    return this.begin;
  }

  /**
   * Get text to put at the beginning of each header line.
   *
   * @return the line prefix content
   * @since 2.2.0
   */
  public @Nullable String linePrefix() {
    return this.linePrefix;
  }

  /**
   * Get text to put at the end of each header line.
   *
   * @return the line suffix content
   * @since 2.2.0
   */
  public @Nullable String lineSuffix() {
    return this.lineSuffix;
  }

  /**
   * Get text to put at the end of an entire header block.
   *
   * @return the end content
   * @since 2.2.0
   */
  public @Nullable String end() {
    return this.end;
  }
}
