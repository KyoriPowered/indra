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
package net.kyori.indra;

import java.util.Set;
import net.kyori.indra.internal.AbstractIndraPublishingPlugin;
import org.gradle.api.Action;
import org.gradle.api.publish.PublishingExtension;
import org.gradle.api.publish.maven.MavenPublication;

public class IndraPublishingPlugin extends AbstractIndraPublishingPlugin {
  @Override
  protected void applyPublishingActions(final PublishingExtension extension, final Set<Action<MavenPublication>> actions) {
    extension.getPublications().named(Indra.PUBLICATION_NAME, MavenPublication.class).configure(publication -> {
      for (final Action<MavenPublication> action : actions) {
        action.execute(publication);
      }
    });
  }

  @Override
  protected void configurePublications(final PublishingExtension extension, final Action<MavenPublication> action) {
    extension.getPublications().register(Indra.PUBLICATION_NAME, MavenPublication.class, action);
  }
}
