import com.gradle.publish.PluginBundleExtension
import net.minecrell.gradle.licenser.LicenseExtension
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
  kotlin("jvm") version embeddedKotlinVersion apply false

  id("net.minecrell.licenser") version "0.4.1"
  id("com.github.ben-manes.versions") version "0.33.0"
}

group = "net.kyori"
version = "1.0.0-SNAPSHOT"

allprojects {
  repositories {
    mavenLocal()
    mavenCentral()
    gradlePluginPortal()
  }
}

subprojects {
  apply(plugin = "java-gradle-plugin")
  apply(plugin = "com.gradle.plugin-publish")
  apply(plugin = "maven-publish")
  apply(plugin = "org.jetbrains.kotlin.jvm")
  apply(plugin = "net.minecrell.licenser")

  group = rootProject.group
  version = rootProject.version
  description = rootProject.description

  tasks.withType<KotlinCompile>().configureEach {
    kotlinOptions {
      jvmTarget = "1.8" // Why is this not the default version? D:
    }
  }

  dependencies {
    // DSL stubs are generated per-project but only for what's applied in the `plugins` block.
    // Since this root project doesn't have anything applied, we don't get DSL extensions for the
    // java configurations so we have to refer to them with strings.
    "implementation"(kotlin("stdlib-jdk8"))
    "implementation"(gradleKotlinDsl())

    "testImplementation"(kotlin("test-junit5"))
    "testImplementation"("org.junit.jupiter:junit-jupiter-api:5.7.0")
    "testRuntimeOnly"("org.junit.jupiter:junit-jupiter-engine:5.7.0")
  }

  tasks.withType<Test>().configureEach {
    useJUnitPlatform()
  }

  extensions.configure<LicenseExtension> {
    header = rootProject.file("header.txt")

    newLine = false
  }

  val pluginBundle = extensions.getByType<PluginBundleExtension>().apply {
    website = "https://github.com/KyoriPowered/indra"
    vcsUrl = "https://github.com/KyoriPowered/indra.git"
    description = ":gronk:"
    tags = listOf("kyori", "standard")
  }

  extensions.getByType<PublishingExtension>().publications.withType<MavenPublication>().configureEach {
    pom {
      name.set(project.name)
      description.set(pluginBundle.description)
      url.set(pluginBundle.vcsUrl)

      organization {
        name.set("KyoriPowered")
        url.set("https://kyori.net")
      }

      developers {
        developer {
          id.set("kashike")
          timezone.set("America/Vancouver")
        }
        developer {
          id.set("zml")
          email.set("zml at stellardrift [.] ca")
          timezone.set("America/Vancouver")
        }
      }

      licenses {
        license {
          name.set("MIT")
          url.set("https://github.com/KyoriPowered/indra/raw/master/license.txt")
          distribution.set("repo")
        }
      }
    }
  }
}
