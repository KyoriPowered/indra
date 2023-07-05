/*
 * This file is part of indra, licensed under the MIT License.
 *
 * Copyright (c) 2020-2023 KyoriPowered
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

import java.io.IOException;
import net.kyori.mammoth.test.TestContext;

public final class SettingsFactory {
  private static final String RESOLVER_VERSION = "0.5.0";

  public enum DSLLanguage {
    GROOVY("settings.gradle") {
      @Override
      public String makeSettings(final String rootName) {
        return "pluginManagement {\n" +
          "  repositories {\n" +
          "    mavenCentral()\n" +
          "    gradlePluginPortal()\n" +
          "  }\n" +
          "}\n" +
          "\nplugins {\n" +
          "  id 'org.gradle.toolchains.foojay-resolver-convention' version '" + RESOLVER_VERSION + "'\n" +
          "}\n" +
          "\nrootProject.name = '" + rootName + "'\n";
      }
    },
    KOTLIN("settings.gradle.kts") {
      @Override
      public String makeSettings(final String rootName) {
        return "pluginManagement {\n" +
          "  repositories {\n" +
          "    mavenCentral()\n" +
          "    gradlePluginPortal()\n" +
          "  }\n" +
          "}\n" +
          "\nplugins {\n" +
          "  id(\"org.gradle.toolchains.foojay-resolver-convention\") version \"" + RESOLVER_VERSION + "\"\n" +
          "}\n" +
          "\nrootProject.name = \"" + rootName + "\"\n";
      }
    };

    private final String fileName;

    DSLLanguage(final String fileName) {
      this.fileName = fileName;
    }

    public abstract String makeSettings(final String rootName);
  }

  private SettingsFactory() {
  }

  public static void createSettings(final TestContext ctx, final String rootName) throws IOException {
    createSettings(ctx, rootName, DSLLanguage.GROOVY);
  }

  public static void createSettings(final TestContext ctx, final String rootName, final DSLLanguage lang) throws IOException {
    ctx.writeText(lang.fileName, lang.makeSettings(rootName));
  }
}
