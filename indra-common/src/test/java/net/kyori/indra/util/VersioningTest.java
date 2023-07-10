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

  @ParameterizedTest
  @ValueSource(strings = {
    "1.0.0",
    "1.0.0-KITTENS",
    "3",
    "stable"
  })
  void testIsReleaseDoesMatch(final String version) {
    final Project project = IndraTesting.project();
    project.setVersion(version);
    assertTrue(Versioning.isRelease(project));
  }

}
