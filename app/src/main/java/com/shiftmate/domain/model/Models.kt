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
    val blockId: Long,
    val date: LocalDate
)

data class GeneratedShift(
    val entries: List<ShiftEntry>,
    val violations: List<String>
)
