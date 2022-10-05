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

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import net.kyori.mammoth.ProjectPlugin;
import org.gradle.api.Project;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.plugins.ExtensionContainer;
import org.gradle.api.plugins.PluginContainer;
import org.gradle.api.plugins.quality.Checkstyle;
import org.gradle.api.plugins.quality.CheckstyleExtension;
import org.gradle.api.plugins.quality.CheckstylePlugin;
import org.gradle.api.tasks.TaskContainer;
import org.gradle.language.base.plugins.LifecycleBasePlugin;
import org.gradle.util.GradleVersion;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Configure Gradle's built-in {@code checkstyle} plugin.
 *
 * @since 2.0.0
 */
public class IndraCheckstylePlugin implements ProjectPlugin {
  public static final String CHECKSTYLE_ALL_TASK = "checkstyleAll";

  private static final String CHECKSTYLE_CONFIGURATION = "checkstyle";

  @Override
  public @Nullable GradleVersion minimumGradleVersion() {
    return Indra.MINIMUM_SUPPORTED;
  }

  @Override
  public void apply(final @NotNull Project project, final @NotNull PluginContainer plugins, final @NotNull ExtensionContainer extensions, final @NotNull TaskContainer tasks) {
    plugins.apply(CheckstylePlugin.class);

    tasks.register(CHECKSTYLE_ALL_TASK, task -> {
      task.setGroup(LifecycleBasePlugin.VERIFICATION_GROUP);
      task.setDescription("Execute checkstyle checks for all source sets");
      task.dependsOn(tasks.withType(Checkstyle.class));
    });

    // Remove task dependencies from checkstyle tasks
    final ObjectFactory objects = project.getObjects();
    tasks.withType(Checkstyle.class).configureEach(check -> check.setClasspath(objects.fileCollection()));

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

        // Add a dependency constraint to ensure we always actually use the requested version of checkstyle
        p.getDependencies().getConstraints().add(CHECKSTYLE_CONFIGURATION, "com.puppycrawl.tools:checkstyle", c -> {
          c.version(v -> v.require(indra.checkstyle().get()));
        });
      });
    });
  }
}
