/*
 * This file is part of indra, licensed under the MIT License.
 *
 * Copyright (c) 2020-2022 KyoriPowered
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
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
