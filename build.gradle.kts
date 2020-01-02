plugins {
    `java-library`
}

repositories {
    mavenCentral()
}

dependencies {
    testImplementation("junit:junit:4.12")
    testImplementation("com.google.guava:guava:28.0-jre")
    testImplementation("com.fasterxml.jackson.core:jackson-databind:2.9.9.1")
}
