package com.example

import android.Manifest
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.SupportAgent
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.credentials.CredentialManager
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.auth.attemptAutoSignIn
import com.example.auth.authStateFlow
import com.example.auth.signOutUser
import com.example.data.AureviaFirestoreRepository
import com.example.data.CatalogRepository
import com.example.ui.components.PackageComparisonDialog
import com.example.ui.screens.AccountScreen
import com.example.ui.screens.AuthScreen
import com.example.ui.screens.BookingWizardScreen
import com.example.ui.screens.BookingsPaymentsScreen
import com.example.ui.screens.DiscoverScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.ServiceDetailScreen
import com.example.ui.screens.SupportScreen
import com.example.ui.theme.AureviaTheme
import com.example.ui.theme.ChampagneGold
import com.example.ui.theme.ChampagneGoldLight
import com.example.ui.theme.DeepCharcoal
import com.example.ui.theme.IvoryBackground
import com.example.ui.theme.IvorySurface
import com.example.ui.theme.MutedTaupe
import com.example.ui.viewmodel.AureviaPortalViewModel
import com.example.ui.viewmodel.PortalEvent
import com.example.ui.viewmodel.UiState
import com.example.util.NotificationHelper
import com.google.firebase.Firebase
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        window.setFlags(
            WindowManager.LayoutParams.FLAG_SECURE,
            WindowManager.LayoutParams.FLAG_SECURE
        )
        NotificationHelper.ensureChannel(this)

        setContent {
            AureviaTheme {
                AureviaAppRoot()
            }
        }
    }
}

private data class PortalTabItem(
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val testTag: String
)

@Composable
private fun AureviaAppRoot() {
    val context = LocalContext.current
    val firebaseReady = remember(context) {
        FirebaseApp.getApps(context).isNotEmpty() ||
            runCatching { FirebaseApp.initializeApp(context) != null }.getOrDefault(false)
    }

    if (!firebaseReady) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Aurevia Firebase Configuration Required",
                    style = MaterialTheme.typography.headlineSmall,
                    color = DeepCharcoal
                )
                Text(
                    text = "This APK was compiled without google-services.json. Upload app/google-services.json to your GitHub repository (under the app/ folder) and rebuild the APK so Google Sign-In and Cloud Firestore can initialize.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MutedTaupe
                )
            }
        }
        return
    }

    val scope = rememberCoroutineScope()
    val credentialManager = remember(context) { CredentialManager.create(context) }

    val authUser by Firebase.auth.authStateFlow()
        .collectAsStateWithLifecycle(initialValue = Firebase.auth.currentUser)

    var autoSignInChecked by remember { mutableStateOf(Firebase.auth.currentUser != null) }

    LaunchedEffect(Unit) {
        if (Firebase.auth.currentUser == null) {
            attemptAutoSignIn(
                context = context,
                credentialManager = credentialManager,
                onAuthSuccess = { autoSignInChecked = true },
                onUnauthenticated = { autoSignInChecked = true },
                scope = scope
            )
        }
    }

    val user = authUser
    if (!autoSignInChecked && user == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = ChampagneGold)
        }
    } else if (user == null) {
        AuthScreen(onAuthSuccess = { autoSignInChecked = true })
    } else {
        key(user.uid) {
            AureviaPortalShell(
                currentUserId = user.uid,
                displayName = user.displayName ?: "Aurevia Member",
                email = user.email ?: "",
                credentialManager = credentialManager
            )
        }
    }
}

@Composable
private fun AureviaPortalShell(
    currentUserId: String,
    displayName: String,
    email: String,
    credentialManager: CredentialManager
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val viewModel: AureviaPortalViewModel = viewModel(
        key = currentUserId,
        factory = viewModelFactory {
            initializer {
                val app = checkNotNull(this[APPLICATION_KEY])
                val databaseId = app.getString(R.string.firestore_database_id)
                val db = FirebaseFirestore.getInstance(databaseId)
                AureviaPortalViewModel(
                    catalog = CatalogRepository(),
                    repository = AureviaFirestoreRepository(db),
                    currentUserId = currentUserId,
                    initialDisplayName = displayName,
                    initialEmail = email
                )
            }
        }
    )

    val dashboardState by viewModel.dashboardState.collectAsStateWithLifecycle()
    val filteredServices by viewModel.filteredServices.collectAsStateWithLifecycle()
    val filter by viewModel.filter.collectAsStateWithLifecycle()
    val bookingDraft by viewModel.bookingDraft.collectAsStateWithLifecycle()
    val selectedServiceId by viewModel.selectedServiceId.collectAsStateWithLifecycle()
    val compareServiceId by viewModel.compareServiceId.collectAsStateWithLifecycle()
    val activeTicketId by viewModel.activeTicketId.collectAsStateWithLifecycle()
    val activeMessages by viewModel.activeTicketMessages.collectAsStateWithLifecycle()
    val isBusy by viewModel.isBusy.collectAsStateWithLifecycle()

    var notificationPromptAttempted by rememberSaveable { mutableStateOf(false) }
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { /* Permission state is reflected dynamically via NotificationHelper */ }

    LaunchedEffect(currentUserId) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            !notificationPromptAttempted &&
            !NotificationHelper.hasNotificationPermission(context)
        ) {
            notificationPromptAttempted = true
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                is PortalEvent.ShowMessage -> snackbarHostState.showSnackbar(event.text)
                is PortalEvent.TriggerLocalNotification ->
                    NotificationHelper.showLocalNotification(context, event.title, event.body, event.type)
                PortalEvent.ForceRemoteSignOut ->
                    signOutUser(credentialManager, scope)
            }
        }
    }

    var selectedTab by remember { mutableIntStateOf(0) }

    val tabs = listOf(
        PortalTabItem("Home", Icons.Filled.Home, Icons.Outlined.Home, "nav_home"),
        PortalTabItem("Discover", Icons.Filled.Explore, Icons.Outlined.Explore, "nav_discover"),
        PortalTabItem("Bookings", Icons.Filled.CalendarMonth, Icons.Outlined.CalendarMonth, "nav_bookings"),
        PortalTabItem("Support", Icons.Filled.SupportAgent, Icons.Outlined.SupportAgent, "nav_support"),
        PortalTabItem("Account", Icons.Filled.AccountCircle, Icons.Outlined.AccountCircle, "nav_account")
    )

    val dashData = (dashboardState as? UiState.Success)?.data
    val favoriteIds = remember(dashData) {
        dashData?.favorites?.map { it.serviceId }?.toSet() ?: emptySet()
    }

    // Full-screen overlay routes (Booking Wizard / Service Detail)
    if (bookingDraft.service != null) {
        BookingWizardScreen(
            draft = bookingDraft,
            savedAddresses = dashData?.addresses.orEmpty(),
            onClose = {
                val wasConfirmed = bookingDraft.confirmedBookingCode != null
                viewModel.closeBookingWizard()
                if (wasConfirmed) selectedTab = 2
            },
            onSetStep = viewModel::setBookingStep,
            onUpdateLocation = viewModel::updateBookingLocation,
            onUpdateDate = viewModel::updateBookingDate,
            onUpdateTimeSlot = viewModel::updateBookingTimeSlot,
            onUpdatePackage = viewModel::updateBookingPackage,
            onUpdateNotesPromo = viewModel::updateBookingNotesAndPromo,
            onUpdateConsent = viewModel::updateBookingConsent,
            onUpdatePayment = viewModel::updatePaymentDetails,
            onInitiateOrder = viewModel::initiatePendingPaymentOrder,
            onConfirmGateway = viewModel::confirmSandboxGatewayPayment
        )
        return
    }

    val selectedService = selectedServiceId?.let { viewModel.catalog.getServiceById(it) }
    if (selectedService != null) {
        ServiceDetailScreen(
            service = selectedService,
            isFavorite = selectedService.id in favoriteIds,
            reviews = dashData?.reviews.orEmpty(),
            onBack = { viewModel.openServiceDetail(null) },
            onToggleFavorite = { viewModel.toggleFavorite(selectedService, selectedService.id in favoriteIds) },
            onComparePackages = { viewModel.openPackageCompare(selectedService.id) },
            onBookPackage = { pkg ->
                viewModel.openServiceDetail(null)
                viewModel.startBooking(selectedService, pkg)
            },
            onSubmitSafetyReport = { reason, details, block ->
                viewModel.submitSafetyReport(
                    serviceId = selectedService.id,
                    providerId = selectedService.providerId,
                    providerName = selectedService.providerName,
                    bookingId = "",
                    reason = reason,
                    details = details,
                    blockProvider = block
                )
            }
        )
        compareServiceId?.let { cmpId ->
            viewModel.catalog.getServiceById(cmpId)?.let { cmpService ->
                PackageComparisonDialog(
                    service = cmpService,
                    onDismiss = { viewModel.openPackageCompare(null) },
                    onSelectPackageToBook = { pkg ->
                        viewModel.openPackageCompare(null)
                        viewModel.openServiceDetail(null)
                        viewModel.startBooking(cmpService, pkg)
                    }
                )
            }
        }
        return
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val useRail = maxWidth >= 600.dp

        Scaffold(
            containerColor = IvoryBackground,
            snackbarHost = { SnackbarHost(snackbarHostState) },
            bottomBar = {
                if (!useRail) {
                    NavigationBar(containerColor = IvorySurface) {
                        tabs.forEachIndexed { idx, tab ->
                            val selected = selectedTab == idx
                            NavigationBarItem(
                                selected = selected,
                                onClick = { selectedTab = idx },
                                icon = {
                                    Icon(
                                        imageVector = if (selected) tab.selectedIcon else tab.unselectedIcon,
                                        contentDescription = tab.label
                                    )
                                },
                                label = { Text(tab.label, style = MaterialTheme.typography.labelSmall) },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = DeepCharcoal,
                                    selectedTextColor = DeepCharcoal,
                                    indicatorColor = ChampagneGoldLight,
                                    unselectedIconColor = MutedTaupe,
                                    unselectedTextColor = MutedTaupe
                                ),
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
                if (useRail) {
                    NavigationRail(containerColor = IvorySurface) {
                        tabs.forEachIndexed { idx, tab ->
                            val selected = selectedTab == idx
                            NavigationRailItem(
                                selected = selected,
                                onClick = { selectedTab = idx },
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

                Box(modifier = Modifier.weight(1f)) {
                    when (selectedTab) {
                        0 -> HomeScreen(
                            catalog = viewModel.catalog,
                            dashboardData = dashData,
                            onOpenNotifications = { selectedTab = 4 },
                            onNavigateDiscover = { category, city ->
                                viewModel.selectCategoryFilter(category)
                                viewModel.selectCityFilter(city)
                                selectedTab = 1
                            },
                            onOpenServiceDetail = { viewModel.openServiceDetail(it.id) },
                            onBookService = { viewModel.startBooking(it) },
                            onToggleFavorite = viewModel::toggleFavorite,
                            onOpenSupport = { selectedTab = 3 }
                        )

                        1 -> DiscoverScreen(
                            catalog = viewModel.catalog,
                            filter = filter,
                            services = filteredServices,
                            favoriteIds = favoriteIds,
                            onSearchChange = viewModel::updateSearchQuery,
                            onCategorySelect = viewModel::selectCategoryFilter,
                            onCitySelect = viewModel::selectCityFilter,
                            onDateSelect = viewModel::selectDateFilter,
                            onMaxPriceChange = viewModel::updateMaxPrice,
                            onMinRatingChange = viewModel::updateMinRating,
                            onClearFilters = viewModel::clearFilters,
                            onToggleFavorite = viewModel::toggleFavorite,
                            onOpenDetails = { viewModel.openServiceDetail(it.id) },
                            onBookNow = { viewModel.startBooking(it) },
                            onComparePackages = { viewModel.openPackageCompare(it.id) }
                        )

                        2 -> {
                            val reviewedBookingIds = remember(dashData) {
                                dashData?.reviews?.map { it.bookingId }?.toSet() ?: emptySet()
                            }
                            BookingsPaymentsScreen(
                                bookings = dashData?.bookings.orEmpty(),
                                payments = dashData?.payments.orEmpty(),
                                reviewedBookingIds = reviewedBookingIds,
                                onConfirmPendingOrder = viewModel::confirmPendingOrderFromLedger,
                                onMarkCompleted = viewModel::markCompleted,
                                onReschedule = viewModel::rescheduleBooking,
                                onCancelBooking = viewModel::cancelBooking,
                                onSubmitReview = viewModel::submitReview,
                                onSendReminder = viewModel::sendBookingReminder,
                                onFinalizeRefund = viewModel::finalizeRefund,
                                onDiscoverServices = { selectedTab = 1 }
                            )
                        }

                        3 -> SupportScreen(
                            faqs = viewModel.catalog.faqs,
                            tickets = dashData?.tickets.orEmpty(),
                            activeTicketId = activeTicketId,
                            activeMessages = activeMessages,
                            onSelectTicket = viewModel::openTicketChat,
                            onCreateTicket = viewModel::createTicket,
                            onSendChatMessage = viewModel::sendChatMessage
                        )

                        4 -> AccountScreen(
                            dashboardData = dashData,
                            allServices = viewModel.catalog.services,
                            isBusy = isBusy,
                            onSaveProfile = viewModel::saveProfile,
                            onAddAddress = viewModel::addSavedAddress,
                            onDeleteAddress = viewModel::deleteSavedAddress,
                            onOpenService = { viewModel.openServiceDetail(it.id) },
                            onMarkNotificationRead = viewModel::markNotificationRead,
                            onMarkAllNotificationsRead = viewModel::markAllNotificationsRead,
                            onSendTestNotification = viewModel::sendTestNotification,
                            onSignOutSingleDevice = {
                                signOutUser(credentialManager, scope)
                            },
                            onSignOutAllDevices = {
                                scope.launch {
                                    viewModel.prepareLogoutAllDevices()
                                    signOutUser(credentialManager, scope)
                                }
                            }
                        )
                    }
                }
            }
        }
    }

    compareServiceId?.let { cmpId ->
        viewModel.catalog.getServiceById(cmpId)?.let { cmpService ->
            PackageComparisonDialog(
                service = cmpService,
                onDismiss = { viewModel.openPackageCompare(null) },
                onSelectPackageToBook = { pkg ->
                    viewModel.openPackageCompare(null)
                    viewModel.startBooking(cmpService, pkg)
                }
            )
        }
    }
}
