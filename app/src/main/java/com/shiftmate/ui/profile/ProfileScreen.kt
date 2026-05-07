package com.shiftmate.ui.profile

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.shiftmate.data.repository.ShiftProfile
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun ProfileScreen(
    vm: ProfileViewModel = hiltViewModel(),
    onBack: () -> Unit
) {
    val profiles by vm.profiles.collectAsState()
    val snackMsg by vm.snackMessage.collectAsState()
    val context = LocalContext.current

    var showSaveDialog by remember { mutableStateOf(false) }
    var loadConfirmTarget by remember { mutableStateOf<ShiftProfile?>(null) }
    var deleteConfirmId by remember { mutableStateOf<Long?>(null) }
    var showImportConfirm by remember { mutableStateOf<Uri?>(null) }

    val snackbarHostState = remember { SnackbarHostState() }

    // Show snackbar when snackMsg changes
    LaunchedEffect(snackMsg) {
        snackMsg?.let {
            snackbarHostState.showSnackbar(it)
            vm.clearSnack()
        }
    }

    // File picker for JSON import
    val importLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) showImportConfirm = uri
    }

    // ── Dialogs ────────────────────────────────────────────────────

    // Save name dialog
    if (showSaveDialog) {
        var name by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showSaveDialog = false },
            title = { Text("設定を保存") },
            text = {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("プロファイル名") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        vm.save(name.trim())
                        showSaveDialog = false
                        vm.snackMessage.value = "「${name.trim()}」を保存しました"
                    },
                    enabled = name.isNotBlank()
                ) { Text("保存") }
            },
            dismissButton = { TextButton(onClick = { showSaveDialog = false }) { Text("キャンセル") } }
        )
    }

    // Load confirm
    loadConfirmTarget?.let { profile ->
        AlertDialog(
            onDismissRequest = { loadConfirmTarget = null },
            title = { Text("設定を読み込む") },
            text = { Text("「${profile.name}」を読み込みます。\n現在のスタッフ・役職・ルール設定は上書きされます。よろしいですか？") },
            confirmButton = {
                TextButton(onClick = {
                    vm.load(profile)
                    loadConfirmTarget = null
                    vm.snackMessage.value = "「${profile.name}」を読み込みました"
                }) { Text("読み込む", color = MaterialTheme.colorScheme.primary) }
            },
            dismissButton = { TextButton(onClick = { loadConfirmTarget = null }) { Text("キャンセル") } }
        )
    }

    // Delete confirm
    deleteConfirmId?.let { id ->
        val target = profiles.find { it.id == id }
        AlertDialog(
            onDismissRequest = { deleteConfirmId = null },
            title = { Text("削除の確認") },
            text = { Text("「${target?.name}」を削除しますか？") },
            confirmButton = {
                TextButton(onClick = { vm.delete(id); deleteConfirmId = null }) {
                    Text("削除", color = Color(0xFFE53935))
                }
            },
            dismissButton = { TextButton(onClick = { deleteConfirmId = null }) { Text("キャンセル") } }
        )
    }

    // Import confirm
    showImportConfirm?.let { uri ->
        AlertDialog(
            onDismissRequest = { showImportConfirm = null },
            title = { Text("設定をインポート") },
            text = { Text("選択したJSONファイルの設定を読み込みます。\n現在のスタッフ・役職・ルール・時間帯設定はすべて上書きされます。\nよろしいですか？") },
            confirmButton = {
                TextButton(onClick = {
                    vm.importJson(context, uri)
                    showImportConfirm = null
                }) { Text("インポートする", color = MaterialTheme.colorScheme.primary) }
            },
            dismissButton = { TextButton(onClick = { showImportConfirm = null }) { Text("キャンセル") } }
        )
    }

    // ── Main UI ────────────────────────────────────────────────────
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("設定の保存・読み込み") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "戻る", tint = Color.White)
                    }
                },
                actions = {
                    TextButton(onClick = { showSaveDialog = true }) {
                        Text("現在の設定を保存", color = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = Color.White
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        LazyColumn(
            Modifier.padding(padding),
            contentPadding = PaddingValues(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {

            // ── Export / Import card ───────────────────────────────
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Filled.SwapHoriz,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Column {
                                Text(
                                    "外部ファイルへのエクスポート・インポート",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                                Text(
                                    "JSONファイルとして出力・読み込みができます。バックアップや他の端末への移行に使用できます。",
                                    fontSize = 11.sp,
                                    color = Color.Gray,
                                    lineHeight = 16.sp
                                )
                            }
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            // Export button
                            Button(
                                onClick = { vm.exportJson(context) },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                            ) {
                                Icon(Icons.Filled.Upload, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("エクスポート", fontSize = 13.sp)
                            }
                            // Import button
                            OutlinedButton(
                                onClick = { importLauncher.launch("application/json") },
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Filled.Download, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("インポート", fontSize = 13.sp)
                            }
                        }
                    }
                }
            }

            // ── Saved profiles ─────────────────────────────────────
            if (profiles.isEmpty()) {
                item {
                    Box(
                        Modifier.fillMaxWidth().padding(vertical = 32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Filled.Storage,
                                contentDescription = null,
                                modifier = Modifier.size(56.dp),
                                tint = Color.LightGray
                            )
                            Spacer(Modifier.height(12.dp))
                            Text("保存済みの設定がありません", color = Color.Gray)
                            Spacer(Modifier.height(8.dp))
                            Text(
                                "右上のボタンから現在の設定を保存できます。",
                                fontSize = 12.sp, color = Color.Gray
                            )
                        }
                    }
                }
            } else {
                item {
                    Text(
                        "保存済みの設定",
                        style = MaterialTheme.typography.titleSmall,
                        color = Color.Gray
                    )
                }
                items(profiles, key = { it.id }) { profile ->
                    ProfileCard(
                        profile = profile,
                        onLoad = { loadConfirmTarget = profile },
                        onDelete = { deleteConfirmId = profile.id }
                    )
                }
            }
        }
    }
}

@Composable
private fun ProfileCard(profile: ShiftProfile, onLoad: () -> Unit, onDelete: () -> Unit) {
    val sdf = remember { SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.JAPAN) }
    val dateStr = sdf.format(Date(profile.createdAt))

    val roleCount  = if (profile.rolesData.isBlank()) 0 else profile.rolesData.lines().size
    val staffCount = if (profile.staffData.isBlank()) 0 else profile.staffData.lines().size
    val blockCount = if (profile.blocksData.isBlank()) 0 else profile.blocksData.lines().size

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Filled.Folder,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(Modifier.width(8.dp))
                Column(Modifier.weight(1f)) {
                    Text(profile.name, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    Text(dateStr, fontSize = 11.sp, color = Color.Gray)
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Filled.Delete, contentDescription = "削除", tint = Color(0xFFE53935))
                }
            }
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ProfileChip("役職 $roleCount")
                ProfileChip("スタッフ $staffCount")
                ProfileChip("時間帯 $blockCount")
            }
            Spacer(Modifier.height(10.dp))
            Button(
                onClick = onLoad,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Icon(Icons.Filled.Download, contentDescription = null)
                Spacer(Modifier.width(6.dp))
                Text("この設定を読み込む")
            }
        }
    }
}

@Composable
private fun ProfileChip(label: String) {
    Surface(
        color = MaterialTheme.colorScheme.primaryContainer,
        shape = MaterialTheme.shapes.small
    ) {
        Text(
            label,
            Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
    }
}
