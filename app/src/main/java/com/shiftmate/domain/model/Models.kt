package com.shiftmate.domain.model

import java.time.LocalDate

data class Role(
    val id: Long = 0,
    val name: String,
    val colorIndex: Int = 0,
    val defaultHoursPerWeek: Int = 40,
    val defaultDaysPerMonth: Int = 20,
    val minPerShift: Int = 0
)

data class Staff(
    val id: Long = 0,
    val name: String,
    val roleId: Long,
    val hourlyWage: Int = 1000,
    val maxHoursPerWeek: Int = 40,
    val maxDaysPerMonth: Int = 20
)

data class TimeBlock(
    val id: Long = 0,
    val storeId: Long = 1,
    val name: String,
    val start: String,   // "09:00"
    val end: String,     // "15:00"
    val required: Int = 1,
    val sortOrder: Int = 0
) {
    val durationHours: Double get() {
        val (sh, sm) = start.split(":").map { it.toInt() }
        val (eh, em) = end.split(":").map { it.toInt() }
        return ((eh * 60 + em) - (sh * 60 + sm)) / 60.0
    }
}

data class ShiftRule(
    val id: Long = 0,
    val storeId: Long = 1,
    val openTime: String = "09:00",
    val closeTime: String = "22:00",
    val maxConsecDays: Int = 5
)

/** ○ = available, △ = prefer off, × = day off */
enum class RequestStatus { AVAILABLE, PREFER_OFF, DAY_OFF }

data class ShiftRequest(
    val id: Long = 0,
    val staffId: Long,
    val blockId: Long,
    val date: LocalDate,
    val status: RequestStatus = RequestStatus.AVAILABLE
)

data class ShiftEntry(
    val id: Long = 0,
    val staffId: Long,
    val blockId: Long?,            // null = spot/custom entry
    val date: LocalDate,
    val customStart: String? = null,   // "HH:MM" for spot entries
    val customEnd: String? = null,
    val customLabel: String? = null
) {
    val isCustom: Boolean get() = blockId == null

    /** Duration computed from custom times (block-based entries use block.durationHours instead) */
    val customDurationHours: Double get() {
        if (customStart == null || customEnd == null) return 0.0
        return try {
            val (sh, sm) = customStart.split(":").map { it.toInt() }
            val (eh, em) = customEnd.split(":").map { it.toInt() }
            maxOf(0.0, ((eh * 60 + em) - (sh * 60 + sm)) / 60.0)
        } catch (_: Exception) { 0.0 }
    }

    /** Display string shown in the shift table cell */
    val displayLabel: String get() = when {
        isCustom -> customLabel?.take(3)?.ifBlank { null }
            ?: "${customStart?.take(5) ?: ""}-${customEnd?.take(5) ?: ""}"
        else -> ""
    }

    /** Time range string for CSV/PDF */
    val timeRange: String get() = when {
        isCustom -> "${customStart ?: ""}〜${customEnd ?: ""}"
        else -> ""
    }
}

data class GeneratedShift(
    val entries: List<ShiftEntry>,
    val violations: List<String>
)
