package com.telefonica.loggerazzi

interface LogsRecorder<LogType> {
    fun clear()
    fun getRecordedLogs(): List<LogType>
}
