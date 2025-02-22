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
import android.net.Uri;
import android.os.Bundle;
import android.os.UserHandle;
import android.provider.MediaStore;
import android.provider.Settings;

import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceScreen;
import androidx.preference.Preference.OnPreferenceChangeListener;
import androidx.preference.SwitchPreferenceCompat;

import com.android.internal.logging.nano.MetricsProto;

import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settingslib.search.SearchIndexable;

import com.crdroid.settings.fragments.quicksettings.QsHeaderImageSettings;
import com.crdroid.settings.preferences.CustomSeekBarPreference;

import lineageos.providers.LineageSettings;

import com.android.internal.util.rising.SystemRestartUtils;
import com.android.internal.util.crdroid.systemUtils;
import com.android.internal.util.crdroid.ThemeUtils;

import com.crdroid.settings.utils.ResourceUtils;
import com.crdroid.settings.utils.ImageUtils;

import java.util.List;
import java.util.ArrayList;

@SearchIndexable
public class QuickSettings extends SettingsPreferenceFragment implements
        Preference.OnPreferenceChangeListener {

    public static final String TAG = "QuickSettings";
    
    private static final int CUSTOM_IMAGE_REQUEST_CODE = 1001;

    private static final String KEY_SHOW_BRIGHTNESS_SLIDER = "qs_show_brightness_slider";
    private static final String KEY_BRIGHTNESS_SLIDER_POSITION = "qs_brightness_slider_position";
    private static final String KEY_SHOW_AUTO_BRIGHTNESS = "qs_show_auto_brightness";
    private static final String KEY_PREF_TILE_ANIM_STYLE = "qs_tile_animation_style";
    private static final String KEY_PREF_TILE_ANIM_DURATION = "qs_tile_animation_duration";
    private static final String KEY_PREF_TILE_ANIM_INTERPOLATOR = "qs_tile_animation_interpolator";
    private static final String KEY_QS_COMPACT_PLAYER  = "qs_compact_media_player_mode";
    private static final String KEY_QS_SPLIT_SHADE = "qs_split_shade";
    private static final String KEY_QS_WIDGETS_ENABLED  = "qs_widgets_enabled";
    private static final String KEY_QS_WIDGETS_PHOTO_IMG  = "qs_widgets_photo_showcase_image";

    private static final String QS_SPLIT_SHADE_LAYOUT_CTG = "android.theme.customization.qs_landscape_layout";
    private static final String QS_SPLIT_SHADE_LAYOUT_PKG = "com.android.systemui.qs.landscape.split_shade_layout";
    private static final String QS_SPLIT_SHADE_LAYOUT_TARGET = "com.android.systemui";

    private ListPreference mShowBrightnessSlider;
    private ListPreference mBrightnessSliderPosition;
    private SwitchPreferenceCompat mShowAutoBrightness;
    private ListPreference mTileAnimationStyle;
    private CustomSeekBarPreference mTileAnimationDuration;
    private ListPreference mTileAnimationInterpolator;
    private Preference mQsCompactPlayer;
    private SwitchPreferenceCompat mSplitShade;
    private Preference mQsWidgetsPref;
    private Preference mQsWidgetPhoto;

    private ThemeUtils mThemeUtils;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.crdroid_settings_quicksettings);

        final Context mContext = getActivity().getApplicationContext();
        final ContentResolver resolver = mContext.getContentResolver();
        final PreferenceScreen prefScreen = getPreferenceScreen();

        mThemeUtils = new ThemeUtils(mContext);

        mShowBrightnessSlider = findPreference(KEY_SHOW_BRIGHTNESS_SLIDER);
        mShowBrightnessSlider.setOnPreferenceChangeListener(this);
        mQsWidgetsPref = findPreference(KEY_QS_WIDGETS_ENABLED);
        mQsWidgetsPref.setOnPreferenceChangeListener(this);
        mQsWidgetPhoto = findPreference(KEY_QS_WIDGETS_PHOTO_IMG);
        mQsWidgetPhoto.setOnPreferenceChangeListener(this);
        boolean showSlider = LineageSettings.Secure.getIntForUser(resolver,
                LineageSettings.Secure.QS_SHOW_BRIGHTNESS_SLIDER, 1, UserHandle.USER_CURRENT) > 0;

        mBrightnessSliderPosition = findPreference(KEY_BRIGHTNESS_SLIDER_POSITION);
        mBrightnessSliderPosition.setEnabled(showSlider);

        mShowAutoBrightness = findPreference(KEY_SHOW_AUTO_BRIGHTNESS);
        boolean automaticAvailable = mContext.getResources().getBoolean(
                com.android.internal.R.bool.config_automatic_brightness_available);
        if (automaticAvailable) {
            mShowAutoBrightness.setEnabled(showSlider);
        } else {
            prefScreen.removePreference(mShowAutoBrightness);
        }
        mTileAnimationStyle = (ListPreference) findPreference(KEY_PREF_TILE_ANIM_STYLE);
        mTileAnimationDuration = (CustomSeekBarPreference) findPreference(KEY_PREF_TILE_ANIM_DURATION);
        mTileAnimationInterpolator = (ListPreference) findPreference(KEY_PREF_TILE_ANIM_INTERPOLATOR);

        mTileAnimationStyle.setOnPreferenceChangeListener(this);

        int tileAnimationStyle = Settings.System.getIntForUser(resolver,
                Settings.System.QS_TILE_ANIMATION_STYLE, 0, UserHandle.USER_CURRENT);
        updateAnimTileStyle(tileAnimationStyle);

        mQsCompactPlayer = (Preference) findPreference(KEY_QS_COMPACT_PLAYER);
        mQsCompactPlayer.setOnPreferenceChangeListener(this);

        mSplitShade = findPreference(KEY_QS_SPLIT_SHADE);
        boolean ssEnabled = isSplitShadeEnabled();
        mSplitShade.setChecked(ssEnabled);
        mSplitShade.setOnPreferenceChangeListener(this);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        ContentResolver resolver = getActivity().getContentResolver();

        if (preference == mShowBrightnessSlider) {
            int value = Integer.parseInt((String) newValue);
            mBrightnessSliderPosition.setEnabled(value > 0);
            if (mShowAutoBrightness != null)
                mShowAutoBrightness.setEnabled(value > 0);
            return true;
        } else if (preference == mTileAnimationStyle) {
            int value = Integer.parseInt((String) newValue);
            updateAnimTileStyle(value);
            return true;
        } else if (preference == mQsCompactPlayer) {
            systemUtils.showSystemUIRestartDialog(getActivity());
            return true;
        } else if (preference == mSplitShade) {
            updateSplitShadeState(((Boolean) newValue).booleanValue());
            return true;
        } else if (preference == mQsWidgetsPref) {
            LineageSettings.Secure.putIntForUser(resolver,
                    LineageSettings.Secure.QS_SHOW_BRIGHTNESS_SLIDER, 0, UserHandle.USER_CURRENT);
            SystemRestartUtils.showSystemUIRestartDialog(getActivity());
            return true;
        }
        return false;
    }

    private boolean isSplitShadeEnabled() {
        return mThemeUtils.isOverlayEnabled(QS_SPLIT_SHADE_LAYOUT_PKG);
    }

    private void updateSplitShadeState(boolean enable) {

        mThemeUtils.setOverlayEnabled(
                QS_SPLIT_SHADE_LAYOUT_CTG,
                enable ? QS_SPLIT_SHADE_LAYOUT_PKG : QS_SPLIT_SHADE_LAYOUT_TARGET,
                QS_SPLIT_SHADE_LAYOUT_TARGET);
    }

    public static void reset(Context mContext) {
        ContentResolver resolver = mContext.getContentResolver();
        Settings.System.putIntForUser(resolver,
                Settings.System.QS_BATTERY_STYLE, -1, UserHandle.USER_CURRENT);
        Settings.System.putIntForUser(resolver,
                Settings.System.QS_SHOW_BATTERY_PERCENT, 2, UserHandle.USER_CURRENT);
        Settings.System.putIntForUser(resolver,
                Settings.System.SECURE_LOCKSCREEN_QS_DISABLED, 0, UserHandle.USER_CURRENT);
//        Settings.System.putIntForUser(resolver,
  //              Settings.System.NOTIFICATION_MATERIAL_DISMISS, 0, UserHandle.USER_CURRENT);
        Settings.System.putIntForUser(resolver,
                Settings.System.QS_TRANSPARENCY, 100, UserHandle.USER_CURRENT);
        Settings.System.putIntForUser(resolver,
                Settings.System.QS_TILE_ANIMATION_STYLE, 0, UserHandle.USER_CURRENT);
        Settings.System.putIntForUser(resolver,
                Settings.System.QS_TILE_ANIMATION_DURATION, 1, UserHandle.USER_CURRENT);
        Settings.System.putIntForUser(resolver,
                Settings.System.QS_TILE_ANIMATION_INTERPOLATOR, 0, UserHandle.USER_CURRENT);
        Settings.System.putIntForUser(resolver,
                Settings.System.QS_LAYOUT_COLUMNS_LANDSCAPE, 4, UserHandle.USER_CURRENT);
        Settings.System.putIntForUser(resolver,
                Settings.System.QQS_LAYOUT_ROWS, 2, UserHandle.USER_CURRENT);
        Settings.System.putIntForUser(resolver,
                Settings.System.QQS_LAYOUT_ROWS_LANDSCAPE, 1, UserHandle.USER_CURRENT);
        Settings.System.putIntForUser(resolver,
                Settings.System.QS_LAYOUT_COLUMNS, 2, UserHandle.USER_CURRENT);
        Settings.System.putIntForUser(resolver,
                Settings.System.QS_TILE_VERTICAL_LAYOUT, 0, UserHandle.USER_CURRENT);
        Settings.System.putIntForUser(resolver,
                Settings.System.QS_TILE_LABEL_HIDE, 0, UserHandle.USER_CURRENT);
        Settings.System.putIntForUser(resolver,
                Settings.System.QS_TILE_LABEL_SIZE, 14, UserHandle.USER_CURRENT);
        Settings.System.putIntForUser(resolver,
                Settings.System.QS_DUAL_TONE, 1, UserHandle.USER_CURRENT);
        Settings.System.putIntForUser(resolver,
                Settings.System.QS_SHOW_DATA_USAGE, 0, UserHandle.USER_CURRENT);
        LineageSettings.Secure.putIntForUser(resolver,
                LineageSettings.Secure.QS_SHOW_BRIGHTNESS_SLIDER, 1, UserHandle.USER_CURRENT);
        LineageSettings.Secure.putIntForUser(resolver,
                LineageSettings.Secure.QS_BRIGHTNESS_SLIDER_POSITION, 0, UserHandle.USER_CURRENT);
        LineageSettings.Secure.putIntForUser(resolver,
                LineageSettings.Secure.QS_SHOW_AUTO_BRIGHTNESS, 1, UserHandle.USER_CURRENT);
        QsHeaderImageSettings.reset(mContext);
        ResourceUtils.updateOverlay(mContext, QS_SPLIT_SHADE_LAYOUT_CTG, QS_SPLIT_SHADE_LAYOUT_TARGET,
                QS_SPLIT_SHADE_LAYOUT_TARGET);
    }

    private void updateAnimTileStyle(int tileAnimationStyle) {
        mTileAnimationDuration.setEnabled(tileAnimationStyle != 0);
        mTileAnimationInterpolator.setEnabled(tileAnimationStyle != 0);
    }
    
        @Override
    public boolean onPreferenceTreeClick(Preference preference) {
        if (preference == mQsWidgetPhoto) {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            intent.setType("image/*");
            startActivityForResult(intent, CUSTOM_IMAGE_REQUEST_CODE);
            return true;
        }
        return super.onPreferenceTreeClick(preference);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent result) {
        super.onActivityResult(requestCode, resultCode, result);
        if (requestCode == CUSTOM_IMAGE_REQUEST_CODE && resultCode == Activity.RESULT_OK && result != null) {
            Uri imgUri = result.getData();
            if (imgUri != null) {
                String savedImagePath = ImageUtils.saveImageToInternalStorage(getContext(), imgUri, "qs_widgets", "QS_WIDGETS_PHOTO_SHOWCASE");
                if (savedImagePath != null) {
                    ContentResolver resolver = getContext().getContentResolver();
                    Settings.System.putStringForUser(resolver, "qs_widgets_photo_showcase_path", savedImagePath, UserHandle.USER_CURRENT);
                    mQsWidgetPhoto.setSummary(savedImagePath);
                }
            }
        }
    }

    @Override
    public int getMetricsCategory() {
        return MetricsProto.MetricsEvent.CRDROID_SETTINGS;
    }

    /**
     * For search
     */
    public static final BaseSearchIndexProvider SEARCH_INDEX_DATA_PROVIDER =
            new BaseSearchIndexProvider(R.xml.crdroid_settings_quicksettings);
}
