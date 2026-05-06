package com.shiftmate.data.local.dao

import androidx.room.*
import com.shiftmate.data.local.entity.*
import kotlinx.coroutines.flow.Flow

@Dao
interface RoleDao {
    @Query("SELECT * FROM roles ORDER BY name")
    fun getAllFlow(): Flow<List<RoleEntity>>

    @Query("SELECT * FROM roles ORDER BY name")
    suspend fun getAll(): List<RoleEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(role: RoleEntity): Long

    @Update
    suspend fun update(role: RoleEntity)

    @Delete
    suspend fun delete(role: RoleEntity)

    @Query("SELECT COUNT(*) FROM staff WHERE roleId = :roleId")
    suspend fun countStaffByRole(roleId: Long): Int
}

@Dao
interface StaffDao {
    @Query("SELECT * FROM staff ORDER BY name")
    fun getAllFlow(): Flow<List<StaffEntity>>

    @Query("SELECT * FROM staff ORDER BY name")
    suspend fun getAll(): List<StaffEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(staff: StaffEntity): Long

    @Update
    suspend fun update(staff: StaffEntity)

    @Delete
    suspend fun delete(staff: StaffEntity)
}

@Dao
interface TimeBlockDao {
    @Query("SELECT * FROM time_blocks WHERE storeId = :storeId ORDER BY sortOrder, start")
    fun getAllFlow(storeId: Long = 1): Flow<List<TimeBlockEntity>>

    @Query("SELECT * FROM time_blocks WHERE storeId = :storeId ORDER BY sortOrder, start")
    suspend fun getAll(storeId: Long = 1): List<TimeBlockEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(block: TimeBlockEntity): Long

    @Update
    suspend fun update(block: TimeBlockEntity)

    @Delete
    suspend fun delete(block: TimeBlockEntity)

    @Query("DELETE FROM time_blocks WHERE storeId = :storeId")
    suspend fun deleteAll(storeId: Long = 1)
}

@Dao
interface ShiftRuleDao {
    @Query("SELECT * FROM shift_rules WHERE storeId = :storeId LIMIT 1")
    fun getFlow(storeId: Long = 1): Flow<ShiftRuleEntity?>

    @Query("SELECT * FROM shift_rules WHERE storeId = :storeId LIMIT 1")
    suspend fun get(storeId: Long = 1): ShiftRuleEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(rule: ShiftRuleEntity): Long

    @Update
    suspend fun update(rule: ShiftRuleEntity)
}

@Dao
interface ShiftRequestDao {
    @Query("SELECT * FROM shift_requests WHERE staffId = :staffId AND date LIKE :yearMonth || '%'")
    fun getByStaffAndMonthFlow(staffId: Long, yearMonth: String): Flow<List<ShiftRequestEntity>>

    @Query("SELECT * FROM shift_requests WHERE date LIKE :yearMonth || '%'")
    suspend fun getByMonth(yearMonth: String): List<ShiftRequestEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(request: ShiftRequestEntity): Long

    @Query("DELETE FROM shift_requests WHERE staffId = :staffId AND blockId = :blockId AND date = :date")
    suspend fun delete(staffId: Long, blockId: Long, date: String)

    @Query("DELETE FROM shift_requests WHERE staffId = :staffId AND date LIKE :yearMonth || '%'")
    suspend fun deleteByStaffAndMonth(staffId: Long, yearMonth: String)
}

@Dao
interface ShiftEntryDao {
    @Query("SELECT * FROM shift_entries WHERE date LIKE :yearMonth || '%'")
    fun getByMonthFlow(yearMonth: String): Flow<List<ShiftEntryEntity>>

    @Query("SELECT * FROM shift_entries WHERE date LIKE :yearMonth || '%'")
    suspend fun getByMonth(yearMonth: String): List<ShiftEntryEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entries: List<ShiftEntryEntity>)

    @Query("DELETE FROM shift_entries WHERE date LIKE :yearMonth || '%'")
    suspend fun deleteByMonth(yearMonth: String)
}
