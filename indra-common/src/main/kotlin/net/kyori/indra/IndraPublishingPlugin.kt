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
@file:JvmName("IndraPublishing")
package net.kyori.indra

import net.kyori.indra.data.ContinuousIntegration
import net.kyori.indra.data.Issues
import net.kyori.indra.data.License
import net.kyori.indra.data.SCM
import net.kyori.indra.task.RequireClean
import org.ajoberstar.grgit.gradle.GrgitPlugin
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.repositories.PasswordCredentials
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.publish.maven.plugins.MavenPublishPlugin
import org.gradle.api.publish.maven.tasks.AbstractPublishToMaven
import org.gradle.api.publish.maven.tasks.PublishToMavenLocal
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.credentials
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.invoke
import org.gradle.kotlin.dsl.named
import org.gradle.kotlin.dsl.register
import org.gradle.kotlin.dsl.withType
import org.gradle.plugins.signing.SigningExtension
import org.gradle.plugins.signing.SigningPlugin

class IndraPublishingPlugin : Plugin<Project> {
  override fun apply(project: Project) {
    with(project) {
      val extension = extension(project)

      apply<MavenPublishPlugin>()
      apply<SigningPlugin>()
      apply<GrgitPlugin>()

      // Inherit options from root project
      if(this != rootProject) {
        group = rootProject.group
        version = rootProject.version
        description = rootProject.description
      }

      val descriptionProvider = project.provider { project.description }
      extensions.configure<PublishingExtension> {
        publications.register(PUBLICATION_NAME, MavenPublication::class.java) {
          it.apply {
            pom.apply {
              name.set(project.name)
              description.set(descriptionProvider)
              url.set(extension.scm.map(SCM::url))

              ciManagement { ci ->
                ci.system.set(extension.ci.map(ContinuousIntegration::system))
                ci.url.set(extension.ci.map(ContinuousIntegration::url))
              }

              issueManagement { issues ->
                issues.system.set(extension.issues.map(Issues::system))
                issues.url.set(extension.issues.map(Issues::url))
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

      extensions.configure<SigningExtension> {
        sign(extensions.getByType<PublishingExtension>().publications)
        useGpgCmd()
      }

      val requireClean = tasks.register("requireClean", RequireClean::class)
      tasks.withType<AbstractPublishToMaven>().configureEach {
        if(it !is PublishToMavenLocal) {
          it.dependsOn(requireClean)
        }
      }

      afterEvaluate {
        extensions.getByType<PublishingExtension>().apply {
          publications.named<MavenPublication>(PUBLICATION_NAME).configure { pub ->
            extension.publishingActions.forEach { it(pub) }
          }

          extension.repositories.all { // will be applied to repositories as they're added
            val username = "${it.id}Username"
            val password = "${it.id}Password"
            if(((it.releases && isRelease(project))
                  || (it.snapshots && isSnapshot(project)))
                && project.hasProperty(username)
                && project.hasProperty(password)) {
                repositories.maven { repository ->
                  repository.name = it.id
                  repository.url = it.url
                  // ${id}Username + ${id}Password properties
                  repository.credentials(PasswordCredentials::class)
                }
              }
          }
        }
      }
    }
  }
}

fun isSnapshot(project: Project) = project.version.toString().endsWith("-SNAPSHOT")

/**
 * Verify that this project is checked out to a release version, meaning that:
 *
 * - The version does not contain SNAPSHOT
 * - The project is managed within a Git repository
 * - the current head commit is tagged
 */
fun isRelease(project: Project): Boolean {
  val tag = headTag(project)
  return (tag != null || grgit(project) == null) && !isSnapshot(project)
}
