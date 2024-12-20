/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Attributions:
 *     Work based on Roborazzi reports -> https://github.com/takahirom/roborazzi
 */
package com.telefonica.loggerazzi

import java.io.File

fun getFailuresReport(
    failureEntries: List<FailureEntry>,
    reportsDir: File,
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
            append("<td class=\"$imgClass\"><iframe $imgAttributes src=\"${entry.golden.relativeTo(reportsDir)}\"></iframe></td>")
            append("<td class=\"$imgClass\"><iframe $imgAttributes src=\"${entry.failure.relativeTo(reportsDir)}\"></iframe></td>")
            append("<td class=\"$imgClass\"><iframe $imgAttributes src=\"${entry.recorded.relativeTo(reportsDir)}\"></iframe></td>")
            append("</tr>")
        }
        append("</tbody>")
        append("</table>")
    }
}

fun getRecordedReport(
    recordedFiles: List<File>,
    reportsDir: File,
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
            append("<td class=\"$imgClass\"><iframe $imgAttributes src=\"${recorded.relativeTo(reportsDir)}\"></iframe></td>")
            append("</tr>")
        }
        append("</tbody>")
        append("</table>")
    }
}