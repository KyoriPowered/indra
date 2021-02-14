rootProject.name = "indra"

listOf(
  "indra-common",
  "indra-publishing-gradle-plugin",
  "indra-publishing-sonatype"
).forEach {
  include(it)
}
