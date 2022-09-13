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

import java.io.IOException;
import net.kyori.indra.test.FunctionalTestDisplayNameGenerator;
import net.kyori.indra.test.IndraFunctionalTest;
import net.kyori.indra.test.IndraTesting;
import net.kyori.mammoth.test.TestContext;
import org.gradle.api.Project;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@DisplayNameGeneration(FunctionalTestDisplayNameGenerator.class)
class SpotlessLicenserPluginTest {
  private static final String ID = "net.kyori.indra.licenser.spotless";

  @Test
  @Disabled
  void testPluginSimplyApplies() {
    // TODO: ProjectBuilder does not register the appropriate service for spotless to be able to apply (build services :(((()
    final Project project = IndraTesting.project();
    assertDoesNotThrow(() -> project.getPlugins().apply(ID));
  }

  @IndraFunctionalTest
  void testApplication(final TestContext ctx) throws IOException {
    ctx.copyInput("build.gradle");
    ctx.copyInput("settings.gradle");

    ctx.build("help");
  }

  // Java

  @IndraFunctionalTest
  void testJava(final TestContext ctx) throws IOException {
    ctx.copyInput("build.gradle");
    ctx.copyInput("settings.gradle");
    ctx.copyInput("license_header.txt");
    ctx.copyInput("Test.java", "src/main/java/test/Test.java");

    // Fails check
    ctx.runner("spotlessCheck").buildAndFail();

    // Then applies, and matches expectation
    ctx.runner("spotlessApply").build();
    ctx.assertOutputEquals("TestFormatted.java", "src/main/java/test/Test.java");
  }

  // Kotlin

  // Groovy

  // Customize header format

  // Per-language header format

  // newLine

  // test non-ascii characters
}
