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
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.kotlin.dsl.property
import org.gradle.plugin.devel.GradlePluginDevelopmentExtension
import javax.inject.Inject

open class IndraPluginPublishingExtension @Inject constructor(
  objects: ObjectFactory,
  private val publishingExtension: GradlePluginDevelopmentExtension,
  private val pluginBundleExtension: PluginBundleExtension
) {

  val pluginIdBase: Property<String> = objects.property(String::class)


  /**
   * Register a plugin to have marker validated, and to be deployed to the Gradle Plugin Portal.
   *
   * The id is relative to [pluginIdBase], which is by default the project's group id. Main class is absolute.
   *
   * If no tags are set on the global plugin bundle, then the first provided set of tags will be applied.
   */
  @JvmOverloads
  fun plugin(id: String, mainClass: String, displayName: String, description: String? = null, tags: List<String> = listOf()) {
    val qualifiedId = "${pluginIdBase.get()}.$id"
    publishingExtension.plugins.create(id) {
      it.id = qualifiedId
      it.implementationClass = mainClass
      if(description != null) {
        it.description
      }
      it.displayName = displayName
    }

    pluginBundleExtension.apply {
      plugins.maybeCreate(id).apply {
        this.id = qualifiedId
        this.displayName = displayName
        if(tags.isNotEmpty()) {
          this.tags = tags
        }
        if(description != null) {
          this.description = description
        }
      }

      if(this.tags.isEmpty()) {
        this.tags = tags
      }
    }
  }
}
