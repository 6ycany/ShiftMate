package com.shiftmate.ui.request

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.shiftmate.domain.model.RequestStatus
import com.shiftmate.domain.model.ShiftRequest
import com.shiftmate.domain.model.TimeBlock
import java.time.LocalDate
import java.time.YearMonth

private val weekdays = listOf("日", "月", "火", "水", "木", "金", "土")

@Composable
fun RequestScreen(vm: RequestViewModel = hiltViewModel()) {
    val staff by vm.staff.collectAsState()
    val blocks by vm.blocks.collectAsState()
    val currentMonth by vm.currentMonth.collectAsState()
    val selectedStaffId by vm.selectedStaffId.collectAsState()
    val requests by vm.requests.collectAsState()

    var showDaySheet by remember { mutableStateOf(false) }
    var selectedDate by remember { mutableStateOf<LocalDate?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("希望休入力") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = Color.White
                )
            )
        }
    ) { padding ->
        if (staff.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Card(
                    modifier = Modifier.padding(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF8E1))
                ) {
                    Column(Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Filled.Warning, contentDescription = null, tint = Color(0xFFFB8C00), modifier = Modifier.size(40.dp))
                        Spacer(Modifier.height(8.dp))
                        Text("スタッフが未登録です", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Spacer(Modifier.height(4.dp))
                        Text("「スタッフ」タブからスタッフを追加してください。", fontSize = 13.sp, color = Color.Gray, textAlign = TextAlign.Center)
                    }
                }
            }
            return@Scaffold
        }

        LazyColumn(Modifier.padding(padding)) {
            // Month nav
            item {
                Row(
                    Modifier.fillMaxWidth().padding(16.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { vm.prevMonth() }) { Icon(Icons.Filled.ChevronLeft, contentDescription = "前月") }
                    Text(
                        "${currentMonth.year}年${currentMonth.monthValue}月",
                        fontSize = 18.sp, fontWeight = FontWeight.Bold,
                        modifier = Modifier.width(140.dp), textAlign = TextAlign.Center
                    )
                    IconButton(onClick = { vm.nextMonth() }) { Icon(Icons.Filled.ChevronRight, contentDescription = "翌月") }
                }
            }

            // Staff selector
            item {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(staff) { s ->
                        val selected = s.id == selectedStaffId
                        FilterChip(
                            selected = selected,
                            onClick = { vm.selectStaff(s.id) },
                            label = { Text(s.name) }
                        )
                    }
                }
            }

            if (selectedStaffId != null) {
                // Bulk actions
                item {
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        item { Text("一括:", fontSize = 12.sp, color = Color.Gray, modifier = Modifier.padding(vertical = 8.dp)) }
                        item { OutlinedButton(onClick = { vm.bulkSet(selectedStaffId!!, currentMonth, RequestStatus.AVAILABLE) { true } }) { Text("全日 ○") } }
                        item { OutlinedButton(onClick = { vm.bulkSet(selectedStaffId!!, currentMonth, RequestStatus.DAY_OFF) { true } }) { Text("全日 ×") } }
                        item { OutlinedButton(onClick = { vm.bulkSet(selectedStaffId!!, currentMonth, RequestStatus.DAY_OFF) { it.dayOfWeek.value == 7 || it.dayOfWeek.value == 6 } }) { Text("土日 ×") } }
                        item { OutlinedButton(onClick = { vm.clearMonth(selectedStaffId!!, currentMonth) }) { Text("リセット") } }
                    }
                }

                // Calendar
                item {
                    CalendarGrid(
                        month = currentMonth,
                        requests = requests,
                        blocks = blocks,
                        onCellClick = { date -> selectedDate = date; showDaySheet = true }
                    )
                }
            } else {
                item {
                    Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                        Text("スタッフを選択してください", color = Color.Gray)
                    }
                }
            }
        }
    }

    if (showDaySheet && selectedDate != null && selectedStaffId != null) {
        DayDetailSheet(
            date = selectedDate!!,
            staffId = selectedStaffId!!,
            blocks = blocks,
            requests = requests,
            onSetRequest = { staffId, blockId, date, status -> vm.setRequest(staffId, blockId, date, status) },
            onDismiss = { showDaySheet = false }
        )
    }
}

@Composable
private fun CalendarGrid(
    month: YearMonth,
    requests: List<ShiftRequest>,
    blocks: List<TimeBlock>,
    onCellClick: (LocalDate) -> Unit
) {
    val firstDow = LocalDate.of(month.year, month.monthValue, 1).dayOfWeek.value % 7
    val daysInMonth = month.lengthOfMonth()

    Column(Modifier.padding(horizontal = 12.dp)) {
        Row {
            weekdays.forEachIndexed { i, wd ->
                Text(
                    wd, modifier = Modifier.weight(1f), textAlign = TextAlign.Center,
                    fontSize = 11.sp, fontWeight = FontWeight.Bold,
                    color = if (i == 0) Color(0xFFE53935) else if (i == 6) Color(0xFF1976D2) else Color.Gray
                )
            }
        }
        Spacer(Modifier.height(4.dp))

        val cells = List(firstDow) { null } + (1..daysInMonth).map { it }
        cells.chunked(7).forEach { week ->
            Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                week.forEach { day ->
                    if (day == null) {
                        Spacer(Modifier.weight(1f))
                    } else {
                        val date = LocalDate.of(month.year, month.monthValue, day)
                        val dow = date.dayOfWeek.value % 7
                        val dayRequests = requests.filter { it.date == date }
                        val indicatorColor = when {
                            dayRequests.isEmpty() -> Color(0xFFE8F5E9)
                            dayRequests.all { it.status == RequestStatus.DAY_OFF } -> Color(0xFFFFCDD2)
                            dayRequests.any { it.status == RequestStatus.DAY_OFF } -> Color(0xFFFFE0B2)
                            dayRequests.any { it.status == RequestStatus.PREFER_OFF } -> Color(0xFFFFF9C4)
                            else -> Color(0xFFE8F5E9)
                        }
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .border(1.dp, Color(0xFFE0E0E0), RoundedCornerShape(8.dp))
                                .background(Color.White)
                                .clickable { onCellClick(date) }
                                .padding(4.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                day.toString(), fontSize = 13.sp, fontWeight = FontWeight.Bold,
                                color = if (dow == 0) Color(0xFFE53935) else if (dow == 6) Color(0xFF1976D2) else Color.Black
                            )
                            Spacer(Modifier.height(4.dp))
                            Box(
                                Modifier.fillMaxWidth().height(6.dp)
                                    .clip(RoundedCornerShape(3.dp)).background(indicatorColor)
                            )
                        }
                    }
                }
                repeat(7 - week.size) { Spacer(Modifier.weight(1f)) }
            }
            Spacer(Modifier.height(3.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DayDetailSheet(
    date: LocalDate,
    staffId: Long,
    blocks: List<TimeBlock>,
    requests: List<ShiftRequest>,
    onSetRequest: (Long, Long, LocalDate, RequestStatus) -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(Modifier.padding(16.dp)) {
            Text(
                "${date.monthValue}月${date.dayOfMonth}日",
                style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(12.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("全 ○" to RequestStatus.AVAILABLE, "全 △" to RequestStatus.PREFER_OFF, "全 ×" to RequestStatus.DAY_OFF).forEach { (label, status) ->
                    val color = when (status) {
                        RequestStatus.AVAILABLE -> Color(0xFF43A047)
                        RequestStatus.PREFER_OFF -> Color(0xFFFB8C00)
                        RequestStatus.DAY_OFF -> Color(0xFFE53935)
                    }
                    OutlinedButton(
                        onClick = { blocks.forEach { onSetRequest(staffId, it.id, date, status) } },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = color)
                    ) { Text(label) }
                }
            }

            Spacer(Modifier.height(8.dp))
            HorizontalDivider()

            blocks.forEach { block ->
                val req = requests.find { it.blockId == block.id && it.date == date }
                val current = req?.status ?: RequestStatus.AVAILABLE

                Row(Modifier.fillMaxWidth().padding(vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text(block.name, fontWeight = FontWeight.SemiBold)
                        Text("${block.start} 〜 ${block.end}", fontSize = 12.sp, color = Color.Gray)
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("○" to RequestStatus.AVAILABLE, "△" to RequestStatus.PREFER_OFF, "×" to RequestStatus.DAY_OFF).forEach { (label, status) ->
                            val sel = current == status
                            val bgColor = when {
                                !sel -> Color(0xFFFAFAFA)
                                status == RequestStatus.AVAILABLE -> Color(0xFFE8F5E9)
                                status == RequestStatus.PREFER_OFF -> Color(0xFFFFF8E1)
                                else -> Color(0xFFFFEBEE)
                            }
                            val borderColor = when {
                                !sel -> Color(0xFFE0E0E0)
                                status == RequestStatus.AVAILABLE -> Color(0xFF43A047)
                                status == RequestStatus.PREFER_OFF -> Color(0xFFFB8C00)
                                else -> Color(0xFFE53935)
                            }
                            Box(
                                Modifier.size(48.dp, 40.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(bgColor)
                                    .border(2.dp, borderColor, RoundedCornerShape(10.dp))
                                    .clickable { onSetRequest(staffId, block.id, date, status) },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(label, fontWeight = FontWeight.Bold, color = borderColor)
                            }
                        }
                    }
                }
                HorizontalDivider()
            }
            Spacer(Modifier.height(16.dp))
        }
    }
}
