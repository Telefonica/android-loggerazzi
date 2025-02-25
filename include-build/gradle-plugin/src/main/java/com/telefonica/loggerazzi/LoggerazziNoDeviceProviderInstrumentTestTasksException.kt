package com.telefonica.loggerazzi

class LoggerazziNoDeviceProviderInstrumentTestTasksException : Exception(
    "No device provider instrument test tasks found. Make sure you are applying the Loggerazzi plugin after the Android app/library plugin."
)