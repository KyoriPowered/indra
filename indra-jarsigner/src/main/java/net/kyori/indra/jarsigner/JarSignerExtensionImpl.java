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

import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.ConfigurationPublications;
import org.gradle.api.artifacts.PublishArtifact;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.internal.artifacts.dsl.LazyPublishArtifact;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.TaskContainer;
import org.gradle.api.tasks.TaskProvider;
import org.gradle.api.tasks.bundling.AbstractArchiveTask;
import org.jetbrains.annotations.NotNull;

class JarSignerExtensionImpl implements JarSignerExtension {
  private final Property<String> alias;
  private final RegularFileProperty keyStore;
  private final Property<String> storePassword;
  private final Property<String> keyPassword;
  private final Property<Boolean> strict;
  private final Property<String> storeFormat;

  private final TaskContainer tasks;

  @Inject
  public JarSignerExtensionImpl(final ObjectFactory objects, final TaskContainer tasks) {
    this.alias = objects.property(String.class);
    this.keyStore = objects.fileProperty();
    this.storePassword = objects.property(String.class);
    this.keyPassword = objects.property(String.class);
    this.strict = objects.property(Boolean.class)
      .convention(DEFAULT_STRICT);
    this.storeFormat = objects.property(String.class)
      .convention(DEFAULT_STORE_FORMAT);

    this.tasks = tasks;
  }

  @Override
  public @NotNull Property<String> alias() {
    return this.alias;
  }

  @Override
  public @NotNull RegularFileProperty keyStore() {
    return this.keyStore;
  }

  @Override
  public @NotNull Property<String> storePassword() {
    return this.storePassword;
  }

  @Override
  public @NotNull Property<String> keyPassword() {
    return this.keyPassword;
  }

  @Override
  public @NotNull Property<Boolean> strict() {
    return this.strict;
  }

  @Override
  public @NotNull Property<String> storeFormat() {
    return this.storeFormat;
  }

  @Override
  public TaskProvider<SignJarTask> sign(final TaskProvider<? extends AbstractArchiveTask> task) {
    final TaskProvider<SignJarTask> ret = this.tasks.register(signName(task.getName()), SignJarTask.class, jarsign -> {
      jarsign.from(task.flatMap(AbstractArchiveTask::getArchiveFile));
      jarsign.getArchiveClassifier().set(task.flatMap(AbstractArchiveTask::getArchiveClassifier).map(clf -> {
        if (clf.equals("unsigned")) {
          return "";
        } else if (clf.endsWith("-unsigned")) {
          return clf.substring(0, clf.length() - 9);
        } else {
          return clf;
        }
      }));
      jarsign.getArchiveAppendix().set(task.flatMap(AbstractArchiveTask::getArchiveAppendix));
      jarsign.getArchiveBaseName().set(task.flatMap(AbstractArchiveTask::getArchiveBaseName));
      jarsign.getArchiveVersion().set(task.flatMap(AbstractArchiveTask::getArchiveVersion));
      jarsign.getArchiveExtension().set(task.flatMap(AbstractArchiveTask::getArchiveExtension));
    });
    task.configure(orig -> {
      if (orig.getArchiveClassifier().getOrElse("").isEmpty()) {
        orig.getArchiveClassifier().set("unsigned");
      } else if (!orig.getArchiveClassifier().get().endsWith("-unsigned")) {
        orig.getArchiveClassifier().set(orig.getArchiveClassifier().get() + "-unsigned");
      }
    });

    return ret;
  }

  @Override
  public TaskProvider<SignJarTask> signConfigurationOutgoing(final Configuration configuration) {
    // Replace the outgoing default artifacts of this configuration
    final ConfigurationPublications outgoing = configuration.getOutgoing();
    final List<Object> newArtifacts = new ArrayList<>();
    for (final PublishArtifact artifact : outgoing.getArtifacts()) {
      if (artifact instanceof LazyPublishArtifact) {
        // ((LazyPublishArtifact) artifact).delegate;
      } else {

      }
      // lazy publish artifact?
      // task-based artifact?
      // unwrap all, create sign tasks based on the artifact source?
    }
    return null;
  }

  private static String signName(final String origName) {
    final int initialChar = origName.codePointAt(0);
    final String subbed;
    if (Character.isBmpCodePoint(initialChar)) {
      subbed = origName.substring(1);
    } else {
      subbed = origName.substring(2);
    }

    return new StringBuilder(origName.length() + 7)
      .append("jarsign")
      .appendCodePoint(Character.toUpperCase(initialChar))
      .append(subbed)
      .toString();
  }
}
