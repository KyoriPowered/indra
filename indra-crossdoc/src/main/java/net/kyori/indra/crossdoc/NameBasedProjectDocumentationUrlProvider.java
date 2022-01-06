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
package net.kyori.indra.crossdoc;

import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.Optional;

/**
 * A documentation URL provider that generates a relative URL based on the project name.
 *
 * <p>The final URL will be in the format {@code <project name>[/<version>]}, where the configured prefix is stripped from the project name if it is present.</p>
 *
 * @since 2.1.0
 */
public interface NameBasedProjectDocumentationUrlProvider extends ProjectDocumentationUrlProvider {
  /**
   * A version string that will be appended.
   *
   * <p>This will be set to the project version by default.</p>
   *
   * @return the version component of the generated URL
   * @since 2.1.0
   */
  @Input
  @Optional
  Property<String> getVersion();

  /**
   * A prefix that will be stripped from project names when building the URL, if it is present.
   *
   * @return the project name prefix
   * @since 2.1.0
   */
  @Input
  @Optional
  Property<String> getProjectNamePrefix();
}
