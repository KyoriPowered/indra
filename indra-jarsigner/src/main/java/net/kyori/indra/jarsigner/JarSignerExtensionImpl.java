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

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.ConfigurationContainer;
import org.gradle.api.artifacts.ConfigurationPublications;
import org.gradle.api.artifacts.PublishArtifact;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.internal.artifacts.publish.ArchivePublishArtifact;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.provider.Property;
import org.gradle.api.provider.Provider;
import org.gradle.api.provider.ProviderFactory;
import org.gradle.api.tasks.TaskContainer;
import org.gradle.api.tasks.TaskProvider;
import org.gradle.api.tasks.bundling.AbstractArchiveTask;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

class JarSignerExtensionImpl implements JarSignerExtension {
  static final Logger LOGGER = Logging.getLogger(JarSignerExtensionImpl.class);

  private final Property<String> alias;
  private final RegularFileProperty keyStore;
  private final Property<String> storePassword;
  private final Property<String> keyPassword;
  private final Property<Boolean> strict;
  private final Property<String> storeFormat;

  private final ProviderFactory providers;
  private final TaskContainer tasks;
  private final ConfigurationContainer configurations;

  @Inject
  public JarSignerExtensionImpl(final ObjectFactory objects, final ProviderFactory providers, final TaskContainer tasks, final ConfigurationContainer configurations) {
    this.alias = objects.property(String.class);
    this.keyStore = objects.fileProperty();
    this.storePassword = objects.property(String.class);
    this.keyPassword = objects.property(String.class);
    this.strict = objects.property(Boolean.class)
      .convention(DEFAULT_STRICT);
    this.storeFormat = objects.property(String.class)
      .convention(DEFAULT_STORE_FORMAT);

    this.providers = providers;
    this.tasks = tasks;
    this.configurations = configurations;
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
    final String expectedName = signName(task.getName());
    if (this.tasks.getNames().contains(expectedName)) { // don't duplicate work
      return this.tasks.named(expectedName, SignJarTask.class);
    }

    final TaskProvider<SignJarTask> ret = this.tasks.register(expectedName, SignJarTask.class, jarsign -> {
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
  public void signConfigurationOutgoing(final Configuration configuration) {
    // Replace the outgoing default artifacts of this configuration
    final ConfigurationPublications outgoing = configuration.getOutgoing();
    final List<OldToNew> newArtifacts = new ArrayList<>();
    for (final PublishArtifact artifact : outgoing.getArtifacts()) {
      final @Nullable TaskProvider<SignJarTask> extracted = this.tryExtract(artifact);
      if (extracted != null) {
        newArtifacts.add(new OldToNew(artifact, extracted));
        continue;
      }

      if (!(artifact instanceof ArchivePublishArtifact)) { // ignore non-archives
        LOGGER.info("Ignoring publish artifact {} of configuration {}", artifact.getName(), configuration.getName());
        newArtifacts.add(new OldToNew(artifact, artifact));
        continue;
      }

      // this code path probably won't trigger often, hope for the best?
      final String expected = signName("artifact" + capitalize(artifact.getName()));
      if (this.tasks.getNames().contains(expected)) {
        newArtifacts.add(new OldToNew(artifact, this.tasks.named(expected)));
        continue;
      }

      // fallback case
      final ProviderFactory providers = this.providers;
      final TaskProvider<SignJarTask> task = this.tasks.register(expected, SignJarTask.class, jarsign -> {
        jarsign.from(providers.provider(() -> artifact.getFile()));
        jarsign.getArchiveClassifier().set(providers.provider(() -> artifact.getClassifier() + "signed"));
        jarsign.getArchiveBaseName().set(providers.provider(() -> artifact.getName()));
        // jarsign.getArchiveVersion().set(providers.provider(() -> project.getVersion()));
        jarsign.getArchiveExtension().set(providers.provider(() -> artifact.getExtension()));
      });
      newArtifacts.add(new OldToNew(artifact, task));
    }

    // todo: rplace default artifact publication set?
    outgoing.getArtifacts().clear();
    for (final OldToNew artifact : newArtifacts) {
      outgoing.artifact(artifact.newOutput);
    }
  }

  @Override
  public void signDefaultConfigurations() {
    this.signConfigurationOutgoing(this.configurations.getByName(JavaPlugin.RUNTIME_ELEMENTS_CONFIGURATION_NAME));
    this.signConfigurationOutgoing(this.configurations.getByName(JavaPlugin.API_ELEMENTS_CONFIGURATION_NAME));
  }

  private @Nullable TaskProvider<SignJarTask> tryExtract(final PublishArtifact artifact) {
    if (LazyArtifactFields.LAZY_PUBLISH_ARTIFACT == null || !LazyArtifactFields.LAZY_PUBLISH_ARTIFACT.isInstance(artifact)) {
      return null;
    }

    return this.tryExtractLazy(artifact);
  }

  @SuppressWarnings("unchecked")
  private @Nullable TaskProvider<SignJarTask> tryExtractLazy(final PublishArtifact artifact) {
    if (LazyArtifactFields.PROVIDER_FIELD == null) {
      return null;
    }

    final Provider<?> source;
    try {
      source = (Provider<?>) LazyArtifactFields.PROVIDER_FIELD.get(artifact);
    } catch (final IllegalAccessException ex) {
      LOGGER.warn("Failed to extract the provider from artifact {}", artifact.getName(), ex);
      return null;
    }

    if (!(source instanceof TaskProvider<?>)) {
      return null;
    }

    return this.sign(((TaskProvider<? extends AbstractArchiveTask>) source));
  }

  private static String signName(final String origName) {
    final StringBuilder ret = new StringBuilder(origName.length() + 7)
      .append("jarsign");
    return capitalize(origName, ret)
      .toString();
  }

  private static String capitalize(final String string) {
    if (string.isEmpty()) {
      return string;
    }

    return capitalize(string, new StringBuilder(string.length())).toString();
  }

  private static StringBuilder capitalize(final String string, final StringBuilder target) {
    if (string.isEmpty()) {
      return target;
    }

    final int initialChar = string.codePointAt(0);
    final int offset;
    if (Character.isBmpCodePoint(initialChar)) {
      offset = 1;
    } else {
      offset = 2;
    }

    return target
      .appendCodePoint(Character.toUpperCase(initialChar))
      .append(string, offset, string.length());
  }

  static class LazyArtifactFields {
    static final Class<?> LAZY_PUBLISH_ARTIFACT;
    static final Field PROVIDER_FIELD;

    static {
      Class<?> lazyArtifact;
      Field provider;
      try {
        lazyArtifact = Class.forName("org.gradle.api.internal.artifacts.dsl.LazyPublishArtifact");
        provider = lazyArtifact.getDeclaredField("provider");
        provider.setAccessible(true);
      } catch (final ClassNotFoundException | NoSuchFieldException ex) {
        JarSignerExtensionImpl.LOGGER.info("Could not locate LazyPublishArtifact class, jar signer performance will be degraded", ex);
        lazyArtifact = null;
        provider = null;
      }

      LAZY_PUBLISH_ARTIFACT = lazyArtifact;
      PROVIDER_FIELD = provider;
    }
  }

  // Mapping from old artifact to replaced artifact
  static final class OldToNew {
    final PublishArtifact oldArtifact;
    final Object newOutput;

    OldToNew(final PublishArtifact oldArtifact, final Object newOutput) {
      this.oldArtifact = oldArtifact;
      this.newOutput = newOutput;
    }
  }
}
