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
 * A helper interface to allow modifying header formats without needing to search for imports.
 *
 * @see HeaderFormat
 * @since 2.2.0
 */
public interface HeaderFormatApplier {

  /**
   * Set the header format to use star-slash, or C-style format.
   *
   * @since 2.2.0
   */
  void starSlash();

  /**
   * Set the header format to use a double-slash prefix.
   *
   * @since 2.2.0
   */
  void doubleSlash();

  /**
   * A header format containing only a prefix for body lines.
   *
   * @param prefix the prefix (no space-padding will be added)
   * @since 2.2.0
   */
  void prefix(final String prefix);

  /**
   * Set a custom header format.
   *
   * <p>Any parameters may be null, but a null parameter will be of limited use.</p>
   *
   * @param begin text to put at the beginning of the header block
   * @param linePrefix text to put at the beginning of every line
   * @param lineSuffix text to put at the end of every line
   * @param end text to put at the end of the header block
   * @since 2.2.0
   */
  void custom(final @Nullable String begin, final @Nullable String linePrefix, final @Nullable String lineSuffix, final @Nullable String end);
}
