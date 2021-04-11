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
package net.kyori.indra.git;

import net.kyori.indra.git.internal.IndraGitExtensionImpl;
import net.kyori.indra.git.internal.IndraGitService;
import net.kyori.indra.git.task.RequireClean;
import net.kyori.mammoth.ProjectPlugin;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.plugins.Convention;
import org.gradle.api.plugins.ExtensionContainer;
import org.gradle.api.plugins.PluginContainer;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.TaskContainer;

/**
 * A plugin that exposes any git repository that might be in a project.
 *
 * @since 2.0.0
 */
public class GitPlugin implements ProjectPlugin {
  private static final String EXTENSION_NAME = "indraGit";
  private static final String SERVICE_NAME = "indraGitService";

  public static final String REQUIRE_CLEAN_TASK = "requireClean";

  @Override
  public void apply(@NonNull final Project project, @NonNull final PluginContainer plugins, @NonNull final ExtensionContainer extensions, @NonNull final Convention convention, @NonNull final TaskContainer tasks) {
    // Register the service, then create an extension
    final Provider<IndraGitService> service = project.getGradle().getSharedServices().registerIfAbsent(SERVICE_NAME, IndraGitService.class, params -> {
      params.getParameters().getBaseDirectory().set(project.getRootDir());
    });
    extensions.create(IndraGitExtension.class, EXTENSION_NAME, IndraGitExtensionImpl.class, project, service);

    // And create a task, but don't ever make it run
    tasks.register(REQUIRE_CLEAN_TASK, RequireClean.class, task -> {
      task.getGit().set(service);
    });
  }
}
