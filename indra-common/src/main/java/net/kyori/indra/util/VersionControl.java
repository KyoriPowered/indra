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

import org.ajoberstar.grgit.Commit;
import org.ajoberstar.grgit.Grgit;
import org.ajoberstar.grgit.Tag;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.gradle.api.Project;

public final class VersionControl {

  private VersionControl() {
  }

  /**
   * Access Grgit.
   *
   * @param project the project that may be managed under git
   * @return a possible {@link Grgit} instance
   */
  public static @Nullable Grgit grgit(final Project project) {
    return project.getExtensions().findByType(Grgit.class);
  }

  /**
   * Find a tag, if any, that corresponds with the current checked out commit.
   */
  public static @Nullable Tag headTag(final Project project) {
    final @Nullable Grgit grgit = grgit(project);
    if(grgit == null) return null;
    final Commit headCommit = grgit.head();
    for(final Tag tag : grgit.getTag().list()) {
      if(tag.getCommit().equals(headCommit)) {
        return tag;
      }
    }
    return null;
  }

}
