dependencies {
  implementation(project(":indra-common"))
  implementation(gradleKotlinDsl()) // needed for gradle-nexus-publish-plugin
  implementation("io.github.gradle-nexus:publish-plugin:1.1.0")
}

indraPluginPublishing {
  plugin(
    "indra.publishing.sonatype",
    "net.kyori.indra.sonatype.IndraSonatypePublishingPlugin",
    "Indra Sonatype Publishing",
    "Reasonable sonatype publishing configuration",
    listOf("boilerplate", "publishing", "nexus", "sonatype")
  )
}
