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
package net.kyori.indra.gradle;

import java.util.List;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.provider.Property;

/**
 * An extension providing helpers for plugin publishing.
 *
 * @since 1.3.0
 */
public interface IndraPluginPublishingExtension {
  ListProperty<String> bundleTags();

  default void bundleTags(final List<String> bundleTags) {
    this.bundleTags().set(bundleTags);
  }

  /**
   * A property providing the base id that indra-declared plugin ids are relative to.
   *
   * @return the base for relative plugin ids
   * @since 2.0.0
   */
  Property<String> pluginIdBase();

  default void pluginIdBase(final String idBase) {
    this.pluginIdBase().set(idBase);
  }

  /**
   * Register a plugin to have marker validated, and to be deployed to the Gradle Plugin Portal.
   *
   * <p>If no tags are set on the global plugin bundle, then the first provided set of tags will be applied.</p>
   *
   * @param id the relative plugin id
   * @param mainClass the fully qualified name of the plugin class
   * @param displayName the display name for the plugin on the Gradle Plugin Portal
   * @since 2.0.0
   */
  default void plugin(final String id, final String mainClass, final String displayName) {
    this.plugin(id, mainClass, displayName, null);
  }

  /**
   * Register a plugin to have marker validated, and to be deployed to the Gradle Plugin Portal.
   *
   * <p>If no tags are set on the global plugin bundle, then the first provided set of tags will be applied.</p>
   *
   * @param id the relative plugin id
   * @param mainClass the fully qualified name of the plugin class
   * @param displayName the display name for the plugin on the Gradle Plugin Portal
   * @param description the plugin description
   * @since 2.0.0
   */
  default void plugin(final String id, final String mainClass, final String displayName, final @Nullable String description) {
    this.plugin(id, mainClass, displayName, description, null);
  }

  /**
   * Register a plugin to have marker validated, and to be deployed to the Gradle Plugin Portal.
   *
   * <p>The id is relative to {@link #pluginIdBase()}, which is by default the project's group id. Main class is absolute.</p>
   *
   * <p>If no tags are set on the global plugin bundle, then the first provided set of tags will be applied.</p>
   *
   * @param id the relative plugin id
   * @param mainClass the fully qualified name of the plugin class
   * @param displayName the display name for the plugin on the Gradle Plugin Portal
   * @param description the plugin description
   * @param tags tags used for the plugin on the plugin portal
   * @since 2.0.0
   */
  void plugin(final String id, final String mainClass, final String displayName, final @Nullable String description, final @Nullable List<String> tags);
}
