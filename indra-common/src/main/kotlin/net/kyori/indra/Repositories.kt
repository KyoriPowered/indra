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
import net.kyori.indra.data.RemoteRepository

fun RepositoryHandler.sonatypeSnapshots() = addRepository(this, RemoteRepository.SONATYPE_SNAPSHOTS)

private fun addRepository(handler: RepositoryHandler, repository: RemoteRepository): MavenArtifactRepository {
  return handler.maven {
    it.name = repository.name
    it.url = repository.url
    when {
      repository.releases && repository.snapshots -> {}
      repository.releases -> it.mavenContent(MavenRepositoryContentDescriptor::releasesOnly)
      repository.snapshots -> it.mavenContent(MavenRepositoryContentDescriptor::snapshotsOnly)
      else -> {} // no-op
    }
  }
}

fun registerRepositoryExtensions(handler: RepositoryHandler, repositories: Iterable<RemoteRepository>) {
  repositories.forEach {
    (handler as ExtensionAware).extensions.add(it.name, object : Closure<Unit>(null, handler) {
      fun doCall() {
        addRepository(this.thisObject as RepositoryHandler, it)
      }
    })
  }
}
