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
package net.kyori.indra.licenser.spotless.internal;

import net.kyori.indra.licenser.spotless.HeaderFormat;
import net.kyori.indra.licenser.spotless.HeaderFormatApplier;
import org.gradle.api.provider.Property;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class HeaderFormatApplierImpl implements HeaderFormatApplier {
  private final Property<HeaderFormat> format;

  public HeaderFormatApplierImpl(final Property<HeaderFormat> format) {
    this.format = format;
  }

  @Override
  public void starSlash() {
    this.format.set(HeaderFormat.starSlash());
  }

  @Override
  public void doubleSlash() {
    this.format.set(HeaderFormat.doubleSlash());
  }

  @Override
  public void prefix(final @NotNull String prefix) {
    this.format.set(HeaderFormat.prefix(prefix));
  }

  @Override
  public void custom(final @Nullable String begin, final @Nullable String linePrefix, final @Nullable String lineSuffix, final @Nullable String end) {
    this.format.set(HeaderFormat.headerFormat(begin, linePrefix, lineSuffix, end));
  }
}
