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
package net.kyori.indra.internal.language;

import java.nio.charset.StandardCharsets;
import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.SourceSet;
import org.gradle.util.GradleVersion;
import org.jetbrains.annotations.NotNull;

public interface LanguageSupport {
  String DEFAULT_ENCODING = StandardCharsets.UTF_8.name();
  boolean HAS_GRADLE_7_2 = GradleVersion.current().compareTo(GradleVersion.version("7.2")) >= 0;

  void registerApplyCallback(
    final @NotNull Project project,
    final @NotNull Action<? super Project> callback
  );

  /**
   * Configure compile tasks.
   *
   * <ul>
   * <li>Toolchain and target versions</li>
   * <li>set encoding to UTF-8</li>
   * </ul>
   *
   * @param project project to configure
   * @param sourceSet source set to find compile tasks in
   * @param toolchainVersion the version to run on
   * @param bytecodeVersion the version to target
   */
  void configureCompileTasks(
    final @NotNull Project project,
    final @NotNull SourceSet sourceSet,
    final @NotNull Provider<Integer> toolchainVersion,
    final @NotNull Provider<Integer> bytecodeVersion
  );

  /**
   * Configure documentation tasks.
   *
   * <ul>
   * <li>Toolchain and target versions</li>
   * <li>set encoding to UTF-8</li>
   * </ul>
   *
   * @param project project to configure
   * @param sourceSet source set to find compile tasks in
   * @param toolchainVersion the Java version to run on
   * @param targetVersion the Java version to target
   */
  default void configureDocTasks(
    final @NotNull Project project,
    final @NotNull SourceSet sourceSet,
    final @NotNull Provider<Integer> toolchainVersion,
    final @NotNull Provider<Integer> targetVersion
  ) {};
}
