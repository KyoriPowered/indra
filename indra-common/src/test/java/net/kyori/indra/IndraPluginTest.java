/*
 * This file is part of indra, licensed under the MIT License.
 *
 * Copyright (c) 2020-2025 KyoriPowered
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
package net.kyori.indra;

import net.kyori.indra.test.IndraTesting;
import org.gradle.api.Project;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class IndraPluginTest {
  private static final String PLUGIN = "net.kyori.indra";

  @Test
  void testPluginSimplyApplies() {
    final Project project = IndraTesting.project();
    project.getPluginManager().apply(PLUGIN);
  }

  @Test
  void testCodebergRepository() {
    final Project project = IndraTesting.project();
    project.getPluginManager().apply(PLUGIN);
    final IndraExtension extension = Indra.extension(project.getExtensions());
    extension.codeberg("kyori", "indra", to -> {
      to.ci(true);
      to.scm(true);
      to.issues(true);
      to.publishing(true);
    });

    // CI
    assertEquals("Forgejo Actions", extension.ci().get().system());
    assertEquals("https://codeberg.org/kyori/indra/actions", extension.ci().get().url());

    // SCM
    assertEquals("scm:git:https://codeberg.org/kyori/indra.git", extension.scm().get().connection());
    assertEquals("scm:git:ssh://git@codeberg.org/kyori/indra.git", extension.scm().get().developerConnection());
    assertEquals("https://codeberg.org/kyori/indra", extension.scm().get().url());

    // Issues
    assertEquals("Forgejo", extension.issues().get().system());
    assertEquals("https://codeberg.org/kyori/indra/issues", extension.issues().get().url());

  }

  @Test
  void testExtensionLicense() {
    final Project project = IndraTesting.project();
    project.getPluginManager().apply(PLUGIN);
    final IndraExtension extension = Indra.extension(project.getExtensions());
    assertFalse(extension.license().isPresent());
    assertThrows(IllegalStateException.class, () -> extension.license().get());
    extension.mitLicense();
    assertTrue(extension.license().isPresent());
    assertEquals("The MIT License", extension.license().get().name());
  }
}
