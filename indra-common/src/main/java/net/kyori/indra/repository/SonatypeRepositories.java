package net.kyori.indra.repository;

import org.gradle.api.artifacts.dsl.RepositoryHandler;
import org.gradle.api.artifacts.repositories.MavenArtifactRepository;

/**
 * An extension to {@link RepositoryHandler} that allows configuring Sonatype OSSRH snapshot repositories.
 *
 * @since 2.1.0
 */
public interface SonatypeRepositories {
  String EXTENSION_NAME = "sonatype";

  /**
   * Add the Sonatype OSS snapshot repository to this project.
   *
   * <p>The URL for this repository is {@literal https://oss.sonatype.org/content/repositories/snapshots/}.</p>
   *
   * @return the created repository
   * @since 2.1.0
   */
  MavenArtifactRepository ossSnapshots();

  /**
   * Add the new Sonatype OSS snapshot repository to this project.
   *
   * <p>The URL for this repository is {@literal https://s01.oss.sonatype.org/content/repositories/snapshots/}.</p>
   *
   * @return the created repository
   * @since 2.1.0
   */
  default MavenArtifactRepository s01Snapshots() {
    return this.snapshotsOn(1);
  }

  /**
   * Add a specific numbered Sonatype OSS snapshots host to this project.
   *
   * <p>The URL for this repository is {@code https://s<host>.oss.sonatype.org/content/repositories/snapshots/}.</p>
   *
   * <p>As of this writing, only one host exists -- {@code s01}.</p>
   *
   * @param host the host number
   * @return the created repository
   * @since 2.1.0
   */
  MavenArtifactRepository snapshotsOn(int host);
}
