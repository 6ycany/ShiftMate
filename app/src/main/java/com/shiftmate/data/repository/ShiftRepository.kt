package com.shiftmate.data.repository

import com.shiftmate.data.local.dao.*
import com.shiftmate.domain.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ShiftRepository @Inject constructor(
    private val roleDao: RoleDao,
    private val staffDao: StaffDao,
    private val blockDao: TimeBlockDao,
    private val ruleDao: ShiftRuleDao,
    private val requestDao: ShiftRequestDao,
    private val entryDao: ShiftEntryDao
) {
    // ── Roles ──
    fun rolesFlow(): Flow<List<Role>> = roleDao.getAllFlow().map { list -> list.map { it.toDomain() } }
    suspend fun getAllRoles(): List<Role> = roleDao.getAll().map { it.toDomain() }
    suspend fun saveRole(role: Role): Long = if (role.id == 0L) roleDao.insert(role.toEntity()) else { roleDao.update(role.toEntity()); role.id }
    suspend fun deleteRole(role: Role) = roleDao.delete(role.toEntity())
    suspend fun isRoleInUse(roleId: Long): Boolean = roleDao.countStaffByRole(roleId) > 0

    // ── Staff ──
    fun staffFlow(): Flow<List<Staff>> = staffDao.getAllFlow().map { list -> list.map { it.toDomain() } }
    suspend fun getAllStaff(): List<Staff> = staffDao.getAll().map { it.toDomain() }
    suspend fun saveStaff(staff: Staff): Long = if (staff.id == 0L) staffDao.insert(staff.toEntity()) else { staffDao.update(staff.toEntity()); staff.id }
    suspend fun deleteStaff(staff: Staff) = staffDao.delete(staff.toEntity())

    // ── TimeBlocks ──
    fun blocksFlow(storeId: Long = 1): Flow<List<TimeBlock>> = blockDao.getAllFlow(storeId).map { list -> list.map { it.toDomain() } }
    suspend fun getAllBlocks(storeId: Long = 1): List<TimeBlock> = blockDao.getAll(storeId).map { it.toDomain() }
    suspend fun saveBlock(block: TimeBlock): Long = if (block.id == 0L) blockDao.insert(block.toEntity()) else { blockDao.update(block.toEntity()); block.id }
    suspend fun deleteBlock(block: TimeBlock) = blockDao.delete(block.toEntity())
    suspend fun saveAllBlocks(blocks: List<TimeBlock>, storeId: Long = 1) {
        blockDao.deleteAll(storeId)
        blocks.forEachIndexed { i, b -> blockDao.insert(b.copy(sortOrder = i).toEntity()) }
    }

    // ── ShiftRule ──
    fun ruleFlow(storeId: Long = 1): Flow<ShiftRule?> = ruleDao.getFlow(storeId).map { it?.toDomain() }
    suspend fun saveRule(rule: ShiftRule) {
        val existing = ruleDao.get(rule.storeId)
        if (existing == null) ruleDao.insert(rule.toEntity()) else ruleDao.update(rule.copy(id = existing.id).toEntity())
    }

    // ── ShiftRequests ──
    fun requestsFlow(staffId: Long, yearMonth: String): Flow<List<ShiftRequest>> =
        requestDao.getByStaffAndMonthFlow(staffId, yearMonth).map { list -> list.map { it.toDomain() } }

    suspend fun getRequestsByMonth(yearMonth: String): List<ShiftRequest> =
        requestDao.getByMonth(yearMonth).map { it.toDomain() }

    suspend fun setRequest(staffId: Long, blockId: Long, date: LocalDate, status: RequestStatus) {
        requestDao.delete(staffId, blockId, date.toString())
        if (status != RequestStatus.AVAILABLE) {
            requestDao.insert(ShiftRequestEntity(staffId = staffId, blockId = blockId, date = date.toString(), status = status.name))
        }
    }

    suspend fun clearMonthRequests(staffId: Long, yearMonth: String) =
        requestDao.deleteByStaffAndMonth(staffId, yearMonth)

    // ── ShiftEntries ──
    fun entriesFlow(yearMonth: String): Flow<List<ShiftEntry>> =
        entryDao.getByMonthFlow(yearMonth).map { list -> list.map { it.toDomain() } }

    suspend fun saveGeneratedShift(entries: List<ShiftEntry>, yearMonth: String) {
        entryDao.deleteByMonth(yearMonth)
        entryDao.insertAll(entries.map { it.toEntity() })
    }
}
