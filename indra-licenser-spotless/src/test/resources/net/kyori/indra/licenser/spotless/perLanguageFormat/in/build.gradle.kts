plugins {
  id("net.kyori.indra.licenser.spotless")
  kotlin("jvm") version "2.0.20"
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

indraSpotlessLicenser {
  languageFormatOverride("kotlin") {
    doubleSlash()
  }
}
