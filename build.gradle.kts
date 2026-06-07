// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    id("com.android.application") version "9.1.0" apply false
    id("org.jetbrains.kotlin.android") version "2.3.21" apply false
    id("org.jetbrains.kotlin.kapt") version "2.3.21" apply false
    id("org.jetbrains.kotlin.plugin.parcelize") version "2.3.21" apply false
    id("org.sonarqube") version "7.3.1.8318"
}

sonar {
  properties {
    property("sonar.projectKey", "PaulaRecaj_LKS_Parking_PaulaRecaj")
    property("sonar.organization", "paularecaj")
  }
}