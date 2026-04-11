package com.example.docbot.ui.screens.home.components

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.example.docbot.ui.screens.home.ConversationState
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

class ConversationListTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val conversations = listOf(
        ConversationState(id = 1, title = "title 1", isFavourite = true),
        ConversationState(id = 2, title = "title 2"),
        ConversationState(id = 3, title = "title 3")
    )

    private fun setConversationList(
        conversations: List<ConversationState> = this.conversations,
        onConversationFavouriteClick: (conversationId: Long, isFavourite: Boolean) -> Unit = {_, _ ->},
        onConversationDeleteClick: (conversationId: Long) -> Unit = {},
        onConversationClick: (id: Long) -> Unit = {}
    ) {
        composeTestRule.setContent {
            ConversationList(
                conversations = conversations,
                onConversationFavouriteClick = onConversationFavouriteClick,
                onConversationDeleteClick = onConversationDeleteClick,
                onConversationClick = onConversationClick
            )
        }
    }


    /****/

    @Test
    fun testNoCardsDisplayedWithEmptyListOfConversations() {
        setConversationList(conversations = emptyList())
        for (conversation in conversations) {
            composeTestRule.onNodeWithText(conversation.title).assertDoesNotExist()
        }
    }

    @Test
    fun testCardsDisplayedWithListOfConversations() {
        setConversationList()
        for (conversation in conversations) {
            composeTestRule.onNodeWithText(conversation.title).assertExists()
        }
    }

    @Test
    fun testOpenConversationTriggersOnConversationClickCallback() {
        var clickedConversation = -1L
        setConversationList(onConversationClick = { clickedConversation = it })

        composeTestRule.onNodeWithText("title 1").performClick()

        assertEquals(1, clickedConversation)
    }

    @Test
    fun testFavouriteClickTriggersOnConversationFavouriteClickCallback() {
        var clickedConversation = -1L
        var favouriteConversation = false
        setConversationList(onConversationFavouriteClick = { id, favourite ->
            clickedConversation = id
            favouriteConversation = favourite
        })

        composeTestRule.onNodeWithContentDescription("Favourite").performClick()

        assertEquals(1, clickedConversation)
        assertTrue(favouriteConversation)
    }

    @Test
    fun testDeleteClickTriggersOnConversationDeleteClickCallback() {
        var clickedConversation = -1L
        setConversationList(onConversationDeleteClick = { clickedConversation = it })

        composeTestRule.onAllNodesWithContentDescription("Delete")[0].performClick()

        assertEquals(1, clickedConversation)
    }
}