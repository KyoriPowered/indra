import com.gradle.publish.PluginBundleExtension
import net.kyori.indra.IndraExtension

plugins {
  val indraVersion = "1.3.1"
  id("net.kyori.indra") version indraVersion apply false
  id("net.kyori.indra.publishing.gradle-plugin") version indraVersion apply false
  id("com.gradle.plugin-publish") version "0.14.0" apply false
  id("com.github.ben-manes.versions") version "0.36.0"
}

group = "net.kyori"
version = "2.0.5-SNAPSHOT"
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
    "testImplementation"("org.junit.jupiter:junit-jupiter-api:5.7.2")
    "testImplementation"("org.junit.jupiter:junit-jupiter-params:5.7.2")
    "testRuntimeOnly"("org.junit.jupiter:junit-jupiter-engine:5.7.2")
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

      versionMapping {
        usage(Usage.JAVA_API) { fromResolutionOf(JavaPlugin.RUNTIME_CLASSPATH_CONFIGURATION_NAME) }
        usage(Usage.JAVA_RUNTIME) { fromResolutionResult() }
      }
    }

    publishSnapshotsTo("stellardrift", "https://repo.stellardrift.ca/repository/snapshots/")
  }

  extensions.getByType(PluginBundleExtension::class).tags = listOf("kyori", "standard")
  extensions.getByType(PluginBundleExtension::class).website = "https://github.com/KyoriPowered/indra/wiki"
}
