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

import java.io.File;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.file.RegularFile;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.TaskProvider;
import org.gradle.api.tasks.bundling.AbstractArchiveTask;
import org.jetbrains.annotations.NotNull;

/**
 * Configuration for the jar signer plugin.
 *
 * @see <a href="https://docs.oracle.com/en/java/javase/17/docs/specs/man/jarsigner.html">jarsigner CLI reference</a>
 * @since 3.1.0
 */
public interface JarSignerExtension {
  boolean DEFAULT_STRICT = false;
  String DEFAULT_STORE_FORMAT = "PKCS12";

  // Jarsigner properties //

  /**
   * Holds the alias within a key store.
   *
   * @return the alias property
   * @since 3.1.0
   */
  @NotNull Property<String> alias();

  /**
   * Set the alias within a key store.
   *
   * @param alias the key alias
   * @since 3.1.0
   */
  default void alias(final @NotNull String alias) {
    this.alias().set(alias);
  }

  /**
   * Set the alias within a key store.
   *
   * @param alias the key alias
   * @since 3.1.0
   */
  default void alias(final @NotNull Provider<? extends String> alias) {
    this.alias().set(alias);
  }

  /**
   * Holds the file containing the keystore.
   *
   * @return the key store file property
   * @since 3.1.0
   */
  @NotNull RegularFileProperty keyStore();

  /**
   * Set the file containing the keystore.
   *
   * @param keyStore the key store file
   * @since 3.1.0
   */
  default void keyStore(final @NotNull File keyStore) {
    this.keyStore().set(keyStore);
  }

  /**
   * Set the file containing the keystore.
   *
   * @param keyStore the key store file
   * @since 3.1.0
   */
  default void keyStore(final @NotNull Provider<? extends RegularFile> keyStore) {
    this.keyStore().set(keyStore);
  }

  /**
   * Holds the password for the keystore.
   *
   * @return the key store password property
   * @since 3.1.0
   */
  @NotNull Property<String> storePassword();

  /**
   * Set the password for the keystore.
   *
   * @param storePassword the key store password
   * @since 3.1.0
   */
  default void storePassword(final @NotNull String storePassword) {
    this.storePassword().set(storePassword);
  }

  /**
   * Set the password for the keystore.
   *
   * @param storePassword the key store password
   * @since 3.1.0
   */
  default void storePassword(final @NotNull Provider<String> storePassword) {
    this.storePassword().set(storePassword);
  }

  /**
   * Holds the password for the key in the keystore.
   *
   * <p>This parameter is optional for some store formats (including the default, PKCS12).</p>
   *
   * @return the key store password property
   * @since 3.1.0
   */
  @NotNull Property<String> keyPassword();

  /**
   * Set the password for the key in the keystore.
   *
   * @param keyPassword the key password
   * @since 3.1.0
   */
  default void keyPassword(final @NotNull String keyPassword) {
    this.keyPassword().set(keyPassword);
  }

  /**
   * Set the password for the key in the keystore.
   *
   * @param keyPassword the key password
   * @since 3.1.0
   */
  default void keyPassword(final @NotNull Provider<String> keyPassword) {
    this.keyPassword().set(keyPassword);
  }

  /**
   * Holds whether the jarsigner should be run in 'strict' mode.
   *
   * <p>By default, this is {@value #DEFAULT_STRICT}.</p>
   *
   * @return the strict mode property
   * @since 3.1.0
   */
  @NotNull Property<Boolean> strict();

  /**
   * Set whether the jarsigner should be run in 'strict' mode.
   *
   * @param strict the strict mode value
   * @since 3.1.0
   */
  default void strict(final boolean strict) {
    this.strict().set(strict);
  }

  /**
   * Set whether the jarsigner should be run in 'strict' mode.
   *
   * @param strict the strict mode value
   * @since 3.1.0
   */
  default void strict(final @NotNull Provider<Boolean> strict) {
    this.strict().set(strict);
  }

  /**
   * Holds the identifier for the format of the key store file.
   *
   * <p>By default, this is {@value #DEFAULT_STORE_FORMAT}.</p>
   *
   * @return the key store format property
   * @since 3.1.0
   */
  @NotNull Property<String> storeFormat();

  /**
   * Set the identifier for the format of the key store file.
   *
   * @param storeFormat the key store format
   * @since 3.1.0
   */
  default void storeFormat(final @NotNull String storeFormat) {
    this.storeFormat().set(storeFormat);
  }

  /**
   * Set the identifier for the format of the key store file.
   *
   * @param storeFormat the key store format
   * @since 3.1.0
   */
  default void storeFormat(final @NotNull Provider<String> storeFormat) {
    this.storeFormat().set(storeFormat);
  }

  /**
   * Create a task signing the output of an existing task.
   *
   * <p>If the input task's classifier does not already end with {@code unsigned}, the input task will be modified to add this prefix.</p>
   *
   * @param task the tasks whose output should be signed
   * @return the new sign task
   * @since 3.1.0
   */
  TaskProvider<SignJarTask> sign(final TaskProvider<? extends AbstractArchiveTask> task);

  /**
   * Create a task signing the outgoing artifact of an existing configuration.
   *
   * <p>If the input task's classifier does not already end with {@code unsigned}, the input task will be modified to add this prefix.</p>
   *
   * @param configuration the configuration whose outgoing artifacts should be signed
   * @return the new sign task
   * @since 3.1.0
   */
  TaskProvider<SignJarTask> signConfigurationOutgoing(final Configuration configuration);
}
