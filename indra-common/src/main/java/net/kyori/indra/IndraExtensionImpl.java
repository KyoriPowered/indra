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
package net.kyori.indra;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import javax.inject.Inject;
import net.kyori.indra.api.model.ApplyTo;
import net.kyori.indra.api.model.ContinuousIntegration;
import net.kyori.indra.api.model.Issues;
import net.kyori.indra.api.model.License;
import net.kyori.indra.api.model.SourceCodeManagement;
import net.kyori.gradle.api.Configurable;
import net.kyori.indra.repository.RemoteRepository;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.gradle.api.Action;
import org.gradle.api.DomainObjectSet;
import org.gradle.api.GradleException;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Property;
import org.gradle.api.publish.maven.MavenPublication;
import org.gradle.process.CommandLineArgumentProvider;

import static java.util.Objects.requireNonNull;

class IndraExtensionImpl implements IndraExtension {
  private final Property<ContinuousIntegration> ci;
  private final Property<Issues> issues;
  private final Property<License> license;
  private final Property<SourceCodeManagement> scm;

  private final Property<Boolean> reproducibleBuilds;
  private final Property<String> checkstyle;

  /**
   * Whether the {@code java} {@link org.gradle.api.component.SoftwareComponent} should be
   * automatically included in publications.
   *
   * <p>This property does not usually need to be changed, unless working with Gradle plugins
   * that publish in a non-standard way.</p>
   */
  private final Property<Boolean> includeJavaSoftwareComponentInPublications;

  private final JavaToolchainVersions javaVersions;
  final DomainObjectSet<RemoteRepository> repositories;

  @Inject
  public IndraExtensionImpl(final ObjectFactory objects) {
    this.ci = objects.property(ContinuousIntegration.class);
    this.issues = objects.property(Issues.class);
    this.license = objects.property(License.class);
    this.scm = objects.property(SourceCodeManagement.class);

    this.reproducibleBuilds = objects.property(Boolean.class).convention(true);

    this.checkstyle = objects.property(String.class).convention("8.37");
    this.includeJavaSoftwareComponentInPublications = objects.property(Boolean.class).convention(true);
    this.javaVersions = objects.newInstance(JavaToolchainVersions.class);
    this.repositories = objects.domainObjectSet(RemoteRepository.class);
  }

  @Override
  public JavaToolchainVersions javaVersions() {
    return this.javaVersions;
  }

  @Override
  public void javaVersions(final Action<JavaToolchainVersions> action) {
    requireNonNull(action, "action").execute(this.javaVersions);
  }

  public CommandLineArgumentProvider previewFeatureArgumentProvider() {
    return () -> {
      this.javaVersions.previewFeaturesEnabled().finalizeValue();
      if(this.javaVersions.previewFeaturesEnabled().get()) {
        return Collections.singletonList("--enable-preview");
      } else {
        return Collections.emptyList();
      }
    };
  }

  // Metadata properties //

  @Override
  public Property<ContinuousIntegration> ci() {
    return this.ci;
  }

  @Override
  public void ci(final ContinuousIntegration ci) {
    this.ci.set(ci);
  }

  @Override
  public void ci(final Action<ContinuousIntegration.Builder> configureAction) {
    this.ci.set(Configurable.configure(ContinuousIntegration.builder(), configureAction).build());
  }

  @Override
  public Property<Issues> issues() {
    return this.issues;
  }

  @Override
  public void issues(final Issues issues) {
    this.issues.set(issues);
  }

  @Override
  public void issues(final Action<Issues.Builder> configureAction) {
    this.issues.set(Configurable.configure(Issues.builder(), configureAction).build());
  }

  @Override
  public Property<SourceCodeManagement> scm() {
    return this.scm;
  }

  @Override
  public void scm(final SourceCodeManagement scm) {
    this.scm.set(scm);
  }

  @Override
  public void scm(final Action<SourceCodeManagement.Builder> configureAction) {
    this.scm.set(Configurable.configure(SourceCodeManagement.builder(), configureAction).build());
  }

  @Override
  public Property<License> license() {
    return this.license;
  }

  @Override
  public void license(final License license) {
    this.license.set(license);
  }

  @Override
  public void license(final Action<License.Builder> configureAction) {
    this.license.set(Configurable.configure(License.builder(), configureAction).build());
  }

  // Configuration for specific platforms //

  @Override
  public void github(final String user, final String repo, final @Nullable Action<ApplyTo> applicable) {
    final ApplyTo options = Configurable.configureIfNonNull(ApplyTo.defaults(), applicable);

    if(options.ci()) {
      this.ci(ci -> ci.system("GitHub Actions").url(String.format("https://github.com/%s/%s/actions", user, repo)));
    }
    if(options.issues()) {
      this.issues(issues -> issues.system("GitHub").url(String.format("https://github.com/%s/%s/issues", user, repo)));
    }
    if(options.scm()) {
      this.scm(scm -> scm
        .connection(String.format("scm:git:https://github.com/%s/%s.git", user, repo))
        .developerConnection(String.format("scm:git:ssh://git@github.com/%s/%s.git", user, repo))
        .url(String.format("https://github.com/%s/%s", user, repo)));
    }
    if(options.publishing()) {
      this.publishReleasesTo("githubPackages", String.format("https://maven.pkg.github.com/%s/%s", user, repo));
    }
  }

  @Override
  public void gitlab(final @NonNull String user, final @NonNull String repo, final @Nullable Action<ApplyTo> applicable) {
    final ApplyTo options = Configurable.configureIfNonNull(ApplyTo.defaults(), applicable);

    if(options.ci()) {
      this.ci(ci -> ci.system("GitLab CI").url(String.format("https://gitlab.com/%s/%s/-/pipelines", user, repo)));
    }
    if(options.issues()) {
      this.issues(issues -> issues.system("GitLab").url(String.format("https://gitlab.com/%s/%s/-/issues", user, repo)));
    }
    if(options.scm()) {
      this.scm(scm -> scm
        .connection(String.format("scm:git:https://gitlab.com/%s/%s.git", user, repo))
        .developerConnection(String.format("scm:git:ssh://git@gitlab.com/%s/%s.git", user, repo))
        .url(String.format("https://gitlab.com/%s/%s", user, repo))
      );
    }
    if(options.publishing()) {
      // TODO: needs project ID, which is separate from user/repo and uses HTTP header-based auth
      throw new GradleException("Publishing cannot yet be automatically configured for GitLab projects");
    }
  }

  // Publishing

  final Set<Action<MavenPublication>> publishingActions = new HashSet<>();

  @Override
  public void publishAllTo(final String id, final String url) {
    this.repositories.add(RemoteRepository.all(id, url));
  }

  @Override
  public void publishReleasesTo(final String id, final String url) {
    this.repositories.add(RemoteRepository.releasesOnly(id, url));
  }

  @Override
  public void publishSnapshotsTo(final String id, final String url) {
    this.repositories.add(RemoteRepository.snapshotsOnly(id, url));
  }

  @Override
  public void configurePublications(final @NonNull Action<MavenPublication> action) {
    this.publishingActions.add(requireNonNull(action, "action"));
  }

  @Override
  public Property<String> checkstyle() {
    return this.checkstyle;
  }

  @Override
  public Property<Boolean> reproducibleBuilds() {
    return this.reproducibleBuilds;
  }

  @Override
  public Property<Boolean> includeJavaSoftwareComponentInPublications() {
    return this.includeJavaSoftwareComponentInPublications;
  }
}
