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
package net.kyori.indra.internal.language;

import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.TaskContainer;
import org.gradle.jvm.toolchain.JavaLanguageVersion;
import org.gradle.jvm.toolchain.JavaLauncher;
import org.gradle.jvm.toolchain.JavaToolchainService;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile;
import org.jetbrains.kotlin.gradle.tasks.UsesKotlinJavaToolchain;

public final class KotlinShim {

  public static void configureCompileTasks(
    final JavaToolchainService toolchains,
    final @NotNull TaskContainer tasks,
    final @NotNull SourceSet sourceSet,
    final @NotNull Provider<Integer> toolchainVersion,
    final @NotNull Provider<Integer> bytecodeVersion
  ) {
    final Provider<JavaLauncher> launcher = toolchains.launcherFor(spec -> spec.getLanguageVersion().set(bytecodeVersion.map(v -> JavaLanguageVersion.of(v))));
    tasks.named(sourceSet.getCompileTaskName("kotlin"), UsesKotlinJavaToolchain.class, task -> {
      task.getKotlinJavaToolchain().getToolchain().use(launcher);
      if (task instanceof KotlinCompile) {
        final KotlinCompile kc = (KotlinCompile) task;
        task.getInputs().property("bytecodeVersion", bytecodeVersion);
        // TODO: this is kinda bad, but we can't add an action because this class is loaded in a non-Gradle class loader.
        kc.getKotlinOptions().setJvmTarget(org.gradle.api.JavaVersion.toVersion(bytecodeVersion.get()).toString());
      }
    });
  }
}
