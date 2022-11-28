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
package net.kyori.indra.jarsigner;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.KeyStore;
import java.util.Map;
import java.util.zip.ZipFile;
import jdk.security.jarsigner.JarSigner;
import jdk.security.jarsigner.JarSignerException;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;

final class JarSignerInvoker {
  private static final Logger LOGGER = Logging.getLogger(JarSignerInvoker.class);

  private JarSignerInvoker() {
  }

  // reflectively invoked, to be able to be used within Gradle workers.
  public static boolean execute(final File source, final File target, final KeyStore.PrivateKeyEntry signingKey, final Map<String, String> properties) {
    final JarSigner.Builder builder = new JarSigner.Builder(signingKey);
    for (final Map.Entry<String, String> entry : properties.entrySet()) {
      builder.setProperty(entry.getKey(), entry.getValue());
    }
    builder.eventHandler((action, file) -> LOGGER.debug("[jarsigner] {}: {}", action, file));

    try (final ZipFile in = new ZipFile(source);
         final BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(target))
    ) {
      builder.build().sign(in, bos);
    } catch (final IOException ex) {
      LOGGER.error("Failed to perform IO", ex);
      return false;
    } catch (final JarSignerException ex) {
      LOGGER.error("Failed to sign jar", ex);
      return false;
    }

    return true;
  }

}
