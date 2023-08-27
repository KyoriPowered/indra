/*
 * This file is part of indra, licensed under the MIT License.
 *
 * Copyright (c) 2023 KyoriPowered
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
package net.kyori.indra.crossdoc;

import org.gradle.api.Project;
import org.gradle.api.artifacts.component.ProjectComponentIdentifier;
import org.gradle.util.GradleVersion;
import org.jetbrains.annotations.NotNull;

import static java.util.Objects.requireNonNull;

// cross-compat: Gradle 8.3+ and <8.3
interface BuildTreeComparer {
  boolean HAS_GRADLE_8_3 = GradleVersion.current().getBaseVersion().compareTo(GradleVersion.version("8.3")) >= 0;

  static @NotNull BuildTreeComparer comparer(final @NotNull Project project) {
    if (HAS_GRADLE_8_3) {
      return new Gradle83Comparer(requireNonNull(project, "project"));
    } else {
      return Gradle82OrOlderComparer.INSTANCE;
    }
  }

  boolean isCurrentBuild(final ProjectComponentIdentifier identifier);

  final class Gradle83Comparer implements BuildTreeComparer {
    private final String projectBuildPath;

    Gradle83Comparer(final Project project) {
      final String projectPath = project.getPath();
      final String absoluteProjectPath = project.getBuildTreePath();
      final String buildPath = absoluteProjectPath.substring(0, absoluteProjectPath.length() - projectPath.length());
      this.projectBuildPath = buildPath.isEmpty() ? ":" : buildPath; // root build needs to still be made absolute
    }

    @Override
    public boolean isCurrentBuild(ProjectComponentIdentifier identifier) {
      return this.projectBuildPath.equals(identifier.getBuild().getBuildPath());
    }
  }

  final class Gradle82OrOlderComparer implements BuildTreeComparer {
    static final Gradle82OrOlderComparer INSTANCE = new Gradle82OrOlderComparer();

    private Gradle82OrOlderComparer() {
    }

    @Override
    @SuppressWarnings("deprecation")
    public boolean isCurrentBuild(ProjectComponentIdentifier identifier) {
      return identifier.getBuild().isCurrentBuild();
    }
  }
}
