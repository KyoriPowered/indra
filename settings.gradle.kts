rootProject.name = "indra"

pluginManagement {
  plugins {
    kotlin("jvm") version embeddedKotlinVersion
  }
}

listOf(
  "indra-common",
  "indra-git",
  "indra-publishing-gradle-plugin",
  "indra-publishing-sonatype"
).forEach {
  include(it)
}
