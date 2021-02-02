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

import java.net.URI
import javax.inject.Inject
import net.kyori.indra.api.model.ContinuousIntegration
import net.kyori.indra.api.model.Issues
import net.kyori.indra.api.model.License
import net.kyori.indra.api.model.SourceCodeManagement
import net.kyori.indra.data.ApplyTo
import net.kyori.indra.data.ContinuousIntegrationImpl
import net.kyori.indra.data.IssuesImpl
import net.kyori.indra.data.LicenseImpl
import net.kyori.indra.data.SourceCodeManagementImpl
import org.gradle.api.Action
import org.gradle.api.GradleException
import org.gradle.api.component.SoftwareComponent
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.kotlin.dsl.domainObjectSet
import org.gradle.kotlin.dsl.invoke
import org.gradle.kotlin.dsl.property
import org.gradle.process.CommandLineArgumentProvider

open class IndraExtension @Inject constructor(objects: ObjectFactory) {
  val reproducibleBuilds: Property<Boolean> = objects.property(Boolean::class).convention(true)

  /**
   * Whether the `java` [SoftwareComponent] should be automatically included in publications.
   *
   * This property does not usually need to be changed, unless working with Gradle plugins that publish in a non-standard way.
   */
  val includeJavaSoftwareComponentInPublications: Property<Boolean> = objects.property(Boolean::class).convention(true)

  /**
   * Options controlling Java toolchain versions
   */
  val javaVersions: JavaToolchainVersions = objects.newInstance(JavaToolchainVersions::class.java)

  /**
   * Configure the versioning configuration.
   */
  fun javaVersions(action: Action<JavaToolchainVersions>) {
    action(javaVersions)
  }

  internal fun previewFeatureArgumentProvider(): CommandLineArgumentProvider = CommandLineArgumentProvider {
    javaVersions.enablePreviewFeatures.finalizeValue()
    if(javaVersions.enablePreviewFeatures.get()) {
      listOf("--enable-preview")
    } else {
      listOf()
    }
  }

  val ci: Property<ContinuousIntegration> = objects.property(ContinuousIntegration::class)
  val issues: Property<Issues> = objects.property(Issues::class)
  val license: Property<License> = objects.property(License::class)
  val scm: Property<SourceCodeManagement> = objects.property(SourceCodeManagement::class)

  @JvmOverloads
  fun github(user: String, repo: String, applicable: Action<ApplyTo>? = null) {
    val options = ApplyTo().also { applicable?.execute(it) }
    if(options.ci) {
      this.ci.set(ContinuousIntegrationImpl("GitHub Actions", "https://github.com/$user/$repo/actions"))
    }
    if(options.issues) {
      this.issues.set(IssuesImpl("GitHub", "https://github.com/$user/$repo/issues"))
    }
    if(options.scm) {
      this.scm.set(SourceCodeManagementImpl("scm:git:https://github.com/$user/$repo.git", "scm:git:ssh://git@github.com/$user/$repo.git", "https://github.com/$user/$repo"))
    }
    if(options.publishing) {
      this.publishReleasesTo("githubPackages", "https://maven.pkg.github.com/$user/$repo")
    }
  }

  @JvmOverloads
  fun gitlab(user: String, repo: String, applicable: Action<ApplyTo>? = null) {
    val options = ApplyTo().also { applicable?.execute(it) }
    if(options.ci) {
      this.ci.set(ContinuousIntegrationImpl("GitLab CI", "https://gitlab.com/$user/$repo/-/pipelines"))
    }
    if(options.issues) {
      this.issues.set(IssuesImpl("GitLab", "https://gitlab.com/$user/$repo/-/issues"))
    }
    if(options.scm) {
      this.scm.set(SourceCodeManagementImpl("scm:git:https://gitlab.com/$user/$repo.git", "scm:git:ssh://git@gitlab.com/$user/$repo.git", "https://gitlab.com/$user/$repo"))
    }
    if(options.publishing) {
      // TODO: needs project ID, which is separate from user/repo and uses HTTP header-based auth
      throw GradleException("Publishing cannot yet be automatically configured for GitLab projects")
    }
  }

  fun apache2License() = this.license.set(LicenseImpl(
    spdx = "Apache-2.0",
    name = "Apache License, Version 2.0",
    url = "https://opensource.org/licenses/Apache-2.0"
  ))

  fun gpl3OnlyLicense() = this.license.set(LicenseImpl(
    spdx = "GPL-3.0-only",
    name = "GNU General Public License version 3",
    url = "https://opensource.org/licenses/GPL-3.0",
    bintray = "GPL-3.0"
  ))

  fun mitLicense() = this.license.set(LicenseImpl(
    spdx = "MIT",
    name = "The MIT License",
    url = "https://opensource.org/licenses/MIT"
  ))

  fun jenkins(url: String) = this.ci.set(ContinuousIntegrationImpl(
    system = "Jenkins",
    url = url
  ))

  // Checkstyle

  val checkstyle: Property<String> = objects.property(String::class).convention("8.37")

  // Publishing

  internal val repositories = objects.domainObjectSet(RepositorySpec::class)

  @Transient
  internal val publishingActions = mutableSetOf<Action<MavenPublication>>()

  fun publishAllTo(id: String, url: String) = this.repositories.add(RepositorySpec(id, URI(url), releases = true, snapshots = true))
  fun publishReleasesTo(id: String, url: String) = this.repositories.add(RepositorySpec(id, URI(url), releases = true, snapshots = false))
  fun publishSnapshotsTo(id: String, url: String) = this.repositories.add(RepositorySpec(id, URI(url), releases = false, snapshots = true))

  fun configurePublications(action: Action<MavenPublication>) {
    this.publishingActions.add(action)
  }
}

internal data class RepositorySpec(
  val id: String,
  val url: URI,
  val releases: Boolean,
  val snapshots: Boolean
)
