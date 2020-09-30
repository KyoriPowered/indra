import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
  kotlin("jvm") version embeddedKotlinVersion
}

repositories {
  jcenter()
  gradlePluginPortal()
}

dependencies {
  implementation(gradleApi())
  implementation(gradleKotlinDsl())
  implementation("com.gradle.publish:plugin-publish-plugin:0.12.0")
}

tasks.withType<KotlinCompile>().configureEach {
  kotlinOptions.jvmTarget = "1.8"
}
