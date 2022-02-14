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
package net.kyori.indra.crossdoc;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import org.gradle.api.DefaultTask;
import org.gradle.api.artifacts.component.ProjectComponentIdentifier;
import org.gradle.api.artifacts.result.ResolvedArtifactResult;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.provider.SetProperty;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputFiles;
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.Nested;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.TaskAction;
import org.gradle.api.tasks.javadoc.Javadoc;

/**
 * Generate an options file containing {@code -linkoffline} info for passing into {@link Javadoc} tasks
 *
 * @since 2.1.0
 */
public abstract class GenerateOfflineLinks extends DefaultTask {
  private static final String LINK_OFFLINE_OPTION = "-linkoffline";

  /**
   * The base URL for submodule links.
   *
   * @return the link root
   * @since 2.1.0
   */
  @Input
  public abstract Property<String> getLinkBaseUrl();

  /**
   * A provider that computes project URLs, relative to the link base URL.
   *
   * @return a provider for the relative project URL
   * @since 2.1.0
   */
  @Nested
  public abstract Property<ProjectDocumentationUrlProvider> getUrlProvider();

  // TEMP: workaround for gradle/gradle#19490
  /**
   * A collection of artifacts on the compile classpath, to generate links t.
   *
   * @return a property including linkable artifacts.
   */
  @InputFiles
  protected abstract ConfigurableFileCollection getLinkableArtifacts();

  @Internal
  protected abstract SetProperty<ResolvedArtifactResult> getTempLinkableArtifacts();

  /**
   * The output file that the generated arguments for the {@code javadoc} tool will be written to.
   *
   * @return the output file
   * @since 2.1.0
   */
  @OutputFile
  public abstract RegularFileProperty getOutputFile();

  @TaskAction
  public void generateArgumentFile() throws IOException {
    final File outputFile = this.getOutputFile().get().getAsFile();
    outputFile.getParentFile().mkdirs();
    try (final BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outputFile), StandardCharsets.UTF_8))) {
      for (final ResolvedArtifactResult it : this.getTempLinkableArtifacts().get()) {
        final File file = it.getFile();
        final ProjectComponentIdentifier identifier = (ProjectComponentIdentifier) it.getId().getComponentIdentifier();
        final String projectName = identifier.getProjectName();
        if (!file.isDirectory()) {
          this.getLogger().info("Failed to link to Javadoc in {} (for {}) because it was not a directory", file, projectName);
          continue;
        }

        String linkRoot = this.getLinkBaseUrl().get();
        if (!linkRoot.endsWith("/")) {
          linkRoot += "/";
        }

        // Write out the option
        writer.append(LINK_OFFLINE_OPTION)
          .append(' ')
          .append(linkRoot + this.getUrlProvider().get().createUrl(projectName, identifier.getProjectPath()))
          .append(' ')
          .append(file.getAbsolutePath());
        writer.newLine();
      }
    }
  }
}
