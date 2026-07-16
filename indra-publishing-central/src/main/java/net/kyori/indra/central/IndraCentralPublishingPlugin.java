/*
 * This file is part of indra, licensed under the MIT License.
 *
 * Copyright (c) 2026 KyoriPowered
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
package net.kyori.indra.central;

import dev.lukebemish.centralportalpublishing.CentralPortalProjectExtension;
import dev.lukebemish.centralportalpublishing.CentralPortalPublishingPlugin;
import dev.lukebemish.centralportalpublishing.CentralPortalRepositoryHandlerExtension;
import org.gradle.api.IsolatedAction;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.initialization.Settings;
import org.gradle.api.plugins.ExtensionAware;
import org.gradle.api.plugins.ExtensionContainer;
import org.gradle.api.publish.PublishingExtension;

/**
 * A settings plugin for configuring publication to Maven Central through the Central Portal.
 *
 * <p>All projects applying the {@code net.kyori.indra.publishing} plugin automatically contribute their publications
 * to the {@code release} bundle defined on the root project.</p>
 */
public final class IndraCentralPublishingPlugin implements Plugin<Settings> {
  private static final String INDRA_PUBLISHING_PLUGIN_ID = "net.kyori.indra.publishing";
  private static final String BUNDLE_NAME = "release";
  private static final String ROOT_PROJECT_PATH = ":";

  @Override
  public void apply(final Settings settings) {
    settings.getGradle().getLifecycle().beforeProject(new ConfigureProject());
  }

  private static final class ConfigureProject implements IsolatedAction<Project> {
    private static final long serialVersionUID = 1L;

    @Override
    public void execute(final Project project) {
      if (ROOT_PROJECT_PATH.equals(project.getPath())) {
        project.getPluginManager().apply(CentralPortalPublishingPlugin.class);
        project.getExtensions().getByType(CentralPortalProjectExtension.class).bundle(BUNDLE_NAME, bundle -> {
          bundle.getUsername().convention(project.getProviders().gradleProperty("sonatypeUsername"));
          bundle.getPassword().convention(project.getProviders().gradleProperty("sonatypePassword"));
          bundle.getPublishingType().convention("AUTOMATIC");
        });
      }

      project.getPluginManager().withPlugin(INDRA_PUBLISHING_PLUGIN_ID, plugin -> {
        project.getPluginManager().apply(CentralPortalPublishingPlugin.class);
        final PublishingExtension publishing = project.getExtensions().getByType(PublishingExtension.class);
        final ExtensionContainer repositoryExtensions = ((ExtensionAware) publishing.getRepositories()).getExtensions();
        repositoryExtensions.getByType(CentralPortalRepositoryHandlerExtension.class).portalBundle(ROOT_PROJECT_PATH, BUNDLE_NAME);
      });
    }
  }
}
