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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.errors.RepositoryNotFoundException;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;
import org.gradle.api.services.BuildService;
import org.gradle.api.services.BuildServiceParameters;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A build service providing the most relevant git repository for any project in the build.
 *
 * @since 2.0.0
 */
public abstract class IndraGitService implements BuildService<IndraGitService.Parameters>, AutoCloseable {
  private static final Logger LOGGER = Logging.getLogger(IndraGitService.class);

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
   * @param projectDir the project directory to locate a git repo in
   * @param displayName the display name for the context being queried
   * @return the build's git repository.
   * @since 2.0.0
   */
  public @Nullable Git git(final File projectDir, final @NotNull String displayName) {
    if(!this.open) {
      throw new IllegalStateException("Tried to access git repository after close");
    }
    final @Nullable GitWrapper wrapper = this.projectRepos.get(projectDir);
    if(wrapper != null) return wrapper.git; // found

    // Attempt to compute a repository based on the project info
    // Travel up the directory tree to try and locate projects
    final File rawProjectDir = projectDir;
    final File rootProjectDir;
    final File realProjectDir;
    try {
      rootProjectDir = this.getParameters().getBaseDirectory().get().getAsFile().getCanonicalFile();
      realProjectDir = rawProjectDir.getCanonicalFile();
      if(!realProjectDir.getPath().startsWith(rootProjectDir.getPath())) {
        throw new IllegalArgumentException("Project directory " + rawProjectDir + " was not within the root project!");
      }

      File targetDir = realProjectDir;
      do {
        if(isGitDir(targetDir)) {
          LOGGER.debug("indra-git: Examining directory {} for {}", targetDir, displayName);
          final GitWrapper potentialExisting = this.projectRepos.get(targetDir);
          if(potentialExisting != null) {
            LOGGER.info("indra-git: Found existing git repository for {} starting in directory {} via {}", displayName, rawProjectDir, targetDir);
            // Once values make it into the map, they are the only possibility
            this.projectRepos.put(rawProjectDir, potentialExisting);
            return potentialExisting.git;
          }

          try {
            final @Nullable File realGit = resolveGit(targetDir);
            if (realGit == null) continue;
            final Git repo = Git.open(realGit);

            GitWrapper repoWrapper = new GitWrapper(repo);
            final GitWrapper existing = this.projectRepos.putIfAbsent(targetDir, repoWrapper);
            if(existing != null) { // only maintain one instance
              repo.close();
              repoWrapper = existing;
            } else {
              LOGGER.info("indra-git: Located and initialized repository for project {} in {}, with git directory at {}", displayName, targetDir, repo.getRepository().getDirectory());
            }

            this.projectRepos.put(rawProjectDir, repoWrapper);
            return repoWrapper.git;
          } catch(final RepositoryNotFoundException ex) {
            LOGGER.debug("indra-git: Unable to open repository found in {} for {}", targetDir, displayName, ex);
            // continue up the directory tree
          }
        } else {
          LOGGER.debug("indra-git: Skipping directory {} while locating repository for {}", targetDir, displayName);
        }
      } while((!rootProjectDir.equals(targetDir)) && (targetDir = targetDir.getParentFile()) != null);
      // At this point we're not found
      this.projectRepos.put(rawProjectDir, GitWrapper.NOT_FOUND);
    } catch(final IOException ex) {
      LOGGER.warn("indra-git: Failed to open git repository for {}:", displayName, ex);
    }
    LOGGER.info("indra-git: No git repository found for {}", displayName);
    return null;
  }

  private static final String GIT_DIR = ".git";
  private static final String GITDIR_PREFIX = "gitdir:";

  private static boolean isGitDir(final File file) {
    return new File(file, GIT_DIR).exists();
  }

  private static File resolveGit(File projectDir) throws IOException {
    // The `.git` folder is not always a folder, sometimes it's also a file
    // We only support checked-out working trees, so we don't have to consider the bare repository case here
    // https://git-scm.com/docs/gitrepository-layout
    if (!projectDir.getName().equals(GIT_DIR)) {
      return resolveGit(new File(projectDir, ".git"));
    } else {
      projectDir = projectDir.getCanonicalFile();
      if (projectDir.isDirectory()) {
        return projectDir;
      } else if (projectDir.isFile()) {
        try (final BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(projectDir), StandardCharsets.UTF_8))) {
          String line;
          while ((line = reader.readLine()) != null) {
            if (line.startsWith(GITDIR_PREFIX)) {
              return new File(projectDir.getParentFile(), line.substring(GITDIR_PREFIX.length()).trim());
            }
          }
        }
      }
      LOGGER.warn("indra-git: Unable to determine actual git directory from {}", projectDir);
      return null;
    }
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
