plugins {
  id("net.kyori.indra.licenser.spotless")
  kotlin("jvm") version "1.7.10"
}

kotlin {
  target {
    compilations.configureEach {
      kotlinOptions {
        jvmTarget = "1.8"
      }
    }
  }
}
