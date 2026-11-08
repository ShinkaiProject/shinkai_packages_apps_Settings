/*
 * SPDX-FileCopyrightText: 2024-2025 PixelOS
 * SPDX-FileCopyrightText: 2026 ShinkaiProject
 * SPDX-License-Identifier: Apache-2.0
 */

package com.android.settings.system.spoofing

import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.lazy.rememberLazyListState
import com.android.settings.widget.scrollbar
import com.android.settingslib.spa.framework.theme.SettingsShape
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.WindowInsets
import android.app.ActivityManager
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.DropdownMenuGroup
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.DropdownMenuPopup
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.android.internal.logging.nano.MetricsProto
import com.android.internal.util.custom.PixelDeviceRepository
import com.android.internal.util.custom.PixelPropsUtils
import com.android.settings.R
import com.android.settings.security.applock.*
import com.android.settingslib.spa.framework.theme.SettingsSpace
import com.android.settings.SettingsPreferenceFragment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private const val PP_TARGETS_KEY = "pi_pp_targets"
private const val PP_MODEL_KEY   = "pi_pp_model"
private const val PP_ENABLED_KEY = "pi_pp_spoof"

data class PpAppEntry(
    val packageName: String,
    val label: String,
    val icon: android.graphics.drawable.Drawable?,
    val isSystem: Boolean,
    var isTargeted: Boolean = false,
)

class PixelPropsSettings : SettingsPreferenceFragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requireActivity().title = getString(R.string.spoofing_pixel_props_title)
    }

    override fun getMetricsCategory() = MetricsProto.MetricsEvent.PAGE_VISIBLE

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {}

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View = ComposeView(requireContext()).apply {
        setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
        setContent {
            ExpressiveTheme {
                PixelPropsContent(context = requireContext())
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun PixelPropsContent(context: android.content.Context) {
    val pm = context.packageManager
    val activityManager = context.getSystemService(ActivityManager::class.java)
    val scope = rememberCoroutineScope()
    val isTablet = remember { PixelDeviceRepository.isTabletDevice(context) }

    var globalEnabled by remember {
        mutableStateOf(
            Settings.Secure.getInt(context.contentResolver, PP_ENABLED_KEY, 1) == 1,
        )
    }

    var selectedCodename by remember {
        mutableStateOf(
            Settings.Secure.getString(context.contentResolver, PP_MODEL_KEY)
                ?: if (isTablet) "tangorpro"
                else PixelDeviceRepository.getDefaultPhoneCodename(
                    PixelDeviceRepository.FALLBACK_PROFILES,
                ),
        )
    }
    var menuExpanded by remember { mutableStateOf(false) }
    var profiles by remember {
        mutableStateOf<List<PixelDeviceRepository.DeviceProfile>>(emptyList())
    }

    var searchQuery by remember { mutableStateOf("") }
    var showSystemApps by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(true) }
    val allApps = remember { mutableStateListOf<AppListEntry>() }

    val hiddenApps = remember {
        setOf(
            "com.google.android.googlequicksearchbox",
            "com.google.android.apps.photos",
            "com.google.android.apps.pixel.agent",
            "com.google.android.apps.pixel.creativeassistant",
            "com.snapchat.android",
        )
    }

    fun readTargetsSet(): Set<String> {
        val raw = Settings.Secure.getString(context.contentResolver, PP_TARGETS_KEY)
        return if (raw.isNullOrEmpty()) PixelDeviceRepository.DEFAULT_PP_TARGETS
        else raw.split(",").toSet()
    }

    fun writeTargetsSet(targets: Set<String>) {
        Settings.Secure.putString(
            context.contentResolver,
            PP_TARGETS_KEY,
            targets.joinToString(","),
        )
    }

    LaunchedEffect(Unit) {
        val loadedProfiles = withContext(Dispatchers.IO) {
            val list = PixelDeviceRepository.getStableProfiles(context, false)
            list.ifEmpty { PixelDeviceRepository.FALLBACK_PROFILES }
        }
        profiles = loadedProfiles

        val defaultCodename = if (isTablet) "tangorpro"
            else PixelDeviceRepository.getDefaultPhoneCodename(loadedProfiles)
        val currentSetting = Settings.Secure.getString(context.contentResolver, PP_MODEL_KEY)
        if (currentSetting.isNullOrEmpty()) {
            selectedCodename = defaultCodename
            Settings.Secure.putString(context.contentResolver, PP_MODEL_KEY, defaultCodename)
        }

        val initialTargets = withContext(Dispatchers.IO) { readTargetsSet() }
        val loaded = withContext(Dispatchers.IO) {
            val list = pm.getInstalledApplications(PackageManager.ApplicationInfoFlags.of(0))
            list.filter { !hiddenApps.contains(it.packageName) }
                .map { appInfo ->
                    AppListEntry(
                        packageName = appInfo.packageName,
                        label = appInfo.loadLabel(pm).toString(),
                        icon = appInfo.loadIcon(pm),
                        isSystem = (appInfo.flags and android.content.pm.ApplicationInfo.FLAG_SYSTEM) != 0,
                        isSelected = initialTargets.contains(appInfo.packageName),
                    )
                }
                .sortedBy { it.label.lowercase() }
        }
        allApps.clear()
        allApps.addAll(loaded)
        isLoading = false
    }

    val selectedLabel = remember(selectedCodename, profiles) {
        profiles.firstOrNull { it.codename == selectedCodename }?.label ?: selectedCodename
    }

    val filteredApps = allApps.filter { app ->
        (showSystemApps || !app.isSystem) &&
            (searchQuery.isEmpty() ||
                app.label.contains(searchQuery, ignoreCase = true) ||
                app.packageName.contains(searchQuery, ignoreCase = true))
    }

    val selectedCount = allApps.count { it.isSelected }

    Scaffold(
        contentWindowInsets = WindowInsets.navigationBars,
        containerColor = MaterialTheme.colorScheme.background,
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = SettingsSpace.small2),
        ) {
            Spacer(modifier = Modifier.height(SettingsSpace.extraSmall6))

            ExpressiveMasterSwitchRow(
                titleText = stringResource(R.string.spoofing_pixel_props_title),
                summaryText = when {
                    !globalEnabled -> stringResource(R.string.app_spoofing_disabled)
                    selectedCount == 0 -> stringResource(R.string.ts_no_targets)
                    else -> stringResource(R.string.pp_target_count, selectedCount)
                },
                isChecked = globalEnabled,
                onChange = { enabled ->
                    globalEnabled = enabled
                    Settings.Secure.putInt(
                        context.contentResolver,
                        PP_ENABLED_KEY,
                        if (enabled) 1 else 0,
                    )
                    scope.launch(Dispatchers.IO) {
                        killPackages(activityManager, allApps.map { it.packageName }.toSet())
                    }
                },
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    modifier = Modifier.size(26.dp),
                )
            }

            Spacer(modifier = Modifier.height(SettingsSpace.extraSmall6))

            ExpressiveAnimatedVisibility(visible = globalEnabled) {
                Column {
                    Box(modifier = Modifier.fillMaxWidth()) {
                        ExpressiveActionRow(
                            actions = listOf(
                                ExpressiveAction(
                                    text = "${stringResource(R.string.pp_spoof_as)}: $selectedLabel",
                                    icon = Icons.Default.ArrowDropDown,
                                    onClick = { menuExpanded = true },
                                ),
                            ),
                            modifier = Modifier.fillMaxWidth(),
                        )

                        DropdownMenuPopup(
                            expanded = menuExpanded,
                            onDismissRequest = { menuExpanded = false },
                            shape = SettingsShape.CornerExtraLarge,
                            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                            tonalElevation = 6.dp,
                            modifier = Modifier.heightIn(max = 400.dp),
                        ) {
                            DropdownMenuGroup(
                                modifier = Modifier.padding(horizontal = SettingsSpace.extraSmall2),
                            ) {
                                profiles.forEachIndexed { index, profile ->
                                    val isSelected = profile.codename == selectedCodename
                                    DropdownMenuItem(
                                        text = {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically,
                                            ) {
                                                Text(
                                                    text = profile.label,
                                                    style = MaterialTheme.typography.bodyLarge,
                                                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                                )
                                                if (isSelected) {
                                                    Icon(
                                                        imageVector = Icons.Default.Check,
                                                        contentDescription = null,
                                                        tint = MaterialTheme.colorScheme.primary,
                                                        modifier = Modifier.size(18.dp),
                                                    )
                                                }
                                            }
                                        },
                                        onClick = {
                                            selectedCodename = profile.codename
                                            menuExpanded = false
                                            Settings.Secure.putString(
                                                context.contentResolver,
                                                PP_MODEL_KEY,
                                                profile.codename,
                                            )
                                            val targeted = allApps.filter { it.isSelected }
                                                .map { it.packageName }.toSet()
                                            scope.launch(Dispatchers.IO) {
                                                killPackages(activityManager, targeted)
                                            }
                                        },
                                        shape = ListItemDefaults.segmentedShapes(
                                            index = index,
                                            count = profiles.size,
                                        ).shape,
                                        colors = MenuDefaults.itemColors(
                                            textColor = if (isSelected) MaterialTheme.colorScheme.primary
                                                else MaterialTheme.colorScheme.onSurface,
                                        ),
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(SettingsSpace.extraSmall4))

                    AppPickerSearchField(
                        query = searchQuery,
                        onQueryChange = { searchQuery = it },
                    )

                    Spacer(modifier = Modifier.height(SettingsSpace.extraSmall4))

                    AppPickerActions(
                        showSystemApps = showSystemApps,
                        onToggleSystemApps = { showSystemApps = !showSystemApps },
                        onSelectAll = {
                            filteredApps.forEach { app ->
                                val i = allApps.indexOfFirst {
                                    it.packageName == app.packageName }
                                if (i >= 0 && !allApps[i].isSelected) {
                                    allApps[i] = allApps[i].copy(isSelected = true)
                                    scope.launch(Dispatchers.IO) {
                                        stopPackage(activityManager, app.packageName) }
                                }
                            }
                            val snapshot = allApps.filter { it.isSelected }
                                .map { it.packageName }.toSet()
                            scope.launch(Dispatchers.IO) { writeTargetsSet(snapshot) }
                        },
                        onReset = {
                            allApps.indices.forEach { i ->
                                allApps[i] = allApps[i].copy(
                                    isSelected = allApps[i].packageName in PixelDeviceRepository.DEFAULT_PP_TARGETS,
                                )
                            }
                            scope.launch(Dispatchers.IO) {
                                Settings.Secure.putString(
                                    context.contentResolver,
                                    PixelDeviceRepository.STABLE_CACHE_KEY,
                                    "",
                                )
                                writeTargetsSet(PixelDeviceRepository.DEFAULT_PP_TARGETS)
                                killPackages(activityManager, PixelDeviceRepository.DEFAULT_PP_TARGETS)
                                val fresh = PixelDeviceRepository.getStableProfiles(context, true)
                                val resolved = fresh.ifEmpty { PixelDeviceRepository.FALLBACK_PROFILES }
                                val defaultCodename = if (isTablet) "tangorpro"
                                    else PixelDeviceRepository.getDefaultPhoneCodename(resolved)
                                Settings.Secure.putString(
                                    context.contentResolver,
                                    PP_MODEL_KEY,
                                    defaultCodename,
                                )
                                withContext(Dispatchers.Main) {
                                    profiles = resolved
                                    selectedCodename = defaultCodename
                                }
                            }
                        },
                    )

                    Spacer(modifier = Modifier.height(SettingsSpace.extraSmall4))

                    AppListAnimatedContent(
                        isLoading = isLoading,
                        isEmpty = filteredApps.isEmpty(),
                        hasSearchQuery = searchQuery.isNotEmpty(),
                        modifier = Modifier.weight(1f),
                        emptyContent = {
                            ExpressiveEmptyState(
                                icon = Icons.Default.Android,
                                title = stringResource(R.string.ts_no_targets),
                                description = stringResource(R.string.pp_empty_description),
                                modifier = Modifier.fillMaxSize(),
                            )
                        },
                    ) {
                        val listState = rememberLazyListState()
                        LazyColumn(
                            state = listState,
                            modifier = Modifier
                                .fillMaxWidth()
                                .scrollbar(listState.scrollIndicatorState, Orientation.Vertical),
                            verticalArrangement = Arrangement.spacedBy(SettingsSpace.extraSmall1),
                        ) {
                            itemsIndexed(filteredApps, key = { _, app -> app.packageName }) { index, app ->
                                AppPickerItem(
                                    packageName = app.packageName,
                                    label = app.label,
                                    icon = app.icon,
                                    isSystem = app.isSystem,
                                    checked = app.isSelected,
                                    shapes = ListItemDefaults.segmentedShapes(index = index, count = filteredApps.size),
                                    onToggle = { nowTargeted ->
                                        if (isLoading) return@AppPickerItem
                                        val index = allApps.indexOfFirst {
                                            it.packageName == app.packageName }
                                        if (index < 0) return@AppPickerItem
                                        allApps[index] = allApps[index].copy(isSelected = nowTargeted)
                                        val snapshot = allApps.filter { it.isSelected }
                                            .map { it.packageName }.toSet()
                                        scope.launch(Dispatchers.IO) {
                                            writeTargetsSet(snapshot)
                                            stopPackage(activityManager, app.packageName)
                                        }
                                    },
                                )
                            }
                            item { Spacer(modifier = Modifier.height(80.dp)) }
                        }
                    }
                }
            }
        }
    }
}
