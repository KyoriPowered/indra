/*
 * This file is part of indra, licensed under the MIT License.
 *
 * Copyright (c) 2020-2021 KyoriPowered
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
package net.kyori.indra.sonatype.internal;

import io.github.gradlenexus.publishplugin.NexusPublishExtension;
import io.github.gradlenexus.publishplugin.NexusRepository;
import java.net.URI;
import javax.inject.Inject;
import net.kyori.indra.sonatype.IndraSonatypePublishingExtension;

public class IndraSonatypePublishingExtensionImpl implements IndraSonatypePublishingExtension {
  private static final String SONATYPE_REPO = "sonatype";

  private final NexusPublishExtension nexusExtension;

  @Inject
  public IndraSonatypePublishingExtensionImpl(final NexusPublishExtension nexusExtension) {
    this.nexusExtension = nexusExtension;
  }

  @Override
  public void useAlternateSonatypeOSSHost(final String name) {
    final NexusRepository repo = this.nexusExtension.getRepositories().maybeCreate(SONATYPE_REPO);

    repo.getNexusUrl().set(this.nexusUrl(name));
    repo.getSnapshotRepositoryUrl().set(this.snapshotUrl(name));
  }

  private String domain(final String prefix) {
    return String.format("https://%soss.sonatype.org/", prefix == null || prefix.isEmpty() ? "" : prefix + '.');
  }

  private URI nexusUrl(final String prefix) {
    return URI.create(this.domain(prefix) + "service/local/");
  }

  private URI snapshotUrl(final String prefix) {
    return URI.create(this.domain(prefix) + "content/repositories/snapshots/");
  }
}
