package com.shiftmate.ui.request

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shiftmate.data.repository.ShiftRepository
import com.shiftmate.domain.model.RequestStatus
import com.shiftmate.domain.model.ShiftRequest
import com.shiftmate.domain.model.Staff
import com.shiftmate.domain.model.TimeBlock
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth
import javax.inject.Inject

data class RequestUiState(
    val currentMonth: YearMonth = YearMonth.now(),
    val selectedStaffId: Long? = null,
    val requests: List<ShiftRequest> = emptyList()
)

@HiltViewModel
class RequestViewModel @Inject constructor(
    private val repo: ShiftRepository
) : ViewModel() {

    private val _month = MutableStateFlow(YearMonth.now())
    private val _selectedStaffId = MutableStateFlow<Long?>(null)

    val staff: StateFlow<List<Staff>> = repo.staffFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val blocks: StateFlow<List<TimeBlock>> = repo.blocksFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _requests = MutableStateFlow<List<ShiftRequest>>(emptyList())
    val requests: StateFlow<List<ShiftRequest>> = _requests

    val currentMonth: StateFlow<YearMonth> = _month
    val selectedStaffId: StateFlow<Long?> = _selectedStaffId

    init {
        viewModelScope.launch {
            combine(_selectedStaffId, _month) { staffId, month ->
                staffId to month
            }.collectLatest { (staffId, month) ->
                if (staffId != null) {
                    repo.requestsFlow(staffId, month.toString())
                        .collect { _requests.value = it }
                }
            }
        }
    }

    fun prevMonth() { _month.value = _month.value.minusMonths(1) }
    fun nextMonth() { _month.value = _month.value.plusMonths(1) }
    fun selectStaff(staffId: Long) { _selectedStaffId.value = staffId }

    fun setRequest(staffId: Long, blockId: Long, date: LocalDate, status: RequestStatus) =
        viewModelScope.launch { repo.setRequest(staffId, blockId, date, status) }

    fun bulkSet(staffId: Long, yearMonth: YearMonth, status: RequestStatus, filter: (LocalDate) -> Boolean) =
        viewModelScope.launch {
            val blocksNow = blocks.value
            val days = yearMonth.lengthOfMonth()
            (1..days).forEach { d ->
                val date = LocalDate.of(yearMonth.year, yearMonth.monthValue, d)
                if (filter(date)) {
                    blocksNow.forEach { block ->
                        repo.setRequest(staffId, block.id, date, status)
                    }
                }
            }
        }

    fun clearMonth(staffId: Long, yearMonth: YearMonth) =
        viewModelScope.launch { repo.clearMonthRequests(staffId, yearMonth.toString()) }
}
