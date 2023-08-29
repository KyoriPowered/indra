/*
 * This file is part of indra, licensed under the MIT License.
 *
 * Copyright (c) 2020-2023 KyoriPowered
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

import java.io.File;
import net.kyori.indra.git.internal.IndraGitExtensionImpl;
import net.kyori.indra.git.internal.IndraGitService;
import net.kyori.indra.git.task.RepositoryTask;
import net.kyori.indra.git.task.RequireClean;
import net.kyori.indra.git.task.RequireTagged;
import net.kyori.mammoth.ProjectOrSettingsPlugin;
import org.gradle.api.Project;
import org.gradle.api.initialization.Settings;
import org.gradle.api.invocation.Gradle;
import org.gradle.api.plugins.ExtensionContainer;
import org.gradle.api.plugins.PluginContainer;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.TaskContainer;
import org.jetbrains.annotations.NotNull;

/**
 * A plugin that exposes any git repository that might be in a project.
 *
 * @since 2.0.0
 */
public class GitPlugin implements ProjectOrSettingsPlugin {
  private static final String EXTENSION_NAME = "indraGit";
  public static final String REQUIRE_CLEAN_TASK = "requireClean";
  public static final String REQUIRE_TAGGED_TASK = "requireTagged";

  @Override
  public void applyToProject(
    final @NotNull Project target,
    final @NotNull PluginContainer plugins,
    final @NotNull ExtensionContainer extensions,
    final @NotNull TaskContainer tasks
  ) {
    final Provider<IndraGitService> service = this.applyCommon(
      target.getGradle(),
      extensions,
      target.getRootDir(),
      target.getProjectDir(),
      target.getDisplayName()
    );

    // And create some validation tasks, but don't ever make them run
    tasks.register(REQUIRE_CLEAN_TASK, RequireClean.class);
    tasks.register(REQUIRE_TAGGED_TASK, RequireTagged.class);
    tasks.withType(RepositoryTask.class).configureEach(task -> {
      task.getGit().set(service);
    });
  }

  @Override
  public void applyToSettings(
    final @NotNull Settings target,
    final @NotNull PluginContainer plugins,
    final @NotNull ExtensionContainer extensions
  ) {
    this.applyCommon(
      target.getGradle(),
      extensions,
      target.getRootDir(),
      target.getRootDir(),
      "settings"
    );
  }

  private Provider<IndraGitService> applyCommon(final @NotNull Gradle gradle, final ExtensionContainer extensions, final File rootDir, final File projectDir, final String displayName) {
    // Register the service, then create an extension
    final Provider<IndraGitService> service = gradle.getSharedServices().registerIfAbsent(IndraGitService.SERVICE_NAME, IndraGitService.class, params -> {
      params.getParameters().getBaseDirectory().set(rootDir);
    });
    extensions.create(IndraGitExtension.class, EXTENSION_NAME, IndraGitExtensionImpl.class, projectDir, displayName, service);
    return service;
  }
}
