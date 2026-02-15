package demo.nexa.clinical_transcription_demo.ui.component

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import demo.nexa.clinical_transcription_demo.ui.theme.AppColors

/**
 * Navigation item for bottom bar.
 */
data class BottomNavItem(
    val id: String,
    val label: String,
    val icon: ImageVector,
    val contentDescription: String
)

/**
 * Bottom Navigation Bar for frequent features.
 * Replaces FABs with a persistent, accessible navigation bar.
 *
 * @param items Navigation items to display
 * @param selectedItemId Currently selected item ID
 * @param onItemSelected Callback when user selects an item
 * @param modifier Modifier for styling
 */
@Composable
fun AppBottomNavigationBar(
    items: List<BottomNavItem>,
    selectedItemId: String,
    onItemSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(72.dp)
            .background(AppColors.SurfaceWhite)
            .drawBehind {
                val strokeWidth = 1.dp.toPx()
                drawLine(
                    color = AppColors.BorderLight,
                    start = Offset(0f, 0f),
                    end = Offset(size.width, 0f),
                    strokeWidth = strokeWidth
                )
            }
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        items.forEach { item ->
            BottomNavBarItem(
                item = item,
                isSelected = item.id == selectedItemId,
                onClick = { onItemSelected(item.id) }
            )
        }
    }
}

/**
 * Individual bottom navigation item.
 */
@Composable
private fun BottomNavBarItem(
    item: BottomNavItem,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val iconColor by animateColorAsState(
        targetValue = if (isSelected) AppColors.TealDark else AppColors.TextTertiary,
        label = "navItemIconColor"
    )

    val textColor by animateColorAsState(
        targetValue = if (isSelected) AppColors.TealDark else AppColors.TextTertiary,
        label = "navItemTextColor"
    )

    Column(
        modifier = Modifier
            .size(width = 60.dp, height = 56.dp)
            .clickable(
                onClick = onClick,
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            )
            .padding(4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = item.icon,
            contentDescription = item.contentDescription,
            tint = iconColor,
            modifier = Modifier.size(24.dp)
        )

        Text(
            text = item.label,
            fontSize = 10.sp,
            fontWeight = FontWeight.Medium,
            color = textColor,
            modifier = Modifier.padding(top = 2.dp)
        )
    }
}

/**
 * Create standard bottom navigation items.
 */
object BottomNavigation {
    fun defaultItems() = listOf(
        BottomNavItem(
            id = "chat",
            label = "Chat",
            icon = Icons.Filled.Chat,
            contentDescription = "Chat Home"
        ),
        BottomNavItem(
            id = "record",
            label = "Record",
            icon = Icons.Filled.Mic,
            contentDescription = "Record Audio"
        ),
        BottomNavItem(
            id = "notes",
            label = "Notes",
            icon = Icons.Filled.Edit,
            contentDescription = "View Notes"
        ),
        BottomNavItem(
            id = "settings",
            label = "Settings",
            icon = Icons.Filled.Settings,
            contentDescription = "Settings"
        )
    )
}
