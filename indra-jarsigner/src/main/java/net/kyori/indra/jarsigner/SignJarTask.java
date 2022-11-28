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
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableEntryException;
import java.security.cert.CertificateException;
import java.util.Map;
import javax.inject.Inject;
import org.gradle.api.GradleException;
import org.gradle.api.InvalidUserDataException;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.MapProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.CacheableTask;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.PathSensitive;
import org.gradle.api.tasks.PathSensitivity;
import org.gradle.jvm.tasks.Jar;
import org.gradle.jvm.toolchain.JavaLauncher;
import org.gradle.workers.WorkAction;
import org.gradle.workers.WorkParameters;
import org.gradle.workers.WorkQueue;
import org.gradle.workers.WorkerExecutor;
import org.jetbrains.annotations.ApiStatus;

/**
 * Execute the {@code jarsigner} tool.
 *
 * <p>This is not strictly a compliant {@link Jar} subtype -- it takes one input that is a jar file to sign,
 * which and has a signed output. All jar task options besides source and target are ignored.</p>
 *
 * <p>All properties passed to the key signer get their defaults from the project's {@link JarSignerExtension}.</p>
 *
 * @since 3.1.0
 */
@CacheableTask
public abstract class SignJarTask extends Jar {
  /**
   * Get the alias within a key store.
   *
   * @return the alias property
   * @since 3.1.0
   */
  @Input
  public abstract Property<String> getAlias();

  /**
   * Get the password for the keystore.
   *
   * @return the key store password property
   * @since 3.1.0
   */
  @Input
  public abstract Property<String> getStorePassword();

  /**
   * Get the password for the specific key in the keystore.
   *
   * <p>This parameter is not used in formats such as PKCS12, which do not have separate key and store passwords.</p>
   *
   * @return the key store password property
   * @since 3.1.0
   */
  @Input
  @Optional
  public abstract Property<String> getKeyPassword();

  /**
   * Get the file containing the keystore.
   *
   * @return the key store file
   * @since 3.1.0
   */
  @InputFile
  @PathSensitive(PathSensitivity.ABSOLUTE)
  public abstract RegularFileProperty getKeyStore();

  /**
   * Get whether the jarsigner should be run in 'strict' mode.
   *
   * @return the strict mode property
   * @since 3.1.0
   */
  @Input
  public abstract Property<Boolean> getStrict();

  /**
   * Get the identifier for the format of the key store file.
   *
   * @return the key store format property
   * @since 3.1.0
   */
  @Input
  public abstract Property<String> getStoreFormat();

  /**
   * Get the java runtime to be used to execute the jarsigner tool.
   *
   * @return the jarsigner tool launcher provider
   * @since 3.1.0
   */
  @Input
  @Optional
  public abstract Property<JavaLauncher> getJavaLauncher();

  /**
   * Get extra properties that would be passed to the jar signer tool.
   *
   * @return the extra properties property
   * @since 3.1.0
   */
  @Input
  @Optional
  public abstract MapProperty<String, String> getExtraProperties();

  @Inject
  protected abstract WorkerExecutor getWorkerExecutor();

  public SignJarTask() {
  }

  @Override
  public void copy() {
    final File file = this.getSource().getSingleFile();
    final File destination = this.getArchiveFile().get().getAsFile();
    final Map<String, String> args = this.getExtraProperties().get();

    final JavaLauncher launcher = this.getJavaLauncher().getOrNull();
    final int javaVersion;
    if (launcher == null) {
      final String versionStr = System.getProperty("java.version");
      javaVersion = Integer.parseInt(versionStr.startsWith("1.") ? versionStr.substring(2) : versionStr);
    } else {
      javaVersion = launcher.getMetadata().getLanguageVersion().asInt();
    }

    if (javaVersion < 9) {
      throw new GradleException("The SignJar task requires a Java 9+ runtime to be executed.\n" +
        "Please run Gradle with a newer Java version or configure a toolchain for the task.");
    }

    final WorkQueue queue;
    if (launcher != null) {
      queue = this.getWorkerExecutor().processIsolation(spec -> {
        spec.forkOptions(opts -> opts.setExecutable(launcher.getExecutablePath().getAsFile()));
      });
    } else {
      queue = this.getWorkerExecutor().noIsolation();
    }

    queue.submit(SignJarAction.class, params -> {
      params.getSource().set(file);
      params.getTarget().set(destination);
      params.getKeyStoreFile().set(this.getKeyStore().getAsFile());
      params.getKeyStoreAlias().set(this.getAlias());
      params.getKeyStoreType().set(this.getStoreFormat());
      params.getKeyStorePassword().set(this.getStorePassword());
      params.getKeyPassword().set(this.getKeyPassword());
      params.getArgs().set(args);
    });

    queue.await();
  }

  /**
   * The action that actually performs the jar signing operation.
   *
   * @since 3.1.0
   */
  @ApiStatus.Internal
  public static abstract class SignJarAction implements WorkAction<SignJarAction.Params> {
    interface Params extends WorkParameters {
      Property<File> getSource();

      Property<File> getTarget();

      Property<File> getKeyStoreFile();

      Property<String> getKeyStoreType();

      Property<String> getKeyStoreAlias();

      Property<String> getKeyStorePassword();

      Property<String> getKeyPassword();

      MapProperty<String, String> getArgs();
    }

    private KeyStore.PrivateKeyEntry loadPrivateKey() {
      final Params params = this.getParameters();

      final KeyStore keyStore;
      try {
        keyStore = KeyStore.getInstance(params.getKeyStoreType().get());
      } catch (final KeyStoreException ex) {
        throw new InvalidUserDataException("Invalid key store type " + params.getKeyStoreType().get(), ex);
      }
      try (final InputStream is = new FileInputStream(params.getKeyStoreFile().get())) {
        keyStore.load(is, params.getKeyStorePassword().get().toCharArray());
      } catch (final CertificateException | IOException | NoSuchAlgorithmException ex) {
        throw new InvalidUserDataException("Failed to load key store file from " + params.getKeyStoreFile().get());
      }

      final KeyStore.Entry potentialEntry; // or a password?
      try {
        final KeyStore.ProtectionParameter param;
        if (this.getParameters().getKeyPassword().isPresent()) {
          param = new KeyStore.PasswordProtection(this.getParameters().getKeyPassword().get().toCharArray());
        } else {
          param = null;
        }

        potentialEntry = keyStore.getEntry(params.getKeyStoreAlias().get(), param);
      } catch (final NoSuchAlgorithmException | UnrecoverableEntryException | KeyStoreException ex) {
        throw new InvalidUserDataException("Unable to read entry '" + params.getKeyStoreAlias().get() + " from key store");
      }
      if (!(potentialEntry instanceof KeyStore.PrivateKeyEntry)) {
        throw new InvalidUserDataException("Key store entry " + params.getKeyStoreAlias().get()
          + " was expected to be a private key, but was a " + potentialEntry + " instead.");
      }

      return (KeyStore.PrivateKeyEntry) potentialEntry;
    }

    @Override
    public void execute() {
      final Params params = this.getParameters();
      final KeyStore.PrivateKeyEntry signingKey = this.loadPrivateKey();

      try {
        Class.forName("net.kyori.indra.jarsigner.JarSignerInvoker")
          .getMethod("execute", File.class, File.class, KeyStore.PrivateKeyEntry.class, Map.class)
          .invoke(null, params.getSource().get(), params.getTarget().get(), signingKey, params.getArgs().get());
      } catch (final InvocationTargetException ex) {
        throw new GradleException("Failed to invoke jar signer", ex.getCause());
      } catch (final IllegalAccessException | NoSuchMethodException | ClassNotFoundException ex) {
        throw new GradleException("Failed to launch jar signer invoker shim", ex);
      }
    }
  }
}
