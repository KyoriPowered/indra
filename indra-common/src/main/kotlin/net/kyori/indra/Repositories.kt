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
@file:JvmName("Repositories")
package net.kyori.indra

import groovy.lang.Closure
import org.gradle.api.artifacts.dsl.RepositoryHandler
import org.gradle.api.artifacts.repositories.MavenArtifactRepository
import org.gradle.api.artifacts.repositories.MavenRepositoryContentDescriptor
import org.gradle.api.plugins.ExtensionAware
import java.net.URI

fun RepositoryHandler.sonatypeSnapshots() = sonatypeSnapshots.addTo(this)
private val sonatypeSnapshots = RemoteRepository("sonatypeSnapshots", "https://oss.sonatype.org/content/groups/public/", releases = false)

internal val DEFAULT_REPOSITORIES = listOf(
  sonatypeSnapshots
)

/**
 * A specification for a maven repository
 */
data class RemoteRepository @JvmOverloads constructor(val name: String, val url: URI, val releases: Boolean = true, val snapshots: Boolean = true) {
  // helper constructor
  @JvmOverloads
  constructor(name: String, url: String, releases: Boolean = true, snapshots: Boolean = true) : this(name, URI(url), releases, snapshots)

  fun addTo(handler: RepositoryHandler): MavenArtifactRepository {
    return handler.maven {
      it.name = name
      it.url = url
      when {
        this.releases && this.snapshots -> {}
        this.releases -> it.mavenContent(MavenRepositoryContentDescriptor::releasesOnly)
        this.snapshots -> it.mavenContent(MavenRepositoryContentDescriptor::snapshotsOnly)
        else -> {} // no-op
      }
    }
  }
}

fun registerRepositoryExtensions(handler: RepositoryHandler, repos: Iterable<RemoteRepository>) {
  val extensions = handler as ExtensionAware
  repos.forEach {
    extensions.extensions.add(it.name, object : Closure<Unit>(null, handler) {
      fun doCall() {
        it.addTo(this.thisObject as RepositoryHandler)
      }
    })
  }
}
