package com.shiftmate.data.local

import com.shiftmate.data.repository.ShiftRepository
import com.shiftmate.domain.model.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DatabaseSeeder @Inject constructor(private val repo: ShiftRepository) {

    suspend fun seedIfEmpty() {
        val roles = repo.getAllRoles()
        if (roles.isNotEmpty()) return

        val managerRoleId = repo.saveRole(
            Role(name = "社員", colorIndex = 0, defaultHoursPerWeek = 40, defaultDaysPerMonth = 22, minPerShift = 1)
        )
        val partRoleId = repo.saveRole(
            Role(name = "アルバイト", colorIndex = 1, defaultHoursPerWeek = 20, defaultDaysPerMonth = 12, minPerShift = 0)
        )

        repo.saveStaff(Staff(name = "山田 太郎", roleId = managerRoleId, hourlyWage = 1500, maxHoursPerWeek = 40, maxDaysPerMonth = 22))
        repo.saveStaff(Staff(name = "鈴木 花子", roleId = partRoleId, hourlyWage = 1050, maxHoursPerWeek = 20, maxDaysPerMonth = 12))

        repo.saveBlock(TimeBlock(name = "早番", start = "09:00", end = "17:00", required = 1, sortOrder = 0))
        repo.saveBlock(TimeBlock(name = "遅番", start = "13:00", end = "21:00", required = 1, sortOrder = 1))

        repo.saveRule(ShiftRule(storeId = 1L, maxConsecDays = 5))
    }
}
