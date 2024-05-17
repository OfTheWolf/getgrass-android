package com.grass.android.repository

import android.text.format.DateUtils
import com.grass.android.data.ActiveIp
import com.grass.android.network.GrassApiService
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import kotlin.time.Duration

data class Earnings(
    val totalEpoch: Double,
    val totalToday: Double
)

class EarningsRepository @Inject constructor(
    private val grassApiService: GrassApiService,
    private val refreshIntervalMs: Duration
) {
    suspend fun earnings() = flow {
        while (true) {
            val data = grassApiService.activeIps().result.data
            val today = data.filter { DateUtils.isToday(it.date.time) }.sumOf(::calculatePoints)
            val epoch = data.sumOf(::calculatePoints)
            emit(Earnings(epoch, today))
            delay(refreshIntervalMs)
        }
    }

    private fun calculatePoints(response: ActiveIp.Response): Double {
        return response.totalUptime / 3600.0 * response.ipScore
    }
}