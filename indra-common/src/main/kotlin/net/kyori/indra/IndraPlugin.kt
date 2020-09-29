package net.kyori.indra

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaLibraryPlugin

class IndraPlugin : Plugin<Project> {
  override fun apply(project: Project) {
    with(project) {
      plugins.apply {
        apply(JavaLibraryPlugin::class.java)
      }
    }
  }
}
