/*
 * This file is part of indra, licensed under the MIT License.
 *
 * Copyright (c) 2020-2022 KyoriPowered
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

import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;
import org.gradle.api.Action;
import org.gradle.api.JavaVersion;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.plugins.scala.ScalaPlugin;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.scala.ScalaCompile;
import org.gradle.api.tasks.scala.ScalaCompileOptions;
import org.gradle.jvm.toolchain.JavaLanguageVersion;
import org.gradle.jvm.toolchain.JavaLauncher;
import org.gradle.jvm.toolchain.JavaToolchainService;
import org.jetbrains.annotations.NotNull;

public class ScalaSupport implements LanguageSupport {
  private final JavaToolchainService toolchains;

  @Inject
  public ScalaSupport(final JavaToolchainService toolchains) {
    this.toolchains = toolchains;
  }

  @Override
  public void registerApplyCallback(final @NotNull Project project, final @NotNull Action<? super Project> callback) {
    project.getPlugins().withType(ScalaPlugin.class, $ -> {
      callback.execute(project);
    });
  }

  @Override
  public void configureCompileTasks(final @NotNull Project project, final @NotNull SourceSet sourceSet, final @NotNull Provider<Integer> toolchainVersion, final @NotNull Provider<Integer> bytecodeVersion) {
    final Provider<JavaLauncher> launcher = this.toolchains.launcherFor(spec -> spec.getLanguageVersion().set(toolchainVersion.map(JavaLanguageVersion::of)));
    final String expectedName = sourceSet.getCompileTaskName("scala");
    project.getTasks().withType(ScalaCompile.class).matching(it -> it.getName().equals(expectedName)).configureEach(task -> {
      final ScalaCompileOptions options = task.getScalaCompileOptions();
      options.setEncoding(DEFAULT_ENCODING);
      options.setDeprecation(true);

      if (HAS_GRADLE_7_2) {
        task.getJavaLauncher().set(launcher);
      }

      final String compatibility = JavaVersion.toVersion(bytecodeVersion.get()).toString();
      task.setSourceCompatibility(compatibility);
      task.setTargetCompatibility(compatibility);
      task.getOptions().getRelease().set(bytecodeVersion);
      task.doFirst(new ParameterAdder(bytecodeVersion));
    });
  }

  static class ParameterAdder implements Action<Task> {
    private final @NotNull Provider<Integer> target;

    public ParameterAdder(final Provider<Integer> target) {
      this.target = target;
    }

    @Override
    public void execute(final Task task) {
      List<String> options = ((ScalaCompile) task).getScalaCompileOptions().getAdditionalParameters();
      if (options != null && options.stream().anyMatch(opt -> opt.startsWith("-target:"))) {
        return;
      } else if (options == null) {
        options = new ArrayList<>();
        ((ScalaCompile) task).getScalaCompileOptions().setAdditionalParameters(options);
      }

      options.add("-target:" + this.target.get());
    }
  }

  // TODO: Scaladoc
  // -target:<bytecodeVersion>
  // -jdk-api-doc-base
  //
}
