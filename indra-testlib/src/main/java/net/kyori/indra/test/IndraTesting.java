/*
 * This file is part of indra, licensed under the MIT License.
 *
 * Copyright (c) 2020-2024 KyoriPowered
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
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.function.Consumer;
import org.gradle.api.Project;
import org.gradle.testfixtures.ProjectBuilder;
import org.jetbrains.annotations.Nullable;

import static org.junit.jupiter.api.Assertions.fail;

public class IndraTesting {
  public static Project project() {
    return project(null);
  }

  public static Project project(final @Nullable Consumer<ProjectBuilder> consumer) {
    final ProjectBuilder builder = ProjectBuilder.builder();
    if(consumer != null) {
      consumer.accept(builder);
    }
    final Project ret = builder.build();
    ret.getExtensions().getExtraProperties().set("net.kyori.indra.testing", true);
    return ret;
  }

  public static String exec(final Path workingDir, final String... cli) throws IOException {
      final StringWriter error = new StringWriter();
      final StringWriter output = new StringWriter();
      final Process process = new ProcessBuilder(cli)
          .directory(workingDir.toFile())
          .redirectError(ProcessBuilder.Redirect.PIPE)
          .redirectOutput(ProcessBuilder.Redirect.PIPE)
          .start();

      try (final InputStreamReader stdout = new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8);
          final InputStreamReader stderr = new InputStreamReader(process.getErrorStream(), StandardCharsets.UTF_8)) {
          int read;
          final char[] data = new char[8192];
          while (process.isAlive()) {
              if ((read = stdout.read(data)) != -1) {
                  output.write(data, 0, read);
              }
              if ((read = stderr.read(data)) != -1) {
                  error.write(data, 0, read);
              }
          }
      }

      if (process.exitValue() != 0) {
          System.err.println("====== Standard error =====");
          System.err.println(error.getBuffer());
          System.err.println("====== Standard output =====");
          System.err.println(output.getBuffer());
          fail("Process " + String.join(" ", cli) + " exited with code " + process.exitValue());
      }

      return output.getBuffer().toString();
  }
}
