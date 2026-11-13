// Extra build configuration for the GTNH/RFG build (RFG applies this file after the main script).
//
// This file owns three things:
//   * the headless unit-test harness (JUnit 4 + Mockito on a Java 8 launcher) and the JaCoCo coverage
//     report/ratchet,
//   * the non-shipped in-game test mod in `src/gameTest` and the `gameTestServer` task that runs it,
//   * the `-PsmokeTest` boot hook that lets CI start a real server and exit.
//
// The layout mirrors MrFuzzihead/PeripheralsPlusPlus (and OpenPeripheral), which is known to work with
// `com.gtnewhorizons.gtnhconvention` + Gradle 9 and the configuration cache.

import java.math.BigDecimal

import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Classpath
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.JavaExec
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.api.tasks.testing.Test
import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.jvm.toolchain.JavaLanguageVersion
import org.gradle.jvm.toolchain.JavaToolchainService
import org.gradle.process.CommandLineArgumentProvider
import org.gradle.testing.jacoco.plugins.JacocoPluginExtension
import org.gradle.testing.jacoco.plugins.JacocoTaskExtension
import org.gradle.testing.jacoco.tasks.JacocoCoverageVerification
import org.gradle.testing.jacoco.tasks.JacocoReport

/**
 * Attaches the JaCoCo agent to a forked game.
 *
 * The `Test` tasks get the agent from the jacoco plugin; a `JavaExec` run task needs it as an explicit
 * `-javaagent` argument. The agent jar is declared as a `@Classpath` input so Gradle resolves it with the task
 * (in configuration-cache terms this is a task input, not a configuration-time resolution).
 */
abstract class JacocoAgentArgumentProvider : CommandLineArgumentProvider {

    @get:Classpath
    abstract val agent: ConfigurableFileCollection

    @get:Input
    abstract val destFile: Property<String>

    @get:Input
    abstract val includes: Property<String>

    @get:Input
    abstract val append: Property<Boolean>

    override fun asArguments(): Iterable<String> {
        val jar = extractAgent()
        return listOf(
            "-javaagent:" + jar.absolutePath +
                "=destfile=" + destFile.get() +
                ",append=" + append.get() +
                ",dumponexit=true" +
                ",includes=" + includes.get()
        )
    }

    /**
     * The `jacocoAgent` configuration resolves to the JaCoCo *distribution* jar, which has no `Premain-Class` of
     * its own: the real agent is `jacocoagent.jar` inside it. Gradle's own JaCoCo support unpacks it the same way.
     */
    private fun extractAgent(): java.io.File {
        val distribution = agent.files.single()
        val target = java.io.File(destFile.get()).parentFile.resolve("jacocoagent.jar")
        java.util.zip.ZipFile(distribution).use { zip ->
            val entry = zip.getEntry("jacocoagent.jar") ?: return distribution
            target.parentFile?.mkdirs()
            zip.getInputStream(entry).use { input -> target.outputStream().use { output -> input.copyTo(output) } }
        }
        return target
    }
}

// ---------------------------------------------------------------------------------------------------------
// Unit tests + coverage
// ---------------------------------------------------------------------------------------------------------

apply(plugin = "jacoco")

project.extensions.configure(JacocoPluginExtension::class.java) {
    toolVersion = "0.8.16"
}

val javaToolchains = project.extensions.getByType(JavaToolchainService::class.java)

tasks.withType<Test>().configureEach {
    extensions.configure(JacocoTaskExtension::class.java) {
        isEnabled = true
    }
    // The mod ships Java 8 bytecode (jabel), and Mockito 4's inline mock maker behaves best on that
    // generation. Running the suite on a Java 8 launcher also proves the tests do not use newer API.
    javaLauncher.set(javaToolchains.launcherFor { languageVersion.set(JavaLanguageVersion.of(8)) })
    // Minecraft 1.7.10's class files are Java 6; the JVM warns about nothing, but RFG's recompiled jar is
    // Java 8, so the launcher is fine. Give every test a predictable, single-threaded environment: the code
    // under test is single-threaded in game and several classes hold static state.
    maxParallelForks = 1
    systemProperty("java.awt.headless", "true")
    // Config.init() resolves "config/ExtraPlanets.cfg" relative to the working directory. Keep the generated file
    // (and anything else a test writes) inside build/ instead of the checkout.
    workingDir = layout.buildDirectory.dir("test-workdir").get().asFile
    doFirst { workingDir.mkdirs() }
    testLogging {
        events("failed")
        exceptionFormat = TestExceptionFormat.FULL
        showStandardStreams = false
    }
}

// Tests need the compileOnly dependencies too: production classes implement compileOnly supertypes
// (cofh.api.energy, mekanism.api.energy, ic2.api.item), so a test cannot even name the class without them on
// its classpath.
val mainSourceSet = project.extensions.getByType(SourceSetContainer::class.java).getByName("main")
configurations.named("testCompileClasspath") {
    extendsFrom(configurations.named("compileOnly").get())
}
configurations.named("testRuntimeClasspath") {
    extendsFrom(configurations.named("compileOnly").get())
}

// Only the unit-test run feeds the report. A gameTestServer run also writes jacoco/gameTest.exec (see the agent
// below), but in a Forge 1.7.10 *dev* launch the LaunchClassLoader round-trips every class through ASM, so the
// class ids in that file cannot match the class files on disk; merging it would only produce "Classes in bundle
// do not match with execution data" warnings and no data.
val jacocoExecFiles = project.files(project.layout.buildDirectory.file("jacoco/test.exec"))

fun jacocoClassDirs() = project.files(mainSourceSet.output.classesDirs).asFileTree.matching {
    // The generated version holder and everything that needs a GL context or a live client are not
    // meaningfully unit-testable headless; measuring them would only dilute the number.
    exclude(
        "com/mjr/extraplanets/Tags.class",
        "com/mjr/extraplanets/client/**",
        "com/mjr/extraplanets/proxy/ClientProxy.class",
        "com/mjr/extraplanets/nei/**"
    )
}

tasks.named<JacocoReport>("jacocoTestReport") {
    group = "verification"
    description = "Generates HTML and XML JaCoCo coverage reports from the unit test run"
    dependsOn(tasks.named("test"))

    reports {
        xml.required.set(true)
        html.required.set(true)
        csv.required.set(false)
    }

    classDirectories.setFrom(jacocoClassDirs())
    sourceDirectories.setFrom(mainSourceSet.allJava.srcDirs)
    executionData.setFrom(jacocoExecFiles)
}

tasks.named<JacocoCoverageVerification>("jacocoTestCoverageVerification") {
    group = "verification"
    description = "Fails the build when unit-test coverage drops below the recorded baseline"
    dependsOn(tasks.named("test"))

    classDirectories.setFrom(jacocoClassDirs())
    sourceDirectories.setFrom(mainSourceSet.allJava.srcDirs)
    executionData.setFrom(jacocoExecFiles)

    violationRules {
        // A regression guard at the current baseline, not a target. ExtraPlanets is ~13 MB of worldgen,
        // rendering and registration code that needs a live game to execute at all; the honest headless
        // ceiling for this codebase is low (measured: 2.04% of 81,200 lines, 0.58% of 34,058 branches).
        // Raise these numbers as tests are added - never lower them.
        rule {
            limit {
                counter = "LINE"
                value = "COVEREDRATIO"
                minimum = BigDecimal("0.020")
            }
            limit {
                counter = "BRANCH"
                value = "COVEREDRATIO"
                minimum = BigDecimal("0.0055")
            }
        }
    }
}

// `check` runs the tests and enforces the floor, so CI catches coverage regressions.
tasks.named("check").configure {
    dependsOn(tasks.named("jacocoTestCoverageVerification"))
}

// ---------------------------------------------------------------------------------------------------------
// In-game test mod (never shipped)
// ---------------------------------------------------------------------------------------------------------

val gameTestSourceSet = project.extensions.getByType(SourceSetContainer::class.java).create("gameTest")

configurations.getByName(gameTestSourceSet.compileClasspathConfigurationName)
    .extendsFrom(configurations.getByName("compileClasspath"))
configurations.getByName(gameTestSourceSet.runtimeClasspathConfigurationName)
    .extendsFrom(configurations.getByName("runtimeClasspath"))
gameTestSourceSet.compileClasspath += mainSourceSet.output
gameTestSourceSet.compileClasspath += mainSourceSet.compileClasspath
gameTestSourceSet.runtimeClasspath += mainSourceSet.output
gameTestSourceSet.runtimeClasspath += mainSourceSet.runtimeClasspath
// The game-test mod is plain Java 8. Leaving Jabel's compiler plugin on this source set's classpath makes javac
// auto-load it through ServiceLoader, which then needs ByteBuddy's agent (not present here).
gameTestSourceSet.compileClasspath = gameTestSourceSet.compileClasspath
    .filter { !it.name.contains("jabel") }

val isGameTestRun = project.hasProperty("gameTest") ||
    gradle.startParameter.taskNames.any { it.contains("gameTestServer") }

if (isGameTestRun) {
    // Only this mod's package is instrumented, and the file is dumped when the game calls System.exit (the game
    // test's exitJava with halt=false), so a crashed or hung run simply leaves no data behind.
    //
    // The data is NOT merged into jacocoTestReport: a dev launch rewrites every class through ASM, so the class
    // ids cannot match the files on disk. It is produced so that a run without the dev transformers - a
    // production launcher - can be merged by pointing executionData at it, and jacocoGameTestReport below
    // reports it against RFB's post-transform class dump.
    val jacocoProvider = objects.newInstance(JacocoAgentArgumentProvider::class.java)
    jacocoProvider.agent.from(configurations.named("jacocoAgent"))
    jacocoProvider.destFile.set(
        layout.buildDirectory.file("jacoco/gameTest.exec")
            .map { it.asFile.absolutePath }
    )
    jacocoProvider.includes.set("com.mjr.extraplanets.*")
    jacocoProvider.append.set(false)

    tasks.withType<JavaExec>().configureEach {
        if (name.startsWith("runServer") || name.startsWith("runClient")) {
            classpath += gameTestSourceSet.output
            systemProperty("extraplanets.gameTest", "true")
            // A reused dev world may reference blocks from mods that are no longer installed; the game test is
            // not about that, so answer FML's "continue anyway?" prompt up front instead of blocking on stdin.
            systemProperty("fml.queryResult", "confirm")
            jvmArgumentProviders.add(jacocoProvider)
        }
    }
}

tasks.named("check").configure {
    dependsOn(gameTestSourceSet.classesTaskName)
}

// ---------------------------------------------------------------------------------------------------------
// In-game coverage report
// ---------------------------------------------------------------------------------------------------------
// To report in-game coverage the class files on disk have to be the *post-transform* bytes JaCoCo saw. The GTNH boot
// layer (RFB) can dump those under <run dir>/RFB_CLASS_DUMP/<loader>/, but only when class dumping is explicitly
// enabled. Until that flag is set for gameTest runs this report has no class directories and skips itself; the exec
// file above is still written, so a production (untransformed) build can be merged by pointing executionData at it.
val inGameDump = project.file(
    "${findProperty("runServerWorkingDirectory") ?: "run"}/RFB_CLASS_DUMP/Launch"
)

tasks.register<JacocoReport>("jacocoGameTestReport") {
    group = "verification"
    description = "Reports coverage from the in-game game test run, using the post-transform classes FML loaded"
    val execFile = project.layout.buildDirectory.file("jacoco/gameTest.exec").get().asFile
    // No `onlyIf`: a script closure cannot be stored in the configuration cache. An empty execution-data
    // collection makes JaCoCo skip the task instead, which is the same outcome.
    executionData.setFrom(project.files(execFile).filter { it.exists() })

    reports {
        xml.required.set(true)
        html.required.set(true)
        csv.required.set(false)
        xml.outputLocation.set(project.layout.buildDirectory.file("reports/jacoco/gameTest/jacocoGameTestReport.xml"))
        html.outputLocation.set(project.layout.buildDirectory.dir("reports/jacoco/gameTest/html"))
    }

    // The dump carries every loader's classes; only this mod's are ours.
    classDirectories.setFrom(
        project.files(inGameDump).asFileTree.matching {
            include("com/mjr/extraplanets/**")
            exclude(
                "com/mjr/extraplanets/Tags.class",
                // The in-game test mod itself is not part of the mod.
                "com/mjr/extraplanets/gametest/**"
            )
        }
    )
    sourceDirectories.setFrom(mainSourceSet.allJava.srcDirs)
}

tasks.register("gameTestServer") {
    group = "verification"
    description = "Boots a dedicated server, runs the in-game game test mod, and exits with its result"
    if (isGameTestRun && tasks.names.contains("runServer")) dependsOn(tasks.named("runServer"))
    finalizedBy(tasks.named("jacocoGameTestReport"))
}

// ---------------------------------------------------------------------------------------------------------
// Headless smoke test
// ---------------------------------------------------------------------------------------------------------
// `./gradlew runServer -PsmokeTest` makes the forked game exit itself once FML reports load complete, so CI can
// boot a real server without an operator. See SmokeTestHook.
if (project.hasProperty("smokeTest")) {
    val smokeTestDelay = (findProperty("smokeTestDelay") as String?) ?: "2000"
    tasks.withType<JavaExec>().configureEach {
        systemProperty("extraplanets.smokeTest", "true")
        systemProperty("extraplanets.smokeTest.delay", smokeTestDelay)
    }
}
