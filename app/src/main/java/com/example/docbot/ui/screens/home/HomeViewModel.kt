package com.example.docbot.ui.screens.home

import androidx.lifecycle.ViewModel
import com.example.docbot.data.repositories.ConversationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val conversationRepository: ConversationRepository
): ViewModel() {


}