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

import net.kyori.indra.git.IndraGitExtension;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.eclipse.jgit.lib.Ref;
import org.gradle.api.JavaVersion;
import org.gradle.api.Project;

public final class Versioning {
  public static int versionNumber(final @NonNull JavaVersion version) {
    return version.ordinal() + 1;
  }

  public static String versionString(final int version) {
    if(version <= 8) {
      return "1." + version;
    } else {
      return String.valueOf(version);
    }
  }

  public static String versionString(final @NonNull JavaVersion version) {
    if(version == JavaVersion.VERSION_1_9) {
      return "9";
    } else if(version == JavaVersion.VERSION_1_10) {
      return "10";
    } else {
      return version.toString();
    }
  }

  public static boolean isSnapshot(final @NonNull Project project) {
    return project.getVersion().toString().contains("-SNAPSHOT");
  }

  /**
   * Verify that this project is checked out to a release version.
   *
   * <p>This means that:</p>
   * <ul>
   * <li>The version does not contain SNAPSHOT</li>
   * <li>The project is managed within a Git repository</li>
   * <li>the current head commit is tagged</li>
   * </ul>
   *
   * @param project the project to check
   * @return if the project is recognized as a release
   */
  public static boolean isRelease(final @NonNull Project project) {
    final @Nullable IndraGitExtension git = project.getExtensions().findByType(IndraGitExtension.class);
    final @Nullable Ref tag = git == null ? null : git.headTag();
    return (tag != null || git == null) && !isSnapshot(project);
  }

  private Versioning() {
  }
}
