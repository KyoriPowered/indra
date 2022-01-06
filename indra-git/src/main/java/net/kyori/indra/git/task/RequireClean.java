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
package net.kyori.indra.git.task;

import net.kyori.indra.git.internal.IndraGitService;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.Status;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.gradle.api.DefaultTask;
import org.gradle.api.GradleException;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.TaskAction;
import org.gradle.language.base.plugins.LifecycleBasePlugin;
import org.jetbrains.annotations.Nullable;

/**
 * Require that the project has no files that are uncommitted to SCM.
 *
 * <p>This prevents accidentally publishing content that does not match the
 * published source.</p>
 *
 * @since 2.0.0
 */
public abstract class RequireClean extends DefaultTask {
  public RequireClean() {
    this.setGroup(LifecycleBasePlugin.VERIFICATION_GROUP);
  }

  @Internal
  public abstract Property<IndraGitService> getGit();

  @TaskAction
  public void check() {
    final @Nullable Git git = this.getGit().get().git(this.getProject());
    if(git == null) return;

    try {
      final Status status = git.status().call();
      if(!status.isClean()) {
        final StringBuilder message = new StringBuilder("Source root must be clean! Make sure your changes are committed. Changed files:");
        for(final String changed : status.getUncommittedChanges()) {
          message.append(System.lineSeparator())
            .append("- ")
            .append(changed);
        }
        for(final String untracked : status.getUntracked()) {
          message.append(System.lineSeparator())
            .append("- ")
            .append(untracked);
        }

        throw new GradleException(message.toString());
      }
    } catch(final GitAPIException ex) {
      this.getLogger().error("Failed to query clean status of current project repository", ex);
    }
  }
}
