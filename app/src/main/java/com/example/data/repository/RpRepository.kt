package com.example.data.repository

import com.example.data.db.UserCharacterDao
import com.example.data.db.RpMessageDao
import com.example.data.model.RpMessage
import com.example.data.model.UserCharacter
import kotlinx.coroutines.flow.Flow

class RpRepository(
    private val userCharacterDao: UserCharacterDao,
    private val rpMessageDao: RpMessageDao
) {
    val userCharacter: Flow<UserCharacter?> = userCharacterDao.getUserCharacter()

    suspend fun saveUserCharacter(character: UserCharacter) {
        userCharacterDao.insertUserCharacter(character)
    }

    suspend fun clearUserCharacter() {
        userCharacterDao.clearUserCharacter()
    }

    fun getMessagesForScenario(scenarioId: String): Flow<List<RpMessage>> {
        return rpMessageDao.getMessagesForScenario(scenarioId)
    }

    suspend fun insertMessage(message: RpMessage) {
        rpMessageDao.insertMessage(message)
    }

    suspend fun clearMessagesForScenario(scenarioId: String) {
        rpMessageDao.clearMessagesForScenario(scenarioId)
    }

    suspend fun clearAllMessages() {
        rpMessageDao.clearAllMessages()
    }
}
