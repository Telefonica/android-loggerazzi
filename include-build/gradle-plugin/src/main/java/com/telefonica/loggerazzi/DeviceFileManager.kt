package com.telefonica.loggerazzi

import com.android.build.gradle.TestedExtension
import com.android.build.gradle.api.TestVariant
import com.android.build.gradle.internal.tasks.DeviceProviderInstrumentTestTask
import com.android.build.gradle.internal.testing.ConnectedDevice
import com.android.ddmlib.CollectingOutputReceiver
import com.android.ddmlib.FileListingService
import com.android.ddmlib.FileListingService.FileEntry
import com.android.ddmlib.IDevice
import org.gradle.api.file.RegularFile
import java.io.File

fun DeviceProviderInstrumentTestTask.deviceFileManager(): DeviceFileManager =
    DeviceFileManager(this)

class DeviceFileManager(
    private val testTask: DeviceProviderInstrumentTestTask,
) {
    private val extension: TestedExtension = testTask
        .project
        .extensions
        .findByType(TestedExtension::class.java)
        ?: throw RuntimeException("TestedExtension not found")

    @Suppress("DEPRECATION")
    private val testedVariant: TestVariant = extension
        .testVariants
        .firstOrNull { it.name == testTask.variantName }
        ?: throw RuntimeException("TestVariant not found")

    fun pullRecordedLogs(
        destinationPath: String,
    ) {
        pullLogs("recorded", destinationPath)
    }

    fun pullFailuresLogs(
        destinationPath: String,
    ) {
        pullLogs("failures", destinationPath)
    }

    fun clearAllLogs() {
        withConnectedDevices { devices ->
            devices.forEach {
                val receiver = CollectingOutputReceiver()
                it.iDevice.executeShellCommand("rm -rf ${getDeviceLoggerazziRootAbsolutePath()}", receiver)
                println(receiver.output)
            }
        }
    }

    private fun String.toFileEntry(): FileEntry {
        val parts = this.split("/")
        var fileEntry = FileEntry(null, null, FileListingService.TYPE_DIRECTORY, true)
        parts.forEach {
            fileEntry = FileEntry(fileEntry, it, FileListingService.TYPE_DIRECTORY, false)
        }
        return fileEntry
    }

    private fun getDeviceLoggerazziRootAbsolutePath(): String =
        "${FileListingService.DIRECTORY_SDCARD}/Download/loggerazzi-logs/${testedVariant.applicationId}"
    private fun getDeviceLoggerazziSubfolderAbsolutePath(subFolder: String): String =
        "${getDeviceLoggerazziRootAbsolutePath()}/$subFolder"

    @Suppress("UnstableApiUsage")
    private fun withConnectedDevices(runnable: (List<ConnectedDevice>) -> Unit) {
        testTask.deviceProviderFactory.getDeviceProvider(
            testTask.project.provider {
                RegularFile { File(extension.adbExecutable.absolutePath) }
            },
            System.getenv("ANDROID_SERIAL"),
        ).let {
            it.use {
                runnable(it.devices.filterIsInstance<ConnectedDevice>())
            }
        }
    }

    private fun pullLogs(
        loggerazziSubFolderInDevice: String,
        destinationPath: String,
    ) {
        val fileEntry = getDeviceLoggerazziSubfolderAbsolutePath(loggerazziSubFolderInDevice).toFileEntry()
        withConnectedDevices { devices ->
            devices.forEach {
                pullFolderFiles(
                    fileEntry,
                    it.iDevice,
                    destinationPath,
                )
            }
        }
    }

    private fun pullFolderFiles(
        loggerazziDeviceFolder: FileEntry,
        device: IDevice,
        destinationPath: String,
    ) {
        device.fileListingService.getChildrenSync(loggerazziDeviceFolder).forEach {
            device.pullFile(it.fullPath, "$destinationPath/${it.name}")
        }
    }
}