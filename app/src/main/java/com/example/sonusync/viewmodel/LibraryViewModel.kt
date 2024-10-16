package com.example.sonusync.viewmodel

import androidx.lifecycle.ViewModel
import com.example.sonusync.data.repository.MusicRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject


@HiltViewModel
class LibraryViewModel @Inject constructor(
    private val musicRepository: MusicRepository,
) : ViewModel() {


}