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
package net.kyori.indra.licenser.spotless;

import com.diffplug.gradle.spotless.FormatExtension;
import com.diffplug.gradle.spotless.SpotlessExtension;
import com.diffplug.spotless.generic.LicenseHeaderStep;
import com.diffplug.spotless.kotlin.KotlinConstants;
import net.kyori.indra.licenser.spotless.internal.SpotlessLicenserExtensionImpl;
import net.kyori.mammoth.ProjectPlugin;
import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.api.plugins.ExtensionContainer;
import org.gradle.api.plugins.PluginContainer;
import org.gradle.api.tasks.TaskContainer;
import org.jetbrains.annotations.NotNull;

/**
 * A plugin to provide user-friendly configuration for the Spotless license header steps.
 *
 * @since 2.2.0
 */
public class SpotlessLicenserPlugin implements ProjectPlugin {
  private static final String JAVA_LICENSE_HEADER_DELIMITER = "package ";

  private static final String HEADER_FILE_NAME = "license_header.txt";

  @Override
  public void apply(
    final @NotNull Project project,
    final @NotNull PluginContainer plugins,
    final @NotNull ExtensionContainer extensions,
    final @NotNull TaskContainer tasks
  ) {
    // Register our own extension
    final SpotlessLicenserExtensionImpl extension = (SpotlessLicenserExtensionImpl) extensions.create(SpotlessLicenserExtension.class, "indraSpotlessLicenser", SpotlessLicenserExtensionImpl.class);

    // Default licenser configuration
    extension.licenseHeaderFile().convention(project.getResources().getText().fromFile(project.getRootProject().file(HEADER_FILE_NAME), "UTF-8"));

    // Apply spotless
    plugins.apply("com.diffplug.spotless");
    final SpotlessExtension spotless = extensions.getByType(SpotlessExtension.class);

    // Apply license header config to individual languages
    plugins.withId("java", $ -> {
      spotless.java(java -> {
        addStep(project, java, extension, "java", JAVA_LICENSE_HEADER_DELIMITER);
      });
    });

    plugins.withId("org.jetbrains.kotlin.jvm", $ -> {
      spotless.kotlin(kotlin -> {
        addStep(project, kotlin, extension, "kotlin", KotlinConstants.LICENSE_HEADER_DELIMITER);
      });
    });

    plugins.withId("groovy", $ -> {
      spotless.groovy(groovy -> {
        addStep(project, groovy, extension, "groovy", JAVA_LICENSE_HEADER_DELIMITER);
      });
    });

    // TODO: scala -- scala doesn't support its own delimiter
  }

  private static void addStep(final Project project, final FormatExtension format, final SpotlessLicenserExtensionImpl indraExtension, final String name, final String delimiter) {
    final LicenseHeaderStep step = LicenseHeaderStep.headerDelimiter(indraExtension.createHeaderSupplier(name), "");
    format.addStep(step.withYearMode(LicenseHeaderStep.YearMode.PRESERVE).build()); // add with dummy settings
    final FormatExtension.LicenseHeaderConfig config = format.new LicenseHeaderConfig(step);
    config.delimiter(delimiter); // replace the step with a properly configured one

    // Then apply any extra steps after evaluation
    project.afterEvaluate(p -> {
      for (final Action<FormatExtension.LicenseHeaderConfig> configStep : indraExtension.extraConfigSteps()) {
        configStep.execute(config);
      }
    });
  }
}
