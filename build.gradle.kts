// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
plugins {
  java
  application
  `maven-publish`
}

group = "io.github.sunwu51"
val refName = System.getenv("GITHUB_REF_NAME")
version = when {
  refName == null -> "0.0.0-SNAPSHOT"
  refName.startsWith("v") -> refName.removePrefix("v")
  else -> refName
}

java {
  toolchain {
    languageVersion = JavaLanguageVersion.of(8)
  }
}

sourceSets {
  main {
    java.srcDirs("src")
  }
  test {
    java.srcDirs("test")
  }
}

repositories {
  mavenCentral()
}

dependencies {
  implementation("org.jetbrains:annotations:24.0.0")
  testImplementation("junit:junit:4.13.2")
  testImplementation("org.assertj:assertj-core:3.26.3")
}

application {
  mainClass = "org.jetbrains.java.decompiler.main.decompiler.ConsoleDecompiler"
}

tasks.withType<CreateStartScripts> {
  applicationName = "fernflower"
}

tasks.jar {
  archiveFileName = "fernflower.jar"
  manifest {
    attributes["Main-Class"] = "org.jetbrains.java.decompiler.main.decompiler.ConsoleDecompiler"
  }
}

tasks.test {
  maxHeapSize = "1024m"
}

publishing {
  publications {
    create<MavenPublication>("mavenJava") {
      from(components["java"])
    }
  }
  repositories {
    maven {
      name = "GitHubPackages"
      url = uri("https://maven.pkg.github.com/sunwu51/fernflower")
      credentials {
        username = System.getenv("GITHUB_ACTOR") ?: (findProperty("gpr.user") as String?)
        password = System.getenv("GITHUB_TOKEN") ?: (findProperty("gpr.key") as String?)
      }
    }
  }
}
