<p>
    <img src="https://img.shields.io/badge/Platform-Android-brightgreen" />
    <img src="https://img.shields.io/badge/Support-%3E%3D%20Android%206.0-brightgreen" />
</p>

# Android Loggerazzi

Logs snapshot testing for Android Instrumentation tests.

## Introduction

Similarly to screenshot testing, which is an easy and maintainable approach to ensure your application UI does not get broken, Loggerazzi brings the same "snapshoting" idea, but for your analytics or any other application logs.

## Usage

You just need to include the Loggerazzi plugin in your project, and the rule in your test class (configuring it properly).

In order to universally include all your existing application tests, rule can be added to your tests base class.

To include the plugin, just include plugin into your application or library module build.gradle using the plugins DSL:

```gradle
plugins {
  id("com.telefonica:loggerazzi-plugin") version $loggerazzi_version
}
```

Also, include the rule dependency in your application or library dependencies block:

```gradle
dependencies {
    ...
    androidTestImplementation "com.telefonica:loggerazzi:$loggerazzi_version"
}
```

Finally, add Loggerazzi rule to your test class (or base instrumentation tests class), where a logs recorded must be provided (Check configuration section):

```kotlin
open class BaseInstrumentationTest {
    @get:Rule
    val loggerazziRule: LoggerazziRule = LoggerazziRule(
        recorder = fakeAnalyticsTracker
    )
}
```

For more details, check included [application example](app).

## Execution

### Verification mode

Regular `connectedXXXXAndroidTest` target invocation is enough for verifications against previously generated logs baselines. Android Studio executions should also work seamlessly.

```bash
./gradlew :app:connectedDebugAndroidTest
```

In case of any failures due logs verifications, regular junit reports include failed tests and comparation failure reason.

Additionally, an specific Loggerazzi report is generated at --> `build/reports/androidTests/connected/debug/loggerazzi/failures.html`

### Recording mode

When the logs baseline needs to be updated, it's enough to include `-Pandroid.testInstrumentationRunnerArguments.record=true`.

```bash
./gradlew :app:connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.record=true
```

This execution won't perform any logs verification, instead, it will execute tests to generate new logs, placing them in the corresponding tests baseline directory.

A loggerazzi report with all recorded logs is generated at --> `build/reports/androidTests/connected/debug/loggerazzi/recorded.html`

## Execution from external runners

In situations where the regular `connectedXXXXAndroidTest` target is not used because execution is performed by a different external test runner (such as composer or marathon), two loggerazzi gradle tasks are provided which should be executed manually before and after external test runner execution:
 - `loggerazziBefore[VariantName]AndroidTest`
 - `loggerazziAfter[VariantName]AndroidTest`

In case test execution is triggered from any gradle task, here's an example on how to configure dependencies with loggerazzi tasks:

```gradle
project.afterEvaluate {
    project.tasks.findByName("externalTestRunner[VariantName]Execution")
        .dependsOn("loggerazziBefore[VariantName]AndroidTest")
        .finalizedBy("loggerazziAfter[VariantName]AndroidTest")
}
```

## Configuration

### Logs recorder

Loggerazzi rule must be configured with a [LogsRecorder](loggerazzi/src/main/java/com/telefonica/loggerazzi/LogsRecorder.kt) implementation which will be used by Loggerazzi to obtain logs recorded at the end of the test. This should be usually implemented as the replacement of the original application tracker in tests.

Example:

```kotlin
class FakeAnalyticsTracker : AnalyticsTracker, LogsRecorder<String> {

    private val logs = mutableListOf<String>()

    override fun clear() {
        logs.clear()
    }

    override fun getRecordedLogs(): List<String> =
        logs.mapIndexed { index, s ->
            "$index: $s"
        }

    override fun init() {}

    override fun trackScreenView(screen: AnalyticsScreen) {
        logs.add("trackScreenView: $screen")
    }

    override fun trackEvent(event: Event.GenericEvent) {
        logs.add("trackEvent: $event")
    }
}
```

### Logs comparator

By default, Loggerazzi rule compares recorded logs by ensuring these are equal and in same order than the baseline logs.

In case a different comparation mechanism is needed (such as ignoring the order of the events, or ignoring certain logs), you can implement an specific [LogComparator](loggerazzi/src/main/java/com/telefonica/loggerazzi/LogComparator.kt), which can be provided to the LoggerazziRule on its creation.
