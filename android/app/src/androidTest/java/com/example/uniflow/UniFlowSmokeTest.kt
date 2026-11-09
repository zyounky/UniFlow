package com.example.uniflow

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertIsOff
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasAnyDescendant
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isEnabled
import androidx.compose.ui.test.isToggleable
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.runBlocking
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.UUID

@RunWith(AndroidJUnit4::class)
class UniFlowSmokeTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun scheduleAndTaskCrudWithRunningBackend() {
        composeRule.onNodeWithText("Четверг").assertIsDisplayed()
        composeRule.onNodeWithText("ауд. 214").assertIsDisplayed()

        val tasksTab = hasText("Задания") and hasClickAction()
        composeRule.onNode(tasksTab).performClick()
        composeRule.onNodeWithText("Ближайшие задания").assertIsDisplayed()

        val addButton = hasText("Добавить новую задачу") and isEnabled()
        composeRule.waitUntil(timeoutMillis = 15_000) {
            composeRule.onAllNodes(addButton).fetchSemanticsNodes().size == 1
        }
        composeRule.onNode(addButton).performClick()

        val taskTitle = "Проверка ${UUID.randomUUID()}"
        val saveButton = composeRule.onNodeWithText("Сохранить")
        saveButton.assertIsNotEnabled()
        composeRule.onNodeWithText("Предмет").performTextInput("Тест")
        composeRule.onNodeWithText("Название").performTextInput(taskTitle)
        saveButton.performClick()

        val taskCard = hasTestTag("task-card") and hasAnyDescendant(hasText(taskTitle))
        val checkbox = isToggleable() and hasAnyAncestor(taskCard) and isEnabled()
        composeRule.waitUntil(timeoutMillis = 15_000) {
            composeRule.onAllNodes(checkbox).fetchSemanticsNodes().size == 1
        }
        composeRule.onNode(checkbox).assertIsOff().performClick()
        composeRule.waitUntil(timeoutMillis = 15_000) {
            composeRule.onAllNodes(checkbox).fetchSemanticsNodes().size == 1
        }
        composeRule.onNode(checkbox).assertIsOn()

        composeRule.onNode(hasText("Расписание") and hasClickAction()).performClick()
        composeRule.onNodeWithText("Четверг").assertIsDisplayed()
        composeRule.onNode(tasksTab).performClick()
        composeRule.waitUntil(timeoutMillis = 15_000) {
            composeRule.onAllNodes(checkbox).fetchSemanticsNodes().size == 1
        }
        composeRule.onNode(checkbox).assertIsOn()

        val deleteButton = hasContentDescription("Удалить задачу") and hasAnyAncestor(taskCard)
        composeRule.onNode(deleteButton).performClick()
        composeRule.waitUntil(timeoutMillis = 15_000) {
            composeRule.onAllNodes(taskCard).fetchSemanticsNodes().isEmpty()
        }
    }

    @Test
    fun manualRefreshShowsTaskCreatedOutsideApp() {
        val tasksTab = hasText("Задания") and hasClickAction()
        val refreshButton = hasText("Обновить список") and isEnabled()
        composeRule.onNode(tasksTab).performClick()
        composeRule.waitUntil(timeoutMillis = 15_000) {
            composeRule.onAllNodes(refreshButton).fetchSemanticsNodes().size == 1
        }

        val taskTitle = "Внешнее задание ${UUID.randomUUID()}"
        val savedTask = runBlocking {
            ApiClient.addTask(CreateTaskRequest("Тест", taskTitle, "", false))
        }
        try {
            composeRule.onNodeWithText(taskTitle).assertDoesNotExist()
            composeRule.onNode(refreshButton).performClick()
            composeRule.waitUntil(timeoutMillis = 15_000) {
                composeRule.onAllNodes(hasText(taskTitle)).fetchSemanticsNodes().size == 1
            }
            composeRule.onNodeWithText(taskTitle).assertIsDisplayed()
        } finally {
            runBlocking { ApiClient.deleteTask(savedTask) }
        }
    }
}
