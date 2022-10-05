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
package net.kyori.indra.internal.multirelease;

import java.util.HashSet;
import java.util.Set;
import javax.inject.Inject;
import net.kyori.indra.multirelease.MultireleaseSourceSet;
import net.kyori.indra.multirelease.MultireleaseVariantDetails;
import net.kyori.indra.task.CheckModuleExports;
import org.gradle.api.Action;
import org.gradle.api.DomainObjectSet;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.SourceSet;
import org.jetbrains.annotations.NotNull;

import static java.util.Objects.requireNonNull;

class MultireleaseSourceSetImpl implements MultireleaseSourceSet {
  static final int MINIMUM_MULTIRELEASE_VERSION = 8;

  private final DomainObjectSet<Integer> alternateVersions;
  private final Property<String> moduleName;
  private final Property<Boolean> applyToJavadoc;
  final Set<Action<MultireleaseVariantDetails>> alternateConfigurationActions = new HashSet<>();
  final Set<Action<? super CheckModuleExports>> exportValidation = new HashSet<>();

  @Inject
  public MultireleaseSourceSetImpl(final ObjectFactory objects) {
    this.alternateVersions = objects.domainObjectSet(Integer.class);
    this.moduleName = objects.property(String.class);
    this.applyToJavadoc = objects.property(Boolean.class).convention(false);
  }

  @Override
  public @NotNull DomainObjectSet<Integer> alternateVersions() {
    return this.alternateVersions;
  }

  @Override
  public @NotNull Property<String> moduleName() {
    return this.moduleName;
  }

  @Override
  public void alternateVersions(final int... alternates) {
    for (final int alternate : alternates) {
      this.alternateVersions.add(alternate);
    }
  }

  // the name of a versioned source set for a specific version
  public static String versionName(final SourceSet parent, final int version) {
    return parent.getTaskName(null, "java" + version);
  }

  @Override
  public void configureVariants(final @NotNull Action<MultireleaseVariantDetails> action) {
    this.alternateConfigurationActions.add(requireNonNull(action, "action"));
  }

  @Override
  public void requireAllPackagesExported() {
    this.requireAllPackagesExported(task -> {});
  }

  @Override
  public void requireAllPackagesExported(final @NotNull Action<? super CheckModuleExports> action) {
    this.exportValidation.add(action);
  }

  @Override
  public Property<Boolean> applyToJavadoc() {
    return this.applyToJavadoc;
  }
}
