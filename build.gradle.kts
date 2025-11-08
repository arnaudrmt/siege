plugins {
    id("java")
}

group = "fr.arnaud"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    mavenLocal()
}

dependencies {

    // Spigot dependency, provided at runtime
    compileOnly("org.spigotmc:spigot:1.8.8-R0.1-SNAPSHOT")

    // test dependencies
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(8))
}

tasks.register("buildAndCopy") {
    dependsOn("build", "copyToServer")
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<Jar> {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    from(sourceSets.main.get().resources)
}

tasks.register<Copy>("copyToServer") {
    dependsOn(tasks.jar)

    val serverPluginsDir = file("/Users/arnaud/Desktop/Documents/Developpment/Serveurs Minecraft/1.8.8/plugins")

    from(tasks.jar.get().archiveFile)
    into(serverPluginsDir)
}