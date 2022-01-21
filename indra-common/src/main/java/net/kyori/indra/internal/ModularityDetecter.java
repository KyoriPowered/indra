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
package net.kyori.indra.internal;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Our version of JavaModuleDetector from Gradle, since that's internal API.
 */
public final class ModularityDetecter {
  private static final Logger LOGGER = Logging.getLogger(ModularityDetecter.class);

  private static final String MODULE_INFO = "module-info.class";
  private static final String MANIFEST = "META-INF/MANIFEST.MF";
  private static final String AUTOMATIC_MODULE_NAME = "Automatic-Module-Name";
  private static final String MULTI_RELEASE = "Multi-Release";
  private static final String MULTIRELEASE_PATH_PREFIX = "META-INF/versions/";

  private ModularityDetecter() {
  }

  public static boolean isModule(final File library, final boolean inferModulePath) {
    if (!inferModulePath) {
      return false;
    }

    if (library.isFile()) {
      // Treat as a jar file
      try (final ZipFile jf = new ZipFile(library)) {
        // Direct module
        if (jf.getEntry(MODULE_INFO) != null) {
          return true;
        }
        final @Nullable ZipEntry manifestEntry = jf.getEntry(MANIFEST);
        if (manifestEntry != null) {
          final Manifest manifest;
          try (final InputStream is = jf.getInputStream(manifestEntry)) {
            manifest = new Manifest(is);
          }
          // Automatic module
          if (manifest.getMainAttributes().getValue(AUTOMATIC_MODULE_NAME) != null) {
            return true;
          }
          // In multi-release variant
          if ("true".equals(manifest.getMainAttributes().getValue(MULTI_RELEASE))) {
            return jf.stream().anyMatch(entry -> isModuleInfo(entry.getName()));
          }
        }
      } catch (final IOException ex) {
        LOGGER.debug("Failed to determine module status for {}:", library, ex);
        return false;
      }
    } else if (library.isDirectory()) {
      // Directory, unpacked module
      // Direct module
      if (new File(library, MODULE_INFO).isFile()) {
        return true;
      }
      final File manifestFile = new File(library, MANIFEST);
      if (manifestFile.isFile()) {
        final Manifest manifest;
        try (final InputStream is = new FileInputStream(manifestFile)) {
          manifest = new Manifest(is);
        } catch (final IOException ex) {
          LOGGER.debug("Failed to determine module status for {}:", library, ex);
          return false;
        }
        // Automatic module
        if (manifest.getMainAttributes().getValue(AUTOMATIC_MODULE_NAME) != null) {
          return true;
        }
        // In multi-release variant
        if ("true".equals(manifest.getMainAttributes().getValue(MULTI_RELEASE))) {
          final File[] variants = new File(library, "META-INF/versions/").listFiles();
          if (variants != null) {
            for (final File variant : variants) {
              if (new File(variant, MODULE_INFO).exists()) {
                return true;
              }
            }
          }
        }
      }
    }
    return false;
  }

  public static boolean isModuleInfo(final @NotNull String path) {
    return "module-info.class".equals(path) || (path.startsWith(MULTIRELEASE_PATH_PREFIX) && path.endsWith("module-info.class")); // todo: stricter multi-release handling
  }
}
