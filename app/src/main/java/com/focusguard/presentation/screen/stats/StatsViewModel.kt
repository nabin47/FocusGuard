package com.focusguard.presentation.screen.stats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.focusguard.domain.model.FocusStats
import com.focusguard.domain.usecase.GetFocusStatsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class StatsViewModel @Inject constructor(
    getFocusStatsUseCase: GetFocusStatsUseCase
) : ViewModel() {

    val stats: StateFlow<FocusStats> = getFocusStatsUseCase()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), FocusStats())
}
