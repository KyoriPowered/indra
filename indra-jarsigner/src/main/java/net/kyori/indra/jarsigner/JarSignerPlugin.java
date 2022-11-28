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

import net.kyori.mammoth.ProjectPlugin;
import org.gradle.api.Project;
import org.gradle.api.artifacts.ConfigurationContainer;
import org.gradle.api.plugins.ExtensionContainer;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.plugins.JavaPluginExtension;
import org.gradle.api.plugins.PluginContainer;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.TaskContainer;
import org.gradle.jvm.toolchain.JavaLauncher;
import org.gradle.jvm.toolchain.JavaToolchainService;
import org.jetbrains.annotations.NotNull;

/**
 * Executes the {@code jarsigner} tool to sign published artifacts.
 *
 * @since 3.1.0
 */
public class JarSignerPlugin implements ProjectPlugin {
  public static final String JAR_SIGNER_EXTENSION = "jarSigning";

  @Override
  public void apply(final @NotNull Project project, final @NotNull PluginContainer plugins, final @NotNull ExtensionContainer extensions, final @NotNull TaskContainer tasks) {
    // extension: register the bits
    final JarSignerExtension extension = extensions.create(JarSignerExtension.class, JAR_SIGNER_EXTENSION, JarSignerExtensionImpl.class);

    tasks.withType(SignJarTask.class).configureEach(task -> {
      task.getAlias().set(extension.alias());
      task.getKeyStore().set(extension.keyStore());
      task.getStorePassword().set(extension.storeFormat());
      task.getKeyPassword().set(extension.keyPassword());
      task.getStrict().set(extension.strict());
      task.getStoreFormat().set(extension.storeFormat());
    });

    // connect jar signer toolchain to project default
    plugins.withType(JavaPlugin.class).configureEach($ -> {
      final JavaToolchainService service = extensions.getByType(JavaToolchainService.class);
      final Provider<JavaLauncher> toolchain = service.launcherFor(extensions.getByType(JavaPluginExtension.class).getToolchain());
      tasks.withType(SignJarTask.class).configureEach(task -> {
        task.getJavaLauncher().convention(toolchain);
      });

      // Sign default configurations
      final ConfigurationContainer configurations = project.getConfigurations();
      extension.signConfigurationOutgoing(configurations.getByName(JavaPlugin.API_ELEMENTS_CONFIGURATION_NAME));
      extension.signConfigurationOutgoing(configurations.getByName(JavaPlugin.RUNTIME_ELEMENTS_CONFIGURATION_NAME));
    });
  }
}
