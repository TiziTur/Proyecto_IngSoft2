@file:Suppress("WildcardImport")

package com.aprovecha.app.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private data class BottomNavItem(
    val label: String,
    val icon: ImageVector,
    val route: String
)

@Composable
fun AprovechaBottomBar(
    currentRoute: String,
    isCommerce: Boolean,
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val consumerItems = listOf(
        BottomNavItem("Inicio", Icons.Default.Home, "home_consumer"),
        BottomNavItem("Favoritos", Icons.Default.Favorite, "favorites"),
        BottomNavItem("Reservas", Icons.Default.DateRange, "my_reservations"),
        BottomNavItem("Perfil", Icons.Default.Person, "profile")
    )
    val commerceItems = listOf(
        BottomNavItem("Inicio", Icons.Default.Home, "home_commerce"),
        BottomNavItem("Perfil", Icons.Default.Person, "profile")
    )
    val items = if (isCommerce) commerceItems else consumerItems

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            shape = RoundedCornerShape(32.dp),
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 8.dp,
            tonalElevation = 4.dp
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                items.forEach { item ->
                    BottomNavTab(
                        item = item,
                        isActive = currentRoute == item.route,
                        onClick = { onNavigate(item.route) }
                    )
                }
            }
        }
    }
}

@Composable
private fun BottomNavTab(item: BottomNavItem, isActive: Boolean, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(24.dp),
        color = if (isActive) MaterialTheme.colorScheme.primary else Color.Transparent
    ) {
        Row(
            modifier = Modifier.padding(horizontal = if (isActive) 14.dp else 10.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                item.icon,
                contentDescription = item.label,
                tint = if (isActive) MaterialTheme.colorScheme.onPrimary
                       else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
            if (isActive) {
                Text(
                    item.label,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    }
}
