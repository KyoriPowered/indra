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
package net.kyori.indra.git;

import java.io.File;
import java.io.IOException;
import javax.inject.Inject;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.eclipse.jgit.api.Git;
import org.gradle.api.Project;
import org.gradle.api.invocation.Gradle;
import org.gradle.api.services.BuildService;
import org.gradle.api.services.BuildServiceParameters;

/**
 * A build service providing the most relevant git repository for any project in the build.
 */
public abstract class IndraGitService implements BuildService<BuildServiceParameters.None>, AutoCloseable {
  private volatile boolean open = true;
  private final @Nullable Git git;

  @Inject
  public IndraGitService(final Gradle gradle) throws IOException {
    final File rootProjectDir = gradle.getRootProject().getRootDir();
    if(new File(rootProjectDir, ".git").exists()) {
      this.git = Git.open(rootProjectDir);
    } else {
      this.git = null;
    }
  }

  /**
   * Get the git repository for this build.
   *
   * <p>If this project is not managed by git, this will return {@code null}.</p>
   *
   * @return the build's git repository.
   */
  public @Nullable Git git(final Project project) {
    // TODO: pre-project git repositories
    if(!this.open) {
      throw new IllegalStateException("Tried to access git repository after close");
    }
    return this.git;
  }

  @Override
  public void close() {
    // TODO: Close repositories
    this.open = false;
    if (this.git != null) {
      this.git.close();
    }
  }
}
