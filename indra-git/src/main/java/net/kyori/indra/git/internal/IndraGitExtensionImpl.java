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
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import javax.inject.Inject;
import net.kyori.indra.git.IndraGitExtension;
import net.kyori.indra.git.RepositoryValueSource;
import net.kyori.mammoth.Configurable;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.RefNotFoundException;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevObject;
import org.eclipse.jgit.revwalk.RevWalk;
import org.gradle.api.Action;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;
import org.gradle.api.provider.Provider;
import org.gradle.api.provider.ProviderFactory;
import org.gradle.api.provider.ValueSourceSpec;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.VisibleForTesting;

public class IndraGitExtensionImpl implements IndraGitExtension {
  private static final Logger LOGGER = Logging.getLogger(IndraGitExtensionImpl.class);
  private final ProviderFactory providers;
  private final File rootDir;
  private final File projectDir;
  private final String displayName;

  @Inject
  public IndraGitExtensionImpl(final ProviderFactory providers, final File rootDir, final File projectDir, final String displayName) {
    this.rootDir = rootDir;
    this.providers = providers;
    this.projectDir = projectDir;
    this.displayName = displayName;
  }

  @Override
  public boolean isPresent() {
    return this.git() != null;
  }

  @VisibleForTesting
  public @Nullable Git git() {
    return GitCache.getOrCreate(this.rootDir).git(this.projectDir, this.displayName);
  }

  @Override
  public <V, P extends RepositoryValueSource.Parameters, S extends RepositoryValueSource<V, P>> Provider<V> repositoryValue(final Class<S> valueSource, final Action<? super ValueSourceSpec<P>> configureAction) {
    return this.providers.of(valueSource, spec -> {
      final RepositoryValueSource.Parameters params = spec.getParameters();
      params.getRootDir().set(this.rootDir);
      params.getProjectDir().set(this.projectDir);
      params.getDisplayName().set(this.displayName);
      Configurable.configure(spec, configureAction);
    });
  }

  public static abstract class QueryTags extends RepositoryValueSource.Parameterless<List<? extends Ref>> {
    @Override
    protected @Nullable List<? extends Ref> obtain(final @NotNull Git repository) {
      try {
        return repository.tagList().call();
      } catch (final GitAPIException ex) {
        LOGGER.error("Failed to query git for a list of tags:", ex);
        return Collections.emptyList();
      }
    }
  }

  @Override
  public @NotNull Provider<? extends List<? extends Ref>> tags() {
    return this.repositoryValue(QueryTags.class).orElse(Collections.emptyList());
  }

  public static @Nullable Ref headTag(final Git git) {
    try {
      final @Nullable Ref head = git.getRepository().findRef(Constants.HEAD);
      if (head == null) return null;

      final ObjectId headCommit = head.getLeaf().getObjectId();

      try (final RevWalk walk = new RevWalk(git.getRepository())) {
        for (final Ref tag : git.tagList().call()) {
          final RevObject parsed = walk.peel(walk.parseAny(tag.getObjectId()));
          final ObjectId commitId = parsed.toObjectId();

          if (ObjectId.isEqual(commitId, headCommit)) {
            return tag;
          }
        }
      }
    } catch (final IOException | GitAPIException ex) {
      LOGGER.error("Failed to resolve current HEAD tag:", ex);
    }
    return null;
  }

  public static abstract class QueryHeadTag extends RepositoryValueSource.Parameterless<Ref> {
    @Override
    protected @Nullable Ref obtain(final @NotNull Git repository) {
      return IndraGitExtensionImpl.headTag(repository);
    }
  }

  @Override
  public @NotNull Provider<Ref> headTag() {
    return this.repositoryValue(QueryHeadTag.class);
  }

  public static abstract class QueryDescribe extends RepositoryValueSource.Parameterless<String> {
    @Override
    protected @Nullable String obtain(final @NotNull Git repository) {
      try {
        return repository.describe().setTags(true).setLong(true).call();
      } catch (final RefNotFoundException ex) {
        // there is no HEAD when in a git repo without a commit
        return null;
      } catch (final GitAPIException ex) {
        LOGGER.error("Failed to query git for a 'describe' result:", ex);
        return null;
      }
    }
  }

  @Override
  public @NotNull Provider<String> describe() {
    return this.repositoryValue(QueryDescribe.class);
  }

  @Override
  public @NotNull Provider<String> branchName() {
    return this.branch().map(branch -> Repository.shortenRefName(branch.getName()));
  }

  public static abstract class QueryBranch extends RepositoryValueSource.Parameterless<Ref> {
    @Override
    protected @Nullable Ref obtain(final @NotNull Git repository) {
      try {
        final @Nullable Ref ref = repository.getRepository().exactRef(Constants.HEAD);
        if (ref == null || !ref.isSymbolic()) return null; // no HEAD, or detached HEAD

        return ref.getTarget();
      } catch(final IOException ex) {
        LOGGER.error("Failed to query current branch name from git:", ex);
        return null;
      }
    }
  }

  @Override
  public @NotNull Provider<Ref> branch() {
    return this.repositoryValue(QueryBranch.class);
  }

  public static abstract class QueryCommit extends RepositoryValueSource.Parameterless<ObjectId> {
    @Override
    protected @Nullable ObjectId obtain(final @NotNull Git repository) {
      try {
        final @Nullable Ref head = repository.getRepository().exactRef(Constants.HEAD);
        if (head == null) return null;

        return head.getObjectId();
      } catch (final IOException ex) {
        LOGGER.error("Failed to query git for the current HEAD commit:", ex);
        return null;
      }
    }
  }

  @Override
  public @NotNull Provider<ObjectId> commit() {
    return this.repositoryValue(QueryCommit.class);
  }
}
