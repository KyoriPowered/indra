rootProject.name = "indra"

listOf(
  "indra-common",
  "indra-publishing-bintray",
  "indra-publishing-sonatype"
).forEach {
  include(it)
}
