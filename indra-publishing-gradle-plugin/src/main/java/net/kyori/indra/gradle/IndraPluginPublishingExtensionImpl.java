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
package net.kyori.indra.gradle;

import com.gradle.publish.PluginBundleExtension;
import com.gradle.publish.PluginConfig;
import java.util.List;
import javax.inject.Inject;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.provider.Property;
import org.gradle.plugin.devel.GradlePluginDevelopmentExtension;

public class IndraPluginPublishingExtensionImpl implements IndraPluginPublishingExtension {
  private final GradlePluginDevelopmentExtension publishingExtension;
  private final PluginBundleExtension pluginBundleExtension;
  private final ListProperty<String> bundleTags;
  private final Property<String> pluginIdBase;
  private final Property<String> website;

  @Inject
  public IndraPluginPublishingExtensionImpl(
    final ObjectFactory objects,
    final GradlePluginDevelopmentExtension publishingExtension,
    final PluginBundleExtension pluginBundleExtension
  ) {
    this.publishingExtension = publishingExtension;
    this.pluginBundleExtension = pluginBundleExtension;
    this.bundleTags = objects.listProperty(String.class);
    this.pluginIdBase = objects.property(String.class);
    this.website = objects.property(String.class);
  }

  @Override
  public ListProperty<String> bundleTags() {
    return this.bundleTags;
  }

  @Override
  public Property<String> pluginIdBase() {
    return this.pluginIdBase;
  }

  @Override
  public Property<String> website() {
    return this.website;
  }

  @Override
  public void plugin(final String id, final String mainClass, final String displayName, final @Nullable String description, final @Nullable List<String> tags) {
    final String qualifiedId = this.pluginIdBase.get() + '.' + id;
    this.publishingExtension.getPlugins().create(id, plugin -> {
      plugin.setId(qualifiedId);
      plugin.setImplementationClass(mainClass);
      plugin.setDisplayName(displayName);
      if(description != null) {
        plugin.setDescription(description);
      }
    });

    final PluginConfig plugin = this.pluginBundleExtension.getPlugins().maybeCreate(id);

    plugin.setId(qualifiedId);
    if(description != null) {
      plugin.setDescription(description);
    }
    plugin.setDisplayName(displayName);
    if(tags != null && !tags.isEmpty()) {
      plugin.setTags(tags);
    }

    if(tags != null && this.pluginBundleExtension.getTags().isEmpty()) {
      this.pluginBundleExtension.setTags(tags);
    }
  }
}
