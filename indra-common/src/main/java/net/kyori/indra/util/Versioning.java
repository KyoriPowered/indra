/*
 * This file is part of indra, licensed under the MIT License.
 *
 * Copyright (c) 2020 KyoriPowered
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

import org.ajoberstar.grgit.Tag;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.gradle.api.JavaVersion;
import org.gradle.api.Project;

public final class Versioning {

  public static boolean isSnapshot(final Project project) {
    return project.getVersion().toString().contains("-SNAPSHOT");
  }

  public static int versionNumber(final JavaVersion version) {
    return version.ordinal() + 1;
  }

  public static String versionString(final int version) {
    if (version <= 8) {
      return "1." + version;
    } else {
      return String.valueOf(version);
    }
  }

  public static String versionString(final JavaVersion version) {
    if (version == JavaVersion.VERSION_1_9) {
      return "9";
    } else if (version == JavaVersion.VERSION_1_10) {
      return "10";
    } else {
      return version.toString();
    }
  }

  /**
   * Verify that this project is checked out to a release version, meaning that:
   *
   * <ul>
   * <li>The version does not contain SNAPSHOT</li>
   * <li>The project is managed within a Git repository</li>
   * <li>the current head commit is tagged</li>
   * </ul>
   */
  public static boolean isRelease(final Project project) {
    final @Nullable Tag tag = VersionControl.headTag(project);
    return (tag != null || VersionControl.grgit(project) == null) && !isSnapshot(project);
  }

  private Versioning() {
  }
}
