package com.shiftmate.ui.rules

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("基本設定", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary)
                        OutlinedTextField(
                            value = openTime, onValueChange = { openTime = it; dirty = true },
                            label = { Text("営業開始") }, modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = closeTime, onValueChange = { closeTime = it; dirty = true },
                            label = { Text("営業終了") }, modifier = Modifier.fillMaxWidth()
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
                            OutlinedTextField(
                                value = block.start,
                                onValueChange = { v -> editableBlocks = editableBlocks.toMutableList().also { it[index] = block.copy(start = v) }; dirty = true },
                                label = { Text("開始") }, modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = block.end,
                                onValueChange = { v -> editableBlocks = editableBlocks.toMutableList().also { it[index] = block.copy(end = v) }; dirty = true },
                                label = { Text("終了") }, modifier = Modifier.weight(1f)
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
