/*
 * Copyright (C) 2023-2024 The risingOS Android Project
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
package com.crdroid.settings.utils;

import android.content.Context;
import androidx.preference.Preference;
import com.android.internal.util.crdroid.Utils;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import com.android.settings.R;

public class PreferenceLayoutUtil {

    private static int extraPreferenceOrder = -151;

    private static final String PACKAGE_WELLBEING = "com.google.android.apps.wellbeing";
    private static final String PACKAGE_GOOGLE_SERVICES = "com.google.android.gms";

    private static final Set<String> topPreferences = new HashSet<>(Arrays.asList(
            "top_level_network",
            "top_level_apps",
            "top_level_accessibility",
            "top_level_emergency",
            "top_level_display"
    ));

    private static final Set<String> middlePreferences = new HashSet<>(Arrays.asList(
            "top_level_battery",
            "top_level_security",
            "top_level_privacy",
            "top_level_storage",
            "top_level_notifications",
            "top_level_communal",
            "top_level_safety_center",
            "top_level_accounts"
    ));

    private static final Set<String> bottomPreferences = new HashSet<>(Arrays.asList(
            "top_level_connected_devices",
            "top_level_sound",
            "top_level_wallpaper",
            "top_level_location",
            "top_level_system"
    ));
    
    private static final Set<String> EXCLUDE_LIST = new HashSet<>(Arrays.asList(
            "top_level_crdroid"
    ));

    public static void updateStartOrder(int startingOrder) {
        extraPreferenceOrder = startingOrder;
    }

    public static void setUpPreferenceLayout(Preference preference, Context context) {
        String key = preference.getKey();
        if (EXCLUDE_LIST.contains(key)) {
            return;
        }
        boolean isWellbeingInstalled = Utils.isPackageInstalled(context, PACKAGE_WELLBEING);
        boolean isGoogleServiceInstalled = Utils.isPackageInstalled(context, PACKAGE_GOOGLE_SERVICES);
        if ("top_level_wellbeing".equals(key) && isWellbeingInstalled) {
            preference.setLayoutResource(AdaptivePreferenceUtils.getLayoutResourceId(context, "wellbeing", true));
        } else if ("top_level_google".equals(key) && isGoogleServiceInstalled) {
            preference.setLayoutResource(AdaptivePreferenceUtils.getLayoutResourceId(context, "google", true));
        } else if (topPreferences.contains(key)) {
            preference.setLayoutResource(AdaptivePreferenceUtils.getLayoutResourceId(context, "top", true));
        } else if (middlePreferences.contains(key)) {
            preference.setLayoutResource(AdaptivePreferenceUtils.getLayoutResourceId(context, "middle", true));
        } else if (bottomPreferences.contains(key)) {
            preference.setLayoutResource(AdaptivePreferenceUtils.getLayoutResourceId(context, "bottom", true));
        } else if (key.equals("top_level_about_device")) {
            preference.setLayoutResource(R.layout.top_level_preference_about);
        } else {
            // highlight injected top level preference e.g OEM parts
            int order = extraPreferenceOrder - 1;
            updateStartOrder(order);
            preference.setOrder(order);
            preference.setLayoutResource(AdaptivePreferenceUtils.getLayoutResourceId(context, "solo", true));
        }
        if (key.equals("top_level_display")) {
            preference.setOrder(-150);
        } else if (key.equals("top_level_wallpaper")) {
            preference.setOrder(-140);
        }
    }
}
