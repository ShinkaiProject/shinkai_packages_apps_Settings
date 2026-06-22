package com.android.settings.system;

import android.app.settings.SettingsEnums;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

import com.android.settings.R;
import com.android.settings.dashboard.DashboardFragment;
import com.android.settings.development.KeyboxDataPreference;
import com.android.settings.development.PifDataPreference;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settingslib.search.SearchIndexable;

@SearchIndexable
public class SpoofOptionFragment extends DashboardFragment {

    private static final String TAG = "SpoofOptionFragment";

    private ActivityResultLauncher<Intent> mKeyboxLauncher;
    private ActivityResultLauncher<Intent> mPifLauncher;

    private KeyboxDataPreference mKeyboxPref;
    private PifDataPreference mPifPref;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mKeyboxLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getData() != null) {
                        Uri uri = result.getData().getData();
                        if (mKeyboxPref != null) mKeyboxPref.handleFileSelected(uri);
                    }
                });

        mPifLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getData() != null) {
                        Uri uri = result.getData().getData();
                        if (mPifPref != null) mPifPref.handleFileSelected(uri);
                    }
                });
    }

    @Override
    public void onStart() {
        super.onStart();
        mKeyboxPref = findPreference("keybox_data_setting");
        mPifPref = findPreference("pif_data_setting");
        if (mKeyboxPref != null) mKeyboxPref.setFilePickerLauncher(mKeyboxLauncher);
        if (mPifPref != null) mPifPref.setFilePickerLauncher(mPifLauncher);
    }

    @Override
    public int getMetricsCategory() {
        return SettingsEnums.PAGE_UNKNOWN;
    }

    @Override
    protected String getLogTag() {
        return TAG;
    }

    @Override
    protected int getPreferenceScreenResId() {
        return R.xml.spoof_option_fragment;
    }

    public static final BaseSearchIndexProvider SEARCH_INDEX_DATA_PROVIDER =
            new BaseSearchIndexProvider(R.xml.spoof_option_fragment);
}
