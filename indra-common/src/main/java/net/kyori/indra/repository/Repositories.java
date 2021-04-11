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
package net.kyori.indra.repository;

import groovy.lang.Closure;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.gradle.api.artifacts.dsl.RepositoryHandler;
import org.gradle.api.plugins.ExtensionAware;

/**
 * Extensions for declaring additional custom repositories with factory methods.
 *
 * @since 2.0.0
 */
public final class Repositories {

  public static void registerRepositoryExtensions(final @NonNull RepositoryHandler handler, final @NonNull RemoteRepository@NonNull... repositories) {
    registerRepositoryExtensions(handler, Arrays.asList(repositories));
  }

  public static void registerRepositoryExtensions(final @NonNull RepositoryHandler handler, final @NonNull Iterable<RemoteRepository> repositories) {
    for(final RemoteRepository repo : repositories) {
      ((ExtensionAware) handler).getExtensions().add(repo.name(), new Closure<Void>(null, handler) {
        public void doCall() {
          repo.addTo((RepositoryHandler) this.getThisObject());
        }
      });
    }
  }

  static URI uri(final String input) {
    try {
      return new URI(input);
    } catch(final URISyntaxException ex) {
      throw new RuntimeException(ex);
    }
  }

  private Repositories() {
  }
}
