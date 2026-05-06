package com.shiftmate.ui.help

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private data class Step(val num: Int, val title: String, val desc: String, val color: Color)

private val steps = listOf(
    Step(1, "役職を設定する", "「スタッフ」タブ → 「役職設定」タブから役職を追加します。\n例：社員、アルバイトなど。", Color(0xFF1976D2)),
    Step(2, "スタッフを登録する", "「スタッフ」タブ → 右下の＋ボタンからスタッフを追加します。\n役職・時給・月最大日数を設定してください。", Color(0xFF43A047)),
    Step(3, "シフトルールを設定する", "「ルール」タブから営業時間・時間帯（早番・遅番など）・連続勤務上限などを設定します。", Color(0xFF9C27B0)),
    Step(4, "希望休を入力する", "「希望休」タブからスタッフを選び、日付ごとに「○ / △ / ×」を設定します。\n×は公休（休み確定）、△は希望休（なるべく休みたい）です。", Color(0xFFFB8C00)),
    Step(5, "シフトを自動生成する", "「シフト」タブで対象月を確認し、「シフト自動生成」ボタンを押します。\n制約を満たした最適なシフト表が完成します。CSV出力も可能です。", Color(0xFFE53935))
)

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
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text(
                    "ShiftMate の使い方",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    "以下の5ステップでシフト自動生成が使えるようになります。",
                    fontSize = 13.sp, color = Color.Gray
                )
                Spacer(Modifier.height(8.dp))
            }

            steps.forEach { step ->
                item {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.Top) {
                            Box(
                                Modifier.size(36.dp).clip(CircleShape).background(step.color),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("${step.num}", color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
                            }
                            Spacer(Modifier.width(12.dp))
                            Column {
                                Text(step.title, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                Spacer(Modifier.height(4.dp))
                                Text(step.desc, fontSize = 13.sp, color = Color(0xFF444444), lineHeight = 20.sp)
                            }
                        }
                    }
                }
            }

            item {
                Spacer(Modifier.height(8.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFE3F2FD))
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Text("ヒント", fontWeight = FontWeight.Bold, color = Color(0xFF1565C0))
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "• サンプルデータ（役職・スタッフ・時間帯）がすでに登録されています。\n" +
                            "• 不要なサンプルデータは各タブから削除できます。\n" +
                            "• シフトを再生成するたびに上書きされます（希望休は保持されます）。",
                            fontSize = 13.sp, color = Color(0xFF1565C0), lineHeight = 20.sp
                        )
                    }
                }
            }
        }
    }
}
