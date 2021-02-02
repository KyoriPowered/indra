import net.kyori.indra.self.declarePlugin

dependencies {
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
