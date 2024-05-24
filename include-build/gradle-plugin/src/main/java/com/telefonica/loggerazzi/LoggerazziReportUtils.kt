package com.telefonica.loggerazzi

import java.io.File

fun getFailuresReport(
    failureEntries: List<FailureEntry>,
): String {
    return buildString {
        append("<h3>Failures Report</h3>")
        val fileNameClass = "flow-text col s3"
        val fileNameStyle = "word-wrap: break-word; word-break: break-all;"
        val imgClass = "col s3"
        val imgAttributes = "style=\"width: 100%; height: 100%; object-fit: cover;\" class=\"modal-trigger\""
        append("<table class=\"highlight\" id=\"\">")
        append("<thead>")
        append("<tr class=\"row\">")
        append("<th class=\"$fileNameClass\" style=\"$fileNameStyle\">File Name</th>")
        append("<th class=\"$imgClass flow-text \">Golden Log</th>")
        append("<th class=\"$imgClass flow-text \">Failure</th>")
        append("<th class=\"$imgClass flow-text \">Recorded Log</th>")
        append("</tr>")
        append("</thead>")
        append("<tbody>")
        failureEntries.forEach { entry ->
            append("<tr class=\"row\">")
            append("<td class=\"$fileNameClass\" style=\"$fileNameStyle\">${entry.failure.name}</td>")
            append("<td class=\"$imgClass\"><iframe $imgAttributes src=\"${entry.golden.absolutePath}\"></iframe></td>")
            append("<td class=\"$imgClass\"><iframe $imgAttributes src=\"${entry.failure.absolutePath}\"></iframe></td>")
            append("<td class=\"$imgClass\"><iframe $imgAttributes src=\"${entry.recorded.absolutePath}\"></iframe></td>")
            append("</tr>")
        }
        append("</tbody>")
        append("</table>")
    }
}

fun getRecordedReport(
    recordedFiles: List<File>,
): String {
    return buildString {
        append("<h3>Recorded Logs Report</h3>")
        val fileNameClass = "flow-text col s3"
        val fileNameStyle = "word-wrap: break-word; word-break: break-all;"
        val imgClass = "col s9"
        val imgAttributes = "style=\"width: 100%; height: 100%; object-fit: cover;\" class=\"modal-trigger\""
        append("<table class=\"highlight\" id=\"\">")
        append("<thead>")
        append("<tr class=\"row\">")
        append("<th class=\"$fileNameClass\" style=\"$fileNameStyle\">File Name</th>")
        append("<th class=\"$imgClass flow-text \">Recorded Log</th>")
        append("</tr>")
        append("</thead>")
        append("<tbody>")
        recordedFiles.forEach { recorded ->
            append("<tr class=\"row\">")
            append("<td class=\"$fileNameClass\" style=\"$fileNameStyle\">${recorded.name}</td>")
            append("<td class=\"$imgClass\"><iframe $imgAttributes src=\"${recorded.absolutePath}\"></iframe></td>")
            append("</tr>")
        }
        append("</tbody>")
        append("</table>")
    }
}