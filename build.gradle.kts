// versions
// https://parchmentmc.org/docs/getting-started
val parchmentVersion = "2023.09.03"
// https://fabricmc.net/develop/
val minecraftVersion = "1.20.1"
val loaderVersion = "0.16.14"
val fapiVersion = "0.92.5+1.20.1"

// in-house dependencies
val flywheelVersion = "1.0.0-217"
val ponderVersion = "1.0.78"
val registrateVersion = "1.3.79-MC1.20.1"
val milkLibVersion = "1.2.60"
val portLibVersion = "2.3.8+1.20.1"
val portLibModules = listOf(
    "accessors", "base", "entity", "extensions", "networking", "obj_loader",
    "tags", "transfer", "models", "tool_actions", "client_events", "brewing"
)

// external dependencies
val configApiVersion = "8.0.0"
val nightConfigVersion =  "3.6.3"
// https://maven.jamieswhiteshirt.com/libs-release/com/jamieswhiteshirt/reach-entity-attributes/
val reaVersion = "2.4.0"
val jsr305Version = "3.0.2"

// compat
// https://modrinth.com/mod/cc-tweaked/versions
val ccVersion = "1.115.1"
// for CC - https://modrinth.com/mod/cloth-config/versions
val clothVersion = "11.1.136+fabric"
// https://modrinth.com/mod/jei/versions
val jeiVersion = "15.20.0.106"
// https://modrinth.com/mod/rei/versions
val reiVersion = "12.1.785"
// https://modrinth.com/mod/emi/versions
val emiVersion = "1.1.22+1.20.1"
// https://modrinth.com/mod/botania
val botaniaVersion = "1.20.1-448-FABRIC"
// https://modrinth.com/mod/modmenu/versions
val modmenuVersion = "7.2.2"
// https://modrinth.com/mod/sandwichable/versions
val sandwichableVersion = "1.3.1+1.20.1"
// https://modrinth.com/mod/sodium
val sodiumVersion = "mc1.20.1-0.5.13-fabric"
// https://modrinth.com/mod/indium
val indiumVersion = "1.0.36+mc1.20.1"
// https://github.com/emilyploszaj/trinkets/releases/
val trinketsVersion = "3.7.1"
// for Trinkets - https://modrinth.com/mod/cardinal-components-api/versions
val ccaVersion = "5.2.3"
// https://modrinth.com/mod/journeymap
val jmVersion = "1.20.1-5.10.3-fabric"
// check the jm jar, it's JiJ
val jmApiVersion = "1.20-1.9-SNAPSHOT"

// dev stuff
val ccRuntime = false
val recipeViewer = "emi" // jei, rei, or emi

plugins {
    id("fabric-loom") version "1.10.+"
    id("maven-publish")
}

val buildNum = providers.environmentVariable("GITHUB_RUN_NUMBER")
    .filter(String::isNotEmpty)
    .map { "-build.$it" }
    .orElse("-local")
    .getOrElse("")

version = "6.0.4.0+mc$minecraftVersion$buildNum"

group = "com.simibubi.create"
base.archivesName = "create-fabric"

repositories {
    maven("https://maven.parchmentmc.org") // Parchment
    maven("https://maven.fabricmc.net") // FAPI, Loader
    maven("https://maven.createmod.net") // Ponder, Flywheel
    maven("https://mvn.devos.one/snapshots") // Registrate, Forge Tags, Milk Lib
    maven("https://mvn.devos.one/releases") // Porting Lib
    maven("https://raw.githubusercontent.com/Fuzss/modresources/main/maven") // Forge Config API Port
    maven("https://maven.shedaniel.me") // REI and deps
    maven("https://api.modrinth.com/maven") { // LazyDFU, Sodium, Sandwichable
        content { includeGroupAndSubgroups("maven.modrinth") }
    }
    maven("https://maven.terraformersmc.com") // Mod Menu, Trinkets
    maven("https://maven.squiddev.cc") // CC:T
    maven("https://modmaven.dev") // Botania
    maven("https://maven.jamieswhiteshirt.com/libs-release") { // Reach Entity Attributes
        content { includeGroup("com.jamieswhiteshirt") }
    }
    maven("https://maven.ladysnake.org/releases") // CCA, for Trinkets
    maven("https://maven.ftb.dev/releases") // FTB
    maven("https://maven.architectury.dev") // Architectury API
    maven("https://jm.gserv.me/repository/maven-public/") // Journey map
}

val ponder = file("Ponder")

dependencies {
    // setup
    minecraft("com.mojang:minecraft:$minecraftVersion")
    mappings(loom.layered {
        officialMojangMappings { nameSyntheticMembers = false }
        parchment("org.parchmentmc.data:parchment-$minecraftVersion:$parchmentVersion@zip")
    })
    modImplementation("net.fabricmc:fabric-loader:$loaderVersion")

    // dependencies
    modImplementation("net.fabricmc.fabric-api:fabric-api:$fapiVersion")
    for (module in portLibModules) {
        modApi(include("io.github.fabricators_of_create.Porting-Lib:$module:$portLibVersion")!!)
    }
    modApi(include("com.tterrag.registrate_fabric:Registrate:$registrateVersion") {
        exclude(mapOf("group" to "io.github.fabricators_of_create")) // avoid duplicate Porting Lib
    })
    modApi(include("com.electronwill.night-config:core:$nightConfigVersion")!!)
    modApi(include("com.electronwill.night-config:toml:$nightConfigVersion")!!)
    modApi(include("fuzs.forgeconfigapiport:forgeconfigapiport-fabric:$configApiVersion")!!)
    modApi(include("dev.engine-room.flywheel:flywheel-fabric-$minecraftVersion:$flywheelVersion")!!)
    modApi(include("com.jamieswhiteshirt:reach-entity-attributes:$reaVersion")!!)
    modApi(include("io.github.tropheusj:milk-lib:$milkLibVersion")!!)
    api(include("com.google.code.findbugs:jsr305:$jsr305Version")!!)

    if (ponder.exists()) {
        implementation("net.createmod.ponder:Ponder-Fabric-$minecraftVersion:$ponderVersion") { isTransitive = false }
        implementation("net.createmod.ponder:Ponder-Common-$minecraftVersion:$ponderVersion")
    } else {
        modApi(include("net.createmod.ponder:Ponder-Fabric-$minecraftVersion:$ponderVersion")!!)
    }

    // compat
    modCompileOnly("cc.tweaked:cc-tweaked-$minecraftVersion-fabric-api:$ccVersion")

    modCompileOnly("vazkii.botania:Botania:$botaniaVersion") { isTransitive = false }
    modCompileOnly("com.terraformersmc:modmenu:$modmenuVersion")
    modCompileOnly("maven.modrinth:sandwichable:$sandwichableVersion")
    modCompileOnly("maven.modrinth:sodium:$sodiumVersion")

    modCompileOnly("dev.emi:trinkets:$trinketsVersion")
    // for Trinkets
    modCompileOnly("dev.onyxstudios.cardinal-components-api:cardinal-components-base:$ccaVersion")
    modCompileOnly("dev.onyxstudios.cardinal-components-api:cardinal-components-entity:$ccaVersion")

    // FIXME - Use gradle.properties for these versions, make change to concealed for this
    modCompileOnly("dev.architectury:architectury-fabric:9.1.12")
    modCompileOnly("dev.ftb.mods:ftb-chunks-fabric:2001.3.1")
    modCompileOnly("dev.ftb.mods:ftb-teams-fabric:2001.3.0")
    modCompileOnly("dev.ftb.mods:ftb-library-fabric:2001.2.4")

    modCompileOnly("maven.modrinth:journeymap:$jmVersion")
    modCompileOnly("info.journeymap:journeymap-api:$jmApiVersion")

    // EMI
    modCompileOnly("dev.emi:emi-fabric:$emiVersion:api") { isTransitive = false }
    // JEI
    modCompileOnly("mezz.jei:jei-$minecraftVersion-fabric:$jeiVersion") { isTransitive = false }
    // REI
    modCompileOnly("me.shedaniel:RoughlyEnoughItems-api-fabric:$reiVersion")
    modCompileOnly("me.shedaniel:RoughlyEnoughItems-default-plugin-fabric:$reiVersion")

    when (recipeViewer) {
        "jei" -> modLocalRuntime("mezz.jei:jei-$minecraftVersion-fabric:$jeiVersion")
        "rei" -> modLocalRuntime("me.shedaniel:RoughlyEnoughItems-fabric:$reiVersion")
        "emi" -> modLocalRuntime("dev.emi:emi-fabric:$emiVersion")
    }

    // dev env
    modLocalRuntime("com.terraformersmc:modmenu:$modmenuVersion")
    modLocalRuntime("dev.emi:trinkets:$trinketsVersion") { isTransitive = false }
    // for Trinkets
    modLocalRuntime("dev.onyxstudios.cardinal-components-api:cardinal-components-base:$ccaVersion")
    modLocalRuntime("dev.onyxstudios.cardinal-components-api:cardinal-components-entity:$ccaVersion")
    if (ccRuntime) {
        modLocalRuntime("cc.tweaked:cc-tweaked-$minecraftVersion-fabric:$ccVersion")
        modLocalRuntime("maven.modrinth:cloth-config:$clothVersion")
    }
    // have deprecated modules present at runtime only
    modLocalRuntime("net.fabricmc.fabric-api:fabric-api-deprecated:$fapiVersion")
}

sourceSets.named("main") {
    resources {
        srcDir("src/generated/resources")
        exclude(".cache/")
    }
}

loom {
    accessWidenerPath = file("src/main/resources/create.accesswidener")

    runs {
        register("datagen") {
            client()
            name("Data Generation")
            vmArg("-Dfabric-api.datagen")
            vmArg("-Dfabric-api.datagen.output-dir=${file("src/generated/resources")}")
            vmArg("-Dfabric-api.datagen.modid=create")
            vmArg("-Dporting_lib.datagen.existing_resources=${file("src/main/resources")}")
        }

        register("gametestServer") {
            server()
            name("Headlesss GameTests")
            ideConfigGenerated(false) // this run is for CI
            vmArg("-Dfabric-api.gametest")
            vmArg("-Dfabric-api.gametest.report-file=${layout.buildDirectory}/junit.xml")
            runDir("run/gametest")
        }

        named("server") {
            runDir("run/server")
        }

        configureEach {
            vmArg("-XX:+AllowEnhancedClassRedefinition")
            vmArg("-XX:+IgnoreUnrecognizedVMOptions")
            property("mixin.debug.export", "true")
        }
    }
}

configurations {
    // this avoids remapping ponder when it's local
    named("runtimeClasspath") {
        attributes {
            attribute(Attribute.of("create.marker", String::class.java), "h")
        }
    }
}

tasks.named<ProcessResources>("processResources") {
    exclude("**/*.bbmodel", "**/*.lnk")

    val properties: MutableMap<String, Any> = mutableMapOf(
        "version" to version,
        "minecraft_version" to minecraftVersion,
        "loader_version" to loaderVersion,
        "fabric_version" to fapiVersion,
        "forge_config_version" to configApiVersion,
        "milk_lib_version" to milkLibVersion,
        "reach_entity_attributes_version" to reaVersion
    )

    for (module in portLibModules) {
        properties["port_lib_${module}_version"] = portLibVersion
    }
    properties["port_lib_tags_version"] = "3.0" // the weird one

    inputs.properties(properties)

    filesMatching("fabric.mod.json") {
        expand(properties)
    }
}

java {
    withSourcesJar()
}

tasks.named<JavaCompile>("compileJava") {
    options.compilerArgs.add("-Xmaxerrs")
    options.compilerArgs.add("10000")
}

publishing {
    publications {
        register<MavenPublication>("mavenJava") {
            artifactId = "create-fabric-$minecraftVersion"
            from(components["java"])
        }
    }

    repositories {
        maven("https://mvn.devos.one/releases") {
            name = "devOsReleases"
            credentials(PasswordCredentials::class)
        }

        maven("https://mvn.devos.one/snapshots") {
            name = "devOsSnapshots"
            credentials(PasswordCredentials::class)
        }
    }
}
