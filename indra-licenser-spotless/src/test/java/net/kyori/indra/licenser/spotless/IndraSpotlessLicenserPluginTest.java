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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.stream.Collectors;
import net.kyori.indra.test.FunctionalTestDisplayNameGenerator;
import net.kyori.indra.test.IndraConfigCacheFunctionalTest;
import net.kyori.indra.test.IndraTesting;
import net.kyori.mammoth.test.TestContext;
import org.gradle.api.Project;
import org.gradle.testkit.runner.BuildResult;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayNameGeneration(FunctionalTestDisplayNameGenerator.class)
class IndraSpotlessLicenserPluginTest {
  private static final String ID = "net.kyori.indra.licenser.spotless";

  @Test
  @Disabled
  void testPluginSimplyApplies() {
    // TODO: ProjectBuilder does not register the appropriate service for spotless to be able to apply (build services :(((()
    final Project project = IndraTesting.project();
    assertDoesNotThrow(() -> project.getPlugins().apply(ID));
  }

  @IndraConfigCacheFunctionalTest
  void testApplication(final TestContext ctx) throws IOException {
    ctx.copyInput("build.gradle");
    ctx.copyInput("settings.gradle");

    ctx.build("help");
  }

  @IndraConfigCacheFunctionalTest
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

  @IndraConfigCacheFunctionalTest
  void testConfigCacheReused(final TestContext ctx) throws IOException {
    ctx.copyInput("build.gradle");
    ctx.copyInput("settings.gradle");
    ctx.copyInput("license_header.txt");
    ctx.copyInput("Test.java", "src/main/java/test/Test.java");

    // First run, creates new config cache
    ctx.build("spotlessApply");
    ctx.build("spotlessCheck");

    // Then on second run, reuses the existing cache
    assertConfigCacheRestored(ctx.build("spotlessCheck"));

  }

  @IndraConfigCacheFunctionalTest
  void testKotlin(final TestContext ctx) throws IOException {
    ctx.copyInput("build.gradle.kts");
    ctx.copyInput("settings.gradle.kts");
    ctx.copyInput("license_header.txt");
    ctx.copyInput("Test.kt", "src/main/kotlin/test/Test.kt");

    // Fails check
    ctx.runner("spotlessCheck").buildAndFail();

    // Then applies, and matches expectation
    ctx.runner("spotlessApply").build();
    ctx.assertOutputEquals("TestFormatted.kt", "src/main/kotlin/test/Test.kt");
  }

  @IndraConfigCacheFunctionalTest
  void testGroovy(final TestContext ctx) throws IOException {
    ctx.copyInput("build.gradle");
    ctx.copyInput("settings.gradle");
    ctx.copyInput("license_header.txt");
    ctx.copyInput("Test.groovy", "src/main/groovy/test/Test.groovy");

    // Fails check
    ctx.runner("spotlessCheck").buildAndFail();

    // Then applies, and matches expectation
    ctx.runner("spotlessApply").build();
    ctx.assertOutputEquals("TestFormatted.groovy", "src/main/groovy/test/Test.groovy");
  }

  @IndraConfigCacheFunctionalTest
  void testCustomFormat(final TestContext ctx) throws IOException {
    ctx.copyInput("build.gradle");
    ctx.copyInput("settings.gradle");
    ctx.copyInput("license_header.txt");
    ctx.copyInput("Test.java", "src/main/java/test/Test.java");
    ctx.copyInput("TestChangeFormat.java", "src/main/java/test/TestChangeFormat.java");

    // Fails check
    ctx.runner("spotlessCheck").buildAndFail();

    // Then applies, and matches expectation
    ctx.runner("spotlessApply").build();
    ctx.assertOutputEquals("TestFormatted.java", "src/main/java/test/Test.java");
    ctx.assertOutputEquals("TestChangedFormat.java", "src/main/java/test/TestChangeFormat.java");
  }

  @IndraConfigCacheFunctionalTest
  void testPerLanguageFormat(final TestContext ctx) throws IOException {
    // kotlin has dobule-slash, Java has slash-star
    ctx.copyInput("build.gradle.kts");
    ctx.copyInput("settings.gradle.kts");
    ctx.copyInput("license_header.txt");
    ctx.copyInput("Test.java", "src/main/java/test/Test.java");
    ctx.copyInput("TestKotlin.kt", "src/main/kotlin/test/TestKotlin.kt");

    // Fails check
    ctx.runner("spotlessCheck").buildAndFail();

    // Then applies, and matches expectation
    ctx.runner("spotlessApply").build();
    ctx.assertOutputEquals("TestFormatted.java", "src/main/java/test/Test.java");
    ctx.assertOutputEquals("TestKotlinFormatted.kt", "src/main/kotlin/test/TestKotlin.kt");
  }

  @IndraConfigCacheFunctionalTest
  void testNewLine(final TestContext ctx) throws IOException {
    ctx.copyInput("build.gradle");
    ctx.copyInput("settings.gradle");
    ctx.copyInput("license_header.txt");
    ctx.copyInput("Test.groovy", "src/main/groovy/test/Test.groovy");

    // Fails check
    ctx.runner("spotlessCheck").buildAndFail();

    // Then applies, and matches expectation
    ctx.runner("spotlessApply").build();
    ctx.assertOutputEquals("TestFormatted.groovy", "src/main/groovy/test/Test.groovy");
  }

  @IndraConfigCacheFunctionalTest
  void testNonAsciiCharacters(final TestContext ctx) throws IOException {
    ctx.copyInput("build.gradle");
    ctx.copyInput("settings.gradle");
    ctx.copyInput("license_header.txt");
    ctx.copyInput("Test.java", "src/main/java/test/Test.java");

    // Fails check
    ctx.runner("spotlessCheck").buildAndFail();

    // Then applies, and matches expectation
    ctx.runner("spotlessApply").build();

    final URL expectedTemplate = this.getClass().getResource("nonAsciiCharacters/out/TestFormatted.java");
    assertNotNull(expectedTemplate, "expectedTemplate should not be null");

    final String contents;
    try (final BufferedReader reader = new BufferedReader(new InputStreamReader(expectedTemplate.openStream(), StandardCharsets.UTF_8))) {
      contents = reader.lines()
        .collect(Collectors.joining("\n", "", "\n"))
        .replace("YEAR", String.valueOf(LocalDate.now().getYear()));
    }
    ctx.assertOutputEqualsLiteral("src/main/java/test/Test.java", contents);
  }

  private static BuildResult assertConfigCacheRestored(final BuildResult result) {
    assertTrue(result.getOutput().contains("Reusing configuration cache"));
    return result;
  }
}
