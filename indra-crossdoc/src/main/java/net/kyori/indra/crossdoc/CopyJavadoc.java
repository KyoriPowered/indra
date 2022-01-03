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
package net.kyori.indra.crossdoc;

import java.io.File;
import javax.inject.Inject;
import org.gradle.api.DefaultTask;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.file.FileSystemOperations;
import org.gradle.api.provider.Property;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputFiles;
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.Nested;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.TaskAction;
import org.gradle.api.tasks.options.Option;

/**
 * Copy project javadoc into the `adventure-javadoc` directory tree
 *
 * @since 2.1.0
 */
public abstract class CopyJavadoc extends DefaultTask {
  @InputFiles
  public abstract ConfigurableFileCollection getJavadocFiles();

  @Internal
  @Option(option = "output", description = "The root of the destination for Javadoc publishing")
  public abstract Property<String> getOutputPath();

  @Nested
  public abstract Property<ProjectDocumentationUrlProvider> getDocumentationUrlProvider();

  @OutputDirectory
  public abstract DirectoryProperty getOutputDirectory();

  // Non user-modifiable properties
  @Internal
  protected abstract DirectoryProperty getRootDir();

  @Input
  protected abstract Property<String> getProjectName();

  @Input
  protected abstract Property<String> getProjectPath();

  @Inject
  protected abstract FileSystemOperations getFileSystemOps();

  public CopyJavadoc() {
    // relative to project root, <output>/<projectName>/<projectVersion>
    final Provider<String> relativeUrl = this.getProjectName()
      .zip(this.getProjectPath(), (name, path) -> new Pair<>(name, path))
      .zip(this.getDocumentationUrlProvider(), (namePath, provider) -> provider.createUrl(namePath.left, namePath.right));
    this.getOutputDirectory().set(this.getRootDir().dir(this.getOutputPath()).zip(relativeUrl, (base, path) -> base.dir(path)));
  }

  @TaskAction
  public void doTransfer() {
    final File dest = this.getOutputDirectory().get().getAsFile();

    this.getFileSystemOps().delete(spec -> spec.delete(dest));
    dest.mkdirs();

    this.getFileSystemOps().copy(spec -> {
      spec.from(this.getJavadocFiles());
      spec.into(dest);
    });
  }

  static final class Pair<A, B> {
    final A left;
    final B right;

    Pair(final A left, final B right) {
      this.left = left;
      this.right = right;
    }
  }
}
