package com.focusguard.di

import com.focusguard.data.repository.BlockedAppRepositoryImpl
import com.focusguard.data.repository.FocusSessionRepositoryImpl
import com.focusguard.data.repository.TaskRepositoryImpl
import com.focusguard.domain.repository.BlockedAppRepository
import com.focusguard.domain.repository.FocusSessionRepository
import com.focusguard.domain.repository.TaskRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindTaskRepository(
        impl: TaskRepositoryImpl
    ): TaskRepository

    @Binds
    @Singleton
    abstract fun bindBlockedAppRepository(
        impl: BlockedAppRepositoryImpl
    ): BlockedAppRepository

    @Binds
    @Singleton
    abstract fun bindFocusSessionRepository(
        impl: FocusSessionRepositoryImpl
    ): FocusSessionRepository
}
