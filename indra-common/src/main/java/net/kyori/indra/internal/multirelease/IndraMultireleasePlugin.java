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
package net.kyori.indra.internal.multirelease;

import java.io.File;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import net.kyori.indra.Indra;
import net.kyori.indra.IndraExtension;
import net.kyori.indra.multirelease.MultireleaseSourceSet;
import net.kyori.indra.multirelease.MultireleaseVariantDetails;
import net.kyori.mammoth.ProjectPlugin;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.gradle.api.Action;
import org.gradle.api.GradleException;
import org.gradle.api.NamedDomainObjectProvider;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.dsl.DependencyHandler;
import org.gradle.api.file.FileCollection;
import org.gradle.api.file.ProjectLayout;
import org.gradle.api.file.SourceDirectorySet;
import org.gradle.api.plugins.Convention;
import org.gradle.api.plugins.ExtensionContainer;
import org.gradle.api.plugins.JavaBasePlugin;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.plugins.PluginContainer;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.Copy;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.SourceSetContainer;
import org.gradle.api.tasks.SourceSetOutput;
import org.gradle.api.tasks.TaskContainer;
import org.gradle.api.tasks.TaskProvider;
import org.gradle.api.tasks.bundling.AbstractArchiveTask;
import org.gradle.api.tasks.bundling.Jar;
import org.gradle.api.tasks.compile.AbstractCompile;
import org.gradle.api.tasks.compile.JavaCompile;
import org.gradle.api.tasks.testing.Test;
import org.gradle.jvm.toolchain.JavaCompiler;
import org.gradle.jvm.toolchain.JavaLanguageVersion;
import org.gradle.jvm.toolchain.JavaToolchainService;
import org.gradle.language.jvm.tasks.ProcessResources;

/**
 * Multirelease jar plugin.
 *
 * <p>Applied within {@link net.kyori.indra.IndraPlugin}.</p>
 *
 * @since 2.0.0
 */
public class IndraMultireleasePlugin implements ProjectPlugin {

  // Based on guidance at https://blog.gradle.org/mrjars
  // and an example at https://github.com/melix/mrjar-gradle

  private static final String MULTI_RELEASE_ATTRIBUTE = "Multi-Release";
  private static final String MULTI_RELEASE_PATH = "META-INF/versions/";
  private static final String CLASSES_VARIANT = "classes"; // apiElements and runtimeElements
  private static final String RESOURCES_VARAINT = "resources"; // runtimeElements

  @Override
  public void apply(final @NonNull Project project, final @NonNull PluginContainer plugins, final @NonNull ExtensionContainer extensions, final @NonNull Convention convention, final @NonNull TaskContainer tasks) {
    // Once the source set container is created, configure the multirelease extension
    plugins.withType(JavaBasePlugin.class, plugin -> {
      final SourceSetContainer sourceSets = extensions.getByType(SourceSetContainer.class);
      this.configureMultiRelease(project, tasks, project.getDependencies(), sourceSets);

      // Then configure standard extra options for main source set and test source set
      plugins.withType(JavaPlugin.class, javaPlugin -> {
        this.configureStandardSourceSetMultireleaseActions(project, Indra.extension(extensions), tasks, sourceSets);
      });
    });
  }

  private void configureMultiRelease(final Project project, final TaskContainer tasks, final DependencyHandler dependencies, final SourceSetContainer sourceSets) {
    final Set<String> alternateNames = ConcurrentHashMap.newKeySet();
    // Perform early setup for things that need to be visible in buildscripts
    sourceSets.all(parent -> {
      if(alternateNames.contains(parent.getName())) {
        // ignore source sets we create ourself
        return;
      }
      final MultireleaseSourceSet multireleaseExtension = parent.getExtensions().create(MultireleaseSourceSet.class, "multirelease", MultireleaseSourceSetImpl.class, project.getObjects());

      multireleaseExtension.alternateVersions().whenObjectAdded(version -> {
        // Ideally we'd be able to initialize the source set here, but for some reason gradle won't let us do that...
        final String derivedSetName = MultireleaseSourceSetImpl.versionName(parent, version);
        alternateNames.add(derivedSetName);
      });
    });

    project.afterEvaluate(p -> {
      final IndraExtension indra = Indra.extension(p.getExtensions());
      final JavaToolchainService javaToolchains = p.getExtensions().getByType(JavaToolchainService.class);

      // Now that the dust has settled, link all the pieces together
      sourceSets.matching(set -> !alternateNames.contains(set.getName())).all(base -> {
        final MultireleaseSourceSetImpl extension = (MultireleaseSourceSetImpl) MultireleaseSourceSet.from(base);

        // Get ourselves an array of versions in order
        final int[] versions = extension.alternateVersions().stream().mapToInt(Integer::intValue).toArray();
        Arrays.sort(versions);

        // Validate that all versions are valid
        final int baseVersion = indra.javaVersions().target().get();
        for(final int version : versions) {
          if(version <= baseVersion) {
            throw new GradleException("Found declared multirelease variant (version " + version + ") of source set '" + base.getName() + "' which was lower than the base version (" + baseVersion + ")");
          } else if(version <= MultireleaseSourceSetImpl.MINIMUM_MULTIRELEASE_VERSION) {
            throw new GradleException("Multirelease jars can only be used for variants targeting a Java version greater than " + MultireleaseSourceSetImpl.MINIMUM_MULTIRELEASE_VERSION + ", but " + version + " was provided in source set " + base.getName());
          }
        }

        final NamedDomainObjectProvider<Configuration> baseApiElements = project.getConfigurations().getNames().contains(base.getApiElementsConfigurationName()) ? project.getConfigurations().named(base.getApiElementsConfigurationName()) : null;
        final NamedDomainObjectProvider<Configuration> baseRuntimeElements = project.getConfigurations().getNames().contains(base.getRuntimeElementsConfigurationName()) ? project.getConfigurations().named(base.getRuntimeElementsConfigurationName()) : null;

        for(int idx = 0, length = versions.length; idx < length; ++idx) {
          final int version = versions[idx];
          // Configure classpath
          final SourceSet parent;
          if(idx == 0) {
            parent = base;
          } else {
            parent = sourceSets.getByName(MultireleaseSourceSetImpl.versionName(base, versions[idx - 1]));
          }

          final SourceSet variant = sourceSets.maybeCreate(MultireleaseSourceSetImpl.versionName(base, version));
          // Source + resource directories
          variant.getJava().setSrcDirs(this.applySourceDirectories(version, base.getJava().getSrcDirs()));
          variant.getResources().setSrcDirs(this.applySourceDirectories(version, base.getResources().getSrcDirs()));

          // Classpath
          dependencies.add(variant.getImplementationConfigurationName(), parent.getOutput());
          variant.setCompileClasspath(variant.getCompileClasspath().plus(parent.getCompileClasspath()));
          variant.setRuntimeClasspath(variant.getRuntimeClasspath().plus(parent.getRuntimeClasspath()));

          // --patch-module with all source dirs from alternates
          final MultireleaseModulePatchArgumentProvider modulePatch = new MultireleaseModulePatchArgumentProvider(p.getObjects());
          modulePatch.getModuleName().set(extension.moduleName());
          modulePatch.getClassDirectories().from(base.getOutput());
          for(int i = 0; i < idx; ++i) {
            modulePatch.getClassDirectories().from(sourceSets.named(MultireleaseSourceSetImpl.versionName(base, versions[i])).map(SourceSet::getOutput));
          }
          final Provider<JavaCompiler> compiler = javaToolchains.compilerFor(spec -> {
            spec.getLanguageVersion().set(indra.javaVersions().actualVersion().map(activeEnv -> JavaLanguageVersion.of(Math.max(activeEnv, version)))); // make sure our JVM is always compatible
          });

          final TaskProvider<JavaCompile> compileJava = tasks.named(variant.getCompileJavaTaskName(), JavaCompile.class, task -> {
            task.getOptions().getRelease().set(version);
            task.getJavaCompiler().set(compiler);

            task.getOptions().getCompilerArgumentProviders().add(modulePatch);
          });

          // Add classes to the base jar
          this.addMultireleaseVariantToJars(tasks, base, variant, version);

          // Add classes to the appropriate outgoing variants of the base configuration
          this.addMultireleaseVariantToBaseOutgoingVariants(tasks, compileJava, variant, baseApiElements, baseRuntimeElements);

          // Then execute user-defined tasks
          if(!extension.alternateConfigurationActions.isEmpty()) {
            final MultireleaseVariantDetails details = MultireleaseVariantDetails.details(base, version, variant);
            for(final Action<MultireleaseVariantDetails> action : extension.alternateConfigurationActions) {
              action.execute(details);
            }
          }
        }

        // Now, just once for the source set if we do in fact have versions,
        if(versions.length > 0) {
          this.configureMultireleaseJarManifestAttribute(tasks, base);
        }
      });
    });
  }

  private void addMultireleaseVariantToJars(final TaskContainer tasks, final SourceSet base, final SourceSet variant, final int version) {
    // Add classes to the base jar
    final SourceSetOutput output = variant.getOutput();
    final String jarTaskName = base.getJarTaskName();
    tasks.matching(task -> task.getName().equals(jarTaskName)).configureEach(task -> {
      final Jar jarTask = (Jar) task;
      jarTask.into(MULTI_RELEASE_PATH + version, spec -> spec.from(output));
    });

    // Add sources to the sources jar
    // TODO: do we want to maybe create multiple sources jars, one for each target version?
    final SourceDirectorySet allSource = variant.getAllSource();
    final String sourcesJarTaskName = base.getSourcesJarTaskName();
    tasks.matching(task -> task.getName().equals(sourcesJarTaskName)).configureEach(task -> {
      final Jar jarTask = (Jar) task;
      jarTask.into(MULTI_RELEASE_PATH + version, spec -> spec.from(allSource));
    });

  }

  private void addMultireleaseVariantToBaseOutgoingVariants(
    final TaskContainer tasks,
    final TaskProvider<JavaCompile> compileJava,
    final SourceSet variant,
    final NamedDomainObjectProvider<Configuration> baseApiElements,
    final NamedDomainObjectProvider<Configuration> baseRuntimeElements
  ) {
    // Add classes to the appropriate outgoing variants of the base configuration
    if(baseApiElements != null) {
      baseApiElements.configure(conf -> {
        // TODO: this isn't entirely accurate, since it won't capture every input to the jar task
        conf.getOutgoing().getVariants().named(CLASSES_VARIANT, classesVariant -> {
          classesVariant.artifact(
            compileJava.flatMap(AbstractCompile::getDestinationDirectory),
            artifact -> artifact.builtBy(compileJava)
          );
        });
      });
    }
    if(baseRuntimeElements != null) {
      final TaskProvider<ProcessResources> processResources = tasks.named(variant.getProcessResourcesTaskName(), ProcessResources.class);
      baseRuntimeElements.configure(conf -> {
        conf.getOutgoing().getVariants().named(CLASSES_VARIANT, classesVariant -> {
          classesVariant.artifact(
            compileJava.flatMap(AbstractCompile::getDestinationDirectory),
            artifact -> artifact.builtBy(compileJava)
          );
        });
        conf.getOutgoing().getVariants().named(RESOURCES_VARAINT, classesVariant -> {
          classesVariant.artifact(
            processResources.map(Copy::getDestinationDir),
            artifact -> artifact.builtBy(processResources)
          );
        });
      });
    }
  }

  private void configureMultireleaseJarManifestAttribute(final TaskContainer tasks, final SourceSet base) {
    tasks.matching(task -> task.getName().equals(base.getJarTaskName())).configureEach(task -> {
      final Jar jarTask = (Jar) task;
      jarTask.getManifest().getAttributes().put(MULTI_RELEASE_ATTRIBUTE, true);
    });
  }

  private Set<File> applySourceDirectories(final int version, final Set<File> originalFiles) {
    final Set<File> sourceDirs = new HashSet<>(originalFiles.size());
    for(final File file : originalFiles) {
      sourceDirs.add(new File(file.getParentFile(), file.getName() + version));
    }
    return sourceDirs;
  }

  private void configureStandardSourceSetMultireleaseActions(final Project project, final IndraExtension indra, final TaskContainer tasks, final SourceSetContainer sourceSets) {
    final SourceSet mainSet = sourceSets.getByName(SourceSet.MAIN_SOURCE_SET_NAME);
    final MultireleaseSourceSet main = MultireleaseSourceSet.from(mainSet);

    // Configure test tasks to build using a jar as soon as the `main` source set has actions
    project.afterEvaluate(p -> {
      if(!main.alternateVersions().isEmpty()) {
        final ProjectLayout layout = p.getLayout();
        final TaskProvider<Jar> jarTask = tasks.named(mainSet.getJarTaskName(), Jar.class);
        final SourceSetOutput mainOutput = mainSet.getOutput();
        tasks.withType(Test.class).configureEach(test -> {
          test.dependsOn(jarTask);
          test.setClasspath(layout.files(jarTask.flatMap(AbstractArchiveTask::getArchiveFile), test.getClasspath()).minus(mainOutput));
        });
      }
    });

    // Add test variants to the indra-created versioned test tasks
    // Also add to the primary test task depending on the active JVM version (when strict multirelease variants aren't present)
    final MultireleaseSourceSet test = MultireleaseSourceSet.from(sourceSets.getByName(SourceSet.TEST_SOURCE_SET_NAME));
    // Add to the global tested versions
    test.alternateVersions().whenObjectAdded(version -> indra.javaVersions().testWith().add(version));

    // And configure classpaths
    test.configureVariants(details -> {
      final Provider<Integer> actualVersion = indra.javaVersions().actualVersion();
      final FileCollection testClassesDirs = details.variant().getOutput().getClassesDirs();
      final FileCollection runtimeClasspath = details.variant().getRuntimeClasspath();
      final String variantCompile = details.variant().getCompileJavaTaskName();
      final int target = details.targetVersion();

      // If our primary JDK is the target, then let's add the variant's classes to the main test task
      tasks.named(JavaPlugin.TEST_TASK_NAME, Test.class, testTask -> {
        if(actualVersion.get() >= target) {
          testTask.setTestClassesDirs(testTask.getTestClassesDirs().plus(testClassesDirs));
          testTask.setClasspath(testTask.getClasspath().plus(runtimeClasspath));
          testTask.dependsOn(variantCompile);
        }
      });

      // But always add to the java 16-specific test task
      // todo: extract a method for this name (shared with versioned test setup in IndraPlugin)
      tasks.matching(it -> it.getName().equals("testJava" + target)).configureEach(task -> {
        if(!(task instanceof Test)) return;
        final Test testTask = (Test) task;

        testTask.setTestClassesDirs(testTask.getTestClassesDirs().plus(testClassesDirs));
        testTask.setClasspath(testTask.getClasspath().plus(runtimeClasspath));
        testTask.dependsOn(variantCompile);
      });
    });
  }
}
