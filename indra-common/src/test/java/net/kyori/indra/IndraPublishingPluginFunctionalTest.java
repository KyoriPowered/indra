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
package net.kyori.indra;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import net.kyori.indra.test.FunctionalTestDisplayNameGenerator;
import net.kyori.indra.test.IndraConfigCacheFunctionalTest;
import net.kyori.indra.test.SettingsFactory;
import net.kyori.mammoth.test.TestContext;
import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.TaskOutcome;
import org.junit.jupiter.api.DisplayNameGeneration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayNameGeneration(FunctionalTestDisplayNameGenerator.class)
public class IndraPublishingPluginFunctionalTest {

  @IndraConfigCacheFunctionalTest
  void testKeyAndPasswordSigning(final TestContext ctx) throws IOException, InterruptedException {
    final String publicKeyFileName = "Test_Organization_0xEB716BFC33B790AB_public.gpg";

    ctx.copyInput("build.gradle");
    ctx.copyInput("gradle.properties");
    ctx.copyInput("Test_Organization_0xEB716BFC33B790AB_SECRET.asc");
    ctx.copyInput(publicKeyFileName);
    ctx.copyInput("src/main/java/pkg/Test.java");

    SettingsFactory.createSettings(ctx, "keyAndPasswordSigning");

    final BuildResult result = ctx.build("signJar", "-PforceSign"); // Force sign snapshot

    assertEquals(TaskOutcome.SUCCESS, result.task(":signJar").getOutcome());
    assertSignatureOf("EB716BFC33B790AB",
      ctx.outputDirectory().resolve(publicKeyFileName),
      ctx.outputDirectory().resolve("build/libs/keyandpasswordsigning-1.0.0-SNAPSHOT.jar"));
  }

  @IndraConfigCacheFunctionalTest
  void testKeyIdAndKeyAndPasswordSigning(final TestContext ctx) throws IOException, InterruptedException {
    final String publicKeyFileName = "Test_Organization_0xEB716BFC33B790AB_public.gpg";

    ctx.copyInput("build.gradle");
    ctx.copyInput("gradle.properties");
    ctx.copyInput("Test_Organization_0xEB716BFC33B790AB_SECRET_SUBKEY_0x38D0BE1A808D8604_Sign.asc");
    ctx.copyInput(publicKeyFileName);
    ctx.copyInput("src/main/java/pkg/Test.java");

    SettingsFactory.createSettings(ctx, "keyIdAndPasswordSigning");

    final BuildResult result = ctx.build("signJar", "-PforceSign"); // Force sign snapshot

    assertEquals(TaskOutcome.SUCCESS, result.task(":signJar").getOutcome());
    assertSignatureOf("38D0BE1A808D8604",
      ctx.outputDirectory().resolve(publicKeyFileName),
      ctx.outputDirectory().resolve("build/libs/keyidandpasswordsigning-1.0.0-SNAPSHOT.jar"));
  }

  private static void assertSignatureOf(final String keyId, final Path keyRingFilePath, final Path artifactFilePath) throws IOException, InterruptedException {
    assertTrue(Files.exists(artifactFilePath));

    final Path signatureFilePath = artifactFilePath.getParent().resolve(artifactFilePath.getFileName() + ".asc");
    assertTrue(Files.exists(signatureFilePath));

    final ProcessBuilder gpgVerificationProcessBuilder = new ProcessBuilder("gpgv",
      "--keyring",
      keyRingFilePath.toString(),
      signatureFilePath.toString(),
      artifactFilePath.toString());
    gpgVerificationProcessBuilder.redirectErrorStream(true);
    Process gpgVerificationProcess = gpgVerificationProcessBuilder.start();
    gpgVerificationProcess.waitFor();

    final List<String> output;
    try (BufferedReader reader = new BufferedReader(new InputStreamReader(gpgVerificationProcess.getInputStream()))) {
      output = reader.lines().toList();
    }
    assertEquals(3, output.size());
    assertEquals("gpgv:                using EDDSA key " + keyId, output.get(1));
    assertEquals("gpgv: Good signature from \"Test Organization <organization@test.local>\"", output.get(2));
  }
}
