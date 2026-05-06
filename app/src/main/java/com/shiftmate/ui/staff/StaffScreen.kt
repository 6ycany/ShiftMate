package com.shiftmate.ui.staff

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.shiftmate.domain.model.Role
import com.shiftmate.domain.model.Staff

private val roleColors = listOf(
    Color(0xFF1976D2), Color(0xFF43A047), Color(0xFFE53935),
    Color(0xFFFB8C00), Color(0xFF9C27B0), Color(0xFF00ACC1)
)

@Composable
fun StaffScreen(vm: StaffViewModel = hiltViewModel()) {
    val staff by vm.staff.collectAsState()
    val roles by vm.roles.collectAsState()

    var selectedTab by remember { mutableIntStateOf(0) }
    var showStaffDialog by remember { mutableStateOf(false) }
    var showRoleDialog by remember { mutableStateOf(false) }
    var editingStaff by remember { mutableStateOf<Staff?>(null) }
    var editingRole by remember { mutableStateOf<Role?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("スタッフ管理") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = Color.White
                )
            )
        },
        floatingActionButton = {
            if (selectedTab == 0) {
                FloatingActionButton(onClick = { editingStaff = null; showStaffDialog = true }) {
                    Icon(Icons.Filled.Add, contentDescription = "追加")
                }
            }
        }
    ) { padding ->
        Column(Modifier.padding(padding)) {
            TabRow(selectedTabIndex = selectedTab) {
                Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }, text = { Text("スタッフ一覧") })
                Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }, text = { Text("役職設定") })
            }

            when (selectedTab) {
                0 -> StaffListTab(
                    staff = staff,
                    roles = roles,
                    onEdit = { editingStaff = it; showStaffDialog = true },
                    onDelete = { vm.deleteStaff(it) }
                )
                1 -> RoleListTab(
                    roles = roles,
                    isInUse = { vm.isRoleInUse(it) },
                    onEdit = { editingRole = it; showRoleDialog = true },
                    onDelete = { vm.deleteRole(it) },
                    onAdd = { editingRole = null; showRoleDialog = true }
                )
            }
        }
    }

    if (showStaffDialog) {
        StaffDialog(
            existing = editingStaff,
            roles = roles,
            onSave = { vm.saveStaff(it); showStaffDialog = false },
            onDismiss = { showStaffDialog = false }
        )
    }

    if (showRoleDialog) {
        RoleDialog(
            existing = editingRole,
            onSave = { vm.saveRole(it); showRoleDialog = false },
            onDismiss = { showRoleDialog = false }
        )
    }
}

@Composable
private fun StaffListTab(
    staff: List<Staff>,
    roles: List<Role>,
    onEdit: (Staff) -> Unit,
    onDelete: (Staff) -> Unit
) {
    val roleMap = roles.associateBy { it.id }
    if (staff.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("スタッフがいません。右下の＋から追加してください。", color = Color.Gray)
        }
        return
    }
    LazyColumn(contentPadding = PaddingValues(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        items(staff, key = { it.id }) { s ->
            val role = roleMap[s.roleId]
            val color = roleColors.getOrElse(role?.colorIndex ?: 0) { roleColors[0] }
            Card(modifier = Modifier.fillMaxWidth()) {
                Row(
                    Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        Modifier.size(40.dp).clip(CircleShape).background(color),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(s.name.take(1), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                    Column(Modifier.weight(1f)) {
                        Text(s.name, fontWeight = FontWeight.Bold)
                        Text(
                            "${role?.name ?: "-"} · ¥${s.hourlyWage}/h · 月${s.maxDaysPerMonth}日",
                            fontSize = 12.sp, color = Color.Gray
                        )
                    }
                    IconButton(onClick = { onEdit(s) }) { Icon(Icons.Filled.Edit, contentDescription = "編集") }
                    IconButton(onClick = { onDelete(s) }) { Icon(Icons.Filled.Delete, contentDescription = "削除", tint = Color(0xFFE53935)) }
                }
            }
        }
        item { Text("合計 ${staff.size} 名", fontSize = 12.sp, color = Color.Gray, modifier = Modifier.padding(4.dp)) }
    }
}

@Composable
private fun RoleListTab(
    roles: List<Role>,
    isInUse: (Long) -> Boolean,
    onEdit: (Role) -> Unit,
    onDelete: (Role) -> Unit,
    onAdd: () -> Unit
) {
    LazyColumn(contentPadding = PaddingValues(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        items(roles, key = { it.id }) { role ->
            val color = roleColors.getOrElse(role.colorIndex) { roleColors[0] }
            Card(modifier = Modifier.fillMaxWidth()) {
                Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(14.dp).clip(CircleShape).background(color))
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text(role.name, fontWeight = FontWeight.SemiBold)
                        Text("週${role.defaultHoursPerWeek}h · 月${role.defaultDaysPerMonth}日 · 最低${role.minPerShift}名/日", fontSize = 12.sp, color = Color.Gray)
                    }
                    IconButton(onClick = { onEdit(role) }) { Icon(Icons.Filled.Edit, contentDescription = "編集") }
                    IconButton(onClick = { onDelete(role) }, enabled = !isInUse(role.id)) {
                        Icon(Icons.Filled.Delete, contentDescription = "削除", tint = if (!isInUse(role.id)) Color(0xFFE53935) else Color.LightGray)
                    }
                }
            }
        }
        item {
            OutlinedButton(onClick = onAdd, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Filled.Add, contentDescription = null)
                Spacer(Modifier.width(4.dp))
                Text("役職を追加")
            }
        }
    }
}

@Composable
private fun StaffDialog(existing: Staff?, roles: List<Role>, onSave: (Staff) -> Unit, onDismiss: () -> Unit) {
    var name by remember { mutableStateOf(existing?.name ?: "") }
    var roleId by remember { mutableLongStateOf(existing?.roleId ?: roles.firstOrNull()?.id ?: 0L) }
    var wage by remember { mutableStateOf((existing?.hourlyWage ?: 1000).toString()) }
    var maxHours by remember { mutableStateOf((existing?.maxHoursPerWeek ?: 40).toString()) }
    var maxDays by remember { mutableStateOf((existing?.maxDaysPerMonth ?: 20).toString()) }
    var roleExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (existing == null) "スタッフ追加" else "スタッフ編集") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("氏名") }, modifier = Modifier.fillMaxWidth())
                ExposedDropdownMenuBox(expanded = roleExpanded, onExpandedChange = { roleExpanded = !roleExpanded }) {
                    OutlinedTextField(
                        value = roles.find { it.id == roleId }?.name ?: "",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("役職") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = roleExpanded) },
                        modifier = Modifier.fillMaxWidth().menuAnchor()
                    )
                    ExposedDropdownMenu(expanded = roleExpanded, onDismissRequest = { roleExpanded = false }) {
                        roles.forEach { role ->
                            DropdownMenuItem(text = { Text(role.name) }, onClick = { roleId = role.id; roleExpanded = false })
                        }
                    }
                }
                OutlinedTextField(value = wage, onValueChange = { wage = it }, label = { Text("時給（円）") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = maxHours, onValueChange = { maxHours = it }, label = { Text("週最大時間（h）") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = maxDays, onValueChange = { maxDays = it }, label = { Text("月最大日数") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = {
            TextButton(onClick = {
                onSave(Staff(
                    id = existing?.id ?: 0L,
                    name = name.trim(),
                    roleId = roleId,
                    hourlyWage = wage.toIntOrNull() ?: 1000,
                    maxHoursPerWeek = maxHours.toIntOrNull() ?: 40,
                    maxDaysPerMonth = maxDays.toIntOrNull() ?: 20
                ))
            }, enabled = name.isNotBlank()) { Text(if (existing == null) "追加" else "更新") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("キャンセル") } }
    )
}

@Composable
private fun RoleDialog(existing: Role?, onSave: (Role) -> Unit, onDismiss: () -> Unit) {
    var name by remember { mutableStateOf(existing?.name ?: "") }
    var colorIndex by remember { mutableIntStateOf(existing?.colorIndex ?: 0) }
    var hoursPerWeek by remember { mutableStateOf((existing?.defaultHoursPerWeek ?: 40).toString()) }
    var daysPerMonth by remember { mutableStateOf((existing?.defaultDaysPerMonth ?: 20).toString()) }
    var minPerShift by remember { mutableStateOf((existing?.minPerShift ?: 0).toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (existing == null) "役職追加" else "役職編集") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("役職名") }, modifier = Modifier.fillMaxWidth())
                Text("カラー", fontSize = 12.sp, color = Color.Gray)
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    roleColors.forEachIndexed { i, color ->
                        Box(
                            Modifier.size(32.dp).clip(CircleShape).background(color),
                            contentAlignment = Alignment.Center
                        ) {
                            if (colorIndex == i) Icon(Icons.Filled.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                            Surface(
                                onClick = { colorIndex = i },
                                modifier = Modifier.fillMaxSize(),
                                color = Color.Transparent,
                                shape = CircleShape
                            ) {}
                        }
                    }
                }
                OutlinedTextField(value = hoursPerWeek, onValueChange = { hoursPerWeek = it }, label = { Text("週最大時間（h）") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = daysPerMonth, onValueChange = { daysPerMonth = it }, label = { Text("月最大日数") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = minPerShift, onValueChange = { minPerShift = it }, label = { Text("最低配置人数/日") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = {
            TextButton(onClick = {
                onSave(Role(
                    id = existing?.id ?: 0L,
                    name = name.trim(),
                    colorIndex = colorIndex,
                    defaultHoursPerWeek = hoursPerWeek.toIntOrNull() ?: 40,
                    defaultDaysPerMonth = daysPerMonth.toIntOrNull() ?: 20,
                    minPerShift = minPerShift.toIntOrNull() ?: 0
                ))
            }, enabled = name.isNotBlank()) { Text(if (existing == null) "追加" else "更新") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("キャンセル") } }
    )
}
