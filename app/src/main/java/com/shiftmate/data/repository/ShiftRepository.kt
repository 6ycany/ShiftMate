package com.shiftmate.data.repository

import com.shiftmate.data.local.dao.*
import com.shiftmate.data.local.entity.RoleEntity
import com.shiftmate.data.local.entity.ShiftEntryEntity
import com.shiftmate.data.local.entity.ShiftProfileEntity
import com.shiftmate.data.local.entity.ShiftRequestEntity
import com.shiftmate.data.local.entity.ShiftRuleEntity
import com.shiftmate.data.local.entity.StaffEntity
import com.shiftmate.data.local.entity.TimeBlockEntity
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
    private val entryDao: ShiftEntryDao,
    private val profileDao: ShiftProfileDao
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
    suspend fun getRule(storeId: Long = 1): ShiftRule? = ruleDao.get(storeId)?.toDomain()
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

    /** Manually set one staff+date assignment (null blockId = clear) */
    suspend fun setEntry(staffId: Long, date: LocalDate, blockId: Long?) {
        entryDao.deleteByStaffAndDate(staffId, date.toString())
        if (blockId != null) {
            entryDao.insert(ShiftEntryEntity(staffId = staffId, blockId = blockId, date = date.toString()))
        }
    }

    // ── Profiles ──
    fun profilesFlow(): Flow<List<ShiftProfile>> =
        profileDao.getAllFlow().map { list -> list.map { it.toDomain() } }

    suspend fun saveProfile(name: String) {
        val roles  = roleDao.getAll()
        val staff  = staffDao.getAll()
        val blocks = blockDao.getAll()
        val rule   = ruleDao.get()
        val roleById = roles.associateBy { it.id }

        val rolesData  = roles.joinToString("\n")  { "${it.name}|${it.colorIndex}|${it.defaultHoursPerWeek}|${it.defaultDaysPerMonth}|${it.minPerShift}" }
        val staffData  = staff.joinToString("\n")  { "${it.name}|${roleById[it.roleId]?.name ?: ""}|${it.hourlyWage}|${it.maxHoursPerWeek}|${it.maxDaysPerMonth}" }
        val blocksData = blocks.joinToString("\n") { "${it.name}|${it.start}|${it.end}|${it.required}" }
        val ruleData   = if (rule != null) "${rule.openTime}|${rule.closeTime}|${rule.maxConsecDays}" else ""

        profileDao.insert(ShiftProfileEntity(name = name, rolesData = rolesData, staffData = staffData, blocksData = blocksData, ruleData = ruleData))
    }

    suspend fun loadProfile(profile: ShiftProfile) {
        staffDao.deleteAll()
        roleDao.deleteAll()
        blockDao.deleteAll()
        ruleDao.deleteAll()

        val roleNameToId = mutableMapOf<String, Long>()
        if (profile.rolesData.isNotBlank()) {
            profile.rolesData.lines().forEach { line ->
                val f = line.split("|"); if (f.size < 5) return@forEach
                val id = roleDao.insert(RoleEntity(name = f[0], colorIndex = f[1].toIntOrNull() ?: 0, defaultHoursPerWeek = f[2].toIntOrNull() ?: 40, defaultDaysPerMonth = f[3].toIntOrNull() ?: 20, minPerShift = f[4].toIntOrNull() ?: 0))
                roleNameToId[f[0]] = id
            }
        }
        if (profile.staffData.isNotBlank()) {
            profile.staffData.lines().forEach { line ->
                val f = line.split("|"); if (f.size < 5) return@forEach
                val roleId = roleNameToId[f[1]] ?: roleNameToId.values.firstOrNull() ?: return@forEach
                staffDao.insert(StaffEntity(name = f[0], roleId = roleId, hourlyWage = f[2].toIntOrNull() ?: 1000, maxHoursPerWeek = f[3].toIntOrNull() ?: 40, maxDaysPerMonth = f[4].toIntOrNull() ?: 20))
            }
        }
        if (profile.blocksData.isNotBlank()) {
            profile.blocksData.lines().forEachIndexed { idx, line ->
                val f = line.split("|"); if (f.size < 4) return@forEachIndexed
                blockDao.insert(TimeBlockEntity(name = f[0], start = f[1], end = f[2], required = f[3].toIntOrNull() ?: 1, sortOrder = idx))
            }
        }
        if (profile.ruleData.isNotBlank()) {
            val f = profile.ruleData.split("|")
            if (f.size >= 3) ruleDao.insert(ShiftRuleEntity(openTime = f[0], closeTime = f[1], maxConsecDays = f[2].toIntOrNull() ?: 5))
        }
    }

    suspend fun deleteProfile(profileId: Long) = profileDao.deleteById(profileId)
}

// ── Profile domain model ───────────────────────────────────────────
data class ShiftProfile(
    val id: Long,
    val name: String,
    val createdAt: Long,
    val rolesData: String,
    val staffData: String,
    val blocksData: String,
    val ruleData: String
)

private fun ShiftProfileEntity.toDomain() =
    ShiftProfile(id, name, createdAt, rolesData, staffData, blocksData, ruleData)
