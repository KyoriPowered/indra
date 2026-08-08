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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.GradleRunner;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.gradle.testkit.runner.TaskOutcome.UP_TO_DATE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class IndraCentralPublishingPluginTest {
  // IndraPublishingPlugin currently copies coordinates from the root project, which is not compatible with project
  // isolation. Keep this scoping test separate from the compatibility test below until that behavior is removed.
  @Test
  void testConfiguresOnlyIndraPublishingSubproject(final @TempDir Path projectDirectory) throws IOException {
    Files.writeString(projectDirectory.resolve("settings.gradle"), """
      plugins {
        id 'net.kyori.indra.publishing.central'
      }
      rootProject.name = 'central-test'
      include 'sub', 'other'
      """);
    Files.writeString(projectDirectory.resolve("build.gradle"), """
      assert tasks.named('publishReleaseCentralPortalBundle').get().publishingType.get() == 'AUTOMATIC'
      """);
    Files.createDirectory(projectDirectory.resolve("sub"));
    Files.writeString(projectDirectory.resolve("sub/build.gradle"), """
      group = 'test'
      version = '1.0.0'
      description = 'test'
      apply plugin: 'net.kyori.indra.publishing'
      assert publishing.repositories.names.contains('centralPortalRelease')
      tasks.register('assertCentralPortalConfigured')
      """);
    Files.createDirectory(projectDirectory.resolve("other"));
    Files.writeString(projectDirectory.resolve("other/build.gradle"), """
      plugins {
        id 'maven-publish'
      }
      assert !publishing.repositories.names.contains('centralPortalRelease')
      tasks.register('assertCentralPortalNotConfigured')
      """);

    final BuildResult result = GradleRunner.create()
      .withProjectDir(projectDirectory.toFile())
      .withPluginClasspath()
      .withArguments(
        ":sub:assertCentralPortalConfigured",
        ":other:assertCentralPortalNotConfigured"
      )
      .build();

    assertEquals(UP_TO_DATE, result.task(":sub:assertCentralPortalConfigured").getOutcome());
    assertEquals(UP_TO_DATE, result.task(":other:assertCentralPortalNotConfigured").getOutcome());
  }

  @Test
  void testConfigurationCacheAndProjectIsolation(final @TempDir Path projectDirectory) throws IOException {
    Files.writeString(projectDirectory.resolve("settings.gradle"), """
      plugins {
        id 'net.kyori.indra.publishing.central'
      }
      rootProject.name = 'central-test'
      include 'other'
      """);
    Files.writeString(projectDirectory.resolve("build.gradle"), """
      group = 'test'
      version = '1.0.0'
      description = 'test'
      apply plugin: 'net.kyori.indra.publishing'
      assert publishing.repositories.names.contains('centralPortalRelease')
      assert tasks.named('publishReleaseCentralPortalBundle').get().publishingType.get() == 'AUTOMATIC'
      tasks.register('assertCentralPortalConfigured')
      """);
    Files.createDirectory(projectDirectory.resolve("other"));
    Files.writeString(projectDirectory.resolve("other/build.gradle"), """
      plugins {
        id 'maven-publish'
      }
      assert !publishing.repositories.names.contains('centralPortalRelease')
      tasks.register('assertCentralPortalNotConfigured')
      """);

    final GradleRunner runner = GradleRunner.create()
      .withProjectDir(projectDirectory.toFile())
      .withPluginClasspath()
      .withArguments(
        ":assertCentralPortalConfigured",
        ":other:assertCentralPortalNotConfigured",
        "--configuration-cache",
        "-Dorg.gradle.unsafe.isolated-projects=true"
      );
    final BuildResult result = runner.build();
    final BuildResult reused = runner.build();

    assertEquals(UP_TO_DATE, result.task(":assertCentralPortalConfigured").getOutcome());
    assertEquals(UP_TO_DATE, result.task(":other:assertCentralPortalNotConfigured").getOutcome());
    assertTrue(reused.getOutput().contains("Configuration cache entry reused."));
  }
}
