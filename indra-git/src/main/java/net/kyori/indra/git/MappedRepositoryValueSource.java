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

import org.eclipse.jgit.api.Git;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A {@link RepositoryValueSource} that maps an intermediary value type to a final value type.
 *
 * @param <I> the intermediary (non-serializable/config-cacheable) value type
 * @param <V> the mapped value type
 * @param <P> the parameters type
 * @since 4.0.0
 */
public abstract class MappedRepositoryValueSource<I, V, P extends RepositoryValueSource.Parameters> extends RepositoryValueSource<V, P> {
  @Override
  protected final @Nullable V obtain(final @NotNull Git repository) {
    final I rawValue = this.getRawValue(repository);
    if (rawValue == null) {
      return null;
    }
    return this.mapValue(repository, rawValue);
  }

  /**
   * Obtains the raw value from the repository.
   *
   * @param repository the git repository
   * @return the raw value, or {@code null}
   */
  protected abstract @Nullable I getRawValue(final @NotNull Git repository);

  /**
   * Maps the raw value to the final value type.
   *
   * @param git the git repository
   * @param value the raw value
   * @return the mapped value, or {@code null}
   */
  protected abstract @Nullable V mapValue(final @NotNull Git git, final @NotNull I value);

  /**
   * A {@link MappedRepositoryValueSource} that does not require any additional parameters.
   *
   * @param <I> the intermediary (non-serializable/config-cacheable) value type
   * @param <V> the mapped value type
   */
  public abstract static class Parameterless<I, V> extends MappedRepositoryValueSource<I, V, Parameters> {
  }
}
