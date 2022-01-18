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
package net.kyori.indra;

import java.io.IOException;
import net.kyori.mammoth.test.TestContext;
import org.gradle.api.Project;
import org.gradle.api.artifacts.component.ModuleComponentIdentifier;
import org.gradle.api.artifacts.result.ResolvedArtifactResult;
import org.gradle.api.internal.project.ProjectInternal;
import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.TaskOutcome;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

class IndraCheckstylePluginTest {
  private static final String PLUGIN = "net.kyori.indra.checkstyle";

  @Test
  void testPluginSimplyApplies() {
    final Project project = IndraTesting.project();
    project.getPluginManager().apply(PLUGIN);
  }

  @Test
  void testPluginForcesCheckstyleVersion() {
    final Project project = IndraTesting.project();
    project.getPlugins().apply(PLUGIN);

    final String configurationVersion = "8.45.1";
    final String extensionVersion = "9.2.1";
    project.getRepositories().mavenCentral();
    project.getDependencies().add("checkstyle", "com.puppycrawl.tools:checkstyle:" + configurationVersion);
    Indra.extension(project.getExtensions()).checkstyle(extensionVersion);

    ((ProjectInternal) project).evaluate(); // trigger afterEvaluate (if this stops working, we can move up to functional tests)

    for (final ResolvedArtifactResult artifact : project.getConfigurations().getByName("checkstyle").getIncoming().getArtifacts()) {
      final ModuleComponentIdentifier id = (ModuleComponentIdentifier) artifact.getId().getComponentIdentifier();
      if (id.getGroup().equals("com.puppycrawl.tools") && id.getModule().equals("checkstyle")) {
        assertEquals(extensionVersion, id.getVersion());
        return;
      }
    }

    fail("Checkstyle could not be found among resolved artifacts");
  }

  @DisplayName("checkstyle")
  @IndraFunctionalTest
  void testGoogleChecks(final TestContext ctx) throws IOException {
    ctx.copyInput("build.gradle");
    ctx.copyInput("settings.gradle");
    ctx.copyInput(".checkstyle/checkstyle.xml");
    ctx.copyInput("src/main/java/CheckstyleTest.java");

    final BuildResult result = ctx.build("checkstyleAll");
    assertEquals(TaskOutcome.SUCCESS, result.task(":checkstyleMain").getOutcome());

    ctx.copyInput("src/main/java/CheckstyleViolations.java");

    assertDoesNotThrow(() -> ctx.runner("checkstyleAll").buildAndFail());
  }
}
