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
import org.gradle.api.Action;
import org.gradle.api.provider.MapProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.provider.Provider;
import org.gradle.api.resources.TextResource;
import org.jetbrains.annotations.NotNull;

import static java.util.Objects.requireNonNull;

public interface SpotlessLicenserExtension {

  @NotNull Property<TextResource> licenseHeaderFile();

  void licenseHeaderFile(final @NotNull Object file);

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

  @NotNull MapProperty<String, HeaderFormat> languageFormatOverrides();

  void languageFormatOverride(final @NotNull String language, final @NotNull HeaderFormat headerFormat);

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
   * Add an extra configure step to modify applied license header configurations.
   *
   * @param configureStep the extra configuration step
   * @since 2.2.0
   */
  void extraConfig(final @NotNull Action<FormatExtension.LicenseHeaderConfig> configureStep);
}
