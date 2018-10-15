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

import android.os.SystemProperties;
import android.util.Log;

import com.qti.service.colorservice.IColorServiceImpl;
import com.qti.snapdragon.sdk.display.ModeInfo;

import java.util.Arrays;

public class DisplayManagement {

    public static IColorServiceImpl mColorService;
    private static final String TAG = "DisplayManagement";
    public static boolean isDisplayManagementSupported;

    public static final int FEATURE_COLOR_BALANCE = 0;
    public static final int FEATURE_COLOR_MODE_SELECTION = 1;
    public static final int FEATURE_COLOR_MODE_MANAGEMENT = 2;
    public static final int FEATURE_ADAPTIVE_BACKLIGHT = 3;
    public static final int FEATURE_GLOBAL_PICTURE_ADJUSTMENT = 4;
    public static final int FEATURE_MEMORY_COLOR_ADJUSTMENT = 5;
    public static final int FEATURE_SUNLIGHT_VISBILITY_IMPROVEMENT = 6;

    public static void init(){
        try {
            mColorService = new IColorServiceImpl();
            mColorService.native_init();
            isDisplayManagementSupported = true;
        } catch (Throwable t) {
            // Ignore, DisplayEngineService not available.
        }
    }

    public static int getActiveMode() {
        int defaultMode = mColorService.native_getDefaultMode(0);
        return defaultMode;
    }

    public static ModeInfo[] getModes() {
        ModeInfo[] modes = mColorService.native_getModes(0, 2);
        return modes;
    }

    public static void setMode(int modeId) {
        if (mColorService == null) {
            init();
        }
        mColorService.native_setActiveMode(0, modeId);
        mColorService.native_setDefaultMode(0, modeId);
    }

    public static boolean isFeatureSupported(int featId) {
        int supported = mColorService.native_isFeatureSupported(0, featId);
        return supported > 0;

    }

    public static int getColorBalance() {
        int balanceValue = mColorService.native_getColorBalance(0);
        return balanceValue;
    }

    public static void setColorBalance(int newValue) {
        mColorService.native_setColorBalance(0, newValue);
    }

    public static int getRangeSunlightVisibilityStrength(int minMax) {
        return mColorService.native_setSVI(0, minMax);
    }

    public static void setSVI(int newValue) {
        mColorService.native_setSVI(0, newValue);
    }

    public static void setBacklightQualityLevel(int index) {
        mColorService.native_setBacklightQualityLevel(0, index);
    }

    public static int[] getRangePAParameter() {
        return mColorService.native_getRangePAParameter(0);
    }

    public static int[] getPAParameters() {
        return mColorService.native_getPAParameters(0);
    }

    public static void setPAParameters(int displayId, int flag, int hue, int saturation, int intensity, int contrast, int satThreshold){
        mColorService.native_setPAParameters(displayId, flag, hue, saturation, intensity, contrast, satThreshold);
    }
}
