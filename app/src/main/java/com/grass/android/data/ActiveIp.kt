package com.grass.android.data

import java.util.Date

object ActiveIp {
    data class Response(
        val ipAddress: String?,
        val date: Date,
        val ipScore: Int,
        val modified: Date?,
        val totalUptime: Long,
        val expiresAt: Long?,
        val entity: String?,
        val userId: String?,
        val created: Date,
        val multiplier: Int
    )
}