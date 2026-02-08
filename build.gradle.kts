// versions
// https://parchmentmc.org/docs/getting-started
val parchmentVersion = "2024.11.17"
// https://fabricmc.net/develop/
val minecraftVersion = "1.21.1"
val loaderVersion = "0.16.10"
val fapiVersion = "0.115.1+1.21.1"

// in-house dependencies
val flywheelVersion = "1.0.1-11"
val ponderVersion = "1.0.50"
val registrateVersion = "1.3.77-MC1.21.1"

// external dependencies
val configApiVersion = "21.1.3"
val nightConfigVersion =  "3.6.3"
val jsr305Version = "3.0.2"
val portingLibVersion = "3.1.0-beta.54+1.21.1"
val portingLibTagsModVersion = "3.0"

// compat
// https://modrinth.com/mod/cc-tweaked/versions
val ccVersion = "1.115.1"
// for CC - https://modrinth.com/mod/cloth-config/versions
val clothVersion = "15.0.140+fabric"
// https://modrinth.com/mod/jei/versions
val jeiVersion = "19.21.0.247"
// https://modrinth.com/mod/rei/versions
val reiVersion = "16.0.799"
// https://modrinth.com/mod/emi/versions
val emiVersion = "1.1.20+1.21.1"
// https://modrinth.com/mod/botania
val botaniaVersion = "1.19.2-436-FABRIC"
// https://modrinth.com/mod/modmenu/versions
val modmenuVersion = "11.0.3"
// https://modrinth.com/mod/sandwichable/versions
val sandwichableVersion = "1.3.1+1.20.1"
// https://modrinth.com/mod/sodium
val sodiumVersion = "mc1.21.1-0.6.9-fabric"
// https://github.com/emilyploszaj/trinkets/releases/
val trinketsVersion = "3.10.0"
// for Trinkets - https://modrinth.com/mod/cardinal-components-api/versions
val ccaVersion = "6.1.2"
// https://modrinth.com/mod/journeymap
val jmVersion = "1.21.1-6.0.0-beta.39+fabric"
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

version = "6.0.0.0+mc$minecraftVersion$buildNum"

group = "com.simibubi.create"
base.archivesName = "create-fabric"

repositories {
    maven("https://maven.parchmentmc.org") // Parchment
    maven("https://maven.fabricmc.net") // FAPI, Loader
    maven("https://maven.createmod.net") // Ponder, Flywheel
    maven("https://mvn.devos.one/snapshots") // Registrate, Forge Tags, Milk Lib
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
        officialMojangMappings()
        parchment("org.parchmentmc.data:parchment-$minecraftVersion:$parchmentVersion@zip")
    })
    modImplementation("net.fabricmc:fabric-loader:$loaderVersion")

    // dependencies
    modImplementation("net.fabricmc.fabric-api:fabric-api:$fapiVersion")

    modApi(include("com.tterrag.registrate_fabric:Registrate:$registrateVersion")!!)

    modApi(include("com.electronwill.night-config:core:$nightConfigVersion")!!)
    modApi(include("com.electronwill.night-config:toml:$nightConfigVersion")!!)
    modApi(include("fuzs.forgeconfigapiport:forgeconfigapiport-fabric:$configApiVersion")!!)
    modApi(include("dev.engine-room.flywheel:flywheel-fabric-$minecraftVersion:$flywheelVersion")!!)
    api(include("com.google.code.findbugs:jsr305:$jsr305Version")!!)
    listOf(
        "accessors",
        "asm",
        "attributes",
        "base",
        "blocks",
        "brewing",
        "client_events",
        "common",
        "conditions",
        "config",
        "core",
        "data",
        "entity",
        "extensions",
        "fluids",
        "gametest",
        "gui_utils",
        "item_abilities",
        "lazy_registration",
        "level_events",
        "mixin_extensions",
        "model_loader",
        "models",
        "obj_loader",
        "render_types",
        "tags",
        "transfer"
    ).forEach { module ->
        modImplementation(include("io.github.fabricators_of_create.Porting-Lib:$module:$portingLibVersion")!!)
    }

    if (ponder.exists()) {
        implementation("net.createmod.ponder:Ponder-Fabric-$minecraftVersion:$ponderVersion") { isTransitive = false }
        implementation("net.createmod.ponder:Ponder-Common-$minecraftVersion:$ponderVersion")
    } else {
        modRuntimeOnly(include("net.createmod.ponder:Ponder-Fabric-$minecraftVersion:$ponderVersion")!!)
        modCompileOnly("net.createmod.ponder:Ponder-Fabric-$minecraftVersion:$ponderVersion") {
            exclude(group = "io.github.fabricators_of_create.Porting-Lib")
        }
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

configurations.configureEach {
    resolutionStrategy.eachDependency {
        if (requested.group == "io.github.fabricators_of_create.Porting-Lib") {
            useVersion(portingLibVersion)
        }
    }
}

sourceSets.named("main") {
    java {
        // Temporary exclusions while the Fabric 1.21.1 runtime path is stabilized.
        exclude(
            "com/simibubi/create/compat/computercraft/implementation/**",
            "com/simibubi/create/compat/curios/**",
            "com/simibubi/create/compat/emi/**",
            "com/simibubi/create/compat/ftb/**",
            "com/simibubi/create/compat/jei/**",
            "com/simibubi/create/compat/rei/**",
            "com/simibubi/create/compat/sandwichable/**",
            "com/simibubi/create/compat/trainmap/**",
            "com/simibubi/create/foundation/data/CreateDatamapProvider.java",
            "com/simibubi/create/foundation/data/RuntimeDataGenerator.java",
            "com/simibubi/create/foundation/data/SimpleDatagenIngredient.java",
            "com/simibubi/create/foundation/data/recipe/*Gen.java",
            "com/simibubi/create/foundation/data/recipe/CreateRecipeProvider.java",
            "com/simibubi/create/foundation/data/recipe/MechanicalCraftingRecipeBuilder.java",
            "com/simibubi/create/foundation/data/recipe/LogStrippingFakeRecipes.java",
            "com/simibubi/create/foundation/data/recipe/ProcessingRecipeGen.java",
            "com/simibubi/create/foundation/data/recipe/StandardRecipeGen.java",
            "com/simibubi/create/foundation/mixin/accessor/FluidInteractionRegistryAccessor.java",
            "com/simibubi/create/foundation/mixin/accessor/ItemStackHandlerAccessor.java",
            "com/simibubi/create/foundation/mixin/datafixer/ItemStackComponentizationFixMixin.java",
            "com/simibubi/create/foundation/mixin/SmithingTrimRecipeMixin.java",
            "com/simibubi/create/infrastructure/data/**",
            "com/simibubi/create/infrastructure/ponder/**",
            "com/simibubi/create/infrastructure/RemapHelper.java"
        )
    }
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
        "port_lib_accessors_version" to portingLibVersion,
        "port_lib_base_version" to portingLibVersion,
        "port_lib_entity_version" to portingLibVersion,
        "port_lib_extensions_version" to portingLibVersion,
        "port_lib_obj_loader_version" to portingLibVersion,
        "port_lib_tags_version" to portingLibTagsModVersion,
        "port_lib_transfer_version" to portingLibVersion,
        "port_lib_models_version" to portingLibVersion,
        "port_lib_client_events_version" to portingLibVersion
    )

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

// Keep Loom's remap classpath empty; the distributable script remaps classes again with --ignoreConflicts.
tasks.named<net.fabricmc.loom.task.RemapJarTask>("remapJar") {
    classpath.setFrom(files())
}

val auditClientLog by tasks.registering(Exec::class) {
    group = "verification"
    description = "Audit run/logs/latest.log for actionable runtime warning signatures."
    commandLine(
        "bash",
        "-lc",
        "if [ ! -f run/logs/latest.log ]; then echo 'Missing log: run/logs/latest.log'; exit 1; fi; scripts/audit_runtime_warnings.sh run/logs/latest.log"
    )
}

val auditServerLog by tasks.registering(Exec::class) {
    group = "verification"
    description = "Audit run/server/logs/latest.log for actionable runtime warning signatures."
    commandLine(
        "bash",
        "-lc",
        "if [ ! -f run/server/logs/latest.log ]; then echo 'Missing log: run/server/logs/latest.log'; exit 1; fi; scripts/audit_runtime_warnings.sh run/server/logs/latest.log"
    )
}

tasks.named("runClient") {
    finalizedBy(auditClientLog)
}

tasks.named("runServer") {
    finalizedBy(auditServerLog)
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
