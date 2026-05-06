package com.shiftmate.data.repository

import com.shiftmate.data.local.entity.*
import com.shiftmate.domain.model.*
import java.time.LocalDate

fun RoleEntity.toDomain() = Role(id, name, colorIndex, defaultHoursPerWeek, defaultDaysPerMonth, minPerShift)
fun Role.toEntity() = RoleEntity(id, name, colorIndex, defaultHoursPerWeek, defaultDaysPerMonth, minPerShift)

fun StaffEntity.toDomain() = Staff(id, name, roleId, hourlyWage, maxHoursPerWeek, maxDaysPerMonth)
fun Staff.toEntity() = StaffEntity(id, name, roleId, hourlyWage, maxHoursPerWeek, maxDaysPerMonth)

fun TimeBlockEntity.toDomain() = TimeBlock(id, storeId, name, start, end, required, sortOrder)
fun TimeBlock.toEntity() = TimeBlockEntity(id, storeId, name, start, end, required, sortOrder)

fun ShiftRuleEntity.toDomain() = ShiftRule(id, storeId, openTime, closeTime, maxConsecDays)
fun ShiftRule.toEntity() = ShiftRuleEntity(id, storeId, openTime, closeTime, maxConsecDays)

fun ShiftRequestEntity.toDomain() = ShiftRequest(
    id, staffId, blockId,
    LocalDate.parse(date),
    RequestStatus.valueOf(status)
)
fun ShiftRequest.toEntity() = ShiftRequestEntity(
    id, staffId, blockId,
    date.toString(),
    status.name
)

fun ShiftEntryEntity.toDomain() = ShiftEntry(id, staffId, blockId, LocalDate.parse(date))
fun ShiftEntry.toEntity() = ShiftEntryEntity(id, staffId, blockId, date.toString())
