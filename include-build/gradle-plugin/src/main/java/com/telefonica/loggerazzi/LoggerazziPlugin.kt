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
            project.tasks
                .withType(DeviceProviderInstrumentTestTask::class.java)
                .configureEach { it.configure() }
        }
    }

    private fun DeviceProviderInstrumentTestTask.configure() {
        val deviceFileManager = deviceFileManager()

        doFirst {
            deviceFileManager.clearAllLogs()
        }

        onTaskCompleted {
            val reportsFolder = reportsDir.get().dir("loggerazzi")
            val recordedFolderFile = reportsFolder.dir("recorded").asFile.apply {
                mkdirs()
                deviceFileManager.pullRecordedLogs(absolutePath)
            }
            val failuresFolderFile = reportsFolder.dir("failures").asFile.apply {
                mkdirs()
                deviceFileManager.pullFailuresLogs(absolutePath)
            }
            val goldenFolderFile = File(getAbsoluteGoldenLogsSourcePath())

            File("${reportsFolder.asFile.absolutePath}/recorded.html").apply {
                createNewFile()
                val recordedFiles = recordedFolderFile.listFiles()?.asList() ?: emptyList()
                val report = LoggerazziReportConst.reportHtml.replace(
                    oldValue = "REPORT_TEMPLATE_BODY",
                    newValue = getRecordedReport(recordedFiles)
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
                            golden = File(goldenFolderFile, failureFile.name)
                        )
                    }
                    val report = LoggerazziReportConst.reportHtml.replace(
                        oldValue = "REPORT_TEMPLATE_BODY",
                        newValue = getFailuresReport(failuresEntries)
                    )
                    writeText(report)
                }
            } else {
                File(getAbsoluteGoldenLogsSourcePath()).apply {
                    mkdirs()
                    deviceFileManager.pullRecordedLogs(absolutePath)
                }
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
            .replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
            .let { "androidTest$it" }
        return "${project.projectDir}/src/$variantSourceFolder/assets/loggerazzi-golden-files"
    }
}
