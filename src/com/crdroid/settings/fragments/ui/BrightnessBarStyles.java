/*
 * Copyright (C) 2023 crDroid Android Project
 * Copyright (C) 2024 AlphaDroid
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

import android.os.Bundle;
import com.crdroid.settings.fragments.AlphaStylesFragment;
import com.android.settings.R;

public class BrightnessBarStyles extends AlphaStylesFragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        setCategory("android.theme.customization.bb_style");
        String title = getContext().getString(R.string.theme_customization_brightness_bar_title);
        setTitle(title);
        super.onCreate(savedInstanceState);
    }
}
