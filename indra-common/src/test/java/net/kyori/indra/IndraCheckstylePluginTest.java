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
import net.kyori.indra.test.IndraConfigCacheFunctionalTest;
import net.kyori.indra.test.IndraTesting;
import net.kyori.mammoth.test.TestContext;
import org.gradle.api.Project;
import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.TaskOutcome;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

class IndraCheckstylePluginTest {
  private static final String PLUGIN = "net.kyori.indra.checkstyle";

  @Test
  void testPluginSimplyApplies() {
    final Project project = IndraTesting.project();
    project.getPluginManager().apply(PLUGIN);
  }

  @DisplayName("forcesCheckstyleVersion")
  @IndraConfigCacheFunctionalTest
  void testForcesCheckstyleVersion(final TestContext ctx) throws IOException {
    ctx.copyInput("build.gradle");

    assertDoesNotThrow(() -> ctx.build("resolveCheckstyle"), "Checkstyle did not match expected version");
  }

  @DisplayName("checkstyle")
  @IndraConfigCacheFunctionalTest
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
