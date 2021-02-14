import net.kyori.indra.self.declarePlugin
plugins {
  kotlin("jvm")
}

dependencies {
  // DSL stubs are generated per-project but only for what's applied in the `plugins` block.
  // Since this root project doesn't have anything applied, we don't get DSL extensions for the
  // java configurations so we have to refer to them with strings.
  "implementation"(gradleKotlinDsl())

  implementation(project(":indra-common"))
  implementation("com.gradle.publish:plugin-publish-plugin:0.12.0")
}

declarePlugin(
  id = "indra.publishing.gradle-plugin",
  mainClass = "gradle.GradlePluginPublishingPlugin",
  displayName = "Indra Gradle Plugin Publishing",
  description = "Reasonable settings for Gradle plugin publishing",
  tags = listOf("publishing", "gradle-plugin", "boilerplate")
)
