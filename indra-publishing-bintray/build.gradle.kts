import net.kyori.indra.self.declarePlugin

dependencies {
  implementation(project(":indra-common"))
  implementation("com.jfrog.bintray.gradle:gradle-bintray-plugin:1.8.5")
}

declarePlugin(
  id = "indra.publishing.bintray",
  mainClass = "bintray.IndraBintrayPublishingPlugin",
  displayName = "Indra Bintray Publishing",
  description = "Reasonable settings for Bintray publishing",
  tags = listOf("publishing", "bintray", "boilerplate")
)
