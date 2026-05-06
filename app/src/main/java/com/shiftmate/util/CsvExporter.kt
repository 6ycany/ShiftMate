package com.shiftmate.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import com.shiftmate.domain.model.ShiftEntry
import com.shiftmate.domain.model.Staff
import com.shiftmate.domain.model.TimeBlock
import java.io.File
import java.time.YearMonth

object CsvExporter {

    fun export(
        context: Context,
        month: YearMonth,
        staff: List<Staff>,
        blocks: List<TimeBlock>,
        entries: List<ShiftEntry>
    ) {
        val blockMap = blocks.associateBy { it.id }
        val entryMap = entries.groupBy { it.staffId to it.date }

        val daysInMonth = month.lengthOfMonth()
        val dates = (1..daysInMonth).map { java.time.LocalDate.of(month.year, month.monthValue, it) }

        val sb = StringBuilder()
        // Header
        sb.append("名前")
        dates.forEach { d -> sb.append(",${d.monthValue}/${d.dayOfMonth}") }
        sb.append(",勤務日数,勤務時間\n")

        staff.forEach { s ->
            sb.append(s.name)
            var days = 0
            var hours = 0.0
            dates.forEach { d ->
                val dayEntries = entryMap[s.id to d]
                if (!dayEntries.isNullOrEmpty()) {
                    val blockName = blockMap[dayEntries.first().blockId]?.name ?: "?"
                    sb.append(",$blockName")
                    days++
                    hours += dayEntries.sumOf { blockMap[it.blockId]?.durationHours ?: 0.0 }
                } else {
                    sb.append(",")
                }
            }
            sb.append(",$days,${String.format("%.1f", hours)}\n")
        }

        val fileName = "シフト表_${month.year}年${month.monthValue}月.csv"
        val file = File(context.cacheDir, fileName)
        file.writeText(sb.toString(), Charsets.UTF_8)

        val uri: Uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/csv"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "シフト表を共有"))
    }
}
