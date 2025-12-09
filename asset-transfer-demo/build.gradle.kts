/* SPDX-License-Identifier: Apache-2.0 */
import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
  alias(libs.plugins.lombok)
  alias(libs.plugins.shadow)
  alias(libs.plugins.spotless)
  application
}

repositories { mavenCentral() }

dependencies {
  implementation(libs.fabric.chaincode.shim)
  implementation(libs.hypernate)
  implementation(libs.jackson.databind)
  implementation(libs.slf4j.api)

  testImplementation(files("libs/testutils.jar"))
  testImplementation(libs.assertj.core)
  testImplementation(libs.junit)
  testImplementation(libs.mockito.core)
  testImplementation(libs.mockito.junit.jupiter)

  testRuntimeOnly(libs.junit.platform.launcher)
}

java { toolchain { languageVersion = JavaLanguageVersion.of(17) } }

application { mainClass = "org.hyperledger.fabric.contract.ContractRouter" }

tasks.named<ShadowJar>("shadowJar") {
  archiveBaseName = "chaincode"
  archiveClassifier = ""
  archiveVersion = ""
  duplicatesStrategy = DuplicatesStrategy.INCLUDE
  mergeServiceFiles() // to make sure the right NameResolverProvider will be used

  manifest { attributes(mapOf("Main-Class" to application.mainClass)) }
}

tasks.named<Test>("test") { useJUnitPlatform() }

spotless {
  java {
    importOrder()
    removeUnusedImports()
    googleJavaFormat()
    formatAnnotations()
    toggleOffOn()
    licenseHeader("/* SPDX-License-Identifier: Apache-2.0 */", "package ")
  }
  kotlinGradle { ktfmt() }
}
