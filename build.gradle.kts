import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
  kotlin("jvm") version "1.4.10" apply false
}

group = "net.kyori"
version = "1.0.0-SNAPSHOT"

allprojects {
  repositories {
    mavenLocal()
    mavenCentral()
  }
}

subprojects {
  apply(plugin = "org.jetbrains.kotlin.jvm")

  tasks.withType<KotlinCompile>().all {
    kotlinOptions {
      jvmTarget = "1.8" // Why is this not the default version? D:
    }
  }

  dependencies {
    // "implementation" in quotes because otherwise it explodes!
    "implementation"(kotlin("stdlib-jdk8"))
    "implementation"(gradleKotlinDsl())
  }
}
