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

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import net.kyori.indra.test.FunctionalTestDisplayNameGenerator;
import net.kyori.indra.test.IndraConfigCacheFunctionalTest;
import net.kyori.mammoth.test.TestContext;
import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.TaskOutcome;
import org.junit.jupiter.api.DisplayNameGeneration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayNameGeneration(FunctionalTestDisplayNameGenerator.class)
class CrossdocPluginFunctionalTest {
  private static final String OFFLINE_LINKS_OUTPUT_LOCATION = "build/tmp/generateOfflineLinks-args.txt";

  @IndraConfigCacheFunctionalTest
  void testSimpleLayout(final TestContext ctx) throws IOException {
    ctx.copyInput("build.gradle");
    ctx.copyInput("settings.gradle");
    ctx.copyInput("producer/src/main/java/producer/Values.java");
    ctx.copyInput("consumer/build.gradle");
    ctx.copyInput("consumer/src/main/java/consumer/ValueHandler.java");
    final BuildResult result = ctx.build(":consumer:javadoc", "--info");
    assertEquals(TaskOutcome.SUCCESS, result.task(":consumer:generateOfflineLinks").getOutcome());

    final Path offlineLinksFile = ctx.outputDirectory().resolve("consumer").resolve(OFFLINE_LINKS_OUTPUT_LOCATION);
    assertTrue(Files.exists(offlineLinksFile));
    assertEquals(1, Files.readAllLines(offlineLinksFile, StandardCharsets.UTF_8).size());

    assertFalse(result.getOutput().contains("Failed to link to Javadoc"), () -> "Javadoc linking failures detected, output:\n" + result.getOutput());
  }

  // TODO: test copyJavadoc?

  @IndraConfigCacheFunctionalTest
  void testNonCrossdocProjects(final TestContext ctx) throws IOException {
    ctx.copyInput("build.gradle");
    ctx.copyInput("settings.gradle");
    ctx.copyInput("unapplied/src/main/java/producer/Values.java");
    ctx.copyInput("consumer/build.gradle");
    ctx.copyInput("consumer/src/main/java/consumer/ValueHandler.java");
    final BuildResult result = ctx.build(":consumer:javadoc", "--info");
    assertEquals(TaskOutcome.SUCCESS, result.task(":consumer:generateOfflineLinks").getOutcome());

    final Path offlineLinksFile = ctx.outputDirectory().resolve("consumer").resolve(OFFLINE_LINKS_OUTPUT_LOCATION);
    assertTrue(Files.exists(offlineLinksFile));
    assertEquals(0, Files.readAllLines(offlineLinksFile, StandardCharsets.UTF_8).size());

    assertTrue(result.getOutput().contains("Failed to link to Javadoc"), () -> "No javadoc linking failures detected, output:\n" + result.getOutput());
  }

  @IndraConfigCacheFunctionalTest
  void testBuildFailsWithoutJavadocJar(final TestContext ctx) throws IOException {
    ctx.copyInput("build.gradle");
    ctx.copyInput("settings.gradle");

    final BuildResult result = ctx.runner("help").buildAndFail();

    assertTrue(result.getOutput().contains("The indra crossdoc plugin requires javadoc and the javadocElements"), () -> "A failure documenting the requirement for a javadocElements configuration was not printed. Actual output was:\n" + result.getOutput());
  }
}
