/*
 * Copyright (C) 2016 The OmniROM Project
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
package org.omnirom.omnidisplaymanager.preferences;

import android.content.ContentResolver;
import android.content.Context;
import android.content.res.Resources;
import android.content.SharedPreferences;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.preference.PreferenceViewHolder;
import android.database.ContentObserver;
import android.util.AttributeSet;
import android.view.View;
import android.widget.SeekBar;
import android.widget.Button;
import android.os.Bundle;
import android.util.Log;
import android.os.Vibrator;

import org.omnirom.omnidisplaymanager.DisplayManagement;
import org.omnirom.omnidisplaymanager.R;

import java.util.Arrays;

public class PictureAdjustmentPreference extends Preference implements
        SeekBar.OnSeekBarChangeListener {

    private SeekBar mSeekBar;
    private int mOldStrength;
    private int mMinValue;
    private int mMaxValue;
    private static final String KEY_CONTRAST_VALUE = "contrast_value";
    private static final String KEY_HUE_VALUE = "hue_value";
    private static final String KEY_INTENSITY_VALUE = "intensity_value";
    private static final String KEY_SATURATION_VALUE = "saturation_value";
    private PictureAdjustment currentAdjustment;

    public enum AdjustmentType {
        CONTRAST,
        HUE,
        INTENSITY,
        SATURATION,
        NONE
    }

    public PictureAdjustmentPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
        int[] ranges = DisplayManagement.getRangePAParameter();
        Log.e("ranges", Arrays.toString(ranges));
        if (ranges != null && ranges.length > 0) {
            mMinValue = ranges[currentAdjustment.minRangeIndex];
            mMaxValue = ranges[currentAdjustment.maxRangeIndex];
        } else {
            mMinValue = 0;
            mMaxValue = 0;
        }

        setLayoutResource(R.layout.preference_seek_bar);
    }

    private void init(AttributeSet attrs) {
        String ajustmentType = attrs.getAttributeValue(null, "adjustmentType");
        switch (ajustmentType) {
            case "contrast":
                currentAdjustment = new PictureAdjustment(6, 7, 4, KEY_CONTRAST_VALUE);
                break;
            case "hue":
                currentAdjustment = new PictureAdjustment(0, 1, 1, KEY_HUE_VALUE);
                break;
            case "intensity":
                currentAdjustment = new PictureAdjustment(4, 5, 3, KEY_INTENSITY_VALUE);
                break;
            case "saturation":
                currentAdjustment = new PictureAdjustment(2, 3, 2, KEY_SATURATION_VALUE);
                break;
        }
    }

    @Override
    public void onBindViewHolder(PreferenceViewHolder holder) {
        super.onBindViewHolder(holder);

        mOldStrength = getValue();
        mSeekBar = (SeekBar) holder.findViewById(R.id.seekbar);
        mSeekBar.setMax(mMaxValue - mMinValue);
        mSeekBar.setProgress(mOldStrength - mMinValue);
        mSeekBar.setOnSeekBarChangeListener(this);
    }

    public int getValue() {
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        return sharedPrefs.getString(currentAdjustment.preferenceKey, "-1").equals("-1") ? DisplayManagement.getPAParameters()[currentAdjustment.parameterIndex] : Integer.parseInt(sharedPrefs.getString(currentAdjustment.preferenceKey, "-1"));
    }

    private void setValue(String newValue) {
        int[] currentValue = DisplayManagement.getPAParameters();
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        int contrast = sharedPrefs.getString(KEY_CONTRAST_VALUE, "-1").equals("-1") ? currentValue[4] : Integer.parseInt(sharedPrefs.getString(KEY_CONTRAST_VALUE, "-1"));
        int hue = sharedPrefs.getString(KEY_HUE_VALUE, "-1").equals("-1") ? currentValue[1] : Integer.parseInt(sharedPrefs.getString(KEY_HUE_VALUE, "-1"));
        int intensity = sharedPrefs.getString(KEY_INTENSITY_VALUE, "-1").equals("-1") ? currentValue[3] : Integer.parseInt(sharedPrefs.getString(KEY_INTENSITY_VALUE, "-1"));
        int saturation = sharedPrefs.getString(KEY_SATURATION_VALUE, "-1").equals("-1") ? currentValue[2] : Integer.parseInt(sharedPrefs.getString(KEY_SATURATION_VALUE, "-1"));

        switch (currentAdjustment.preferenceKey) {
            case KEY_CONTRAST_VALUE:
                DisplayManagement.setPAParameters(0, currentValue[0], hue, saturation, intensity, Integer.parseInt(newValue), currentValue[5]);
                break;
            case KEY_HUE_VALUE:
                DisplayManagement.setPAParameters(0, currentValue[0], Integer.parseInt(newValue), saturation, intensity, contrast, currentValue[5]);
                break;
            case KEY_INTENSITY_VALUE:
                DisplayManagement.setPAParameters(0, currentValue[0], hue, saturation, Integer.parseInt(newValue), contrast, currentValue[5]);
                break;
            case KEY_SATURATION_VALUE:
                DisplayManagement.setPAParameters(0, currentValue[0], hue, Integer.parseInt(newValue), intensity, contrast, currentValue[5]);
                break;
        }

        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(getContext()).edit();
        editor.putString(currentAdjustment.preferenceKey, newValue);
        editor.commit();
    }

    public void onProgressChanged(SeekBar seekBar, int progress,
                                  boolean fromTouch) {
        setValue(String.valueOf(progress + mMinValue));
    }

    public void onStartTrackingTouch(SeekBar seekBar) {
        // NA
    }

    public void onStopTrackingTouch(SeekBar seekBar) {
        // NA
    }

    public class PictureAdjustment {
        public int minRangeIndex;
        public int maxRangeIndex;
        public int parameterIndex;
        public String preferenceKey;

        public PictureAdjustment(int min, int max, int param, String key) {
            minRangeIndex = min;
            maxRangeIndex = max;
            parameterIndex = param;
            preferenceKey = key;
        }
    }
}


