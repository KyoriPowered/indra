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
package net.kyori.indra.gradle;

import com.gradle.publish.PluginBundleExtension;
import com.gradle.publish.PublishPlugin;
import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;
import net.kyori.indra.Indra;
import net.kyori.indra.IndraExtension;
import net.kyori.indra.IndraPlugin;
import net.kyori.indra.api.model.SourceCodeManagement;
import net.kyori.indra.internal.AbstractIndraPublishingPlugin;
import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.api.publish.PublishingExtension;
import org.gradle.api.publish.maven.MavenPublication;
import org.gradle.plugin.devel.GradlePluginDevelopmentExtension;
import org.gradle.plugin.devel.plugins.JavaGradlePluginPlugin;
import org.gradle.util.GradleVersion;
import org.jetbrains.annotations.Nullable;

public class GradlePluginPublishingPlugin extends AbstractIndraPublishingPlugin {

  static final boolean HAS_GRADLE_7_6 = GradleVersion.current().compareTo(GradleVersion.version("7.6")) >= 0;
  private static final String EXTENSION_NAME = "indraPluginPublishing";

  @Override
  protected void extraApplySteps(final Project project) {
    // TODO: do we want to apply these plugins ourselves instead of only acting when the user chooses to do so?
    project.getPlugins().withType(PublishPlugin.class, $ -> {
      final @Nullable PluginBundleExtension pluginBundleExtension = project.getExtensions().findByType(PluginBundleExtension.class);

      // Needed to publish plugins using GH actions secrets, which can only be specified in environment variables.
      // Unfortunately, the plugin we are forced to use to publish to the plugin portal does not
      // support customizing these properties, so instead we just have to copy from original properties
      // to the ones that plugin expects.
      final BiConsumer<String, String> copyProperty = (definedProperty, originalProperty) -> {
        final Object property = project.findProperty(definedProperty);
        if(property != null) {
          project.getExtensions().getExtraProperties().set(originalProperty, property);
        }
      };
      copyProperty.accept("pluginPortalApiKey", "gradle.publish.key");
      copyProperty.accept("pluginPortalApiSecret", "gradle.publish.secret");

      project.getPlugins().withType(JavaGradlePluginPlugin.class, $$ -> {
        final GradlePluginDevelopmentExtension pluginDevelopment = project.getExtensions().getByType(GradlePluginDevelopmentExtension.class);
        // When we have both plugins, we can create an extension
        final IndraPluginPublishingExtensionImpl extension = (IndraPluginPublishingExtensionImpl) project.getExtensions().create(
          IndraPluginPublishingExtension.class,
          EXTENSION_NAME,
          IndraPluginPublishingExtensionImpl.class,
          pluginDevelopment,
          pluginBundleExtension
        );

        extension.pluginIdBase().convention(project.provider(() -> (String) project.getGroup()));
        extension.fallbackDescription.set(project.provider(project::getDescription));

        if (HAS_GRADLE_7_6) {
          this.applyExtensionDataModern(project, extension, pluginDevelopment);
        } else {
          this.applyExtensionDataPreGradle76(project, extension, pluginBundleExtension);
        }
      });
    });

    project.getPlugins().withType(IndraPlugin.class, $ -> {
      Indra.extension(project.getExtensions()).includeJavaSoftwareComponentInPublications().set(false);
    });
  }

  private void applyExtensionDataModern(final Project project, final IndraPluginPublishingExtension indra, final GradlePluginDevelopmentExtension extension) {
    // Set VCS if present
    final IndraExtension indraExtension = Indra.extension(project.getExtensions());
    extension.getVcsUrl().set(indraExtension.scm().map(SourceCodeManagement::url));

    // Set website if present
    extension.getWebsite().set(indra.website());

    // Tags and description are only set per-plugin, handled within the extension
  }

  private void applyExtensionDataPreGradle76(final Project project, final IndraPluginPublishingExtension indra, final PluginBundleExtension extension) {
    // Set tags if present
    project.afterEvaluate(p -> {
      if (indra.bundleTags().isPresent()) {
        final List<String> tags = indra.bundleTags().get();
        if(!tags.isEmpty()) {
          extension.setTags(tags);
        }
      }
      // Set website if present
      if (indra.website().isPresent()) {
        extension.setWebsite(indra.website().get());
      }

      // Inherit properties from plugin and project
      final IndraExtension indraExtension = Indra.extension(p.getExtensions());
      if (indraExtension.scm().isPresent() && extension.getVcsUrl() == null) {
        extension.setVcsUrl(indraExtension.scm().get().url());
      }

      if (p.getDescription() != null && extension.getDescription() == null) {
        extension.setDescription(p.getDescription());
      }
    });
  }

  @Override
  protected void applyPublishingActions(final PublishingExtension extension, final Set<Action<MavenPublication>> actions) {
    extension.getPublications().withType(MavenPublication.class).configureEach(publication -> {
      for (final Action<MavenPublication> action : actions) {
        action.execute(publication);
      }
    });
  }

  @Override
  protected void configurePublications(final PublishingExtension extension, final Action<MavenPublication> action) {
    extension.getPublications().withType(MavenPublication.class).configureEach(action);
  }
}
