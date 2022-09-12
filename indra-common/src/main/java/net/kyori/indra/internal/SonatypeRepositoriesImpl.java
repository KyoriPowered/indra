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
package net.kyori.indra.internal;

import java.util.Locale;
import javax.inject.Inject;
import net.kyori.indra.repository.SonatypeRepositories;
import org.gradle.api.artifacts.dsl.RepositoryHandler;
import org.gradle.api.artifacts.repositories.MavenArtifactRepository;
import org.gradle.api.artifacts.repositories.MavenRepositoryContentDescriptor;

public class SonatypeRepositoriesImpl implements SonatypeRepositories {
  private final RepositoryHandler repositories;

  @Inject
  public SonatypeRepositoriesImpl(final RepositoryHandler repositories) {
    this.repositories = repositories;
  }

  @Override
  public MavenArtifactRepository ossSnapshots() {
    return this.repositories.maven(repo -> {
      repo.setName("sonatypeSnapshots");
      repo.setUrl("https://oss.sonatype.org/content/repositories/snapshots/");
      repo.mavenContent(MavenRepositoryContentDescriptor::snapshotsOnly);
    });
  }

  @Override
  public MavenArtifactRepository snapshotsOn(final int host) {
    return this.repositories.maven(repo -> {
      repo.setName("sonatypeSnapshots");
      repo.setUrl(formatOssHost(host));
      repo.mavenContent(MavenRepositoryContentDescriptor::snapshotsOnly);
    });
  }

  static String formatOssHost(final int host) {
    if (host < 1) {
      throw new IllegalArgumentException("Only hosts numbered >= 1 are supported, but " + host + " was provided");
    }
    return String.format(Locale.ROOT, "https://s%02d.oss.sonatype.org/content/repositories/snapshots/", host);
  }
}
