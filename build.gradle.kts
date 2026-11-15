import net.minecrell.pluginyml.bukkit.BukkitPluginDescription

plugins {
    java
    id("net.minecrell.plugin-yml.bukkit") version "0.6.0"
}

group = providers.gradleProperty("group").get()
version = providers.gradleProperty("version").get()

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://repo.xenondevs.xyz/releases")
    maven("https://repo.codemc.io/repository/maven-releases/")
}

val minecraftVersion = providers.gradleProperty("minecraft.version").get()
val rebarVersion = providers.gradleProperty("rebar.version").get()
val pylonVersion = providers.gradleProperty("pylon.version").get()

dependencies {
    compileOnly("io.papermc.paper:paper-api:$minecraftVersion.build.+")
    compileOnly("io.github.pylonmc:rebar:$rebarVersion")
    compileOnly("io.github.pylonmc:pylon:$pylonVersion")
    testImplementation("org.junit.jupiter:junit-jupiter:5.11.4")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

java {
    toolchain.languageVersion = JavaLanguageVersion.of(25)
}

tasks.test {
    useJUnitPlatform()
}

tasks.jar {
    archiveBaseName.set("Rebar_Relics")
    archiveVersion.set(project.version.toString())
}

bukkit {
    name = "Rebar_Relics"
    main = "io.github.wickidcow.rebarrelics.RebarRelics"
    version = project.version.toString()
    apiVersion = minecraftVersion
    depend = listOf("Rebar", "Pylon")
    load = BukkitPluginDescription.PluginLoadOrder.STARTUP
    description = "Original talismans, charms, and relics for Rebar / Pylon"
    authors = listOf("wickidcow")
}
