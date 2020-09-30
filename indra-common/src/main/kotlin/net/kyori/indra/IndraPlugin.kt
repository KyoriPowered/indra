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

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaLibraryPlugin
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.tasks.bundling.AbstractArchiveTask
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.api.tasks.javadoc.Javadoc
import org.gradle.api.tasks.testing.Test
import org.gradle.external.javadoc.StandardJavadocDocletOptions
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.findByType
import org.gradle.kotlin.dsl.withType

class IndraPlugin : Plugin<Project> {
  override fun apply(project: Project) {
    with(project) {
      val extension = extensions.findByType(IndraExtension::class) ?: extensions.create(EXTENSION_NAME, IndraExtension::class)

      apply<JavaLibraryPlugin>()

      extensions.configure<JavaPluginExtension> {
        sourceCompatibility = extension.java.get()
        targetCompatibility = extension.java.get()
      }

      tasks.withType<JavaCompile>().configureEach {
        it.options.apply {
          encoding = Charsets.UTF_8.name()
          release.set(versionNumber(extension.java.get()))
          compilerArgs.addAll(
            listOf(
              // Generate metadata for reflection on method parameters
              "-parameters",
              // Enable all warnings
              "-Xlint:all"
            )
          )
          if(version(it.toolChain).isJava9Compatible) {
            compilerArgs.addAll(
              listOf(
                "-Xdoclint",
                // Only warn for missing javadocs for things with public access
                "-Xdoclint:-missing/package",
                "-Xdoclint:-missing/protected",
                "-Xdoclint:-missing/private"
              )
            )
          }
        }
      }

      tasks.withType<Javadoc>().configureEach {
        with(it.options) {
          encoding = Charsets.UTF_8.name()

          if(this is StandardJavadocDocletOptions) {
            charSet = Charsets.UTF_8.name()
            source = versionString(extension.java.get())

            if(version(it.toolChain).isJava9Compatible) {
              addBooleanOption("html5", true)
            }
          }
        }
      }

      tasks.withType<Test>().configureEach {
        it.useJUnitPlatform()
      }

      if(extension.reproducibleBuilds.get()) {
        tasks.withType<AbstractArchiveTask>().configureEach {
          it.isPreserveFileTimestamps = false
          it.isReproducibleFileOrder = true
        }
      }
    }
  }
}
