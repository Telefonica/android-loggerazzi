package com.telefonica.loggerazzi

import com.android.build.gradle.internal.tasks.AndroidVariantTask
import com.android.build.gradle.internal.tasks.DeviceProviderInstrumentTestTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.internal.build.event.BuildEventListenerRegistryInternal
import org.gradle.tooling.events.OperationCompletionListener
import java.io.File
import javax.inject.Inject

class LoggerazziPlugin @Inject constructor(
    private val buildEventListenerRegistry: BuildEventListenerRegistryInternal
) : Plugin<Project> {

    override fun apply(project: Project) {
        project.afterEvaluate {

            val deviceProviderInstrumentTestTasks = project.tasks
                .withType(DeviceProviderInstrumentTestTask::class.java)

            if (deviceProviderInstrumentTestTasks.isEmpty()) {
                throw LoggerazziNoDeviceProviderInstrumentTestTasksException()
            }

            deviceProviderInstrumentTestTasks
                .forEach { deviceProviderTask ->
                    val capitalizedVariant = deviceProviderTask.variantName.capitalizeFirstLetter()
                    val beforeTaskName = "loggerazziBefore$capitalizedVariant"
                    project.tasks.register(beforeTaskName, Task::class.java) { task ->
                        task.doFirst {
                            deviceProviderTask.deviceFileManager().clearAllLogs()
                        }
                    }
                    deviceProviderTask.dependsOn(beforeTaskName)

                    val afterTaskName = "loggerazziAfter$capitalizedVariant"
                    project.tasks.register(afterTaskName, Task::class.java) { task ->
                        task.doLast {
                            deviceProviderTask.afterExecution()
                        }
                    }
                    deviceProviderTask.onTaskCompleted {
                        deviceProviderTask.afterExecution()
                    }
                }
        }
    }

    private fun DeviceProviderInstrumentTestTask.afterExecution() {
        val deviceFileManager = deviceFileManager()

        val reportsFolder = reportsDir.get().dir("loggerazzi")
        val recordedFolderFile = reportsFolder.dir("recorded").asFile.apply {
            mkdirs()
            deviceFileManager.pullRecordedLogs(absolutePath)
            processAndFilterResults()
        }
        val failuresFolderFile = reportsFolder.dir("failures").asFile.apply {
            mkdirs()
            deviceFileManager.pullFailuresLogs(absolutePath)
            processAndFilterResults()
        }
        val goldenForFailuresReportFolderFile = reportsFolder.dir("golden").asFile.apply {
            mkdirs()
        }
        val goldenFolderFile = File(getAbsoluteGoldenLogsSourcePath())

        File("${reportsFolder.asFile.absolutePath}/recorded.html").apply {
            createNewFile()
            val recordedFiles = recordedFolderFile.listFiles()?.asList() ?: emptyList()
            val report = LoggerazziReportConst.reportHtml.replace(
                oldValue = "REPORT_TEMPLATE_BODY",
                newValue = getRecordedReport(recordedFiles, reportsFolder.asFile)
            )
            writeText(report)
        }

        if (project.properties["android.testInstrumentationRunnerArguments.record"] != "true") {
            File("${reportsFolder.asFile.absolutePath}/failures.html").apply {
                createNewFile()
                val failuresFiles = failuresFolderFile.listFiles()?.asList() ?: emptyList()
                val failuresEntries = failuresFiles.map { failureFile ->
                    FailureEntry(
                        failure = failureFile,
                        recorded = File(recordedFolderFile, failureFile.name),
                        golden = File(goldenFolderFile, failureFile.name).let {
                            it.copyTo(
                                File(goldenForFailuresReportFolderFile, it.name),
                                true
                            )
                        }
                    )
                }
                val report = LoggerazziReportConst.reportHtml.replace(
                    oldValue = "REPORT_TEMPLATE_BODY",
                    newValue = getFailuresReport(failuresEntries, reportsFolder.asFile)
                )
                writeText(report)
            }
        } else {
            File(getAbsoluteGoldenLogsSourcePath()).apply {
                mkdirs()
                recordedFolderFile.copyRecursively(this, true)
            }
        }
    }

    private fun Task.onTaskCompleted(onCompleted: () -> Unit) {
        buildEventListenerRegistry.onTaskCompletion(
            project.provider {
                OperationCompletionListener {
                    if (it.descriptor.name != path) {
                        return@OperationCompletionListener
                    }
                    onCompleted()
                }
            }
        )
    }

    private fun AndroidVariantTask.getAbsoluteGoldenLogsSourcePath(): String {
        val variantSourceFolder = this
            .variantName
            .replace("AndroidTest", "")
            .capitalizeFirstLetter()
            .let { "androidTest$it" }
        return "${project.projectDir}/src/$variantSourceFolder/assets/loggerazzi-golden-files"
    }

    private fun String.capitalizeFirstLetter(): String {
        return replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
    }

    private fun File.processAndFilterResults() {
        listFiles()
            ?.groupBy {
                it.name.substringBeforeLast(".")
            }
            ?.forEach { (key, filesGroup) ->
                val lastFile = filesGroup.maxByOrNull {
                    it.name.substringAfterLast(".").toLong()
                }
                filesGroup.forEach { file ->
                    if (file != lastFile) {
                        file.delete()
                    }
                }
                lastFile?.renameTo(File(this, "$key.txt"))
            }
    }
}
