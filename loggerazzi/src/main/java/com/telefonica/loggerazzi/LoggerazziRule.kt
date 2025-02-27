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

        val recordedLogs = recorder.getRecordedLogs()
        val log = recordedLogs.joinToString("\n") { stringMapper.fromLog(it) }
        val testFile = File(recordedDir, fileName)
        testFile.createNewFile()
        testFile.writeText(log)

        if (InstrumentationRegistry.getArguments().getString("record") != "true" && !isTestIgnored) {
            val goldenFile =
                InstrumentationRegistry.getInstrumentation().context.assets.open(
                    "loggerazzi-golden-files/${testName}.txt"
                )
            val goldenStringLogs = String(goldenFile.readBytes()).takeIf { it.isNotEmpty() }?.split("\n") ?: emptyList()
            val result = comparator.compare(recordedLogs, goldenStringLogs.map { stringMapper.toLog(it) })
            if (result != null) {
                val compareFile = File(failuresDir, fileName)
                compareFile.createNewFile()
                compareFile.writeText(result)
                throw AssertionError("Logs do not match:\n$result")
            }
        }
    }
}