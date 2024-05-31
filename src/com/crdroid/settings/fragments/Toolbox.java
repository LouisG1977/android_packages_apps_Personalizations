/*
 * Copyright (C) 2016-2022 crDroid Android Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.crdroid.settings.fragments;

import android.app.Activity;
import android.app.settings.SettingsEnums;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.provider.Settings;

import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceScreen;
import androidx.preference.Preference.OnPreferenceChangeListener;
import androidx.preference.SwitchPreference;

import com.android.internal.logging.nano.MetricsProto;
import com.android.internal.util.rising.SystemRestartUtils;
import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settingslib.search.SearchIndexable;

import java.util.List;
import java.util.ArrayList;

@SearchIndexable
public class Toolbox extends SettingsPreferenceFragment implements Preference.OnPreferenceChangeListener {

    public static final String TAG = "Toolbox";
    private static final String KEY_QUICKSWITCH_PREFERENCE = "quickswitch";
    private static final String SYS_GMS_SPOOF = "persist.sys.pixelprops.gms";
    private static final String SYS_PROP_OPTIONS = "persist.sys.pixelprops.all";

    private Preference mGmsSpoof;
    private Preference mPropOptions;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.crdroid_settings_misc);
        final boolean targetHasSingleLauncher = SystemProperties.getInt("persist.sys.target_has_single_launcher", 0) != 0;
        PreferenceScreen preferenceScreen = getPreferenceScreen();
        if (targetHasSingleLauncher) {
            Preference quickSwitchPreference = findPreference(KEY_QUICKSWITCH_PREFERENCE);
            if (quickSwitchPreference != null) {
                quickSwitchPreference.setEnabled(false);
                quickSwitchPreference.setSummary(R.string.quickswitch_not_supported);
            }
        }
        mGmsSpoof = (Preference) findPreference(SYS_GMS_SPOOF);
        mGmsSpoof.setOnPreferenceChangeListener(this);
        mPropOptions = (Preference) findPreference(SYS_PROP_OPTIONS);
        mPropOptions.setOnPreferenceChangeListener(this);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference == mGmsSpoof || preference == mPropOptions) {
            SystemRestartUtils.showSystemRestartDialog(getContext());
            return true;
        }
        return false;
    }

    @Override
    public int getMetricsCategory() {
        return MetricsProto.MetricsEvent.VIEW_UNKNOWN;
    }

    /**
     * For search
     */
    public static final BaseSearchIndexProvider SEARCH_INDEX_DATA_PROVIDER =
            new BaseSearchIndexProvider(R.xml.crdroid_settings_misc) {

                @Override
                public List<String> getNonIndexableKeys(Context context) {
                    List<String> keys = super.getNonIndexableKeys(context);

                    return keys;
                }
            };
}
