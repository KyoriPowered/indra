/*
 * This file is part of indra, licensed under the MIT License.
 *
 * Copyright (c) 2023-2024 KyoriPowered
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
package net.kyori.indra.git.task;

import net.kyori.indra.git.internal.IndraGitService;
import org.eclipse.jgit.api.Git;
import org.gradle.api.DefaultTask;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Internal;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

/**
 * Base class for tasks that work with the project's {@link Git} repository.
 *
 * <p>These tasks have free access to the git repository without having to work within the bounds of a value source.</p>
 *
 * @since 4.0.0
 */
public abstract class RepositoryTask extends DefaultTask {
  @ApiStatus.Internal
  @Internal
  public abstract Property<IndraGitService> getGit();

  @Internal
  abstract DirectoryProperty getProjectDirectory();

  @Internal
  abstract Property<String> getProjectDisplayName();

  public RepositoryTask() {
    this.getProjectDirectory().fileValue(this.getProject().getProjectDir());
    this.getProjectDisplayName().convention(this.getProject().getDisplayName());
  }

  /**
   * Get the actual repo.
   *
   * @return the repo
   * @since 4.0.0
   */
  protected @Nullable Git repo() {
    return this.getGit().get().git(this.getProjectDirectory().get().getAsFile(), this.getProjectDisplayName().get());
  }
}
