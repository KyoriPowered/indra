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
package net.kyori.indra.bintray

import com.jfrog.bintray.gradle.BintrayExtension
import com.jfrog.bintray.gradle.BintrayPlugin
import net.kyori.indra.IndraPublishingPlugin
import net.kyori.indra.extension
import net.kyori.indra.headTag
import net.kyori.indra.isRelease
import org.ajoberstar.grgit.Tag
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.publish.PublishingExtension
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.getByType
import java.time.format.DateTimeFormatter

private val DATE_FORMAT_BINTRAY = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ")

class IndraBintrayPublishingPlugin : Plugin<Project> {
  override fun apply(project: Project) {
    with(project) {
      val extension = extension(project)
      apply<IndraPublishingPlugin>()
      apply<BintrayPlugin>()

      val bintrayExtension = extensions.getByType<BintrayExtension>().apply {
        user = findProperty("bintrayUser") as String?
        key = findProperty("bintrayKey") as String?
        publish = true

        pkg.apply {
          repo = findProperty("bintrayRepo") as String?
          name = project.name
          vcsUrl = extension.scm.orNull?.connection
          version.apply {
            val tag: Tag? = headTag(project)
            vcsTag = tag?.name
            desc = tag?.shortMessage
            released = tag?.commit?.dateTime?.format(DATE_FORMAT_BINTRAY)
          }
        }
      }

      tasks.named("bintrayUpload").configure {
        it.dependsOn(tasks.named("requireClean"))
        it.onlyIf {
          isRelease(project)
        }
      }

      afterEvaluate {
        bintrayExtension.apply {
          setPublications(*extensions.getByType(PublishingExtension::class).publications.names.toTypedArray())

          pkg.apply {
            vcsUrl = extension.scm.orNull?.connection
            issueTrackerUrl = extension.issues.orNull?.url
            version.name = project.version as String
            extension.license.orNull?.apply {
              setLicenses(bintray)
            }
          }
        }
      }
    }
  }
}
