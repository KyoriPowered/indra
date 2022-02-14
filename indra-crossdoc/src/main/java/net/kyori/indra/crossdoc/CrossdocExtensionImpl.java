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
package net.kyori.indra.crossdoc;

import javax.inject.Inject;
import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Property;
import org.gradle.api.provider.Provider;
import org.gradle.api.provider.ProviderFactory;

class CrossdocExtensionImpl implements CrossdocExtension {
  private final ObjectFactory objects;
  private final Property<String> baseUrlProperty;
  private final Property<ProjectDocumentationUrlProvider> docUrlProviderProperty;
  private final Provider<String> projectVersionProvider;

  @Inject
  public CrossdocExtensionImpl(final ObjectFactory objects, final ProviderFactory providers, final Project project) {
    this.objects = objects;
    this.baseUrlProperty = objects.property(String.class);
    this.docUrlProviderProperty = objects.property(ProjectDocumentationUrlProvider.class);
    this.projectVersionProvider = providers.provider(() -> String.valueOf(project.getVersion()));

    final NameBasedProjectDocumentationUrlProvider defaultProvider = this.objects.newInstance(NameBasedProjectDocumentationUrlProviderImpl.class);
    defaultProvider.getVersion().convention(this.projectVersionProvider);
    this.docUrlProviderProperty.convention(defaultProvider);
  }

  @Override
  public Property<ProjectDocumentationUrlProvider> projectDocumentationUrlProvider() {
    return this.docUrlProviderProperty;
  }

  @Override
  public Property<String> baseUrl() {
    return this.baseUrlProperty;
  }

  @Override
  public void nameBasedDocumentationUrlProvider(final Action<? super NameBasedProjectDocumentationUrlProvider> action) {
    final NameBasedProjectDocumentationUrlProvider provider = this.objects.newInstance(NameBasedProjectDocumentationUrlProviderImpl.class);
    provider.getVersion().convention(this.projectVersionProvider);
    action.execute(provider);
    this.docUrlProviderProperty.set(provider);
  }
}
