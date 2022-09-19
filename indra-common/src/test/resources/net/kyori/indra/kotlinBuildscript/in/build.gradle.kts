plugins {
  id("net.kyori.indra")
}

group = "com.example"
version = "1.0.0-SNAPSHOT"

repositories {
  sonatype.ossSnapshots()
}

sourceSets {
  main {
    multirelease {
      // just make sure this exists
    }
  }
}

indra {
  github("Organization", "Repository") {
    ci(true)
    publishing(true)
  }
  mitLicense()
}
