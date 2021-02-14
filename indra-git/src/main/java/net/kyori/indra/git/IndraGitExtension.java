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

import java.util.List;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Ref;
import org.gradle.api.java.archives.Manifest;

/**
 * An extension exposing git information.
 *
 * <p>If the current project is not in a git repository, </p>
 */
public interface IndraGitExtension {
  static String MANIFEST_ATTRIBUTE_GIT_COMMIT = "Git-Commit";
  static String MANIFEST_ATTRIBUTE_GIT_BRANCH = "Git-Branch";

  /**
   * Get if a git repository is present.
   *
   * @return whether or not a git repository is present for the current project.
   */
  default boolean isPresent() {
    return this.git() != null;
  }

  /**
   * Access the underlying Git repository.
   *
   * <p>This will look for a git repository in the root project directory.</p>
   *
   * <!-- TODO: This will search in the current project's directory, and if the
   * project is not a git checkout, will traverse parent projects until
   * a {@code .git} folder is found. -->
   *
   * @return the git repository
   */
  @Nullable Git git();

  List<Ref> tags();

  @Nullable Ref headTag();

  String describe();

  String branchName();

  @Nullable Ref branch();

  @Nullable ObjectId commit();

  /**
   * Apply metadata about the current git state to the provided manifest.
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
   */
  default void applyVcsInformationToManifest(final Manifest manifest) {
    if(this.isPresent()) {
      // Git-Commit and Git-Branch
      manifest.getAttributes().put(MANIFEST_ATTRIBUTE_GIT_COMMIT, this.commit().name());
      manifest.getAttributes().put(MANIFEST_ATTRIBUTE_GIT_BRANCH, this.branchName());
    }
  }
}
