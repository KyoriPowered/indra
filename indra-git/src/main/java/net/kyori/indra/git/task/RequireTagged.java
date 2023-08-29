/*
 * This file is part of indra, licensed under the MIT License.
 *
 * Copyright (c) 2023 KyoriPowered
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

import net.kyori.indra.git.internal.IndraGitExtensionImpl;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.Ref;
import org.gradle.api.GradleException;
import org.gradle.api.tasks.TaskAction;
import org.jetbrains.annotations.Nullable;

/**
 * Require that the current project is checked out to a tag.
 *
 * @since 4.0.0
 */
public abstract class RequireTagged extends RepositoryTask {
  /**
   * Perform the task action.
   *
   * @since 4.0.0
   */
  @TaskAction
  public void checkTagged() {
    final @Nullable Git repo = this.repo();
    final @Nullable Ref tag = repo == null ? null : IndraGitExtensionImpl.headTag(repo);

    if (tag == null && repo != null) {
      throw new GradleException("The current repository must be checked out to a tagged commit to perform this operation.");
    }
  }

}
