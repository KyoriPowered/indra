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
package net.kyori.indra.api.model;

import org.immutables.value.Value;
import org.jetbrains.annotations.NotNull;

/**
 * SCM metadata information.
 *
 * @since 2.0.0
 */
@Value.Immutable
public interface SourceCodeManagement {
  /**
   * Create a new builder for {@link SourceCodeManagement} instances.
   *
   * @return a new builder
   * @since 2.0.0
   */
  static @NotNull Builder builder() {
    return new SourceCodeManagementImpl.BuilderImpl();
  }

  /**
   * Get a read-only access to the project's repository.
   *
   * <p>This must be in a {@code scm:[provider]:[provider-specific arguments]} format.</p>
   *
   * @return the read-only project repository
   * @since 2.0.0
   */
  @NotNull String connection();

  /**
   * Get a read-write access to the project's repository.
   *
   * <p>This must be in a {@code scm:[provider]:[provider-specific arguments]} format.</p>
   *
   * @return the read-write project repository
   * @since 2.0.0
   */
  @NotNull String developerConnection();

  /**
   * Get a web URL to view this project's repository.
   *
   * @return the project's SCM web URL
   * @since 2.0.0
   */
  @NotNull String url();

  /**
   * A builder for {@link SourceCodeManagement} instances.
   *
   * @since 2.0.0
   */
  interface Builder {
    /**
     * Set a read-only access to the project's repository.
     *
     * <p>This must be in a {@code scm:[provider]:[provider-specific arguments]} format.</p>
     *
     * @param connection the read-only project repository
     * @return this builder
     * @since 2.0.0
     */
    @SuppressWarnings({"NullableProblems", "override"}) // generated
    @NotNull Builder connection(final @NotNull String connection);

    /**
     * Set a read-write access to the project's repository.
     *
     * <p>This must be in a {@code scm:[provider]:[provider-specific arguments]} format.</p>
     *
     * @param developerConnection the read-write project repository
     * @return this builder
     * @since 2.0.0
     */
    @SuppressWarnings({"NullableProblems", "override"}) // generated
    @NotNull Builder developerConnection(final @NotNull String developerConnection);

    /**
     * Get a web URL to view this project's repository.
     *
     * @param url the project's SCM web URL
     * @return this builder
     * @since 2.0.0
     */
    @SuppressWarnings({"NullableProblems", "override"}) // generated
    @NotNull Builder url(final @NotNull String url);

    /**
     * Creates a new {@link SourceCodeManagement} instance.
     *
     * <p>The {@link #connection(String)}, {@link #developerConnection(String)}, and {@link #url(String)} properties must be set.</p>
     *
     * @return a new {@link SourceCodeManagement} instance
     * @since 2.0.0
     */
    @SuppressWarnings({"NullableProblems", "override"}) // generated
    @NotNull SourceCodeManagement build();
  }
}
