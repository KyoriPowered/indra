/*
 * This file is part of indra, licensed under the MIT License.
 *
 * Copyright (c) 2024 KyoriPowered
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

import javax.inject.Inject;
import net.kyori.indra.git.internal.GitCache;
import org.eclipse.jgit.api.Git;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.provider.ValueSource;
import org.gradle.api.provider.ValueSourceParameters;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * An implementation of {@link ValueSource} which queries the project's git repository for info.
 *
 * <p>This value source must be configured via {@link IndraGitExtension#repositoryValue(Class, org.gradle.api.Action)} to ensure it is populated with repository information.</p>
 *
 * <p>Implementations must be abstract, and only implement {@link #obtain(Git)}.</p>
 *
 * @param <V> the value type
 * @param <P> the parameter type
 * @since 4.0.0
 */
public abstract class RepositoryValueSource<V, P extends RepositoryValueSource.Parameters> implements ValueSource<V, P> {
  public interface Parameters extends ValueSourceParameters {
    @ApiStatus.Internal
    DirectoryProperty getRootDir();
    @ApiStatus.Internal
    DirectoryProperty getProjectDir();
    @ApiStatus.Internal
    Property<String> getDisplayName();
  }

  @Inject
  public RepositoryValueSource() {
  }

  @Override
  public final @Nullable V obtain() {
    final Parameters params = this.getParameters();
    final Git git = GitCache.get(params.getRootDir().get().getAsFile()).git(params.getProjectDir().get().getAsFile(), params.getDisplayName().get());
    if (git == null) return null;

    return this.obtain(git);
  }

  protected abstract @Nullable V obtain(final @NotNull Git repository);

  /**
   * A value source that requires no extra parameters.
   * @param <V>
   */
  public static abstract class Parameterless<V> extends RepositoryValueSource<V, RepositoryValueSource.Parameters> {
  }
}
