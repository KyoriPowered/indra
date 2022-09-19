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
package net.kyori.indra.repository;

import groovy.lang.Closure;
import java.util.Arrays;
import org.gradle.api.artifacts.dsl.RepositoryHandler;
import org.gradle.api.plugins.ExtensionAware;
import org.jetbrains.annotations.NotNull;

/**
 * Extensions for declaring additional custom repositories with factory methods.
 *
 * @deprecated for removal since 2.2.0, will be removed in 3.0.0
 * @since 2.0.0
 */
@Deprecated
public final class Repositories {
  private Repositories() {
  }

  public static void registerRepositoryExtensions(final @NotNull RepositoryHandler handler, final @NotNull RemoteRepository@NotNull... repositories) {
    registerRepositoryExtensions(handler, Arrays.asList(repositories));
  }

  public static void registerRepositoryExtensions(final @NotNull RepositoryHandler handler, final @NotNull Iterable<RemoteRepository> repositories) {
    for (final RemoteRepository repo : repositories) {
      ((ExtensionAware) handler).getExtensions().add(repo.name(), new Closure<Void>(null, handler) {
        public void doCall() {
          repo.addTo((RepositoryHandler) this.getThisObject());
        }
      });
    }
  }
}
