package com.shiftmate.di

import android.content.Context
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.shiftmate.data.local.dao.RoleDao
import com.shiftmate.data.local.dao.ShiftEntryDao
import com.shiftmate.data.local.dao.ShiftProfileDao
import com.shiftmate.data.local.dao.ShiftRequestDao
import com.shiftmate.data.local.dao.ShiftRuleDao
import com.shiftmate.data.local.dao.StaffDao
import com.shiftmate.data.local.dao.TimeBlockDao
import com.shiftmate.data.local.database.ShiftMateDatabase
import com.shiftmate.domain.scheduler.ShiftScheduler
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/** Drop and recreate shift_entries to add nullable blockId + custom spot-shift columns.
 *  All other tables (roles, staff, blocks, rules, profiles, requests) are untouched. */
private val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("DROP TABLE IF EXISTS `shift_entries`")
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS `shift_entries` (
                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                `staffId` INTEGER NOT NULL,
                `blockId` INTEGER,
                `date` TEXT NOT NULL,
                `customStart` TEXT,
                `customEnd` TEXT,
                `customLabel` TEXT,
                FOREIGN KEY(`staffId`) REFERENCES `staff`(`id`) ON UPDATE NO_ACTION ON DELETE CASCADE,
                FOREIGN KEY(`blockId`) REFERENCES `time_blocks`(`id`) ON UPDATE NO_ACTION ON DELETE CASCADE
            )
        """.trimIndent())
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_shift_entries_staffId` ON `shift_entries` (`staffId`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_shift_entries_blockId` ON `shift_entries` (`blockId`)")
    }
}

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext ctx: Context): ShiftMateDatabase =
        Room.databaseBuilder(ctx, ShiftMateDatabase::class.java, "shiftmate.db")
            .addMigrations(MIGRATION_2_3)
            .fallbackToDestructiveMigration()
            .build()

    @Provides fun provideRoleDao(db: ShiftMateDatabase): RoleDao = db.roleDao()
    @Provides fun provideStaffDao(db: ShiftMateDatabase): StaffDao = db.staffDao()
    @Provides fun provideTimeBlockDao(db: ShiftMateDatabase): TimeBlockDao = db.timeBlockDao()
    @Provides fun provideShiftRuleDao(db: ShiftMateDatabase): ShiftRuleDao = db.shiftRuleDao()
    @Provides fun provideShiftRequestDao(db: ShiftMateDatabase): ShiftRequestDao = db.shiftRequestDao()
    @Provides fun provideShiftEntryDao(db: ShiftMateDatabase): ShiftEntryDao = db.shiftEntryDao()
    @Provides fun provideShiftProfileDao(db: ShiftMateDatabase): ShiftProfileDao = db.shiftProfileDao()

    @Provides
    @Singleton
    fun provideShiftScheduler(): ShiftScheduler = ShiftScheduler()
}
