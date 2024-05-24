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
    fun test1() {
        recorder.record("My log")
        recorder.record("My log 2")
        recorder.record("My log 3")
    }

    @Test
    fun test2() {
        recorder.record("My second log")
        recorder.record("My second logs 2")
        recorder.record("My second log 3")
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
