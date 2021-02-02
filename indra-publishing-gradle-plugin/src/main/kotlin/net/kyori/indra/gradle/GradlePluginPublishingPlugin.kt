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
package net.kyori.indra.gradle

import com.gradle.publish.PluginBundleExtension
import com.gradle.publish.PublishPlugin
import net.kyori.indra.AbstractIndraPublishingPlugin
import net.kyori.indra.extension
import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.withType
import org.gradle.plugin.devel.GradlePluginDevelopmentExtension
import org.gradle.plugin.devel.plugins.JavaGradlePluginPlugin

/**
 * An indra publishing plugin for Gradle plugin publications
 */
class GradlePluginPublishingPlugin : AbstractIndraPublishingPlugin() {

  override fun extraApplySteps(target: Project) {
    // TODO: do we want to apply these plugins ourselves instead of only acting when the user chooses to do so?
    target.plugins.withType(PublishPlugin::class.java) {
      val pluginBundleExtension = target.extensions.getByType(PluginBundleExtension::class)
      target.plugins.withType(JavaGradlePluginPlugin::class.java) {
        val extension = target.extensions.create(
          "indraPluginPublishing",
          IndraPluginPublishingExtension::class,
          target.extensions.getByType(GradlePluginDevelopmentExtension::class),
          pluginBundleExtension
        )

        extension.pluginIdBase.convention(target.provider { target.group as String })
      }

      target.afterEvaluate {
        val indraExtension = extension(it)
        if(indraExtension.scm.isPresent && pluginBundleExtension.vcsUrl == null) {
          pluginBundleExtension.vcsUrl = indraExtension.scm.get().url
        }

        if(it.description != null && pluginBundleExtension.description == null) {
          pluginBundleExtension.description = it.description
        }
      }
    }
  }


  override fun applyPublishingActions(publishing: PublishingExtension, actions: Set<Action<MavenPublication>>) {
    publishing.publications.withType(MavenPublication::class).configureEach { publication ->
      actions.forEach { it.execute(publication) }
    }
  }

  override fun configurePublications(publishing: PublishingExtension, configuration: Action<MavenPublication>) {
    publishing.publications.withType(MavenPublication::class).configureEach(configuration)
  }
}
