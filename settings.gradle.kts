rootProject.name = "indra"

pluginManagement {
  plugins {
    kotlin("jvm") version "1.5.31"
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
