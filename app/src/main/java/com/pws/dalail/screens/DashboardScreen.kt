
// ─── Bottom Nav Item ──────────────────────────────────────────────────────────
package com.pws.dalail.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.pws.dalail.R
import com.pws.dalail.commond.jakartasans
import com.pws.dalail.navigation.AppScreen

// ─── Colors ───────────────────────────────────────────────────────────────────

private val DashGreen         = Color(0xFF1A6B45)
private val DashGreenDark     = Color(0xFF0F4A30)
private val DashGreenLight    = Color(0xFFE8F5EE)
private val DashGreenMuted    = Color(0xFF4CAF80)
private val DashBackground    = Color(0xFFF5F5F5)
private val DashCard          = Color(0xFFFFFFFF)
private val DashTextPrimary   = Color(0xFF1C1C1E)
private val DashTextSecondary = Color(0xFF8E8E93)

// ─── Bottom Nav Item (pakai painter resource) ─────────────────────────────────

data class BottomNavItem(
    val label: String,
    val iconRes: Int,
    val route: String
)

// ─── Main Screen ──────────────────────────────────────────────────────────────

@Composable
fun DashboardScreen(navController: NavController) {
    val bottomNavItems = listOf(
        BottomNavItem("Beranda",    R.drawable.beranda,    AppScreen.Dashboard.route),
        BottomNavItem("Daftar Isi", R.drawable.daftarisi,  AppScreen.Daftarisi.route)
    )

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    Scaffold(
        containerColor = DashBackground,
        bottomBar = {
            DashboardBottomBar(
                items      = bottomNavItems,
                currentRoute = currentRoute,
                onItemClick  = { route ->
                    if (route != currentRoute) {
                        navController.navigate(route) {
                            popUpTo(AppScreen.Dashboard.route) { saveState = true }
                            launchSingleTop = true
                            restoreState    = true
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
        ) {
            DashboardHeader()
            KitabCard(onReadClick = { navController.navigate(AppScreen.Daftarisi.route) })
            Spacer(modifier = Modifier.height(24.dp))
            MenuSection(onDaftarIsiClick = { navController.navigate(AppScreen.Daftarisi.route) })
            Spacer(modifier = Modifier.height(24.dp))
            TerakhirDibacaSection()
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

// ─── Header ──────────────────────────────────────────────────────────────────

@Composable
fun DashboardHeader() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(DashBackground)
            .padding(horizontal = 20.dp, vertical = 24.dp)
    ) {
        Text(
            text       = "Assalamu'alaikum",
            fontSize   = 26.sp,
            fontWeight = FontWeight.Bold,
            color      = DashGreen,
            fontFamily = jakartasans
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text       = "Selamat datang, semoga hari ini penuh\nkeberkahan.",
            fontSize   = 13.sp,
            color      = DashTextSecondary,
            fontFamily = jakartasans,
            lineHeight = 18.sp
        )
    }
}

// ─── Kitab Card ──────────────────────────────────────────────────────────────

@Composable
fun KitabCard(onReadClick: () -> Unit) {
    Card(
        modifier  = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        shape     = RoundedCornerShape(16.dp),
        colors    = CardDefaults.cardColors(containerColor = DashCard),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier            = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Cover Image + Label overlay dalam satu Box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp) // sesuaikan dengan Figma
            ) {
                Image(
                    painter            = painterResource(id = R.drawable.coverkitab),
                    contentDescription = "Kitab Dalailul Khairat",
                    contentScale       = ContentScale.FillBounds,
                    modifier           = Modifier.fillMaxSize()
                )

                Column(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(top = 25.dp), // ← naikkan nilai ini untuk geser ke atas
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(0.dp)
                ) {
                    Text(
                        text          = "KITAB",
                        fontSize      = 11.sp,
                        fontWeight    = FontWeight.Medium,
                        color         = DashTextSecondary,
                        fontFamily    = jakartasans,
                        letterSpacing = 1.2.sp
                    )

                    Box(
                        modifier = Modifier
                            .padding(top = 4.dp)
                            .width(30.dp)
                            .height(2.dp)
                            .background(DashGreen, RoundedCornerShape(1.dp))
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text       = "Dalailul Khairat",
                        fontSize   = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color      = DashTextPrimary,
                        fontFamily = jakartasans
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Tombol Membaca
            Button(
                onClick  = onReadClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .height(46.dp),
                shape  = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = DashGreen)
            ) {
                Icon(
                    imageVector        = Icons.Filled.MenuBook,
                    contentDescription = null,
                    tint               = Color.White,
                    modifier           = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text       = "Membaca",
                    fontSize   = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color      = Color.White,
                    fontFamily = jakartasans
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
// ─── Menu Section ─────────────────────────────────────────────────────────────

@Composable
fun MenuSection(onDaftarIsiClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
    ) {
        Text(
            text       = "Menu",
            fontSize   = 18.sp,
            fontWeight = FontWeight.Bold,
            color      = DashTextPrimary,
            fontFamily = jakartasans
        )
        Spacer(modifier = Modifier.height(12.dp))
        Row(
            modifier            = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            MenuCard(
                iconRes  = R.drawable.daftarisi,
                label    = "Daftar Isi",
                modifier = Modifier.weight(1f),
                onClick  = onDaftarIsiClick
            )
            MenuCard(
                iconRes  = R.drawable.menubookmark,
                label    = "Bookmark",
                modifier = Modifier.weight(1f),
                onClick  = {}
            )
        }
    }
}

@Composable
fun MenuCard(
    iconRes: Int,
    label: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier  = modifier.clickable(onClick = onClick),
        shape     = RoundedCornerShape(12.dp),
        colors    = CardDefaults.cardColors(containerColor = DashCard),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier              = Modifier
                .fillMaxWidth()
                .padding(vertical = 20.dp, horizontal = 12.dp),
            horizontalAlignment   = Alignment.CenterHorizontally,
            verticalArrangement   = Arrangement.Center
        ) {
            Icon(
                painter            = painterResource(id = iconRes),
                contentDescription = label,
                tint               = DashGreenDark,
                modifier           = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text       = label,
                fontSize   = 13.sp,
                fontWeight = FontWeight.Medium,
                color      = DashTextPrimary,
                fontFamily = jakartasans
            )
        }
    }
}

// ─── Terakhir Dibaca ─────────────────────────────────────────────────────────

@Composable
fun TerakhirDibacaSection() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
    ) {
        Text(
            text       = "Terakhir Dibaca",
            fontSize   = 18.sp,
            fontWeight = FontWeight.Bold,
            color      = DashTextPrimary,
            fontFamily = jakartasans
        )
        Spacer(modifier = Modifier.height(12.dp))
        Card(
            modifier  = Modifier.fillMaxWidth(),
            shape     = RoundedCornerShape(12.dp),
            colors    = CardDefaults.cardColors(containerColor = DashCard),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Row(
                modifier              = Modifier
                    .fillMaxWidth()
                    .clickable {}
                    .padding(14.dp),
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier         = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(DashGreenLight),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter            = painterResource(id = R.drawable.daftarisi),
                        contentDescription = null,
                        tint               = DashGreenMuted,
                        modifier           = Modifier.size(22.dp)
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text       = "Hizib Jumat",
                        fontSize   = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color      = DashTextPrimary,
                        fontFamily = jakartasans
                    )
                    Text(
                        text       = "Halaman 120 - 140",
                        fontSize   = 12.sp,
                        color      = DashTextSecondary,
                        fontFamily = jakartasans
                    )
                }
                Icon(
                    imageVector        = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null,
                    tint               = DashTextSecondary,
                    modifier           = Modifier.size(20.dp)
                )
            }
        }
    }
}

// ─── Bottom Navigation Bar ────────────────────────────────────────────────────

@Composable
fun DashboardBottomBar(
    items: List<BottomNavItem>,
    currentRoute: String?,
    onItemClick: (String) -> Unit
) {
    NavigationBar(
        containerColor = DashCard,
        tonalElevation = 8.dp
    ) {
        items.forEach { item ->
            val selected = currentRoute == item.route
            NavigationBarItem(
                selected = selected,
                onClick  = { onItemClick(item.route) },
                icon = {
                    Icon(
                        painter            = painterResource(id = item.iconRes),
                        contentDescription = item.label,
                        modifier           = Modifier.size(22.dp)
                    )
                },
                label = {
                    Text(
                        text       = item.label,
                        fontSize   = 11.sp,
                        fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                        fontFamily = jakartasans
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor   = DashGreen,
                    selectedTextColor   = DashGreen,
                    indicatorColor      = DashGreenLight,
                    unselectedIconColor = DashTextSecondary,
                    unselectedTextColor = DashTextSecondary
                )
            )
        }
    }
}

// ─── Preview ─────────────────────────────────────────────────────────────────

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun DashboardScreenPreview() {
    MaterialTheme {
        DashboardScreen(navController = rememberNavController())
    }
}
