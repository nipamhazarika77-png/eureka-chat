package com.example

import com.example.models.Chat
import com.example.models.ChatType
import org.junit.Assert.*
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }

    @Test
    fun testChatArchivedByUserIds() {
        val chat = Chat(
            id = "chat_test",
            type = ChatType.ONE_TO_ONE,
            name = "Test Chat",
            isArchivedByUserIds = emptyList()
        )

        // Archive for user_1
        val userId = "user_1"
        val archivedChat = chat.copy(isArchivedByUserIds = (chat.isArchivedByUserIds + userId).distinct())
        assertTrue(archivedChat.isArchivedByUserIds.contains(userId))

        // Unarchive for user_1
        val unarchivedChat = archivedChat.copy(isArchivedByUserIds = archivedChat.isArchivedByUserIds - userId)
        assertFalse(unarchivedChat.isArchivedByUserIds.contains(userId))
    }
}
