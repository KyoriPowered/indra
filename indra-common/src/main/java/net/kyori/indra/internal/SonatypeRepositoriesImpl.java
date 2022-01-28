package net.kyori.indra.internal;

import java.util.Locale;
import javax.inject.Inject;
import net.kyori.indra.repository.SonatypeRepositories;
import org.gradle.api.artifacts.dsl.RepositoryHandler;
import org.gradle.api.artifacts.repositories.MavenArtifactRepository;

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
      repo.mavenContent(content -> content.snapshotsOnly());
    });
  }

  @Override
  public MavenArtifactRepository snapshotsOn(final int host) {
    return this.repositories.maven(repo -> {
      repo.setName("sonatypeSnapshots");
      repo.setUrl(formatOssHost(host));
      repo.mavenContent(content -> content.snapshotsOnly());
    });
  }

  static String formatOssHost(final int host) {
    if (host < 1) {
      throw new IllegalArgumentException("Only hosts numbered >= 1 are supported, but " + host + " was provided");
    }
    return String.format(Locale.ROOT, "https://s%02d.oss.sonatype.org/content/repositories/snapshots/", host);
  }
}
