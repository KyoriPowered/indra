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

import net.kyori.mammoth.ProjectPlugin;
import org.gradle.api.Action;
import org.gradle.api.InvalidUserDataException;
import org.gradle.api.NamedDomainObjectProvider;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.artifacts.ArtifactCollection;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.component.ProjectComponentIdentifier;
import org.gradle.api.artifacts.dsl.DependencyHandler;
import org.gradle.api.artifacts.type.ArtifactTypeDefinition;
import org.gradle.api.attributes.Bundling;
import org.gradle.api.attributes.Category;
import org.gradle.api.attributes.DocsType;
import org.gradle.api.attributes.Usage;
import org.gradle.api.file.RegularFile;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.plugins.ExtensionContainer;
import org.gradle.api.plugins.JavaLibraryPlugin;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.plugins.PluginContainer;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.TaskContainer;
import org.gradle.api.tasks.TaskProvider;
import org.gradle.api.tasks.javadoc.Javadoc;
import org.jetbrains.annotations.NotNull;

/**
 * Generate offline links to Javadoc for other projects within the same multi-module build.
 *
 * <p>This plugin currently only applies to the Javadoc published by the main source set,
 * since conventions for alternate publications are unclear.</p>
 *
 * <p>Behavior when this plugin is only applied to <em>some</em> but not all projects
 * with published Javadoc within a single build is undefined.</p>
 *
 * @since 2.1.0
 */
public class CrossdocPlugin implements ProjectPlugin {
  /**
   * Name for the main source set {@link GenerateOfflineLinks} task.
   *
   * @since 2.1.0
   */
  public static final String GENERATE_OFFLINE_LINKS_TASK_NAME = "generateOfflineLinks";

  /**
   * Name for the main source set {@link CopyJavadoc} task.
   *
   * @since 2.1.0
   */
  public static final String COPY_JAVADOC_TASK_NAME = "copyJavadoc";

  /**
   * The configuration used for resolving projects for javadoc linking.
   *
   * @since 2.1.0
   */
  public static final String OFFLINE_LINKED_JAVADOC_CONFIGURATION_NAME = "offlineLinkedJavadoc";

  /**
   * The name of the extension created on a project to configure this plugin.
   *
   * @since 2.1.0
   */
  public static final String EXTENSION_NAME = "indraCrossdoc";

  @Override
  public void apply(
    final @NotNull Project project,
    final @NotNull PluginContainer plugins,
    final @NotNull ExtensionContainer extensions,
    final @NotNull TaskContainer tasks
  ) {
    // Register extension and attribute
    this.prepareAttributeSchema(project.getDependencies());
    final CrossdocExtension extension = this.createExtension(project);

    // Once we've had the java-library plugin applied, we can actually do our work
    plugins.withType(JavaLibraryPlugin.class, $ -> {
      // Modify outgoing configurations to expose unpacked javadoc cross-project
      final NamedDomainObjectProvider<Configuration> offlineLinkedJavadoc = this.createOfflineLinkedResolvableConfiguration(project);
      this.addUnpackedResultToJavadocConfiguration(project);

      // Then configure the javadoc task to link to project dependencies
      this.configureJavadocTask(project, extension, offlineLinkedJavadoc);
      // And add a convenience task to copy this project's unpacked javadoc to a specific location.
      this.registerCopyTask(project, extension);
    });

    // We don't depend on javadoc being configured before we're applied, but it has to happen eventually
    // Let's make sure that happens
    project.afterEvaluate(p -> {
      if (!this.hasJavadocTaskAndConfiguration(project)) {
        throw new InvalidUserDataException("The indra crossdoc plugin requires javadoc and the javadocElements configuration to be set up, but they weren't.\n"
          + "\n"
          + "Did you forget to call java.withJavadocJar() in your buildscript?");
      }
    });
  }

  private CrossdocExtension createExtension(final Project project) {
    return project.getExtensions().create(CrossdocExtension.class, EXTENSION_NAME, CrossdocExtensionImpl.class, project);
  }

  private void addUnpackedResultToJavadocConfiguration(final Project project) {
    // Register unpacked Javadoc as an artifact for cross-linking
    project.getConfigurations().matching(c -> c.getName().equals(JavaPlugin.JAVADOC_ELEMENTS_CONFIGURATION_NAME)).configureEach(c -> {
      c.getOutgoing().getVariants().create("files", v -> {
        final TaskProvider<Javadoc> javadocTask = project.getTasks().named(JavaPlugin.JAVADOC_TASK_NAME, Javadoc.class);
        v.artifact(javadocTask.map(it -> it.getDestinationDir()), a -> {
          a.builtBy(javadocTask);
          a.setType(ArtifactTypeDefinition.DIRECTORY_TYPE); // JavaBasePlugin has a hardcoded list of artifact types that can't be published. this is one of them
        });
        v.getAttributes().attribute(
          JavadocPackaging.JAVADOC_PACKAGING_ATTRIBUTE,
          project.getObjects().named(JavadocPackaging.class, JavadocPackaging.DIRECTORY)
        );
      });
    });
  }

  private NamedDomainObjectProvider<Configuration> createOfflineLinkedResolvableConfiguration(final Project project) {
    // Resolve JD for cross-linking between modules
    final ObjectFactory objects = project.getObjects();
    final NamedDomainObjectProvider<Configuration> offlineLinkedJavadoc = project.getConfigurations().register(OFFLINE_LINKED_JAVADOC_CONFIGURATION_NAME, c -> {
      c.setCanBeResolved(true);
      c.setCanBeConsumed(false);

      c.attributes(a -> {
        // Matching the attributes of the javadocElements configuration
        a.attribute(Category.CATEGORY_ATTRIBUTE, objects.named(Category.class, Category.DOCUMENTATION));
        a.attribute(Bundling.BUNDLING_ATTRIBUTE, objects.named(Bundling.class, Bundling.EXTERNAL));
        a.attribute(DocsType.DOCS_TYPE_ATTRIBUTE, objects.named(DocsType.class, DocsType.JAVADOC));
        a.attribute(Usage.USAGE_ATTRIBUTE, objects.named(Usage.class, Usage.JAVA_RUNTIME));

        // plus this, to prefer directories
        a.attribute(JavadocPackaging.JAVADOC_PACKAGING_ATTRIBUTE, objects.named(JavadocPackaging.class, JavadocPackaging.DIRECTORY));
      });

      c.extendsFrom(project.getConfigurations().getByName(JavaPlugin.API_ELEMENTS_CONFIGURATION_NAME));
    });
    return offlineLinkedJavadoc;
  }

  private void prepareAttributeSchema(final DependencyHandler handler) {
    handler.getAttributesSchema().attribute(JavadocPackaging.JAVADOC_PACKAGING_ATTRIBUTE);
  }

  private void configureJavadocTask(final Project project, final CrossdocExtension extension, final NamedDomainObjectProvider<Configuration> offlineLinkedJavadoc) {
    // link to modules in project
    final Provider<ArtifactCollection> jdLinks = offlineLinkedJavadoc.map(oLJ -> oLJ.getIncoming()
      .artifactView(view -> {
        view.componentFilter(c -> c instanceof ProjectComponentIdentifier && ((ProjectComponentIdentifier) c).getBuild().isCurrentBuild()); // only in-project, and not included builds
        view.setLenient(true); // ignore artifacts with no javadoc elements variant
      }).getArtifacts());

    final TaskProvider<GenerateOfflineLinks> generateLinks = project.getTasks().register(GENERATE_OFFLINE_LINKS_TASK_NAME, GenerateOfflineLinks.class, t -> {
      t.getLinkBaseUrl().set(extension.baseUrl());
      t.getLinkableArtifactFiles().from(jdLinks.map(ArtifactCollection::getArtifactFiles));
      t.getLinkableArtifacts().set(jdLinks.flatMap(ArtifactCollection::getResolvedArtifacts));
      t.getUrlProvider().set(extension.projectDocumentationUrlProvider());
      final Provider<RegularFile> argsDest = project.getLayout().getBuildDirectory().file("tmp/" + t.getName() + "-args.txt");
      t.getOutputFile().set(argsDest);
    });

    // Hook that into the Javadoc task
    final Provider<RegularFile> linksOutput = generateLinks.flatMap(t -> t.getOutputFile());
    project.getTasks().matching(t -> t.getName().equals(JavaPlugin.JAVADOC_TASK_NAME) && t instanceof Javadoc).configureEach(t -> {
      t.getInputs().file(linksOutput)
        .withPropertyName("crossDocOfflineLinks");

      t.doFirst(new Action<Task>() {
        @Override
        public void execute(final Task arg0) {
          ((Javadoc) arg0).getOptions().optionFiles(linksOutput.get().getAsFile());
        }
      });
    });
  }

  private void registerCopyTask(final Project project, final CrossdocExtension extension) {
    project.getTasks().register(COPY_JAVADOC_TASK_NAME, CopyJavadoc.class, t -> {
      t.getProjectName().set(project.provider(() -> project.getName()));
      t.getProjectPath().set(project.provider(() -> project.getPath()));
      t.getDocumentationUrlProvider().set(extension.projectDocumentationUrlProvider());

      // todo: sensitive to tasks being created eagerly
      t.getJavadocFiles().from(project.getTasks().named(JavaPlugin.JAVADOC_TASK_NAME));
      t.getRootDir().set(project.getRootDir());
    });
  }

  private boolean hasJavadocTaskAndConfiguration(final Project project) {
    return project.getTasks().getNames().contains(JavaPlugin.JAVADOC_TASK_NAME) && project.getConfigurations().getNames().contains(JavaPlugin.JAVADOC_ELEMENTS_CONFIGURATION_NAME);
  }
}
