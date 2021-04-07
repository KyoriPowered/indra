dependencies {
  implementation(project(":indra-common"))
  implementation(gradleKotlinDsl()) // needed for gradle-nexus-publish-plugin
  implementation("io.github.gradle-nexus:publish-plugin:1.0.0")
}

indraPluginPublishing {
  plugin(
    id = "indra.publishing.sonatype",
    mainClass = "net.kyori.indra.sonatype.IndraSonatypePublishingPlugin",
    displayName = "Indra Sonatype Publishing",
    description = "Reasonable sonatype publishing configuration",
    tags = listOf("boilerplate", "publishing", "nexus", "sonatype")
  )
}
