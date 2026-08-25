package com.focusguard.domain.usecase

import com.focusguard.domain.repository.TaskRepository
import javax.inject.Inject

class CompleteTaskUseCase @Inject constructor(
    private val taskRepository: TaskRepository
) {
    suspend operator fun invoke(taskId: Long) {
        taskRepository.completeTask(taskId)
    }
}
