package com.shiftmate.ui.rules

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.shiftmate.domain.model.ShiftRule
import com.shiftmate.domain.model.TimeBlock

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RulesScreen(vm: RulesViewModel = hiltViewModel()) {
    val rule by vm.rule.collectAsState()
    val blocks by vm.blocks.collectAsState()

    // Local editable state
    var openTime by remember(rule) { mutableStateOf(rule?.openTime ?: "09:00") }
    var closeTime by remember(rule) { mutableStateOf(rule?.closeTime ?: "22:00") }
    var maxConsec by remember(rule) { mutableStateOf((rule?.maxConsecDays ?: 5).toString()) }
    var editableBlocks by remember(blocks) { mutableStateOf(blocks.toMutableList()) }

    var dirty by remember { mutableStateOf(false) }
    var showUnsavedDialog by remember { mutableStateOf(false) }

    // TimePicker dialog state
    var showOpenTimePicker by remember { mutableStateOf(false) }
    var showCloseTimePicker by remember { mutableStateOf(false) }

    // Helper to parse "HH:MM" → Pair(hour, minute)
    fun parseTime(s: String): Pair<Int, Int> {
        val parts = s.split(":")
        val h = parts.getOrNull(0)?.toIntOrNull()?.coerceIn(0, 23) ?: 9
        val m = parts.getOrNull(1)?.toIntOrNull()?.coerceIn(0, 59) ?: 0
        return h to m
    }

    // Auto-save when composable leaves composition (tab switch)
    val latestDirty = rememberUpdatedState(dirty)
    val latestOpenTime = rememberUpdatedState(openTime)
    val latestCloseTime = rememberUpdatedState(closeTime)
    val latestMaxConsec = rememberUpdatedState(maxConsec)
    val latestBlocks = rememberUpdatedState(editableBlocks)
    val latestRuleId = rememberUpdatedState(rule?.id ?: 0L)

    DisposableEffect(Unit) {
        onDispose {
            if (latestDirty.value) {
                vm.saveRule(ShiftRule(
                    id = latestRuleId.value,
                    openTime = latestOpenTime.value,
                    closeTime = latestCloseTime.value,
                    maxConsecDays = latestMaxConsec.value.toIntOrNull() ?: 5
                ))
                vm.saveAllBlocks(latestBlocks.value)
            }
        }
    }

    // Back button: show dialog
    BackHandler(enabled = dirty) {
        showUnsavedDialog = true
    }

    // Unsaved changes dialog (back press)
    if (showUnsavedDialog) {
        AlertDialog(
            onDismissRequest = { showUnsavedDialog = false },
            title = { Text("未保存の変更があります") },
            text = { Text("変更内容を保存しますか？\n「保存しない」を選ぶと変更が破棄されます。") },
            confirmButton = {
                TextButton(onClick = {
                    vm.saveRule(ShiftRule(
                        id = rule?.id ?: 0L,
                        openTime = openTime,
                        closeTime = closeTime,
                        maxConsecDays = maxConsec.toIntOrNull() ?: 5
                    ))
                    vm.saveAllBlocks(editableBlocks)
                    dirty = false
                    showUnsavedDialog = false
                }) { Text("保存する") }
            },
            dismissButton = {
                TextButton(onClick = {
                    dirty = false
                    showUnsavedDialog = false
                }) { Text("保存しない", color = Color(0xFFE53935)) }
            }
        )
    }

    // Open time picker dialog
    if (showOpenTimePicker) {
        val (h, m) = parseTime(openTime)
        TimePickerDialog(
            title = "営業開始時刻",
            initialHour = h,
            initialMinute = m,
            onConfirm = { hour, minute ->
                openTime = "%02d:%02d".format(hour, minute)
                dirty = true
                showOpenTimePicker = false
            },
            onDismiss = { showOpenTimePicker = false }
        )
    }

    // Close time picker dialog
    if (showCloseTimePicker) {
        val (h, m) = parseTime(closeTime)
        TimePickerDialog(
            title = "営業終了時刻",
            initialHour = h,
            initialMinute = m,
            onConfirm = { hour, minute ->
                closeTime = "%02d:%02d".format(hour, minute)
                dirty = true
                showCloseTimePicker = false
            },
            onDismiss = { showCloseTimePicker = false }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("シフトルール設定") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = Color.White
                ),
                actions = {
                    TextButton(
                        onClick = {
                            vm.saveRule(ShiftRule(
                                id = rule?.id ?: 0L,
                                openTime = openTime,
                                closeTime = closeTime,
                                maxConsecDays = maxConsec.toIntOrNull() ?: 5
                            ))
                            vm.saveAllBlocks(editableBlocks)
                            dirty = false
                        },
                        enabled = dirty
                    ) {
                        Text("保存", color = if (dirty) Color.White else Color.White.copy(alpha = 0.5f))
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            Modifier.padding(padding).imePadding(),
            contentPadding = PaddingValues(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Description card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Info, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "営業時間・連続勤務上限・時間帯ブロックを設定します。時間帯ブロックはシフト生成の単位になります。変更後は右上の「保存」を押してください。",
                            fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("基本設定", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary)

                        // Open time — tap to open picker
                        OutlinedTextField(
                            value = openTime,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("営業開始") },
                            trailingIcon = {
                                IconButton(onClick = { showOpenTimePicker = true }) {
                                    Icon(Icons.Filled.AccessTime, contentDescription = "時刻を選択")
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                        // The entire field is tappable
                        // (trailingIcon button handles the tap)

                        // Close time
                        OutlinedTextField(
                            value = closeTime,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("営業終了") },
                            trailingIcon = {
                                IconButton(onClick = { showCloseTimePicker = true }) {
                                    Icon(Icons.Filled.AccessTime, contentDescription = "時刻を選択")
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = maxConsec, onValueChange = { maxConsec = it; dirty = true },
                            label = { Text("連続勤務上限（日）") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            item {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text("時間帯ブロック", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary, modifier = Modifier.weight(1f))
                    FilledTonalButton(onClick = {
                        editableBlocks = (editableBlocks + TimeBlock(name = "新ブロック", start = "09:00", end = "17:00")).toMutableList()
                        dirty = true
                    }) {
                        Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("追加")
                    }
                }
            }

            itemsIndexed(editableBlocks) { index, block ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                color = MaterialTheme.colorScheme.primaryContainer,
                                shape = MaterialTheme.shapes.small
                            ) {
                                Text("${index + 1}", Modifier.padding(horizontal = 8.dp, vertical = 4.dp), fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                            }
                            Spacer(Modifier.width(8.dp))
                            OutlinedTextField(
                                value = block.name,
                                onValueChange = { v -> editableBlocks = editableBlocks.toMutableList().also { it[index] = block.copy(name = v) }; dirty = true },
                                label = { Text("ブロック名") }, modifier = Modifier.weight(1f)
                            )
                            Spacer(Modifier.width(8.dp))
                            IconButton(onClick = { editableBlocks = editableBlocks.toMutableList().also { it.removeAt(index) }; dirty = true }, enabled = editableBlocks.size > 1) {
                                Icon(Icons.Filled.Delete, contentDescription = "削除", tint = if (editableBlocks.size > 1) Color(0xFFE53935) else Color.LightGray)
                            }
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            // Start time with picker
                            var showBlockStartPicker by remember { mutableStateOf(false) }
                            var showBlockEndPicker by remember { mutableStateOf(false) }

                            if (showBlockStartPicker) {
                                val (h, m) = parseTime(block.start)
                                TimePickerDialog(
                                    title = "開始時刻",
                                    initialHour = h,
                                    initialMinute = m,
                                    onConfirm = { hour, minute ->
                                        val v = "%02d:%02d".format(hour, minute)
                                        editableBlocks = editableBlocks.toMutableList().also { it[index] = block.copy(start = v) }
                                        dirty = true
                                        showBlockStartPicker = false
                                    },
                                    onDismiss = { showBlockStartPicker = false }
                                )
                            }
                            if (showBlockEndPicker) {
                                val (h, m) = parseTime(block.end)
                                TimePickerDialog(
                                    title = "終了時刻",
                                    initialHour = h,
                                    initialMinute = m,
                                    onConfirm = { hour, minute ->
                                        val v = "%02d:%02d".format(hour, minute)
                                        editableBlocks = editableBlocks.toMutableList().also { it[index] = block.copy(end = v) }
                                        dirty = true
                                        showBlockEndPicker = false
                                    },
                                    onDismiss = { showBlockEndPicker = false }
                                )
                            }

                            OutlinedTextField(
                                value = block.start,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("開始") },
                                trailingIcon = {
                                    IconButton(onClick = { showBlockStartPicker = true }) {
                                        Icon(Icons.Filled.AccessTime, contentDescription = null, modifier = Modifier.size(18.dp))
                                    }
                                },
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = block.end,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("終了") },
                                trailingIcon = {
                                    IconButton(onClick = { showBlockEndPicker = true }) {
                                        Icon(Icons.Filled.AccessTime, contentDescription = null, modifier = Modifier.size(18.dp))
                                    }
                                },
                                modifier = Modifier.weight(1f)
                            )
                        }
                        OutlinedTextField(
                            value = block.required.toString(),
                            onValueChange = { v -> editableBlocks = editableBlocks.toMutableList().also { it[index] = block.copy(required = v.toIntOrNull() ?: 1) }; dirty = true },
                            label = { Text("必要人数") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TimePickerDialog(
    title: String,
    initialHour: Int,
    initialMinute: Int,
    onConfirm: (Int, Int) -> Unit,
    onDismiss: () -> Unit
) {
    val state = rememberTimePickerState(
        initialHour = initialHour,
        initialMinute = initialMinute,
        is24Hour = true
    )
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxWidth()) {
                TimePicker(state = state)
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(state.hour, state.minute) }) { Text("OK") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("キャンセル") }
        }
    )
}
