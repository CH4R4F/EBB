package app.ebbplanner.ui.inbox

import androidx.lifecycle.ViewModel
import app.ebbplanner.data.repository.TaskRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class InboxViewModel
    @Inject
    constructor(
        @Suppress("UnusedPrivateProperty") private val taskRepository: TaskRepository,
    ) : ViewModel()
