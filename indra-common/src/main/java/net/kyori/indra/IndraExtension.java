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

import net.kyori.indra.api.model.ApplyTo;
import net.kyori.indra.api.model.ContinuousIntegration;
import net.kyori.indra.api.model.Issues;
import net.kyori.indra.api.model.License;
import net.kyori.indra.api.model.SourceCodeManagement;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.gradle.api.Action;
import org.gradle.api.provider.Property;
import org.gradle.api.publish.maven.MavenPublication;

import static java.util.Objects.requireNonNull;

/**
 * Extension exposing extra functionality provided by Indra.
 *
 * @since 2.0.0
 */
public interface IndraExtension {
  /**
   * Options controlling Java toolchain versions.
   *
   * @since 2.0.0
   */
  JavaToolchainVersions javaVersions();

  /**
   * Configure the versioning configuration.
   *
   * @since 2.0.0
   */
  void javaVersions(Action<JavaToolchainVersions> action);

  Property<ContinuousIntegration> ci();

  void ci(ContinuousIntegration ci);

  void ci(Action<ContinuousIntegration.Builder> configureAction);

  default void jenkins(String url) {
    requireNonNull(url, "url");

    this.ci(ContinuousIntegration.builder()
      .system("Jenkins")
      .url(url)
      .build());
  }

  Property<Issues> issues();

  void issues(Issues issues);

  void issues(Action<Issues.Builder> configureAction);

  Property<SourceCodeManagement> scm();

  void scm(SourceCodeManagement scm);

  void scm(Action<SourceCodeManagement.Builder> configureAction);

  Property<License> license();

  void license(License license);

  void license(Action<License.Builder> configureAction);

  default void apache2License() {
    this.license(License.apache2());
  }

  default void gpl3OnlyLicense() {
    this.license(License.gpl3Only());
  }

  default void mitLicense() {
    this.license(License.mit());
  }

  default void github(@NonNull String user, @NonNull String repo) {
    this.github(user, repo, null);
  }

  void github(String user, String repo, @Nullable Action<ApplyTo> applicable);

  default void gitlab(@NonNull String user, @NonNull String repo) {
    this.gitlab(user, repo, null);
  }

  void gitlab(@NonNull String user, @NonNull String repo, @Nullable Action<ApplyTo> applicable);

  // Publishing repositories

  void publishAllTo(String id, String url);

  void publishReleasesTo(String id, String url);

  void publishSnapshotsTo(String id, String url);

  void configurePublications(@NonNull Action<MavenPublication> action);

  /**
   * A property representing the version of checkstyle to be used.
   *
   * <p>If any custom additions are applied to the {@code checkstyle} configuration,
   * this value <b>will be ignored.</b></p>
   *
   * @return the checkstyle version property
   * @since 2.0.0
   */
  Property<String> checkstyle();

  /**
   * Set the version of checkstyle to be used.
   *
   * @param checkstyleVersion the target checkstyle version
   * @see #checkstyle() for information on limitations
   * @since 2.0.0
   */
  default void checkstyle(final String checkstyleVersion) {
    this.checkstyle().set(checkstyleVersion);
  }

  Property<Boolean> reproducibleBuilds();

  default void reproducibleBuilds(final boolean reproducibleBuilds) {
    this.reproducibleBuilds().set(reproducibleBuilds);
  }

  /**
   * Whether the {@code java} {@link org.gradle.api.component.SoftwareComponent} should be
   * automatically included in publications.
   *
   * <p>This property does not usually need to be changed, unless working with Gradle plugins
   * that publish in a non-standard way.</p>
   *
   * @return the property representing this option
   * @since 2.0.0
   */
  Property<Boolean> includeJavaSoftwareComponentInPublications();

  default void includeJavaSoftwareComponentInPublications(boolean include) {
    this.includeJavaSoftwareComponentInPublications().set(include);
  }
}
