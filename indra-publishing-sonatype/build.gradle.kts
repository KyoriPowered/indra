import net.kyori.indra.self.declarePlugin

dependencies {
  implementation(project(":indra-common"))
  implementation("de.marcphilipp.gradle:nexus-publish-plugin:0.4.0")
}

declarePlugin(
  id = "indra.publishing.sonatype",
  mainClass = "sonatype.IndraSonatypePublishingPlugin",
  displayName = "Indra Sonatype Publishing",
  description = "Reasonable sonatype publishing configuration"
)
