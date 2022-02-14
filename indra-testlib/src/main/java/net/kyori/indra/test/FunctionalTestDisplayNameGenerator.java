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
package net.kyori.indra.test;

import java.lang.reflect.Method;
import org.junit.jupiter.api.DisplayNameGenerator;

/**
 * An extension of the standard display name generator that only uses method names for test display names.
 *
 * <p>This is better suited for test directory selection.</p>
 *
 * @since 1.1.0
 */
public final class FunctionalTestDisplayNameGenerator extends DisplayNameGenerator.Standard {
  @Override
  public String generateDisplayNameForMethod(final Class<?> testClass, final Method testMethod) {
    final String name = testMethod.getName();
    if (name.startsWith("test") && name.length() > 5) {
      return Character.toLowerCase(name.charAt(4)) + name.substring(5);
    } else {
      return name;
    }
  }
}
