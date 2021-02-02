package net.kyori.indra.data

import java.net.URI

/**
 * A specification for a maven repository.
 */
data class RemoteRepository @JvmOverloads constructor(
  val name: String,
  val url: URI,
  val releases: Boolean = true,
  val snapshots: Boolean = true
) {
  companion object {
    val SONATYPE_SNAPSHOTS = RemoteRepository("sonatypeSnapshots", "https://oss.sonatype.org/content/repositories/snapshots/", releases = false)
  }

  @JvmOverloads constructor(name: String, url: String, releases: Boolean = true, snapshots: Boolean = true) : this(name, URI(url), releases, snapshots)
}
