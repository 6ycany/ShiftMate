package com.shiftmate.ui.shift

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shiftmate.data.repository.ShiftRepository
import com.shiftmate.domain.model.*
import com.shiftmate.domain.scheduler.ShiftScheduler
import com.shiftmate.util.CsvExporter
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.YearMonth
import javax.inject.Inject

@HiltViewModel
class ShiftViewModel @Inject constructor(
    private val repo: ShiftRepository,
    private val scheduler: ShiftScheduler
) : ViewModel() {

    private val _month = MutableStateFlow(YearMonth.now())
    val currentMonth: StateFlow<YearMonth> = _month

    val staff: StateFlow<List<Staff>> = repo.staffFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val blocks: StateFlow<List<TimeBlock>> = repo.blocksFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val rule: StateFlow<ShiftRule?> = repo.ruleFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private val _entries = MutableStateFlow<List<ShiftEntry>>(emptyList())
    val entries: StateFlow<List<ShiftEntry>> = _entries

    private val _violations = MutableStateFlow<List<String>>(emptyList())
    val violations: StateFlow<List<String>> = _violations

    private val _generated = MutableStateFlow(false)
    val generated: StateFlow<Boolean> = _generated

    init {
        viewModelScope.launch {
            _month.collectLatest { month ->
                repo.entriesFlow(month.toString()).collect { _entries.value = it }
                _generated.value = _entries.value.isNotEmpty()
            }
        }
    }

    fun prevMonth() { _month.value = _month.value.minusMonths(1) }
    fun nextMonth() { _month.value = _month.value.plusMonths(1) }

    fun generateShifts() = viewModelScope.launch {
        val month = _month.value
        val allStaff = staff.value
        val allBlocks = blocks.value
        val shiftRule = rule.value ?: ShiftRule()
        val requests = repo.getRequestsByMonth(month.toString())

        val result = scheduler.generate(
            year = month.year,
            month = month.monthValue,
            staff = allStaff,
            blocks = allBlocks,
            rule = shiftRule,
            requests = requests
        )
        repo.saveGeneratedShift(result.entries, month.toString())
        _violations.value = result.violations
        _generated.value = true
    }

    fun exportCsv(context: Context) = viewModelScope.launch {
        CsvExporter.export(context, _month.value, staff.value, blocks.value, _entries.value)
    }
}
