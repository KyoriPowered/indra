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
package net.kyori.indra.task;

import net.kyori.indra.util.VersionControl;
import org.ajoberstar.grgit.Grgit;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.gradle.api.DefaultTask;
import org.gradle.api.GradleException;
import org.gradle.api.tasks.TaskAction;
import org.gradle.language.base.plugins.LifecycleBasePlugin;

/**
 * Require that the project has no files that are uncommitted to SCM.
 *
 * <p>This prevents accidentally publishing content that does not match the
 * published source.</p>
 *
 * @since 1.0.0
 */
public class RequireClean extends DefaultTask {
  public static final String NAME = "requireClean";

  public RequireClean() {
    this.setGroup(LifecycleBasePlugin.VERIFICATION_GROUP);
  }

  @TaskAction
  public void check() {
    final @Nullable Grgit grgit = VersionControl.grgit(this.getProject());
    if(grgit != null && !grgit.status().isClean()) {
      throw new GradleException("Source root must be clean! Make sure your changes are committed");
    }
  }
}
