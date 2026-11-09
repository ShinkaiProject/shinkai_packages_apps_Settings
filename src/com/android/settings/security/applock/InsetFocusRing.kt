/*
 * SPDX-FileCopyrightText: 2024-2025 The Android Open Source Project
 * SPDX-FileCopyrightText: 2026 ShinkaiProject
 * SPDX-License-Identifier: Apache-2.0
 */

package com.android.settings.security.applock

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.android.settings.widget.insetFocusRing as settingsInsetFocusRing

fun Modifier.insetFocusRing(
    shape: Shape,
    ringWidth: Dp = Dp.Unspecified,
    ringHeight: Dp = Dp.Unspecified,
): Modifier = settingsInsetFocusRing(shape, ringWidth, ringHeight)

fun Modifier.switchFocusRing(): Modifier =
    insetFocusRing(CircleShape, ringWidth = 52.dp, ringHeight = 32.dp)

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun Modifier.iconButtonFocusRing(): Modifier {
    val size = IconButtonDefaults.smallContainerSize()
    return insetFocusRing(IconButtonDefaults.standardShape, size.width, size.height)
}

fun Modifier.radioButtonFocusRing(): Modifier =
    insetFocusRing(CircleShape, ringWidth = 48.dp, ringHeight = 48.dp)
