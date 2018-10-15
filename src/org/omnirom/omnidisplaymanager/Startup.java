/*
 * Copyright (C) 2018 The OmniROM Project
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 */
package org.omnirom.omnidisplaymanager;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.preference.PreferenceManager;
import android.provider.Settings;
import android.text.TextUtils;

public class Startup extends BroadcastReceiver {

    private static final String KEY_CONTRAST_VALUE = "contrast_value";
    private static final String KEY_HUE_VALUE = "hue_value";
    private static final String KEY_INTENSITY_VALUE = "intensity_value";
    private static final String KEY_SATURATION_VALUE = "saturation_value";

    private static final String KEY_COLOR_BALANCE = "color_balance";

    @Override
    public void onReceive(final Context context, final Intent bootintent) {
        DisplayManagement.init();
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);

        int[] currentValue = DisplayManagement.getPAParameters();
        int contrast = sharedPrefs.getString(KEY_CONTRAST_VALUE, "-1").equals("-1") ? currentValue[4] : Integer.parseInt(sharedPrefs.getString(KEY_CONTRAST_VALUE, "-1"));
        int hue = sharedPrefs.getString(KEY_HUE_VALUE, "-1").equals("-1") ? currentValue[1] : Integer.parseInt(sharedPrefs.getString(KEY_HUE_VALUE, "-1"));
        int intensity = sharedPrefs.getString(KEY_INTENSITY_VALUE, "-1").equals("-1") ? currentValue[3] : Integer.parseInt(sharedPrefs.getString(KEY_INTENSITY_VALUE, "-1"));
        int saturation = sharedPrefs.getString(KEY_SATURATION_VALUE, "-1").equals("-1") ? currentValue[2] : Integer.parseInt(sharedPrefs.getString(KEY_SATURATION_VALUE, "-1"));

        DisplayManagement.setPAParameters(0, currentValue[0], hue, saturation, intensity, contrast, currentValue[5]);

        int colorBalance = sharedPrefs.getString(KEY_COLOR_BALANCE, "-1").equals("-1") ? DisplayManagement.getColorBalance() : Integer.parseInt(sharedPrefs.getString(KEY_SATURATION_VALUE, "-1"));
        DisplayManagement.setColorBalance(colorBalance);

        DisplayManagement.setMode(DisplayManagement.getActiveMode());
    }
}
