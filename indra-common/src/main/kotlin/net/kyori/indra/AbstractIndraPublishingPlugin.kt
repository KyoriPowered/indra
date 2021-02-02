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

import net.kyori.indra.task.RequireClean
import org.ajoberstar.grgit.gradle.GrgitPlugin
import org.gradle.api.Action
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.publish.maven.plugins.MavenPublishPlugin
import org.gradle.api.publish.maven.tasks.AbstractPublishToMaven
import org.gradle.api.publish.maven.tasks.PublishToMavenLocal
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.credentials
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.register
import org.gradle.kotlin.dsl.withType
import org.gradle.plugins.signing.Sign
import org.gradle.plugins.signing.SigningExtension
import org.gradle.plugins.signing.SigningPlugin

/**
 * Common behaviour between 'standard' and Gradle Plugin publishing.
 */
abstract class AbstractIndraPublishingPlugin : Plugin<Project> {
  final override fun apply(target: Project) {
    with(target) {
      val extension = extension(project)

      plugins.apply(MavenPublishPlugin::class)
      plugins.apply(SigningPlugin::class)
      plugins.apply(GrgitPlugin::class)

      // Inherit options from root project
      if(this != rootProject) {
        group = rootProject.group
        version = rootProject.version
        description = rootProject.description
      }

      val descriptionProvider = project.provider { project.description }
      configurePublications(extensions.getByType(PublishingExtension::class), Action {
        it.pom.apply {
          name.set(project.name)
          description.set(descriptionProvider)
          url.set(extension.scm.map(net.kyori.indra.data.SCM::url))

          ciManagement { ci ->
            ci.system.set(extension.ci.map(net.kyori.indra.data.ContinuousIntegration::system))
            ci.url.set(extension.ci.map(net.kyori.indra.data.ContinuousIntegration::url))
          }

          issueManagement { issues ->
            issues.system.set(extension.issues.map(net.kyori.indra.data.Issues::system))
            issues.url.set(extension.issues.map(net.kyori.indra.data.Issues::url))
          }

          licenses { licenses ->
            licenses.license { license ->
              license.name.set(extension.license.map(net.kyori.indra.data.License::name))
              license.url.set(extension.license.map(net.kyori.indra.data.License::url))
            }
          }

          scm { scm ->
            scm.connection.set(extension.scm.map(net.kyori.indra.data.SCM::connection))
            scm.developerConnection.set(extension.scm.map(net.kyori.indra.data.SCM::developerConnection))
            scm.url.set(extension.scm.map(net.kyori.indra.data.SCM::url))
          }
        }
      })

      extensions.configure<SigningExtension> {
        sign(extensions.getByType<PublishingExtension>().publications)
        useGpgCmd()
      }

      tasks.withType(Sign::class).configureEach {
        it.onlyIf {
          project.hasProperty("forceSign") || isRelease(project)
        }
      }

      val requireClean = tasks.register(RequireClean.NAME, RequireClean::class)
      tasks.withType(AbstractPublishToMaven::class).configureEach {
        if(it !is PublishToMavenLocal) {
          it.dependsOn(requireClean)
        }
      }

      afterEvaluate {
        extensions.getByType(PublishingExtension::class).apply {
          applyPublishingActions(this, extension.publishingActions)

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
                repository.credentials(org.gradle.api.artifacts.repositories.PasswordCredentials::class)
              }
            }
          }
        }
      }
      extraApplySteps(target)
    }
  }

  /**
   * Add any extra steps sub-plugins might want to perform on application
   */
  open fun extraApplySteps(target: Project) { }

  /**
   * Apply publishing actions to all publications targeted.
   */
  abstract fun applyPublishingActions(publishing: PublishingExtension, actions: Set<Action<MavenPublication>>)

  /**
   * Configure and/or create publications, applying the provided common configuration action.
   */
  abstract fun configurePublications(publishing: PublishingExtension, configuration: Action<MavenPublication>)
}

fun isSnapshot(project: Project) = project.version.toString().contains("-SNAPSHOT")

/**
 * Verify that this project is checked out to a release version, meaning that:
 *
 * - The version does not contain SNAPSHOT
 * - The project is managed within a Git repository
 * - the current head commit is tagged
 */
fun isRelease(project: Project): Boolean {
  val tag = net.kyori.indra.util.headTag(project)
  return (tag != null || net.kyori.indra.util.grgit(project) == null) && !isSnapshot(project)
}
