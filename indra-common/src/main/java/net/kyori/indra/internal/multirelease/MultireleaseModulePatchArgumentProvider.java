/*
 * This file is part of indra, licensed under the MIT License.
 *
 * Copyright (c) 2021 KyoriPowered
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
package net.kyori.indra.internal.multirelease;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.stream.Collectors;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.Optional;
import org.gradle.process.CommandLineArgumentProvider;

class MultireleaseModulePatchArgumentProvider implements CommandLineArgumentProvider {

  private final Property<String> moduleName;

  private final ConfigurableFileCollection classDirectories;

  MultireleaseModulePatchArgumentProvider(final ObjectFactory objects) {
    this.moduleName = objects.property(String.class);
    this.classDirectories = objects.fileCollection();
  }

  @Input
  @Optional
  public Property<String> getModuleName() {
    return this.moduleName;
  }

  @Internal // dependency relations already exist because of compile/runtime classpaths
  public ConfigurableFileCollection getClassDirectories() {
    return this.classDirectories;
  }

  @Override
  public Iterable<String> asArguments() {
    final @Nullable String moduleName = this.moduleName.getOrNull();
    if(moduleName == null) return Collections.emptyList();

    this.getClassDirectories().finalizeValue();
    final String directories = this.getClassDirectories().getFiles()
      .stream()
      .map(File::getAbsolutePath)
      .collect(Collectors.joining(File.pathSeparator, moduleName + "=", ""));

    return Arrays.asList(
      "--patch-module",
      directories
    );
  }
}
