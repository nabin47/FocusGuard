package com.focusguard.domain.usecase

import com.focusguard.domain.repository.TaskRepository
import javax.inject.Inject

class AddTaskUseCase @Inject constructor(
    private val taskRepository: TaskRepository
) {
    suspend operator fun invoke(title: String): Long {
        if (title.isBlank()) return -1
        return taskRepository.addTask(title.trim())
    }
}
