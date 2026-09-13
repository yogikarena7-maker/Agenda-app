package com.example.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.EventNote
import androidx.compose.material.icons.automirrored.outlined.EventNote
import androidx.compose.material.icons.filled.Mail
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Mail
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.example.ui.agenda.AgendaScreen
import com.example.ui.agenda.AgendaViewModel
import com.example.ui.email.EmailAssistantScreen
import com.example.ui.email.EmailAssistantViewModel
import com.example.ui.settings.SettingsScreen

enum class MainTab {
    AGENDA,
    EMAIL,
    SETTINGS
}

@Composable
fun MainAppScreen(
    agendaViewModel: AgendaViewModel,
    emailViewModel: EmailAssistantViewModel,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableStateOf(MainTab.AGENDA) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        bottomBar = {
            NavigationBar(modifier = Modifier.testTag("main_navigation_bar")) {
                NavigationBarItem(
                    selected = selectedTab == MainTab.AGENDA,
                    onClick = { selectedTab = MainTab.AGENDA },
                    icon = {
                        Icon(
                            imageVector = if (selectedTab == MainTab.AGENDA) Icons.AutoMirrored.Filled.EventNote else Icons.AutoMirrored.Outlined.EventNote,
                            contentDescription = "Agenda"
                        )
                    },
                    label = { Text("Agenda") },
                    modifier = Modifier.testTag("nav_agenda_tab")
                )

                NavigationBarItem(
                    selected = selectedTab == MainTab.EMAIL,
                    onClick = { selectedTab = MainTab.EMAIL },
                    icon = {
                        Icon(
                            imageVector = if (selectedTab == MainTab.EMAIL) Icons.Filled.Mail else Icons.Outlined.Mail,
                            contentDescription = "Email Assistant"
                        )
                    },
                    label = { Text("Email AI") },
                    modifier = Modifier.testTag("nav_email_tab")
                )

                NavigationBarItem(
                    selected = selectedTab == MainTab.SETTINGS,
                    onClick = { selectedTab = MainTab.SETTINGS },
                    icon = {
                        Icon(
                            imageVector = if (selectedTab == MainTab.SETTINGS) Icons.Filled.Settings else Icons.Outlined.Settings,
                            contentDescription = "Settings"
                        )
                    },
                    label = { Text("Settings") },
                    modifier = Modifier.testTag("nav_settings_tab")
                )
            }
        }
    ) { innerPadding ->
        when (selectedTab) {
            MainTab.AGENDA -> AgendaScreen(
                viewModel = agendaViewModel,
                modifier = Modifier.padding(innerPadding)
            )
            MainTab.EMAIL -> EmailAssistantScreen(
                viewModel = emailViewModel,
                modifier = Modifier.padding(innerPadding)
            )
            MainTab.SETTINGS -> SettingsScreen(
                modifier = Modifier.padding(innerPadding)
            )
        }
    }
}
