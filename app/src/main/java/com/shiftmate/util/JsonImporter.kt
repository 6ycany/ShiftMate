package com.shiftmate.util

import com.shiftmate.data.repository.ShiftProfile
import org.json.JSONObject

object JsonImporter {

    /**
     * Parse ShiftMate JSON export file into a ShiftProfile (pipe-delimited format).
     * Throws JSONException or IllegalArgumentException on parse failure.
     */
    fun parse(jsonText: String): ShiftProfile {
        val root = JSONObject(jsonText)

        // ── Roles ──────────────────────────────────────────────────────
        val rolesLines = StringBuilder()
        val rolesArr = root.optJSONArray("roles")
        if (rolesArr != null) {
            for (i in 0 until rolesArr.length()) {
                val r = rolesArr.getJSONObject(i)
                if (rolesLines.isNotEmpty()) rolesLines.append('\n')
                rolesLines.append(
                    "${r.optString("name")}|${r.optInt("colorIndex", 0)}|" +
                    "${r.optInt("defaultHoursPerWeek", 40)}|${r.optInt("defaultDaysPerMonth", 20)}|" +
                    "${r.optInt("minPerShift", 0)}"
                )
            }
        }

        // ── Staff ───────────────────────────────────────────────────────
        val staffLines = StringBuilder()
        val staffArr = root.optJSONArray("staff")
        if (staffArr != null) {
            for (i in 0 until staffArr.length()) {
                val s = staffArr.getJSONObject(i)
                if (staffLines.isNotEmpty()) staffLines.append('\n')
                staffLines.append(
                    "${s.optString("name")}|${s.optString("roleName")}|" +
                    "${s.optInt("hourlyWage", 1000)}|${s.optInt("maxHoursPerWeek", 40)}|" +
                    "${s.optInt("maxDaysPerMonth", 20)}"
                )
            }
        }

        // ── Blocks ──────────────────────────────────────────────────────
        val blocksLines = StringBuilder()
        val blocksArr = root.optJSONArray("blocks")
        if (blocksArr != null) {
            for (i in 0 until blocksArr.length()) {
                val b = blocksArr.getJSONObject(i)
                if (blocksLines.isNotEmpty()) blocksLines.append('\n')
                blocksLines.append(
                    "${b.optString("name")}|${b.optString("start", "09:00")}|" +
                    "${b.optString("end", "17:00")}|${b.optInt("required", 1)}"
                )
            }
        }

        // ── Rule ────────────────────────────────────────────────────────
        var ruleData = ""
        val ruleObj = root.optJSONObject("rule")
        if (ruleObj != null) {
            ruleData = "${ruleObj.optString("openTime", "09:00")}|" +
                       "${ruleObj.optString("closeTime", "22:00")}|" +
                       "${ruleObj.optInt("maxConsecDays", 5)}"
        }

        return ShiftProfile(
            id = 0L,
            name = "imported",
            createdAt = System.currentTimeMillis(),
            rolesData  = rolesLines.toString(),
            staffData  = staffLines.toString(),
            blocksData = blocksLines.toString(),
            ruleData   = ruleData
        )
    }
}
