package com.thermoheal.ai.data.sync

import android.content.Context
import androidx.work.*
import com.thermoheal.ai.data.local.dao.SyncQueueDao
import com.thermoheal.ai.data.local.entity.SyncQueueEntity
import com.thermoheal.ai.utils.AppLogger
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.UUID
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manages background synchronization scheduling and queueing (Phase 10).
 */
@Singleton
class SyncManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val syncQueueDao: SyncQueueDao
) {
    private val workManager = WorkManager.getInstance(context)

    fun schedulePeriodicSync() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .setRequiresBatteryNotLow(true)
            .build()

        val syncRequest = PeriodicWorkRequestBuilder<SyncWorker>(15, TimeUnit.MINUTES)
            .setConstraints(constraints)
            .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 30, TimeUnit.SECONDS)
            .build()

        workManager.enqueueUniquePeriodicWork(
            "ThermoHealSync",
            ExistingPeriodicWorkPolicy.KEEP,
            syncRequest
        )
        AppLogger.d("Periodic sync scheduled with WorkManager (15 min interval, network required).")
    }

    suspend fun enqueueForSync(entityType: String, entityId: String, payloadJson: String) {
        val item = SyncQueueEntity(
            id = UUID.randomUUID().toString(),
            entityType = entityType,
            entityId = entityId,
            payloadJson = payloadJson,
            status = "PENDING",
            retryCount = 0,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        syncQueueDao.enqueue(item)
    }

    fun requestExpeditedSync() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val immediateRequest = OneTimeWorkRequestBuilder<SyncWorker>()
            .setConstraints(constraints)
            .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
            .build()

        workManager.enqueue(immediateRequest)
    }
}
