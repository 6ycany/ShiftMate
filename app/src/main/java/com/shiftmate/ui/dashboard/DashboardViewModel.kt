package com.shiftmate.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shiftmate.data.repository.ShiftRepository
import com.shiftmate.domain.model.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import java.time.YearMonth
import javax.inject.Inject

data class StaffStat(
    val staff: Staff,
    val workDays: Int,
    val workHours: Double,
    val laborCost: Int,
    val utilRate: Int
)

data class DashboardUiState(
    val stats: List<StaffStat> = emptyList(),
    val totalHours: Double = 0.0,
    val totalCost: Int = 0,
    val understaffedDays: Int = 0,
    val avgByDow: List<Float> = List(7) { 0f }
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val repo: ShiftRepository
) : ViewModel() {

    private val _month = MutableStateFlow(YearMonth.now())
    val currentMonth: StateFlow<YearMonth> = _month

    val blocks: StateFlow<List<TimeBlock>> = repo.blocksFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val uiState: StateFlow<DashboardUiState> = combine(
        repo.staffFlow(),
        repo.blocksFlow(),
        _month.flatMapLatest { repo.entriesFlow(it.toString()) }
    ) { allStaff, allBlocks, entries ->
        buildState(allStaff, allBlocks, entries, _month.value)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DashboardUiState())

    fun prevMonth() { _month.value = _month.value.minusMonths(1) }
    fun nextMonth() { _month.value = _month.value.plusMonths(1) }

    private fun buildState(
        staff: List<Staff>,
        blocks: List<TimeBlock>,
        entries: List<ShiftEntry>,
        month: YearMonth
    ): DashboardUiState {
        val blockMap = blocks.associateBy { it.id }
        val staffStats = staff.map { s ->
            val myEntries = entries.filter { it.staffId == s.id }
            val days = myEntries.map { it.date }.distinct().size
            val hours = myEntries.sumOf { e ->
                if (e.isCustom) e.customDurationHours
                else blockMap[e.blockId]?.durationHours ?: 0.0
            }
            val cost = (hours * s.hourlyWage).toInt()
            val util = if (s.maxDaysPerMonth > 0) (days * 100 / s.maxDaysPerMonth) else 0
            StaffStat(s, days, hours, cost, util)
        }

        val totalHours = staffStats.sumOf { it.workHours }
        val totalCost = staffStats.sumOf { it.laborCost }

        val daysInMonth = month.lengthOfMonth()
        val understaffed = (1..daysInMonth).count { d ->
            val date = java.time.LocalDate.of(month.year, month.monthValue, d)
            val dayEntries = entries.filter { it.date == date }
            blocks.any { block -> dayEntries.count { it.blockId == block.id } < block.required }
        }

        val dowCounts = IntArray(7)
        val dowDays = IntArray(7)
        (1..daysInMonth).forEach { d ->
            val date = java.time.LocalDate.of(month.year, month.monthValue, d)
            val dow = date.dayOfWeek.value % 7  // 0=Sun..6=Sat
            dowDays[dow]++
            dowCounts[dow] += entries.filter { it.date == date }.map { it.staffId }.distinct().size
        }
        val avgByDow = (0..6).map { if (dowDays[it] > 0) dowCounts[it].toFloat() / dowDays[it] else 0f }

        return DashboardUiState(staffStats, totalHours, totalCost, understaffed, avgByDow)
    }
}
