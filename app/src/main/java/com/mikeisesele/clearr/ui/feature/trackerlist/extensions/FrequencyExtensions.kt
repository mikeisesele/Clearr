package com.mikeisesele.clearr.ui.feature.trackerlist.extensions

import com.mikeisesele.clearr.data.model.Frequency

internal fun Frequency.displayName(): String = when (this) {
    Frequency.MONTHLY -> "Monthly"
    Frequency.WEEKLY -> "Weekly"
    Frequency.QUARTERLY -> "Quarterly"
    Frequency.TERMLY -> "Termly"
    Frequency.BIANNUAL -> "Biannual"
    Frequency.ANNUAL -> "Annual"
    Frequency.CUSTOM -> "Custom"
}
