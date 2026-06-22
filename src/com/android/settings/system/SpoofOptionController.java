package com.android.settings.system;

import android.content.Context;
import android.provider.Settings;

import androidx.preference.Preference;
import androidx.preference.PreferenceScreen;
import androidx.preference.TwoStatePreference;

import com.android.settings.core.TogglePreferenceController;

public class SpoofOptionController extends TogglePreferenceController {

    private static final String SETTINGS_KEY = "spoof_option_enabled";

    private Preference mKeyboxPref;
    private Preference mPifPref;

    public SpoofOptionController(Context context, String key) {
        super(context, key);
    }

    @Override
    public int getAvailabilityStatus() {
        return AVAILABLE;
    }

    @Override
    public void displayPreference(PreferenceScreen screen) {
        super.displayPreference(screen);
        mKeyboxPref = screen.findPreference("keybox_data_setting");
        mPifPref = screen.findPreference("pif_data_setting");
        updateDependentPreferences();
    }

    @Override
    public boolean isChecked() {
        return Settings.System.getInt(mContext.getContentResolver(),
                SETTINGS_KEY, 0) != 0;
    }

    @Override
    public boolean setChecked(boolean isChecked) {
        Settings.System.putInt(mContext.getContentResolver(),
                SETTINGS_KEY, isChecked ? 1 : 0);
        updateDependentPreferences();
        return true;
    }

    private void updateDependentPreferences() {
        boolean enabled = isChecked();
        if (mKeyboxPref != null) mKeyboxPref.setEnabled(enabled);
        if (mPifPref != null) mPifPref.setEnabled(enabled);
    }

    @Override
    public int getSliceHighlightMenuRes() {
        return NO_RES;
    }
}
