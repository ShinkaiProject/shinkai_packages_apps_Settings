package com.custom.settings.preferences;

import android.content.Context;
import android.provider.Settings;
import android.util.AttributeSet;
import androidx.preference.SwitchPreferenceCompat;

public class GlobalSettingSwitchPreference extends SwitchPreferenceCompat {
    public GlobalSettingSwitchPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected boolean persistBoolean(boolean value) {
        Settings.Global.putInt(getContext().getContentResolver(), getKey(), value ? 1 : 0);
        return true;
    }

    @Override
    protected boolean getPersistedBoolean(boolean defaultReturnValue) {
        return Settings.Global.getInt(getContext().getContentResolver(), getKey(),
                defaultReturnValue ? 1 : 0) == 1;
    }
}
