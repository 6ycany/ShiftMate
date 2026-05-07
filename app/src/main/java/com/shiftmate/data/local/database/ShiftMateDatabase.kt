package com.shiftmate.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.shiftmate.data.local.dao.*
import com.shiftmate.data.local.entity.*

@Database(
    entities = [
        RoleEntity::class,
        StaffEntity::class,
        TimeBlockEntity::class,
        ShiftRuleEntity::class,
        ShiftRequestEntity::class,
        ShiftEntryEntity::class,
        ShiftProfileEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class ShiftMateDatabase : RoomDatabase() {
    abstract fun roleDao(): RoleDao
    abstract fun staffDao(): StaffDao
    abstract fun timeBlockDao(): TimeBlockDao
    abstract fun shiftRuleDao(): ShiftRuleDao
    abstract fun shiftRequestDao(): ShiftRequestDao
    abstract fun shiftEntryDao(): ShiftEntryDao
    abstract fun shiftProfileDao(): ShiftProfileDao
}
