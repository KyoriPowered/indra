/*
 * This file is part of indra, licensed under the MIT License.
 *
 * Copyright (c) 2020-2024 KyoriPowered
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
package net.kyori.indra.git.internal;

import java.io.File;
import org.eclipse.jgit.api.Git;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.services.BuildService;
import org.gradle.api.services.BuildServiceParameters;
import org.gradle.tooling.events.FinishEvent;
import org.gradle.tooling.events.OperationCompletionListener;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A build service providing the most relevant git repository for any project in the build.
 *
 * @since 2.0.0
 */
public abstract class IndraGitService implements BuildService<IndraGitService.Parameters>, AutoCloseable, OperationCompletionListener {
  public static final String SERVICE_NAME = "indraGitService";

  private GitCache.GitProvider inner;


  public interface Parameters extends BuildServiceParameters {
    /**
     * The base directory of the build.
     *
     * <p>All git repositories must be contained within this directory.</p>
     *
     * @return the base directory
     */
    DirectoryProperty getBaseDirectory();
  }

  public IndraGitService() {
    this.inner = GitCache.getOrCreate(this.getParameters().getBaseDirectory().get().getAsFile());
  }

  /**
   * Get the git repository for this build.
   *
   * <p>If this project is not managed by git, this will return {@code null}.</p>
   *
   * @param projectDir the project directory to locate a git repo in
   * @param displayName the display name for the context being queried
   * @return the build's git repository.
   * @since 2.0.0
   */
  public @Nullable Git git(final File projectDir, final @NotNull String displayName) {
    return this.inner.git(projectDir, displayName);
  }


  @Override
  public void close() {
    GitCache.close(this.inner);
  }

  @Override
  public void onFinish(final FinishEvent finishEvent) {
    // no-op
  }
}
