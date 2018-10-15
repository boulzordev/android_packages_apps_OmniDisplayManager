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

import android.content.ContentResolver;
import android.content.Context;
import android.content.res.Resources;
import android.opengl.Matrix;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Parcel;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceScreen;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import android.support.v14.preference.PreferenceFragment;
import android.support.v14.preference.SwitchPreference;
import android.os.ServiceManager;
import android.provider.Settings;
import android.provider.SearchIndexableResource;
import android.util.SparseArray;
import android.util.Slog;

import com.android.internal.logging.nano.MetricsProto.MetricsEvent;
import com.android.internal.util.omni.DeviceUtils;

import com.qti.snapdragon.sdk.display.ModeInfo;

import java.util.List;
import java.util.Arrays;

public class ColorSettings extends PreferenceFragment implements Preference.OnPreferenceChangeListener {
    private static final String TAG = "DisplaySettings";
    private ListPreference mDisplayMode;
    private ListPreference mAdaptiveBacklightMode;
    private SwitchPreference mReadingMode;

    private static final String PREF_DISPLAY_MODE = "display_management_mode";
    private static final String PREF_COLOR_BALANCE = "color_balance";
    private static final String PREF_SVI = "sunlight_enhancement";
    private static final String PREF_ADAPTIVE_BACKLIGHT = "adaptive_backlight";
    private static final String PREF_READING_MODE = "reading_mode";

    private static final int LEVEL_COLOR_MATRIX_READING = 201;

    private static final SparseArray<float[]> mColorMatrix = new SparseArray<>(3);
    private static final int SURFACE_FLINGER_TRANSACTION_COLOR_MATRIX = 1015;
    private static final float[][] mTempColorMatrix = new float[2][16];

    /**
     * Matrix and offset used for converting color to grayscale.
     * Copied from com.android.server.accessibility.DisplayAdjustmentUtils.MATRIX_GRAYSCALE
     */
    private static final float[] MATRIX_GRAYSCALE = new float[] {
            .2126f, .2126f, .2126f, 0,
            .7152f, .7152f, .7152f, 0,
            .0722f, .0722f, .0722f, 0,
            0,      0,      0, 1
    };

    /** Full color matrix and offset */
    private static final float[] MATRIX_NORMAL = new float[] {
            1, 0, 0, 0,
            0, 1, 0, 0,
            0, 0, 1, 0,
            0, 0, 0, 1
    };

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {

        ensureDisplayManagementUp();

        setPreferencesFromResource(R.xml.display_management_settings, rootKey);
        if (!DisplayManagement.isFeatureSupported(DisplayManagement.FEATURE_COLOR_BALANCE)) {
            Preference pref = getPreferenceScreen().findPreference(PREF_COLOR_BALANCE);
            if (pref != null) {
                getPreferenceScreen().removePreference(pref);
            }
        }

        if (!DisplayManagement.isFeatureSupported(DisplayManagement.FEATURE_SUNLIGHT_VISBILITY_IMPROVEMENT)) {
            Preference pref = getPreferenceScreen().findPreference(PREF_SVI);
            if (pref != null) {
                getPreferenceScreen().removePreference(pref);
            }
        }

        if (!DisplayManagement.isFeatureSupported(DisplayManagement.FEATURE_ADAPTIVE_BACKLIGHT)) {
            Preference pref = getPreferenceScreen().findPreference(PREF_ADAPTIVE_BACKLIGHT);
            if (pref != null) {
                getPreferenceScreen().removePreference(pref);
            }
        } else {
            mAdaptiveBacklightMode = (ListPreference) findPreference(PREF_ADAPTIVE_BACKLIGHT);
            mAdaptiveBacklightMode.setOnPreferenceChangeListener(this);
        }

        if (!DisplayManagement.isFeatureSupported(DisplayManagement.FEATURE_COLOR_MODE_SELECTION)) {
            Preference pref = getPreferenceScreen().findPreference(PREF_SVI);
            if (pref != null) {
                getPreferenceScreen().removePreference(pref);
            }
        } else {
            mDisplayMode = (ListPreference) findPreference(PREF_DISPLAY_MODE);
            mDisplayMode.setOnPreferenceChangeListener(this);

            ModeInfo[] modes = DisplayManagement.getModes();
            if (modes != null) {
                CharSequence[] entries = new CharSequence[modes.length];
                CharSequence[] entryValues = new CharSequence[modes.length];
                for (int i = 0; i < modes.length; i++) {
                    entries[i] = modes[i].getName();
                    entryValues[i] = String.valueOf(modes[i].getId());
                }

                mDisplayMode.setEntries(entries);
                mDisplayMode.setEntryValues(entryValues);
                mDisplayMode.setValueIndex(DisplayManagement.getActiveMode());
            }
        }

        mReadingMode = (SwitchPreference) findPreference(PREF_READING_MODE);
    }

    private void ensureDisplayManagementUp() {
        if (DisplayManagement.mColorService == null) {
            DisplayManagement.init();
        }
    }

    @Override
    public boolean onPreferenceTreeClick(Preference preference) {
        if (preference == mReadingMode) {
            boolean checked = ((SwitchPreference)preference).isChecked();
            setReadingMode(checked);
            return true;
        }
        return super.onPreferenceTreeClick(preference);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference == mDisplayMode) {
            int val = Integer.parseInt((String) newValue);
            int index = mDisplayMode.findIndexOfValue((String) newValue);
            DisplayManagement.setMode(index);
            mDisplayMode.setSummary(mDisplayMode.getEntries()[index]);
            return true;
        } else if (preference == mAdaptiveBacklightMode) {
            int val = Integer.parseInt((String) newValue);
            int index = mAdaptiveBacklightMode.findIndexOfValue((String) newValue);
            DisplayManagement.setBacklightQualityLevel(index);
            mAdaptiveBacklightMode.setSummary(mAdaptiveBacklightMode.getEntries()[index]);
        }
        return false;
    }

    public static void setReadingMode(boolean state) {
        setColorMatrix(LEVEL_COLOR_MATRIX_READING,
                state ? MATRIX_GRAYSCALE : MATRIX_NORMAL);
    }

    public static void setColorMatrix(int level, float[] value) {
        if (value != null && value.length != 16) {
            throw new IllegalArgumentException("Expected length: 16 (4x4 matrix)"
                    + ", actual length: " + value.length);
        }

        synchronized (mColorMatrix) {
            final float[] oldValue = mColorMatrix.get(level);
            if (!Arrays.equals(oldValue, value)) {
                if (value == null) {
                    mColorMatrix.remove(level);
                } else if (oldValue == null) {
                    mColorMatrix.put(level, Arrays.copyOf(value, value.length));
                } else {
                    System.arraycopy(value, 0, oldValue, 0, value.length);
                }

                // Update the current color transform.
                applyColorMatrix(computeColorMatrixLocked());
            }
        }
    }

    private static void applyColorMatrix(float[] m) {
        final IBinder flinger = ServiceManager.getService("SurfaceFlinger");
        if (flinger != null) {
            final Parcel data = Parcel.obtain();
            data.writeInterfaceToken("android.ui.ISurfaceComposer");
            if (m != null) {
                data.writeInt(1);
                for (int i = 0; i < 16; i++) {
                    data.writeFloat(m[i]);
                }
            } else {
                data.writeInt(0);
            }
            try {
                flinger.transact(SURFACE_FLINGER_TRANSACTION_COLOR_MATRIX, data, null, 0);
            } catch (RemoteException ex) {
                Slog.e(TAG, "Failed to set color transform", ex);
            } finally {
                data.recycle();
            }
        }
    }

    private static float[] computeColorMatrixLocked() {
        final int count = mColorMatrix.size();
        if (count == 0) {
            return null;
        }

        final float[][] result = mTempColorMatrix;
        Matrix.setIdentityM(result[0], 0);
        for (int i = 0; i < count; i++) {
            float[] rhs = mColorMatrix.valueAt(i);
            Matrix.multiplyMM(result[(i + 1) % 2], 0, result[i % 2], 0, rhs, 0);
        }
        return result[count % 2];
    }
}

