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
package net.kyori.indra.licenser.spotless;

import com.diffplug.gradle.spotless.FormatExtension;
import groovy.text.SimpleTemplateEngine;
import net.kyori.indra.licenser.spotless.internal.HeaderFormatApplierImpl;
import org.gradle.api.Action;
import org.gradle.api.provider.MapProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.provider.Provider;
import org.gradle.api.resources.TextResource;
import org.jetbrains.annotations.NotNull;

import static java.util.Objects.requireNonNull;

/**
 * Configuration options determining how license header contents should be applied to Spotless's licenser.
 *
 * @since 2.2.0
 */
public interface SpotlessLicenserExtension {

  /**
   * A property holding the file to use for a license header.
   *
   * @return the license header file property
   * @since 2.2.0
   */
  @NotNull Property<TextResource> licenseHeaderFile();

  /**
   * Set the license header file to use.
   *
   * <p>The contents of this file will be processed to apply a comment style and expand template parameters.</p>
   *
   * @param file the file to read, via {@code Project.file()}
   * @since 2.2.0
   */
  void licenseHeaderFile(final @NotNull Object file);

  /**
   * A property containing the header format to use.
   *
   * <p>Language-specific overrides can be set using {@link #languageFormatOverride(String, HeaderFormat)}.</p>
   *
   * @return the header format property
   * @since 2.2.0
   */
  @NotNull Property<HeaderFormat> headerFormat();

  /**
   * Set the header format to use.
   *
   * @param format the header format
   * @since 2.2.0
   */
  default void headerFormat(final @NotNull HeaderFormat format) {
    this.headerFormat().set(requireNonNull(format, "format"));
  }

  /**
   * Set the header format to use.
   *
   * @param configurer an action that will be passed callbacks for common header format presets
   * @since 2.2.0
   */
  default void headerFormat(final @NotNull Action<HeaderFormatApplier> configurer) {
    requireNonNull(configurer, "configurer").execute(new HeaderFormatApplierImpl(this.headerFormat()));
  }

  /**
   * A property providing language-specific header format overrides.
   *
   * @return the map property containing overrides
   * @since 2.2.0
   */
  @NotNull MapProperty<String, HeaderFormat> languageFormatOverrides();

  /**
   * Set a language format override for a specific formatter task.
   *
   * @param language the formatter task to configure
   * @param headerFormat the header format to apply
   * @since 2.2.0
   */
  void languageFormatOverride(final @NotNull String language, final @NotNull HeaderFormat headerFormat);

  /**
   * Set a language format override for a specific formatter task.
   *
   * @param language the formatter task to configure
   * @param configurer an action that will be passed callbacks for common header format presets
   * @since 2.2.0
   */
  void languageFormatOverride(final @NotNull String language, final @NotNull Action<HeaderFormatApplier> configurer);

  /**
   * Properties to replace within license header contents.
   *
   * <p>The Groovy {@link SimpleTemplateEngine} is used to pre-process license headers.</p>
   *
   * @return the properties map
   * @since 2.2.0
   */
  @NotNull MapProperty<String, Object> properties();

  /**
   * Add a property to be expanded.
   *
   * @param key the property key
   * @param value the value to resolve to
   * @return this extension, for chaining
   * @since 2.2.0
   */
  default @NotNull SpotlessLicenserExtension property(final @NotNull String key, final @NotNull String value) {
    this.properties().put(key, value);
    return this;
  }

  /**
   * Add a property to be expanded.
   *
   * @param key the property key
   * @param value the value provider to resolve to
   * @return this extension, for chaining
   * @since 2.2.0
   */
  default @NotNull SpotlessLicenserExtension property(final @NotNull String key, final @NotNull Provider<?> value) {
    this.properties().put(key, value);
    return this;
  }

  /**
   * Get whether to append an additional newline at the end of files.
   *
   * @return the newline property
   * @since 2.2.0
   */
  @NotNull Property<Boolean> newLine();

  /**
   * Get whether to append an additional newline at the end of files.
   *
   * @param newLine the newline value
   * @since 2.2.0
   */
  default void newLine(final boolean newLine) {
    this.newLine().set(newLine);
  }

  /**
   * Add an extra configure step to modify applied license header configurations.
   *
   * @param configureStep the extra configuration step
   * @since 2.2.0
   */
  void extraConfig(final @NotNull Action<FormatExtension.LicenseHeaderConfig> configureStep);
}
