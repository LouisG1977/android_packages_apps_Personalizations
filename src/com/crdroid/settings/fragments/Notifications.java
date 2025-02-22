/*
 * Copyright (C) 2016-2024 crDroid Android Project
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
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.UserHandle;
import android.provider.Settings;
import android.widget.Toast;

import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceScreen;
import androidx.preference.Preference.OnPreferenceChangeListener;
import androidx.preference.SwitchPreferenceCompat;

import com.android.internal.logging.nano.MetricsProto;
import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settingslib.search.SearchIndexable;

import com.android.internal.util.crdroid.Utils;

import com.crdroid.settings.fragments.notifications.IslandSettings;
import com.crdroid.settings.preferences.CustomSeekBarPreference;

import java.util.List;

@SearchIndexable
public class Notifications extends SettingsPreferenceFragment implements
        Preference.OnPreferenceChangeListener {

    public static final String TAG = "Notifications";

    private static final String ALERT_SLIDER_PREF = "alert_slider_notifications";
    private static final String LIGHT_BRIGHTNESS_CATEGORY = "light_brightness";
    private static final String BATTERY_LIGHTS_PREF = "battery_lights";
    private static final String NOTIFICATION_LIGHTS_PREF = "notification_lights";
    private static final String FLASHLIGHT_CATEGORY = "flashlight_category";
    private static final String FLASHLIGHT_CALL_PREF = "flashlight_on_call";
    private static final String FLASHLIGHT_DND_PREF = "flashlight_on_call_ignore_dnd";
    private static final String FLASHLIGHT_RATE_PREF = "flashlight_on_call_rate";
    private static final String HEADS_UP_TIMEOUT_PREF = "heads_up_timeout";
    private static final String NOTIF_PANEL_MAX_NOTIF_CONFIG = "notif_panel_max_notif_cofig";
    private static final String KEY_TOAST_ANIMATION = "toast_animation";

    private Context mContext;
    private Preference mAlertSlider;
    private Preference mBatLights;
    private Preference mNotLights;

    private ListPreference mToastAnimation;
    private ListPreference mFlashOnCall;
    private SwitchPreferenceCompat mFlashOnCallIgnoreDND;
    private CustomSeekBarPreference mFlashOnCallRate;
    private CustomSeekBarPreference mHeadsUpTimeOut;
    private CustomSeekBarPreference mMaxNotifPanelNotifConfig;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.crdroid_settings_notifications);

        mContext = getActivity().getApplicationContext();
        final PreferenceScreen prefScreen = getPreferenceScreen();
        final ContentResolver resolver = mContext.getContentResolver();
        final Resources res = mContext.getResources();

        mAlertSlider = (Preference) prefScreen.findPreference(ALERT_SLIDER_PREF);
        boolean mAlertSliderAvailable = res.getBoolean(
                com.android.internal.R.bool.config_hasAlertSlider);
        if (!mAlertSliderAvailable)
            prefScreen.removePreference(mAlertSlider);

        mMaxNotifPanelNotifConfig = (CustomSeekBarPreference) findPreference(NOTIF_PANEL_MAX_NOTIF_CONFIG);
        int nPconf = Settings.System.getInt(getContentResolver(),
                Settings.System.NOTIF_PANEL_MAX_NOTIF_CONFIG, 3);
        mMaxNotifPanelNotifConfig.setValue(nPconf);
        mMaxNotifPanelNotifConfig.setOnPreferenceChangeListener(this);

        mHeadsUpTimeOut = (CustomSeekBarPreference)
                            prefScreen.findPreference(HEADS_UP_TIMEOUT_PREF);
        mHeadsUpTimeOut.setDefaultValue(getDefaultDecay(mContext));

        mBatLights = (Preference) prefScreen.findPreference(BATTERY_LIGHTS_PREF);
        boolean mBatLightsSupported = res.getInteger(
                org.lineageos.platform.internal.R.integer.config_deviceLightCapabilities) >= 64;
        if (!mBatLightsSupported)
            prefScreen.removePreference(mBatLights);

        mNotLights = (Preference) prefScreen.findPreference(NOTIFICATION_LIGHTS_PREF);
        boolean mNotLightsSupported = res.getBoolean(
                com.android.internal.R.bool.config_intrusiveNotificationLed);
        if (!mNotLightsSupported)
            prefScreen.removePreference(mNotLights);

        if (!mBatLightsSupported && !mNotLightsSupported) {
            final PreferenceCategory lightsCategory =
                    (PreferenceCategory) prefScreen.findPreference(LIGHT_BRIGHTNESS_CATEGORY);
            prefScreen.removePreference(lightsCategory);
        }

        if (!Utils.deviceHasFlashlight(mContext)) {
            final PreferenceCategory flashlightCategory =
                    (PreferenceCategory) prefScreen.findPreference(FLASHLIGHT_CATEGORY);
            prefScreen.removePreference(flashlightCategory);
        } else {
            mFlashOnCall = (ListPreference)
                    prefScreen.findPreference(FLASHLIGHT_CALL_PREF);
            mFlashOnCall.setOnPreferenceChangeListener(this);

            mFlashOnCallIgnoreDND = (SwitchPreferenceCompat)
                    prefScreen.findPreference(FLASHLIGHT_DND_PREF);
            int value = Settings.System.getInt(resolver,
                    Settings.System.FLASHLIGHT_ON_CALL, 0);

            mFlashOnCallRate = (CustomSeekBarPreference)
                    prefScreen.findPreference(FLASHLIGHT_RATE_PREF);

            mFlashOnCallIgnoreDND.setEnabled(value > 1);
            mFlashOnCallRate.setEnabled(value > 0);
        }

        mToastAnimation = (ListPreference) findPreference(KEY_TOAST_ANIMATION);
        mToastAnimation.setSummary(mToastAnimation.getEntry());
        int CurrentToastAnimation = Settings.Global.getInt(resolver, Settings.Global.TOAST_ANIMATION, 1);
        mToastAnimation.setValueIndex(CurrentToastAnimation); //set to index of default value
        mToastAnimation.setSummary(mToastAnimation.getEntries()[CurrentToastAnimation]);
        mToastAnimation.setOnPreferenceChangeListener(this);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference == mFlashOnCall) {
            int value = Integer.parseInt((String) newValue);
            mFlashOnCallIgnoreDND.setEnabled(value > 1);
            mFlashOnCallRate.setEnabled(value > 0);
        } else if (preference == mMaxNotifPanelNotifConfig) {
            int nPconf = (Integer) newValue;
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.NOTIF_PANEL_MAX_NOTIF_CONFIG, nPconf);
            return true;
        } else if (preference == mToastAnimation) {
            int index = mToastAnimation.findIndexOfValue((String) newValue);
            Settings.Global.putString(getActivity().getContentResolver(),
                         Settings.Global.TOAST_ANIMATION, (String) newValue);
            mToastAnimation.setSummary(mToastAnimation.getEntries()[index]);
            Toast.makeText(mContext, "Testing Toast Style", Toast.LENGTH_SHORT).show();
            return true;
        }
        return false;
    }

    private static int getDefaultDecay(Context context) {
        int defaultHeadsUpTimeOut = 5;
        Resources systemUiResources;
        try {
            systemUiResources = context.getPackageManager().getResourcesForApplication("com.android.systemui");
            defaultHeadsUpTimeOut = systemUiResources.getInteger(systemUiResources.getIdentifier(
                    "com.android.systemui:integer/heads_up_notification_decay", null, null)) / 1000;
        } catch (Exception e) {
        }
        return defaultHeadsUpTimeOut;
    }

    public static void reset(Context mContext) {
        ContentResolver resolver = mContext.getContentResolver();
        Settings.Global.putInt(resolver,
                Settings.Global.HEADS_UP_NOTIFICATIONS_ENABLED, 1);
        Settings.System.putIntForUser(resolver,
                Settings.System.LESS_BORING_HEADS_UP, 0, UserHandle.USER_CURRENT);
        Settings.System.putIntForUser(resolver,
                Settings.System.NOTIFICATION_SOUND_VIB_SCREEN_ON, 1, UserHandle.USER_CURRENT);
        Settings.System.putIntForUser(resolver,
                Settings.System.FLASHLIGHT_ON_CALL, 0, UserHandle.USER_CURRENT);
        Settings.System.putIntForUser(resolver,
                Settings.System.FLASHLIGHT_ON_CALL_IGNORE_DND, 0, UserHandle.USER_CURRENT);
        Settings.System.putIntForUser(resolver,
                Settings.System.FLASHLIGHT_ON_CALL_RATE, 1, UserHandle.USER_CURRENT);
        Settings.System.putIntForUser(resolver,
                Settings.System.ALERT_SLIDER_NOTIFICATIONS, 1, UserHandle.USER_CURRENT);
        Settings.System.putIntForUser(resolver,
                Settings.System.NOTIFICATION_GUTS_KILL_APP_BUTTON, 0, UserHandle.USER_CURRENT);
        Settings.System.putIntForUser(resolver,
                Settings.System.HEADS_UP_TIMEOUT, getDefaultDecay(mContext), UserHandle.USER_CURRENT);
        Settings.System.putIntForUser(resolver,
                Settings.System.NOTIF_PANEL_CUSTOM_NOTIF, 0, UserHandle.USER_CURRENT);
        Settings.System.putIntForUser(resolver,
                Settings.System.NOTIF_PANEL_MAX_NOTIF_CONFIG, 3, UserHandle.USER_CURRENT);
        IslandSettings.reset(mContext);
    }

    @Override
    public int getMetricsCategory() {
        return MetricsProto.MetricsEvent.CRDROID_SETTINGS;
    }

    /**
     * For search
     */
    public static final BaseSearchIndexProvider SEARCH_INDEX_DATA_PROVIDER =
            new BaseSearchIndexProvider(R.xml.crdroid_settings_notifications) {

                @Override
                public List<String> getNonIndexableKeys(Context context) {
                    List<String> keys = super.getNonIndexableKeys(context);
                    final Resources res = context.getResources();

                    boolean mAlertSliderAvailable = res.getBoolean(
                            com.android.internal.R.bool.config_hasAlertSlider);
                    if (!mAlertSliderAvailable)
                        keys.add(ALERT_SLIDER_PREF);

                    boolean mBatLightsSupported = res.getInteger(
                            org.lineageos.platform.internal.R.integer.config_deviceLightCapabilities) >= 64;
                    if (!mBatLightsSupported)
                        keys.add(BATTERY_LIGHTS_PREF);

                    boolean mNotLightsSupported = res.getBoolean(
                            com.android.internal.R.bool.config_intrusiveNotificationLed);
                    if (!mNotLightsSupported)
                        keys.add(NOTIFICATION_LIGHTS_PREF);

                    if (!Utils.deviceHasFlashlight(context)) {
                        keys.add(FLASHLIGHT_CALL_PREF);
                        keys.add(FLASHLIGHT_DND_PREF);
                        keys.add(FLASHLIGHT_RATE_PREF);
                    }

                    return keys;
                }
            };
}
