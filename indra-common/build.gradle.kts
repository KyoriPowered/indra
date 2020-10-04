import net.kyori.indra.self.declarePlugin

dependencies {
  implementation("org.ajoberstar.grgit:grgit-gradle:4.1.0")
  implementation("gradle.plugin.org.cadixdev.gradle:licenser:0.5.0")
}

declarePlugin(
  id = "indra",
  mainClass = "IndraPlugin",
  displayName = "Indra",
  description = "Sensible configuration for JVM projects"
)

declarePlugin(
  id = "indra.checkstyle",
  mainClass = "IndraCheckstylePlugin",
  displayName = "Indra Checkstyle",
  description = "Reasonable checkstyle configuration"
)

declarePlugin(
  id = "indra.license-header",
  mainClass = "IndraLicenseHeaderPlugin",
  displayName = "Indra License Header",
  description = "Reasonable license-header configuration"
)

declarePlugin(
  id = "indra.publishing",
  mainClass = "IndraPublishingPlugin",
  displayName = "Indra Publishing",
  description = "Reasonable publishing configuration"
)
