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

@Suppress("unused")
class AnyOrderLogComparator<LogType> : LogComparator<LogType> {
    override fun compare(recorded: List<LogType>, golden: List<LogType>): String? {
        val goldenSet = golden.toSet()
        val recordedSet = recorded.toSet()

        val missing = goldenSet - recordedSet
        val extra = recordedSet - goldenSet

        if (missing.isEmpty() && extra.isEmpty()) {
            return null
        }

        val result = StringBuilder()
        if (missing.isNotEmpty()) {
            result.appendLine("Missing entries (in golden but not in recorded): ${missing.toList()}")
        }
        if (extra.isNotEmpty()) {
            result.appendLine("Extra entries (in recorded but not in golden): ${extra.toList()}")
        }

        return result.toString().trimEnd()
    }
}
