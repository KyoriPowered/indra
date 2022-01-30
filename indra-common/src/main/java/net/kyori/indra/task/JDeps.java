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
package net.kyori.indra.task;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.inject.Inject;
import net.kyori.indra.util.Versioning;
import org.gradle.api.DefaultTask;
import org.gradle.api.InvalidUserDataException;
import org.gradle.api.JavaVersion;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputFiles;
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.Nested;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.TaskAction;
import org.gradle.jvm.toolchain.JavaLanguageVersion;
import org.gradle.jvm.toolchain.JavaLauncher;
import org.gradle.jvm.toolchain.JavaToolchainService;
import org.gradle.process.CommandLineArgumentProvider;
import org.gradle.process.ExecOperations;

import static java.util.Objects.requireNonNull;

/**
 * Execute the {@code jdeps} tool.
 *
 * @since 2.1.0
 */
public abstract class JDeps extends DefaultTask {
  private final List<CommandLineArgumentProvider> argumentProviders = new ArrayList<>();

  /**
   * Get the module path to pass to the JDeps tool.
   *
   * @return a file collection representing the module path to pass to the tool
   * @since 2.1.0
   */
  @InputFiles
  public abstract ConfigurableFileCollection getModulePath();

  /**
   * Set the Java instance to execute the {@code jdeps} tool with.
   *
   * @return a property pointing to the java launcher
   * @since 2.1.0
   */
  @Input
  public abstract Property<JavaLauncher> getJavaLauncher();

  /**
   * Class files, directories, and/or JAR files to process.
   *
   * @return the collection
   * @since 2.1.0
   */
  @Optional
  @InputFiles
  public abstract ConfigurableFileCollection getProcessClasses();

  /**
   * Free-form arguments to pass to {@code jdeps}.
   *
   * @return a list property of arguments to pass
   * @since 2.1.0
   */
  @Input
  @Optional
  public abstract ListProperty<String> getArguments();

  /**
   * Extra argument providers to generate jdeps arguments.
   *
   * @return argument providers
   * @since 2.1.0
   */
  @Nested
  public List<CommandLineArgumentProvider> getArgumentProviders() {
    return this.argumentProviders;
  }

  /**
   * Set the specific version to test against given a multi-release input jar.
   *
   * @return the version to test against
   * @since 2.1.0
   */
  @Input
  @Optional
  public abstract Property<Integer> getMultireleaseVersion();

  public void argumentProvider(final CommandLineArgumentProvider argumentProvider) {
    this.argumentProviders.add(requireNonNull(argumentProvider, "argumentProvider"));
  }

  @Inject
  protected abstract JavaToolchainService getToolchains();

  @Inject
  protected abstract ExecOperations getExecOps();

  /**
   * Create a new task instance.
   *
   * <p>Not to be called directly</p>
   *
   * @since 2.1.0
   */
  public JDeps() {
    this.getJavaLauncher().convention(this.getToolchains()
      .launcherFor(spec -> spec.getLanguageVersion().set(JavaLanguageVersion.of(Versioning.versionNumber(JavaVersion.current())))));
      this.getMultireleaseVersion().finalizeValueOnRead();
  }

  /**
   * Calculate all arguments for the task, using freeform arguments and argument providers.
   *
   * @return all arguments
   * @since 2.1.0
   */
  @Internal
  public List<String> getAllArguments() {
    final List<String> args = new ArrayList<>(this.getArguments().get());
    for (final CommandLineArgumentProvider clap : this.getArgumentProviders()) {
      final Iterable<String> provided = clap.asArguments();
      if (provided instanceof Collection<?>) {
        args.addAll((Collection<String>) provided);
      } else {
        for (final String arg : provided) {
          args.add(arg);
        }
      }
    }
    return args;
  }

  private File findJDeps() {
    final JavaLauncher launcher = this.getJavaLauncher().get();
    final File executable = launcher.getExecutablePath().getAsFile();
    final String jdepsFileName;
    final int dotIdx = executable.getName().indexOf('.');
    if (dotIdx != -1) {
      jdepsFileName = "jdeps" + executable.getName().substring(dotIdx);
    } else {
      jdepsFileName = "jdeps";
    }
    final File jdeps = new File(executable.getParentFile(), jdepsFileName);
    if (!jdeps.exists()) {
      throw new InvalidUserDataException("Toolchain of version " + launcher.getMetadata().getLanguageVersion() + " did not have a jdeps executable.");
    }
    return jdeps;
  }

  /**
   * Execute the task.
   *
   * @since 2.1.0
   */
  @TaskAction
  public void execute() {
    // TODO: Use ToolProvider API when available? would need a J9 source set
    final File jdeps = this.findJDeps();
    this.getExecOps().exec(spec -> {
      spec.setExecutable(jdeps);
      if (this.getMultireleaseVersion().isPresent()) {
        spec.args("--multi-release", this.getMultireleaseVersion().get().toString());
      }
      spec.args("--module-path", this.getModulePath().getAsPath()); // TODO: split between classpath and module path
      spec.args(this.getAllArguments());
      for (final File file : this.getProcessClasses()) {
        spec.args(file.getAbsolutePath());
      }
    }).assertNormalExitValue();
  }
}
