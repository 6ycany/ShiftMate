package com.shiftmate.ui.help

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// ── Step definitions ──────────────────────────────────────────────────
private data class GuideStep(
    val num: Int,
    val title: String,
    val desc: String,
    val accent: Color,
    val icon: ImageVector,
    val tips: List<String> = emptyList()
)

private val guideSteps = listOf(
    GuideStep(
        1, "役職を設定する",
        "「スタッフ」タブ → 「役職設定」タブを開き、「役職を追加」ボタンで役職を登録します。\n役職ごとに週の稼働時間・月の最大出勤日数・最低配置人数・表示カラーを設定できます。",
        Color(0xFF1976D2), Icons.Filled.Badge,
        listOf("まず役職を追加しないとスタッフを登録できません", "例：社員（週40h）、アルバイト（週25h）")
    ),
    GuideStep(
        2, "スタッフを登録する",
        "「スタッフ」タブの右下にある ＋ボタンを押してスタッフを追加します。\n氏名・役職・時給・週最大時間・月最大日数を入力してください。",
        Color(0xFF43A047), Icons.Filled.PersonAdd,
        listOf("スタッフは後から編集・削除できます", "カードをタップして編集も可能")
    ),
    GuideStep(
        3, "シフトルールを設定する",
        "「ルール」タブで営業時間・連続勤務上限・時間帯ブロックを設定します。\n時間帯ブロックはシフト生成の単位です。早番・遅番などを自由に定義できます。",
        Color(0xFF9C27B0), Icons.Filled.Settings,
        listOf("時刻はアイコンをタップするとGUIで選択できます", "変更後は必ず「保存」ボタンを押してください", "ブロックの「必要人数」がシフト充足チェックに使われます")
    ),
    GuideStep(
        4, "希望休を入力する",
        "「希望休」タブでスタッフを選択し、日付をタップしてブロックごとに希望を設定します。\n○＝出勤可、△＝できれば休み、×＝休み確定。カレンダーの色付き点がブロック別の状態を示します。",
        Color(0xFFFB8C00), Icons.Filled.CalendarToday,
        listOf("「土日 ×」などの一括ボタンで効率入力できます", "ブロックごとに別々の希望を設定できます")
    ),
    GuideStep(
        5, "シフトを自動生成・手動編集する",
        "「シフト」タブで月を確認し、「シフト自動生成」ボタンを押します。\nシフト表のセルをタップすると手動で変更もできます。問題がある場合は⚠️マークで確認できます。",
        Color(0xFFE53935), Icons.Filled.TableChart,
        listOf("生成後はセルをタップして個別に編集可能", "前月・翌月の切り替えも可能", "アプリを終了しても生成済みシフトは保持されます")
    ),
    GuideStep(
        6, "CSV / PDF でエクスポートする",
        "シフト生成後、「CSV」「PDF」ボタンが表示されます。\nCSVは表計算ソフト用、PDFは印刷や共有に便利です。",
        Color(0xFF00ACC1), Icons.Filled.Download,
        listOf("PDFはA4横向きで出力されます", "共有アプリ（メール・Google Drive等）で受け渡しできます")
    ),
    GuideStep(
        7, "集計ダッシュボードで確認する",
        "「集計」タブでスタッフ別の稼働率・人件費・曜日別平均人員を確認できます。\n右上のアイコンからダッシュボード全体を画像として保存することもできます。",
        Color(0xFF7B1FA2), Icons.Filled.Analytics,
        listOf("稼働率が95%超のスタッフは赤く表示されます", "画像保存はすべての情報が含まれます")
    ),
    GuideStep(
        8, "設定をプロファイルに保存する",
        "スタッフ画面の「⊙」アイコン → 「設定の保存・読み込み」画面で、現在の設定に名前を付けて保存できます。\n複数のプロファイルを作成して切り替えることも可能です。",
        Color(0xFF558B2F), Icons.Filled.Storage,
        listOf("プロファイルにはスタッフ・役職・ルール・時間帯が保存されます", "シフト表・希望休は保存されません")
    ),
    GuideStep(
        9, "設定をエクスポート・インポートする",
        "「設定の保存・読み込み」画面の上部にある「エクスポート」「インポート」ボタンを使うと、設定をJSONファイルとして外部に書き出したり、読み込んだりできます。\n他の端末への移行やバックアップに活用できます。",
        Color(0xFFE65100), Icons.Filled.SwapHoriz,
        listOf("エクスポート：役職・スタッフ・ルール・時間帯をJSONで出力", "インポート：JSONファイルを選択して一括読み込み", "既存の設定はインポート時に上書きされます")
    )
)

// ── Screen ────────────────────────────────────────────────────────────
@Composable
fun HelpScreen(onBack: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("セットアップガイド") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "戻る", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = Color.White
                )
            )
        }
    ) { padding ->
        LazyColumn(
            Modifier.padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text(
                    "ShiftMate の使い方",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    "以下の${guideSteps.size}ステップでシフト自動生成が使えるようになります。",
                    fontSize = 13.sp, color = Color.Gray
                )
            }

            guideSteps.forEach { step ->
                item {
                    StepCard(step)
                }
            }

            item {
                Spacer(Modifier.height(4.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFE3F2FD))
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.Lightbulb, contentDescription = null, tint = Color(0xFF1565C0), modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("便利なヒント", fontWeight = FontWeight.Bold, color = Color(0xFF1565C0))
                        }
                        Spacer(Modifier.height(8.dp))
                        listOf(
                            "サンプルデータ（役職・スタッフ・時間帯）がすでに登録されています",
                            "不要なサンプルデータは各タブから削除できます",
                            "シフトを再生成するたびに上書きされます（希望休は保持されます）",
                            "設定をJSONでエクスポートして他の端末でインポートできます"
                        ).forEach { tip ->
                            Row(Modifier.padding(vertical = 2.dp)) {
                                Text("• ", color = Color(0xFF1565C0), fontSize = 13.sp)
                                Text(tip, fontSize = 13.sp, color = Color(0xFF1565C0), lineHeight = 20.sp)
                            }
                        }
                    }
                }
                Spacer(Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun StepCard(step: GuideStep) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {

            // ── Header ──
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier.size(40.dp).clip(CircleShape).background(step.accent),
                    contentAlignment = Alignment.Center
                ) {
                    Text("${step.num}", color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 18.sp)
                }
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(step.title, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
                Icon(step.icon, contentDescription = null, tint = step.accent.copy(alpha = 0.5f), modifier = Modifier.size(24.dp))
            }

            Spacer(Modifier.height(10.dp))

            // ── Description ──
            Text(step.desc, fontSize = 13.sp, color = Color(0xFF333333), lineHeight = 20.sp)

            // ── Mock screen ──
            Spacer(Modifier.height(12.dp))
            MockScreenFrame(step.num)

            // ── Tips ──
            if (step.tips.isNotEmpty()) {
                Spacer(Modifier.height(10.dp))
                Surface(
                    color = step.accent.copy(alpha = 0.07f),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(Modifier.padding(10.dp)) {
                        step.tips.forEach { tip ->
                            Row(Modifier.padding(vertical = 2.dp)) {
                                Text("💡 ", fontSize = 12.sp)
                                Text(tip, fontSize = 12.sp, color = Color(0xFF444444), lineHeight = 18.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

// ── Mock screen dispatcher ─────────────────────────────────────────────
@Composable
private fun MockScreenFrame(stepNum: Int) {
    Box(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .border(1.5.dp, Color(0xFFE0E0E0), RoundedCornerShape(12.dp))
            .background(Color(0xFFF5F5F5))
    ) {
        // Top bar mock
        Column {
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(28.dp)
                    .background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.CenterStart
            ) {
                val title = when (stepNum) {
                    1, 2 -> "スタッフ管理"
                    3    -> "シフトルール設定"
                    4    -> "希望休入力"
                    5    -> "シフト表"
                    6    -> "シフト表"
                    7    -> "集計ダッシュボード"
                    8    -> "設定の保存・読み込み"
                    9    -> "設定の保存・読み込み"
                    else -> "ShiftMate"
                }
                Text(title, color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 10.dp))
            }

            Box(Modifier.padding(8.dp)) {
                when (stepNum) {
                    1    -> MockRoleList()
                    2    -> MockStaffList()
                    3    -> MockRulesScreen()
                    4    -> MockCalendar()
                    5    -> MockShiftTable()
                    6    -> MockExportButtons()
                    7    -> MockDashboard()
                    8    -> MockProfileList()
                    9    -> MockJsonExportImport()
                    else -> {}
                }
            }
        }
    }
}

// ── Individual mock screens ────────────────────────────────────────────

@Composable
private fun MockRoleList() {
    val roles = listOf(
        Triple("社員", Color(0xFF1976D2), "週40h · 月20日 · 最低2名"),
        Triple("アルバイト", Color(0xFF43A047), "週25h · 月15日 · 最低1名"),
        Triple("店長", Color(0xFFE53935), "週45h · 月22日 · 最低1名")
    )
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        roles.forEach { (name, color, sub) ->
            MockCard {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)) {
                    Box(Modifier.size(10.dp).clip(CircleShape).background(color))
                    Spacer(Modifier.width(8.dp))
                    Column(Modifier.weight(1f)) {
                        Text(name, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
                        Text(sub, fontSize = 8.sp, color = Color.Gray)
                    }
                    Icon(Icons.Filled.Edit, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(12.dp))
                    Spacer(Modifier.width(4.dp))
                    Icon(Icons.Filled.Delete, contentDescription = null, tint = Color(0xFFE53935), modifier = Modifier.size(12.dp))
                }
            }
        }
        // Add button
        Row(
            Modifier.fillMaxWidth().padding(top = 2.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            Surface(
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                shape = RoundedCornerShape(20.dp)
            ) {
                Row(
                    Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Filled.Add, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(12.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("役職を追加", fontSize = 9.sp, color = MaterialTheme.colorScheme.primary)
                }
            }
        }
    }
}

@Composable
private fun MockStaffList() {
    val staff = listOf(
        Triple("田中 太郎", "社員", Color(0xFF1976D2)),
        Triple("鈴木 花子", "アルバイト", Color(0xFF43A047)),
        Triple("佐藤 次郎", "アルバイト", Color(0xFF43A047))
    )
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        staff.forEach { (name, role, color) ->
            MockCard {
                Row(
                    Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        Modifier.size(22.dp).clip(CircleShape).background(color),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(name.take(1), color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    }
                    Column(Modifier.weight(1f)) {
                        Text(name, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        Text("$role · ¥1,200/h · 月20日", fontSize = 8.sp, color = Color.Gray)
                    }
                    Icon(Icons.Filled.Edit, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(12.dp))
                }
            }
        }
        // FAB hint
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            Box(
                Modifier.size(24.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Filled.Add, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
            }
        }
    }
}

@Composable
private fun MockRulesScreen() {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        MockCard {
            Column(Modifier.padding(horizontal = 8.dp, vertical = 6.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("基本設定", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                // Time field mock
                listOf("営業開始" to "09:00", "営業終了" to "22:00").forEach { (label, time) ->
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .border(1.dp, Color(0xFFBDBDBD), RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(label, fontSize = 8.sp, color = Color.Gray, modifier = Modifier.weight(1f))
                        Text(time, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
                        Spacer(Modifier.width(4.dp))
                        Icon(Icons.Filled.AccessTime, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(12.dp))
                    }
                }
            }
        }
        // Block cards
        listOf("早番  09:00〜17:00  必要2名" to Color(0xFFDCEDC8), "遅番  14:00〜22:00  必要2名" to Color(0xFFFFCDD2)).forEach { (label, bg) ->
            Surface(color = bg, shape = RoundedCornerShape(6.dp), modifier = Modifier.fillMaxWidth()) {
                Text(label, fontSize = 9.sp, modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp))
            }
        }
    }
}

@Composable
private fun MockCalendar() {
    val dayLabels = listOf("日", "月", "火", "水", "木", "金", "土")
    val blockColors = listOf(Color(0xFF43A047), Color(0xFF1976D2))
    // Sample: some days have requests
    // 0=no req, 1=×, 2=△
    val sampleWeek = listOf(1, 0, 0, 2, 0, 0, 1)

    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
        Row {
            dayLabels.forEachIndexed { i, wd ->
                Text(
                    wd, modifier = Modifier.weight(1f), textAlign = TextAlign.Center,
                    fontSize = 9.sp, fontWeight = FontWeight.Bold,
                    color = if (i == 0) Color(0xFFE53935) else if (i == 6) Color(0xFF1976D2) else Color.Gray
                )
            }
        }
        // 2 sample weeks
        repeat(2) { weekIdx ->
            Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                sampleWeek.forEachIndexed { i, req ->
                    val day = weekIdx * 7 + i + 1
                    val dow = i
                    val bg = when (req) {
                        1    -> Color(0xFFFFEBEE)
                        2    -> Color(0xFFFFFDE7)
                        else -> Color.White
                    }
                    val border = when (req) {
                        1    -> Color(0xFFEF9A9A)
                        2    -> Color(0xFFFFE082)
                        else -> Color(0xFFE0E0E0)
                    }
                    Column(
                        Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(4.dp))
                            .border(1.dp, border, RoundedCornerShape(4.dp))
                            .background(bg)
                            .padding(2.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            "$day", fontSize = 9.sp, fontWeight = FontWeight.Bold,
                            color = if (dow == 0) Color(0xFFE53935) else if (dow == 6) Color(0xFF1976D2) else Color.Black
                        )
                        Row(horizontalArrangement = Arrangement.Center) {
                            blockColors.forEachIndexed { bi, bc ->
                                val dotColor = when {
                                    req == 1 -> Color(0xFFE53935)
                                    req == 2 && bi == 0 -> Color(0xFFFB8C00)
                                    else -> bc.copy(alpha = 0.3f)
                                }
                                Box(Modifier.size(6.dp).clip(CircleShape).background(dotColor))
                                if (bi == 0) Spacer(Modifier.width(1.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MockShiftTable() {
    val blockCellColors = listOf(Color(0xFFDCEDC8) to "早", Color(0xFFFFCDD2) to "遅", Color(0xFFE1BEE7) to "中")
    val days = listOf("1\n日", "2\n月", "3\n火", "4\n水", "5\n木", "6\n金", "7\n土")
    val staffRows = listOf(
        listOf(0, -1, 0, 1, -1, 0, -1),
        listOf(1, 0, -1, 0, 1, -1, 0),
        listOf(-1, 1, 1, -1, 0, 1, 0)
    )
    val names = listOf("田中", "鈴木", "佐藤")

    Column {
        // Header
        Row(Modifier.background(MaterialTheme.colorScheme.primary).padding(vertical = 2.dp)) {
            Text("スタッフ", Modifier.width(30.dp).padding(start = 2.dp), color = Color.White, fontSize = 7.sp, fontWeight = FontWeight.Bold)
            days.forEachIndexed { i, d ->
                val bg = if (i == 0) Color(0xFFB71C1C) else if (i == 6) Color(0xFF558B2F) else MaterialTheme.colorScheme.primary
                Box(Modifier.size(20.dp, 22.dp).background(bg), contentAlignment = Alignment.Center) {
                    Text(d, color = Color.White, fontSize = 6.sp, textAlign = TextAlign.Center, lineHeight = 8.sp)
                }
            }
        }
        // Staff rows
        staffRows.forEachIndexed { si, row ->
            Row(
                Modifier.background(Color.White).padding(vertical = 1.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(names[si], Modifier.width(30.dp).padding(start = 2.dp), fontSize = 7.sp, fontWeight = FontWeight.SemiBold)
                row.forEachIndexed { di, block ->
                    Box(Modifier.size(20.dp), contentAlignment = Alignment.Center) {
                        if (block >= 0) {
                            val (bg, label) = blockCellColors[block]
                            Surface(color = bg, shape = RoundedCornerShape(2.dp)) {
                                Text(label, Modifier.padding(1.dp), fontSize = 7.sp, fontWeight = FontWeight.Bold,
                                    color = Color(0xFF33691E))
                            }
                        } else {
                            Text("－", fontSize = 7.sp, color = Color.LightGray)
                        }
                    }
                }
            }
            HorizontalDivider(color = Color(0xFFEEEEEE), thickness = 0.5.dp)
        }
    }
}

@Composable
private fun MockExportButtons() {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text("シフト生成完了 ✅", fontSize = 10.sp, color = Color(0xFF2E7D32), fontWeight = FontWeight.SemiBold)
        Text("※ セルをタップして個別に編集できます", fontSize = 8.sp, color = Color.Gray)
        Spacer(Modifier.height(4.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("CSV" to Icons.Filled.Download, "PDF" to Icons.Filled.Description).forEach { (label, icon) ->
                Surface(
                    modifier = Modifier.weight(1f),
                    color = Color.White,
                    shape = RoundedCornerShape(6.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
                ) {
                    Row(
                        Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(4.dp))
                        Text(label, fontSize = 11.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}

@Composable
private fun MockDashboard() {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        // Stat boxes row
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            listOf(
                Triple("スタッフ", "3名", MaterialTheme.colorScheme.primary),
                Triple("月間人件費", "¥24万", Color(0xFF43A047))
            ).forEach { (label, value, color) ->
                MockCard(Modifier.weight(1f)) {
                    Column(Modifier.padding(6.dp)) {
                        Box(Modifier.fillMaxWidth().height(3.dp).clip(RoundedCornerShape(2.dp)).background(color))
                        Spacer(Modifier.height(3.dp))
                        Text(label, fontSize = 7.sp, color = Color.Gray)
                        Text(value, fontSize = 13.sp, fontWeight = FontWeight.ExtraBold, color = color)
                    }
                }
            }
        }
        // Staff card with progress
        MockCard {
            Column(Modifier.padding(6.dp), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(18.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primary), contentAlignment = Alignment.Center) {
                        Text("田", color = Color.White, fontSize = 7.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(Modifier.width(6.dp))
                    Column(Modifier.weight(1f)) {
                        Text("田中 太郎", fontSize = 9.sp, fontWeight = FontWeight.SemiBold)
                        Text("18日  120.0h  ¥144,000", fontSize = 7.sp, color = Color.Gray)
                    }
                    Text("90%", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFFFB8C00))
                }
                Box(
                    Modifier.fillMaxWidth().height(4.dp).clip(RoundedCornerShape(2.dp)).background(Color(0xFFE0E0E0))
                ) {
                    Box(Modifier.fillMaxWidth(0.9f).fillMaxHeight().clip(RoundedCornerShape(2.dp)).background(Color(0xFFFB8C00)))
                }
            }
        }
    }
}

@Composable
private fun MockProfileList() {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        // Save button hint
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            Surface(color = MaterialTheme.colorScheme.primary, shape = RoundedCornerShape(4.dp)) {
                Text("現在の設定を保存", color = Color.White, fontSize = 8.sp, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
            }
        }
        // Profile cards
        listOf("春夏シフト" to "2026/04/01 10:00", "冬季体制" to "2026/01/10 09:30").forEach { (name, date) ->
            MockCard {
                Column(Modifier.padding(6.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Folder, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(6.dp))
                        Column(Modifier.weight(1f)) {
                            Text(name, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            Text(date, fontSize = 7.sp, color = Color.Gray)
                        }
                        Icon(Icons.Filled.Delete, contentDescription = null, tint = Color(0xFFE53935), modifier = Modifier.size(12.dp))
                    }
                    Spacer(Modifier.height(4.dp))
                    Box(
                        Modifier.fillMaxWidth().clip(RoundedCornerShape(4.dp)).background(MaterialTheme.colorScheme.primary).padding(vertical = 4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("この設定を読み込む", color = Color.White, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun MockJsonExportImport() {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        // Export/Import card
        Surface(
            color = MaterialTheme.colorScheme.surfaceVariant,
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(Modifier.padding(8.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.SwapHoriz, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("外部ファイルへのエクスポート・インポート", fontSize = 8.sp, fontWeight = FontWeight.Bold)
                }
                Text("JSONファイルとして出力・読み込みができます。バックアップや他の端末への移行に。", fontSize = 7.sp, color = Color.Gray, lineHeight = 11.sp)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Box(
                        Modifier.weight(1f).clip(RoundedCornerShape(4.dp)).background(MaterialTheme.colorScheme.primary).padding(vertical = 5.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                            Icon(Icons.Filled.Upload, contentDescription = null, tint = Color.White, modifier = Modifier.size(10.dp))
                            Spacer(Modifier.width(3.dp))
                            Text("エクスポート", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    Box(
                        Modifier.weight(1f).clip(RoundedCornerShape(4.dp)).border(1.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(4.dp)).padding(vertical = 5.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                            Icon(Icons.Filled.Download, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(10.dp))
                            Spacer(Modifier.width(3.dp))
                            Text("インポート", color = MaterialTheme.colorScheme.primary, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
        // JSON content hint
        Surface(
            color = Color(0xFF263238),
            shape = RoundedCornerShape(6.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                "{\n  \"version\": 1,\n  \"roles\": [...],\n  \"staff\": [...],\n  \"blocks\": [...],\n  \"rule\": {...}\n}",
                color = Color(0xFF80CBC4),
                fontSize = 8.sp,
                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                modifier = Modifier.padding(8.dp),
                lineHeight = 12.sp
            )
        }
    }
}

// ── Shared helper ─────────────────────────────────────────────────────
@Composable
private fun MockCard(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = Color.White,
        shape = RoundedCornerShape(6.dp),
        shadowElevation = 1.dp,
        content = content
    )
}
