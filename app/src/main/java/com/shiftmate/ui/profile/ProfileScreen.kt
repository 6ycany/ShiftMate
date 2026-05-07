package com.shiftmate.ui.profile

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
    var showSaveDialog by remember { mutableStateOf(false) }
    var loadConfirmTarget by remember { mutableStateOf<ShiftProfile?>(null) }
    var deleteConfirmId by remember { mutableStateOf<Long?>(null) }
    var snackMessage by remember { mutableStateOf<String?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(snackMessage) {
        snackMessage?.let {
            snackbarHostState.showSnackbar(it)
            snackMessage = null
        }
    }

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
                    onClick = { vm.save(name.trim()); showSaveDialog = false; snackMessage = "「${name.trim()}」を保存しました" },
                    enabled = name.isNotBlank()
                ) { Text("保存") }
            },
            dismissButton = { TextButton(onClick = { showSaveDialog = false }) { Text("キャンセル") } }
        )
    }

    // Load confirm dialog
    loadConfirmTarget?.let { profile ->
        AlertDialog(
            onDismissRequest = { loadConfirmTarget = null },
            title = { Text("設定を読み込む") },
            text = { Text("「${profile.name}」を読み込みます。\n現在のスタッフ・役職・ルール設定は上書きされます。よろしいですか？") },
            confirmButton = {
                TextButton(onClick = {
                    vm.load(profile); loadConfirmTarget = null; snackMessage = "「${profile.name}」を読み込みました"
                }) { Text("読み込む", color = MaterialTheme.colorScheme.primary) }
            },
            dismissButton = { TextButton(onClick = { loadConfirmTarget = null }) { Text("キャンセル") } }
        )
    }

    // Delete confirm dialog
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
        if (profiles.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Filled.FolderOpen, contentDescription = null, modifier = Modifier.size(56.dp), tint = Color.LightGray)
                    Spacer(Modifier.height(12.dp))
                    Text("保存済みの設定がありません", color = Color.Gray)
                    Spacer(Modifier.height(8.dp))
                    Text("右上のボタンから現在の設定を保存できます。", fontSize = 12.sp, color = Color.Gray)
                }
            }
        } else {
            LazyColumn(Modifier.padding(padding), contentPadding = PaddingValues(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                item {
                    Text("保存済みの設定", style = MaterialTheme.typography.titleSmall, color = Color.Gray)
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
                Icon(Icons.Filled.Folder, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(22.dp))
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
                Icon(Icons.Filled.FileDownload, contentDescription = null)
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
        Text(label, Modifier.padding(horizontal = 8.dp, vertical = 4.dp), fontSize = 11.sp, color = MaterialTheme.colorScheme.onPrimaryContainer)
    }
}
