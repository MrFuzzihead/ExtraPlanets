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
                ",includes=" + includes.get() +
                // The bytes the agent actually sees, i.e. after FML's transformers. Diffing one of these against the
                // compiled class is how we find out what rewrites our classes at load time.
                ",classdumpdir=" + java.io.File(destFile.get()).parentFile.resolve("gameTest-classes").absolutePath
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
    // NoStrayTracesTest lints src/main/java, which the redirected working directory puts out of reach.
    systemProperty("extraplanets.projectDir", layout.projectDirectory.asFile.absolutePath)
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
        // ceiling for this codebase is low (measured: 5.26% of 63,485 lines, 2.74% of 28,498 branches).
        // Raise these numbers as tests are added - never lower them. (The ratio moves when dead code is
        // deleted, which is why it jumped in the village refactor: fewer lines, not more tests.)
        rule {
            limit {
                counter = "LINE"
                value = "COVEREDRATIO"
                minimum = BigDecimal("0.051")
            }
            limit {
                counter = "BRANCH"
                value = "COVEREDRATIO"
                minimum = BigDecimal("0.026")
            }
        }
    }
}

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

// Where the in-game agent dumps the classes it actually loaded (see the report section below).
val gameTestClassDump = layout.buildDirectory.dir("jacoco/gameTest-classes")

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
            // The agent appends to its class dump, so a class that was recompiled between runs leaves two copies with
            // different ids behind. Start each run from an empty dump so the report sees exactly what this run loaded.
            // A plain File (not the Provider) is captured, or the configuration cache refuses to store the closure.
            val dumpDir = gameTestClassDump.get().asFile
            doFirst { dumpDir.deleteRecursively() }
        }
    }
}

tasks.named("check").configure {
    dependsOn(gameTestSourceSet.classesTaskName)
}

// ---------------------------------------------------------------------------------------------------------
// In-game coverage report
// ---------------------------------------------------------------------------------------------------------
// Why this needs its own class directory: FML's SideTransformer re-emits *every* class through ASM (it does not
// check whether it changed anything), so the bytes the JVM defines are never the bytes on disk - and JaCoCo keys its
// data by a hash of the class it instrumented. Pointing a report at `build/classes/java/main` therefore matches
// nothing at all (measured: 862 of 862 classes "do not match").
//
// The agent's own class dump is exactly those loaded bytes, though, so the report is built against that. The dump is
// per-load-side: on a dedicated server the client-only members are gone, which means this report is a *server-side*
// view and must not be merged with the unit-test data (the probe arrays differ). It stands on its own, and the
// game-test workflow enforces a floor on it.
val gameTestClassesUnpacked = layout.buildDirectory.dir("jacoco/gameTest-classes-unpacked")

val unpackGameTestClasses = tasks.register<Copy>("unpackGameTestClasses") {
    group = "verification"
    description = "Strips JaCoCo's class-id suffix from the in-game class dump so a report can use it"
    // The dump is written while the game JVM exits; copying it before the run finishes races the writer (on Windows
    // that surfaces as "could not get file mode").
    mustRunAfter("runServer")
    from(gameTestClassDump)
    into(gameTestClassesUnpacked)
    // The dump names files "<internal name>.<16 hex digits>.class"; the report wants "<internal name>.class".
    rename { name -> name.replace(Regex("""\.[0-9a-f]{16}\.class$"""), ".class") }
    // A leftover dump can hold two ids for one class; either copy is the same code, so keep the first.
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}

tasks.register<JacocoReport>("jacocoGameTestReport") {
    group = "verification"
    description = "Reports coverage from the in-game game test run, against the classes the JVM actually loaded"
    dependsOn(unpackGameTestClasses)
    mustRunAfter("runServer")

    // No `onlyIf`: a script closure cannot be stored in the configuration cache. An empty execution-data
    // collection makes JaCoCo skip the task instead, which is the same outcome.
    executionData.setFrom(project.files(project.layout.buildDirectory.file("jacoco/gameTest.exec")).filter { it.exists() })
    classDirectories.setFrom(gameTestClassesUnpacked)
    sourceDirectories.setFrom(mainSourceSet.allJava.srcDirs)

    reports {
        xml.required.set(true)
        html.required.set(true)
        csv.required.set(false)
        xml.outputLocation.set(project.layout.buildDirectory.file("reports/jacoco/gameTest/jacocoGameTestReport.xml"))
        html.outputLocation.set(project.layout.buildDirectory.dir("reports/jacoco/gameTest/html"))
    }
}

// A floor for the in-game suite, kept separate from `check` because it needs a game run. The game-test workflow runs
// `gameTestServer jacocoGameTestCoverageVerification`.
tasks.register<JacocoCoverageVerification>("jacocoGameTestCoverageVerification") {
    group = "verification"
    description = "Fails when in-game coverage of the server-side classes drops below the recorded baseline"
    dependsOn(unpackGameTestClasses)
    mustRunAfter("runServer")

    executionData.setFrom(project.files(project.layout.buildDirectory.file("jacoco/gameTest.exec")).filter { it.exists() })
    classDirectories.setFrom(gameTestClassesUnpacked)
    sourceDirectories.setFrom(mainSourceSet.allJava.srcDirs)

    violationRules {
        rule {
            limit {
                counter = "LINE"
                value = "COVEREDRATIO"
                // Measured at 50.86% of the server-side classes (27,481 of 54,030 lines). The denominator is every
                // class the server actually loaded, so it moves as checks load more; keep this under the measurement.
                minimum = BigDecimal("0.49")
            }
        }
    }
}

tasks.register("gameTestServer") {
    group = "verification"
    description = "Boots a dedicated server, runs the in-game game test mod, and exits with its result"
    if (isGameTestRun && tasks.names.contains("runServer")) dependsOn(tasks.named("runServer"))
    finalizedBy(tasks.named("jacocoGameTestReport"))
}

// The golden snapshots (VillageSnapshots) need to know where the sources are and, when recording, the right to
// rewrite their own baseline. `-PrecordGoldens` is the only supported way to regenerate one.
tasks.withType<JavaExec>().configureEach {
    systemProperty("extraplanets.projectDir", layout.projectDirectory.asFile.absolutePath)
    if (project.hasProperty("recordGoldens")) {
        systemProperty("extraplanets.recordGoldens", "true")
    }
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
