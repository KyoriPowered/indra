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
package net.kyori.indra.gradle.api;

import org.gradle.api.plugins.ExtensionContainer;

public final class Extensions {
  public static <E> E findOrCreate(final ExtensionContainer extensions, final String name, final Class<E> type) {
    E extension = extensions.findByType(type);
    if(extension == null) {
      extension = extensions.create(name, type);
    }
    return extension;
  }

  @SuppressWarnings("unchecked")
  public static <E> E findOrCreate(final ExtensionContainer extensions, final String name, final Class<? super E> publicType, final Class<E> implementationType) {
    E extension = extensions.findByType(implementationType);
    if(extension == null) {
      extension = (E) extensions.create(publicType, name, implementationType);
    }
    return extension;
  }

  private Extensions() {
  }
}
