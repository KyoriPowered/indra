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

import net.kyori.indra.test.IndraTesting;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class NameBasedProjectDocumentationUrlProviderTest {
  private NameBasedProjectDocumentationUrlProvider tested;

  @BeforeEach
  private void setUp() {
    this.tested = IndraTesting.project().getObjects().newInstance(NameBasedProjectDocumentationUrlProviderImpl.class);
  }

  @Test
  void testPlainNames() {
    assertEquals("testproject", this.tested.createUrl("testproject", ":"));
  }

  @Test
  void testPrefixStripping() {
    this.tested.getProjectNamePrefix().set("testproject-");
    assertEquals("core", this.tested.createUrl("testproject-core", ":testproject-core"));
  }

  @Test
  void testPrefixNotPresentInProjectNameIgnored() {
    this.tested.getProjectNamePrefix().set("testproject-");
    assertEquals("unrelated", this.tested.createUrl("unrelated", ":unrelated"));
  }

  @Test
  void testVersionAppended() {
    this.tested.getVersion().set("1.2");
    assertEquals("testproject/1.2", this.tested.createUrl("testproject", ":"));
  }

  @Test
  void testPrefixStrippingAndVersion() {
    this.tested.getProjectNamePrefix().set("testproject-");
    this.tested.getVersion().set("2.x");
    assertEquals("core/2.x", this.tested.createUrl("testproject-core", ":testproject-core"));
  }
}
