package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Mosque
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.Build
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material.icons.outlined.Mosque
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.example.localization.stringsFor
import com.example.navigation.NoorRoute
import com.example.ui.screens.BookmarksScreen
import com.example.ui.screens.DigitalTasbihScreen
import com.example.ui.screens.DuaLibraryScreen
import com.example.ui.screens.FiveKalimasScreen
import com.example.ui.screens.GlobalSearchScreen
import com.example.ui.screens.GreetingCardsScreen
import com.example.ui.screens.HadithKnowledgeScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.IslamicCalendarScreen
import com.example.ui.screens.IslamicQuizScreen
import com.example.ui.screens.NamazEducationScreen
import com.example.ui.screens.NamesOfAllahScreen
import com.example.ui.screens.NoorSplashScreen
import com.example.ui.screens.PersonalNotesScreen
import com.example.ui.screens.PrayerHubScreen
import com.example.ui.screens.PrivacyAboutScreen
import com.example.ui.screens.ProfileHubScreen
import com.example.ui.screens.QiblaCompassScreen
import com.example.ui.screens.QuranHomeScreen
import com.example.ui.screens.RamadanModuleScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.SurahReaderScreen
import com.example.ui.screens.ToolsHubScreen
import com.example.ui.screens.WallpapersScreen
import com.example.ui.screens.WuduGhuslScreen
import com.example.ui.screens.ZakatCalculatorScreen
import com.example.ui.theme.NoorLifeTheme
import com.example.viewmodel.NoorViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val viewModel: NoorViewModel = viewModel()
            val prefs by viewModel.prefs.collectAsStateWithLifecycle()

            NoorLifeTheme(themeMode = prefs.themeMode) {
                var showSplash by rememberSaveable { mutableStateOf(true) }
                if (showSplash) {
                    NoorSplashScreen(onFinished = { showSplash = false })
                } else {
                    NoorLifeRootApp(viewModel = viewModel)
                }
            }
        }
    }
}

private data class TopLevelTab(
    val route: NoorRoute,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val testTag: String
)

@Composable
fun NoorLifeRootApp(viewModel: NoorViewModel) {
    val prefs by viewModel.prefs.collectAsStateWithLifecycle()
    val strings = stringsFor(prefs.language)
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentDest = backStackEntry?.destination

    val topTabs = listOf(
        TopLevelTab(NoorRoute.Home, strings.navHome, Icons.Filled.Home, Icons.Outlined.Home, "nav_home"),
        TopLevelTab(NoorRoute.QuranHome, strings.navQuran, Icons.Filled.MenuBook, Icons.Outlined.MenuBook, "nav_quran"),
        TopLevelTab(NoorRoute.PrayerHub, strings.navPrayer, Icons.Filled.Mosque, Icons.Outlined.Mosque, "nav_prayer"),
        TopLevelTab(NoorRoute.ToolsHub, strings.navTools, Icons.Filled.Build, Icons.Outlined.Build, "nav_tools"),
        TopLevelTab(NoorRoute.ProfileHub, strings.navProfile, Icons.Filled.Person, Icons.Outlined.Person, "nav_profile")
    )

    val isTopLevelDestination = currentDest?.let { dest ->
        topTabs.any { dest.hasRoute(it.route::class) }
    } ?: true

    fun navigateTopTab(route: NoorRoute) {
        navController.navigate(route) {
            popUpTo(navController.graph.findStartDestination().id) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }
    }

    fun navigateTo(route: NoorRoute) {
        if (topTabs.any { it.route == route }) {
            navigateTopTab(route)
        } else {
            navController.navigate(route) {
                launchSingleTop = true
            }
        }
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val useNavigationRail = maxWidth >= 600.dp

        Scaffold(
            contentWindowInsets = WindowInsets.safeDrawing,
            bottomBar = {
                if (isTopLevelDestination && !useNavigationRail) {
                    NavigationBar {
                        topTabs.forEach { tab ->
                            val selected = currentDest?.hasRoute(tab.route::class) == true
                            NavigationBarItem(
                                selected = selected,
                                onClick = { navigateTopTab(tab.route) },
                                icon = {
                                    Icon(
                                        imageVector = if (selected) tab.selectedIcon else tab.unselectedIcon,
                                        contentDescription = tab.label
                                    )
                                },
                                label = {
                                    Text(
                                        text = tab.label,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                },
                                modifier = Modifier.testTag(tab.testTag)
                            )
                        }
                    }
                }
            }
        ) { innerPadding ->
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                if (isTopLevelDestination && useNavigationRail) {
                    NavigationRail {
                        topTabs.forEach { tab ->
                            val selected = currentDest?.hasRoute(tab.route::class) == true
                            NavigationRailItem(
                                selected = selected,
                                onClick = { navigateTopTab(tab.route) },
                                icon = {
                                    Icon(
                                        imageVector = if (selected) tab.selectedIcon else tab.unselectedIcon,
                                        contentDescription = tab.label
                                    )
                                },
                                label = { Text(tab.label) },
                                modifier = Modifier.testTag(tab.testTag)
                            )
                        }
                    }
                }

                NavHost(
                    navController = navController,
                    startDestination = NoorRoute.Home,
                    modifier = Modifier.weight(1f)
                ) {
                    composable<NoorRoute.Home> {
                        HomeScreen(viewModel = viewModel, strings = strings, onNavigate = ::navigateTo)
                    }
                    composable<NoorRoute.QuranHome> {
                        QuranHomeScreen(viewModel = viewModel, strings = strings, onNavigate = ::navigateTo)
                    }
                    composable<NoorRoute.PrayerHub> {
                        PrayerHubScreen(viewModel = viewModel, strings = strings, onNavigate = ::navigateTo)
                    }
                    composable<NoorRoute.ToolsHub> {
                        ToolsHubScreen(viewModel = viewModel, strings = strings, onNavigate = ::navigateTo)
                    }
                    composable<NoorRoute.ProfileHub> {
                        ProfileHubScreen(viewModel = viewModel, strings = strings, onNavigate = ::navigateTo)
                    }
                    composable<NoorRoute.SurahReader> { entry ->
                        val args = entry.toRoute<NoorRoute.SurahReader>()
                        SurahReaderScreen(
                            surahNumber = args.surahNumber,
                            targetAyah = args.targetAyah,
                            viewModel = viewModel,
                            strings = strings,
                            onBack = { navController.popBackStack() }
                        )
                    }
                    composable<NoorRoute.QiblaCompass> {
                        QiblaCompassScreen(viewModel = viewModel, onBack = { navController.popBackStack() })
                    }
                    composable<NoorRoute.DigitalTasbih> {
                        DigitalTasbihScreen(viewModel = viewModel, onBack = { navController.popBackStack() })
                    }
                    composable<NoorRoute.NamesOfAllah> {
                        NamesOfAllahScreen(viewModel = viewModel, onBack = { navController.popBackStack() })
                    }
                    composable<NoorRoute.DuaLibrary> {
                        DuaLibraryScreen(viewModel = viewModel, onBack = { navController.popBackStack() })
                    }
                    composable<NoorRoute.FiveKalimas> {
                        FiveKalimasScreen(onBack = { navController.popBackStack() })
                    }
                    composable<NoorRoute.NamazEducation> {
                        NamazEducationScreen(onBack = { navController.popBackStack() })
                    }
                    composable<NoorRoute.WuduGhusl> {
                        WuduGhuslScreen(onBack = { navController.popBackStack() })
                    }
                    composable<NoorRoute.IslamicCalendar> {
                        IslamicCalendarScreen(viewModel = viewModel, onBack = { navController.popBackStack() })
                    }
                    composable<NoorRoute.RamadanModule> {
                        RamadanModuleScreen(viewModel = viewModel, onBack = { navController.popBackStack() })
                    }
                    composable<NoorRoute.ZakatCalculator> {
                        ZakatCalculatorScreen(viewModel = viewModel, onBack = { navController.popBackStack() })
                    }
                    composable<NoorRoute.HadithKnowledge> {
                        HadithKnowledgeScreen(viewModel = viewModel, onBack = { navController.popBackStack() })
                    }
                    composable<NoorRoute.IslamicQuiz> {
                        IslamicQuizScreen(viewModel = viewModel, onBack = { navController.popBackStack() })
                    }
                    composable<NoorRoute.GreetingCards> {
                        GreetingCardsScreen(onBack = { navController.popBackStack() })
                    }
                    composable<NoorRoute.Wallpapers> {
                        WallpapersScreen(onBack = { navController.popBackStack() })
                    }
                    composable<NoorRoute.PersonalNotes> {
                        PersonalNotesScreen(viewModel = viewModel, onBack = { navController.popBackStack() })
                    }
                    composable<NoorRoute.GlobalSearch> {
                        GlobalSearchScreen(
                            viewModel = viewModel,
                            strings = strings,
                            onNavigate = ::navigateTo,
                            onBack = { navController.popBackStack() }
                        )
                    }
                    composable<NoorRoute.Bookmarks> {
                        BookmarksScreen(
                            viewModel = viewModel,
                            onNavigate = ::navigateTo,
                            onBack = { navController.popBackStack() }
                        )
                    }
                    composable<NoorRoute.Settings> {
                        SettingsScreen(
                            viewModel = viewModel,
                            strings = strings,
                            onBack = { navController.popBackStack() }
                        )
                    }
                    composable<NoorRoute.PrivacyAbout> {
                        PrivacyAboutScreen(onBack = { navController.popBackStack() })
                    }
                }
            }
        }
    }
}
