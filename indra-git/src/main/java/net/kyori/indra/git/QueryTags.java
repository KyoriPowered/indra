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

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Ref;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;
import org.jetbrains.annotations.NotNull;

/**
 * A {@link MappedRepositoryValueSource} that queries the git repository for a list of tags.
 * Returns an empty list if the repository is not present or if an error occurs while querying.
 *
 * @param <V> value type
 */
public abstract class QueryTags<V> extends MappedRepositoryValueSource.Parameterless<List<? extends Ref>, V> {
  private static final Logger LOGGER = Logging.getLogger(QueryTags.class);

  @Override
  protected @NotNull List<? extends Ref> getRawValue(final @NotNull Git repository) {
    try {
      return repository.tagList().call();
    } catch (final GitAPIException ex) {
      LOGGER.error("Failed to query git for a list of tags:", ex);
      return Collections.emptyList();
    }
  }

  public abstract static class Names extends QueryTags<List<String>> {
    @Override
    protected @NotNull List<String> mapValue(final @NotNull Git git, final @NotNull List<? extends Ref> value) {
      return value.stream()
        .map(Ref::getName)
        .collect(Collectors.toList());
    }
  }
}
