package com.shiftmate.ui.staff

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shiftmate.data.repository.ShiftRepository
import com.shiftmate.domain.model.Role
import com.shiftmate.domain.model.Staff
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class StaffViewModel @Inject constructor(
    private val repo: ShiftRepository
) : ViewModel() {

    val staff: StateFlow<List<Staff>> = repo.staffFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val roles: StateFlow<List<Role>> = repo.rolesFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun saveStaff(staff: Staff) = viewModelScope.launch { repo.saveStaff(staff) }
    fun deleteStaff(staff: Staff) = viewModelScope.launch { repo.deleteStaff(staff) }

    fun saveRole(role: Role) = viewModelScope.launch { repo.saveRole(role) }
    fun deleteRole(role: Role) = viewModelScope.launch {
        if (!repo.isRoleInUse(role.id)) repo.deleteRole(role)
    }

    fun isRoleInUse(roleId: Long): Boolean =
        staff.value.any { it.roleId == roleId }
}
