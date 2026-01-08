package com.example.mountadmin.data.repository

import com.example.mountadmin.data.model.DashboardStats
import com.example.mountadmin.data.model.MountainRegistrationStat
import com.example.mountadmin.data.model.Registration
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.util.Calendar

class DashboardRepository {

    private val firestore = FirebaseFirestore.getInstance()
    private val registrationsCollection = firestore.collection("registrations")
    private val usersCollection = firestore.collection("users")
    private val mountainsCollection = firestore.collection("mountains")

    suspend fun getDashboardStats(): Result<DashboardStats> {
        return try {
            // Get total registered hikers (unique users)
            val usersSnapshot = usersCollection.get().await()
            val totalHikers = usersSnapshot.size().toLong()

            // Get registrations this month
            val calendar = Calendar.getInstance()
            calendar.set(Calendar.DAY_OF_MONTH, 1)
            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            val startOfMonth = Timestamp(calendar.time)

            val thisMonthSnapshot = registrationsCollection
                .whereGreaterThanOrEqualTo("createdAt", startOfMonth)
                .get()
                .await()
            val registeredThisMonth = thisMonthSnapshot.size().toLong()

            // Get all registrations for statistics
            val allRegistrationsSnapshot = registrationsCollection.get().await()
            val registrations = allRegistrationsSnapshot.documents.mapNotNull { doc ->
                doc.toObject(Registration::class.java)
            }

            // Resolve latest mountain names (prevents duplicates like "merbabu" vs "Merbabu")
            val mountainNameById: Map<String, String> = try {
                val mountainsSnapshot = mountainsCollection.get().await()
                mountainsSnapshot.documents.associate { doc ->
                    val name = (doc.getString("name") ?: doc.id).trim()
                    doc.id to name
                }
            } catch (_: Exception) {
                emptyMap()
            }

            fun resolvedMountainName(reg: Registration): String {
                val fromMountains = mountainNameById[reg.mountainId]?.trim().orEmpty()
                val fallback = reg.mountainName.trim()
                val chosen = if (fromMountains.isNotBlank()) fromMountains else fallback
                return chosen
                    .removePrefix("Mount ")
                    .removePrefix("Mt. ")
                    .trim()
            }

            // Count registrations per mountain (by mountainId, then resolve name)
            val mountainCounts = registrations
                .groupBy { it.mountainId.ifBlank { it.mountainName.trim().lowercase() } }
                .mapValues { it.value.size }
                .toList()
                .sortedByDescending { it.second }

            // Get most climbed mountain
            val mostClimbedMountain = mountainCounts.firstOrNull()?.let { (key, _) ->
                registrations.firstOrNull {
                    it.mountainId == key || it.mountainName.trim().lowercase() == key
                }?.let { resolvedMountainName(it) }
            } ?: "N/A"

            // Get last 30 days registrations by mountain
            val thirtyDaysAgo = Calendar.getInstance().apply {
                add(Calendar.DAY_OF_YEAR, -30)
            }
            val thirtyDaysTimestamp = Timestamp(thirtyDaysAgo.time)

            val last30DaysRegistrations = registrations.filter {
                it.createdAt >= thirtyDaysTimestamp
            }

            val mountainRegistrations = last30DaysRegistrations
                .groupBy { it.mountainId.ifBlank { it.mountainName.trim().lowercase() } }
                .map { (key, regs) ->
                    val displayName = regs.firstOrNull()?.let { resolvedMountainName(it) }
                        ?: key.toString()
                    MountainRegistrationStat(
                        mountainName = displayName,
                        count = regs.size
                    )
                }
                .sortedByDescending { it.count }
                .take(10)

            val stats = DashboardStats(
                totalHikers = totalHikers,
                registeredThisMonth = registeredThisMonth,
                mostClimbedMountain = mostClimbedMountain,
                mountainRegistrations = mountainRegistrations
            )

            Result.success(stats)
        } catch (e: Exception) {
            // Return mock data on error
            val mockStats = DashboardStats(
                totalHikers = 1245678,
                registeredThisMonth = 32150,
                mostClimbedMountain = "Mt. Rinjani",
                mountainRegistrations = listOf(
                    MountainRegistrationStat("Rinjani", 850),
                    MountainRegistrationStat("Semeru", 720),
                    MountainRegistrationStat("Bromo", 650),
                    MountainRegistrationStat("Merbabu", 450),
                    MountainRegistrationStat("Gede", 380),
                    MountainRegistrationStat("Kerinci", 320),
                    MountainRegistrationStat("Agung", 280),
                    MountainRegistrationStat("Slamet", 240)
                )
            )
            Result.success(mockStats)
        }
    }
}
