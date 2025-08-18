/*
 * This file is part of indra, licensed under the MIT License.
 *
 * Copyright (c) 2025 KyoriPowered
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
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.Ref;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A {@link MappedRepositoryValueSource} that obtains values from the current branch,
 * or {@code null} if the project is not in a git repository or is checked out to a detached {@code HEAD}.
 *
 * @param <V> the value type
 */
public abstract class QueryBranch<V> extends MappedRepositoryValueSource.Parameterless<Ref, V> {
  private static final Logger LOGGER = Logging.getLogger(QueryBranch.class);

  @Override
  protected @Nullable Ref getRawValue(final @NotNull Git repository) {
    try {
      final @Nullable Ref ref = repository.getRepository().exactRef(Constants.HEAD);
      if (ref == null || !ref.isSymbolic()) return null; // no HEAD, or detached HEAD

      return ref.getTarget();
    } catch(final IOException ex) {
      LOGGER.error("Failed to query current branch name from git:", ex);
      return null;
    }
  }

  /**
   * Queries the {@link Ref#getName() name} of the current branch ref.
   */
  public abstract static class Name extends QueryBranch<String> {
    @Override
    protected @NotNull String mapValue(final @NotNull Git git, final @NotNull Ref ref) {
      return ref.getName();
    }
  }
}
