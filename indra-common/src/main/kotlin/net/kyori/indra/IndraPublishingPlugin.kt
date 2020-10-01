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

import net.kyori.indra.data.Issues
import net.kyori.indra.data.License
import net.kyori.indra.data.SCM
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.publish.plugins.PublishingPlugin
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.configure

class IndraPublishingPlugin : Plugin<Project> {
  override fun apply(project: Project) {
    with(project) {
      val extension = extension(project)

      apply<PublishingPlugin>()

      extensions.configure<PublishingExtension> {
        publications.register(PUBLICATION_NAME, MavenPublication::class.java) {
          it.apply {
            pom.apply {
              name.set(project.name)
              description.set(project.description)
              url.set(extension.scm.map(SCM::url))

              issueManagement { issues ->
                issues.url.set(extension.issues.map(Issues::url))
                issues.system.set(extension.issues.map(Issues::system))
              }

              licenses { licenses ->
                licenses.license { license ->
                  license.name.set(extension.license.map(License::name))
                  license.url.set(extension.license.map(License::url))
                }
              }

              scm { scm ->
                scm.connection.set(extension.scm.map(SCM::connection))
                scm.developerConnection.set(extension.scm.map(SCM::developerConnection))
                scm.url.set(extension.scm.map(SCM::url))
              }
            }
          }
        }
      }
    }
  }
}
