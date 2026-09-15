package com.thermoheal.ai.data.repository

import com.thermoheal.ai.data.local.dao.UserDao
import com.thermoheal.ai.data.local.entity.UserEntity
import com.thermoheal.ai.domain.model.*
import com.thermoheal.ai.domain.repository.UserRepository
import com.thermoheal.ai.utils.PreferencesManager
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import java.security.MessageDigest
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Local-first auth for the prototype. Firebase Authentication can be
 * dropped in later by implementing the same [UserRepository] interface
 * against FirebaseAuth + Firestore — no ViewModel changes required.
 */
@Singleton
class UserRepositoryImpl @Inject constructor(
    private val userDao: UserDao,
    private val prefs: PreferencesManager
) : UserRepository {

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun observeUser(): Flow<UserProfile?> = prefs.currentUserId.flatMapLatest { id ->
        if (id == null) flowOf(null) else userDao.observeUser(id).map { it?.toDomain() }
    }

    override suspend fun signUp(profile: UserProfile, password: String): Result<UserProfile> {
        if (userDao.findByEmail(profile.email) != null) {
            return Result.failure(IllegalStateException("An account with this email already exists."))
        }
        val id = UUID.randomUUID().toString()
        val entity = UserEntity(
            id = id, name = profile.name, email = profile.email, passwordHash = hash(password),
            ageYears = profile.ageYears, heightCm = profile.heightCm, weightKg = profile.weightKg,
            footSizeEu = profile.footSizeEu, dominantFoot = profile.dominantFoot.name,
            activityLevel = profile.activityLevel.name, occupation = profile.occupation,
            isDemoUser = false, createdAt = System.currentTimeMillis()
        )
        userDao.upsert(entity)
        prefs.setCurrentUserId(id)
        return Result.success(entity.toDomain())
    }

    override suspend fun login(email: String, password: String): Result<UserProfile> {
        val entity = userDao.findByEmail(email)
            ?: return Result.failure(IllegalArgumentException("No account found for this email."))
        if (entity.passwordHash != hash(password)) {
            return Result.failure(IllegalArgumentException("Incorrect password."))
        }
        prefs.setCurrentUserId(entity.id)
        return Result.success(entity.toDomain())
    }

    override suspend fun continueAsDemoUser(): Result<UserProfile> {
        val id = "demo-user"
        val existing = userDao.findByEmail("demo@thermoheal.ai")
        val entity = existing ?: UserEntity(
            id = id, name = "Research Demo User", email = "demo@thermoheal.ai",
            passwordHash = "", ageYears = 29, heightCm = 170.0, weightKg = 68.0, footSizeEu = 41.0,
            dominantFoot = DominantFoot.RIGHT.name, activityLevel = ActivityLevel.MODERATE.name,
            occupation = null, isDemoUser = true, createdAt = System.currentTimeMillis()
        ).also { userDao.upsert(it) }
        prefs.setCurrentUserId(entity.id)
        prefs.setDemoModeEnabled(true)
        return Result.success(entity.toDomain())
    }

    override suspend fun logout() {
        prefs.setCurrentUserId(null)
    }

    override suspend fun updateProfile(profile: UserProfile) {
        val existing = userDao.findByEmail(profile.email)
        val entity = UserEntity(
            id = profile.id, name = profile.name, email = profile.email,
            passwordHash = existing?.passwordHash ?: "", ageYears = profile.ageYears,
            heightCm = profile.heightCm, weightKg = profile.weightKg, footSizeEu = profile.footSizeEu,
            dominantFoot = profile.dominantFoot.name, activityLevel = profile.activityLevel.name,
            occupation = profile.occupation, isDemoUser = profile.isDemoUser, createdAt = profile.createdAt
        )
        userDao.upsert(entity)
    }

    override suspend fun isLoggedIn(): Boolean = prefs.currentUserId.first() != null

    private fun hash(input: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(input.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }
}
