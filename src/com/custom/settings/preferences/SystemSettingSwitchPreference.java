package com.custom.settings.preferences;

import android.content.Context;
import android.provider.Settings;
import android.util.AttributeSet;
import androidx.preference.SwitchPreferenceCompat;

public class SystemSettingSwitchPreference extends SwitchPreferenceCompat {
    public SystemSettingSwitchPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected boolean persistBoolean(boolean value) {
        Settings.System.putInt(getContext().getContentResolver(), getKey(), value ? 1 : 0);
        return true;
    }

    @Override
    protected boolean getPersistedBoolean(boolean defaultReturnValue) {
        return Settings.System.getInt(getContext().getContentResolver(), getKey(),
                defaultReturnValue ? 1 : 0) == 1;
    }
}
