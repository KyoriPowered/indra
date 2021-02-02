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
package net.kyori.indra.v2;

import net.kyori.gradle.api.ProjectPlugin;
import net.kyori.indra.Indra;
import net.kyori.indra.api.model.ContinuousIntegration;
import net.kyori.indra.api.model.Issues;
import net.kyori.indra.api.model.License;
import net.kyori.indra.api.model.SourceCodeManagement;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.api.plugins.Convention;
import org.gradle.api.plugins.ExtensionContainer;
import org.gradle.api.plugins.PluginContainer;
import org.gradle.api.publish.PublishingExtension;
import org.gradle.api.publish.maven.MavenPublication;
import org.gradle.api.publish.maven.plugins.MavenPublishPlugin;
import org.gradle.api.tasks.TaskContainer;
import org.gradle.plugins.signing.SigningExtension;
import org.gradle.plugins.signing.SigningPlugin;

public abstract class AbstractIndraPublishingPlugin implements ProjectPlugin {
  @Override
  public void apply(final @NonNull Project project, final @NonNull PluginContainer plugins, final @NonNull ExtensionContainer extensions, final @NonNull Convention convention, final @NonNull TaskContainer tasks) {
    plugins.apply(MavenPublishPlugin.class);
    plugins.apply(SigningPlugin.class);

    final IndraExtension indra = Indra.extension(extensions);

    this.configurePublications(extensions.getByType(PublishingExtension.class), publication -> {
      publication.pom(pom -> {
        pom.getName().set(project.getName());
        pom.getDescription().set(project.provider(project::getDescription));
        pom.getUrl().set(indra.scm.map(SourceCodeManagement::url));

        pom.ciManagement(ci -> {
          ci.getSystem().set(indra.ci.map(ContinuousIntegration::system));
          ci.getUrl().set(indra.ci.map(ContinuousIntegration::url));
        });

        pom.issueManagement(issues -> {
          issues.getSystem().set(indra.issues.map(Issues::system));
          issues.getUrl().set(indra.issues.map(Issues::url));
        });

        pom.licenses(licenses -> {
          licenses.license(license -> {
            license.getName().set(indra.license.map(License::name));
            license.getUrl().set(indra.license.map(License::url));
          });
        });

        pom.scm(scm -> {
          scm.getConnection().set(indra.scm.map(SourceCodeManagement::connection));
          scm.getDeveloperConnection().set(indra.scm.map(SourceCodeManagement::developerConnection));
          scm.getUrl().set(indra.scm.map(SourceCodeManagement::url));
        });
      });
    });

    extensions.configure(SigningExtension.class, extension -> {
      extension.sign(extensions.getByType(PublishingExtension.class).getPublications());
      extension.useGpgCmd();
    });
  }

  protected abstract void configurePublications(final PublishingExtension extension, final Action<MavenPublication> action);
}
