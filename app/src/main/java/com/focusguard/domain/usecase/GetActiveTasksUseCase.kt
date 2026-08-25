package com.focusguard.domain.usecase

import com.focusguard.domain.model.Task
import com.focusguard.domain.repository.TaskRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetActiveTasksUseCase @Inject constructor(
    private val taskRepository: TaskRepository
) {
    operator fun invoke(): Flow<List<Task>> {
        return taskRepository.getActiveTasks()
    }
}
