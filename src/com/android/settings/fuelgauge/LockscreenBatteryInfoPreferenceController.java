package com.android.settings.fuelgauge;

import android.content.Context;
import android.provider.Settings;
import com.android.settings.core.TogglePreferenceController;

public class LockscreenBatteryInfoPreferenceController extends TogglePreferenceController {

    public LockscreenBatteryInfoPreferenceController(Context context, String key) {
        super(context, key);
    }

    @Override
    public int getAvailabilityStatus() {
        return AVAILABLE;
    }

    @Override
    public boolean isChecked() {
        return Settings.System.getInt(mContext.getContentResolver(),
                Settings.System.LOCKSCREEN_BATTERY_INFO, 1) == 1;
    }

    @Override
    public boolean setChecked(boolean isChecked) {
        Settings.System.putInt(mContext.getContentResolver(),
                Settings.System.LOCKSCREEN_BATTERY_INFO, isChecked ? 1 : 0);
        return true;
    }

    @Override
    public int getSliceHighlightMenuRes() {
        return NO_RES;
    }
}
