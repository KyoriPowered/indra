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
package net.kyori.indra.multirelease;

import net.kyori.indra.internal.ImmutablesStyle;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.gradle.api.tasks.SourceSet;
import org.immutables.value.Value;

/**
 * Details about a specific version variant of a multirelease artifact.
 *
 * @since 2.0.0
 */
@ImmutablesStyle
@Value.Immutable
public interface MultireleaseVariantDetails {
  /**
   * Create a new instance of multirelease variant details.
   *
   * @param base the base version source set
   * @param targetVersion the variant target version
   * @param variant the target variant source set
   * @return a new variant details
   * @since 2.0.0
   */
  static @NonNull MultireleaseVariantDetails details(final @NonNull SourceSet base, final int targetVersion, final @NonNull SourceSet variant) {
    return new MultireleaseVariantDetailsImpl(base, targetVersion, variant);
  }

  /**
   * The source set containing the base version.
   *
   * @return the base source set
   * @since 2.0.0
   */
  @Value.Parameter
  @NonNull SourceSet base();

  /**
   * The target Java major release.
   *
   * @return the target major release
   * @since 2.0.0
   */
  @Value.Parameter
  int targetVersion();

  /**
   * The source set containing the {@link #targetVersion()} version variant.
   *
   * @return the variant source set
   * @since 2.0.0
   */
  @Value.Parameter
  @NonNull SourceSet variant();
}
