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

import org.checkerframework.checker.nullness.qual.NonNull;
import org.gradle.api.provider.Property;
import org.gradle.api.provider.Provider;
import org.gradle.api.provider.SetProperty;

/**
 * Options configuring Java toolchain versioning.
 *
 * @since 1.1.0
 */
public interface JavaToolchainVersions {
  /**
   * The target Java version to compile for.
   *
   * <p>Default: 8</p>
   *
   * @return a property providing the target version
   * @since 1.1.1
   */
  @NonNull Property<Integer> target();

  /**
   * Set the target to compile Java for.
   *
   * @param target the java compile target
   * @since 2.0.0
   */
  void target(final int target);

  /**
   * The minimum toolchain version to use when building.
   *
   * <p>Default: 11</p>
   *
   * @return a property providing the minimum toolchain version
   */
  @NonNull Property<Integer> minimumToolchain();

  /**
   * Sets the minimum toolchain version.
   *
   * @param minimumToolchain the minimum toolchain version
   */
  void minimumToolchain(final int minimumToolchain);

  /**
   * Whether to strictly apply toolchain versions.
   *
   * <p>If this is set to {@code false}, java toolchains will only be overridden for
   * the Gradle JVM's version is less than the target version,
   * and cross-version testing will not be performed.</p>
   *
   * <p>Default: {@code false} unless the {@code CI} environment variable or {@code strictMultireleaseVersions} gradle property are set.</p>
   *
   * @return whether strict versions are enabled
   * @since 2.0.0
   */
  @NonNull Property<Boolean> strictVersions();

  void strictVersions(final boolean strictVersions);

  /**
   * Toolchains that should be used to execute tests when strict versions are enabled.
   *
   * @return a property containing the versions to test with
   * @since 2.0.0
   */
  @NonNull SetProperty<Integer> testWith();

  /**
   * Add alternate versions that should be tested with, when strict versions are enabled.
   * 
   * @param testVersions versions to test with
   * @since 2.0.0
   */
  void testWith(final int... testVersions);

  /**
   * Whether to enable Java preview features on compile, test, and execution tasks.
   *
   * @return a property providing preview feature enabled state
   * @since 2.0.0
   */
  @NonNull Property<Boolean> previewFeaturesEnabled();

  /**
   * Set whether Java preview features are enabled on compile, test, and execution tasks.
   *
   * @param previewFeaturesEnabled whether to enable preview features
   * @since 2.0.0
   */
  void previewFeaturesEnabled(final boolean previewFeaturesEnabled);

  /**
   * The version that should be used to run compile tasks, taking into account strict version and current JVM.
   *
   * @return a provider resolving the actual Java toolchain version to use
   */
  @NonNull Provider<Integer> actualVersion();
}
