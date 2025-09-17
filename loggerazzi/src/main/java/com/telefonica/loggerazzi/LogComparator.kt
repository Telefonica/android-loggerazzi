package com.telefonica.loggerazzi

import java.lang.StringBuilder

interface LogComparator<LogType> {
    fun compare(recorded: List<LogType>, golden: List<LogType>): String?
}

class DefaultLogComparator<LogType> : LogComparator<LogType> {
    override fun compare(recorded: List<LogType>, golden: List<LogType>): String? {
        if (recorded.size != golden.size) {
            return "Different number of lines: golden=${golden.size}, recorded=${recorded.size}"
        }

        val compareResult = StringBuilder()
        for (i in recorded.indices) {
            if (recorded[i] != golden[i]) {
                compareResult.appendLine("Different line at index $i: recorded=${recorded[i]}, golden=${golden[i]}")
            }
        }

        return compareResult.toString().takeIf { it.isNotEmpty() }
    }
}