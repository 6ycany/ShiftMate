package com.shiftmate.ui.shift

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shiftmate.data.repository.ShiftRepository
import com.shiftmate.domain.model.*
import com.shiftmate.domain.scheduler.ShiftScheduler
import com.shiftmate.util.CsvExporter
import com.shiftmate.util.PdfExporter
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
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

    // Derived from entries so it auto-restores after app restart
    val generated: StateFlow<Boolean> = _entries
        .map { it.isNotEmpty() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    init {
        viewModelScope.launch {
            _month.collectLatest { month ->
                repo.entriesFlow(month.toString()).collect { entries ->
                    _entries.value = entries
                }
            }
        }
    }

    fun prevMonth() { _month.value = _month.value.minusMonths(1) }
    fun nextMonth() { _month.value = _month.value.plusMonths(1) }
    fun clearError() { _errorMessage.value = null }

    fun generateShifts() = viewModelScope.launch {
        val allStaff = staff.value
        val allBlocks = blocks.value

        if (allStaff.isEmpty()) {
            _errorMessage.value = "スタッフが登録されていません。\n「スタッフ」タブからスタッフを追加してください。"
            return@launch
        }
        if (allBlocks.isEmpty()) {
            _errorMessage.value = "時間帯（シフト区分）が設定されていません。\n「ルール」タブから時間帯を追加してください。"
            return@launch
        }

        val month = _month.value
        val shiftRule = rule.value ?: ShiftRule()
        val requests = repo.getRequestsByMonth(month.toString())

        val result = scheduler.generate(
            year = month.year, month = month.monthValue,
            staff = allStaff, blocks = allBlocks, rule = shiftRule, requests = requests
        )
        repo.saveGeneratedShift(result.entries, month.toString())
        _violations.value = result.violations
    }

    /** Manually change one cell in the shift table (block-based) */
    fun setEntry(staffId: Long, date: LocalDate, blockId: Long?) = viewModelScope.launch {
        repo.setEntry(staffId, date, blockId)
    }

    /** Manually add a spot/custom shift with explicit start/end times */
    fun setCustomEntry(staffId: Long, date: LocalDate, start: String, end: String, label: String) =
        viewModelScope.launch {
            repo.setCustomEntry(staffId, date, start, end, label)
        }

    fun exportCsv(context: Context) = viewModelScope.launch {
        CsvExporter.export(context, _month.value, staff.value, blocks.value, _entries.value)
    }

    fun exportPdf(context: Context) = viewModelScope.launch {
        PdfExporter.export(context, _month.value, staff.value, blocks.value, _entries.value)
    }
}
