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

import java.util.Arrays;
import java.util.Collections;
import javax.inject.Inject;
import net.kyori.indra.internal.PropertyUtils;
import net.kyori.indra.util.Versioning;
import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.compile.CompileOptions;
import org.gradle.api.tasks.compile.JavaCompile;
import org.gradle.api.tasks.javadoc.Javadoc;
import org.gradle.external.javadoc.JavadocOptionFileOption;
import org.gradle.external.javadoc.MinimalJavadocOptions;
import org.gradle.external.javadoc.StandardJavadocDocletOptions;
import org.gradle.jvm.toolchain.JavaLanguageVersion;
import org.gradle.jvm.toolchain.JavaToolchainService;
import org.gradle.jvm.toolchain.JavadocTool;
import org.gradle.process.CommandLineArgumentProvider;
import org.jetbrains.annotations.NotNull;

public class JavaSupport implements LanguageSupport {
  private final JavaToolchainService toolchains;

  @Inject
  public JavaSupport(final JavaToolchainService toolchains) {
    this.toolchains = toolchains;
  }

  @Override
  public void registerApplyCallback(final @NotNull Project project, final @NotNull Action<? super Project> callback) {
    project.getPlugins().withType(JavaPlugin.class, $ -> {
      callback.execute(project);
    });
  }

  @Override
  public void configureCompileTasks(final @NotNull Project project, final @NotNull SourceSet sourceSet, final @NotNull Provider<Integer> toolchainVersion, final @NotNull Provider<Integer> bytecodeVersion) {
    project.getTasks().named(sourceSet.getCompileJavaTaskName(), JavaCompile.class, task -> {
      final CompileOptions options = task.getOptions();
      options.setEncoding(DEFAULT_ENCODING);
      PropertyUtils.applyFinalizingAndLogging(task.getJavaCompiler(), this.toolchains.compilerFor(spec -> spec.getLanguageVersion().set(toolchainVersion.map(JavaLanguageVersion::of))), task.getName());

      PropertyUtils.applyFinalizingAndLogging(options.getRelease(), toolchainVersion.flatMap(toolchain -> toolchain >= 9 ? bytecodeVersion : null), task.getName());
      // bleh
      final String compatibility = Versioning.versionString(PropertyUtils.getAndLog(bytecodeVersion, task.getName()));
      task.setSourceCompatibility(compatibility);
      task.setTargetCompatibility(compatibility);

      options.getCompilerArgumentProviders().add(new IndraCompileArgumentProvider(PropertyUtils.logValueComputation(toolchainVersion, task.getName())));
    });
  }

  @Override
  public void configureDocTasks(final @NotNull Project project, final @NotNull SourceSet sourceSet, final @NotNull Provider<Integer> toolchainVersion, final @NotNull Provider<Integer> targetVersion) {
    final String taskName = sourceSet.getJavadocTaskName();
    final Provider<JavadocTool> javadocTool = this.toolchains.javadocToolFor(spec -> PropertyUtils.applyFinalizingAndLogging(spec.getLanguageVersion(), toolchainVersion.map(JavaLanguageVersion::of), "javadoc"));
    project.getTasks().withType(Javadoc.class).matching(t -> t.getName().equals(taskName)).configureEach(task -> {
      final MinimalJavadocOptions minimalOpts = task.getOptions();
      minimalOpts.setEncoding(DEFAULT_ENCODING);
      task.getJavadocTool().set(javadocTool);

      if (minimalOpts instanceof StandardJavadocDocletOptions) {
        final StandardJavadocDocletOptions options = (StandardJavadocDocletOptions) minimalOpts;
        options.charSet(DEFAULT_ENCODING);

        task.getInputs().property("targetVersion", targetVersion);
        task.doFirst(new IndraJavadocPrepareAction(PropertyUtils.logValueComputation(targetVersion, task.getName()), options));
      }
    });
  }

  static final class IndraCompileArgumentProvider implements CommandLineArgumentProvider {
    private final @NotNull Provider<Integer> toolchainVersion;

    IndraCompileArgumentProvider(final @NotNull Provider<Integer> toolchainVersion) {
      this.toolchainVersion = toolchainVersion;
    }

    @Override
    public Iterable<String> asArguments() {
      if (this.toolchainVersion.get() >= 9) {
        return Arrays.asList(
          "-Xdoclint",
          "-Xdoclint:-missing"
        );
      } else {
        return Collections.emptyList();
      }
    }
  }

  static final class IndraJavadocPrepareAction implements Action<Task> {
    private final @NotNull Provider<Integer> targetVersion;
    private final JavadocOptionFileOption<String> release;

    IndraJavadocPrepareAction(final @NotNull Provider<Integer> targetVersion, final StandardJavadocDocletOptions initOptions) {
      this.targetVersion = targetVersion;
      this.release = initOptions.addStringOption("-release");
    }

    @Override
    public void execute(final @NotNull Task t) {
      final StandardJavadocDocletOptions options = (StandardJavadocDocletOptions) ((Javadoc) t).getOptions();
      final int target = this.targetVersion.get();
      final int actual = ((Javadoc) t).getJavadocTool().get().getMetadata().getLanguageVersion().asInt();
      // Java 16 automatically links with the API documentation anyways
      if (actual < 16) {
        options.links(jdkApiDocs(target));
      }
      if (actual >= 9) {
        this.release.setValue(Integer.toString(target));
      } else {
        options.setSource(Versioning.versionString(target));
      }
    }
  }

  private static String jdkApiDocs(final int javaVersion) {
    final String template;
    if (javaVersion >= 11) {
      template = "https://docs.oracle.com/en/java/javase/%s/docs/api";
    } else {
      template = "https://docs.oracle.com/javase/%s/docs/api";
    }
    return String.format(template, javaVersion);
  }
}
