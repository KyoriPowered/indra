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
package net.kyori.indra;

import net.kyori.indra.internal.IndraExtensionImpl;
import net.kyori.mammoth.Extensions;
import org.gradle.api.plugins.ExtensionContainer;
import org.gradle.util.GradleVersion;
import org.jetbrains.annotations.NotNull;

public class Indra {

  /**
   * The minimum supported Gradle version for the Indra suite.
   *
   * @since 3.0.0
   */
  public static final GradleVersion MINIMUM_SUPPORTED = GradleVersion.version("7.5");
  public static final String EXTENSION_NAME = "indra";
  public static final String PUBLICATION_NAME = "maven";

  public static String testJava(final int version) {
    return "testJava" + version;
  }

  /**
   * Get or create the {@code indra} extension for a project.
   *
   * @param extensions the extensions container
   * @return the appropriate extension instance
   */
  public static @NotNull IndraExtension extension(final @NotNull ExtensionContainer extensions) {
    return Extensions.findOrCreate(extensions, EXTENSION_NAME, IndraExtension.class, IndraExtensionImpl.class);
  }
}
