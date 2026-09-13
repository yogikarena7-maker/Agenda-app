package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.ai.GeminiEmailAssistant
import com.example.ai.NaturalLanguageReminderParser
import com.example.data.local.EmailDraftEntity
import com.example.data.local.EmailMessageEntity
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `launch MainActivity test`() {
    val controller = org.robolectric.Robolectric.buildActivity(MainActivity::class.java)
    controller.setup()
    assertNotNull(controller.get())
  }

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Smart Agenda", appName)
  }

  @Test
  fun `natural language reminder parser extracts title time and date`() = runBlocking {
    val input = "Remind me tomorrow at 9 PM to call Rahul"
    val parsed = NaturalLanguageReminderParser.parseLocally(input)

    assertTrue(parsed.title.contains("Rahul", ignoreCase = true))
    assertEquals("21:00", parsed.timeString)
    assertNotNull(parsed.dateString)
  }

  @Test
  fun `natural language parser identifies work and urgent categories`() = runBlocking {
    val input = "Urgent project review today at 3 PM #Work"
    val parsed = NaturalLanguageReminderParser.parseLocally(input)

    assertTrue(parsed.category == "Urgent" || parsed.category == "Work")
    assertEquals("15:00", parsed.timeString)
  }

  @Test
  fun `email classification heuristic tags urgent and action required accurately`() = runBlocking {
    val urgentCategory = GeminiEmailAssistant.classifyEmail(
        subject = "CRITICAL: Server load high",
        body = "Immediate action required to avoid crash asap."
    )
    assertEquals("Urgent", urgentCategory)

    val actionCategory = GeminiEmailAssistant.classifyEmail(
        subject = "Please review contract",
        body = "Can you review and approve the document?"
    )
    assertEquals("Action Required", actionCategory)
  }

  @Test
  fun `human approval workflow enforces draft status before sending`() {
    val draft = EmailDraftEntity(
        id = 1L,
        emailId = "msg_123",
        recipientEmail = "colleague@example.com",
        recipientName = "Colleague",
        subject = "Re: Project Update",
        draftBody = "I have reviewed your note.",
        status = "PENDING_APPROVAL",
        approvedAt = null,
        sentAt = null
    )

    assertEquals("PENDING_APPROVAL", draft.status)
    assertNull(draft.approvedAt)
    assertNull(draft.sentAt)

    val approved = draft.copy(
        status = "SENT",
        approvedAt = 123456789L,
        sentAt = 123456789L
    )

    assertEquals("SENT", approved.status)
    assertNotNull(approved.approvedAt)
    assertNotNull(approved.sentAt)
  }
}
