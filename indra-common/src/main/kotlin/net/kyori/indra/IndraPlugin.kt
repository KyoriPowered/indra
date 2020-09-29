package net.kyori.indra

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaLibraryPlugin
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.api.tasks.javadoc.Javadoc
import org.gradle.api.tasks.testing.Test
import org.gradle.external.javadoc.StandardJavadocDocletOptions
import org.gradle.kotlin.dsl.withType
import java.nio.charset.StandardCharsets

class IndraPlugin : Plugin<Project> {
  override fun apply(project: Project) {
    with(project) {
      plugins.apply {
        apply(JavaLibraryPlugin::class.java)
      }

      afterEvaluate {
        tasks.withType<JavaCompile>().configureEach {
          it.options.apply {
            encoding = StandardCharsets.UTF_8.name()
            compilerArgs.addAll(
              listOf(
                "-parameters",
                "-Xlint:all"
              )
            )
          }
        }

        tasks.withType<Javadoc>().configureEach {
          with(it.options) {
            encoding = StandardCharsets.UTF_8.name()

            if(this is StandardJavadocDocletOptions) {
              charSet = StandardCharsets.UTF_8.name()
            }
          }
        }

        tasks.withType<Test>().configureEach {
          it.useJUnitPlatform()
        }
      }
    }
  }
}
