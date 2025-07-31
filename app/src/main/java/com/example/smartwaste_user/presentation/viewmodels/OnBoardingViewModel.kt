package com.example.smartwaste_user.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartwaste_user.datastore.DataStoreManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val dataStoreManager: DataStoreManager
) : ViewModel() {

    val onboardingCompleted = dataStoreManager.onboardingCompleted

    fun setOnboardingShown() {
        viewModelScope.launch {
            dataStoreManager.setOnboardingCompleted(true)
        }
    }
}