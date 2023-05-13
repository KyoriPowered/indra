/*
 * This file is part of indra, licensed under the MIT License.
 *
 * Copyright (c) 2020-2023 KyoriPowered
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

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import net.kyori.indra.internal.IndraExtensionImpl;
import net.kyori.indra.internal.SonatypeRepositoriesImpl;
import net.kyori.indra.internal.language.LanguageSupport;
import net.kyori.indra.internal.multirelease.IndraMultireleasePlugin;
import net.kyori.indra.repository.SonatypeRepositories;
import net.kyori.mammoth.ProjectPlugin;
import net.kyori.mammoth.Properties;
import org.gradle.api.Action;
import org.gradle.api.JavaVersion;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.dsl.RepositoryHandler;
import org.gradle.api.plugins.BasePluginExtension;
import org.gradle.api.plugins.ExtensionAware;
import org.gradle.api.plugins.ExtensionContainer;
import org.gradle.api.plugins.JavaLibraryPlugin;
import org.gradle.api.plugins.JavaPluginExtension;
import org.gradle.api.plugins.PluginContainer;
import org.gradle.api.plugins.PluginManager;
import org.gradle.api.provider.Property;
import org.gradle.api.provider.Provider;
import org.gradle.api.provider.SetProperty;
import org.gradle.api.tasks.JavaExec;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.SourceSetContainer;
import org.gradle.api.tasks.TaskContainer;
import org.gradle.api.tasks.TaskProvider;
import org.gradle.api.tasks.bundling.AbstractArchiveTask;
import org.gradle.api.tasks.compile.CompileOptions;
import org.gradle.api.tasks.compile.JavaCompile;
import org.gradle.api.tasks.javadoc.Javadoc;
import org.gradle.api.tasks.testing.Test;
import org.gradle.external.javadoc.JavadocOptionFileOption;
import org.gradle.external.javadoc.MinimalJavadocOptions;
import org.gradle.external.javadoc.StandardJavadocDocletOptions;
import org.gradle.jvm.toolchain.JavaLanguageVersion;
import org.gradle.jvm.toolchain.JavaToolchainService;
import org.gradle.language.base.plugins.LifecycleBasePlugin;
import org.gradle.language.jvm.tasks.ProcessResources;
import org.gradle.plugins.ide.api.GeneratorTask;
import org.gradle.plugins.ide.eclipse.model.EclipseModel;
import org.gradle.util.GradleVersion;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * The primary Indra plugin providing project configuration.
 *
 * @since 1.0.0
 */
public class IndraPlugin implements ProjectPlugin {
  private static final String DIFFPLUG_GOOMPH_APT = "com.diffplug.eclipse.apt";
  private static final String[] APT_TASKS = {"eclipseJdtApt", "eclipseJdt", "eclipseFactorypath"};

  @Override
  public @Nullable GradleVersion minimumGradleVersion() {
    return Indra.MINIMUM_SUPPORTED;
  }

  @Override
  public void apply(final @NotNull Project project, final @NotNull PluginContainer plugins, final @NotNull ExtensionContainer extensions, final @NotNull TaskContainer tasks) {
    plugins.apply(JavaLibraryPlugin.class);
    if (GradleVersion.current().compareTo(GradleVersion.version("7.0")) >= 0) {
      // Fix a lot of JVM library artifact inconsistencies
      // Gradle <7.0 doesn't provide the necessary attributes for this to work effectively
      plugins.apply("org.gradlex.java-ecosystem-capabilities");
    }

    final IndraExtensionImpl indra = (IndraExtensionImpl) Indra.extension(extensions);

    project.getExtensions().getByType(BasePluginExtension.class).getArchivesName().set(project.getName().toLowerCase(Locale.ROOT));

    extensions.configure(JavaPluginExtension.class, java -> {
      java.getToolchain().getLanguageVersion().set(indra.javaVersions().actualVersion().map(JavaLanguageVersion::of));
    });
    this.applyIdeConfigurationOptions(project.getPluginManager(), extensions, tasks);

    final Provider<String> projectVersion = project.provider(() -> {
      final String raw = String.valueOf(project.getVersion());
      if (raw.equals("unspecified")) {
        return null;
      } else {
        return raw;
      }
    });
    tasks.withType(JavaCompile.class).configureEach(task -> {
      final CompileOptions options = task.getOptions();
      options.getCompilerArgs().addAll(Arrays.asList(
        // Generate metadata for reflection on method parameters
        "-parameters",
        // Enable all warnings
        "-Xlint:all"
      ));

      // Enable preview features if option is set in extension
      options.getCompilerArgumentProviders().add(indra.previewFeatureArgumentProvider());

      // Provide a module version when compiling module infos
      options.getJavaModuleVersion().set(projectVersion);
    });

    tasks.withType(JavaExec.class).configureEach(task -> {
      task.getArgumentProviders().add(indra.previewFeatureArgumentProvider());
    });

    tasks.withType(Javadoc.class).configureEach(task -> {
      // Apply preview feature flag
      final MinimalJavadocOptions minimalOpts = task.getOptions();
      if (minimalOpts instanceof StandardJavadocDocletOptions) {
        final StandardJavadocDocletOptions options = (StandardJavadocDocletOptions) minimalOpts;
        final JavadocOptionFileOption<Boolean> enablePreview = options.addBooleanOption("-enable-preview");
        final JavadocOptionFileOption<Boolean> doclintMissing = options.addBooleanOption("Xdoclint:-missing");
        final JavadocOptionFileOption<Boolean> html5 = options.addBooleanOption("html5");
        final JavadocOptionFileOption<Boolean> noModuleDirectories = options.addBooleanOption("-no-module-directories");
        final Property<Boolean> previewFeaturesEnabledProp = indra.javaVersions().previewFeaturesEnabled();
        task.doFirst(new Action<Task>() {
          @Override
          public void execute(final @NotNull Task t) {
            final int actual = ((Javadoc) t).getJavadocTool().get().getMetadata().getLanguageVersion().asInt();

            if (actual >= 9) {
              if (actual < 12) {
                // Apply workaround for https://bugs.openjdk.java.net/browse/JDK-8215291
                // This will probably never be backported......
                noModuleDirectories.setValue(true);
              }
              if (actual >= 12) {
                enablePreview.setValue(previewFeaturesEnabledProp.get());
              }

              doclintMissing.setValue(true);
              html5.setValue(true);
            }
          }
        });
      }
    });

    tasks.withType(ProcessResources.class).configureEach(task -> {
      task.setFilteringCharset(LanguageSupport.DEFAULT_ENCODING);
    });

    extensions.configure(JavaPluginExtension.class, extension -> {
      extension.withJavadocJar();
      extension.withSourcesJar();
    });

    tasks.withType(Test.class).configureEach(Test::useJUnitPlatform);

    // If we are publishing, publish java
    indra.configurePublications(publication -> {
      if (Properties.finalized(indra.includeJavaSoftwareComponentInPublications()).get()) {
        publication.from(project.getComponents().getByName("java"));
      }
    });

    // For things that are eagerly applied (field accesses, anything where you need to `get()`)
    project.afterEvaluate(p -> {
      extensions.configure(JavaPluginExtension.class, javaPlugin -> {
        final Property<Integer> versionProp = Properties.finalized(indra.javaVersions().target());
        javaPlugin.setSourceCompatibility(JavaVersion.toVersion(versionProp.get()));
        javaPlugin.setTargetCompatibility(JavaVersion.toVersion(versionProp.get()));
      });

      if (indra.reproducibleBuilds().get()) {
        tasks.withType(AbstractArchiveTask.class).configureEach(archive -> {
          archive.setPreserveFileTimestamps(false);
          archive.setReproducibleFileOrder(true);
        });
      }

      // Set up testing on the selected Java versions
      final JavaToolchainService toolchains = extensions.getByType(JavaToolchainService.class);
      final SetProperty<Integer> testWithProp = Properties.finalized(indra.javaVersions().testWith());
      final Provider<SourceSet> testSet = p.getExtensions().getByType(SourceSetContainer.class)
        .named(SourceSet.TEST_SOURCE_SET_NAME);
      final Provider<Configuration> testRuntimeClasspathConfig = testSet
        .flatMap(set -> p.getConfigurations().named(set.getRuntimeClasspathConfigurationName()));
      final List<String> requestedTasks = p.getGradle().getStartParameter().getTaskNames();
      testWithProp.get().forEach(targetRuntime -> {
        // Create task that will use that version
        final Property<Boolean> strictVersions = indra.javaVersions().strictVersions();
        final Provider<Integer> actualVersion = indra.javaVersions().actualVersion();
        final TaskProvider<Test> versionedTest = tasks.register(Indra.testJava(targetRuntime), Test.class, test -> {
          test.setDescription("Runs tests on Java " + targetRuntime + " if necessary based on build settings");
          test.setGroup(LifecycleBasePlugin.VERIFICATION_GROUP);
          test.setClasspath(testRuntimeClasspathConfig.get().getIncoming().getFiles());
          test.setTestClassesDirs(testSet.get().getOutput().getClassesDirs());

          test.onlyIf($ -> {
            // Only run if explicitly requested, our runtime is not the standard runtime, and we're doing strict versions.
            return requestedTasks.contains(test.getName()) || requestedTasks.contains(test.getPath())
              || strictVersions.get() && !Objects.equals(targetRuntime, actualVersion.get());
          });
          test.getJavaLauncher().set(toolchains.launcherFor(it -> it.getLanguageVersion().set(strictVersions.zip(actualVersion, (strict, actual) -> JavaLanguageVersion.of(strict ? targetRuntime : actual)))));
        });

        tasks.named(LifecycleBasePlugin.CHECK_TASK_NAME).configure(it -> it.dependsOn(versionedTest));
      });
    });

    // Give our actions priority over the multirelease afterEvaluate actions (eww)
    plugins.apply(IndraMultireleasePlugin.class);
    this.registerRepositoryExtensions(project.getRepositories());
  }

  private void registerRepositoryExtensions(final RepositoryHandler repositories) {
    // Sonatype OSSRH (new, support for more hosts)
    ((ExtensionAware) repositories).getExtensions().create(
      SonatypeRepositories.class,
      SonatypeRepositories.EXTENSION_NAME,
      SonatypeRepositoriesImpl.class,
      repositories
    );
  }

  private void applyIdeConfigurationOptions(final PluginManager manager, final ExtensionContainer extensions, final TaskContainer tasks) {
    // also applies the eclipse plugin
    manager.withPlugin(DIFFPLUG_GOOMPH_APT, applied -> {
      extensions.configure(EclipseModel.class, eclipse -> {
        // https://github.com/diffplug/goomph/issues/125
        // buildship pls stop being broken thanks
        for (final String task : APT_TASKS) {
          eclipse.synchronizationTasks(tasks.named(task));
        }
        // To handle updating dependencies properly, we need to overwrite the factorypath, not just add to it.
        tasks.named("eclipseFactorypath", GeneratorTask.class, t -> {
          t.doFirst(new CleanFactorypath());
        });
      });

    });
  }

  static class CleanFactorypath implements Action<Task> {
    @Override
    public void execute(final @NotNull Task arg0) {
      final GeneratorTask<?> generator = (GeneratorTask<?>) arg0;
      if (generator.getInputFile().exists()) {
        generator.getInputFile().delete();
      }
    }
  }

}
