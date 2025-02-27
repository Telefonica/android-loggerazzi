package com.telefonica.loggerazzi

import androidx.test.ext.junit.runners.AndroidJUnit4

import org.junit.Test
import org.junit.runner.RunWith

import org.junit.Rule

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class ExampleInstrumentedTest {

    private val recorder = FakeTestRecorder()

    @JvmField
    @Rule
    val loggerazziRule = LoggerazziRule(
        recorder = recorder
    )

    @Test
    fun testSingleLog() {
        recorder.record("My log")
    }

    @Test
    fun testMultipleLogs() {
        recorder.record("My first log")
        recorder.record("My second log")
        recorder.record("My third log")
    }

    @Test
    fun testEmpty() {
        // Empty, just to test empty logs comparation.
    }

    @Test
    @IgnoreLoggerazzi
    fun testIgnoreLoggerazzi() {
        recorder.record("My log")
    }
}

class FakeTestRecorder: LogsRecorder<String> {

    private val logs = mutableListOf<String>()

    fun record(log: String) {
        logs.add(log)
    }

    override fun getRecordedLogs(): List<String> {
        return logs
    }

    override fun clear() {
        logs.clear()
    }
}
