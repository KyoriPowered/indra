/*
 * This file is part of indra, licensed under the MIT License.
 *
 * Copyright (c) 2020-2021 KyoriPowered
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
import net.kyori.mammoth.Configurable;
import org.gradle.api.Action;
import org.gradle.api.provider.Property;
import org.gradle.api.publish.maven.MavenPublication;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
  @NotNull JavaToolchainVersions javaVersions();

  /**
   * Configure the versioning configuration.
   *
   * @since 2.0.0
   */
  void javaVersions(final @NotNull Action<JavaToolchainVersions> action);

  @NotNull Property<ContinuousIntegration> ci();

  default void ci(final @NotNull ContinuousIntegration ci) {
    this.ci().set(ci);
  }

  default void ci(final @NotNull Action<ContinuousIntegration.Builder> configureAction) {
    this.ci().set(Configurable.configure(ContinuousIntegration.builder(), configureAction).build());
  }

  default void jenkins(final @NotNull String url) {
    requireNonNull(url, "url");

    this.ci(ContinuousIntegration.builder()
      .system("Jenkins")
      .url(url)
      .build());
  }

  @NotNull Property<Issues> issues();

  default void issues(final @NotNull Issues issues) {
    this.issues().set(issues);
  }

  default void issues(final @NotNull Action<Issues.Builder> configureAction) {
    this.issues().set(Configurable.configure(Issues.builder(), configureAction).build());
  }

  @NotNull Property<SourceCodeManagement> scm();

  default void scm(final @NotNull SourceCodeManagement scm) {
    this.scm().set(scm);
  }

  default void scm(final @NotNull Action<SourceCodeManagement.Builder> configureAction) {
    this.scm().set(Configurable.configure(SourceCodeManagement.builder(), configureAction).build());
  }

  @NotNull Property<License> license();

  default void license(final @NotNull License license) {
    this.license().set(license);
  }

  default void license(final @NotNull Action<License.Builder> configureAction) {
    this.license().set(Configurable.configure(License.builder(), configureAction).build());
  }

  default void apache2License() {
    this.license(License.apache2());
  }

  default void gpl3OnlyLicense() {
    this.license(License.gpl3Only());
  }

  default void gpl3OrLaterLicense() {
    this.license(License.gpl3OrLater());
  }

  default void lgpl3OnlyLicense() {
    this.license(License.lgpl3Only());
  }

  default void lgpl3OrLaterLicense() {
    this.license(License.lgpl3OrLater());
  }

  default void mitLicense() {
    this.license(License.mit());
  }

  default void mpl2License() {
    this.license(License.mpl2());
  }

  default void github(final @NotNull String user, final @NotNull String repo) {
    this.github(user, repo, null);
  }

  void github(final @NotNull String user, final @NotNull String repo, final @Nullable Action<ApplyTo> applicable);

  default void gitlab(final @NotNull String user, final @NotNull String repo) {
    this.gitlab(user, repo, null);
  }

  void gitlab(final @NotNull String user, final @NotNull String repo, final @Nullable Action<ApplyTo> applicable);

  // Publishing repositories

  void publishAllTo(final @NotNull String id, final @NotNull String url);

  void publishReleasesTo(final @NotNull String id, final @NotNull String url);

  void publishSnapshotsTo(final @NotNull String id, final @NotNull String url);

  void configurePublications(final @NotNull Action<MavenPublication> action);

  /**
   * A property representing the version of checkstyle to be used.
   *
   * <p>If any custom additions are applied to the {@code checkstyle} configuration,
   * this value <b>will be ignored.</b></p>
   *
   * @return the checkstyle version property
   * @since 2.0.0
   */
  @NotNull Property<String> checkstyle();

  /**
   * Set the version of checkstyle to be used.
   *
   * @param checkstyleVersion the target checkstyle version
   * @see #checkstyle() for information on limitations
   * @since 2.0.0
   */
  default void checkstyle(final @NotNull String checkstyleVersion) {
    this.checkstyle().set(checkstyleVersion);
  }

  /**
   * Whether options that support reproducible builds should be enabled.
   *
   * <p>Default: {@code true}</p>
   *
   * @return the property configuring reproducible builds
   * @since 2.0.0
   */
  @NotNull Property<Boolean> reproducibleBuilds();

  /**
   * Set whether options that support reproducible builds should be enabled.
   *
   * <p>Default: {@code true}</p>
   *
   * @param reproducibleBuilds whether to build in a way that encourages reproducibility
   * @since 2.0.0
   */
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
  @NotNull Property<Boolean> includeJavaSoftwareComponentInPublications();

  /**
   * Set whether the {@code java} component should be included in publications.
   *
   * @param include whether the component should be included
   * @see #includeJavaSoftwareComponentInPublications() for more details
   * @since 2.0.0
   */
  default void includeJavaSoftwareComponentInPublications(final boolean include) {
    this.includeJavaSoftwareComponentInPublications().set(include);
  }
}
