/*
 * Copyright (C) 2025 The Android Open Source Project
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

package com.android.settings.applications;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.UserHandle;
import android.text.TextUtils;

import androidx.preference.Preference;

import com.android.settings.core.BasePreferenceController;

/**
 * Settings entry point for the Sandbox app ("AxionOS Sandbox").
 *
 * Sandbox is a system-privileged privacy/isolation app (com.android.axion.sandbox)
 * whose launcher icon is intentionally not exposed; this controller is how the user
 * reaches it from the Settings app.
 */
public class SandboxController extends BasePreferenceController {

    private static final String SANDBOX_PACKAGE = "com.android.axion.sandbox";
    private static final String SANDBOX_MAIN_ACTIVITY = "com.android.axion.sandbox.MainActivity";

    private final PackageManager mPackageManager;

    public SandboxController(Context context, String preferenceKey) {
        super(context, preferenceKey);
        mPackageManager = mContext.getPackageManager();
    }

    private Intent settingsIntent() {
        Intent intent = new Intent();
        ComponentName component = new ComponentName(SANDBOX_PACKAGE, SANDBOX_MAIN_ACTIVITY);
        intent.setComponent(component);
        return intent;
    }

    @Override
    public int getAvailabilityStatus() {
        return mContext.getPackageManager().resolveActivity(settingsIntent(), 0) != null
                ? AVAILABLE : UNSUPPORTED_ON_DEVICE;
    }

    @Override
    public boolean handlePreferenceTreeClick(Preference preference) {
        if (!TextUtils.equals(preference.getKey(), getPreferenceKey())) {
            return false;
        }

        Intent intent = settingsIntent();
        intent.putExtra("referer", this.getClass().getCanonicalName());
        mContext.startActivityAsUser(intent, UserHandle.CURRENT);
        return true;
    }
}
