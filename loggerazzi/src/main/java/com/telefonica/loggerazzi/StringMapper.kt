package com.telefonica.loggerazzi

interface StringMapper<LogType> {
    fun fromLog(log: LogType): String

    fun toLog(stringLog: String): LogType
}