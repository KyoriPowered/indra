/*
 * This file is part of indra, licensed under the MIT License.
 *
 * Copyright (c) 2020-2024 KyoriPowered
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
package net.kyori.indra.git;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import net.kyori.indra.git.internal.GitCache;
import net.kyori.indra.git.internal.IndraGitExtensionImpl;
import net.kyori.indra.test.IndraTesting;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.gradle.api.Project;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class IndraGitPluginTest {
  private static final String PLUGIN = "net.kyori.indra.git";
  private static final String DEFAULT_BRANCH = "trunk";
  private static final PersonIdent COMMITTER = new PersonIdent("CI", "noreply@kyori.net");

  @TempDir
  private Path projectDir;

  private GitCache.GitProvider prov;

  @BeforeEach
  void setupGitCache() {
    this.prov = GitCache.getOrCreate(this.projectDir.toFile());
  }

  @AfterEach
  void tearDownGitCache() {
    GitCache.close(this.prov);
    this.prov = null;
  }

  @Test
  void testPluginSimplyApplies() {
    final Project project = IndraTesting.project();
    project.getPluginManager().apply(PLUGIN);
  }

  @Test
  void testNoRepositoryDetected() {
    final IndraGitExtension extension = this.createExtension();
    assertFalse(extension.isPresent());
  }

  @Test
  void testRepositoryDetected() throws IOException, GitAPIException {
    final IndraGitExtension extension = this.createExtensionAndRepo();
    assertTrue(extension.isPresent());
  }

  @Test
  void testRepositoryDetectedOnSubprojects() throws IOException, GitAPIException {
    initRepo(this.projectDir);
    final Project base = this.createProject();
    final Project subproject = IndraTesting.project(b -> b.withParent(base));
    subproject.getPlugins().apply(PLUGIN);

    assertTrue(subproject.getExtensions().getByType(IndraGitExtension.class).isPresent());
  }

  @Test
  void testRepositoryIsDetectedWhenOnlyAppliedToSubprojects() throws IOException, GitAPIException {
    initRepo(this.projectDir);
    final Project base = IndraTesting.project(b -> b.withProjectDir(this.projectDir.toFile()));
    final Project subproject = IndraTesting.project(b -> b.withParent(base));
    subproject.getPlugins().apply(PLUGIN);

    assertTrue(subproject.getExtensions().getByType(IndraGitExtension.class).isPresent());
  }

  @Test
  void testRepositoryDetectedThroughSubmodule() throws IOException, GitAPIException {
    final Path mainProject = this.projectDir.resolve("main");
    final Path submodule = this.projectDir.resolve("submodule");
    Files.createDirectories(mainProject);
    Files.createDirectories(submodule);

    IndraTesting.exec(mainProject, "git", "init");
    IndraTesting.exec(submodule, "git", "init");
    try (final Git git = Git.open(submodule.toFile())) {
      git.commit()
        .setAllowEmpty(true)
        .setMessage("initial commit")
        .setAuthor(COMMITTER)
        .setCommitter(COMMITTER)
        .call();
    }

    IndraTesting.exec(mainProject, "git", "-c", "protocol.file.allow=always", "submodule", "add", "../submodule", "child");

    final Path referencedProjectDir = mainProject.resolve("child");
    final Project inSubmodule = IndraTesting.project(b -> b.withProjectDir(referencedProjectDir.toFile()));
    inSubmodule.getPluginManager().apply(PLUGIN);

    assertTrue(inSubmodule.getExtensions().getByType(IndraGitExtension.class).isPresent());
  }

  @Test
  void testHeadTagNullWhenNotCheckedOutToTag() throws IOException, GitAPIException {
    final IndraGitExtensionImpl extension = this.createExtensionAndRepo();
    assertFalse(extension.headTag().isPresent());

    Files.write(this.projectDir.resolve("test.properties"), Collections.singletonList("boink"), StandardCharsets.UTF_8);

    extension.git().commit()
      .setAll(true)
      .setMessage("Initial commit")
      .setCommitter(COMMITTER)
      .call();

    extension.git().tag()
      .setName("v1")
      .setAnnotated(false)
      .call();

    // Then move past the tag

    Files.write(this.projectDir.resolve("test.properties"), Collections.singletonList("blah"), StandardCharsets.UTF_8);

    extension.git().commit()
      .setAll(true)
      .setMessage("stage 2")
      .setCommitter(COMMITTER)
      .call();

    assertFalse(extension.headTag().isPresent());
  }

  @Test
  void testHeadTag() throws IOException, GitAPIException {
    final IndraGitExtensionImpl extension = this.createExtensionAndRepo();

    Files.write(this.projectDir.resolve("test.properties"), Collections.singletonList("boink"), StandardCharsets.UTF_8);

    extension.git().commit()
      .setAll(true)
      .setMessage("Initial commit")
      .setCommitter(COMMITTER)
      .call();

    extension.git().tag()
      .setName("v1")
      .setAnnotated(false)
      .call();

    assertEquals("v1", Repository.shortenRefName(extension.headTag().get().getName()));
  }

  @Test
  void testAnnotatedHeadTag() throws IOException, GitAPIException {
    final IndraGitExtensionImpl extension = this.createExtensionAndRepo();
    Files.write(this.projectDir.resolve("test.properties"), Collections.singletonList("boink"), StandardCharsets.UTF_8);

    extension.git().commit()
      .setAll(true)
      .setMessage("Initial commit")
      .setCommitter(COMMITTER)
      .call();

    extension.git().tag()
      .setName("v1")
      .setMessage("Release v1")
      .setTagger(COMMITTER)
      .setAnnotated(true)
      .call();

    assertEquals("v1", Repository.shortenRefName(extension.headTag().get().getName()));
  }

  @Test
  void testBranchOnInitialCommit() throws IOException, GitAPIException {
    final IndraGitExtension extension = this.createExtensionAndRepo();
    assertEquals(DEFAULT_BRANCH, extension.branchName().get());
  }

  @Test
  void testBranch() throws IOException, GitAPIException {
    final IndraGitExtensionImpl extension = this.createExtensionAndRepo();
    Files.write(this.projectDir.resolve("test.properties"), Collections.singletonList("boink"), StandardCharsets.UTF_8);

    extension.git().commit()
      .setAll(true)
      .setMessage("Initial commit")
      .setCommitter(COMMITTER)
      .call();

    assertEquals(DEFAULT_BRANCH, extension.branchName().get());
  }

  @Test
  void testBranchOnDetachedHead() throws IOException, GitAPIException {
    final IndraGitExtensionImpl extension = this.createExtensionAndRepo();
    Files.write(this.projectDir.resolve("test.properties"), Collections.singletonList("boink"), StandardCharsets.UTF_8);

    final RevCommit commit = extension.git().commit()
      .setAll(true)
      .setMessage("Initial commit")
      .setCommitter(COMMITTER)
      .call();

    extension.git().checkout().setName(commit.name()).call();
    assertFalse(extension.branchName().isPresent());
  }

  @Test
  void testCommit() throws IOException, GitAPIException {
    final IndraGitExtensionImpl extension = this.createExtensionAndRepo();
    Files.write(this.projectDir.resolve("test.properties"), Collections.singletonList("boink"), StandardCharsets.UTF_8);

    final RevCommit commit = extension.git().commit()
      .setAll(true)
      .setMessage("Initial commit")
      .setCommitter(COMMITTER)
      .call();

    assertEquals(commit.getName(), extension.commit().get().getName());
  }

  private Project createProject() {
    final Project project = IndraTesting.project(p -> p.withProjectDir(this.projectDir.toFile()));
    project.getPluginManager().apply(PLUGIN);
    return project;
  }

  private IndraGitExtensionImpl createExtension() {
    return (IndraGitExtensionImpl) this.createProject().getExtensions().getByType(IndraGitExtension.class);
  }

  private IndraGitExtensionImpl createExtensionAndRepo() throws IOException, GitAPIException {
    initRepo(this.projectDir);
    return this.createExtension();
  }

  static Git initRepo(final Path repoDir) throws IOException, GitAPIException {
    Files.createDirectories(repoDir);
    final Git repo = Git.init()
      .setDirectory(repoDir.toFile())
      .setInitialBranch(DEFAULT_BRANCH)
      .call();

    return repo;
  }
}
