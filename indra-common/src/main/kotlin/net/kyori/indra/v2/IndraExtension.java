/*
 * This file is part of indra, licensed under the MIT License.
 *
 * Copyright (c) 2020 KyoriPowered
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
package net.kyori.indra.v2;

import javax.inject.Inject;
import net.kyori.indra.api.model.ContinuousIntegration;
import net.kyori.indra.api.model.Issues;
import net.kyori.indra.api.model.License;
import net.kyori.indra.api.model.SourceCodeManagement;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Property;

public class IndraExtension {
  public final Property<ContinuousIntegration> ci;
  public final Property<Issues> issues;
  public final Property<License> license;
  public final Property<SourceCodeManagement> scm;

  public final Property<Boolean> reproducibleBuilds;

  public final Property<String> checkstyle;

  @Inject
  public IndraExtension(final ObjectFactory objects) {
    this.ci = objects.property(ContinuousIntegration.class);
    this.issues = objects.property(Issues.class);
    this.license = objects.property(License.class);
    this.scm = objects.property(SourceCodeManagement.class);

    this.reproducibleBuilds = objects.property(Boolean.class).convention(true);

    this.checkstyle = objects.property(String.class).convention("8.37");
  }
}
