/*
 * This file is part of indra, licensed under the MIT License.
 *
 * Copyright (c) 2020 KyoriPowered
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
package net.kyori.gradle.api;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.plugins.Convention;
import org.gradle.api.plugins.ExtensionContainer;
import org.gradle.api.plugins.PluginContainer;
import org.gradle.api.tasks.TaskContainer;

// todo(kashike): promote to own project if this package (net.kyori.gradle) becomes useful for others to use

/**
 * A more friendly interface for creating a {@link Plugin} that operates on a {@link Project}.
 */
public interface ProjectPlugin extends Plugin<Project> {
  @Override
  default void apply(final @NonNull Project project) {
    this.apply(
      project,
      project.getPlugins(),
      project.getExtensions(),
      project.getConvention(),
      project.getTasks()
    );
  }

  void apply(
    final @NonNull Project project,
    final @NonNull PluginContainer plugins,
    final @NonNull ExtensionContainer extensions,
    final @NonNull Convention convention,
    final @NonNull TaskContainer tasks
  );
}
