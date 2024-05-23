package com.telefonica.loggerazzi

import java.io.File

data class FailureEntry(
    val failure: File,
    val recorded: File,
    val golden: File,
)