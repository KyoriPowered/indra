package net.kyori.indra.self

import com.gradle.publish.PluginBundleExtension
import org.gradle.api.Project
import org.gradle.plugin.devel.GradlePluginDevelopmentExtension

const val BASE = "net.kyori.indra"

fun Project.declarePlugin(id: String, mainClass: String, displayName: String, description: String? = null, tags: List<String> = listOf()) {
  val qualifiedId = "$group.$id"
  extensions.getByType(GradlePluginDevelopmentExtension::class.java).plugins.create(id) {
    it.id = qualifiedId
    it.implementationClass = "$BASE.$mainClass"
  }

  extensions.configure(PluginBundleExtension::class.java) {
    it.plugins.maybeCreate(id).apply {
      this.id = qualifiedId
      this.displayName = displayName
      if(tags.isNotEmpty()) {
        this.tags = tags
      }
      if(description != null) {
        this.description = description
      }
    }

    if (it.tags.isEmpty()) {
      it.tags = tags
    }

    it.vcsUrl = "https://github.com/KyoriPowered/indra"
  }
}
