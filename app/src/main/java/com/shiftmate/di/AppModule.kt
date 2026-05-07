package com.shiftmate.di

import android.content.Context
import androidx.room.Room
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

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext ctx: Context): ShiftMateDatabase =
        Room.databaseBuilder(ctx, ShiftMateDatabase::class.java, "shiftmate.db")
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
