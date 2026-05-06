package com.shiftmate.ui.rules

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shiftmate.data.repository.ShiftRepository
import com.shiftmate.domain.model.ShiftRule
import com.shiftmate.domain.model.TimeBlock
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RulesViewModel @Inject constructor(
    private val repo: ShiftRepository
) : ViewModel() {

    val rule: StateFlow<ShiftRule?> = repo.ruleFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val blocks: StateFlow<List<TimeBlock>> = repo.blocksFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun saveRule(rule: ShiftRule) = viewModelScope.launch { repo.saveRule(rule) }

    fun saveAllBlocks(blocks: List<TimeBlock>) = viewModelScope.launch {
        repo.saveAllBlocks(blocks)
    }
}
