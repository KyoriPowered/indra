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
package net.kyori.indra.crossdoc;

import org.gradle.api.Named;
import org.gradle.api.attributes.Attribute;

/**
 * Indicate the packaging of a Javadoc variant.
 *
 * @since 2.1.0
 */
public interface JavadocPackaging extends Named {
  /**
   * The attribute itself.
   *
   * @since 2.1.0
   */
  Attribute<JavadocPackaging> JAVADOC_PACKAGING_ATTRIBUTE = Attribute.of("net.kyori.indra.javadoc-packaging", JavadocPackaging.class);

  /**
   * Packaged as an archive, such as a {@code jar}.
   *
   * @since 2.1.0
   */
  String ARCHIVE = "archive";

  /**
   * Published as an exploded directory, the original output format.
   *
   * @since 2.1.0
   */
  String DIRECTORY = "directory";
}
