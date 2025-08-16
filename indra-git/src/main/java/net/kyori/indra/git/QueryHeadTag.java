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

import net.kyori.indra.git.internal.IndraGitExtensionImpl;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.Ref;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A {@link MappedRepositoryValueSource} that obtains values from the tag pointing to the commit checked out as {@code HEAD},
 * or {@code null} if the project is not in a git repository or is not checked out to a tag
 *
 * @param <V> value type
 */
public abstract class QueryHeadTag<V> extends MappedRepositoryValueSource.Parameterless<Ref, V> {
  @Override
  protected @Nullable Ref getRawValue(final @NotNull Git repository) {
    return IndraGitExtensionImpl.headTag(repository);
  }

  public abstract static class Name extends QueryHeadTag<String> {
    @Override
    protected @NotNull String mapValue(final @NotNull Git git, final @NotNull Ref ref) {
      return ref.getName();
    }
  }
}
