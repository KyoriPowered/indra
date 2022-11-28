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

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import javax.inject.Inject;
import net.kyori.indra.IndraExtension;
import net.kyori.indra.JavaToolchainVersions;
import net.kyori.indra.api.model.ApplyTo;
import net.kyori.indra.api.model.ContinuousIntegration;
import net.kyori.indra.api.model.Issues;
import net.kyori.indra.api.model.License;
import net.kyori.indra.api.model.SourceCodeManagement;
import net.kyori.mammoth.Configurable;
import net.kyori.mammoth.Properties;
import org.gradle.api.Action;
import org.gradle.api.DomainObjectSet;
import org.gradle.api.GradleException;
import org.gradle.api.file.ProjectLayout;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Property;
import org.gradle.api.provider.Provider;
import org.gradle.api.provider.ProviderFactory;
import org.gradle.api.publish.maven.MavenPublication;
import org.gradle.plugins.signing.SigningExtension;
import org.gradle.process.CommandLineArgumentProvider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static java.util.Objects.requireNonNull;

public class IndraExtensionImpl implements IndraExtension {

  private static final Logger LOGGER = Logging.getLogger(IndraExtensionImpl.class);
  private static final String DEFAULT_CHECKSTYLE_VERSION = "9.3";

  private final Property<ContinuousIntegration> ci;
  private final Property<Issues> issues;
  private final Property<License> license;
  private final Property<SourceCodeManagement> scm;

  private final Property<Boolean> reproducibleBuilds;
  private final Property<String> checkstyle;

  private final Property<Boolean> includeJavaSoftwareComponentInPublications;

  private final JavaToolchainVersionsImpl javaVersions;
  final DomainObjectSet<RemoteRepository> repositories;

  private transient final ProviderFactory providers;
  private transient final ProjectLayout layout;
  private transient @Nullable Action<SigningExtension> signingAction;
  private transient SigningExtension signingExtension;

  private boolean alternateSigningConfigured;

  @Inject
  public IndraExtensionImpl(final ObjectFactory objects, final ProviderFactory providers, final ProjectLayout layout) {
    this.providers = providers;
    this.layout = layout;
    this.ci = objects.property(ContinuousIntegration.class);
    this.issues = objects.property(Issues.class);
    this.license = objects.property(License.class);
    this.scm = objects.property(SourceCodeManagement.class);

    this.reproducibleBuilds = objects.property(Boolean.class).convention(true);

    this.checkstyle = objects.property(String.class).convention(DEFAULT_CHECKSTYLE_VERSION);
    this.includeJavaSoftwareComponentInPublications = objects.property(Boolean.class).convention(true);
    this.javaVersions = objects.newInstance(JavaToolchainVersionsImpl.class);
    this.repositories = objects.domainObjectSet(RemoteRepository.class);
  }

  @Override
  public @NotNull JavaToolchainVersions javaVersions() {
    return this.javaVersions;
  }

  @Override
  public void javaVersions(final @NotNull Action<JavaToolchainVersions> action) {
    Configurable.configure(this.javaVersions, action);
  }

  public CommandLineArgumentProvider previewFeatureArgumentProvider() {
    // needs to be a static class to avoid capturing `this`
    return new PreviewFeatureArgumentProvider(this.javaVersions.previewFeaturesEnabled());
  }

  // Metadata properties //

  @Override
  public @NotNull Property<ContinuousIntegration> ci() {
    return this.ci;
  }

  @Override
  public @NotNull Property<Issues> issues() {
    return this.issues;
  }

  @Override
  public @NotNull Property<SourceCodeManagement> scm() {
    return this.scm;
  }

  @Override
  public @NotNull Property<License> license() {
    return this.license;
  }

  // Configuration for specific platforms //

  @Override
  public void github(final @NotNull String user, final @NotNull String repo, final @Nullable Action<ApplyTo> applicable) {
    final ApplyTo options = Configurable.configureIfNonNull(ApplyTo.defaults(), applicable);

    if (options.ci()) {
      this.ci(ci -> ci.system("GitHub Actions").url(String.format("https://github.com/%s/%s/actions", user, repo)));
    }
    if (options.issues()) {
      this.issues(issues -> issues.system("GitHub").url(String.format("https://github.com/%s/%s/issues", user, repo)));
    }
    if (options.scm()) {
      this.scm(scm -> scm
        .connection(String.format("scm:git:https://github.com/%s/%s.git", user, repo))
        .developerConnection(String.format("scm:git:ssh://git@github.com/%s/%s.git", user, repo))
        .url(String.format("https://github.com/%s/%s", user, repo)));
    }
    if (options.publishing()) {
      this.publishReleasesTo("githubPackages", String.format("https://maven.pkg.github.com/%s/%s", user, repo));
    }
  }

  @Override
  public void gitlab(final @NotNull String user, final @NotNull String repo, final @Nullable Action<ApplyTo> applicable) {
    final ApplyTo options = Configurable.configureIfNonNull(ApplyTo.defaults(), applicable);

    if (options.ci()) {
      this.ci(ci -> ci.system("GitLab CI").url(String.format("https://gitlab.com/%s/%s/-/pipelines", user, repo)));
    }
    if (options.issues()) {
      this.issues(issues -> issues.system("GitLab").url(String.format("https://gitlab.com/%s/%s/-/issues", user, repo)));
    }
    if (options.scm()) {
      this.scm(scm -> scm
        .connection(String.format("scm:git:https://gitlab.com/%s/%s.git", user, repo))
        .developerConnection(String.format("scm:git:ssh://git@gitlab.com/%s/%s.git", user, repo))
        .url(String.format("https://gitlab.com/%s/%s", user, repo))
      );
    }
    if (options.publishing()) {
      // TODO: needs project ID, which is separate from user/repo and uses HTTP header-based auth
      throw new GradleException("Publishing cannot yet be automatically configured for GitLab projects");
    }
  }

  // Publishing

  final Set<Action<MavenPublication>> publishingActions = new HashSet<>();

  @Override
  public void publishAllTo(final @NotNull String id, final @NotNull String url) {
    this.repositories.add(RemoteRepository.all(id, url));
  }

  @Override
  public void publishReleasesTo(final @NotNull String id, final @NotNull String url) {
    this.repositories.add(RemoteRepository.releasesOnly(id, url));
  }

  @Override
  public void publishSnapshotsTo(final @NotNull String id, final @NotNull String url) {
    this.repositories.add(RemoteRepository.snapshotsOnly(id, url));
  }

  @Override
  public void configurePublications(final @NotNull Action<MavenPublication> action) {
    this.publishingActions.add(requireNonNull(action, "action"));
  }

  @Override
  public @NotNull Property<String> checkstyle() {
    return this.checkstyle;
  }

  @Override
  public @NotNull Property<Boolean> reproducibleBuilds() {
    return this.reproducibleBuilds;
  }

  @Override
  public @NotNull Property<Boolean> includeJavaSoftwareComponentInPublications() {
    return this.includeJavaSoftwareComponentInPublications;
  }

  @Override
  public void signWithKeyFromPrefixedProperties(final String prefix) {
    if (prefix.isEmpty()) {
      throw new IllegalArgumentException("Prefix '" + prefix + "' must not be empty");
    }

    this.signWithKeyFromProperties(
      prefix + "SigningKey",
      prefix + "SigningPassword"
    );
  }

  @Override
  public void signWithKeyFromProperties(final String keyFileOrContentsProperty, final String keyPasswordProperty) {
    final Provider<String> keyFileOrContents = this.providers.gradleProperty(keyFileOrContentsProperty);
    final Provider<String> keyPassword = this.providers.gradleProperty(keyPasswordProperty);
    if (!keyFileOrContents.isPresent()) {
      LOGGER.info("Skipping configuring file-based signing because property '{}' had no value", keyFileOrContentsProperty);
      return;
    }
    if (!keyPassword.isPresent()) {
      LOGGER.info("Skipping configuring file-based signing because property '{}' had no value", keyPasswordProperty);
      return;
    }

    this.alternateSigningConfigured = true;
    if (this.signingExtension != null) {
      this.configureSigningExtension(this.signingExtension, keyFileOrContents.get(), keyPassword.get());
    } else {
      this.signingAction = ext -> this.configureSigningExtension(ext, keyFileOrContents.get(), keyPassword.get());
    }
  }

  private void configureSigningExtension(final SigningExtension extension, final String keyFileOrContents, final String keyPassword) {
    final File keyFile = this.layout.getProjectDirectory().file(keyFileOrContents).getAsFile();
    if (keyFile.exists()) {
      final StringBuilder contents = new StringBuilder();
      try (final BufferedReader reader = Files.newBufferedReader(keyFile.toPath(), StandardCharsets.UTF_8)) {
        final char[] buf = new char[2048];
        int read;
        while ((read = reader.read(buf)) != -1) {
          contents.append(buf, 0, read);
        }
      } catch (final IOException ex) {
        throw new GradleException("Failed to read signing key file", ex);
      }
      extension.useInMemoryPgpKeys(contents.toString(), keyPassword);
    } else {
      extension.useInMemoryPgpKeys(keyFileOrContents, keyPassword);
    }
  }

  public void initSigningExtension(final SigningExtension extension) {
    if (this.signingAction != null) {
      try {
        this.signingAction.execute(extension);
      } finally {
        this.signingAction = null;
      }
    }
    this.signingExtension = extension;
  }

  public boolean alternateSigningConfigured() {
    return this.alternateSigningConfigured;
  }

  static class PreviewFeatureArgumentProvider implements CommandLineArgumentProvider {
    private final Property<Boolean> previewFeaturesEnabledProp;

    PreviewFeatureArgumentProvider(final Property<Boolean> previewFeaturesEnabledProp) {
      this.previewFeaturesEnabledProp = previewFeaturesEnabledProp;
    }

    @Override
    public Iterable<String> asArguments() {
      if (Properties.finalized(this.previewFeaturesEnabledProp).get()) {
        return Collections.singletonList("--enable-preview");
      } else {
        return Collections.emptyList();
      }
    }
  }
}
