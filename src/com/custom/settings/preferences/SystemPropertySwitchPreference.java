package com.custom.settings.preferences;

import android.content.Context;
import android.os.SystemProperties;
import android.util.AttributeSet;
import androidx.preference.SwitchPreferenceCompat;

public class SystemPropertySwitchPreference extends SwitchPreferenceCompat {
    public SystemPropertySwitchPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected boolean persistBoolean(boolean value) {
        SystemProperties.set(getKey(), value ? "true" : "false");
        return true;
    }

    @Override
    protected boolean getPersistedBoolean(boolean defaultReturnValue) {
        return SystemProperties.getBoolean(getKey(), defaultReturnValue);
    }
}
