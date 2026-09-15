package com.thermoheal.ai.di

import android.content.Context
import androidx.room.Room
import com.thermoheal.ai.data.ai.AIEngine
import com.thermoheal.ai.data.ai.RuleBasedAIEngine
import com.thermoheal.ai.data.bluetooth.BleRepository
import com.thermoheal.ai.data.bluetooth.DemoDataSimulator
import com.thermoheal.ai.data.local.ThermoHealDatabase
import com.thermoheal.ai.data.local.dao.*
import com.thermoheal.ai.data.repository.*
import com.thermoheal.ai.domain.repository.*
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): ThermoHealDatabase =
        Room.databaseBuilder(context, ThermoHealDatabase::class.java, ThermoHealDatabase.DATABASE_NAME)
            .fallbackToDestructiveMigration() // acceptable for a research prototype; revisit before production
            .build()

    @Provides fun provideUserDao(db: ThermoHealDatabase): UserDao = db.userDao()
    @Provides fun provideDeviceDao(db: ThermoHealDatabase): DeviceDao = db.deviceDao()
    @Provides fun provideSensorReadingDao(db: ThermoHealDatabase): SensorReadingDao = db.sensorReadingDao()
    @Provides fun providePressureDao(db: ThermoHealDatabase): PressureReadingDao = db.pressureReadingDao()
    @Provides fun provideTemperatureDao(db: ThermoHealDatabase): TemperatureReadingDao = db.temperatureReadingDao()
    @Provides fun provideMoistureDao(db: ThermoHealDatabase): MoistureReadingDao = db.moistureReadingDao()
    @Provides fun provideGaitDao(db: ThermoHealDatabase): GaitReadingDao = db.gaitReadingDao()
    @Provides fun provideAIInsightDao(db: ThermoHealDatabase): AIInsightDao = db.aiInsightDao()
    @Provides fun provideSummaryDao(db: ThermoHealDatabase): SummaryDao = db.summaryDao()
}

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    // BLE: bound to the mock simulator by default so the app is fully usable
    // without hardware (section 10). Swap the @Binds target to
    // RealBleRepository once the insole's GATT profile is finalized.
    @Binds
    @Singleton
    abstract fun bindBleRepository(impl: DemoDataSimulator): BleRepository

    @Binds
    @Singleton
    abstract fun bindAIEngine(impl: RuleBasedAIEngine): AIEngine

    @Binds
    @Singleton
    abstract fun bindSensorRepository(impl: SensorRepositoryImpl): SensorRepository

    @Binds
    @Singleton
    abstract fun bindAIRepository(impl: AIRepositoryImpl): AIRepository

    @Binds
    @Singleton
    abstract fun bindUserRepository(impl: UserRepositoryImpl): UserRepository

    @Binds
    @Singleton
    abstract fun bindDeviceRepository(impl: DeviceRepositoryImpl): DeviceRepository

    @Binds
    @Singleton
    abstract fun bindSummaryRepository(impl: SummaryRepositoryImpl): SummaryRepository
}
