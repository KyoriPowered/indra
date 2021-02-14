/*
 * This file is part of indra, licensed under the MIT License.
 *
 * Copyright (c) 2020 KyoriPowered
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

import java.net.URI;
import net.kyori.indra.util.ImmutablesStyle;
import org.gradle.api.artifacts.dsl.RepositoryHandler;
import org.immutables.value.Value;

/**
 * A definition for a repository that can be added to projects.
 *
 * @since 2.0.0
 * @see Repositories#registerRepositoryExtensions(RepositoryHandler, Iterable) to registere remote repository extensions
 */
@Value.Immutable(builder = false)
@ImmutablesStyle
public interface RemoteRepository {
  RemoteRepository SONATYPE_SNAPSHOTS = RemoteRepository.snapshotsOnly("sonatypeSnapshots", "https://oss.sonatype.org/content/repositories/snapshots/");

  /**
   * Create a repository that will publish/resolve both releases and snapshots.
   *
   * @param name the repository name
   * @param url the repository URL
   * @return a new repository description
   * @since 2.0.0
   */
  static RemoteRepository all(final String name, final URI url) {
    return new RemoteRepositoryImpl(name, url, true, true);
  }

  /**
   * Create a repository that will publish/resolve both releases and snapshots.
   *
   * @param name the repository name
   * @param url the repository URL
   * @return a new repository description
   * @since 2.0.0
   */
  static RemoteRepository all(final String name, final String url) {
    return new RemoteRepositoryImpl(name, Repositories.uri(url), true, true);
  }

  /**
   * Create a repository that will only publish/resolve releases.
   *
   * @param name the repository name
   * @param url the repository URL
   * @return a new repository description
   * @since 2.0.0
   */
  static RemoteRepository releasesOnly(final String name, final URI url) {
    return new RemoteRepositoryImpl(name, url, true, false);
  }

  /**
   * Create a repository that will only publish/resolve releases.
   *
   * @param name the repository name
   * @param url the repository URL
   * @return a new repository description
   * @since 2.0.0
   */
  static RemoteRepository releasesOnly(final String name, final String url) {
    return new RemoteRepositoryImpl(name, Repositories.uri(url), true, false);
  }

  /**
   * Create a repository that will only publish/resolve snapshots.
   *
   * @param name the repository name
   * @param url the repository URL
   * @return a new repository description
   * @since 2.0.0
   */
  static RemoteRepository snapshotsOnly(final String name, final URI url) {
    return new RemoteRepositoryImpl(name, url, false, true);
  }

  /**
   * Create a repository that will only publish/resolve snapshots.
   *
   * @param name the repository name
   * @param url the repository URL
   * @return a new repository description
   * @since 2.0.0
   */
  static RemoteRepository snapshotsOnly(final String name, final String url) {
    return new RemoteRepositoryImpl(name, Repositories.uri(url), false, true);
  }

  /**
   * The name of the repository.
   *
   * <p>This name should be provided in {@code camelCase} format.</p>
   *
   * @return the internal repository name
   * @since 2.0.0
   */
  @Value.Parameter
  String name();

  /**
   * The URL providing the remote location of this repository.
   *
   * @return the repository URL
   * @since 2.0.0
   */
  @Value.Parameter
  URI url();

  /**
   * Whether releases should be included in this repository.
   *
   * @return whether releases should be published/resolved
   * @since 2.0.0
   */
  @Value.Parameter
  @Value.Default
  default boolean releases() {
    return true;
  }

  /**
   * Whether snapshots should be included in this repository.
   *
   * @return whether snapshots should be published/resolved
   * @since 2.0.0
   */
  @Value.Parameter
  @Value.Default
  default boolean snapshots() {
    return true;
  }
}
