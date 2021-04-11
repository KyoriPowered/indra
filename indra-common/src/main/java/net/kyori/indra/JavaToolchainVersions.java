/*
 * This file is part of indra, licensed under the MIT License.
 *
 * Copyright (c) 2021 KyoriPowered
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

import javax.inject.Inject;
import net.kyori.indra.util.Versioning;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.gradle.api.JavaVersion;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Property;
import org.gradle.api.provider.Provider;
import org.gradle.api.provider.ProviderFactory;
import org.gradle.api.provider.SetProperty;

/**
 * Options configuring Java toolchain versioning.
 *
 * @since 1.1.0
 */
public class JavaToolchainVersions {
  private static final String STRICT_MULTIRELEASE_VERSIONS = "strictMultireleaseVersions";
  private static final String CI = "CI";

  private final Property<Integer> target;
  private final Property<Integer> minimumToolchain;
  private final Property<Boolean> strictVersions;
  private final SetProperty<Integer> testWith;
  private final Property<Boolean> enablePreviewFeatures;
  private final Provider<Integer> actualVersion;

  @Inject
  public JavaToolchainVersions(final ObjectFactory objects, final ProviderFactory providers) {
    this.target = objects.property(Integer.class).convention(8);
    this.minimumToolchain = objects.property(Integer.class).convention(11);
    this.strictVersions = objects.property(Boolean.class)
      .convention(
        providers.gradleProperty(STRICT_MULTIRELEASE_VERSIONS).forUseAtConfigurationTime()
          .orElse(providers.environmentVariable(CI).forUseAtConfigurationTime()) // set by GH Actions and Travis
          .map(Boolean::parseBoolean)
          .orElse(false)
      );
    this.testWith = objects.setProperty(Integer.class);
    this.testWith.add(this.target);
    this.enablePreviewFeatures = objects.property(Boolean.class).convention(false);
    this.actualVersion = this.strictVersions.map(strict -> {
      final int running = Versioning.versionNumber(JavaVersion.current());
      this.target.finalizeValue();
      this.minimumToolchain.finalizeValue();
      final int minimum = Math.max(this.minimumToolchain.get(), this.target.get()); // If target > minimum toolchain, the target is our new minimum
      if(strict || running < minimum) {
        return minimum;
      } else {
        return running;
      }
    });
  }

  /**
   * The target Java version to compile for.
   *
   * <p>Default: 8</p>
   *
   * @return a property providing the target version
   * @since 1.1.1
   */
  public @NonNull Property<Integer> target() {
    return this.target;
  }

  /**
   * Set the target to compile Java for.
   *
   * @param target the java compile target
   */
  public void target(final int target) {
    this.target.set(target);
  }

  /**
   * The minimum toolchain version to use when building.
   *
   * <p>Default: 11</p>
   *
   * @return A property providing the minimum toolchain version
   */
  public @NonNull Property<Integer> minimumToolchain() {
    return this.minimumToolchain;
  }

  public void minimumToolchain(final int minimumToolchain) {
    this.minimumToolchain.set(minimumToolchain);
  }

  /**
   * Whether to strictly apply toolchain versions.
   *
   * <p>If this is set to {@code false}, java toolchains will only be overridden for
   * the Gradle JVM's version is less than the target version,
   * and cross-version testing will not be performed.</p>
   *
   * <p>Default: {@code false} unless the {@code CI} environment variable or {@code strictMultireleaseVersions} gradle property are set.</p>
   */
  public @NonNull Property<Boolean> strictVersions() {
    return this.strictVersions;
  }

  public void strictVersions(final boolean strictVersions) {
    this.strictVersions.set(strictVersions);
  }

  /**
   * Toolchains that should be used to execute tests when strict versions are enabled.
   */
  public @NonNull SetProperty<Integer> testWith() {
    return this.testWith;
  }

  /**
   * Add alternate versions that should be tested with, when strict versions are enabled.
   */
  public void testWith(final int... testVersions) {
    for(final int version : testVersions) {
      this.testWith.add(version);
    }
  }

  /**
   * Whether to enable Java preview features on compile, test, and execution tasks.
   *
   * @return a property providing preview feature enabled state
   */
  public @NonNull Property<Boolean> previewFeaturesEnabled() {
    return this.enablePreviewFeatures;
  }

  public void previewFeaturesEnabled(final boolean previewFeaturesEnabled) {
    this.enablePreviewFeatures.set(previewFeaturesEnabled);
  }

  /**
   * The version that should be used to run compile tasks, taking into account strict version and current JVM.
   *
   * @return a provider resolving the actual Java toolchain version to use
   */
  public @NonNull Provider<Integer> actualVersion() {
    return this.actualVersion;
  }
}
