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
package net.kyori.indra.git;

import java.io.IOException;
import net.kyori.indra.test.FunctionalTestDisplayNameGenerator;
import net.kyori.indra.test.IndraFunctionalTest;
import net.kyori.mammoth.test.TestContext;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.gradle.testkit.runner.BuildResult;
import org.junit.jupiter.api.DisplayNameGeneration;

import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayNameGeneration(FunctionalTestDisplayNameGenerator.class)
class IndraGitPluginFunctionalTest {

  // Just test the interactions with Gradle API here, really
  @IndraFunctionalTest
  void testGitApplication(final TestContext ctx) throws IOException, GitAPIException {
    IndraGitPluginTest.initRepo(ctx.outputDirectory());
    ctx.copyInput("build.gradle");
    ctx.copyInput("settings.gradle");

    final BuildResult result = ctx.build("printGitStatus");

    assertTrue(result.getOutput().contains("Git present: true"), "Plugin application not detected");

  }
}
