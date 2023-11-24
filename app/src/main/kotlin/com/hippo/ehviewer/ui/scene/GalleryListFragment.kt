package com.hippo.ehviewer.ui.scene

import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.view.HapticFeedbackConstants
import android.view.LayoutInflater
import android.view.View
import android.view.ViewConfiguration
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.rememberLazyStaggeredGridState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text2.input.rememberTextFieldState
import androidx.compose.foundation.text2.input.setTextAndPlaceCursorAtEnd
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material.icons.automirrored.filled.LastPage
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Reorder
import androidx.compose.material.icons.outlined.Bookmarks
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DismissDirection
import androidx.compose.material3.DismissValue
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.ShapeDefaults
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.material3.rememberDismissState
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.LocalViewConfiguration
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastAny
import androidx.compose.ui.util.fastForEach
import androidx.compose.ui.util.lerp
import androidx.core.os.bundleOf
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.paging.LoadState
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingSource
import androidx.paging.PagingState
import androidx.paging.cachedIn
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemContentType
import androidx.paging.compose.itemKey
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.CalendarConstraints.DateValidator
import com.google.android.material.datepicker.CompositeDateValidator
import com.google.android.material.datepicker.DateValidatorPointBackward
import com.google.android.material.datepicker.DateValidatorPointForward
import com.google.android.material.datepicker.MaterialDatePicker
import com.hippo.ehviewer.EhDB
import com.hippo.ehviewer.R
import com.hippo.ehviewer.Settings
import com.hippo.ehviewer.client.EhEngine
import com.hippo.ehviewer.client.EhTagDatabase
import com.hippo.ehviewer.client.EhUrl
import com.hippo.ehviewer.client.EhUtils
import com.hippo.ehviewer.client.data.BaseGalleryInfo
import com.hippo.ehviewer.client.data.ListUrlBuilder
import com.hippo.ehviewer.client.data.ListUrlBuilder.Companion.MODE_IMAGE_SEARCH
import com.hippo.ehviewer.client.data.ListUrlBuilder.Companion.MODE_NORMAL
import com.hippo.ehviewer.client.data.ListUrlBuilder.Companion.MODE_SUBSCRIPTION
import com.hippo.ehviewer.client.data.ListUrlBuilder.Companion.MODE_TAG
import com.hippo.ehviewer.client.data.ListUrlBuilder.Companion.MODE_TOPLIST
import com.hippo.ehviewer.client.data.ListUrlBuilder.Companion.MODE_UPLOADER
import com.hippo.ehviewer.client.data.ListUrlBuilder.Companion.MODE_WHATS_HOT
import com.hippo.ehviewer.client.exception.CloudflareBypassException
import com.hippo.ehviewer.client.parser.GalleryDetailUrlParser
import com.hippo.ehviewer.client.parser.GalleryPageUrlParser
import com.hippo.ehviewer.dao.QuickSearch
import com.hippo.ehviewer.icons.EhIcons
import com.hippo.ehviewer.icons.big.SadAndroid
import com.hippo.ehviewer.icons.filled.GoTo
import com.hippo.ehviewer.image.Image.Companion.decodeBitmap
import com.hippo.ehviewer.ui.MainActivity
import com.hippo.ehviewer.ui.WebViewActivity
import com.hippo.ehviewer.ui.doGalleryInfoAction
import com.hippo.ehviewer.ui.legacy.BaseDialogBuilder
import com.hippo.ehviewer.ui.legacy.FAB_ANIMATE_TIME
import com.hippo.ehviewer.ui.main.AdvancedSearchOption
import com.hippo.ehviewer.ui.main.FabLayout
import com.hippo.ehviewer.ui.main.GalleryInfoGridItem
import com.hippo.ehviewer.ui.main.GalleryInfoListItem
import com.hippo.ehviewer.ui.main.ImageSearch
import com.hippo.ehviewer.ui.main.NormalSearch
import com.hippo.ehviewer.ui.main.SearchAdvanced
import com.hippo.ehviewer.ui.scene.GalleryListFragment.Companion.toStartArgs
import com.hippo.ehviewer.ui.tools.Deferred
import com.hippo.ehviewer.ui.tools.FastScrollLazyColumn
import com.hippo.ehviewer.ui.tools.FastScrollLazyVerticalStaggeredGrid
import com.hippo.ehviewer.ui.tools.LocalDialogState
import com.hippo.ehviewer.ui.tools.LocalTouchSlopProvider
import com.hippo.ehviewer.ui.tools.animateFloatMergePredictiveBackAsState
import com.hippo.ehviewer.ui.tools.observed
import com.hippo.ehviewer.ui.tools.rememberInVM
import com.hippo.ehviewer.util.AppConfig
import com.hippo.ehviewer.util.ExceptionUtils
import com.hippo.ehviewer.util.findActivity
import com.hippo.ehviewer.util.getParcelableCompat
import com.hippo.ehviewer.util.isAtLeastOMR1
import com.hippo.ehviewer.util.isAtLeastR
import com.hippo.ehviewer.util.isAtLeastU
import com.hippo.ehviewer.util.pickVisualMedia
import com.ramcosta.composedestinations.annotation.Destination
import eu.kanade.tachiyomi.util.lang.launchIO
import eu.kanade.tachiyomi.util.lang.withIOContext
import eu.kanade.tachiyomi.util.lang.withUIContext
import eu.kanade.tachiyomi.util.system.pxToDp
import java.io.File
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.roundToInt
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import moe.tarsin.coroutines.runSuspendCatching
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyColumnState

@Destination
@Composable
fun GalleryListScreen(lub: ListUrlBuilder, navigator: NavController) {
    val searchFieldState = rememberTextFieldState()
    var urlBuilder by rememberSaveable(lub) { mutableStateOf(lub) }
    var searchBarOffsetY by remember { mutableStateOf(0) }
    var showSearchLayout by rememberSaveable { mutableStateOf(false) }

    var searchNormalMode by rememberSaveable { mutableStateOf(true) }
    var searchAdvancedMode by rememberSaveable { mutableStateOf(false) }
    var category by rememberSaveable { mutableIntStateOf(Settings.searchCategory) }
    var searchMethod by rememberSaveable { mutableIntStateOf(1) }
    var advancedSearchOption by rememberSaveable { mutableStateOf(AdvancedSearchOption()) }
    var useSimilarityScan by rememberSaveable { mutableStateOf(false) }
    var searchCoverOnly by rememberSaveable { mutableStateOf(false) }
    var imagePath by rememberSaveable { mutableStateOf("") }

    LaunchedEffect(urlBuilder) {
        if (urlBuilder.mode == MODE_SUBSCRIPTION) searchMethod = 2
        if (urlBuilder.category != EhUtils.NONE) category = urlBuilder.category
        if (urlBuilder.mode != MODE_TOPLIST) {
            var keyword = urlBuilder.keyword.orEmpty()
            if (urlBuilder.mode == MODE_TAG) {
                keyword = wrapTagKeyword(keyword)
            }
            if (keyword.isNotBlank()) {
                searchFieldState.setTextAndPlaceCursorAtEnd(keyword)
            }
        }
    }

    val animatedSearchLayout by animateFloatMergePredictiveBackAsState(
        enable = showSearchLayout,
        animationSpec = tween(FAB_ANIMATE_TIME * 2),
    ) { showSearchLayout = false }
    val context = LocalContext.current
    val activity = remember { context.findActivity<MainActivity>() }
    val windowSizeClass = calculateWindowSizeClass(activity)
    val dialogState = LocalDialogState.current
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val listState = rememberLazyListState()
    val gridState = rememberLazyStaggeredGridState()
    val isTopList = remember(urlBuilder) { urlBuilder.mode == MODE_TOPLIST }
    val ehHint = stringResource(R.string.gallery_list_search_bar_hint_e_hentai)
    val exHint = stringResource(R.string.gallery_list_search_bar_hint_exhentai)
    val searchBarHint = remember { if (EhUtils.isExHentai) exHint else ehHint }
    val suitableTitle = getSuitableTitleForUrlBuilder(urlBuilder)
    val data = rememberInVM(urlBuilder) {
        Pager(PagingConfig(25)) {
            object : PagingSource<String, BaseGalleryInfo>() {
                override fun getRefreshKey(state: PagingState<String, BaseGalleryInfo>): String? = null
                override suspend fun load(params: LoadParams<String>) = withIOContext {
                    if (urlBuilder.mode == MODE_TOPLIST) {
                        // TODO: Since we know total pages, let pager support jump
                        val key = (params.key ?: urlBuilder.mJumpTo ?: "0").toInt()
                        val prev = (key - 1).takeIf { it > 0 }
                        val next = (key + 1).takeIf { it < TOPLIST_PAGES }
                        runSuspendCatching {
                            urlBuilder.setJumpTo(key)
                            EhEngine.getGalleryList(urlBuilder.build())
                        }.onFailure {
                            return@withIOContext LoadResult.Error(it)
                        }.onSuccess {
                            return@withIOContext LoadResult.Page(it.galleryInfoList, prev?.toString(), next?.toString())
                        }
                    }
                    when (params) {
                        is LoadParams.Prepend -> urlBuilder.setIndex(params.key, isNext = false)
                        is LoadParams.Append -> urlBuilder.setIndex(params.key, isNext = true)
                        is LoadParams.Refresh -> {
                            val key = params.key
                            if (key.isNullOrBlank()) {
                                if (urlBuilder.mJumpTo != null) {
                                    urlBuilder.mNext ?: urlBuilder.setIndex("2", true)
                                }
                            } else {
                                urlBuilder.setIndex(key, false)
                            }
                        }
                    }
                    val r = runSuspendCatching {
                        if (MODE_IMAGE_SEARCH == urlBuilder.mode) {
                            EhEngine.imageSearch(
                                File(urlBuilder.imagePath!!),
                                urlBuilder.isUseSimilarityScan,
                                urlBuilder.isOnlySearchCovers,
                            )
                        } else {
                            val url = urlBuilder.build()
                            EhEngine.getGalleryList(url)
                        }
                    }.onFailure {
                        return@withIOContext LoadResult.Error(it)
                    }.getOrThrow()
                    urlBuilder.mJumpTo = null
                    LoadResult.Page(r.galleryInfoList, r.prev, r.next)
                }
            }
        }.flow.cachedIn(viewModelScope)
    }.collectAsLazyPagingItems()
    val listMode by remember {
        Settings.listModeBackField.valueFlow()
    }.collectAsState(Settings.listMode)

    val quickSearchList = remember { mutableStateListOf<QuickSearch>() }
    val toplists = (stringArrayResource(id = R.array.toplist_entries) zip stringArrayResource(id = R.array.toplist_values)).toMap()
    val quickSearchName = getSuitableTitleForUrlBuilder(urlBuilder, false)
    var saveProgress by Settings::qSSaveProgress.observed

    fun launchSnackbar(content: String) = coroutineScope.launch { snackbarHostState.showSnackbar(content) }
    fun getFirstVisibleItemIndex() = if (listMode == 0) {
        listState.firstVisibleItemIndex
    } else {
        gridState.firstVisibleItemIndex
    }

    LaunchedEffect(Unit) {
        val list = withIOContext { EhDB.getAllQuickSearch() }
        quickSearchList.addAll(list)
    }

    with(activity) {
        if (isTopList) {
            ProvideSideSheetContent { sheetState ->
                TopAppBar(
                    title = { Text(text = stringResource(id = R.string.toplist)) },
                    windowInsets = WindowInsets(0),
                )
                toplists.forEach { (name, keyword) ->
                    Text(
                        text = name,
                        modifier = Modifier.clickable {
                            Settings.recentToplist = keyword
                            urlBuilder = ListUrlBuilder(MODE_TOPLIST, mKeyword = keyword)
                            showSearchLayout = false
                            coroutineScope.launch { sheetState.close() }
                        }.fillMaxWidth().minimumInteractiveComponentSize().padding(horizontal = 16.dp),
                    )
                }
            }
        } else {
            ProvideSideSheetContent { sheetState ->
                TopAppBar(
                    title = { Text(text = stringResource(id = R.string.quick_search)) },
                    actions = {
                        IconButton(onClick = {
                            coroutineScope.launch {
                                dialogState.awaitPermissionOrCancel(
                                    showCancelButton = false,
                                    title = R.string.quick_search,
                                ) {
                                    Text(text = stringResource(id = R.string.add_quick_search_tip))
                                }
                            }
                        }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Default.Help,
                                contentDescription = stringResource(id = R.string.readme),
                            )
                        }
                        IconButton(onClick = {
                            if (data.itemCount == 0) return@IconButton

                            if (urlBuilder.mode == MODE_IMAGE_SEARCH) {
                                launchSnackbar(context.getString(R.string.image_search_not_quick_search))
                                return@IconButton
                            }

                            val firstItem = data.itemSnapshotList.items[getFirstVisibleItemIndex()]
                            val next = firstItem.gid + 1
                            quickSearchList.fastForEach { q ->
                                if (urlBuilder.equalsQuickSearch(q)) {
                                    val nextStr = q.name.substringAfterLast('@', "")
                                    if (nextStr.toLongOrNull() == next) {
                                        launchSnackbar(context.getString(R.string.duplicate_quick_search, q.name))
                                        return@IconButton
                                    }
                                }
                            }

                            coroutineScope.launch {
                                dialogState.awaitInputTextWithCheckBox(
                                    initial = quickSearchName ?: urlBuilder.keyword.orEmpty(),
                                    title = R.string.add_quick_search_dialog_title,
                                    hint = R.string.quick_search,
                                    checked = saveProgress,
                                    checkBoxText = R.string.save_progress,
                                ) { input, checked ->
                                    var text = input.trim()
                                    if (text.isEmpty()) {
                                        return@awaitInputTextWithCheckBox context.getString(R.string.name_is_empty)
                                    }

                                    if (checked) {
                                        text += "@$next"
                                    }
                                    if (quickSearchList.fastAny { it.name == text }) {
                                        return@awaitInputTextWithCheckBox context.getString(R.string.duplicate_name)
                                    }

                                    val quickSearch = urlBuilder.toQuickSearch(text)
                                    quickSearch.position = quickSearchList.size
                                    // Insert to DB first to update the id
                                    withIOContext {
                                        EhDB.insertQuickSearch(quickSearch)
                                    }
                                    quickSearchList.add(quickSearch)
                                    saveProgress = checked
                                    return@awaitInputTextWithCheckBox null
                                }
                            }
                        }) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = stringResource(id = R.string.add),
                            )
                        }
                    },
                    windowInsets = WindowInsets(0),
                )
                val view = LocalView.current
                Box(modifier = Modifier.fillMaxSize()) {
                    val quickSearchListState = rememberLazyListState()
                    val reorderableLazyListState = rememberReorderableLazyColumnState(quickSearchListState) { from, to ->
                        val fromIndex = from.index
                        val toIndex = to.index
                        quickSearchList.apply {
                            add(toIndex, removeAt(fromIndex))
                        }
                        coroutineScope.launchIO {
                            val range = if (fromIndex < toIndex) fromIndex..toIndex else toIndex..fromIndex
                            val list = quickSearchList.slice(range)
                            list.zip(range).forEach { it.first.position = it.second }
                            EhDB.updateQuickSearch(list)
                        }
                        val feedbackConstant = if (isAtLeastU) {
                            HapticFeedbackConstants.SEGMENT_FREQUENT_TICK
                        } else if (isAtLeastOMR1) {
                            HapticFeedbackConstants.TEXT_HANDLE_MOVE
                        } else {
                            HapticFeedbackConstants.CLOCK_TICK
                        }
                        view.performHapticFeedback(feedbackConstant)
                    }
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        state = quickSearchListState,
                    ) {
                        items(quickSearchList, key = { it.id!! }) { item ->
                            ReorderableItem(reorderableLazyListState, key = item.id!!) {
                                val dismissState = rememberDismissState(
                                    confirmValueChange = {
                                        if (it == DismissValue.DismissedToStart) {
                                            quickSearchList.remove(item)
                                            coroutineScope.launchIO {
                                                EhDB.deleteQuickSearch(item)
                                            }
                                        }
                                        true
                                    },
                                )
                                val viewConfiguration = LocalViewConfiguration.current
                                LocalTouchSlopProvider(Settings.touchSlopFactor.toFloat()) {
                                    SwipeToDismissBox(
                                        state = dismissState,
                                        backgroundContent = {},
                                        directions = setOf(DismissDirection.EndToStart),
                                    ) {
                                        Row(
                                            modifier = Modifier.clickable {
                                                if (urlBuilder.mode == MODE_WHATS_HOT) {
                                                    navigator.navAnimated(R.id.galleryListScene, ListUrlBuilder(item).toStartArgs())
                                                } else {
                                                    urlBuilder = ListUrlBuilder(item)
                                                }
                                                showSearchLayout = false
                                                coroutineScope.launch { sheetState.close() }
                                            }.fillMaxWidth().padding(horizontal = 16.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                        ) {
                                            Text(
                                                text = item.name,
                                                modifier = Modifier.weight(1f),
                                            )
                                            CompositionLocalProvider(LocalViewConfiguration provides viewConfiguration) {
                                                IconButton(
                                                    onClick = {},
                                                    modifier = Modifier.draggableHandle(
                                                        onDragStarted = {
                                                            val feedbackConstant = if (isAtLeastU) {
                                                                HapticFeedbackConstants.DRAG_START
                                                            } else if (isAtLeastR) {
                                                                HapticFeedbackConstants.GESTURE_START
                                                            } else if (isAtLeastOMR1) {
                                                                HapticFeedbackConstants.KEYBOARD_PRESS
                                                            } else {
                                                                HapticFeedbackConstants.VIRTUAL_KEY
                                                            }
                                                            view.performHapticFeedback(feedbackConstant)
                                                        },
                                                        onDragStopped = {
                                                            val feedbackConstant = if (isAtLeastR) {
                                                                HapticFeedbackConstants.GESTURE_END
                                                            } else if (isAtLeastOMR1) {
                                                                HapticFeedbackConstants.KEYBOARD_RELEASE
                                                            } else {
                                                                HapticFeedbackConstants.VIRTUAL_KEY_RELEASE
                                                            }
                                                            view.performHapticFeedback(feedbackConstant)
                                                        },
                                                    ),
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.Reorder,
                                                        contentDescription = null,
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                    Deferred({ delay(200) }) {
                        if (quickSearchList.isEmpty()) {
                            Text(
                                text = stringResource(id = R.string.quick_search_tip),
                                modifier = Modifier.align(Alignment.Center),
                            )
                        }
                    }
                }
            }
        }
    }

    val refreshState = rememberPullToRefreshState {
        data.loadState.refresh is LoadState.NotLoading
    }

    if (refreshState.isRefreshing) {
        LaunchedEffect(Unit) {
            urlBuilder.setIndex(null, true)
            urlBuilder.mJumpTo = null
            data.refresh()
        }
    }

    var hidden by remember { mutableStateOf(false) }
    val searchBarConnection = remember {
        val slop = ViewConfiguration.get(context).scaledTouchSlop
        object : NestedScrollConnection {
            override fun onPostScroll(consumed: Offset, available: Offset, source: NestedScrollSource): Offset {
                val dy = -consumed.y
                if (dy >= slop) {
                    hidden = true
                } else if (dy <= -slop / 2) {
                    hidden = false
                }
                searchBarOffsetY = (searchBarOffsetY - dy).roundToInt().coerceIn(-300, 0)
                return Offset.Zero // We never consume it
            }
        }
    }
    val combinedModifier = Modifier.nestedScroll(searchBarConnection).nestedScroll(refreshState.nestedScrollConnection)

    val openGalleryKeyword = stringResource(R.string.gallery_list_search_bar_open_gallery)
    abstract class UrlSuggestion : Suggestion() {
        override val keyword = openGalleryKeyword
        override val canOpenDirectly = true
        override fun onClick() {
            navigator.navAnimated(destination, args)
            showSearchLayout = false
        }
        abstract val destination: Int
        abstract val args: Bundle
    }

    class GalleryDetailUrlSuggestion(
        gid: Long,
        token: String,
    ) : UrlSuggestion() {
        override val destination = R.id.galleryDetailScene
        override val args = bundleOf(GalleryDetailScene.KEY_ARGS to TokenArgs(gid, token))
    }

    class GalleryPageUrlSuggestion(
        gid: Long,
        pToken: String,
        page: Int,
    ) : UrlSuggestion() {
        override val destination = R.id.progressScene
        override val args = bundleOf(
            ProgressFragment.KEY_GID to gid,
            ProgressFragment.KEY_PTOKEN to pToken,
            ProgressFragment.KEY_PAGE to page,
        )
    }

    var expanded by remember { mutableStateOf(false) }

    val searchErr1 = stringResource(R.string.search_sp_err1)
    val searchErr2 = stringResource(R.string.search_sp_err2)
    val selectImageFirst = stringResource(R.string.select_image_first)
    SearchBarScreen(
        title = suitableTitle,
        searchFieldState = searchFieldState,
        searchFieldHint = searchBarHint,
        showSearchFab = showSearchLayout,
        onApplySearch = { query ->
            val builder = ListUrlBuilder()
            val oldMode = urlBuilder.mode
            if (!showSearchLayout) {
                // If it's MODE_SUBSCRIPTION, keep it
                val newMode = if (oldMode == MODE_SUBSCRIPTION) MODE_SUBSCRIPTION else MODE_NORMAL
                builder.mode = newMode
                builder.keyword = query
            } else {
                if (searchNormalMode) {
                    when (searchMethod) {
                        1 -> builder.mode = MODE_NORMAL
                        2 -> builder.mode = MODE_SUBSCRIPTION
                        3 -> builder.mode = MODE_UPLOADER
                        4 -> builder.mode = MODE_TAG
                    }
                    builder.keyword = query
                    builder.category = category
                    if (searchAdvancedMode) {
                        builder.advanceSearch = advancedSearchOption.advanceSearch
                        builder.minRating = advancedSearchOption.minRating
                        val pageFrom = advancedSearchOption.fromPage
                        val pageTo = advancedSearchOption.toPage
                        if (pageTo != -1 && pageTo < 10) {
                            activity.showTip(searchErr1, BaseScene.LENGTH_LONG)
                            return@SearchBarScreen
                        } else if (pageFrom != -1 && pageTo != -1 && pageTo - pageFrom < 20) {
                            activity.showTip(searchErr2, BaseScene.LENGTH_LONG)
                            return@SearchBarScreen
                        }
                        builder.pageFrom = pageFrom
                        builder.pageTo = pageTo
                    }
                } else {
                    builder.mode = MODE_IMAGE_SEARCH
                    if (imagePath.isBlank()) {
                        activity.showTip(selectImageFirst, BaseScene.LENGTH_LONG)
                        return@SearchBarScreen
                    }
                    val uri = Uri.parse(imagePath)
                    val temp = AppConfig.createTempFile() ?: return@SearchBarScreen
                    val bitmap = context.decodeBitmap(uri) ?: return@SearchBarScreen
                    temp.outputStream().use { bitmap.compress(Bitmap.CompressFormat.JPEG, 90, it) }
                    builder.imagePath = temp.path
                    builder.isUseSimilarityScan = useSimilarityScan
                    builder.isOnlySearchCovers = searchCoverOnly
                }
            }
            when (oldMode) {
                MODE_TOPLIST, MODE_WHATS_HOT -> {
                    // Wait for search view to hide
                    delay(300)
                    withUIContext { navigator.navAnimated(R.id.galleryListScene, builder.toStartArgs()) }
                }
                else -> urlBuilder = builder
            }
            showSearchLayout = false
        },
        onSearchExpanded = { hidden = true },
        onSearchHidden = { hidden = false },
        refreshState = refreshState,
        suggestionProvider = {
            GalleryDetailUrlParser.parse(it, false)?.run {
                GalleryDetailUrlSuggestion(gid, token)
            } ?: GalleryPageUrlParser.parse(it, false)?.run {
                GalleryPageUrlSuggestion(gid, pToken, page)
            }
        },
        searchBarOffsetY = searchBarOffsetY,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        trailingIcon = {
            IconButton(onClick = { activity.openSideSheet() }) {
                Icon(imageVector = Icons.Outlined.Bookmarks, contentDescription = stringResource(id = R.string.quick_search))
            }
            IconButton(onClick = { showSearchLayout = !showSearchLayout }) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    modifier = Modifier.rotate(lerp(90f, 0f, animatedSearchLayout)),
                )
            }
        },
    ) { paddingValues ->
        val layoutDirection = LocalLayoutDirection.current
        val marginH = dimensionResource(id = R.dimen.gallery_list_margin_h)
        val marginV = dimensionResource(id = R.dimen.gallery_list_margin_v)
        val realPadding = PaddingValues(
            top = paddingValues.calculateTopPadding() + marginV,
            bottom = paddingValues.calculateBottomPadding() + marginV,
            start = paddingValues.calculateStartPadding(layoutDirection) + marginH,
            end = paddingValues.calculateEndPadding(layoutDirection) + marginH,
        )

        Column(
            modifier = Modifier.imePadding().verticalScroll(rememberScrollState())
                .padding(
                    top = realPadding.calculateTopPadding(),
                    start = realPadding.calculateStartPadding(layoutDirection),
                    end = realPadding.calculateEndPadding(layoutDirection),
                    bottom = 8.dp,
                )
                .windowInsetsPadding(WindowInsets.systemBars.only(WindowInsetsSides.Bottom))
                .scale(1 - animatedSearchLayout).alpha(1 - animatedSearchLayout),
        ) {
            AnimatedVisibility(visible = searchNormalMode) {
                ElevatedCard(modifier = Modifier.fillMaxWidth().padding(vertical = dimensionResource(id = R.dimen.search_layout_margin_v))) {
                    Column(modifier = Modifier.padding(horizontal = dimensionResource(id = R.dimen.search_category_padding_h), vertical = dimensionResource(id = R.dimen.search_category_padding_v))) {
                        Text(
                            text = stringResource(id = R.string.search_normal),
                            modifier = Modifier.height(dimensionResource(id = R.dimen.search_category_title_height)),
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleMedium,
                        )
                        NormalSearch(
                            category = category,
                            onCategoryChanged = {
                                Settings.searchCategory = it
                                category = it
                            },
                            searchMode = searchMethod,
                            onSearchModeChanged = { searchMethod = it },
                            isAdvanced = searchAdvancedMode,
                            onAdvancedChanged = { searchAdvancedMode = it },
                            showInfo = { BaseDialogBuilder(context).setMessage(R.string.search_tip).show() },
                            maxItemsInEachRow = when (windowSizeClass.widthSizeClass) {
                                WindowWidthSizeClass.Compact -> 2
                                WindowWidthSizeClass.Medium -> 3
                                else -> 5
                            },
                        )
                    }
                }
            }
            AnimatedVisibility(visible = searchNormalMode && searchAdvancedMode) {
                ElevatedCard(modifier = Modifier.fillMaxWidth().padding(vertical = dimensionResource(id = R.dimen.search_layout_margin_v))) {
                    Column(modifier = Modifier.padding(horizontal = dimensionResource(id = R.dimen.search_category_padding_h), vertical = dimensionResource(id = R.dimen.search_category_padding_v))) {
                        Text(
                            text = stringResource(id = R.string.search_advance),
                            modifier = Modifier.height(dimensionResource(id = R.dimen.search_category_title_height)),
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleMedium,
                        )
                        SearchAdvanced(
                            state = advancedSearchOption,
                            onStateChanged = { advancedSearchOption = it },
                        )
                    }
                }
            }
            AnimatedVisibility(visible = !searchNormalMode) {
                ElevatedCard(modifier = Modifier.fillMaxWidth().padding(vertical = dimensionResource(id = R.dimen.search_layout_margin_v))) {
                    Column(modifier = Modifier.padding(horizontal = dimensionResource(id = R.dimen.search_category_padding_h), vertical = dimensionResource(id = R.dimen.search_category_padding_v))) {
                        Text(
                            text = stringResource(id = R.string.search_image),
                            modifier = Modifier.height(dimensionResource(id = R.dimen.search_category_title_height)),
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleMedium,
                        )
                        ImageSearch(
                            imagePath = imagePath,
                            onSelectImage = {
                                coroutineScope.launch {
                                    val image = context.pickVisualMedia(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                    if (image != null) imagePath = image.toString()
                                }
                            },
                            uss = useSimilarityScan,
                            onUssChecked = { useSimilarityScan = it },
                            osc = searchCoverOnly,
                            onOscChecked = { searchCoverOnly = it },
                        )
                    }
                }
            }
            SecondaryTabRow(
                selectedTabIndex = if (searchNormalMode) 0 else 1,
                divider = {},
            ) {
                Tab(
                    selected = searchNormalMode,
                    onClick = { searchNormalMode = true },
                    text = { Text(text = stringResource(id = R.string.keyword_search)) },
                )
                Tab(
                    selected = !searchNormalMode,
                    onClick = { searchNormalMode = false },
                    text = { Text(text = stringResource(id = R.string.search_image)) },
                )
            }
        }

        Box(modifier = Modifier.fillMaxSize().scale(animatedSearchLayout).alpha(animatedSearchLayout)) {
            when (val loadState = data.loadState.refresh) {
                is LoadState.Loading -> if (data.itemCount == 0) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                is LoadState.Error -> {
                    LaunchedEffect(loadState) {
                        if (loadState.error.cause is CloudflareBypassException) {
                            dialogState.awaitPermissionOrCancel(title = R.string.cloudflare_bypass_failed) {
                                Text(text = stringResource(id = R.string.open_in_webview))
                            }
                            withUIContext { navigator.navAnimated(R.id.webView, bundleOf(WebViewActivity.KEY_URL to EhUrl.host)) }
                        }
                    }
                    Column(
                        modifier = Modifier.align(Alignment.Center).widthIn(max = 228.dp)
                            .clip(ShapeDefaults.Small).clickable { data.retry() },
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Icon(
                            imageVector = EhIcons.Big.Default.SadAndroid,
                            contentDescription = null,
                            modifier = Modifier.padding(16.dp).size(120.dp),
                            tint = MaterialTheme.colorScheme.tertiary,
                        )
                        Text(
                            text = ExceptionUtils.getReadableString(loadState.error),
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = TextAlign.Center,
                        )
                    }
                }
                is LoadState.NotLoading -> SideEffect {
                    refreshState.endRefresh()
                }
            }
            if (listMode == 0) {
                val height = (3 * Settings.listThumbSize * 3).pxToDp.dp
                val showPages = Settings.showGalleryPages
                FastScrollLazyColumn(
                    modifier = combinedModifier,
                    state = listState,
                    contentPadding = realPadding,
                    verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.gallery_list_interval)),
                ) {
                    items(
                        count = data.itemCount,
                        key = data.itemKey(key = { item -> item.gid }),
                        contentType = data.itemContentType(),
                    ) { index ->
                        val info = data[index]
                        if (info != null) {
                            GalleryInfoListItem(
                                onClick = {
                                    navigator.navAnimated(
                                        R.id.galleryDetailScene,
                                        bundleOf(GalleryDetailScene.KEY_ARGS to GalleryInfoArgs(info)),
                                    )
                                },
                                onLongClick = {
                                    coroutineScope.launch {
                                        dialogState.doGalleryInfoAction(info, context)
                                    }
                                },
                                info = info,
                                isInFavScene = false,
                                showPages = showPages,
                                modifier = Modifier.height(height),
                            )
                        }
                    }
                }
            } else {
                val gridInterval = dimensionResource(R.dimen.gallery_grid_interval)
                FastScrollLazyVerticalStaggeredGrid(
                    columns = StaggeredGridCells.Adaptive(Settings.thumbSizeDp.dp),
                    modifier = combinedModifier,
                    state = gridState,
                    verticalItemSpacing = gridInterval,
                    horizontalArrangement = Arrangement.spacedBy(gridInterval),
                    contentPadding = realPadding,
                ) {
                    items(
                        count = data.itemCount,
                        key = data.itemKey(key = { item -> item.gid }),
                        contentType = data.itemContentType(),
                    ) { index ->
                        val info = data[index]
                        if (info != null) {
                            GalleryInfoGridItem(
                                onClick = {
                                    navigator.navAnimated(
                                        R.id.galleryDetailScene,
                                        bundleOf(GalleryDetailScene.KEY_ARGS to GalleryInfoArgs(info)),
                                    )
                                },
                                onLongClick = {
                                    coroutineScope.launch {
                                        dialogState.doGalleryInfoAction(info, context)
                                    }
                                },
                                info = info,
                            )
                        }
                    }
                }
            }
        }
    }

    val gotoTitle = stringResource(R.string.go_to)
    val invalidNum = stringResource(R.string.error_invalid_number)
    val outOfRange = stringResource(R.string.error_out_of_range)
    FabLayout(
        hidden = hidden || showSearchLayout,
        expanded = expanded,
        onExpandChanged = { expanded = it },
        autoCancel = true,
    ) {
        onClick(Icons.Default.Refresh) {
            urlBuilder.setIndex(null, true)
            urlBuilder.mJumpTo = null
            data.refresh()
        }
        if (urlBuilder.mode != MODE_WHATS_HOT) {
            onClick(EhIcons.Default.GoTo) {
                if (isTopList) {
                    val page = urlBuilder.mJumpTo?.toIntOrNull() ?: 0
                    val hint = context.getString(R.string.go_to_hint, page + 1, TOPLIST_PAGES)
                    val text = dialogState.awaitInputText(title = gotoTitle, hint = hint, isNumber = true) { oriText ->
                        val text = oriText.trim()
                        val goTo = runCatching {
                            text.toInt() - 1
                        }.onFailure {
                            return@awaitInputText invalidNum
                        }.getOrThrow()
                        if (goTo !in 0..<TOPLIST_PAGES) outOfRange else null
                    }.trim().toInt() - 1
                    urlBuilder.setJumpTo(text)
                    data.refresh()
                } else {
                    val local = LocalDateTime.of(2007, 3, 21, 0, 0)
                    val fromDate =
                        local.atZone(ZoneId.ofOffset("UTC", ZoneOffset.UTC)).toInstant().toEpochMilli()
                    val toDate = MaterialDatePicker.todayInUtcMilliseconds()
                    val listValidators = ArrayList<DateValidator>()
                    listValidators.add(DateValidatorPointForward.from(fromDate))
                    listValidators.add(DateValidatorPointBackward.before(toDate))
                    val constraintsBuilder = CalendarConstraints.Builder()
                        .setStart(fromDate)
                        .setEnd(toDate)
                        .setValidator(CompositeDateValidator.allOf(listValidators))
                    val datePicker = MaterialDatePicker.Builder.datePicker()
                        .setCalendarConstraints(constraintsBuilder.build())
                        .setTitleText(R.string.go_to)
                        .setSelection(toDate)
                        .build()
                    datePicker.show(activity.supportFragmentManager, "date-picker")
                    datePicker.addOnPositiveButtonClickListener { time ->
                        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.US).withZone(ZoneOffset.UTC)
                        val jumpTo = formatter.format(Instant.ofEpochMilli(time))
                        urlBuilder.mJumpTo = jumpTo
                        data.refresh()
                    }
                }
            }
            onClick(Icons.AutoMirrored.Default.LastPage) {
                if (isTopList) {
                    urlBuilder.mJumpTo = "${TOPLIST_PAGES - 1}"
                    data.refresh()
                } else {
                    urlBuilder.setIndex("1", false)
                    data.refresh()
                }
            }
        }
    }
}

class GalleryListFragment : BaseScene() {
    override val enableDrawerGestures = true

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val navController = findNavController()
        val args = when (arguments?.getString(KEY_ACTION) ?: ACTION_HOMEPAGE) {
            ACTION_HOMEPAGE -> ListUrlBuilder()
            ACTION_SUBSCRIPTION -> ListUrlBuilder(MODE_SUBSCRIPTION)
            ACTION_WHATS_HOT -> ListUrlBuilder(MODE_WHATS_HOT)
            ACTION_TOP_LIST -> ListUrlBuilder(MODE_TOPLIST, mKeyword = Settings.recentToplist)
            ACTION_LIST_URL_BUILDER -> arguments?.getParcelableCompat<ListUrlBuilder>(KEY_LIST_URL_BUILDER)?.copy() ?: ListUrlBuilder()
            else -> error("Wrong KEY_ACTION:${arguments?.getString(KEY_ACTION)} when handle args!")
        }
        return ComposeWithMD3 { GalleryListScreen(args, navController) }
    }

    companion object {
        const val KEY_ACTION = "action"
        const val ACTION_HOMEPAGE = "action_homepage"
        const val ACTION_SUBSCRIPTION = "action_subscription"
        const val ACTION_WHATS_HOT = "action_whats_hot"
        const val ACTION_TOP_LIST = "action_top_list"
        const val ACTION_LIST_URL_BUILDER = "action_list_url_builder"
        const val KEY_LIST_URL_BUILDER = "list_url_builder"
        fun ListUrlBuilder.toStartArgs() = bundleOf(
            KEY_ACTION to ACTION_LIST_URL_BUILDER,
            KEY_LIST_URL_BUILDER to this,
        )
    }
}

private const val TOPLIST_PAGES = 200

@Composable
@Stable
private fun getSuitableTitleForUrlBuilder(urlBuilder: ListUrlBuilder, appName: Boolean = true): String? {
    val context = LocalContext.current
    val keyword = urlBuilder.keyword
    val category = urlBuilder.category
    val mode = urlBuilder.mode
    return if (mode == MODE_WHATS_HOT) {
        stringResource(R.string.whats_hot)
    } else if (!keyword.isNullOrEmpty()) {
        when (mode) {
            MODE_TOPLIST -> {
                when (keyword) {
                    "11" -> stringResource(R.string.toplist_alltime)
                    "12" -> stringResource(R.string.toplist_pastyear)
                    "13" -> stringResource(R.string.toplist_pastmonth)
                    "15" -> stringResource(R.string.toplist_yesterday)
                    else -> null
                }
            }
            MODE_TAG -> {
                val canTranslate = Settings.showTagTranslations && EhTagDatabase.isTranslatable(context) && EhTagDatabase.initialized
                wrapTagKeyword(keyword, canTranslate)
            }
            else -> keyword
        }
    } else if (category == EhUtils.NONE && urlBuilder.advanceSearch == -1) {
        val appNameStr = stringResource(R.string.app_name)
        val homepageStr = stringResource(R.string.homepage)
        when (mode) {
            MODE_NORMAL -> if (appName) appNameStr else homepageStr
            MODE_SUBSCRIPTION -> stringResource(R.string.subscription)
            else -> null
        }
    } else if (category.countOneBits() == 1) {
        EhUtils.getCategory(category)
    } else {
        null
    }
}