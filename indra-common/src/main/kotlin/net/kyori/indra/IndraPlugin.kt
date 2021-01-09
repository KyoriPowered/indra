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
package net.kyori.indra

import net.kyori.indra.util.versionString
import org.gradle.api.Action
import org.gradle.api.JavaVersion
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.BasePluginConvention
import org.gradle.api.plugins.JavaLibraryPlugin
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.tasks.JavaExec
import org.gradle.api.tasks.bundling.AbstractArchiveTask
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.api.tasks.javadoc.Javadoc
import org.gradle.api.tasks.testing.Test
import org.gradle.external.javadoc.StandardJavadocDocletOptions
import org.gradle.jvm.toolchain.JavaLanguageVersion
import org.gradle.jvm.toolchain.JavaToolchainService
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.get
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.getPlugin
import org.gradle.kotlin.dsl.withType
import org.gradle.language.base.plugins.LifecycleBasePlugin
import org.gradle.process.CommandLineArgumentProvider

class IndraPlugin : Plugin<Project> {
  override fun apply(project: Project) {
    with(project) {
      val extension = extension(project)

      apply<JavaLibraryPlugin>()

      convention.getPlugin<BasePluginConvention>().archivesBaseName = project.name.toLowerCase()

      extensions.getByType(JavaPluginExtension::class).apply {
        // Ensure that we're only running the latest version
        this.toolchain.languageVersion.set(extension.javaVersions.actualVersion.map { JavaLanguageVersion.of(it) })
      }

      tasks.withType<JavaCompile>().configureEach {compile ->
        compile.options.apply {
          encoding = Charsets.UTF_8.name()
          compilerArgs.addAll(
            listOf(
              // Generate metadata for reflection on method parameters
              "-parameters",
              // Enable all warnings
              "-Xlint:all"
            )
          )

          // JDK 9+ only arguments
          compilerArgumentProviders += CommandLineArgumentProvider {
            if(extension.javaVersions.minimumToolchain.get() >= 9) {
              listOf(
                "-Xdoclint",
                "-Xdoclint:-missing"
              )
            } else {
              emptyList()
            }
          }

          // Enable preview features if option is set in extension
          compilerArgumentProviders += extension.previewFeatureArgumentProvider()
        }
      }

      tasks.withType(JavaExec::class).configureEach {
        // Enable preview features if option is set in extension
        it.jvmArgumentProviders += extension.previewFeatureArgumentProvider()
      }

      tasks.withType<Javadoc>().configureEach {
        with(it.options) {
          encoding = Charsets.UTF_8.name()

          if(this is StandardJavadocDocletOptions) {
            charSet = Charsets.UTF_8.name()
          }
        }
      }

      extensions.configure<JavaPluginExtension> {
        withJavadocJar()
        withSourcesJar()
      }

      tasks.withType<Test>().configureEach {
        it.useJUnitPlatform()
      }

      registerRepositoryExtensions(repositories, DEFAULT_REPOSITORIES)

      // If we are publishing, publish java
      extension.configurePublications(Action {
        extension.includeJavaSoftwareComponentInPublications.finalizeValue()
        if(extension.includeJavaSoftwareComponentInPublications.get()) {
          it.from(components["java"])
        }
      })

      // For things that are eagerly applied (field accesses, anything where you need to `get()`)
      afterEvaluate {
        extensions.configure<JavaPluginExtension> {
          val versionProp = extension.javaVersions.target
          versionProp.finalizeValue()
          sourceCompatibility = JavaVersion.toVersion(versionProp.get())
          targetCompatibility = JavaVersion.toVersion(versionProp.get())
        }

        tasks.withType(JavaCompile::class).configureEach {
          with(it.options) {
            if (!release.isPresent && extension.javaVersions.minimumToolchain.get() >= 9) {
              release.set(extension.javaVersions.target)
            }
          }
        }

        if(extension.reproducibleBuilds.get()) {
          tasks.withType<AbstractArchiveTask>().configureEach {
            it.isPreserveFileTimestamps = false
            it.isReproducibleFileOrder = true
          }
        }

        tasks.withType<Javadoc>().configureEach { jd ->
          with(jd.options) {
            if(this is StandardJavadocDocletOptions) {
              val doclintMissing = addBooleanOption("Xdoclint:-missing")
              val html5 = addBooleanOption("html5")
              val release = addStringOption("-release")
              val enablePreview = addBooleanOption("-enable-preview")
              val noModuleDirectories = addBooleanOption("-no-module-directories")

              jd.doFirst {
                val versions = extension.javaVersions
                val target = versions.target.get()
                links(jdkApiDocs(target))

                if(versions.minimumToolchain.get() >= 9) {
                  if(versions.actualVersion.get() < 12) {
                    // Apply workaround for https://bugs.openjdk.java.net/browse/JDK-8215291
                    // Hopefully this gets backported some day... (JDK-8215291)
                    noModuleDirectories.value = true
                  }

                  release.value = target.toString()
                  doclintMissing.value = true
                  html5.value = true
                  enablePreview.value = versions.enablePreviewFeatures.get()
                } else {
                  source = versionString(target)
                }
              }
            }
          }
        }

        // Set up testing on the selected Java versions
        val toolchains = extensions.getByType(JavaToolchainService::class)
        val testWithProp = extension.javaVersions.testWith
        testWithProp.finalizeValue()
        testWithProp.get().forEach { targetRuntime ->
          // Create task that will use that version
          val versionedTest = tasks.register("testJava$targetRuntime", Test::class.java) {
            it.description = "Runs tests on Java $targetRuntime if necessary based on build settings"
            it.group = LifecycleBasePlugin.VERIFICATION_GROUP
            // Appropriate classpath and test class source information is set on all test tasks by JavaPlugin

            it.onlyIf {
              // Only run if our runtime is not the standard runtime, and we're doing strict versions.
              extension.javaVersions.strictVersions.get() && targetRuntime != extension.javaVersions.actualVersion.get()
            }
            it.javaLauncher.set(toolchains.launcherFor { it.languageVersion.set(JavaLanguageVersion.of(targetRuntime)) })
          }

          tasks.named(LifecycleBasePlugin.CHECK_TASK_NAME) { it.dependsOn(versionedTest) }
        }
      }
    }
  }

  /**
   * Link to the API documentation for a specific java version.
   */
  private fun jdkApiDocs(javaVersion: Int): String = if(javaVersion >= 11) {
    "https://docs.oracle.com/en/java/javase/$javaVersion/docs/api"
  } else {
    "https://docs.oracle.com/javase/$javaVersion/docs/api"
  }
}
