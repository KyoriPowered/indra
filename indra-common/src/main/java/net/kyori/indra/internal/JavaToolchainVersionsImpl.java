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
package net.kyori.indra.internal;

import javax.inject.Inject;
import net.kyori.indra.JavaToolchainVersions;
import net.kyori.indra.util.Versioning;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.gradle.api.JavaVersion;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Property;
import org.gradle.api.provider.Provider;
import org.gradle.api.provider.ProviderFactory;
import org.gradle.api.provider.SetProperty;

public class JavaToolchainVersionsImpl implements JavaToolchainVersions {
  private static final String STRICT_MULTIRELEASE_VERSIONS = "strictMultireleaseVersions";
  private static final String CI = "CI";

  private final Property<Integer> target;
  private final Property<Integer> minimumToolchain;
  private final Property<Boolean> strictVersions;
  private final SetProperty<Integer> testWith;
  private final Property<Boolean> enablePreviewFeatures;
  private final Provider<Integer> actualVersion;

  @Inject
  public JavaToolchainVersionsImpl(final ObjectFactory objects, final ProviderFactory providers) {
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

  @Override
  public @NonNull Property<Integer> target() {
    return this.target;
  }

  @Override
  public void target(final int target) {
    this.target.set(target);
  }

  @Override
  public @NonNull Property<Integer> minimumToolchain() {
    return this.minimumToolchain;
  }

  @Override
  public void minimumToolchain(final int minimumToolchain) {
    this.minimumToolchain.set(minimumToolchain);
  }

  @Override
  public @NonNull Property<Boolean> strictVersions() {
    return this.strictVersions;
  }

  @Override
  public void strictVersions(final boolean strictVersions) {
    this.strictVersions.set(strictVersions);
  }

  @Override
  public @NonNull SetProperty<Integer> testWith() {
    return this.testWith;
  }

  @Override
  public void testWith(final int... testVersions) {
    for(final int version : testVersions) {
      this.testWith.add(version);
    }
  }

  @Override
  public @NonNull Property<Boolean> previewFeaturesEnabled() {
    return this.enablePreviewFeatures;
  }

  @Override
  public void previewFeaturesEnabled(final boolean previewFeaturesEnabled) {
    this.enablePreviewFeatures.set(previewFeaturesEnabled);
  }

  @Override
  public @NonNull Provider<Integer> actualVersion() {
    return this.actualVersion;
  }
}
