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
package net.kyori.indra;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import net.kyori.gradle.api.ProjectPlugin;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.gradle.api.Project;
import org.gradle.api.plugins.Convention;
import org.gradle.api.plugins.ExtensionContainer;
import org.gradle.api.plugins.PluginContainer;
import org.gradle.api.plugins.quality.Checkstyle;
import org.gradle.api.plugins.quality.CheckstyleExtension;
import org.gradle.api.plugins.quality.CheckstylePlugin;
import org.gradle.api.tasks.TaskContainer;
import org.gradle.language.base.plugins.LifecycleBasePlugin;

public class IndraCheckstylePlugin implements ProjectPlugin {
  public static final String CHECKSTYLE_ALL_TASK = "checkstyleAll";

  @Override
  public void apply(final @NonNull Project project, final @NonNull PluginContainer plugins, final @NonNull ExtensionContainer extensions, final @NonNull Convention convention, final @NonNull TaskContainer tasks) {
    plugins.apply(CheckstylePlugin.class);

    tasks.register(CHECKSTYLE_ALL_TASK, task -> {
      task.setGroup(LifecycleBasePlugin.VERIFICATION_GROUP);
      task.setDescription("Execute checkstyle checks for all source sets");
      task.dependsOn(tasks.withType(Checkstyle.class));
    });

    project.afterEvaluate(p -> {
      final IndraExtension indra = Indra.extension(p.getExtensions());
      p.getExtensions().configure(CheckstyleExtension.class, cs -> {
        cs.setToolVersion(indra.checkstyle().get());
        final File checkstyleDir = p.getRootProject().file(".checkstyle");
        cs.getConfigDirectory().set(checkstyleDir);

        final Map<String, Object> props = new HashMap<>();
        props.put("configDirectory", checkstyleDir);
        props.put("severity", "error");
        cs.setConfigProperties(props);
      });
    });
  }
}
