import com.gradle.publish.PluginBundleExtension
import net.kyori.indra.IndraExtension

plugins {
  val indraVersion = "1.3.1"
  id("net.kyori.indra") version indraVersion apply false
  id("net.kyori.indra.publishing.gradle-plugin") version indraVersion apply false
  id("com.gradle.plugin-publish") version "0.12.0" apply false
  id("com.github.ben-manes.versions") version "0.36.0"
}

group = "net.kyori"
version = "1.4.0-SNAPSHOT"
description = "KyoriPowered organizational build standards and utilities"

allprojects {
  repositories {
    mavenCentral()
    gradlePluginPortal()
  }
}

subprojects {
  apply(plugin = "java-gradle-plugin")
  apply(plugin = "com.gradle.plugin-publish")
  apply(plugin = "net.kyori.indra")
  apply(plugin = "net.kyori.indra.license-header")
  apply(plugin = "net.kyori.indra.publishing.gradle-plugin")

  dependencies {
    "testImplementation"("org.junit.jupiter:junit-jupiter-api:5.7.0")
    "testRuntimeOnly"("org.junit.jupiter:junit-jupiter-engine:5.7.0")
  }

  extensions.configure(IndraExtension::class) {
    github("KyoriPowered", "indra") {
      ci = true
    }
    mitLicense()

    configurePublications {
      pom {
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
      }
    }
  }

  extensions.getByType(PluginBundleExtension::class).tags = listOf("kyori", "standard")
}
