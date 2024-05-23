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
package net.kyori.indra.git;

import java.util.List;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Ref;
import org.gradle.api.Action;
import org.gradle.api.java.archives.Manifest;
import org.gradle.api.provider.Provider;
import org.gradle.api.provider.ValueSourceSpec;
import org.jetbrains.annotations.NotNull;

/**
 * An extension exposing git information.
 *
 * <p>To find the appropriate repository, indra-git will look for a git repository in the root project directory.</p>
 *
 * <p>This will search in the current project's directory, and if the
 * project is not a git checkout, will traverse parent directories until
 * a {@code .git} folder is found.</p>
 *
 * @since 2.0.0
 */
public interface IndraGitExtension {
  /**
   * The manifest attribute used to indicate the git commit an archive is built from.
   *
   * @since 2.0.0
   */
  String MANIFEST_ATTRIBUTE_GIT_COMMIT = "Git-Commit";

  /**
   * The manifest attribute used to indicate the git branch an archive is built from.
   *
   * @since 2.0.0
   */
  String MANIFEST_ATTRIBUTE_GIT_BRANCH = "Git-Branch";

  /**
   * Get if a git repository is present.
   *
   * @return whether or not a git repository is present for the current project.
   * @since 2.0.0
   */
  boolean isPresent();

  /**
   * Create a parameterless which uses a value from a git repository.
   *
   * @param valueSource the value source type
   * @param <V> the value type
   * @param <P> the value source type
   * @return the value provider
   * @since 4.0.0
   */
  default <V, P extends RepositoryValueSource.Parameters, S extends RepositoryValueSource<V, P>> Provider<V> repositoryValue(final Class<S> valueSource) {
    return this.repositoryValue(valueSource, $ -> {});
  }

  /**
   * Create a provider which uses a value from a git repository.
   *
   * @param valueSource the value source type
   * @param configureAction an action to configure any additional parameters on the value source
   * @param <V> the value type
   * @param <P> the value source type
   * @return the value provider
   * @since 4.0.0
   */
  <V, P extends RepositoryValueSource.Parameters, S extends RepositoryValueSource<V, P>> Provider<V> repositoryValue(final Class<S> valueSource, final Action<? super ValueSourceSpec<P>> configureAction);

  /**
   * Get all tags created on this repository.
   *
   * @return the tags on this repository, or an empty list if this project is not in a git repository
   * @since 2.0.0
   */
  @NotNull Provider<? extends List<? extends Ref>> tags();

  /**
   * Get the tag pointing to the commit checked out as {@code HEAD}.
   *
   * @return the tag at {@code HEAD}, or {@code null} if the project is not in a git repository or is not checked out to a tag
   * @since 2.0.0
   */
  @NotNull Provider<Ref> headTag();

  /**
   * Get a <a href="https://git-scm.com/docs/git-describe">{@code git describe}</a> string for the project's repository.
   *
   * <p>The result will be equivalent to the result of executing {@code git describe --tags --long}</p>
   *
   * @return the describe string, or {@code null} if this project is not in a git repository or if there are no tags in the project's history
   * @since 2.0.0
   */
  @NotNull Provider<String> describe();

  /**
   * Get the name of the current branch.
   *
   * @return the name of the active branch, or {@code null} if the project is not in a git repository or is checked out to a detached {@code HEAD}.
   * @since 2.0.0
   */
  @NotNull Provider<String> branchName();

  /**
   * Get an object pointing to the current branch.
   *
   * @return the active branch, or {@code null} if the project is not in a git repository or is checked out to a detached {@code HEAD}.
   * @since 2.0.0
   */
  @NotNull Provider<Ref> branch();

  /**
   * Get the ID of the current commit.
   *
   * @return the commit id, or {@code null} if the project is not in a git repository or has not had its initial commit
   * @since 2.0.0
   */
  @NotNull Provider<ObjectId> commit();

  /**
   * Apply metadata about the current git state to the provided manifest.
   *
   * <p>Any unavailable state will not </p>
   *
   * <p>Current supported parameters are:</p>
   * <dl>
   *   <dt><code>Git-Branch</code></dt>
   *   <dd>The current branch being built</dd>
   *   <dt><code>Git-Commit</code></dt>
   *   <dd>The current commit being built</dd>
   * </dl>
   *
   * @param manifest the manifest to decorate
   * @since 2.0.0
   */
  default void applyVcsInformationToManifest(final Manifest manifest) {
    if (this.isPresent()) {
      // Git-Commit and Git-Branch
      final Provider<ObjectId> commit = this.commit();
      final Provider<String> branchName = this.branchName();
      if (commit.isPresent()) manifest.getAttributes().put(MANIFEST_ATTRIBUTE_GIT_COMMIT, commit.get().name());
      if (branchName.isPresent()) manifest.getAttributes().put(MANIFEST_ATTRIBUTE_GIT_BRANCH, branchName.get());
    }
  }
}
