dependencies {
  implementation(project(":indra-common"))
  implementation("com.gradle.publish:plugin-publish-plugin:0.12.0")
}

indraPluginPublishing {
  plugin(
    "indra.publishing.gradle-plugin",
    "net.kyori.indra.gradle.GradlePluginPublishingPlugin",
    "Indra Gradle Plugin Publishing",
    "Reasonable settings for Gradle plugin publishing",
    listOf("publishing", "gradle-plugin", "boilerplate")
  )
}
