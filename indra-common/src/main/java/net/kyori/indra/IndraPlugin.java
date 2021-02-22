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
package net.kyori.indra;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import net.kyori.gradle.api.ProjectPlugin;
import net.kyori.indra.repository.RemoteRepository;
import net.kyori.indra.repository.Repositories;
import net.kyori.indra.util.Versioning;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.gradle.api.JavaVersion;
import org.gradle.api.Project;
import org.gradle.api.plugins.BasePluginConvention;
import org.gradle.api.plugins.Convention;
import org.gradle.api.plugins.ExtensionContainer;
import org.gradle.api.plugins.JavaLibraryPlugin;
import org.gradle.api.plugins.JavaPluginExtension;
import org.gradle.api.plugins.PluginContainer;
import org.gradle.api.provider.Property;
import org.gradle.api.provider.SetProperty;
import org.gradle.api.tasks.JavaExec;
import org.gradle.api.tasks.TaskContainer;
import org.gradle.api.tasks.TaskProvider;
import org.gradle.api.tasks.bundling.AbstractArchiveTask;
import org.gradle.api.tasks.compile.CompileOptions;
import org.gradle.api.tasks.compile.JavaCompile;
import org.gradle.api.tasks.javadoc.Javadoc;
import org.gradle.api.tasks.testing.Test;
import org.gradle.external.javadoc.JavadocOptionFileOption;
import org.gradle.external.javadoc.StandardJavadocDocletOptions;
import org.gradle.jvm.toolchain.JavaLanguageVersion;
import org.gradle.jvm.toolchain.JavaToolchainService;
import org.gradle.language.base.plugins.LifecycleBasePlugin;
import org.gradle.language.jvm.tasks.ProcessResources;

public class IndraPlugin implements ProjectPlugin {
  @Override
  public void apply(final @NonNull Project project, final @NonNull PluginContainer plugins, final @NonNull ExtensionContainer extensions, final @NonNull Convention convention, final @NonNull TaskContainer tasks) {
    plugins.apply(JavaLibraryPlugin.class);

    final IndraExtensionImpl indra = (IndraExtensionImpl) Indra.extension(extensions);

    convention.getPlugin(BasePluginConvention.class).setArchivesBaseName(project.getName().toLowerCase());

    tasks.withType(JavaCompile.class, task -> {
      final CompileOptions options = task.getOptions();
      options.setEncoding(StandardCharsets.UTF_8.name());
      options.getCompilerArgs().addAll(Arrays.asList(
        // Generate metadata for reflection on method parameters
        "-parameters",
        // Enable all warnings
        "-Xlint:all"
      ));

      // JDK 9+ only arguments
      options.getCompilerArgumentProviders().add(() -> {
        if(indra.javaVersions().minimumToolchain().get() >= 9) {
          return Arrays.asList(
            "-Xdoclint",
            "-Xdoclint:-missing"
          );
        } else {
          return Collections.emptyList();
        }
      });

      // Enable preview features if option is set in extension
      options.getCompilerArgumentProviders().add(indra.previewFeatureArgumentProvider());
    });

    tasks.withType(JavaExec.class).configureEach(task -> {
      task.getArgumentProviders().add(indra.previewFeatureArgumentProvider());
    });

    tasks.withType(Javadoc.class, task -> {
      task.options(options -> {
        options.setEncoding(StandardCharsets.UTF_8.name());

        if(options instanceof StandardJavadocDocletOptions) {
          ((StandardJavadocDocletOptions) options).charSet(StandardCharsets.UTF_8.name());
        }
      });
    });

    tasks.withType(ProcessResources.class, task -> {
      task.setFilteringCharset(StandardCharsets.UTF_8.name());
    });

    extensions.configure(JavaPluginExtension.class, extension -> {
      extension.withJavadocJar();
      extension.withSourcesJar();
    });

    tasks.withType(Test.class, Test::useJUnitPlatform);

    // If we are publishing, publish java
    indra.configurePublications(publication -> {
      indra.includeJavaSoftwareComponentInPublications().finalizeValue();
      if(indra.includeJavaSoftwareComponentInPublications().get()) {
        publication.from(project.getComponents().getByName("java"));
      }
    });

    // For things that are eagerly applied (field accesses, anything where you need to `get()`)
    project.afterEvaluate(p -> {
      extensions.configure(JavaPluginExtension.class, javaPlugin -> {
        final Property<Integer> versionProp = indra.javaVersions().target();
        versionProp.finalizeValue();
        javaPlugin.setSourceCompatibility(JavaVersion.toVersion(versionProp.get()));
        javaPlugin.setTargetCompatibility(JavaVersion.toVersion(versionProp.get()));
      });

      tasks.withType(JavaCompile.class).configureEach(compile -> {
        final Property<Integer> release = compile.getOptions().getRelease();
        if(!release.isPresent() && indra.javaVersions().minimumToolchain().get() >= 9) {
          release.set(indra.javaVersions().target());
        }
      });

      if(indra.reproducibleBuilds().get()) {
        tasks.withType(AbstractArchiveTask.class).configureEach(archive -> {
          archive.setPreserveFileTimestamps(false);
          archive.setReproducibleFileOrder(true);
        });
      }

      tasks.withType(Javadoc.class).configureEach(jd -> {
        if(jd.getOptions() instanceof StandardJavadocDocletOptions) {
          final StandardJavadocDocletOptions options = (StandardJavadocDocletOptions) jd.getOptions();
          final JavadocOptionFileOption<Boolean> doclintMissing = options.addBooleanOption("Xdoclint:-missing");
          final JavadocOptionFileOption<Boolean> html5 = options.addBooleanOption("html5");
          final JavadocOptionFileOption<String> release = options.addStringOption("-release");
          final JavadocOptionFileOption<Boolean> enablePreview = options.addBooleanOption("-enable-preview");
          final JavadocOptionFileOption<Boolean> noModuleDirectories = options.addBooleanOption("-no-module-directories");

          jd.doFirst(t -> {
            final JavaToolchainVersions versions = indra.javaVersions();
            final int target = versions.target().get();
            options.links(jdkApiDocs(target));

            if(versions.minimumToolchain().get() >= 9) {
              if(versions.actualVersion().get() < 12) {
                // Apply workaround for https://bugs.openjdk.java.net/browse/JDK-8215291
                // Hopefully this gets backported some day... (JDK-8215291)
                noModuleDirectories.setValue(true);
              }

              release.setValue(Integer.toString(target));
              doclintMissing.setValue(true);
              html5.setValue(true);
              enablePreview.setValue(versions.previewFeaturesEnabled().get());
            } else {
              options.setSource(Versioning.versionString(target));
            }
          });
        }
      });

      // Set up testing on the selected Java versions
      final JavaToolchainService toolchains = extensions.getByType(JavaToolchainService.class);
      final SetProperty<Integer> testWithProp = indra.javaVersions().testWith();
      testWithProp.finalizeValue();
      testWithProp.get().forEach(targetRuntime -> {
        // Create task that will use that version
        final TaskProvider<Test> versionedTest = tasks.register("testJava$targetRuntime", Test.class, test -> {
          test.setDescription("Runs tests on Java $targetRuntime if necessary based on build settings");
          test.setGroup(LifecycleBasePlugin.VERIFICATION_GROUP);
          // Appropriate classpath and test class source information is set on all test tasks by JavaPlugin

          test.onlyIf($ -> {
            // Only run if our runtime is not the standard runtime, and we're doing strict versions.
            return indra.javaVersions().strictVersions().get() && targetRuntime != indra.javaVersions().actualVersion().get();
          });
          test.getJavaLauncher().set(toolchains.launcherFor(it -> it.getLanguageVersion().set(JavaLanguageVersion.of(targetRuntime))));
        });

        tasks.named(LifecycleBasePlugin.CHECK_TASK_NAME).configure(it -> it.dependsOn(versionedTest));
      });
    });

    // TODO: Repository extensions in Kotlin buildscript
    Repositories.registerRepositoryExtensions(project.getRepositories(), RemoteRepository.SONATYPE_SNAPSHOTS);
  }

  private static String jdkApiDocs(final int javaVersion) {
    final String template;
    if(javaVersion >= 11) {
      template = "https://docs.oracle.com/en/java/javase/%s/docs/api";
    } else {
      template = "https://docs.oracle.com/javase/%s/docs/api";
    }
    return String.format(template, javaVersion);
  }
}
