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

import org.gradle.api.Project;
import org.jetbrains.annotations.NotNull;

/**
 * A provider that, given a project name will compute the eventual published URL, relative to a separately provided base URL.
 *
 * <p>Implementations may annotate java bean-style getters with task input
 * annotations for accurate up-to-date checks on projects.</p>
 *
 * @since 2.1.0
 */
public interface ProjectDocumentationUrlProvider {
  /**
   * Create the relative, public-facing URL for the Javadoc for a certain project.
   *
   * @param projectName plain name of the project (see {@link Project#getName()}
   * @param projectPath full, colon-separated path to this project (see {@link Project#getPath()})
   * @return the relative, public-facing URL for the Javadoc for a certain project
   * @since 2.1.0
   */
  @NotNull String createUrl(final @NotNull String projectName, final @NotNull String projectPath);
}
