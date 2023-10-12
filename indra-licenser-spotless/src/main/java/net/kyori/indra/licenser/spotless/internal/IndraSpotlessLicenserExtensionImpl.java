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
package net.kyori.indra.licenser.spotless.internal;

import com.diffplug.gradle.spotless.FormatExtension;
import com.diffplug.spotless.ThrowingEx;
import groovy.text.SimpleTemplateEngine;
import java.io.BufferedReader;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javax.inject.Inject;
import net.kyori.indra.licenser.spotless.HeaderFormat;
import net.kyori.indra.licenser.spotless.HeaderFormatApplier;
import net.kyori.indra.licenser.spotless.IndraSpotlessLicenserExtension;
import net.kyori.mammoth.Properties;
import org.gradle.api.Action;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.MapProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.resources.TextResource;
import org.gradle.api.resources.TextResourceFactory;
import org.jetbrains.annotations.NotNull;

import static java.util.Objects.requireNonNull;

public class IndraSpotlessLicenserExtensionImpl implements IndraSpotlessLicenserExtension {
  private static final Pattern LINE_SPLIT = Pattern.compile("\r?\n");

  private final ObjectFactory objects;
  private final TextResourceFactory textResources;

  private final Property<TextResource> licenseHeaderFile;
  private final Property<HeaderFormat> headerFormat;
  private final MapProperty<String, HeaderFormat> languageFormatOverrides;
  private final MapProperty<String, Object> properties;
  private final Property<Boolean> newLine;
  private final List<Action<FormatExtension.LicenseHeaderConfig>> extraConfigureSteps;

  @Inject
  public IndraSpotlessLicenserExtensionImpl(final ObjectFactory objects, final TextResourceFactory textResources) {
    this.objects = objects;
    this.textResources = textResources;

    this.licenseHeaderFile = objects.property(TextResource.class);
    this.headerFormat = objects.property(HeaderFormat.class).convention(HeaderFormat.starSlash());
    this.languageFormatOverrides = objects.mapProperty(String.class, HeaderFormat.class);
    this.properties = objects.mapProperty(String.class, Object.class);
    this.newLine = objects.property(Boolean.class).convention(false);
    this.extraConfigureSteps = new ArrayList<>();
  }

  @Override
  public @NotNull Property<TextResource> licenseHeaderFile() {
    return this.licenseHeaderFile;
  }

  @Override
  public void licenseHeaderFile(final @NotNull Object file) {
    this.licenseHeaderFile.set(this.textResources.fromFile(file, "UTF-8"));
  }

  @Override
  public @NotNull Property<HeaderFormat> headerFormat() {
    return this.headerFormat;
  }

  @Override
  public @NotNull MapProperty<String, HeaderFormat> languageFormatOverrides() {
    return this.languageFormatOverrides;
  }

  @Override
  public void languageFormatOverride(final @NotNull String language, final @NotNull HeaderFormat headerFormat) {
    this.languageFormatOverrides.put(language, headerFormat);
  }

  @Override
  public void languageFormatOverride(final @NotNull String language, final @NotNull Action<HeaderFormatApplier> configurer) {
    final Property<HeaderFormat> headerFormat = this.objects.property(HeaderFormat.class);
    this.languageFormatOverrides.put(language, headerFormat);
    requireNonNull(configurer, "configurer").execute(new HeaderFormatApplierImpl(headerFormat));
  }

  @Override
  public @NotNull MapProperty<String, Object> properties() {
    return this.properties;
  }

  @Override
  public @NotNull Property<Boolean> newLine() {
    return this.newLine;
  }

  @Override
  public void extraConfig(final @NotNull Action<FormatExtension.LicenseHeaderConfig> configureStep) {
    this.extraConfigureSteps.add(requireNonNull(configureStep, "configureStep"));
  }

  public List<Action<FormatExtension.LicenseHeaderConfig>> extraConfigSteps() {
    return Collections.unmodifiableList(this.extraConfigureSteps);
  }

  public ThrowingEx.Supplier<String> createHeaderSupplier(final String name) {
    return () -> {
      // Read
      final File licenseHeaderFile = this.licenseHeaderFile().get().asFile("UTF-8");
      final String licenseHeader;
      try (final BufferedReader reader = Files.newBufferedReader(licenseHeaderFile.toPath(), StandardCharsets.UTF_8)) {

        final Map<String, Object> properties = Properties.finalized(this.properties()).get();
        if (!properties.isEmpty()) {
          final Map<String, Object> templateParams = new HashMap<>(properties);
          templateParams.putIfAbsent("YEAR", "$YEAR");

          licenseHeader = new SimpleTemplateEngine().createTemplate(reader)
            .make(templateParams)
            .toString();
        } else {
          licenseHeader = new String(Files.readAllBytes(licenseHeaderFile.toPath()), StandardCharsets.UTF_8);
        }
      }

      final HeaderFormat format = Properties.finalized(this.languageFormatOverrides).get().getOrDefault(name, Properties.finalized(this.headerFormat()).get());

      // Apply header format to contents
      return formatHeader(licenseHeader, format, true, Properties.finalized(this.newLine()).get()); // todo: expose trim option
    };
  }

  private static String formatHeader(final String header, final HeaderFormat format, final boolean trimBody, final boolean newLine) {
    final String lineSeparator = System.lineSeparator();
    // Apply header format to contents
    final String prefix = format.begin() != null ? format.begin() + lineSeparator : "";
    String suffix;
    if (format.end() != null) {
      suffix = lineSeparator + format.end() + lineSeparator;
    } else {
      suffix = lineSeparator;
    }
    if (newLine) {
      suffix = suffix + lineSeparator;
    }
    return LINE_SPLIT.splitAsStream(header)
      .map(line -> {
        if (format.linePrefix() != null || format.lineSuffix() != null) {
          final StringBuilder builder = new StringBuilder(line.length() + 4);
          if (format.linePrefix() != null) {
            builder.append(format.linePrefix());
          }
          builder.append(line);
          if (format.lineSuffix() != null) {
            builder.append(format.lineSuffix());
          }

          return trimBody ? trimEnd(builder.toString()) : builder.toString();
        } else {
          return line;
        }
      })
      .collect(Collectors.joining(
        lineSeparator,
        prefix,
        suffix
      ));
  }

  private static String trimEnd(final String input) {
    int i;
    for (i = input.length() - 1; i >= 0; i--) {
      if (!Character.isWhitespace(input.charAt(i))) {
        break;
      }
    }

    return input.substring(0, i + 1);
  }
}
