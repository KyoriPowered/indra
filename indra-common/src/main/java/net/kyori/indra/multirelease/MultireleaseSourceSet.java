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
package net.kyori.indra.multirelease;

import net.kyori.indra.task.CheckModuleExports;
import org.gradle.api.Action;
import org.gradle.api.DomainObjectSet;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.SourceSet;
import org.jetbrains.annotations.NotNull;

/**
 * Information about multirelease variants of a source set.
 *
 * @since 2.0.0
 */
public interface MultireleaseSourceSet {
  /**
   * Given an existing source set, get the multirelease extension.
   *
   * <p>This will fail when the provided source set is already a multirelease variant of a base source set.</p>
   *
   * @param set the source set
   * @return the multirelease extension
   */
  static @NotNull MultireleaseSourceSet from(final @NotNull SourceSet set) {
    return set.getExtensions().getByType(MultireleaseSourceSet.class);
  }

  /**
   * Get the property listing alternate versions for this source set.
   *
   * @return the alternate versions property
   */
  @NotNull DomainObjectSet<Integer> alternateVersions();

  /**
   * Add alternate versions to this source set.
   *
   * @param alternates the alternate versions
   * @since 2.0.0
   */
  void alternateVersions(final int... alternates);

  /**
   * The module name needs to be explicitly passed if a modular multirelease jar is desired.
   *
   * <p>This is optional in non-modular environments.</p>
   *
   * @return the module name property
   * @since 2.0.0
   */
  @NotNull Property<String> moduleName();

  /**
   * The module name to explicitly pass if a modular multirelease jar is desired.
   *
   * <p>This is optional in non-modular environments.</p>
   *
   * @param moduleName the name of the module that the different version variants should contribute to
   * @since 2.0.0
   */
  default void moduleName(final @NotNull String moduleName) {
    this.moduleName().set(moduleName);
  }

  /**
   * Configure derived source sets.
   *
   * @param action an action that receives each source set
   * @since 2.0.0
   */
  void configureVariants(final @NotNull Action<MultireleaseVariantDetails> action);

  /**
   * Register a task to validate that all packages in the module this source set defines are exported.
   *
   * @since 2.1.0
   */
  void requireAllPackagesExported();

  /**
   * Register a task to validate that packages in the module this source set defines are exported.
   *
   * <p>The task is provided to allow for additional configuration, such as exclusions to required exported packages.</p>
   *
   * @param action an action to configure the created task
   * @since 2.1.0
   */
  void requireAllPackagesExported(final @NotNull Action<? super CheckModuleExports> action);
  /**
   * Configure whether multi-release contents are exposed to Javadoc generation.
   *
   * <p>This includes adding a module descriptor to Javadoc that may otherwise only be present in multi-release variants.</p>
   *
   * @return a property controlling whether Javadoc generation should be multi-release aware
   * @since 2.1.0
   */
  Property<Boolean> applyToJavadoc();

  /**
   * Configure whether multi-release contents are exposed to Javadoc generation.
   *
   * <p>This includes adding a module descriptor to Javadoc that may otherwise only be present in multi-release variants.</p>
   *
   * @param applyToJavadoc whether Javadoc generation should be multi-release aware
   * @since 2.1.0
   */
  default void applyToJavadoc(final boolean applyToJavadoc) {
    this.applyToJavadoc().set(applyToJavadoc);
  }
}
