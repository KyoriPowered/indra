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
package net.kyori.indra.internal.language;

import javax.inject.Inject;
import org.gradle.api.Action;
import org.gradle.api.JavaVersion;
import org.gradle.api.Project;
import org.gradle.api.plugins.GroovyPlugin;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.compile.GroovyCompile;
import org.gradle.jvm.toolchain.JavaLanguageVersion;
import org.gradle.jvm.toolchain.JavaLauncher;
import org.gradle.jvm.toolchain.JavaToolchainService;
import org.jetbrains.annotations.NotNull;

/**
 * Support for Groovy-language plugins
 */
public class GroovySupport implements LanguageSupport {
  private static final String GROOVY = "groovy";

  private final JavaToolchainService toolchains;

  @Inject
  public GroovySupport(final JavaToolchainService toolchains) {
    this.toolchains = toolchains;
  }

  @Override
  public void registerApplyCallback(final @NotNull Project project, final @NotNull Action<? super Project> callback) {
    project.getPlugins().withType(GroovyPlugin.class, $ -> {
      callback.execute(project);
    });
  }

  @Override
  public void configureCompileTasks(final Project project, final SourceSet sourceSet, final Provider<Integer> toolchainVersion, final Provider<Integer> bytecodeVersion) {
    final Provider<JavaLauncher> launcher = this.toolchains.launcherFor(spec -> spec.getLanguageVersion().set(bytecodeVersion.map(v -> JavaLanguageVersion.of(v))));
    project.getTasks().named(sourceSet.getCompileTaskName(GROOVY), GroovyCompile.class, groovyCompile -> {
      groovyCompile.getOptions().getRelease().set(bytecodeVersion);
      if (HAS_GRADLE_7_2) {
        // The Groovy plugin doesn't allow cross-compiling, so we have to use the specific target JDK
        groovyCompile.getJavaLauncher().set(launcher);
      }
      final String compatibility = JavaVersion.toVersion(bytecodeVersion.get()).toString();
      groovyCompile.setSourceCompatibility(compatibility);
      groovyCompile.setTargetCompatibility(compatibility);

      groovyCompile.getGroovyOptions().setEncoding(DEFAULT_ENCODING);
    });
  }

  /*@Override
  public void configureDocTasks(@NotNull Project project, @NotNull SourceSet sourceSet, @NotNull Provider<Integer> toolchainVersion, @NotNull Provider<Integer> targetVersion) {
    project.getTasks().withType(Groovydoc.class).matching(t -> t.getName().equals(sourceSet.getTaskName(null, GroovyPlugin.GROOVYDOC_TASK_NAME))).configureEach(task -> {
      task.get
    });
  }*/
}