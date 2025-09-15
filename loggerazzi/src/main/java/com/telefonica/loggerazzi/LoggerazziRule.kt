package com.telefonica.loggerazzi

import android.os.Environment
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.rules.TestWatcher
import org.junit.runner.Description
import java.io.File

class LoggerazziRule(
    recorder: LogsRecorder<String>,
    comparator: LogComparator<String> = DefaultLogComparator(),
) : GenericLoggerazziRule<String>(
    recorder = recorder,
    stringMapper = object : StringMapper<String> {
        override fun fromLog(log: String): String = log
        override fun toLog(stringLog: String): String = stringLog
    },
    comparator = comparator,
)

open class GenericLoggerazziRule<LogType>(
    val recorder: LogsRecorder<LogType>,
    private val stringMapper: StringMapper<LogType>,
    private val comparator: LogComparator<LogType> = DefaultLogComparator(),
) : TestWatcher() {

    private val context = InstrumentationRegistry.getInstrumentation().context

    private val downloadDir = File(
        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).absolutePath
    )
    private val loggerazziDir = File(downloadDir, "loggerazzi-logs/${context.packageName}")
    private val failuresDir = File(loggerazziDir, "failures")
    private val recordedDir = File(loggerazziDir, "recorded")

    override fun starting(description: Description?) {
        super.starting(description)

        recorder.clear()
        if (!failuresDir.exists()) {
            failuresDir.mkdirs()
        }
        if (!recordedDir.exists()) {
            recordedDir.mkdirs()
        }
    }

    override fun succeeded(description: Description?) {
        super.succeeded(description)

        val isTestIgnored = description?.getAnnotation(IgnoreLoggerazzi::class.java) != null

        val testName = "${description?.className}_${description?.methodName}"
        val fileName = "${testName}.${System.nanoTime()}"

        val recordedLogs: List<LogType>
        if (InstrumentationRegistry.getArguments().getString("record") != "true" && !isTestIgnored) {
            val goldenFile =
                InstrumentationRegistry.getInstrumentation().context.assets.open(
                    "loggerazzi-golden-files/${testName}.txt"
                )
            val goldenStringLogs = String(goldenFile.readBytes()).takeIf { it.isNotEmpty() }?.split("\n") ?: emptyList()
            val comparison = compare(goldenStringLogs)
            if (!comparison.success) {
                val compareFile = File(failuresDir, fileName)
                compareFile.createNewFile()
                compareFile.writeText(comparison.result!!)
                throw AssertionError("Logs do not match:\n${comparison.result}")
            }
            recordedLogs = comparison.recordedLogs
        } else {
            recordedLogs = recorder.getRecordedLogs()
        }

        val log = recordedLogs.joinToString("\n") { stringMapper.fromLog(it) }
        val testFile = File(recordedDir, fileName)
        testFile.createNewFile()
        testFile.writeText(log)
    }

    private fun compare(goldenStringLogs: List<String>): Comparison<LogType> {
        val startTime = System.currentTimeMillis()
        var comparison: Comparison<LogType>
        do {
            val recordedLogs = recorder.getRecordedLogs()
            val result = comparator.compare(recordedLogs, goldenStringLogs.map { stringMapper.toLog(it) })
            comparison = Comparison(result, recordedLogs)
            if (!comparison.success) {
                Thread.sleep(RESULT_POLLING_INTERVAL_MS)
            }
        } while (!comparison.success && System.currentTimeMillis() - startTime < RESULT_TIMEOUT_MS)
        return comparison
    }

    private data class Comparison<LogType>(
        val result: String?,
        val recordedLogs: List<LogType>,
    ) {
        val success: Boolean
            get() = result == null
    }

    private companion object {
        const val RESULT_POLLING_INTERVAL_MS = 500L
        const val RESULT_TIMEOUT_MS = 5000L
    }
}