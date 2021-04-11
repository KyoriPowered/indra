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
package net.kyori.indra.git.internal;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.errors.RepositoryNotFoundException;
import org.gradle.api.Project;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.services.BuildService;
import org.gradle.api.services.BuildServiceParameters;

/**
 * A build service providing the most relevant git repository for any project in the build.
 *
 * @since 2.0.0
 */
public abstract class IndraGitService implements BuildService<IndraGitService.Parameters>, AutoCloseable {
  private volatile boolean open = true;
  private final Map<File, GitWrapper> projectRepos = new ConcurrentHashMap<>();

  public interface Parameters extends BuildServiceParameters {
    /**
     * The base directory of the project.
     *
     * <p>All git repositories must be contained within this directory.</p>
     *
     * @return the base directory
     */
    DirectoryProperty getBaseDirectory();
  }

  /**
   * Get the git repository for this build.
   *
   * <p>If this project is not managed by git, this will return {@code null}.</p>
   *
   * @return the build's git repository.
   * @since 2.0.0
   */
  public @Nullable Git git(final Project project) {
    if(!this.open) {
      throw new IllegalStateException("Tried to access git repository after close");
    }
    final @Nullable GitWrapper wrapper = this.projectRepos.get(project.getProjectDir());
    if(wrapper != null) return wrapper.git; // found

    // Attempt to compute a repository based on the project info
    // Travel up the directory tree to try and locate projects
    final File rawProjectDir = project.getProjectDir();
    final File rootProjectDir;
    final File realProjectDir;
    try {
      rootProjectDir = this.getParameters().getBaseDirectory().get().getAsFile().getCanonicalFile();
      realProjectDir = rawProjectDir.getCanonicalFile();
      if(!realProjectDir.getPath().startsWith(rootProjectDir.getPath())) {
        throw new IllegalArgumentException("Project directory " + rawProjectDir + " was no within the root project!");
      }

      File targetDir = realProjectDir;
      do {
        if(isGitDir(targetDir)) {
          final GitWrapper potentialExisting = this.projectRepos.get(targetDir);
          if(potentialExisting != null) {
            // Once values make it into the map, they are the only possibility
            this.projectRepos.put(rawProjectDir, potentialExisting);
            return potentialExisting.git;
          }

          try {
            final Git repo = Git.open(realProjectDir);

            GitWrapper repoWrapper = new GitWrapper(repo);
            final GitWrapper existing = this.projectRepos.putIfAbsent(targetDir, repoWrapper);
            if(existing != null) { // only maintain one instance
              repo.close();
              repoWrapper = existing;
            }

            this.projectRepos.put(rawProjectDir, repoWrapper);
            return repoWrapper.git;
          } catch(final RepositoryNotFoundException ignored) {
            // continue up the directory tree
          }
        }
      } while((!rootProjectDir.equals(targetDir)) && (targetDir = targetDir.getParentFile()) != null);
      // At this point we're not found
      this.projectRepos.put(rawProjectDir, GitWrapper.NOT_FOUND);
    } catch(final IOException ex) {
      project.getLogger().warn("Failed to open git repository for {}:", project.getDisplayName(), ex);
    }
    return null;
  }

  private static boolean isGitDir(final File file) {
    return new File(file, ".git").exists();
  }

  @Override
  public void close() {
    this.open = false;
    final Set<GitWrapper> repos = new HashSet<>(this.projectRepos.values());
    this.projectRepos.clear();
    for(final GitWrapper wrapper : repos) {
      if(wrapper.git != null) {
        wrapper.git.close();
      }
    }
  }

  private static class GitWrapper {
    static final GitWrapper NOT_FOUND = new GitWrapper(null);

    final @Nullable Git git;

    GitWrapper(final @Nullable Git repo) {
      this.git = repo;
    }
  }
}
