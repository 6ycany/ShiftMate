package com.shiftmate.util

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import androidx.core.content.FileProvider
import com.shiftmate.domain.model.ShiftEntry
import com.shiftmate.domain.model.Staff
import com.shiftmate.domain.model.TimeBlock
import java.io.File
import java.time.LocalDate
import java.time.YearMonth

object PdfExporter {

    fun export(
        context: Context,
        month: YearMonth,
        staff: List<Staff>,
        blocks: List<TimeBlock>,
        entries: List<ShiftEntry>
    ) {
        val pdfDoc = PdfDocument()
        val pageW = 842; val pageH = 595 // A4 landscape pt
        val page = pdfDoc.startPage(PdfDocument.PageInfo.Builder(pageW, pageH, 1).create())
        draw(page.canvas, pageW, pageH, month, staff, blocks, entries)
        pdfDoc.finishPage(page)

        val file = File(context.cacheDir, "ShiftMate_${month}.pdf")
        file.outputStream().use { pdfDoc.writeTo(it) }
        pdfDoc.close()

        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        val chooser = Intent.createChooser(intent, "PDFを共有").apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        try {
            context.startActivity(chooser)
        } catch (e: Exception) {
            android.widget.Toast.makeText(context, "PDF共有アプリが見つかりません", android.widget.Toast.LENGTH_SHORT).show()
        }
    }

    private fun draw(
        canvas: Canvas, w: Int, h: Int,
        month: YearMonth, staff: List<Staff>,
        blocks: List<TimeBlock>, entries: List<ShiftEntry>
    ) {
        val pHeader = Paint().apply { color = Color.parseColor("#7CB342"); style = Paint.Style.FILL }
        val pText   = Paint().apply { color = Color.WHITE;  textSize = 9f;  isAntiAlias = true }
        val pBlack  = Paint().apply { color = Color.BLACK;  textSize = 9f;  isAntiAlias = true }
        val pGray   = Paint().apply { color = Color.GRAY;   textSize = 8f;  isAntiAlias = true }
        val pLine   = Paint().apply { color = Color.LTGRAY; strokeWidth = 0.5f }
        val pSun    = Paint().apply { color = Color.parseColor("#B71C1C"); textSize = 9f; isAntiAlias = true }
        val pSat    = Paint().apply { color = Color.parseColor("#558B2F"); textSize = 9f; isAntiAlias = true }
        val pCell   = Paint().apply { color = Color.parseColor("#DCEDC8"); style = Paint.Style.FILL }
        val pCellRed= Paint().apply { color = Color.parseColor("#FFCDD2"); style = Paint.Style.FILL }

        // Title
        val titlePaint = Paint().apply { color = Color.parseColor("#33691E"); textSize = 14f; isFakeBoldText = true; isAntiAlias = true }
        canvas.drawText("${month.year}年${month.monthValue}月 シフト表", 16f, 20f, titlePaint)

        val daysInMonth = month.lengthOfMonth()
        val dates = (1..daysInMonth).map { LocalDate.of(month.year, month.monthValue, it) }

        val leftMargin = 16f
        val topMargin  = 30f
        val rowH = if (staff.size <= 20) ((h - topMargin - 20) / (staff.size + 2)).coerceAtLeast(12f) else 12f
        val staffColW = 80f
        val dayColW = ((w - leftMargin - staffColW - 60) / daysInMonth).coerceIn(10f, 22f)
        val daysColW = 30f
        val hoursColW = 30f

        val blockMap = blocks.associateBy { it.id }
        val blockNames = blocks.map { it.name.take(2) }
        val blockColors = listOf("#DCEDC8","#FFCDD2","#E1BEE7","#FFE0B2").map { Color.parseColor(it) }

        // Header row
        val hx = leftMargin
        val hy = topMargin
        canvas.drawRect(hx, hy, hx + staffColW + dates.size * dayColW + daysColW + hoursColW, hy + rowH, pHeader)
        pText.textSize = 8f
        canvas.drawText("スタッフ", hx + 2, hy + rowH - 3, pText)

        dates.forEachIndexed { i, d ->
            val x = hx + staffColW + i * dayColW
            val dow = d.dayOfWeek.value % 7
            val p2 = when (dow) { 0 -> pSun; 6 -> pSat; else -> pText }.also { it.textSize = 7f; it.color = Color.WHITE }
            canvas.drawText(d.dayOfMonth.toString(), x + 2, hy + rowH / 2, p2)
            canvas.drawText(listOf("日","月","火","水","木","金","土")[dow], x + 2, hy + rowH - 2, p2)
        }

        val dx = hx + staffColW + dates.size * dayColW
        canvas.drawText("日", dx + 2, hy + rowH - 3, pText.also { it.textSize = 8f })
        canvas.drawText("h", dx + daysColW + 2, hy + rowH - 3, pText)

        // Staff rows
        staff.forEachIndexed { si, s ->
            val ry = hy + (si + 1) * rowH
            val myEntries = entries.filter { it.staffId == s.id }
            val workDays = myEntries.map { it.date }.distinct().size
            val workHours = myEntries.sumOf { e ->
                if (e.isCustom) e.customDurationHours else blockMap[e.blockId]?.durationHours ?: 0.0
            }

            // Row bg alternate
            if (si % 2 == 1) canvas.drawRect(hx, ry, hx + staffColW + dates.size * dayColW + daysColW + hoursColW, ry + rowH, Paint().apply { color = Color.parseColor("#F8FFF4"); style = Paint.Style.FILL })

            canvas.drawText(s.name.take(8), hx + 2, ry + rowH - 3, pBlack.also { it.textSize = 8f })

            dates.forEachIndexed { i, d ->
                val x = hx + staffColW + i * dayColW
                val entry = myEntries.find { it.date == d }
                if (entry != null) {
                    if (entry.isCustom) {
                        // Spot entry — indigo background
                        canvas.drawRect(x + 1, ry + 1, x + dayColW - 1, ry + rowH - 1,
                            Paint().apply { color = Color.parseColor("#E8EAF6"); style = Paint.Style.FILL })
                        val spotLabel = entry.customLabel?.take(2)?.ifBlank { null }
                            ?: entry.customStart?.take(5) ?: "SP"
                        canvas.drawText(spotLabel, x + 1, ry + rowH - 2,
                            pBlack.also { it.textSize = 5f; it.color = Color.parseColor("#3F51B5") })
                    } else {
                        val blockIdx = blocks.indexOfFirst { it.id == entry.blockId }.coerceAtLeast(0)
                        val cPaint = Paint().apply { color = blockColors.getOrElse(blockIdx) { blockColors[0] }; style = Paint.Style.FILL }
                        canvas.drawRect(x + 1, ry + 1, x + dayColW - 1, ry + rowH - 1, cPaint)
                        val bn = blockNames.getOrElse(blockIdx) { "?" }
                        canvas.drawText(bn, x + 1, ry + rowH - 2, pBlack.also { it.textSize = 6f })
                    }
                }
                canvas.drawLine(x, ry, x, ry + rowH, pLine)
            }

            canvas.drawLine(hx, ry, hx + staffColW + dates.size * dayColW + daysColW + hoursColW, ry, pLine)
            canvas.drawText("$workDays", dx + 4, ry + rowH - 3, pBlack.also { it.textSize = 8f })
            canvas.drawText("${String.format("%.0f",workHours)}", dx + daysColW + 4, ry + rowH - 3, pBlack)
        }

        // Bottom line
        val endY = hy + (staff.size + 1) * rowH
        canvas.drawLine(hx, endY, hx + staffColW + dates.size * dayColW + daysColW + hoursColW, endY, pLine)

        // Block legend
        val legendY = endY + 12f
        canvas.drawText("凡例:", hx, legendY + 8, pGray)
        blocks.forEachIndexed { i, b ->
            val lx = hx + 30 + i * 80f
            canvas.drawRect(lx, legendY, lx + 12, legendY + 10, Paint().apply { color = blockColors.getOrElse(i) { blockColors[0] }; style = Paint.Style.FILL })
            canvas.drawText(b.name, lx + 14, legendY + 9, pGray)
        }
    }
}
