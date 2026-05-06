package com.shiftmate.domain.scheduler

import com.shiftmate.domain.model.*
import java.time.LocalDate

class ShiftScheduler {

    fun generate(
        year: Int,
        month: Int,
        staff: List<Staff>,
        blocks: List<TimeBlock>,
        rule: ShiftRule,
        requests: List<ShiftRequest>
    ): GeneratedShift {
        val entries = mutableListOf<ShiftEntry>()
        val violations = mutableListOf<String>()

        val daysInMonth = LocalDate.of(year, month, 1).lengthOfMonth()
        val dates = (1..daysInMonth).map { LocalDate.of(year, month, it) }

        val requestMap = requests.groupBy { it.staffId to it.date }
        val workDaysCount = staff.associate { it.id to 0 }.toMutableMap()
        val consecCount = staff.associate { it.id to 0 }.toMutableMap()
        val lastWorked = staff.associate { it.id to null as LocalDate? }.toMutableMap()

        for (date in dates) {
            val sortedBlocks = blocks.sortedBy { it.sortOrder }
            for (block in sortedBlocks) {
                val needed = block.required
                val assigned = mutableListOf<Long>()

                val candidates = staff.shuffled().sortedWith(
                    compareBy(
                        { isHardOff(it.id, block.id, date, requestMap) },
                        { workDaysCount[it.id] ?: 0 }
                    )
                )

                for (s in candidates) {
                    if (assigned.size >= needed) break
                    if (isHardOff(s.id, block.id, date, requestMap)) continue
                    if ((workDaysCount[s.id] ?: 0) >= s.maxDaysPerMonth) continue
                    if (alreadyAssignedToday(s.id, date, entries)) continue

                    val consec = consecCount[s.id] ?: 0
                    if (consec >= rule.maxConsecDays) continue

                    entries.add(ShiftEntry(staffId = s.id, blockId = block.id, date = date))
                    assigned.add(s.id)
                    workDaysCount[s.id] = (workDaysCount[s.id] ?: 0) + 1
                    val last = lastWorked[s.id]
                    if (last == null || last.plusDays(1) == date) {
                        consecCount[s.id] = (consecCount[s.id] ?: 0) + 1
                    } else {
                        consecCount[s.id] = 1
                    }
                    lastWorked[s.id] = date
                }

                if (assigned.size < needed) {
                    violations.add("${date.monthValue}/${date.dayOfMonth} ${block.name}: 必要${needed}名に対し${assigned.size}名しか確保できません")
                }
            }

            for (s in staff) {
                if (!alreadyAssignedToday(s.id, date, entries)) {
                    val last = lastWorked[s.id]
                    if (last != null && last.plusDays(1) == date) {
                        // break in consecutive — reset
                    }
                    if (last == null || last.plusDays(1) != date) {
                        consecCount[s.id] = 0
                    }
                }
            }
        }

        return GeneratedShift(entries = entries, violations = violations)
    }

    private fun isHardOff(staffId: Long, blockId: Long, date: LocalDate, map: Map<Pair<Long, LocalDate>, List<ShiftRequest>>): Boolean {
        val reqs = map[staffId to date] ?: return false
        return reqs.any { it.blockId == blockId && it.status == RequestStatus.DAY_OFF }
            || reqs.all { it.status == RequestStatus.DAY_OFF }
    }

    private fun alreadyAssignedToday(staffId: Long, date: LocalDate, entries: List<ShiftEntry>): Boolean =
        entries.any { it.staffId == staffId && it.date == date }
}
