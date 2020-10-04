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
import net.kyori.indra.data.ApplyTo
import net.kyori.indra.data.Issues
import net.kyori.indra.data.License
import net.kyori.indra.data.SCM
import org.gradle.api.Action
import org.gradle.api.JavaVersion
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.kotlin.dsl.property
import org.gradle.kotlin.dsl.domainObjectSet

open class IndraExtension(objects: ObjectFactory) {
  val java: Property<JavaVersion> = objects.property(JavaVersion::class).convention(JavaVersion.VERSION_1_8)
  val reproducibleBuilds: Property<Boolean> = objects.property(Boolean::class).convention(true)

  val issues: Property<Issues> = objects.property(Issues::class)
  val license: Property<License> = objects.property(License::class)
  val scm: Property<SCM> = objects.property(SCM::class)

  fun github(user: String, repo: String) = this.github(user, repo, null) // for groovy buildscript support
  fun github(user: String, repo: String, applicable: Action<ApplyTo>? = null) {
    val options = ApplyTo().also { applicable?.execute(it) }
    if(options.issues) {
      this.issues.set(Issues("GitHub", "https://github.com/$user/$repo/issues"))
    }
    if(options.scm) {
      this.scm.set(SCM("scm:git:https://github.com/$user/$repo.git", "scm:git:ssh://git@github.com/$user/$repo.git", "https://github.com/$user/$repo"))
    }
  }

  fun gitlab(user: String, repo: String) = this.gitlab(user, repo, null) // for groovy buildscript support
  fun gitlab(user: String, repo: String, applicable: Action<ApplyTo>? = null) {
    val options = ApplyTo().also { applicable?.execute(it) }
    if(options.issues) {
      this.issues.set(Issues("GitLab", "https://gitlab.com/$user/$repo/-/issues"))
    }
    if(options.scm) {
      this.scm.set(SCM("scm:git:https://gitlab.com/$user/$repo.git", "scm:git:ssh://git@gitlab.com/$user/$repo.git", "https://gitlab.com/$user/$repo"))
    }
  }

  fun apache2License() = this.license.set(License(
    spdx = "Apache-2.0",
    name = "Apache License, Version 2.0",
    url = "https://opensource.org/licenses/Apache-2.0"
  ))

  fun mitLicense() = this.license.set(License(
    spdx = "MIT",
    name = "The MIT License",
    url = "https://opensource.org/licenses/MIT"
  ))

  // Checkstyle

  val checkstyle: Property<String> = objects.property(String::class).convention("8.36.2")

  // Publishing

  internal val repositories = objects.domainObjectSet(RepositorySpec::class)

  fun publishAllTo(id: String, url: String) = this.repositories.add(RepositorySpec(id, URI(url), releases = true, snapshots = true))
  fun publishReleasesTo(id: String, url: String) = this.repositories.add(RepositorySpec(id, URI(url), releases = true, snapshots = false))
  fun publishSnapshotsTo(id: String, url: String) = this.repositories.add(RepositorySpec(id, URI(url), releases = false, snapshots = true))
}

internal data class RepositorySpec(
  val id: String,
  val url: URI,
  val releases: Boolean,
  val snapshots: Boolean
)
