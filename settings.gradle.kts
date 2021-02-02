rootProject.name = "indra"

listOf(
  "indra-common",
  "indra-publishing-bintray",
  "indra-publishing-gradle-plugin",
  "indra-publishing-sonatype"
).forEach {
  include(it)
}
