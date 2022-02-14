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
package net.kyori.indra.task;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;
import net.kyori.indra.internal.ModularityDetecter;
import org.gradle.api.DefaultTask;
import org.gradle.api.GradleException;
import org.gradle.api.InvalidUserDataException;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.SetProperty;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.TaskAction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.VisibleForTesting;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ModuleVisitor;
import org.objectweb.asm.Opcodes;

/**
 * Validate that all packages within a module are appropriately exported.
 *
 * @since 2.1.0
 */
public abstract class CheckModuleExports extends DefaultTask {
  private static final String MULTIRELEASE_PATH_PREFIX = "META-INF/versions/";

  /**
   * A file property referring to the module jar checked in this task.
   *
   * @return the checked module
   */
  @InputFile
  public abstract RegularFileProperty getCheckedModule();

  /**
   * Get package prefixes that do not have to be exported.
   *
   * @return the package exclusion property
   */
  @Input
  public abstract SetProperty<String> getExclusions();

  /**
   * Add package prefix exclusions for module validation.
   *
   * @param prefixes the prefixes to exclude
   */
  public void exclude(final String... prefixes) {
    this.getExclusions().addAll(prefixes);
  }

  @TaskAction
  public void validateModule() throws ZipException, IOException {
    final Set<String> exclusions = this.getExclusions().get();
    final Set<String> knownPackages = new HashSet<>();
    final Set<String> exports = new HashSet<>();
    boolean moduleInfoSeen = false;
    // require that the checked module is a jar
    final File inspected = this.getCheckedModule().get().getAsFile();
    if (!inspected.getName().endsWith(".jar")) {
      throw new InvalidUserDataException("Inspected file '" + inspected + "' was not a jar, when the CheckModuleExports task expected it to be");
    }

    // traverse the paths, build a set of directory entries
    // if path is `module-info`, then read it and get the exported packages
    try (final ZipFile jar = new ZipFile(inspected)) {
      for (final Enumeration<? extends ZipEntry> entries = jar.entries(); entries.hasMoreElements();) {
        final ZipEntry check = entries.nextElement();
        if (ModularityDetecter.isModuleInfo(check.getName())) {
          exports.addAll(this.exports(jar.getInputStream(check))); // todo: this just merges all multi-release module descriptors, should we refine our logic further?
          moduleInfoSeen = true;
        } else if (!check.isDirectory()) {
          final @Nullable String packageName = packageNameOf(check.getName());
          if (packageName != null) {
            knownPackages.add(packageName);
          }
        }
      }
    }

    this.getLogger().debug("Detected exported packages: {}", exports);
    if (!moduleInfoSeen) {
      throw new InvalidUserDataException("Jar file " + inspected + " did not contain any module descriptor!");
    }

    final Set<String> exportProblems = this.checkExports(exports, knownPackages, exclusions);
    if (!exportProblems.isEmpty()) {
      this.getLogger().error("Some packages in {} were not exported when they were expected to be:", inspected);
      for (final String problem : exportProblems) {
        this.getLogger().error("- {}", problem);
      }
      throw new GradleException();
    }
  }

  private Set<String> checkExports(final Set<String> exported, final Set<String> known, final Set<String> excludedPrefixes) {
    final Set<String> problems = new HashSet<>(known);
    problems.removeAll(exported);
    for (final Iterator<String> it = known.iterator(); it.hasNext();) {
      final String check = it.next();
      for (final String prefix : excludedPrefixes) {
        if (check.startsWith(prefix)) {
          it.remove();
          break;
        }
      }
    }
    return problems;
  }

  @VisibleForTesting
  static @Nullable String packageNameOf(@NotNull String file) {
    if (file.startsWith(MULTIRELEASE_PATH_PREFIX)) { // META-INF/versions: strip prefix
      final int nextSlash = file.indexOf("/", MULTIRELEASE_PATH_PREFIX.length());
      if (nextSlash == -1) {
        return null;
      }
      file = file.substring(nextSlash + 1);
    } else if (file.startsWith("META-INF/")) { // META-INF/: ignore
      return null;
    }
    final int lastSlash = file.lastIndexOf('/');
    if (lastSlash == -1) {
      return null;
    }
    return file.substring(0, lastSlash).replace('/', '.');
  }

  private Set<String> exports(final InputStream is) throws IOException {
    final ClassReader reader = new ClassReader(is);
    final Set<String> exports = new HashSet<>();
    reader.accept(new ModuleExportsVisitor(exports), ClassReader.SKIP_CODE | ClassReader.SKIP_FRAMES);
    return Collections.unmodifiableSet(exports);
  }

  static final class ModuleExportsVisitor extends ClassVisitor {
    private static final int ASMAPI = Opcodes.ASM9;
    private final Set<String> exports;

    public ModuleExportsVisitor(final Set<String> exports) {
      super(ASMAPI);
      this.exports = exports;
    }

    final class Exports extends ModuleVisitor {
      Exports(final ModuleVisitor parent) {
        super(ASMAPI, parent);
      }

      @Override
      public void visitExport(final String packaze, final int access, final String... modules) {
        ModuleExportsVisitor.this.exports.add(packaze.replace('/', '.'));
        super.visitExport(packaze, access, modules);
      }
    }

    @Override
    public ModuleVisitor visitModule(final String name, final int access, final String version) {
      return new Exports(super.visitModule(name, access, version));
    }
  }
}
