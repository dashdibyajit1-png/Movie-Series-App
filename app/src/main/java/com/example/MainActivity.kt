package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.data.ReleaseItem
import com.example.data.TrackedRelease
import com.example.ui.AssistantState
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.StreamTrackerViewModel
import com.example.ui.theme.CinemaAccent
import com.example.ui.theme.CinemaBlack
import com.example.ui.theme.CinemaDarkCard
import com.example.ui.theme.CinemaGray
import com.example.ui.theme.NetflixRed
import com.example.ui.theme.PrimeBlue

class MainActivity : ComponentActivity() {

    private val viewModel: StreamTrackerViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                MainScreen(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun MainScreen(viewModel: StreamTrackerViewModel) {
    var selectedTab by remember { mutableStateOf("feeds") } // "feeds", "watchlist", "assistant"

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .testTag("app_scaffold"),
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            BottomNavigationBar(
                selectedTab = selectedTab,
                onTabSelected = { selectedTab = it }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (selectedTab) {
                "feeds" -> DiscoverFeedScreen(viewModel = viewModel)
                "watchlist" -> WatchlistScreen(viewModel = viewModel)
                "assistant" -> AssistantScreen(viewModel = viewModel)
            }

            // Dialogs
            viewModel.editingRelease?.let { release ->
                EditReleaseDetailsDialog(
                    release = release,
                    onDismiss = { viewModel.editingRelease = null },
                    onSave = { status, rating, notes ->
                        viewModel.updateReleaseDetails(release.id, status, rating, notes)
                        viewModel.editingRelease = null
                    },
                    onDelete = {
                        viewModel.untrackRelease(release)
                        viewModel.editingRelease = null
                    }
                )
            }

            if (viewModel.showAddCustomDialog) {
                AddCustomReleaseDialog(
                    onDismiss = { viewModel.showAddCustomDialog = false },
                    onAdd = { title, platform, genre, date, syn ->
                        viewModel.addCustomRelease(title, platform, genre, date, syn)
                        viewModel.showAddCustomDialog = false
                    }
                )
            }
        }
    }
}

@Composable
fun BottomNavigationBar(selectedTab: String, onTabSelected: (String) -> Unit) {
    val onBg = MaterialTheme.colorScheme.onBackground
    val muted = Color(0xFF938F99)
    val dividerColor = Color(0xFFE0E0E0)
    
    Column(modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars)) {
        HorizontalDivider(color = dividerColor, thickness = 1.dp)
        NavigationBar(
            containerColor = MaterialTheme.colorScheme.surface,
            tonalElevation = 0.dp
        ) {
        NavigationBarItem(
            selected = selectedTab == "feeds",
            onClick = { onTabSelected("feeds") },
            label = { 
                Text(
                    "Discover", 
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, letterSpacing = 0.sp),
                    fontWeight = if (selectedTab == "feeds") FontWeight.Bold else FontWeight.Medium
                ) 
            },
            icon = { 
                Icon(
                    imageVector = if (selectedTab == "feeds") Icons.Default.Home else Icons.Default.Home, 
                    contentDescription = "Discover Feed"
                ) 
            },
            modifier = Modifier.testTag("track_feed_tab"),
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = onBg,
                selectedTextColor = onBg,
                unselectedIconColor = muted,
                unselectedTextColor = muted,
                indicatorColor = MaterialTheme.colorScheme.background
            )
        )
        NavigationBarItem(
            selected = selectedTab == "watchlist",
            onClick = { onTabSelected("watchlist") },
            label = { 
                Text(
                    "Watchlist", 
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, letterSpacing = 0.sp),
                    fontWeight = if (selectedTab == "watchlist") FontWeight.Bold else FontWeight.Medium
                ) 
            },
            icon = { 
                Icon(
                    imageVector = if (selectedTab == "watchlist") Icons.Default.List else Icons.Default.List,
                    contentDescription = "My Watchlist"
                ) 
            },
            modifier = Modifier.testTag("watchlist_tab"),
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = onBg,
                selectedTextColor = onBg,
                unselectedIconColor = muted,
                unselectedTextColor = muted,
                indicatorColor = MaterialTheme.colorScheme.background
            )
        )
        NavigationBarItem(
            selected = selectedTab == "assistant",
            onClick = { onTabSelected("assistant") },
            label = { 
                Text(
                    "AI Assistant", 
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, letterSpacing = 0.sp),
                    fontWeight = if (selectedTab == "assistant") FontWeight.Bold else FontWeight.Medium
                ) 
            },
            icon = { 
                Icon(
                    imageVector = if (selectedTab == "assistant") Icons.Default.Search else Icons.Default.Search, 
                    contentDescription = "AI Assistant Search"
                ) 
            },
            modifier = Modifier.testTag("ai_assistant_tab"),
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = onBg,
                selectedTextColor = onBg,
                unselectedIconColor = muted,
                unselectedTextColor = muted,
                indicatorColor = MaterialTheme.colorScheme.background
            )
        )
        }
    }
}

// ========================
// TAB 1: DISCOVER/CURATED FEED
// ========================
@Composable
fun DiscoverFeedScreen(viewModel: StreamTrackerViewModel) {
    val allTrackedList by viewModel.allTrackedReleases.collectAsStateWithLifecycle()
    val trackedTitles = remember(allTrackedList) {
        allTrackedList.map { it.title.lowercase().trim() }.toSet()
    }
    
    val curatedFeed = remember(allTrackedList, viewModel.feedPlatformFilter, viewModel.feedSearchQuery) {
        com.example.data.CuratedReleases.list.filter { item ->
            val matchesPlatform = when (viewModel.feedPlatformFilter) {
                "Netflix" -> item.platform == "Netflix"
                "Prime Video" -> item.platform == "Prime Video"
                else -> true
            }
            val matchesSearch = item.title.contains(viewModel.feedSearchQuery, ignoreCase = true) ||
                    item.genre.contains(viewModel.feedSearchQuery, ignoreCase = true)

            matchesPlatform && matchesSearch
        }.map { item ->
            val isTracked = trackedTitles.contains(item.title.lowercase().trim())
            Pair(item, isTracked)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))
        
        HeaderSection("Discover Feed", "Fresh streaming drops")

        // Search Bar
        OutlinedTextField(
            value = viewModel.feedSearchQuery,
            onValueChange = { viewModel.feedSearchQuery = it },
            placeholder = { Text("Search title, genre...", color = Color(0xFF938F99)) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search", tint = Color(0xFF938F99)) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp)
                .testTag("feed_search_input"),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.onBackground,
                unfocusedBorderColor = Color(0xFFE0E0E0),
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
                focusedTextColor = MaterialTheme.colorScheme.onBackground,
                unfocusedTextColor = MaterialTheme.colorScheme.onBackground
            ),
            shape = RoundedCornerShape(12.dp)
        )

        // Platform Filter Tabs
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val filters = listOf("All", "Netflix", "Prime Video")
            filters.forEach { platform ->
                val selected = viewModel.feedPlatformFilter == platform
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(32.dp))
                        .background(
                            color = if (selected) MaterialTheme.colorScheme.onBackground else Color.Transparent
                        )
                        .border(
                            width = 1.dp,
                            color = if (selected) Color.Transparent else Color(0xFFCAC4D0),
                            shape = RoundedCornerShape(32.dp)
                        )
                        .clickable { viewModel.feedPlatformFilter = platform }
                        .padding(vertical = 10.dp)
                        .testTag("filter_chip_${platform.lowercase().replace(" ", "_")}"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = platform.uppercase(),
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            letterSpacing = 1.sp
                        ),
                        color = if (selected) Color.White else Color(0xFF49454F)
                    )
                }
            }
        }

        // List
        if (curatedFeed.isEmpty()) {
            EmptyStateView("No releases match your search.", Icons.Filled.Info)
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                items(curatedFeed) { (release, isTracked) ->
                    DiscoverReleaseCard(
                        release = release,
                        isTracked = isTracked,
                        onTrackClick = {
                            if (isTracked) {
                                viewModel.untrackRelease(release.title)
                            } else {
                                viewModel.trackRelease(release)
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun DiscoverReleaseCard(
    release: ReleaseItem,
    isTracked: Boolean,
    onTrackClick: () -> Unit
) {
    val durationTag = release.durationOrSeasons
    val brandColor = if (release.platform == "Netflix") NetflixRed else PrimeBlue

    // Outer Box with original test tag to preserve testing selectors
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp, end = 8.dp)
            .testTag("discover_card_${release.title.lowercase().replace(" ", "_")}")
    ) {
        // Asymmetric offset branding underlay shape from Editorial Theme
        Box(
            modifier = Modifier
                .matchParentSize()
                .offset(x = 6.dp, y = 6.dp)
                .background(
                    color = brandColor.copy(alpha = 0.12f),
                    shape = RoundedCornerShape(24.dp)
                )
        )

        // Main Paper Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, Color(0xFFE0E0E0))
        ) {
            Column {
                // Visual Banner Header
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(130.dp)
                        .background(
                            Brush.verticalGradient(
                                listOf(brandColor.copy(alpha = 0.3f), Color.Transparent)
                            )
                        )
                ) {
                    if (!release.imageUrl.isNullOrEmpty()) {
                        AsyncImage(
                            model = release.imageUrl,
                            contentDescription = "Cover for ${release.title}",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }
                    
                    // Overlay Platform tag
                    Surface(
                        color = brandColor,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .padding(12.dp)
                            .align(Alignment.TopEnd)
                    ) {
                        Text(
                            text = release.platform.uppercase(),
                            color = Color.White,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            ),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }

                    // Overlay Release Date schedule description
                    Surface(
                        color = Color.Black.copy(alpha = 0.7f),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier
                            .padding(12.dp)
                            .align(Alignment.BottomStart)
                    ) {
                        Text(
                            text = release.releaseDate,
                            color = Color.White,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            ),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                // Text Details
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = release.title,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Serif
                                ),
                                color = MaterialTheme.colorScheme.onBackground,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "${release.genre} • $durationTag".uppercase(),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 11.sp,
                                    color = Color(0xFF49454F),
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.5.sp
                                ),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        
                        Spacer(modifier = Modifier.width(8.dp))

                        // Track Button
                        IconButton(
                            onClick = onTrackClick,
                            modifier = Modifier
                                .background(
                                    color = if (isTracked) Color(0xFF1B5E20) else MaterialTheme.colorScheme.onBackground,
                                    shape = CircleShape
                                )
                                .testTag(
                                    if (isTracked) "untrack_movie_button_${release.title.lowercase().replace(" ", "_")}"
                                    else "track_movie_button_${release.title.lowercase().replace(" ", "_")}"
                                )
                        ) {
                            Icon(
                                imageVector = if (isTracked) Icons.Default.Check else Icons.Default.Add,
                                contentDescription = if (isTracked) "Tracked" else "Track Release",
                                tint = Color.White
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    
                    Text(
                        text = release.synopsis,
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontSize = 13.5.sp,
                            fontFamily = FontFamily.Serif,
                            lineHeight = 19.sp
                        ),
                        color = Color(0xFF49454F),
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

// ========================
// TAB 2: WATCHLIST SCREEN
// ========================
@Composable
fun WatchlistScreen(viewModel: StreamTrackerViewModel) {
    val allTrackedList by viewModel.allTrackedReleases.collectAsStateWithLifecycle()
    
    val watchlist = remember(allTrackedList, viewModel.watchlistStatusFilter, viewModel.watchlistSearchQuery) {
        allTrackedList.filter { item ->
            val matchesStatus = when (viewModel.watchlistStatusFilter) {
                "All" -> true
                else -> item.status.lowercase() == viewModel.watchlistStatusFilter.lowercase()
            }
            val matchesSearch = item.title.contains(viewModel.watchlistSearchQuery, ignoreCase = true) ||
                    item.genre.contains(viewModel.watchlistSearchQuery, ignoreCase = true)

            matchesStatus && matchesSearch
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            HeaderSection("My Watchlist", "Manage your tracker library")
            
            // Add Custom Button
            Button(
                onClick = { viewModel.showAddCustomDialog = true },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.onBackground,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(24.dp),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                modifier = Modifier.testTag("custom_movie_add_button")
            ) {
                Icon(Icons.Default.Edit, contentDescription = "Add custom release", modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    "CUSTOM", 
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 11.sp, 
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                )
            }
        }

        // Search in Watchlist
        OutlinedTextField(
            value = viewModel.watchlistSearchQuery,
            onValueChange = { viewModel.watchlistSearchQuery = it },
            placeholder = { Text("Search watchlist...", color = Color(0xFF938F99)) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search Watchlist", tint = Color(0xFF938F99)) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp)
                .testTag("watchlist_search_input"),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.onBackground,
                unfocusedBorderColor = Color(0xFFE0E0E0),
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
                focusedTextColor = MaterialTheme.colorScheme.onBackground,
                unfocusedTextColor = MaterialTheme.colorScheme.onBackground
            ),
            shape = RoundedCornerShape(12.dp)
        )

        // Watchlist status filters
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            val states = listOf("All", "To Watch", "Watching", "Completed")
            states.forEach { statusState ->
                val selected = viewModel.watchlistStatusFilter == statusState
                val pillBgColor = if (selected) MaterialTheme.colorScheme.onBackground else Color.Transparent
                val pillTextColor = if (selected) Color.White else Color(0xFF49454F)
                val pillBorderColor = if (selected) Color.Transparent else Color(0xFFCAC4D0)

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(32.dp))
                        .background(pillBgColor)
                        .border(
                            width = 1.dp,
                            color = pillBorderColor,
                            shape = RoundedCornerShape(32.dp)
                        )
                        .clickable { viewModel.watchlistStatusFilter = statusState }
                        .padding(vertical = 8.dp)
                        .testTag("watchlist_filter_chip_${statusState.lowercase().replace(" ", "_")}"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = statusState.uppercase(),
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 9.sp,
                            letterSpacing = 0.5.sp
                        ),
                        color = pillTextColor
                    )
                }
            }
        }

        // Watchlist catalog
        if (watchlist.isEmpty()) {
            EmptyStateView("Your track list is empty. Explore releases or add items!", Icons.Filled.Info)
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                items(watchlist) { release ->
                    WatchlistReleaseCard(
                        release = release,
                        onCardEditClick = { viewModel.editingRelease = release }
                    )
                }
            }
        }
    }
}

@Composable
fun WatchlistReleaseCard(
    release: TrackedRelease,
    onCardEditClick: () -> Unit
) {
    val brandColor = if (release.platform == "Netflix") NetflixRed else PrimeBlue
    val statusColor = when (release.status) {
        "Watching" -> PrimeBlue
        "Completed" -> Color(0xFF2E7D32)
        else -> Color(0xFF49454F)
    }

    // Outer Box with original test tag to preserve testing selectors
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp, end = 8.dp)
            .testTag("watchlist_card_${release.title.lowercase().replace(" ", "_")}")
    ) {
        // Asymmetric offset branding underlay shape from Editorial Theme
        Box(
            modifier = Modifier
                .matchParentSize()
                .offset(x = 6.dp, y = 6.dp)
                .background(
                    color = brandColor.copy(alpha = 0.12f),
                    shape = RoundedCornerShape(24.dp)
                )
        )

        // Main Paper Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onCardEditClick() },
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, Color(0xFFE0E0E0))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            // Custom Entry indicator
                            if (release.isCustom) {
                                Text(
                                    text = "CUSTOM • ".uppercase(),
                                    color = CinemaAccent,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 1.sp
                                    )
                                )
                            }
                            Text(
                                text = release.platform.uppercase(),
                                color = brandColor,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.5.sp
                                )
                            )
                        }

                        Spacer(modifier = Modifier.height(2.dp))
                        
                        Text(
                            text = release.title,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Serif
                            ),
                            color = MaterialTheme.colorScheme.onBackground,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = release.genre.uppercase(),
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 10.sp,
                                color = Color(0xFF49454F),
                                letterSpacing = 0.5.sp
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // Clicking this pencil invokes editing parameters
                    IconButton(
                        onClick = onCardEditClick,
                        modifier = Modifier.testTag("edit_movie_button_${release.title.lowercase().replace(" ", "_")}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit, 
                            contentDescription = "Edit Release Details", 
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Rating Stars & Status Indicator Pill
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Stars
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        val currentRating = release.rating?.toInt() ?: 0
                        repeat(5) { starIndex ->
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = "Rating Star ${starIndex + 1}",
                                tint = if (starIndex < currentRating) CinemaAccent else Color(0xFFCAC4D0).copy(alpha = 0.6f),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        if (release.rating == null) {
                            Text(
                                text = " (Not Rated)",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 11.sp,
                                    color = Color(0xFF938F99),
                                    letterSpacing = 0.sp
                                ),
                                modifier = Modifier.padding(start = 4.dp)
                            )
                        } else {
                            Text(
                                text = " (${release.rating.toInt()}/5)",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onBackground,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.sp
                                ),
                                modifier = Modifier.padding(start = 4.dp)
                            )
                        }
                    }

                    // Status Pill
                    Surface(
                        color = statusColor.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, statusColor.copy(alpha = 0.4f))
                    ) {
                        Text(
                            text = release.status.uppercase(),
                            color = statusColor,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp
                            ),
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }

                // User notes displaying
                if (!release.userNotes.isNullOrEmpty()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Surface(
                        color = MaterialTheme.colorScheme.background,
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, Color(0xFFE0E0E0)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "MY NOTES",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 9.sp,
                                    color = brandColor,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp
                                )
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = release.userNotes,
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    fontSize = 13.sp,
                                    fontFamily = FontFamily.Serif,
                                    lineHeight = 18.sp
                                ),
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        }
                    }
                }
            }
        }
    }
}


// ========================
// TAB 3: GEMINI AI ASSISTANT
// ========================
@Composable
fun AssistantScreen(viewModel: StreamTrackerViewModel) {
    val focusManager = LocalFocusManager.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        HeaderSection("AI Assistant", "Generate schedules & recommendations")

        // Chat Text Input Component
        OutlinedTextField(
            value = viewModel.aiQueryText,
            onValueChange = { viewModel.aiQueryText = it },
            placeholder = { Text("Ask: 'Upcoming sci-fi thrillers on Netflix in 2026'...", color = Color(0xFF938F99)) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "AI search prompt", tint = MaterialTheme.colorScheme.onBackground) },
            trailingIcon = {
                if (viewModel.aiQueryText.isNotBlank()) {
                    IconButton(
                        onClick = {
                            focusManager.clearFocus()
                            viewModel.searchWithAI()
                        },
                        modifier = Modifier.testTag("ai_search_button")
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = "Query Gemini assistant", tint = MaterialTheme.colorScheme.onBackground)
                    }
                }
            },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = {
                focusManager.clearFocus()
                viewModel.searchWithAI()
            }),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp)
                .testTag("ai_prompt_input"),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.onBackground,
                unfocusedBorderColor = Color(0xFFE0E0E0),
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
                focusedTextColor = MaterialTheme.colorScheme.onBackground,
                unfocusedTextColor = MaterialTheme.colorScheme.onBackground
            ),
            shape = RoundedCornerShape(12.dp)
        )

        // Suggestion Chips Slider
        Text(
            text = "Try these suggested queries:".uppercase(),
            style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF49454F),
                letterSpacing = 0.5.sp
            ),
            modifier = Modifier.padding(bottom = 6.dp)
        )
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val suggestions = listOf(
                "Upcoming Sci-Fi on Netflix in late 2026",
                "New crime action shows on Amazon Prime",
                "Trending thrillers on both platforms"
            )
            items(suggestions) { suggested ->
                Surface(
                    color = Color.White,
                    shape = RoundedCornerShape(32.dp),
                    border = BorderStroke(1.dp, Color(0xFFCAC4D0)),
                    modifier = Modifier.clickable {
                        focusManager.clearFocus()
                        viewModel.setAIQueryAndSearch(suggested)
                    }
                ) {
                    Text(
                        text = suggested,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onBackground,
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = 0.sp
                        ),
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                    )
                }
            }
        }

        HorizontalDivider(color = Color(0xFFE0E0E0), thickness = 1.dp, modifier = Modifier.padding(vertical = 4.dp))

        // State machine renderer
        when (val state = viewModel.assistantState) {
            is AssistantState.Idle -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search illustration",
                            tint = CinemaGray.copy(alpha = 0.5f),
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            "Ask the AI Assistant to gather real releases\nand add them instantly to your Watchlist!",
                            fontSize = 14.sp,
                            color = CinemaGray,
                            lineHeight = 20.sp,
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        )
                    }
                }
            }
            is AssistantState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = CinemaAccent)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("Gemini is searching schedule feeds...", color = CinemaGray, fontSize = 13.sp)
                    }
                }
            }
            is AssistantState.Error -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(state.message, color = NetflixRed, modifier = Modifier.padding(16.dp))
                }
            }
            is AssistantState.Success -> {
                if (state.releases.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No matching results found. Try rephrasing your search query.", color = CinemaGray)
                    }
                } else {
                    val allTrackedList by viewModel.allTrackedReleases.collectAsStateWithLifecycle()
                    val trackedTitles = allTrackedList.map { it.title.lowercase().trim() }.toSet()

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        contentPadding = PaddingValues(bottom = 16.dp, top = 8.dp)
                    ) {
                        items(state.releases) { release ->
                            val isTracked = trackedTitles.contains(release.title.lowercase().trim())
                            DiscoverReleaseCard(
                                release = release,
                                isTracked = isTracked,
                                onTrackClick = {
                                    if (isTracked) {
                                        viewModel.untrackRelease(release.title)
                                    } else {
                                        viewModel.trackRelease(release)
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}


// ===================================
// CUSTOM REUSABLE COMPONENTS
// ===================================
@Composable
fun HeaderSection(title: String, subtitle: String) {
    Column(modifier = Modifier.padding(vertical = 12.dp)) {
        Text(
            text = title.uppercase(),
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Black,
                letterSpacing = (-0.5).sp
            ),
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = subtitle.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f)
        )
    }
}

@Composable
fun EmptyStateView(message: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color(0xFF938F99).copy(alpha = 0.6f),
                modifier = Modifier.size(56.dp)
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontSize = 14.sp,
                    fontFamily = FontFamily.Serif
                ),
                color = Color(0xFF49454F)
            )
        }
    }
}


// ===================================
// EDIT DIALOG STATE
// ===================================
@Composable
fun EditReleaseDetailsDialog(
    release: TrackedRelease,
    onDismiss: () -> Unit,
    onSave: (String, Float?, String?) -> Unit,
    onDelete: () -> Unit
) {
    var status by remember { mutableStateOf(release.status) }
    var rating by remember { mutableStateOf(release.rating) }
    var notes by remember { mutableStateOf(release.userNotes ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "Update tracked item",
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = Color.White
            )
        },
        containerColor = CinemaDarkCard,
        confirmButton = {
            Button(
                onClick = { onSave(status, rating, notes.ifBlank { null }) },
                colors = ButtonDefaults.buttonColors(containerColor = CinemaAccent, contentColor = Color.Black),
                modifier = Modifier.testTag("save_changes_button")
            ) {
                Text("Save", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            Row {
                TextButton(onClick = onDelete) {
                    Text("Delete", color = NetflixRed, fontWeight = FontWeight.Bold)
                }
                TextButton(onClick = onDismiss) {
                    Text("Cancel", color = Color.White)
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    release.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = CinemaAccent
                )

                // Status row
                Column {
                    Text("Status", color = CinemaGray, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val choices = listOf("To Watch", "Watching", "Completed")
                        choices.forEach { choice ->
                            val active = status == choice
                            val choiceColor = when (choice) {
                                "Watching" -> PrimeBlue
                                "Completed" -> Color(0xFF2E7D32)
                                else -> CinemaGray
                            }

                            Surface(
                                color = if (active) choiceColor.copy(alpha = 0.2f) else CinemaBlack.copy(alpha = 0.3f),
                                border = BorderStroke(1.dp, if (active) choiceColor else CinemaBlack.copy(alpha = 0.3f)),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { status = choice }
                            ) {
                                Box(
                                    modifier = Modifier.padding(vertical = 12.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        choice,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (active) choiceColor else CinemaGray
                                    )
                                }
                            }
                        }
                    }
                }

                // Rating stars
                Column {
                    Text("My Rating", color = CinemaGray, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row {
                            repeat(5) { starIdx ->
                                val score = (starIdx + 1).toFloat()
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = "Edit star ${starIdx + 1}",
                                    tint = if (score <= (rating ?: 0f)) CinemaAccent else CinemaGray.copy(alpha = 0.3f),
                                    modifier = Modifier
                                        .size(28.dp)
                                        .clickable { rating = score }
                                        .testTag("rating_star_$starIdx")
                                )
                            }
                        }
                        if (rating != null) {
                            TextButton(onClick = { rating = null }) {
                                Text("Clear", color = NetflixRed, fontSize = 12.sp)
                            }
                        }
                    }
                }

                // User Notes text area
                Column {
                    Text("User Notes", color = CinemaGray, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp)
                            .padding(top = 8.dp)
                            .testTag("user_notes_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = CinemaAccent,
                            unfocusedBorderColor = CinemaBlack,
                            focusedContainerColor = CinemaBlack.copy(alpha = 0.4f),
                            unfocusedContainerColor = CinemaBlack.copy(alpha = 0.4f)
                        ),
                        shape = RoundedCornerShape(8.dp)
                    )
                }
            }
        }
    )
}


// ===================================
// ADD CUSTOM RELEASES DIALOG
// ===================================
@Composable
fun AddCustomReleaseDialog(
    onDismiss: () -> Unit,
    onAdd: (String, String, String, String, String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var platform by remember { mutableStateOf("Netflix") }
    var genre by remember { mutableStateOf("") }
    var releaseDate by remember { mutableStateOf("") }
    var synopsis by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "Add Custom Show",
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = Color.White
            )
        },
        containerColor = CinemaDarkCard,
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        onAdd(
                            title,
                            platform,
                            genre.ifBlank { "Uncategorized" },
                            releaseDate.ifBlank { "Now Streamable" },
                            synopsis.ifBlank { "No synopsis provided." }
                        )
                    }
                },
                enabled = title.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = PrimeBlue, contentColor = Color.White),
                modifier = Modifier.testTag("custom_add_confirm_button")
            ) {
                Text("Add to Library", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = Color.White)
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Title
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title", color = CinemaGray) },
                    modifier = Modifier.fillMaxWidth().testTag("custom_title_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = PrimeBlue
                    )
                )

                // Platform Choice Segment
                Column {
                    Text("Select Platform", color = CinemaGray, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val platforms = listOf("Netflix", "Prime Video")
                        platforms.forEach { choice ->
                            val active = platform == choice
                            val platformColor = if (choice == "Netflix") NetflixRed else PrimeBlue

                            Surface(
                                color = if (active) platformColor.copy(alpha = 0.2f) else CinemaBlack.copy(alpha = 0.3f),
                                border = BorderStroke(1.dp, if (active) platformColor else CinemaBlack.copy(alpha = 0.3f)),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { platform = choice }
                            ) {
                                Box(
                                    modifier = Modifier.padding(vertical = 10.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        choice.uppercase(),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (active) platformColor else CinemaGray
                                    )
                                }
                            }
                        }
                    }
                }

                // Genre
                OutlinedTextField(
                    value = genre,
                    onValueChange = { genre = it },
                    label = { Text("Genre (e.g., Action / Drama)", color = CinemaGray) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = PrimeBlue
                    )
                )

                // Release Date scheduling
                OutlinedTextField(
                    value = releaseDate,
                    onValueChange = { releaseDate = it },
                    label = { Text("Release Schedule (e.g., Nov 2026)", color = CinemaGray) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = PrimeBlue
                    )
                )

                // Synopsis description text area
                OutlinedTextField(
                    value = synopsis,
                    onValueChange = { synopsis = it },
                    label = { Text("Short Synopsis", color = CinemaGray) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = PrimeBlue
                    )
                )
            }
        }
    )
}
