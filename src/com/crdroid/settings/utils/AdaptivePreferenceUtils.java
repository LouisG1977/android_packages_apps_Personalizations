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
import android.content.res.TypedArray;
import android.os.Handler;
import android.provider.Settings;
import android.util.AttributeSet;
import com.android.settings.R;
import android.widget.Toast;

import com.android.internal.util.crdroid.ThemeUtils;

public class AdaptivePreferenceUtils {

    private static final String overlayThemeTarget  = "com.android.systemui";

    public static void refreshTheme(Context context) {
        final ThemeUtils themeUtils = new ThemeUtils(context);
        Toast.makeText(context, context.getString(R.string.reevaluating_theme), Toast.LENGTH_SHORT).show();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                themeUtils.setOverlayEnabled("android.theme.customization.sysui_reevaluate", overlayThemeTarget, overlayThemeTarget);
                themeUtils.setOverlayEnabled("android.theme.customization.sysui_reevaluate", "com.android.system.qs.sysui_reevaluate", overlayThemeTarget);
            }
        }, Toast.LENGTH_SHORT + 500L);
    }

    public static String getPosition(Context context, AttributeSet attrs) {
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.AdaptivePreference);
        String positionAttribute = typedArray.getString(R.styleable.AdaptivePreference_position);
        typedArray.recycle();

        return positionAttribute;
    }
    
    public static boolean isLineageSettings(Context context, AttributeSet attrs) {
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.AdaptivePreference);
        boolean isLineage = typedArray.getBoolean(R.styleable.AdaptivePreference_isLineageSettings, false);
        typedArray.recycle();

        return isLineage;
    }

    public static int getLayoutResourceId(Context context, AttributeSet attrs) {
        final String positionString = getPosition(context, attrs);
        return getLayoutResourceId(context, positionString, false);
    }
    
    public static int getSettingsTheme(Context context) {
        return Settings.System.getInt(context.getContentResolver(), "settings_theme_style", 0);
    }
    
    public static int getLayoutResourceId(Context context, String positionString, boolean isHomePage) {
        int settingsTheme = getSettingsTheme(context);
        return getLayoutResourceId(context, settingsTheme, positionString, isHomePage);
    }
    
    private static String getLayoutIdentifier(int settingsTheme) {
        String[] layoutId = {"card", "ayan", "card_material"};
        return layoutId[settingsTheme];
    }
    
    private static String getCustomLayoutIdentifier(int settingsTheme) {
        return settingsTheme > 1 ? "_mt" : "";
    }
    
    public static int getLayoutResourceId(Context context, int settingsTheme, String positionString, boolean isHomePage) {
        final Position position = Position.fromAttribute(positionString);
        String layout = getLayoutIdentifier(settingsTheme);
        if (positionString != null && positionString.equals("wellbeing")) {
            return context.getResources().getIdentifier("top_level_preference_wellbeing_" + layout, "layout", "com.android.settings");
        } else if (positionString != null && positionString.equals("google")) {
            return context.getResources().getIdentifier("top_level_preference_google_" + layout, "layout", "com.android.settings");
        }
        if (position == null) {
            return context.getResources().getIdentifier("top_level_preference_middle_" + layout, "layout", "com.android.settings");
        }
        switch (position) {
            case TOP:
                return context.getResources().getIdentifier("top_level_preference_top_" + layout, "layout", "com.android.settings");
            case BOTTOM:
                return context.getResources().getIdentifier("top_level_preference_bottom_" + layout, "layout", "com.android.settings");
            case MIDDLE:
                return context.getResources().getIdentifier("top_level_preference_middle_" + layout, "layout", "com.android.settings");
            default:
            case SOLO:
                return R.layout.top_level_preference_solo_card;
            case NONE:
                return -1;
        }
    }

    public static int getSeekBarLayoutResourceId(Context context, AttributeSet attrs) {
        int settingsTheme = getSettingsTheme(context);
        final String positionString = getPosition(context, attrs);
        final Position position = Position.fromAttribute(positionString);
        String layout = getCustomLayoutIdentifier(settingsTheme);
        if (position == null) {
            return context.getResources().getIdentifier("preference_custom_seekbar_middle" + layout, "layout", "com.android.settings");
        }
        switch (position) {
            case TOP:
                return context.getResources().getIdentifier("preference_custom_seekbar_top" + layout, "layout", "com.android.settings");
            case BOTTOM:
                return context.getResources().getIdentifier("preference_custom_seekbar_bottom" + layout, "layout", "com.android.settings");
            case MIDDLE:
                return context.getResources().getIdentifier("preference_custom_seekbar_middle" + layout, "layout", "com.android.settings");
            case SOLO:
                return R.layout.preference_custom_seekbar_solo;
            case NONE:
                return -1;
            default:
                return R.layout.preference_custom_seekbar_solo;
        }
    }
    
    public static int getComposeLayoutResourceId(Context context, AttributeSet attrs) {
        int settingsTheme = getSettingsTheme(context);
        final String positionString = getPosition(context, attrs);
        final Position position = Position.fromAttribute(positionString);
        String layout = getCustomLayoutIdentifier(settingsTheme);
        if (position == null) {
            return context.getResources().getIdentifier("preference_compose", "layout", "com.android.settings");
        }
        switch (position) {
            case TOP:
                return context.getResources().getIdentifier("preference_compose_custom_top" + layout, "layout", "com.android.settings");
            case BOTTOM:
                return context.getResources().getIdentifier("preference_compose_custom_bottom" + layout, "layout", "com.android.settings");
            case MIDDLE:
                return context.getResources().getIdentifier("preference_compose_custom_middle" + layout, "layout", "com.android.settings");
            case SOLO:
            default:
                return context.getResources().getIdentifier("preference_compose", "layout", "com.android.settings");
            case NONE:
                return -1;
        }
    }

    public enum Position {
        TOP,
        MIDDLE,
        BOTTOM,
        SOLO,
        NONE;

        public static Position fromAttribute(String attribute) {
            if (attribute != null) {
                switch (attribute.toLowerCase()) {
                    case "top":
                        return TOP;
                    case "bottom":
                        return BOTTOM;
                    case "middle":
                        return MIDDLE;
                    case "solo":
                        return SOLO;
                    case "none":
                        return NONE;
                    default:
                        return null;
                }
            }
            return null;
        }
    }
}
