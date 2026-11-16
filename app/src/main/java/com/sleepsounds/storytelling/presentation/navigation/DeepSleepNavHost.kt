package com.sleepsounds.storytelling.presentation.navigation

import android.os.SystemClock
import androidx.activity.compose.BackHandler
import androidx.activity.compose.LocalActivity
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.sleepsounds.storytelling.ads.AdMobManager
import com.sleepsounds.storytelling.ads.StickyCollapsibleBannerAdBar
import com.sleepsounds.storytelling.presentation.components.DeepSleepBottomBar
import com.sleepsounds.storytelling.presentation.components.NowPlayingBar
import com.sleepsounds.storytelling.presentation.dream.DreamDictionaryScreen
import com.sleepsounds.storytelling.presentation.home.HomeScreen
import com.sleepsounds.storytelling.presentation.journal.SleepJournalScreen
import com.sleepsounds.storytelling.presentation.meditate.BreathingScreen
import com.sleepsounds.storytelling.presentation.mixer.MixerScreen
import com.sleepsounds.storytelling.presentation.onboarding.AppStartDestination
import com.sleepsounds.storytelling.presentation.onboarding.OnboardingScreen
import com.sleepsounds.storytelling.presentation.onboarding.OnboardingViewModel
import com.sleepsounds.storytelling.presentation.paywall.PaywallScreen
import com.sleepsounds.storytelling.presentation.player.NowPlayingScreen
import com.sleepsounds.storytelling.presentation.podcard.PodcardDetailScreen
import com.sleepsounds.storytelling.presentation.products.ProductsScreen
import com.sleepsounds.storytelling.presentation.profile.ProfileScreen
import com.sleepsounds.storytelling.presentation.profile.TermsScreen
import com.sleepsounds.storytelling.presentation.scene.SceneDetailScreen
import com.sleepsounds.storytelling.presentation.sounds.SoundsTabScreen
import com.sleepsounds.storytelling.presentation.tracker.SleepTrackerScreen
import com.sleepsounds.storytelling.service.AudioPlayerManager
import com.sleepsounds.storytelling.service.PlaybackDeepLinks
import com.sleepsounds.storytelling.service.PlaybackMode
import com.sleepsounds.storytelling.service.PlaybackNavigationManager
import com.sleepsounds.storytelling.service.PlaybackState
import com.sleepsounds.storytelling.service.SleepTimerManager
import com.sleepsounds.storytelling.ui.theme.MidnightBlue

@Composable
fun DeepSleepNavHost(
    audioPlayerManager: AudioPlayerManager,
    sleepTimerManager: SleepTimerManager,
    playbackNavigationManager: PlaybackNavigationManager,
    adMobManager: AdMobManager,
    isSplashVisible: Boolean = false,
    initialRoute: String? = null,
    onDeepLinkConsumed: () -> Unit = {},
    launchSessionId: Int = 1,
    onContentFirstFrame: () -> Unit = {},
    onboardingViewModel: OnboardingViewModel = hiltViewModel(),
) {
    val navController = rememberNavController()
    val hostActivity = LocalActivity.current
    var tabScrollResetGeneration by rememberSaveable { mutableIntStateOf(0) }
    var lastNavigationClickElapsedMs by remember { mutableLongStateOf(0L) }

    fun isBackStackReadyForNavigation(): Boolean {
        val topEntry = navController.currentBackStackEntry ?: return false
        if (!topEntry.lifecycle.currentState.isAtLeast(Lifecycle.State.CREATED)) return false
        val prevEntry = navController.previousBackStackEntry
        return prevEntry == null || prevEntry.lifecycle.currentState.isAtLeast(Lifecycle.State.CREATED)
    }

    fun allowUserNavigation(cooldownMs: Long = 220L): Boolean {
        val now = SystemClock.elapsedRealtime()
        if (now - lastNavigationClickElapsedMs < cooldownMs) return false
        if (!isBackStackReadyForNavigation()) return false
        lastNavigationClickElapsedMs = now
        return true
    }

    fun switchBottomTab(destinationRoute: String) {
        if (!isBackStackReadyForNavigation()) return
        val activeRoute = navController.currentBackStackEntry?.destination?.route
        if (activeRoute == destinationRoute) return
        adMobManager.onUserAction(hostActivity) {
            runCatching {
                if (destinationRoute == AppRoute.Home.route) {
                    val popped = navController.popBackStack(AppRoute.Home.route, inclusive = false)
                    if (!popped) {
                        navController.navigate(AppRoute.Home.route) {
                            popUpTo(MAIN_GRAPH_ROUTE) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                } else {
                    navController.navigate(destinationRoute) {
                        popUpTo(AppRoute.Home.route) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            }
        }
    }

    fun navigateUpSafely() {
        if (!isBackStackReadyForNavigation()) return
        adMobManager.onBackNavigation(hostActivity) {
            runCatching {
                val popped = navController.popBackStack()
                if (!popped && navController.currentBackStackEntry?.destination?.route != AppRoute.Home.route) {
                    switchBottomTab(AppRoute.Home.route)
                }
            }
        }
    }

    fun openPaywallScreen() {
        if (!allowUserNavigation()) return
        if (navController.currentBackStackEntry?.destination?.route == AppRoute.Paywall.route) return
        runCatching {
            navController.navigate(AppRoute.Paywall.route) {
                launchSingleTop = true
            }
        }
    }

    fun openSceneDetailScreen(sceneId: String) {
        if (sceneId.isBlank() || !allowUserNavigation()) return
        val topEntry = navController.currentBackStackEntry
        val activeRoute = topEntry?.destination?.route
        val activeSceneId = topEntry?.arguments?.getString(AppRoute.SceneDetail.SCENE_ID_ARG)
        if (activeRoute == AppRoute.SceneDetail.route && activeSceneId == sceneId) return

        adMobManager.maybeShowInterstitial(hostActivity) {
            runCatching {
                navController.navigate(AppRoute.SceneDetail.withId(sceneId)) {
                    launchSingleTop = true
                }
            }
        }
    }

    fun openPodcastDetailScreen(podcardId: String) {
        if (podcardId.isBlank() || !allowUserNavigation()) return
        val topEntry = navController.currentBackStackEntry
        val activeRoute = topEntry?.destination?.route
        val activePodcardId = topEntry?.arguments?.getString(AppRoute.PodcardDetail.PODCARD_ID_ARG)
        if (activeRoute == AppRoute.PodcardDetail.route && activePodcardId == podcardId) return

        adMobManager.onUserAction(hostActivity) {
            if (activeRoute == AppRoute.PodcardDetail.route) {
                runCatching {
                    topEntry.savedStateHandle[AppRoute.PodcardDetail.PODCARD_ID_ARG] = podcardId
                }
            } else {
                runCatching {
                    navController.navigate(AppRoute.PodcardDetail.withId(podcardId)) {
                        launchSingleTop = true
                    }
                }
            }
        }
    }

    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStackEntry?.destination?.route
    val appStartDestination by onboardingViewModel.appStartDestination.collectAsStateWithLifecycle()
    val hasSelectedLanguageFirstOpen by onboardingViewModel.hasSelectedLanguageFirstOpen.collectAsStateWithLifecycle()
    val playbackState by audioPlayerManager.playbackState.collectAsStateWithLifecycle()

    var queuedDeepLink by remember { mutableStateOf(initialRoute) }
    var isMainGraphMounted by remember { mutableStateOf(false) }
    var aidsInitialSubtab by rememberSaveable { mutableIntStateOf(1) }
    var selectedDreamId by rememberSaveable { mutableStateOf<String?>(null) }

    LaunchedEffect(initialRoute) {
        if (!initialRoute.isNullOrBlank()) {
            queuedDeepLink = initialRoute
        }
    }

    LaunchedEffect(currentRoute) {
        if (currentRoute != null &&
            currentRoute != AppRoute.Bootstrap.route &&
            currentRoute != AppRoute.Onboarding.route
        ) {
            isMainGraphMounted = true
        }
    }

    LaunchedEffect(isMainGraphMounted, launchSessionId, appStartDestination) {
        if (isMainGraphMounted && appStartDestination == AppStartDestination.Home) {
            onContentFirstFrame()
        }
    }

    val tabBarItems = remember { bottomNavItems(includeProducts = true) }
    val tabBarRouteSet = remember(tabBarItems) { tabBarItems.map { it.route.route }.toSet() }
    val isTabBarVisible = currentRoute in tabBarRouteSet

    LaunchedEffect(Unit) {
        playbackNavigationManager.navigateToNowPlaying.collect {
            if (isMainGraphMounted) {
                val target = PlaybackDeepLinks.routeFor(audioPlayerManager.playbackState.value)
                routeToActivePlaybackScreen(navController, target, audioPlayerManager.playbackState.value)
            }
        }
    }

    LaunchedEffect(isMainGraphMounted, queuedDeepLink) {
        if (!isMainGraphMounted) return@LaunchedEffect
        val target = queuedDeepLink ?: return@LaunchedEffect
        queuedDeepLink = null
        onDeepLinkConsumed()
        onContentFirstFrame()
        routeToActivePlaybackScreen(navController, target, audioPlayerManager.playbackState.value)
    }

    LaunchedEffect(Unit) {
        playbackNavigationManager.deepLinkRoute.collect { route: String ->
            if (isMainGraphMounted) {
                queuedDeepLink = null
                onDeepLinkConsumed()
                onContentFirstFrame()
                routeToActivePlaybackScreen(navController, route, audioPlayerManager.playbackState.value)
            } else {
                queuedDeepLink = route
            }
        }
    }

    val onSceneScreen = currentRoute?.startsWith("scene/") == true
    val onPodcastScreen = currentRoute?.startsWith("podcard/") == true
    val playbackActive = playbackState.mode != PlaybackMode.IDLE
    val showMiniPlayerBar = when {
        currentRoute == AppRoute.Sounds.route -> false
        currentRoute == AppRoute.Products.route -> false
        onSceneScreen || onPodcastScreen -> false
        isTabBarVisible -> playbackActive
        else -> false
    }
    val showStickyBottomBanner = isMainGraphMounted &&
        currentRoute != null &&
        currentRoute != AppRoute.Bootstrap.route &&
        currentRoute != AppRoute.Onboarding.route &&
        currentRoute != AppRoute.Paywall.route
    val hasBottomChrome = showMiniPlayerBar || isTabBarVisible || showStickyBottomBanner
    val navBarBottomInsets = WindowInsets.navigationBars.only(WindowInsetsSides.Bottom)

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            containerColor = MidnightBlue,
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            bottomBar = {
                Column(
                    modifier = if (hasBottomChrome) {
                        Modifier
                            .fillMaxWidth()
                            .windowInsetsPadding(navBarBottomInsets)
                            .consumeWindowInsets(navBarBottomInsets)
                    } else {
                        Modifier.fillMaxWidth()
                    },
                ) {
                    if (showMiniPlayerBar) {
                        NowPlayingBar(
                            audioPlayerManager = audioPlayerManager,
                            sleepTimerManager = sleepTimerManager,
                            onExpand = {
                                val route = PlaybackDeepLinks.routeFor(playbackState)
                                routeToActivePlaybackScreen(navController, route, playbackState)
                            },
                        )
                    }
                    if (isTabBarVisible) {
                        DeepSleepBottomBar(
                            items = tabBarItems,
                            currentRoute = currentRoute,
                            onItemSelected = { item ->
                                if (currentRoute != item.route.route) {
                                    switchBottomTab(item.route.route)
                                } else {
                                    tabScrollResetGeneration++
                                }
                            },
                        )
                    }
                    if (showStickyBottomBanner) {
                        StickyCollapsibleBannerAdBar(
                            modifier = Modifier.fillMaxWidth(),
                            adMobManager = adMobManager,
                        )
                    }
                }
            },
        ) { scaffoldPadding ->
            NavHost(
                navController = navController,
                startDestination = remember { AppRoute.Bootstrap.route },
                enterTransition = { EnterTransition.None },
                exitTransition = { ExitTransition.None },
                popEnterTransition = { EnterTransition.None },
                popExitTransition = { ExitTransition.None },
                sizeTransform = null,
                modifier = Modifier
                    .padding(bottom = scaffoldPadding.calculateBottomPadding())
                    .then(
                        if (hasBottomChrome) {
                            Modifier.consumeWindowInsets(navBarBottomInsets)
                        } else {
                            Modifier
                        },
                    ),
            ) {
                composable(AppRoute.Bootstrap.route) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MidnightBlue),
                    )
                    LaunchedEffect(appStartDestination, queuedDeepLink) {
                        if (!queuedDeepLink.isNullOrBlank() && hasSelectedLanguageFirstOpen) {
                            runCatching {
                                navController.navigate(MAIN_GRAPH_ROUTE) {
                                    popUpTo(AppRoute.Bootstrap.route) { inclusive = true }
                                    launchSingleTop = true
                                }
                            }
                            return@LaunchedEffect
                        }
                        when (appStartDestination) {
                            AppStartDestination.Loading -> Unit
                            AppStartDestination.Onboarding -> {
                                runCatching {
                                    navController.navigate(AppRoute.Onboarding.route) {
                                        popUpTo(AppRoute.Bootstrap.route) { inclusive = true }
                                        launchSingleTop = true
                                    }
                                }
                            }
                            AppStartDestination.Home -> {
                                runCatching {
                                    navController.navigate(MAIN_GRAPH_ROUTE) {
                                        popUpTo(AppRoute.Bootstrap.route) { inclusive = true }
                                        launchSingleTop = true
                                    }
                                }
                            }
                        }
                    }
                }

                composable(AppRoute.Onboarding.route) {
                    OnboardingScreen(
                        onFirstContentReady = onContentFirstFrame,
                        onComplete = {
                            onboardingViewModel.completeOnboarding()
                            adMobManager.resetClickCounter()
                            runCatching {
                                navController.navigate(MAIN_GRAPH_ROUTE) {
                                    popUpTo(AppRoute.Onboarding.route) { inclusive = true }
                                    launchSingleTop = true
                                }
                            }
                        },
                    )
                }

                navigation(
                    route = MAIN_GRAPH_ROUTE,
                    startDestination = AppRoute.Home.route,
                    enterTransition = { EnterTransition.None },
                    exitTransition = { ExitTransition.None },
                    popEnterTransition = { EnterTransition.None },
                    popExitTransition = { ExitTransition.None },
                    sizeTransform = null,
                ) {
                    composable(AppRoute.Home.route) {
                        HomeScreen(
                            onFirstContentReady = onContentFirstFrame,
                            tabResetGeneration = tabScrollResetGeneration,
                            onNavigateToSettings = { switchBottomTab(AppRoute.Profile.route) },
                            onNavigateToSceneDetail = ::openSceneDetailScreen,
                            onNavigateToAids = { subtab ->
                                aidsInitialSubtab = subtab
                                switchBottomTab(AppRoute.Sounds.route)
                            },
                            onNavigateToPodcardDetail = ::openPodcastDetailScreen,
                            onNavigateToDreamDictionary = { dreamId ->
                                if (allowUserNavigation()) {
                                    selectedDreamId = dreamId
                                    adMobManager.onUserAction(hostActivity) {
                                        runCatching {
                                            navController.navigate(AppRoute.DreamDictionary.route) {
                                                launchSingleTop = true
                                            }
                                        }
                                    }
                                }
                            },
                            onNavigateToBreathing = {
                                if (allowUserNavigation()) {
                                    adMobManager.onUserAction(hostActivity) {
                                        runCatching {
                                            navController.navigate(AppRoute.Breathing.route) {
                                                launchSingleTop = true
                                            }
                                        }
                                    }
                                }
                            },
                            onNavigateToPaywall = ::openPaywallScreen,
                        )
                    }

                    composable(AppRoute.Sounds.route) {
                        SoundsTabScreen(
                            sleepTimerManager = sleepTimerManager,
                            tabResetGeneration = tabScrollResetGeneration,
                            initialSubtab = aidsInitialSubtab,
                            onOpenMixDetail = {
                                if (allowUserNavigation()) {
                                    adMobManager.onUserAction(hostActivity) {
                                        runCatching {
                                            navController.navigate(AppRoute.NowPlaying.route) {
                                                launchSingleTop = true
                                            }
                                        }
                                    }
                                }
                            },
                            onNavigateToSceneDetail = ::openSceneDetailScreen,
                            onNavigateToPodcardDetail = ::openPodcastDetailScreen,
                            onOpenPaywall = ::openPaywallScreen,
                        )
                    }

                    composable(AppRoute.Tracker.route) {
                        SleepTrackerScreen(
                            onOpenAids = {
                                aidsInitialSubtab = 1
                                switchBottomTab(AppRoute.Sounds.route)
                            },
                            onSessionSavedNavigateToJournal = {
                                switchBottomTab(AppRoute.Journal.route)
                            },
                        )
                    }

                    composable(AppRoute.Journal.route) {
                        SleepJournalScreen(
                            onNavigateToTracker = { switchBottomTab(AppRoute.Tracker.route) },
                        )
                    }

                    composable(AppRoute.Products.route) {
                        ProductsScreen(tabResetGeneration = tabScrollResetGeneration)
                    }

                    composable(AppRoute.Profile.route) {
                        ProfileScreen(
                            onNavigateToBreathing = {
                                if (allowUserNavigation()) {
                                    adMobManager.onUserAction(hostActivity) {
                                        runCatching {
                                            navController.navigate(AppRoute.Breathing.route) {
                                                launchSingleTop = true
                                            }
                                        }
                                    }
                                }
                            },
                            onNavigateToTerms = {
                                if (allowUserNavigation()) {
                                    adMobManager.onUserAction(hostActivity) {
                                        runCatching {
                                            navController.navigate(AppRoute.Terms.route) {
                                                launchSingleTop = true
                                            }
                                        }
                                    }
                                }
                            },
                            onNavigateToTracker = { switchBottomTab(AppRoute.Tracker.route) },
                            onNavigateToJournal = { switchBottomTab(AppRoute.Journal.route) },
                            onNavigateToDreamDictionary = {
                                if (allowUserNavigation()) {
                                    selectedDreamId = null
                                    adMobManager.onUserAction(hostActivity) {
                                        runCatching {
                                            navController.navigate(AppRoute.DreamDictionary.route) {
                                                launchSingleTop = true
                                            }
                                        }
                                    }
                                }
                            },
                            onNavigateToProducts = { switchBottomTab(AppRoute.Products.route) },
                            onNavigateToPaywall = ::openPaywallScreen,
                            onReplayOnboarding = {
                                if (allowUserNavigation()) {
                                    runCatching {
                                        navController.navigate(AppRoute.Onboarding.route) {
                                            launchSingleTop = true
                                        }
                                    }
                                }
                            },
                        )
                    }
                }

                composable(AppRoute.Paywall.route) {
                    PaywallScreen(
                        onBack = ::navigateUpSafely,
                        onOpenTerms = {
                            if (allowUserNavigation()) {
                                runCatching {
                                    navController.navigate(AppRoute.Terms.route) {
                                        launchSingleTop = true
                                    }
                                }
                            }
                        },
                    )
                }

                composable(AppRoute.DreamDictionary.route) {
                    DreamDictionaryScreen(
                        initialDreamId = selectedDreamId,
                        onBack = ::navigateUpSafely,
                    )
                }

                composable(
                    route = AppRoute.SceneDetail.route,
                    arguments = listOf(
                        navArgument(AppRoute.SceneDetail.SCENE_ID_ARG) { type = NavType.StringType },
                    ),
                ) {
                    SceneDetailScreen(
                        onBack = ::navigateUpSafely,
                        onOpenMixer = { presetId ->
                            if (allowUserNavigation()) {
                                adMobManager.onUserAction(hostActivity) {
                                    runCatching {
                                        navController.navigate(AppRoute.Mixer.route(presetId)) {
                                            launchSingleTop = true
                                        }
                                    }
                                }
                            }
                        },
                        sleepTimerManager = sleepTimerManager,
                    )
                }

                composable(
                    route = AppRoute.PodcardDetail.route,
                    arguments = listOf(
                        navArgument(AppRoute.PodcardDetail.PODCARD_ID_ARG) { type = NavType.StringType },
                    ),
                ) {
                    PodcardDetailScreen(
                        onBack = ::navigateUpSafely,
                        onOpenRelated = ::openPodcastDetailScreen,
                        onOpenPaywall = ::openPaywallScreen,
                    )
                }

                composable(
                    route = AppRoute.Mixer.route,
                    arguments = listOf(
                        navArgument(AppRoute.Mixer.PRESET_ID_ARG) {
                            type = NavType.StringType
                            defaultValue = ""
                        },
                    ),
                ) {
                    MixerScreen(
                        onBack = ::navigateUpSafely,
                        onOpenPaywall = ::openPaywallScreen,
                    )
                }

                composable(AppRoute.NowPlaying.route) {
                    NowPlayingScreen(
                        onBack = ::navigateUpSafely,
                        onOpenMixer = { editTargetId ->
                            if (allowUserNavigation()) {
                                adMobManager.onUserAction(hostActivity) {
                                    runCatching {
                                        navController.navigate(AppRoute.Mixer.route(editTargetId)) {
                                            launchSingleTop = true
                                        }
                                    }
                                }
                            }
                        },
                        sleepTimerManager = sleepTimerManager,
                        onStartTracker = { switchBottomTab(AppRoute.Tracker.route) },
                    )
                }

                composable(AppRoute.Breathing.route) {
                    BreathingScreen(onBack = ::navigateUpSafely)
                }

                composable(AppRoute.Terms.route) {
                    TermsScreen(onBack = ::navigateUpSafely)
                }
            }
        }
    }

    val interceptBackPress = currentRoute != null &&
        currentRoute != AppRoute.Bootstrap.route &&
        currentRoute != AppRoute.Onboarding.route

    BackHandler(enabled = interceptBackPress) {
        when {
            currentRoute == AppRoute.Home.route -> hostActivity?.moveTaskToBack(true)
            currentRoute in tabBarRouteSet -> switchBottomTab(AppRoute.Home.route)
            else -> navigateUpSafely()
        }
    }
}

private fun routeToActivePlaybackScreen(
    navController: NavHostController,
    requestedRoute: String,
    playbackState: PlaybackState,
) {
    val activeEntry = navController.currentBackStackEntry ?: return
    if (!activeEntry.lifecycle.currentState.isAtLeast(Lifecycle.State.CREATED)) return

    val prevEntry = navController.previousBackStackEntry
    if (prevEntry != null && !prevEntry.lifecycle.currentState.isAtLeast(Lifecycle.State.CREATED)) return

    val targetRoute = when {
        requestedRoute.startsWith("scene/") ||
            requestedRoute.startsWith("podcard/") ||
            requestedRoute.startsWith("mixer") ||
            requestedRoute == AppRoute.NowPlaying.route -> requestedRoute

        playbackState.mode == PlaybackMode.PODCARD -> AppRoute.NowPlaying.route
        playbackState.mode == PlaybackMode.MIXER && playbackState.currentTrackId != null ->
            PlaybackDeepLinks.sceneRoute(playbackState.currentTrackId)

        playbackState.mode != PlaybackMode.IDLE -> AppRoute.NowPlaying.route
        else -> return
    }

    if (activeEntry.destination.route == targetRoute) return

    runCatching {
        navController.navigate(targetRoute) {
            launchSingleTop = true
        }
    }
}
