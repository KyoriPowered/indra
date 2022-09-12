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
package net.kyori.indra;

import java.util.HashSet;
import java.util.Set;
import net.kyori.mammoth.ProjectPlugin;
import org.cadixdev.gradle.licenser.LicenseExtension;
import org.cadixdev.gradle.licenser.Licenser;
import org.gradle.api.Project;
import org.gradle.api.plugins.ExtensionContainer;
import org.gradle.api.plugins.PluginContainer;
import org.gradle.api.tasks.TaskContainer;
import org.jetbrains.annotations.NotNull;

/**
 * A plugin to apply and configure the Cadix licenser plugin.
 *
 * @deprecated since 2.2.0, for replacement with <a href="https://github.com/diffplug/spotless">Spotless</a>
 * @since 1.0.0
 */
@Deprecated
public class IndraLicenseHeaderPlugin implements ProjectPlugin {
  // Copied from Indra since this plugin is just temporary anyways
  private static final Set<String> SOURCE_FILES = sourceFiles();

  private static Set<String> sourceFiles() {
    final Set<String> sourceFiles = new HashSet<>();
    sourceFiles.add( "**/*.groovy");
    sourceFiles.add( "**/*.java");
    sourceFiles.add( "**/*.kt");
    sourceFiles.add( "**/*.scala");
    return sourceFiles;
  }

  private static final String HEADER_FILE_NAME = "license_header.txt";

  @Override
  public void apply(final @NotNull Project project, final @NotNull PluginContainer plugins, final @NotNull ExtensionContainer extensions, final @NotNull TaskContainer tasks) {
    plugins.apply(Licenser.class);

    // Configure sensible defaults
    extensions.configure(LicenseExtension.class, extension -> {
      extension.header(project.getRootProject().file(HEADER_FILE_NAME));
      extension.include(SOURCE_FILES);
      extension.getNewLine().set(false);
    });
  }
}
