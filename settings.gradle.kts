rootProject.name = "indra"

listOf(
  "indra-common",
  "indra-publishing-sonatype"
).forEach {
  include(it)
}
