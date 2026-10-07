package com.smartagenda.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.FormatListBulleted
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.smartagenda.app.ui.theme.BrandLimeAccent
import com.smartagenda.app.ui.theme.OnLimeBlack
import com.smartagenda.app.ui.theme.OnSurfaceWhite

enum class AgendaNavView {
    TIMELINE,
    MONTH_GRID,
    EMAIL_AI,
    SETTINGS
}

@Composable
fun FloatingNavPill(
    currentView: AgendaNavView = AgendaNavView.TIMELINE,
    onViewChange: (AgendaNavView) -> Unit = {},
    onAddClick: () -> Unit = {}
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        // Floating 4-view pill
        Row(
            modifier = Modifier
                .clip(CircleShape)
                .background(Color(0xEE1A1D24))
                .border(1.dp, Color.White.copy(alpha = 0.12f), CircleShape)
                .padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // View 1: Timeline List
            val timelineBg = if (currentView == AgendaNavView.TIMELINE) Color.White.copy(alpha = 0.18f) else Color.Transparent
            Box(
                modifier = Modifier
                    .size(36.dp, 32.dp)
                    .clip(CircleShape)
                    .background(timelineBg)
                    .clickable { onViewChange(AgendaNavView.TIMELINE) },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.FormatListBulleted,
                    contentDescription = "Timeline View",
                    tint = if (currentView == AgendaNavView.TIMELINE) OnSurfaceWhite else OnSurfaceWhite.copy(alpha = 0.5f),
                    modifier = Modifier.size(17.dp)
                )
            }

            Spacer(modifier = Modifier.width(6.dp))

            // View 2: Month Grid View
            val monthBg = if (currentView == AgendaNavView.MONTH_GRID) Color.White.copy(alpha = 0.18f) else Color.Transparent
            Box(
                modifier = Modifier
                    .size(36.dp, 32.dp)
                    .clip(CircleShape)
                    .background(monthBg)
                    .clickable { onViewChange(AgendaNavView.MONTH_GRID) },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.GridView,
                    contentDescription = "Month Grid View",
                    tint = if (currentView == AgendaNavView.MONTH_GRID) OnSurfaceWhite else OnSurfaceWhite.copy(alpha = 0.5f),
                    modifier = Modifier.size(17.dp)
                )
            }

            Spacer(modifier = Modifier.width(6.dp))

            // View 3: Email AI Assistant
            val emailBg = if (currentView == AgendaNavView.EMAIL_AI) Color.White.copy(alpha = 0.18f) else Color.Transparent
            Box(
                modifier = Modifier
                    .size(36.dp, 32.dp)
                    .clip(CircleShape)
                    .background(emailBg)
                    .clickable { onViewChange(AgendaNavView.EMAIL_AI) },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = "Email AI Assistant",
                    tint = if (currentView == AgendaNavView.EMAIL_AI) Color(0xFF00E5FF) else OnSurfaceWhite.copy(alpha = 0.5f),
                    modifier = Modifier.size(17.dp)
                )
            }

            Spacer(modifier = Modifier.width(6.dp))

            // View 4: Settings
            val settingsBg = if (currentView == AgendaNavView.SETTINGS) Color.White.copy(alpha = 0.18f) else Color.Transparent
            Box(
                modifier = Modifier
                    .size(36.dp, 32.dp)
                    .clip(CircleShape)
                    .background(settingsBg)
                    .clickable { onViewChange(AgendaNavView.SETTINGS) },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Settings",
                    tint = if (currentView == AgendaNavView.SETTINGS) OnSurfaceWhite else OnSurfaceWhite.copy(alpha = 0.5f),
                    modifier = Modifier.size(17.dp)
                )
            }
        }

        Spacer(modifier = Modifier.width(10.dp))

        // Floating Action Button (+) with accessible 48dp touch target
        IconButton(
            onClick = onAddClick,
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(BrandLimeAccent)
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Add Event",
                tint = OnLimeBlack,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}
