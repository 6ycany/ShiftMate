package com.shiftmate.util

import android.content.Context
import com.shiftmate.domain.model.Role
import com.shiftmate.domain.model.ShiftRule
import com.shiftmate.domain.model.Staff
import com.shiftmate.domain.model.TimeBlock
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.time.LocalDateTime

object JsonExporter {

    fun buildJson(
        roles: List<Role>,
        staff: List<Staff>,
        blocks: List<TimeBlock>,
        rule: ShiftRule?
    ): String {
        val roleById = roles.associateBy { it.id }

        val rolesArr = JSONArray()
        roles.forEach { r ->
            rolesArr.put(JSONObject().apply {
                put("name", r.name)
                put("colorIndex", r.colorIndex)
                put("defaultHoursPerWeek", r.defaultHoursPerWeek)
                put("defaultDaysPerMonth", r.defaultDaysPerMonth)
                put("minPerShift", r.minPerShift)
            })
        }

        val staffArr = JSONArray()
        staff.forEach { s ->
            staffArr.put(JSONObject().apply {
                put("name", s.name)
                put("roleName", roleById[s.roleId]?.name ?: "")
                put("hourlyWage", s.hourlyWage)
                put("maxHoursPerWeek", s.maxHoursPerWeek)
                put("maxDaysPerMonth", s.maxDaysPerMonth)
            })
        }

        val blocksArr = JSONArray()
        blocks.forEach { b ->
            blocksArr.put(JSONObject().apply {
                put("name", b.name)
                put("start", b.start)
                put("end", b.end)
                put("required", b.required)
            })
        }

        val root = JSONObject().apply {
            put("version", 1)
            put("exportedAt", LocalDateTime.now().toString())
            put("roles", rolesArr)
            put("staff", staffArr)
            put("blocks", blocksArr)
            if (rule != null) {
                put("rule", JSONObject().apply {
                    put("openTime", rule.openTime)
                    put("closeTime", rule.closeTime)
                    put("maxConsecDays", rule.maxConsecDays)
                })
            }
        }

        return root.toString(2)
    }

    fun writeToCache(context: Context, json: String, tag: String = "settings"): File {
        val file = File(context.cacheDir, "ShiftMate_${tag}.json")
        file.writeText(json, Charsets.UTF_8)
        return file
    }
}
