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
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.jar.Manifest;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import net.kyori.indra.test.FunctionalTestDisplayNameGenerator;
import net.kyori.indra.test.IndraConfigCacheFunctionalTest;
import net.kyori.indra.test.IndraFunctionalTest;
import net.kyori.mammoth.test.TestContext;
import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.TaskOutcome;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Opcodes;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

@DisplayNameGeneration(FunctionalTestDisplayNameGenerator.class)
class IndraPluginFunctionalTest {

  @IndraConfigCacheFunctionalTest
  void testSimpleBuild(final TestContext ctx) throws IOException {
    ctx.copyInput("build.gradle");
    ctx.copyInput("settings.gradle");
    ctx.copyInput("src/main/java/pkg/Test.java");

    ctx.build("build"); // run build

    final Path builtJar = ctx.outputDirectory().resolve("build/libs/simplebuild-1.0.0-SNAPSHOT.jar");
    assertTrue(Files.exists(builtJar));
    assertBytecodeVersionEquals(builtJar, "pkg/Test.class", 52);

    // todo: add a source file, resource, etc with utf-8 characters and confirm they compile properly
  }

  @IndraConfigCacheFunctionalTest
  void testGroovy(final TestContext ctx) throws IOException {
    ctx.copyInput("build.gradle");
    ctx.copyInput("settings.gradle");
    ctx.copyInput("src/main/groovy/pkg/Test.groovy");

    final BuildResult result = ctx.build("build"); // run build
    System.out.println(result.getOutput());

    final Path builtJar = ctx.outputDirectory().resolve("build/libs/groovy-1.0.0-SNAPSHOT.jar");
    assertTrue(Files.exists(builtJar));
    assertBytecodeVersionEquals(builtJar, "pkg/Test.class", 52);
  }

  @IndraConfigCacheFunctionalTest
  void testScala(final TestContext ctx) throws IOException {
    ctx.copyInput("build.gradle");
    ctx.copyInput("settings.gradle");
    ctx.copyInput("src/main/scala/pkg/Main.scala");

    ctx.build("build"); // run build

    final Path builtJar = ctx.outputDirectory().resolve("build/libs/scala-1.0.0-SNAPSHOT.jar");
    assertTrue(Files.exists(builtJar));
    assertBytecodeVersionEquals(builtJar, "pkg/Main.class", 52);
  }

  @IndraFunctionalTest
  void testKotlinBuild(final TestContext ctx) throws IOException {
    ctx.copyInput("build.gradle");
    ctx.copyInput("settings.gradle");
    ctx.copyInput("src/main/kotlin/pkg/Test.kt");

    ctx.build("build"); // run build

    final Path builtJar = ctx.outputDirectory().resolve("build/libs/kotlin-1.0.0-SNAPSHOT.jar");
    assertTrue(Files.exists(builtJar));
    assertBytecodeVersionEquals(builtJar, "pkg/Test.class", 52);
  }

  @IndraConfigCacheFunctionalTest
  void testKotlinBuildscript(final TestContext ctx) throws IOException {
    ctx.copyInput("build.gradle.kts");
    ctx.copyInput("settings.gradle.kts");

    ctx.build("build"); // run build

    assertTrue(Files.exists(ctx.outputDirectory().resolve("build/libs/kotlinbuildscript-1.0.0-SNAPSHOT.jar")));
  }

  @IndraConfigCacheFunctionalTest
  void testMultiprojectModular(final TestContext ctx) throws IOException {
    ctx.copyInput("settings.gradle");

    ctx.copyInput("subprojects/core/build.gradle");
    ctx.copyInput("subprojects/core/j8/testproject/core/InformationProvider.java", "subprojects/core/src/main/java/testproject/core/InformationProvider.java");
    ctx.copyInput("subprojects/core/j9/module-info.java", "subprojects/core/src/main/java9/module-info.java");

    ctx.copyInput("subprojects/module-consumer/build.gradle");
    ctx.copyInput("subprojects/module-consumer/ModuleConsumer.java", "subrojects/module-consumer/src/main/java/testproject/consumer/ModuleConsumer.java");
    ctx.copyInput("subprojects/module-consumer/module-info.java", "subrojects/module-consumer/src/main/java/module-info.java");

    ctx.copyInput("subprojects/multirelease-module-consumer/build.gradle");
    ctx.copyInput("subprojects/multirelease-module-consumer/j8/Main.java", "subprojects/multirelease-module-consumer/src/main/java/testproject/consumer/multirelease/Main.java");
    ctx.copyInput("subprojects/multirelease-module-consumer/j9/module-info.java", "subprojects/multirelease-module-consumer/src/main/java9/module-info.java");
    ctx.copyInput("subprojects/multirelease-module-consumer/j11/Main.java", "subprojects/multirelease-module-consumer/src/main/java11/testproject/consumer/multirelease/Main.java");

    ctx.copyInput("subprojects/non-modular-consumer/build.gradle");
    ctx.copyInput("subprojects/non-modular-consumer/Main.java", "subprojects/non-modular-consumer/src/main/java/testproject/consumer/nonmodular/Main.java");

    // The goal here is to test that the module paths are set up appropriately within the projects. We already validate multirelease jar building in another test.
    assertDoesNotThrow(() -> ctx.build("build"));
  }

  @IndraConfigCacheFunctionalTest
  void testMultirelease(final TestContext ctx) throws IOException {
    ctx.copyInput("build.gradle");
    ctx.copyInput("settings.gradle");

    ctx.copyInput("j8/pkg/Actor.java", "src/main/java/pkg/Actor.java");
    ctx.copyInput("j8/pkg/Main.java", "src/main/java/pkg/Main.java");
    ctx.copyInput("j9/pkg/Actor.java", "src/main/java9/pkg/Actor.java");
    ctx.copyInput("j17/pkg/Actor.java", "src/main/java17/pkg/Actor.java");

    final BuildResult result = ctx.build("jar");

    // First: the tasks ran
    Stream.of("compileJava", "compileJava9Java", "compileJava17Java").forEach(name -> {
      assertEquals(TaskOutcome.SUCCESS, result.task(":" + name).getOutcome());
    });

    // Second: The output jar exists and has the appropriate variants

    final Path jar = ctx.outputDirectory().resolve("build/libs/multirelease-1.0.0-SNAPSHOT.jar");
    assertTrue(Files.exists(jar));

    try (final ZipFile jf = new ZipFile(jar.toFile())) {
      try (final InputStream is = jf.getInputStream(jf.getEntry("META-INF/MANIFEST.MF"))) {
        final Manifest manifest = new Manifest(is);
        assertEquals("true", manifest.getMainAttributes().getValue("Multi-Release"), "Jar does not have Multi-Release attribute");
      }

      Stream.of("pkg/Actor.class", "META-INF/versions/9/pkg/Actor.class", "META-INF/versions/17/pkg/Actor.class").forEach(file -> {
        assertNotNull(jf.getEntry(file), () -> file + " was not found in the built archive");
      });
    }

    assertBytecodeVersionEquals(jar, "pkg/Actor.class", 52);
    assertBytecodeVersionEquals(jar, "META-INF/versions/9/pkg/Actor.class",53);
    assertBytecodeVersionEquals(jar, "META-INF/versions/17/pkg/Actor.class", 61);

    // TODO: test that multirelease tests work
  }

  private static void assertBytecodeVersionEquals(final Path jarPath, final String resource, final int bytecodeVersion) throws IOException {
    final VersionCollector collector = new VersionCollector();
    try (final ZipFile jar = new ZipFile(jarPath.toFile())) {
      final ZipEntry entry = jar.getEntry(resource);
      assertNotNull(entry, () -> "Could not find an entry " + resource + " in " + jarPath);

      try (final InputStream is = jar.getInputStream(entry)) {
        final ClassReader reader = new ClassReader(is);
        reader.accept(collector, ClassReader.SKIP_CODE);
      }
    }
    if (collector.version == -1) {
      fail("Did not get a bytecode version from " + resource);
    }
    assertEquals(bytecodeVersion, collector.version, () -> "Expected bytecode version to be " + bytecodeVersion);
  }

  static final class VersionCollector extends org.objectweb.asm.ClassVisitor {
    int version = -1;

    public VersionCollector() {
      super(Opcodes.ASM9);
    }

    @Override
    public void visit(final int version, final int access, final String name, final String signature, final String superName, final String[] interfaces) {
      this.version = version;
    }
  }
}
