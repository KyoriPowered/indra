import net.kyori.indra.self.declarePlugin

dependencies {
  api("org.ajoberstar.grgit:grgit-gradle:4.1.0")
  implementation("gradle.plugin.org.cadixdev.gradle:licenser:0.5.0")
}

declarePlugin(
  id = "indra",
  mainClass = "IndraPlugin",
  displayName = "Indra",
  description = "Simplified tools for configuring modern JVM projects",
  tags = listOf("boilerplate", "java", "jvm")
)

declarePlugin(
  id = "indra.checkstyle",
  mainClass = "IndraCheckstylePlugin",
  displayName = "Indra Checkstyle",
  description = "Checkstyle configuration in line with the Indra file layout",
  tags = listOf("boilerplate", "checkstyle")
)

declarePlugin(
  id = "indra.license-header",
  mainClass = "IndraLicenseHeaderPlugin",
  displayName = "Indra License Header",
  description = "License header configuration in line with the Indra file layout",
  tags = listOf("boilerplate", "license", "license-header", "licensing")
)

declarePlugin(
  id = "indra.publishing",
  mainClass = "IndraPublishingPlugin",
  displayName = "Indra Publishing",
  description = "Reasonable publishing configuration and repository aliases",
  tags = listOf("boilerplate", "publishing", "nexus")
)
