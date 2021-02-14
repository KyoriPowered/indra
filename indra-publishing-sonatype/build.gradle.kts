dependencies {
  implementation(project(":indra-common"))
  implementation("de.marcphilipp.gradle:nexus-publish-plugin:0.4.0")
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
