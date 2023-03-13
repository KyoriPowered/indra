/*
 * This file is part of indra, licensed under the MIT License.
 *
 * Copyright (c) 2020-2023 KyoriPowered
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
package net.kyori.indra.jarsigner;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import net.kyori.indra.test.FunctionalTestDisplayNameGenerator;
import net.kyori.indra.test.IndraConfigCacheFunctionalTest;
import net.kyori.indra.test.SettingsFactory;
import net.kyori.mammoth.test.TestContext;
import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.BuildTask;
import org.gradle.testkit.runner.TaskOutcome;
import org.junit.jupiter.api.DisplayNameGeneration;

import static net.kyori.indra.test.IndraTesting.exec;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@DisplayNameGeneration(FunctionalTestDisplayNameGenerator.class)
class JarSignerPluginTest {

  @IndraConfigCacheFunctionalTest
  void testPluginApplication(final TestContext ctx) throws IOException {
    // tada!
    ctx.copyInput("build.gradle");
    ctx.copyInput("Test.java", "src/main/java/test/Test.java");
    SettingsFactory.createSettings(ctx, "pluginApplication");

    assertDoesNotThrow(() -> ctx.build("build"));
  }

  private static String jdkToolPath(final String toolName) {
    return Paths.get(System.getProperty("java.home")).resolve("bin").resolve(toolName).toString();
  }

  @IndraConfigCacheFunctionalTest
  void testSignOutput(final TestContext ctx) throws IOException {
    // Generate a transient signing key to make sure everything works
    // There is

    final String keyPass = "test123";
    final Path keyStore = ctx.outputDirectory().resolve("test.pkcs12");
    exec(ctx.outputDirectory(), jdkToolPath("keytool"), "-genkeypair",
      "-alias", "test",
      "-dname", "CN=example.org, o=ExamplesRUs",
      "-keystore", keyStore.toString(),
      "-validity", "90",
      "-keypass", keyPass,
      "-storepass", keyPass,
      "-keyalg", "RSA",
      "-keysize", "4096",
      "-storetype", "PKCS12"
    ); // create signing key

    ctx.copyInput("build.gradle");
    ctx.copyInput("Test.java", "src/main/java/test/Test.java");
    SettingsFactory.createSettings(ctx, "signOutput");

    final BuildResult result = ctx.build(
      "--info",
      "-PsigningKey=" + keyStore.toAbsolutePath(),
      "-PsigningPassword=" + keyPass,
      "build",
      "jarsignJar"
    );

    System.out.println(result.getOutput());

    assertThat(result.task(":jarsignJar"))
      .describedAs("task :jarsignJar")
      .isNotNull()
      .extracting(BuildTask::getOutcome)
        .isEqualTo(TaskOutcome.SUCCESS); // ensure task actually executed

    assertThatNoException()
      .isThrownBy(() -> exec(ctx.outputDirectory(), jdkToolPath("jarsigner"), "-verify", "build/libs/signOutput.jar"));
  }
}
