/*
 * Copyright (C) 2018-2024 crDroid Android Project
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

package com.crdroid.settings.fragments.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.UserHandle;
import android.provider.Settings;
import static android.provider.Settings.Secure.DOZE_ENABLED;

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

import com.crdroid.settings.fragments.ui.doze.EdgeLightSettings;
import com.crdroid.settings.fragments.ui.doze.Utils;
import com.crdroid.settings.preferences.SecureSettingSeekBarPreference;

import java.util.List;
import java.util.ArrayList;

@SearchIndexable
public class DozeSettings extends SettingsPreferenceFragment implements
        Preference.OnPreferenceChangeListener {

    public static final String TAG = "DozeSettings";

    private static final String KEY_DOZE_ENABLED = "doze_for_notifications";
    private static final String KEY_DOZE_ALWAYS_ON = "doze_always_on";
    private static final String KEY_DOZE_ALWAYS_ON_SCHEDULE = "always_on_display_schedule";

    private static final String CATEG_DOZE_SENSOR = "doze_sensor";

    private static final String KEY_DOZE_TILT_GESTURE = "doze_tilt_gesture";
    private static final String KEY_DOZE_PICK_UP_GESTURE = "doze_pick_up_gesture";
    private static final String KEY_DOZE_HANDWAVE_GESTURE = "doze_handwave_gesture";
    private static final String KEY_DOZE_POCKET_GESTURE = "doze_pocket_gesture";
    private static final String KEY_RAISE_TO_WAKE_GESTURE = "raise_to_wake_gesture";
    private static final String KEY_DOZE_GESTURE_VIBRATE = "doze_gesture_vibrate";

    private SwitchPreferenceCompat mDozeEnabledPreference;
    private SwitchPreferenceCompat mDozeAlwaysOnPreference;
    private SwitchPreferenceCompat mTiltPreference;
    private SwitchPreferenceCompat mPickUpPreference;
    private SwitchPreferenceCompat mHandwavePreference;
    private SwitchPreferenceCompat mPocketPreference;
    private SwitchPreferenceCompat mRaiseToWakePreference;
    private SecureSettingSeekBarPreference mDozeVibratePreference;

    private Preference mDozeAlwaysOnSchedulePreference;

    private SharedPreferences mPreferences;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.doze_settings);

        Context context = getContext();
        ContentResolver resolver = context.getContentResolver();

        PreferenceCategory dozeSensorCategory =
                (PreferenceCategory) getPreferenceScreen().findPreference(CATEG_DOZE_SENSOR);

        mDozeEnabledPreference = (SwitchPreferenceCompat) findPreference(KEY_DOZE_ENABLED);
        mDozeAlwaysOnPreference = (SwitchPreferenceCompat) findPreference(KEY_DOZE_ALWAYS_ON);
        mDozeAlwaysOnSchedulePreference = findPreference(KEY_DOZE_ALWAYS_ON_SCHEDULE);

        boolean dozeEnabledDefault = context.getResources().getBoolean(
                com.android.internal.R.bool.config_doze_enabled_by_default);
        boolean dozeEnabled = Settings.Secure.getIntForUser(resolver,
                Settings.Secure.DOZE_ENABLED,
                dozeEnabledDefault ? 1 : 0, UserHandle.USER_CURRENT) != 0;
        mDozeEnabledPreference.setChecked(dozeEnabled);

        mTiltPreference = (SwitchPreferenceCompat) findPreference(KEY_DOZE_TILT_GESTURE);
        mPickUpPreference = (SwitchPreferenceCompat) findPreference(KEY_DOZE_PICK_UP_GESTURE);
        mHandwavePreference = (SwitchPreferenceCompat) findPreference(KEY_DOZE_HANDWAVE_GESTURE);
        mPocketPreference = (SwitchPreferenceCompat) findPreference(KEY_DOZE_POCKET_GESTURE);
        mRaiseToWakePreference = (SwitchPreferenceCompat) findPreference(KEY_RAISE_TO_WAKE_GESTURE);
        mDozeVibratePreference = (SecureSettingSeekBarPreference) findPreference(KEY_DOZE_GESTURE_VIBRATE);

        // Hide sensor related features if the device doesn't support them
        if (!Utils.getTiltSensor(context) && !Utils.getPickupSensor(context)
                && !Utils.getProximitySensor(context)) {
            getPreferenceScreen().removePreference(dozeSensorCategory);
        } else {
            if (!Utils.getTiltSensor(context)) {
                dozeSensorCategory.removePreference(mTiltPreference);
            } else {
                mTiltPreference.setOnPreferenceChangeListener(this);
            }
            if (!Utils.getPickupSensor(context)) {
                dozeSensorCategory.removePreference(mPickUpPreference);
            } else {
                boolean pickupGestureDefault = context.getResources().getBoolean(
                        com.android.internal.R.bool.config_dozePickupGestureEnabled);
                boolean pickupGesture = Settings.Secure.getIntForUser(resolver,
                        Settings.Secure.DOZE_PICK_UP_GESTURE,
                        pickupGestureDefault ? 1 : 0, UserHandle.USER_CURRENT) != 0;
                mPickUpPreference.setChecked(pickupGesture);
                mPickUpPreference.setOnPreferenceChangeListener(this);
            }
            if (!Utils.getProximitySensor(context)) {
                dozeSensorCategory.removePreference(mHandwavePreference);
                dozeSensorCategory.removePreference(mPocketPreference);
            } else {
                mHandwavePreference.setOnPreferenceChangeListener(this);
                mPocketPreference.setOnPreferenceChangeListener(this);
            }
                boolean pickupGestureAmbientDefault = Settings.Secure.getIntForUser(resolver,
                            Settings.Secure.DOZE_PICK_UP_GESTURE_AMBIENT,
                            0, UserHandle.USER_CURRENT) != 0;
                boolean pickupGestureAmbient = Settings.Secure.getIntForUser(resolver,
                        Settings.Secure.DOZE_PICK_UP_GESTURE_AMBIENT,
                        pickupGestureAmbientDefault ? 1 : 0, UserHandle.USER_CURRENT) != 0;
            mRaiseToWakePreference.setChecked(!pickupGestureAmbient);
            mRaiseToWakePreference.setOnPreferenceChangeListener(this);
            checkService(context);
        }

        // Hides always on toggle if device doesn't support it (based on config_dozeAlwaysOnDisplayAvailable overlay)
        if (!Utils.isDozeAlwaysOnAvailable(context)) {
            getPreferenceScreen().removePreference(mDozeAlwaysOnPreference);
            getPreferenceScreen().removePreference(mDozeAlwaysOnSchedulePreference);
        } else {
            boolean dozeAlwaysOnDefault = context.getResources().getBoolean(
                    com.android.internal.R.bool.config_dozeAlwaysOnEnabled);
            boolean dozeAlwaysOn = Settings.Secure.getIntForUser(resolver,
                    Settings.Secure.DOZE_ALWAYS_ON,
                    dozeAlwaysOnDefault ? 1 : 0, UserHandle.USER_CURRENT) != 0;
            mDozeAlwaysOnPreference.setChecked(dozeAlwaysOn);
            mDozeAlwaysOnPreference.setOnPreferenceChangeListener(this);
        }
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        Context context = getContext();
        ContentResolver resolver = context.getContentResolver();

        if (preference == mDozeAlwaysOnPreference) {
            boolean value = (Boolean) newValue;
            Settings.Secure.putIntForUser(resolver, Settings.Secure.DOZE_ALWAYS_ON, 
                 value ? 1 : 0, UserHandle.USER_CURRENT);
            checkService(context);
            return true;
        } else if (preference == mTiltPreference) {
            boolean value = (Boolean) newValue;
            Settings.Secure.putIntForUser(resolver, Settings.Secure.DOZE_TILT_GESTURE, 
                 value ? 1 : 0, UserHandle.USER_CURRENT);
            checkService(context);
            return true;
        } else if (preference == mPickUpPreference) {
            boolean value = (Boolean) newValue;
            Settings.Secure.putIntForUser(resolver, Settings.Secure.DOZE_PICK_UP_GESTURE, 
                 value ? 1 : 0, UserHandle.USER_CURRENT);
            checkService(context);
            return true;
        } else if (preference == mHandwavePreference) {
            boolean value = (Boolean) newValue;
            Settings.Secure.putIntForUser(resolver, Settings.Secure.DOZE_HANDWAVE_GESTURE, 
                 value ? 1 : 0, UserHandle.USER_CURRENT);
            checkService(context);
            return true;
        } else if (preference == mPocketPreference) {
            boolean value = (Boolean) newValue;
            Settings.Secure.putIntForUser(resolver, Settings.Secure.DOZE_POCKET_GESTURE, 
                 value ? 1 : 0, UserHandle.USER_CURRENT);
            checkService(context);
            return true;
        } else if (preference == mRaiseToWakePreference) {
            boolean value = (Boolean) newValue;
            Settings.Secure.putIntForUser(resolver, Settings.Secure.RAISE_TO_WAKE_GESTURE, 
                 value ? 1 : 0, UserHandle.USER_CURRENT);

        // Retrieve the new value of RAISE_TO_WAKE_GESTURE
        int raiseToWakeGesture = Settings.Secure.getIntForUser(resolver, 
            Settings.Secure.RAISE_TO_WAKE_GESTURE, 0, UserHandle.USER_CURRENT);
        // Set DOZE_PICK_UP_GESTURE_AMBIENT to the opposite value
        int dozePickupGestureAmbient = raiseToWakeGesture == 1 ? 0 : 1;
        Settings.Secure.putIntForUser(resolver, Settings.Secure.DOZE_PICK_UP_GESTURE_AMBIENT, 
            dozePickupGestureAmbient, UserHandle.USER_CURRENT);

            checkService(context);
            return true;
        }
        return false;
    }

    private void checkService(Context context) {
        boolean serviceEnabled = Utils.enableService(context);
        boolean alwaysOnEnabled = Utils.isDozeAlwaysOnEnabled(context);
        boolean raiseToWakeEnabled = Utils.isRaiseToWakeEnabled(context);
        mRaiseToWakePreference.setEnabled(serviceEnabled);
        mDozeVibratePreference.setEnabled(serviceEnabled &&
                !raiseToWakeEnabled);
        if (mTiltPreference != null) {
            mTiltPreference.setEnabled(!alwaysOnEnabled);
        }
        if (mPickUpPreference != null) {
            mPickUpPreference.setEnabled(!alwaysOnEnabled);
        }
        if (mHandwavePreference != null) {
            mHandwavePreference.setEnabled(!alwaysOnEnabled);
        }
        if (mPocketPreference != null) {
            mPocketPreference.setEnabled(!alwaysOnEnabled);
        }
        if (serviceEnabled) {
            sensorWarning(context);
        }
    }

    private void sensorWarning(Context context) {
        mPreferences = context.getSharedPreferences("dozesettingsfragment", Activity.MODE_PRIVATE);
        if (mPreferences.getBoolean("sensor_warning_shown", false)) {
            return;
        }
        context.getSharedPreferences("dozesettingsfragment", Activity.MODE_PRIVATE)
                .edit()
                .putBoolean("sensor_warning_shown", true)
                .commit();

        new AlertDialog.Builder(context)
                .setTitle(getResources().getString(R.string.caution))
                .setMessage(getResources().getString(R.string.sensor_warning_message))
                .setNegativeButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                        }
                }).show();
    }

    public static void reset(Context mContext) {
        ContentResolver resolver = mContext.getContentResolver();
        Settings.Secure.putIntForUser(resolver,
                Settings.Secure.DOZE_ENABLED, mContext.getResources().getBoolean(
                com.android.internal.R.bool.config_doze_enabled_by_default) ? 1 : 0,
                UserHandle.USER_CURRENT);
        Settings.Secure.putIntForUser(resolver,
                Settings.Secure.DOZE_ALWAYS_ON, mContext.getResources().getBoolean(
                com.android.internal.R.bool.config_dozeAlwaysOnEnabled) ? 1 : 0,
                UserHandle.USER_CURRENT);
        Settings.Secure.putIntForUser(resolver,
                Settings.Secure.DOZE_ALWAYS_ON_AUTO_MODE, 0, UserHandle.USER_CURRENT);
        Settings.Secure.putIntForUser(resolver,
                Settings.Secure.DOZE_TILT_GESTURE, 0, UserHandle.USER_CURRENT);
        Settings.Secure.putIntForUser(resolver,
                Settings.Secure.DOZE_PICK_UP_GESTURE, mContext.getResources().getBoolean(
                com.android.internal.R.bool.config_dozePickupGestureEnabled) ? 1 : 0,
                UserHandle.USER_CURRENT);
        Settings.Secure.putIntForUser(resolver,
                Settings.Secure.DOZE_HANDWAVE_GESTURE, 0, UserHandle.USER_CURRENT);
        Settings.Secure.putIntForUser(resolver,
                Settings.Secure.DOZE_POCKET_GESTURE, 0, UserHandle.USER_CURRENT);
        Settings.Secure.putIntForUser(resolver,
                Settings.Secure.RAISE_TO_WAKE_GESTURE, 1, UserHandle.USER_CURRENT);
        Settings.Secure.putIntForUser(resolver,
                Settings.Secure.DOZE_GESTURE_VIBRATE, 0, UserHandle.USER_CURRENT);
        Settings.Secure.putIntForUser(resolver,
                Settings.Secure.DOZE_ON_CHARGE, 0, UserHandle.USER_CURRENT);
        EdgeLightSettings.reset(mContext);
    }

    @Override
    public int getMetricsCategory() {
        return MetricsProto.MetricsEvent.CRDROID_SETTINGS;
    }

    /**
     * For search
     */
    public static final BaseSearchIndexProvider SEARCH_INDEX_DATA_PROVIDER =
            new BaseSearchIndexProvider(R.xml.doze_settings) {

                @Override
                public List<String> getNonIndexableKeys(Context context) {
                    List<String> keys = super.getNonIndexableKeys(context);

                    if (!Utils.isDozeAlwaysOnAvailable(context)) {
                        keys.add(KEY_DOZE_ALWAYS_ON);
                        keys.add(KEY_DOZE_ALWAYS_ON_SCHEDULE);
                    }

                    keys.add(KEY_DOZE_GESTURE_VIBRATE);
                    if (!Utils.getTiltSensor(context)) {
                        keys.add(KEY_DOZE_TILT_GESTURE);
                    }
                    if (!Utils.getPickupSensor(context)) {
                        keys.add(KEY_DOZE_PICK_UP_GESTURE);
                    }
                    if (!Utils.getProximitySensor(context)) {
                        keys.add(KEY_DOZE_HANDWAVE_GESTURE);
                        keys.add(KEY_DOZE_POCKET_GESTURE);
                    }

                    return keys;
                }
            };
}
