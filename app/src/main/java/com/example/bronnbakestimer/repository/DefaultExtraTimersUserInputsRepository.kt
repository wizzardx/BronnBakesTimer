package com.example.bronnbakestimer.repository

import com.example.bronnbakestimer.model.ExtraTimerUserInputData
import com.example.bronnbakestimer.service.SingleTimerCountdownData
import com.example.bronnbakestimer.service.TimerData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.koin.core.context.GlobalContext

/**
 * Repository implementation for managing user input data of extra timers in the BronnBakesTimer app.
 *
 * This repository is responsible for handling the user input data related to extra timers. It maintains and
 * updates a list of [ExtraTimerUserInputData] that represents the state of each extra timer as input by the
 * user. The class ensures synchronization between user input data and the corresponding countdown data
 * in the [DefaultExtraTimersCountdownRepository].
 *
 * Functions:
 * - [updateData]: Updates the user input data for all extra timers with new information, and synchronizes
 *   this data with the countdown repository.
 *
 * Properties:
 * - [timerData]: A read-only [StateFlow] that emits the current state of user input data for extra timers.
 */
class DefaultExtraTimersUserInputsRepository : IExtraTimersUserInputsRepository {
    // Use Koin to inject a IExtraTimersCountdownRepository, using get
    private val extraTimersCountdownRepository: IExtraTimersCountdownRepository = GlobalContext.get().get()

    // MutableStateFlow for internal updates
    private val internalTimerData = MutableStateFlow<List<ExtraTimerUserInputData>>(listOf())

    /**
     * A read-only [StateFlow] that emits the current state of user input data for extra timers.
     * The data is a list of [ExtraTimerUserInputData].
     */
    override val timerData: StateFlow<List<ExtraTimerUserInputData>> = internalTimerData

    /**
     * Updates the user input data for extra timers with new data.
     *
     * @param newData The new list of [ExtraTimerUserInputData] to be used.
     */
    override fun updateData(newData: List<ExtraTimerUserInputData>) {
        internalTimerData.value = newData

        // Update related countdown data for all timers
        val repo = extraTimersCountdownRepository

        // Remove entries in countdown data whose id are no longer in our data:
        val repoData = repo.timerData.value
        val newDataIds = newData.map { it.id }
        val repoDataIds = repoData.keys
        val idsToRemove = repoDataIds.filter { it !in newDataIds }
        for (id in idsToRemove) {
            repoData.remove(id)
        }

        // Add new entries in countdown data for timers that new to it:
        val idsToAdd = newDataIds.filter { it !in repoDataIds }
        for (id in idsToAdd) {
            val timerData = TimerData()
            val singleTimerCountdownData =
                SingleTimerCountdownData(
                    data = timerData,
                    useInputTimerId = id,
                )
            repoData[id] = singleTimerCountdownData
        }

        // Save updated repo data back to the repo
        repo.updateData(repoData)
    }
}
