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

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import javax.inject.Inject;
import net.kyori.indra.git.IndraGitExtension;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.RefNotFoundException;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.gradle.api.Project;
import org.gradle.api.logging.Logger;
import org.gradle.api.provider.Provider;

public class IndraGitExtensionImpl implements IndraGitExtension {
  private final Logger logger;
  private final Provider<Git> service;

  @Inject
  public IndraGitExtensionImpl(final Project project, final Provider<IndraGitService> service) {
    this.logger = project.getLogger();
    this.service = service.map(s -> s.git(project));
  }

  @Override
  public @Nullable Git git() {
    return this.service.getOrNull();
  }

  @Override
  public @NonNull List<Ref> tags() {
    final @Nullable Git git = this.git();
    if(git == null) return Collections.emptyList();

    try {
      return git.tagList().call();
    } catch(final GitAPIException ex) {
      this.logger.error("Failed to query git for a list of tags:", ex);
      return Collections.emptyList();
    }
  }

  @Override
  public @Nullable Ref headTag() {
    final @Nullable Git git = this.git();
    if(git == null) return null;

    try {
      final @Nullable Ref head = git.getRepository().findRef(Constants.HEAD);
      if(head == null) return null;

      final ObjectId headCommit = head.getLeaf().getObjectId();

      for(final Ref tag : git.tagList().call()) {
        @Nullable ObjectId tagId = tag.getPeeledObjectId();
        if(tagId == null) tagId = tag.getObjectId();

        if(ObjectId.isEqual(tagId, headCommit)) {
          return tag;
        }
      }
    } catch(final IOException | GitAPIException ex) {
      this.logger.error("Failed to resolve current HEAD tag:", ex);
    }
    return null;
  }

  @Override
  public @Nullable String describe() {
    final @Nullable Git git = this.git();
    if(git == null) return null;

    try {
      return git.describe().setTags(true).setLong(true).call();
    } catch(final RefNotFoundException ex) {
      // there is no HEAD when in a git repo without a commit
      return null;
    } catch(final GitAPIException ex) {
      this.logger.error("Failed to query git for a 'describe' result:", ex);
      return null;
    }
  }

  @Override
  public @Nullable String branchName() {
    final @Nullable Git git = this.git();
    if(git == null) return null;

    final @Nullable Ref branch = this.branch();
    return branch == null ? null : Repository.shortenRefName(branch.getName());
  }

  @Override
  public @Nullable Ref branch() {
    final @Nullable Git git = this.git();
    if(git == null) return null;

    try {
      final @Nullable Ref ref = git.getRepository().exactRef(Constants.HEAD);
      if(ref == null || !ref.isSymbolic()) return null; // no HEAD, or detached HEAD

      return ref.getTarget();
    } catch(final IOException ex) {
      this.logger.error("Failed to query current branch name from git:", ex);
      return null;
    }
  }

  @Override
  public @Nullable ObjectId commit() {
    final @Nullable Git git = this.git();
    if(git == null) return null;

    try {
      final @Nullable Ref head = git.getRepository().exactRef(Constants.HEAD);
      if(head == null) return null;

      return head.getObjectId();
    } catch(final IOException ex) {
      this.logger.error("Failed to query git for the current HEAD commit:", ex);
      return null;
    }
  }
}
