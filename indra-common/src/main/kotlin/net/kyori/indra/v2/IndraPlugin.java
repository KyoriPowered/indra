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

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import net.kyori.gradle.api.ProjectPlugin;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.gradle.api.Project;
import org.gradle.api.plugins.BasePluginConvention;
import org.gradle.api.plugins.Convention;
import org.gradle.api.plugins.ExtensionContainer;
import org.gradle.api.plugins.JavaLibraryPlugin;
import org.gradle.api.plugins.JavaPluginExtension;
import org.gradle.api.plugins.PluginContainer;
import org.gradle.api.tasks.TaskContainer;
import org.gradle.api.tasks.compile.CompileOptions;
import org.gradle.api.tasks.compile.JavaCompile;
import org.gradle.api.tasks.javadoc.Javadoc;
import org.gradle.api.tasks.testing.Test;
import org.gradle.external.javadoc.StandardJavadocDocletOptions;

public class IndraPlugin implements ProjectPlugin {
  @Override
  public void apply(final @NonNull Project project, final @NonNull PluginContainer plugins, final @NonNull ExtensionContainer extensions, final @NonNull Convention convention, final @NonNull TaskContainer tasks) {
    plugins.apply(JavaLibraryPlugin.class);

    convention.getPlugin(BasePluginConvention.class).setArchivesBaseName(project.getName().toLowerCase());

    tasks.withType(JavaCompile.class, task -> {
      final CompileOptions options = task.getOptions();
      options.setEncoding(StandardCharsets.UTF_8.name());
      options.getCompilerArgs().addAll(Arrays.asList(
        // Generate metadata for reflection on method parameters
        "-parameters",
        // Enable all warnings
        "-Xlint:all"
      ));
    });

    tasks.withType(Javadoc.class, task -> {
      task.options(options -> {
        options.setEncoding(StandardCharsets.UTF_8.name());

        if(options instanceof StandardJavadocDocletOptions) {
          ((StandardJavadocDocletOptions) options).charSet(StandardCharsets.UTF_8.name());
        }
      });
    });

    extensions.configure(JavaPluginExtension.class, extension -> {
      extension.withJavadocJar();
      extension.withSourcesJar();
    });

    tasks.withType(Test.class, Test::useJUnitPlatform);
  }
}
