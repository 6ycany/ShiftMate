package com.shiftmate.ui.profile

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shiftmate.data.repository.ShiftProfile
import com.shiftmate.data.repository.ShiftRepository
import com.shiftmate.util.JsonExporter
import com.shiftmate.util.JsonImporter
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
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

    /** Snackbar message for export / import result feedback */
    val snackMessage = MutableStateFlow<String?>(null)

    // ── In-app profile management ──────────────────────────────────
    fun save(name: String) = viewModelScope.launch { repo.saveProfile(name) }
    fun load(profile: ShiftProfile) = viewModelScope.launch { repo.loadProfile(profile) }
    fun delete(profileId: Long) = viewModelScope.launch { repo.deleteProfile(profileId) }

    // ── JSON Export ───────────────────────────────────────────────
    fun exportJson(context: Context) = viewModelScope.launch {
        try {
            val roles  = repo.getAllRoles()
            val staff  = repo.getAllStaff()
            val blocks = repo.getAllBlocks()
            val rule   = repo.getRule()

            val json = JsonExporter.buildJson(roles, staff, blocks, rule)
            val file = JsonExporter.writeToCache(context, json)

            val uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "application/json"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            val chooser = Intent.createChooser(intent, "設定をエクスポート").apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(chooser)
        } catch (e: Exception) {
            snackMessage.value = "エクスポートに失敗しました: ${e.message}"
        }
    }

    // ── JSON Import ───────────────────────────────────────────────
    fun importJson(context: Context, uri: Uri) = viewModelScope.launch {
        try {
            val json = context.contentResolver.openInputStream(uri)
                ?.bufferedReader(Charsets.UTF_8)
                ?.readText()
                ?: throw IllegalStateException("ファイルを読み込めませんでした")

            val profile = JsonImporter.parse(json)
            repo.loadProfile(profile)
            snackMessage.value = "設定をインポートしました"
        } catch (e: Exception) {
            snackMessage.value = "インポートに失敗しました: ${e.message}"
        }
    }

    fun clearSnack() { snackMessage.value = null }
}
