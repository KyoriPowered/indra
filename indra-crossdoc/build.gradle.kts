dependencies {
  compileOnlyApi("org.checkerframework:checker-qual:3.13.0")
  api("net.kyori:mammoth:1.0.0")
}

indraPluginPublishing {
  plugin(
    "indra.crossdoc",
    "net.kyori.indra.crossdoc.CrossdocPlugin",
    "Indra Javadoc Cross-linking",
    "Perform cross-linking between Javadoc publications within a multi-module Gradle project",
    listOf("javadoc", "multimodule", "indra")
  )
}
