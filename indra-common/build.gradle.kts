import net.kyori.indra.self.declarePlugin

declarePlugin(
  id = "indra",
  mainClass = "IndraPlugin",
  displayName = "Indra",
  description = "Sensible configuration for JVM projects"
)

declarePlugin(
  id = "indra.publishing",
  mainClass = "IndraPublishingPlugin",
  displayName = "Indra Publishing",
  description = "Reasonable publishing configuration"
)
