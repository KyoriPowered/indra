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
package net.kyori.indra.crossdoc;

import org.gradle.api.Action;
import org.gradle.api.provider.Property;
import org.gradle.api.provider.Provider;

/**
 * Configuration for the crossdoc plugin, applicable across multiple tasks.
 */
public interface CrossdocExtension {
  /**
   * The base URL where the eventually published Javadoc will be hosted.
   *
   * <p>If this URL does not end with a '{@code /}', one will be appended automatically.</p>
   *
   * @return the base URL property
   * @since 2.1.0
   */
  Property<String> baseUrl();

  /**
   * Set the base URL where the eventually published Javadoc will be hosted.
   *
   * <p>If this URL does not end with a '{@code /}', one will be appended automatically.</p>
   *
   * @param baseUrl the base URL
   * @since 2.1.0
   */
  default void baseUrl(final String baseUrl) {
    this.baseUrl().set(baseUrl);
  }

  /**
   * Set the base URL where the eventually published Javadoc will be hosted.
   *
   * <p>If this URL does not end with a '{@code /}', one will be appended automatically.</p>
   *
   * @param baseUrl the base URL
   * @since 2.1.0
   */
  default void baseUrl(final Provider<? extends String> baseUrl) {
    this.baseUrl().set(baseUrl);
  }

  /**
   * The default provider to use for generating project documentation.
   *
   * <p>This property will default to a standard implementation appending the project version to the project name.</p>
   *
   * @return a property providing the project documentation url
   * @since 2.1.0
   */
  Property<ProjectDocumentationUrlProvider> projectDocumentationUrlProvider();

  /**
   * Set the {@linkplain #projectDocumentationUrlProvider() documentation provider} to a standard implementation.
   *
   * <p>The computed URLs will be in the format {@code <project name (stripped of prefix)>[/<version>]}.</p>
   *
   * @see NameBasedProjectDocumentationUrlProvider
   * @since 2.1.0
   */
  void nameBasedDocumentationUrlProvider(final Action<? super NameBasedProjectDocumentationUrlProvider> action);
}
