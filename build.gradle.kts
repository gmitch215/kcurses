import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget

plugins {
	kotlin("multiplatform") version "2.4.0"
	id("org.jetbrains.dokka") version "2.0.0"
	id("com.vanniktech.maven.publish") version "0.36.0"

	`maven-publish`
	signing
}

val v = "1.0.0"

group = "dev.gmitch215"
version = "${if (project.hasProperty("snapshot")) "$v-SNAPSHOT" else v}${
	project.findProperty("suffix")?.toString()?.run { "-${this}" } ?: ""
}"
description = "Kotlin/Native Bindings for ncurses library"

repositories {
	mavenCentral()
	mavenLocal()
}

java {
	toolchain {
		languageVersion.set(JavaLanguageVersion.of(21))
	}
}

kotlin {
	configureSourceSets()
	applyDefaultHierarchyTemplate()
	withSourcesJar()

	val hostOs = System.getProperty("os.name").lowercase()
	val hostArch = System.getProperty("os.arch").lowercase()
	val isMac = hostOs.contains("mac") || hostOs.contains("darwin")
	val isArm64 = hostArch.contains("aarch64") || hostArch.contains("arm64")
	val isX64 = hostArch.contains("amd64") || hostArch.contains("x86_64")

	when {
		hostOs.contains("linux") && isX64 -> linuxX64()
		isMac && isArm64 -> macosArm64()
		else -> throw GradleException(
			"Host OS '$hostOs' with architecture '$hostArch' is not supported for native compilation.",
		)
	}

	val macSdkPath: String? = if (isMac) {
		providers.exec { commandLine("xcrun", "--show-sdk-path") }
			.standardOutput.asText.get().trim()
	} else null

	targets.filterIsInstance<KotlinNativeTarget>().forEach { target ->
		target.compilations.all {
			cinterops {
				create("ncurses") {
					defFile(project.file("src/interop/ncurses.def"))
					if (target.name.startsWith("macos") && macSdkPath != null) {
						includeDirs.allHeaders("$macSdkPath/usr/include")
					}
				}
			}
		}
	}
}

fun KotlinMultiplatformExtension.configureSourceSets() {
	sourceSets
		.matching { it.name !in listOf("main", "test") }
		.all {
			val srcDir = if ("Test" in name) "test" else "main"
			val resourcesPrefix = if (name.endsWith("Test")) "test-" else ""
			val platform = when {
				(name.endsWith("Main") || name.endsWith("Test")) && "android" !in name -> name.dropLast(4)
				else -> name.substringBefore(name.first { it.isUpperCase() })
			}

			kotlin.srcDir("src/$platform/$srcDir")
			resources.srcDir("src/$platform/${resourcesPrefix}resources")

			languageSettings.apply {
				progressiveMode = true
			}
		}
}

signing {
	val signingKey: String? by project
	val signingPassword: String? by project

	if (signingKey != null && signingPassword != null)
		useInMemoryPgpKeys(signingKey, signingPassword)

	sign(publishing.publications)
}

publishing {
	publications {
		filterIsInstance<MavenPublication>().forEach {
			it.apply {
				pom {
					name = "kcurses"

					licenses {
						license {
							name = "MIT License"
							url = "https://opensource.org/licenses/MIT"
						}
					}

					developers {
						developer {
							id = "gmitch215"
							name = "Gregory Mitchell"
							email = "me@gmitch215.xyz"
						}
					}

					scm {
						connection = "scm:git:git://github.com/gmitch215/kcurses.git"
						developerConnection = "scm:git:ssh://github.com/gmitch215/kcurses.git"
						url = "https://github.com/gmitch215/kcurses"
					}
				}
			}
		}
	}

	repositories {
		if (!version.toString().endsWith("SNAPSHOT")) {
			maven {
				name = "GithubPackages"
				credentials {
					username = System.getenv("GITHUB_ACTOR")
					password = System.getenv("GITHUB_TOKEN")
				}

				url = uri("https://maven.pkg.github.com/gmitch215/kcurses")
			}
		}
	}
}

mavenPublishing {
	coordinates(project.group.toString(), project.name, project.version.toString())

	pom {
		name.set("kcurses")
		url.set("https://github.com/gmitch215/kcurses")
		inceptionYear.set("2026")

		licenses {
			license {
				name.set("MIT License")
				url.set("https://opensource.org/licenses/MIT")
			}
		}

		developers {
			developer {
				id = "gmitch215"
				name = "Gregory Mitchell"
				email = "me@gmitch215.xyz"
			}
		}

		scm {
			connection = "scm:git:git://github.com/gmitch215/kcurses.git"
			developerConnection = "scm:git:ssh://github.com/gmitch215/kcurses.git"
			url = "https://github.com/gmitch215/kcurses"
		}
	}

	publishToMavenCentral(true)
	signAllPublications()
}
