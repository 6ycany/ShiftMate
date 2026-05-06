package com.shiftmate.ui.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
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

@Composable
fun DashboardScreen(vm: DashboardViewModel = hiltViewModel()) {
    val month by vm.currentMonth.collectAsState()
    val state by vm.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("集計ダッシュボード") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = Color.White
                )
            )
        }
    ) { padding ->
        if (state.stats.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("先に「シフト」タブでシフトを生成してください。", color = Color.Gray)
            }
            return@Scaffold
        }

        LazyColumn(Modifier.padding(padding), contentPadding = PaddingValues(12.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            item {
                // Month nav
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { vm.prevMonth() }) { Icon(Icons.Filled.ChevronLeft, contentDescription = "前月") }
                    Text("${month.year}年${month.monthValue}月", fontSize = 18.sp, fontWeight = FontWeight.Bold, modifier = Modifier.width(140.dp), textAlign = TextAlign.Center)
                    IconButton(onClick = { vm.nextMonth() }) { Icon(Icons.Filled.ChevronRight, contentDescription = "翌月") }
                }
            }

            item {
                // Summary stats
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    StatBox(Modifier.weight(1f), "${state.stats.size}", "名", "スタッフ", Color(0xFF1976D2))
                    StatBox(Modifier.weight(1f), "${state.understaffedDays}", "日", "人員不足", Color(0xFFFB8C00))
                }
                Spacer(Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    StatBox(Modifier.weight(1f), String.format("%.0f", state.totalHours), "h", "月間総時間", Color(0xFF9C27B0))
                    StatBox(Modifier.weight(1f), "¥${"%,d".format(state.totalCost)}", "", "月間人件費", Color(0xFF43A047))
                }
            }

            item {
                Text("スタッフ別集計", style = MaterialTheme.typography.titleSmall, color = Color.Gray, modifier = Modifier.padding(top = 4.dp))
            }

            items(state.stats) { stat ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                Modifier.size(34.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primary),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(stat.staff.name.take(1), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                            Spacer(Modifier.width(10.dp))
                            Column(Modifier.weight(1f)) {
                                Text(stat.staff.name, fontWeight = FontWeight.SemiBold)
                                Text(
                                    "${stat.workDays} / ${stat.staff.maxDaysPerMonth}日  ${String.format("%.1f", stat.workHours)}h  ¥${"%,d".format(stat.laborCost)}",
                                    fontSize = 11.sp, color = Color.Gray
                                )
                            }
                            Text("${stat.utilRate}%", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                        Spacer(Modifier.height(6.dp))
                        LinearProgressIndicator(
                            progress = { (stat.utilRate / 100f).coerceIn(0f, 1f) },
                            modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                            color = if (stat.utilRate > 95) Color(0xFFE53935) else if (stat.utilRate > 80) Color(0xFFFB8C00) else MaterialTheme.colorScheme.primary,
                            trackColor = Color(0xFFE0E0E0)
                        )
                    }
                }
            }

            item {
                Text("曜日別平均人員", style = MaterialTheme.typography.titleSmall, color = Color.Gray)
            }

            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Row(Modifier.padding(12.dp)) {
                        listOf("日","月","火","水","木","金","土").forEachIndexed { i, wd ->
                            Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(wd, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = if (i == 0) Color(0xFFE53935) else if (i == 6) Color(0xFF1976D2) else Color.Gray)
                                Spacer(Modifier.height(6.dp))
                                Text(String.format("%.1f", state.avgByDow.getOrElse(i) { 0f }), fontSize = 18.sp, fontWeight = FontWeight.ExtraBold)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StatBox(modifier: Modifier, value: String, unit: String, label: String, accentColor: Color) {
    Card(modifier = modifier) {
        Column(Modifier.padding(14.dp)) {
            Box(Modifier.fillMaxWidth().height(4.dp).clip(RoundedCornerShape(2.dp)).background(accentColor))
            Spacer(Modifier.height(8.dp))
            Text(label, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
            Spacer(Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.Baseline) {
                Text(value, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold)
                if (unit.isNotEmpty()) {
                    Spacer(Modifier.width(2.dp))
                    Text(unit, fontSize = 12.sp, color = Color.Gray)
                }
            }
        }
    }
}
