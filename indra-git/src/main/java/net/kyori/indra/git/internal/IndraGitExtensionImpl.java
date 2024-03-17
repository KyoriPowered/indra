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
package net.kyori.indra.git.internal;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import javax.inject.Inject;
import net.kyori.indra.git.IndraGitExtension;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.RefNotFoundException;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevObject;
import org.eclipse.jgit.revwalk.RevWalk;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;
import org.gradle.api.provider.Property;
import org.gradle.api.provider.Provider;
import org.gradle.api.provider.ProviderFactory;
import org.gradle.api.provider.ValueSource;
import org.gradle.api.provider.ValueSourceParameters;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class IndraGitExtensionImpl implements IndraGitExtension {
  private static final Logger LOGGER = Logging.getLogger(IndraGitExtensionImpl.class);
  private final Provider<IndraGitService> service;
  private final ProviderFactory providers;
  private final File projectDir;
  private final String displayName;

  @Inject
  public IndraGitExtensionImpl(final ProviderFactory providers, final File projectDir, final String displayName, final Provider<IndraGitService> service) {
    this.providers = providers;
    this.projectDir = projectDir;
    this.displayName = displayName;

    this.service = service;
  }

  @SuppressWarnings("unchecked")
  protected <V> Provider<V> repoQuery(final Function<Git, V> provider) {
    return this.providers.of(GitRepoValueSource.class, spec -> {
      final GitRepoValueSource.Params params = spec.getParameters();

      params.getService().set(this.service);
      params.getProjectDirectory().set(this.projectDir);
      params.getProjectDisplayName().set(this.displayName);
      params.getValueProvider().set(provider);
    }).map(v -> (V) v);
  }

  static abstract class GitRepoValueSource implements ValueSource<Object, GitRepoValueSource.Params> {
    interface Params extends ValueSourceParameters {
      Property<IndraGitService> getService();
      DirectoryProperty getProjectDirectory();
      Property<String> getProjectDisplayName();

      Property<Function<Git, ?>> getValueProvider();
    }

    @Nullable
    @Override
    public Object obtain() {
      final Params params = this.getParameters();
      final Git git = params.getService().get().git(params.getProjectDirectory().get().getAsFile(), params.getProjectDisplayName().get());
      if (git == null) return null;

      return params.getValueProvider().get().apply(git);
    }
  }

  @Override
  public @NotNull Provider<Git> git() {
    return this.service.map(service -> service.git(this.projectDir, this.displayName));
  }

  @Override
  public @NotNull Provider<? extends List<? extends Ref>> tags() {
    return this.git().<List<Ref>>map(git -> {
      try {
        return git.tagList().call();
      } catch(final GitAPIException ex) {
        LOGGER.error("Failed to query git for a list of tags:", ex);
        return Collections.emptyList();
      }
    }).orElse(Collections.emptyList());
  }

  public static @Nullable Ref headTag(final Git git) {
    try {
      final @Nullable Ref head = git.getRepository().findRef(Constants.HEAD);
      if(head == null) return null;

      final ObjectId headCommit = head.getLeaf().getObjectId();

      try(final RevWalk walk = new RevWalk(git.getRepository())) {
        for(final Ref tag : git.tagList().call()) {
          final RevObject parsed = walk.peel(walk.parseAny(tag.getObjectId()));
          final ObjectId commitId = parsed.toObjectId();

          if(ObjectId.isEqual(commitId, headCommit)) {
            return tag;
          }
        }
      }
    } catch(final IOException | GitAPIException ex) {
      LOGGER.error("Failed to resolve current HEAD tag:", ex);
    }
    return null;
  }

  @Override
  public @NotNull Provider<Ref> headTag() {
    return this.git().map(IndraGitExtensionImpl::headTag);
  }

  @Override
  public @NotNull Provider<String> describe() {
    return this.git().map(git -> {
      try {
        return git.describe().setTags(true).setLong(true).call();
      } catch(final RefNotFoundException ex) {
        // there is no HEAD when in a git repo without a commit
        return null;
      } catch(final GitAPIException ex) {
        LOGGER.error("Failed to query git for a 'describe' result:", ex);
        return null;
      }
    });
  }

  @Override
  public @NotNull Provider<String> branchName() {
    return this.branch().map(branch -> Repository.shortenRefName(branch.getName()));
  }

  @Override
  public @NotNull Provider<Ref> branch() {
    return this.git().map(git -> {
      try {
        final @Nullable Ref ref = git.getRepository().exactRef(Constants.HEAD);
        if(ref == null || !ref.isSymbolic()) return null; // no HEAD, or detached HEAD

        return ref.getTarget();
      } catch(final IOException ex) {
        LOGGER.error("Failed to query current branch name from git:", ex);
        return null;
      }
    });
  }

  @Override
  public @NotNull Provider<ObjectId> commit() {
    return this.git().map(git -> {
      try {
        final @Nullable Ref head = git.getRepository().exactRef(Constants.HEAD);
        if(head == null) return null;

        return head.getObjectId();
      } catch(final IOException ex) {
        LOGGER.error("Failed to query git for the current HEAD commit:", ex);
        return null;
      }
    });
  }
}
