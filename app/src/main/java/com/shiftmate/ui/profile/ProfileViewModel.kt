package com.shiftmate.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shiftmate.data.repository.ShiftProfile
import com.shiftmate.data.repository.ShiftRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val repo: ShiftRepository
) : ViewModel() {

    val profiles: StateFlow<List<ShiftProfile>> = repo.profilesFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun save(name: String) = viewModelScope.launch { repo.saveProfile(name) }

    fun load(profile: ShiftProfile) = viewModelScope.launch { repo.loadProfile(profile) }

    fun delete(profileId: Long) = viewModelScope.launch { repo.deleteProfile(profileId) }
}
