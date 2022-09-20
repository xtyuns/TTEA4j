plugins {
    id("java-library")
    id("maven-publish")
    id("signing")
}

group = "io.github.xtyuns"
version = "1.0.1"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.9.0")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.9.0")
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}


java {
    withSourcesJar()
    withJavadocJar()
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

tasks.withType<Javadoc> {
    options.encoding = System.getProperty("file.encoding") ?: "UTF-8"
    if (JavaVersion.current().isJava9Compatible) {
        (options as StandardJavadocDocletOptions).addBooleanOption("html5", true)
    }
}

publishing {
    repositories {
        mavenLocal()
        maven {
            name = "OSSRH"

            val repoUrl = if (version.toString().endsWith("SNAPSHOT")) {
                "https://s01.oss.sonatype.org/content/repositories/snapshots/"
            } else {
                "https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/"
            }
            url = uri(repoUrl)

            credentials {
                username = properties["mavenUser"].toString()
                password = properties["mavenPwd"].toString()
            }
        }
    }

    publications {
        create<MavenPublication>("-${project.name}-") {
            from(components["java"])

            pom {
                name.set(project.name)
                description.set("Tencent Tiny Encryption algorithm library")
                url.set("https://github.com/xtyuns/TTEA4j")

                licenses {
                    license {
                        name.set("Apache License, Version 2.0")
                        url.set("https://www.apache.org/licenses/LICENSE-2.0")
                    }
                }

                developers {
                    developer {
                        id.set("xtyuns")
                        name.set("xtyuns")
                        email.set("xtyuns@163.com")
                    }
                }

                scm {
                    connection.set("scm:git:git://github.com/xtyuns/TTEA4j.git")
                    developerConnection.set("scm:git:ssh://github.com/xtyuns/TTEA4j.git")
                    url.set("https://github.com/xtyuns/TTEA4j")
                }
            }

            versionMapping {
                usage("java-api") {
                    fromResolutionOf("runtimeClasspath")
                }
                usage("java-runtime") {
                    fromResolutionResult()
                }
            }
        }
    }
}

signing {
    sign(publishing.publications["-${project.name}-"])
}
