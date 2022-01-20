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
package net.kyori.indra.util;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;
import net.kyori.indra.git.GitPlugin;
import net.kyori.indra.test.IndraTesting;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.gradle.api.JavaVersion;
import org.gradle.api.Project;
import org.junit.jupiter.api.DynamicNode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

class VersioningTest {
  @Test
  void testVersionNumber() {
    assertEquals(8, Versioning.versionNumber(JavaVersion.VERSION_1_8));
    assertEquals(9, Versioning.versionNumber(JavaVersion.VERSION_1_9));
    assertEquals(10, Versioning.versionNumber(JavaVersion.VERSION_1_10));
    assertEquals(16, Versioning.versionNumber(JavaVersion.VERSION_16));
  }

  @Test
  void testVersionString() {
    assertEquals("1.8", Versioning.versionString(8));
    assertEquals("9", Versioning.versionString(9));
    assertEquals("16", Versioning.versionString(16));
  }

  @Test
  void testVersionStringFromJavaVersion() {
    assertEquals("1.8", Versioning.versionString(JavaVersion.VERSION_1_8));
    assertEquals("9", Versioning.versionString(JavaVersion.VERSION_1_9));
    assertEquals("10", Versioning.versionString(JavaVersion.VERSION_1_10));
    assertEquals("16", Versioning.versionString(JavaVersion.VERSION_16));
  }

  @ParameterizedTest
  @ValueSource(strings = {
    "1.0.0-SNAPSHOT",
    "1.0.0-KITTENS-SNAPSHOT"
  })
  void testIsSnapshotMatches(final String version) {
    final Project project = IndraTesting.project();
    project.setVersion(version);
    assertTrue(Versioning.isSnapshot(project));
  }

  @ParameterizedTest
  @ValueSource(strings = {
    "1.0.0",
    "1.0.0-KITTENS"
  })
  void testIsSnapshotDoesNotMatch(final String version) {
    final Project project = IndraTesting.project();
    project.setVersion(version);
    assertFalse(Versioning.isSnapshot(project));
  }

  @ParameterizedTest
  @ValueSource(strings = {
    "1.0.0-SNAPSHOT",
    "1.0.0-KITTENS-SNAPSHOT"
  })
  void testIsReleaseDoesNotMatch(final String version) {
    final Project project = IndraTesting.project();
    project.setVersion(version);
    assertFalse(Versioning.isRelease(project));
  }

  @TestFactory
  Stream<DynamicNode> testIsReleaseMatches(@TempDir final Path tempDir) {
    return Stream.of(
      "1.0.0",
      "1.0.0-KITTENS",
      "3",
      "stable"
    ).flatMap(version -> Stream.of(
      dynamicTest(version + " - matches, no repo", () -> {
        final Project project = IndraTesting.project();
        project.getPlugins().apply(GitPlugin.class);
        project.setVersion(version);
        assertTrue(Versioning.isRelease(project));
      }),
      dynamicTest(version + " - matches, on tag (non-annotated)", () -> {
        final Path testPath = tempDir.resolve(version + "-ontag");
        Files.createDirectories(testPath);
        final Git repo = initRepoWithCommit(testPath);

        repo.tag()
        .setName("v" + version)
        .call();

        final Project project = IndraTesting.project(b -> b.withProjectDir(testPath.toFile()));
        project.getPlugins().apply(GitPlugin.class);
        project.setVersion(version);
        assertTrue(Versioning.isRelease(project));
      }),
      dynamicTest(version + " - matches, on tag (annotated)", () -> {
        final Path testPath = tempDir.resolve(version + "-onannotatedtag");
        Files.createDirectories(testPath);
        final Git repo = initRepoWithCommit(testPath);

        repo.tag()
        .setName("v" + version)
        .setAnnotated(true)
        .setMessage("Release " + version)
        .call();

        final Project project = IndraTesting.project(b -> b.withProjectDir(testPath.toFile()));
        project.getPlugins().apply(GitPlugin.class);
        project.setVersion(version);
        assertTrue(Versioning.isRelease(project));
      }),
      dynamicTest(version + " - does not match, with repo no tag", () -> {
        final Path testPath = tempDir.resolve(version + "-untagged");
        Files.createDirectories(testPath);
        initRepoWithCommit(testPath);

        final Project project = IndraTesting.project(b -> b.withProjectDir(testPath.toFile()));
        project.getPlugins().apply(GitPlugin.class);
        project.setVersion(version);
        assertFalse(Versioning.isRelease(project));
      })
    ));
  }

  private static Git initRepoWithCommit(final Path repoDir) throws IOException, GitAPIException {
    Files.createDirectories(repoDir);
    final Git repo = Git.init()
      .setDirectory(repoDir.toFile())
      .setInitialBranch("trunk")
      .call();

    Files.write(repoDir.resolve("gradle.properties"), "filler=test".getBytes(StandardCharsets.UTF_8));
    repo.commit()
    .setAuthor("CI", "noreply@kyori.net")
    .setCommitter("CI", "noreply@kyori.net")
    .setAll(true)
    .setMessage("Initial commit")
    .call();

    return repo;
  }

  // release matches non-git checkout content


}
