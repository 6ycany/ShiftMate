package com.shiftmate.ui.dashboard

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.time.YearMonth

@Composable
fun DashboardScreen(vm: DashboardViewModel = hiltViewModel()) {
    val month by vm.currentMonth.collectAsState()
    val state by vm.uiState.collectAsState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var showSaveConfirmDialog by remember { mutableStateOf(false) }

    // Confirmation dialog before saving
    if (showSaveConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showSaveConfirmDialog = false },
            title = { Text("画像を保存") },
            text = { Text("ダッシュボードの全情報を画像ファイルとしてギャラリーに保存しますか？") },
            confirmButton = {
                TextButton(onClick = {
                    showSaveConfirmDialog = false
                    scope.launch {
                        val saved = withContext(Dispatchers.Default) {
                            val bitmap = generateDashboardBitmap(state, month)
                            saveDashboardImage(context, bitmap, "ShiftMate_Dashboard_${month}.png")
                        }
                        snackbarHostState.showSnackbar(
                            if (saved) "画像をギャラリーに保存しました" else "保存に失敗しました"
                        )
                    }
                }) { Text("保存する") }
            },
            dismissButton = {
                TextButton(onClick = { showSaveConfirmDialog = false }) { Text("キャンセル") }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("集計ダッシュボード") },
                actions = {
                    if (state.stats.isNotEmpty()) {
                        IconButton(onClick = { showSaveConfirmDialog = true }) {
                            Icon(Icons.Filled.Save, contentDescription = "画像保存", tint = Color.White)
                        }
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
        if (state.stats.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Filled.Analytics, contentDescription = null, modifier = Modifier.size(56.dp), tint = Color.LightGray)
                    Spacer(Modifier.height(12.dp))
                    Text("先に「シフト」タブでシフトを生成してください。", color = Color.Gray)
                }
            }
            return@Scaffold
        }

        LazyColumn(
            modifier = Modifier.padding(padding),
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
                            "シフトの集計データを確認できます。人件費・稼働率・曜日別人員をチェックしてください。右上のボタンで全情報を画像保存できます。",
                            fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            item {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { vm.prevMonth() }) { Icon(Icons.Filled.ChevronLeft, contentDescription = "前月") }
                    Text("${month.year}年${month.monthValue}月", fontSize = 18.sp, fontWeight = FontWeight.Bold, modifier = Modifier.width(140.dp), textAlign = TextAlign.Center)
                    IconButton(onClick = { vm.nextMonth() }) { Icon(Icons.Filled.ChevronRight, contentDescription = "翌月") }
                }
            }

            item {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    StatBox(Modifier.weight(1f), "${state.stats.size}", "名", "スタッフ", MaterialTheme.colorScheme.primary)
                    StatBox(Modifier.weight(1f), "${state.understaffedDays}", "日", "人員不足", Color(0xFFEF5350))
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

            item { Text("曜日別平均人員", style = MaterialTheme.typography.titleSmall, color = Color.Gray) }

            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Row(Modifier.padding(12.dp)) {
                        listOf("日","月","火","水","木","金","土").forEachIndexed { i, wd ->
                            Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(wd, fontSize = 11.sp, fontWeight = FontWeight.Bold,
                                    color = if (i == 0) Color(0xFFE53935) else if (i == 6) MaterialTheme.colorScheme.primary else Color.Gray)
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
            Row(verticalAlignment = Alignment.Bottom) {
                Text(value, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold)
                if (unit.isNotEmpty()) {
                    Spacer(Modifier.width(2.dp))
                    Text(unit, fontSize = 12.sp, color = Color.Gray)
                }
            }
        }
    }
}

private data class DashStatBox(val label: String, val value: String, val color: Int)

// ── Programmatic full-content bitmap generation ─────────────────────
private fun generateDashboardBitmap(state: DashboardUiState, month: YearMonth): Bitmap {
    val width = 1080
    val rowHeight = 90
    val headerHeight = 160
    val summaryHeight = 260
    val staffSection = state.stats.size * rowHeight + 60  // title + rows
    val dowSection = 180
    val totalHeight = headerHeight + summaryHeight + staffSection + dowSection + 40

    val bitmap = Bitmap.createBitmap(width, totalHeight, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    canvas.drawColor(android.graphics.Color.parseColor("#F8FFF4"))

    val pGreen   = android.graphics.Color.parseColor("#7CB342")
    val pBlack   = android.graphics.Color.BLACK
    val pGray    = android.graphics.Color.GRAY
    val pWhite   = android.graphics.Color.WHITE
    val pRed     = android.graphics.Color.parseColor("#EF5350")
    val pPurple  = android.graphics.Color.parseColor("#9C27B0")
    val pMoneyGreen = android.graphics.Color.parseColor("#43A047")
    val pSunRed  = android.graphics.Color.parseColor("#E53935")
    val pSatBlue = android.graphics.Color.parseColor("#1976D2")

    fun paint(color: Int, size: Float = 28f, bold: Boolean = false) = Paint().apply {
        this.color = color; textSize = size; isAntiAlias = true
        if (bold) typeface = Typeface.DEFAULT_BOLD
    }
    fun fillPaint(color: Int) = Paint().apply { this.color = color; style = Paint.Style.FILL; isAntiAlias = true }

    // Header bar
    canvas.drawRect(0f, 0f, width.toFloat(), headerHeight.toFloat(), fillPaint(pGreen))
    canvas.drawText("${month.year}年${month.monthValue}月 集計ダッシュボード", 40f, 80f, paint(pWhite, 44f, true))
    canvas.drawText("ShiftMate", 40f, 130f, paint(pWhite, 28f))

    var y = headerHeight.toFloat() + 30f

    // Summary section title
    canvas.drawText("サマリー", 40f, y + 36f, paint(pGray, 28f, true))
    y += 60f

    // 4 stat boxes (2 rows × 2)
    val boxW = (width - 120) / 2f
    val boxH = 90f
    val stats = listOf(
        DashStatBox("スタッフ数", "${state.stats.size}名", pGreen),
        DashStatBox("人員不足日", "${state.understaffedDays}日", pRed),
        DashStatBox("月間総時間", "${String.format("%.0f", state.totalHours)}h", pPurple),
        DashStatBox("月間人件費", "¥${"%,d".format(state.totalCost)}", pMoneyGreen)
    )
    stats.forEachIndexed { i, s ->
        val bx = 40f + (i % 2) * (boxW + 40f)
        val by = y + (i / 2) * (boxH + 20f)
        canvas.drawRoundRect(RectF(bx, by, bx + boxW, by + boxH), 12f, 12f, fillPaint(pWhite))
        canvas.drawRect(bx, by, bx + boxW, by + 6f, fillPaint(s.color))
        canvas.drawText(s.label, bx + 16f, by + 30f, paint(pGray, 22f))
        canvas.drawText(s.value, bx + 16f, by + 70f, paint(s.color, 36f, true))
    }
    y += boxH * 2 + 60f + 40f

    // Staff section
    canvas.drawText("スタッフ別集計", 40f, y, paint(pGray, 28f, true))
    y += 20f

    state.stats.forEach { stat ->
        // Card bg
        canvas.drawRoundRect(RectF(20f, y, width - 20f, y + rowHeight - 8f), 10f, 10f, fillPaint(pWhite))

        // Avatar circle
        canvas.drawCircle(68f, y + (rowHeight - 8) / 2f, 26f, fillPaint(pGreen))
        canvas.drawText(stat.staff.name.take(1), 57f, y + (rowHeight - 8) / 2f + 10f, paint(pWhite, 28f, true))

        // Name & details
        canvas.drawText(stat.staff.name, 110f, y + 34f, paint(pBlack, 28f, true))
        val details = "${stat.workDays}日  ${String.format("%.1f", stat.workHours)}h  ¥${"%,d".format(stat.laborCost)}"
        canvas.drawText(details, 110f, y + 62f, paint(pGray, 22f))

        // Util rate
        val rateText = "${stat.utilRate}%"
        val rateColor = when {
            stat.utilRate > 95 -> pRed
            stat.utilRate > 80 -> android.graphics.Color.parseColor("#FB8C00")
            else -> pGreen
        }
        canvas.drawText(rateText, width - 120f, y + 50f, paint(rateColor, 32f, true))

        // Progress bar
        val barLeft = 110f; val barRight = width - 140f; val barTop = y + 72f; val barBottom = y + 82f
        canvas.drawRoundRect(RectF(barLeft, barTop, barRight, barBottom), 5f, 5f, fillPaint(android.graphics.Color.parseColor("#E0E0E0")))
        val fill = (barLeft + (barRight - barLeft) * (stat.utilRate / 100f).coerceIn(0f, 1f))
        canvas.drawRoundRect(RectF(barLeft, barTop, fill, barBottom), 5f, 5f, fillPaint(rateColor))

        y += rowHeight.toFloat()
    }

    y += 20f

    // Day-of-week section
    canvas.drawText("曜日別平均人員", 40f, y, paint(pGray, 28f, true))
    y += 20f
    val dowW = (width - 80f) / 7f
    listOf("日","月","火","水","木","金","土").forEachIndexed { i, wd ->
        val dx = 40f + i * dowW
        val dowColor = if (i == 0) pSunRed else if (i == 6) pSatBlue else pBlack
        canvas.drawText(wd, dx + dowW / 2f - 14f, y + 36f, paint(dowColor, 26f, true))
        val avg = state.avgByDow.getOrElse(i) { 0f }
        canvas.drawText(String.format("%.1f", avg), dx + dowW / 2f - 24f, y + 80f, paint(dowColor, 36f, true))
    }

    return bitmap
}

private fun saveDashboardImage(context: Context, bitmap: Bitmap, filename: String): Boolean {
    return try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val values = ContentValues().apply {
                put(MediaStore.Images.Media.DISPLAY_NAME, filename)
                put(MediaStore.Images.Media.MIME_TYPE, "image/png")
                put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/ShiftMate")
            }
            val uri = context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values) ?: return false
            context.contentResolver.openOutputStream(uri)?.use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
            true
        } else {
            val dir = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "ShiftMate")
            dir.mkdirs()
            File(dir, filename).outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
            true
        }
    } catch (e: Exception) { false }
}
