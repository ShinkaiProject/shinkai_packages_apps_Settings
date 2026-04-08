/*
 * Copyright (C) 2025 ShinkaiProject
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.settings.deviceinfo.firmwareversion

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.SystemProperties
import androidx.preference.Preference
import com.android.settings.R
import com.android.settings.utils.getLocale
import com.android.settingslib.DeviceInfoUtils
import com.android.settingslib.metadata.PreferenceAvailabilityProvider
import com.android.settingslib.metadata.PreferenceMetadata
import com.android.settingslib.metadata.PreferenceSummaryProvider
import com.android.settingslib.preference.PreferenceBinding

class ShinkaiVersionPreference :
    PreferenceMetadata,
    PreferenceAvailabilityProvider,
    PreferenceSummaryProvider,
    PreferenceBinding {

    val KEY_SHINKAI_VERSION = "ro.shinkai.version"

    private var currentVersion: String? = null

    override val key: String
        get() = "shinkai_version"

    override val title: Int
        get() = R.string.shinkai_version

    override fun intent(context: Context): Intent? =
        Intent(Intent.ACTION_VIEW)
            .setData(Uri.parse("https://github.com/Shinkaiprjkt"))

    override fun isAvailable(context: Context) = context.getVersion().isNotEmpty()

    override fun getSummary(context: Context) = context.getVersion()

    private fun Context.getVersion(): String =
        SystemProperties.get(KEY_SHINKAI_VERSION, getString(R.string.unknown))

    override fun bind(preference: Preference, metadata: PreferenceMetadata) {
        super.bind(preference, metadata)
        preference.isCopyingEnabled = true
    }
}
