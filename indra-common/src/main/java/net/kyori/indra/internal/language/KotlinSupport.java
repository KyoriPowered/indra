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
package net.kyori.indra.internal.language;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Map;
import java.util.WeakHashMap;
import javax.inject.Inject;
import net.kyori.indra.internal.SelfPreferringClassLoader;
import org.gradle.api.Action;
import org.gradle.api.GradleException;
import org.gradle.api.Project;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.TaskContainer;
import org.gradle.jvm.toolchain.JavaToolchainService;
import org.jetbrains.annotations.NotNull;

public class KotlinSupport implements LanguageSupport {
  private static final String KOTLIN_PLUGIN = "org.jetbrains.kotlin.jvm";
  private static final String SENTINEL_CLASS = "org.jetbrains.kotlin.gradle.tasks.KotlinCompile";

  private final JavaToolchainService toolchains;
  private final ThreadLocal<KotlinInvoker> childClass = new ThreadLocal<>();
  private final Map<org.gradle.api.Plugin<?>, KotlinInvoker> invokers = new WeakHashMap<>();

  @Inject
  public KotlinSupport(final JavaToolchainService toolchains) {
    this.toolchains = toolchains;
  }

  @Override
  public void registerApplyCallback(final @NotNull Project project, final @NotNull Action<? super Project> callback) {
    project.getPlugins().withId(KOTLIN_PLUGIN, pl -> {
      // Create a classloader for us, if necessary
      synchronized (this.invokers) {
        this.childClass.set(this.invokers.computeIfAbsent(pl, key -> {
          try {
            try {
              this.getClass().getClassLoader().loadClass(SENTINEL_CLASS);
              return new KotlinInvoker(this.getClass().getClassLoader());
            } catch (final ClassNotFoundException ex) {
              final URLClassLoader classLoader = new SelfPreferringClassLoader(
                  new URL[]{KotlinSupport.class.getProtectionDomain().getCodeSource().getLocation()},
                  key.getClass().getClassLoader()
              );
              return new KotlinInvoker(classLoader);
            }
          } catch (final ReflectiveOperationException ex) {
            throw new GradleException("Unable to create a Kotlin invoker", ex);
          }
        }));
      }

      try {
        callback.execute(project);
      } finally {
        this.childClass.remove();
      }
    });

  }

  @Override
  public void configureCompileTasks(final @NotNull Project project, final @NotNull SourceSet sourceSet, final @NotNull Provider<Integer> toolchainVersion, final @NotNull Provider<Integer> bytecodeVersion) {
    this.childClass.get().configureCompileTasks(this.toolchains, project.getTasks(), sourceSet, toolchainVersion, bytecodeVersion);
  }

  static final class KotlinInvoker {
    private static final String KOTLIN_SHIM = "net.kyori.indra.internal.language.KotlinShim";

    private final Method configureCompileTasks;

    KotlinInvoker(final ClassLoader loader) throws ReflectiveOperationException {
      final Class<?> kotlinShim = loader.loadClass(KOTLIN_SHIM);
      this.configureCompileTasks = kotlinShim.getMethod("configureCompileTasks", JavaToolchainService.class, TaskContainer.class, SourceSet.class, Provider.class, Provider.class);
    }

    void configureCompileTasks(
      final JavaToolchainService toolchains,
      final @NotNull TaskContainer tasks,
      final @NotNull SourceSet sourceSet,
      final @NotNull Provider<Integer> toolchainVersion,
      final @NotNull Provider<Integer> bytecodeVersion
    ) {
      try {
        this.configureCompileTasks.invoke(null, toolchains, tasks, sourceSet, toolchainVersion, bytecodeVersion);
      } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
        throw new GradleException("Failed to invoke compile task configuration step for Kotlin source set", ex);
      }
    }
  }
}
