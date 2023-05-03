plugins {
  id("net.kyori.indra.licenser.spotless")
  kotlin("jvm") version "1.8.21"
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
