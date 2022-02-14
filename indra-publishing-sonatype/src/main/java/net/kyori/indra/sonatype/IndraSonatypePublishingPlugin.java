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
package net.kyori.indra.sonatype;

import io.github.gradlenexus.publishplugin.NexusPublishExtension;
import io.github.gradlenexus.publishplugin.NexusPublishPlugin;
import net.kyori.indra.IndraPlugin;
import net.kyori.indra.IndraPublishingPlugin;
import net.kyori.indra.sonatype.internal.IndraSonatypePublishingExtensionImpl;
import net.kyori.mammoth.ProjectPlugin;
import org.gradle.api.Project;
import org.gradle.api.plugins.ExtensionContainer;
import org.gradle.api.plugins.PluginContainer;
import org.gradle.api.tasks.TaskContainer;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;

/**
 * A plugin for configuring publication to Sonatype OSSRH.
 *
 * <p>This plugin can only be applied to the root project.</p>
 *
 * @since 2.0.0
 */
public class IndraSonatypePublishingPlugin implements ProjectPlugin {
  private static final String EXTENSION_NAME = "indraSonatype";

  @Override
  public void apply(final @NotNull Project project, final @NotNull PluginContainer plugins, final @NotNull ExtensionContainer extensions, final @NotNull TaskContainer tasks) {
    plugins.withType(IndraPlugin.class, plugin -> plugins.apply(IndraPublishingPlugin.class));
    plugins.apply(NexusPublishPlugin.class);

    extensions.configure(NexusPublishExtension.class, extension -> {
      extension.getRepositories().sonatype();

      // Bump out timeouts for days when OSSRH is slow
      extension.getClientTimeout().set(Duration.ofMinutes(5));
      extension.getConnectTimeout().set(Duration.ofMinutes(5));
    });

    extensions.create(IndraSonatypePublishingExtension.class, EXTENSION_NAME, IndraSonatypePublishingExtensionImpl.class, extensions.getByType(NexusPublishExtension.class));
  }
}
