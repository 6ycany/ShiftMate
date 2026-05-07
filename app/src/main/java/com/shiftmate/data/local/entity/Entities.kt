package com.shiftmate.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "roles")
data class RoleEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val colorIndex: Int = 0,
    val defaultHoursPerWeek: Int = 40,
    val defaultDaysPerMonth: Int = 20,
    val minPerShift: Int = 0
)

@Entity(
    tableName = "staff",
    foreignKeys = [ForeignKey(
        entity = RoleEntity::class,
        parentColumns = ["id"],
        childColumns = ["roleId"],
        onDelete = ForeignKey.RESTRICT
    )],
    indices = [Index("roleId")]
)
data class StaffEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val roleId: Long,
    val hourlyWage: Int = 1000,
    val maxHoursPerWeek: Int = 40,
    val maxDaysPerMonth: Int = 20
)

@Entity(tableName = "time_blocks")
data class TimeBlockEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val storeId: Long = 1,
    val name: String,
    val start: String,
    val end: String,
    val required: Int = 1,
    val sortOrder: Int = 0
)

@Entity(tableName = "shift_rules")
data class ShiftRuleEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val storeId: Long = 1,
    val openTime: String = "09:00",
    val closeTime: String = "22:00",
    val maxConsecDays: Int = 5
)

@Entity(
    tableName = "shift_requests",
    foreignKeys = [
        ForeignKey(entity = StaffEntity::class, parentColumns = ["id"], childColumns = ["staffId"], onDelete = ForeignKey.CASCADE),
        ForeignKey(entity = TimeBlockEntity::class, parentColumns = ["id"], childColumns = ["blockId"], onDelete = ForeignKey.CASCADE)
    ],
    indices = [Index("staffId"), Index("blockId")]
)
data class ShiftRequestEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val staffId: Long,
    val blockId: Long,
    val date: String,   // ISO: "2026-05-01"
    val status: String = "AVAILABLE"  // AVAILABLE / PREFER_OFF / DAY_OFF
)

@Entity(
    tableName = "shift_entries",
    foreignKeys = [
        ForeignKey(entity = StaffEntity::class, parentColumns = ["id"], childColumns = ["staffId"], onDelete = ForeignKey.CASCADE),
        ForeignKey(entity = TimeBlockEntity::class, parentColumns = ["id"], childColumns = ["blockId"], onDelete = ForeignKey.CASCADE)
    ],
    indices = [Index("staffId"), Index("blockId")]
)
data class ShiftEntryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val staffId: Long,
    val blockId: Long,
    val date: String    // ISO: "2026-05-01"
)

// ── Saved configuration profiles ─────────────────────────────────
@Entity(tableName = "shift_profiles")
data class ShiftProfileEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val createdAt: Long = System.currentTimeMillis(),
    // Tab-separated records, newline between items
    val rolesData: String = "",   // name|colorIndex|hoursPerWeek|daysPerMonth|minPerShift
    val staffData: String = "",   // name|roleName|wage|maxHours|maxDays
    val blocksData: String = "",  // name|start|end|required
    val ruleData: String = ""     // openTime|closeTime|maxConsecDays
)
