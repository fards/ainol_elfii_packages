/*
 * Copyright (C) 2008 The Android Open Source Project
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

package com.android.settings;

import android.app.ActivityManagerNative;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.backup.IBackupManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.VerifierDeviceIdentity;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Parcel;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.StrictMode;
import android.os.SystemProperties;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.preference.Preference.OnPreferenceChangeListener;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.IWindowManager;
import android.os.SystemProperties;

import android.preference.PreferenceCategory;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import android.util.Log;
import java.util.StringTokenizer;

/*
 * Displays preferences for application developers.
 */
public class ModeSettings extends PreferenceFragment implements OnPreferenceChangeListener
{

	private static final String CPU_MODE_KEY = "cpu_mode";
	private ListPreference mCpuMode;
    private String [] mMaxfreqList = new String[3];
    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        addPreferencesFromResource(R.xml.mode_settings);

        mCpuMode = (ListPreference) findPreference(CPU_MODE_KEY);
        if (mCpuMode != null) {
            mCpuMode.setOnPreferenceChangeListener(this);
            initCpuModeValue();
        }  
    }


    @Override
    public void onResume() {
        super.onResume();
        updateCpuModeOptions();
    }

    private void initCpuModeValue() {
        //load the max freq for different mode
        String maxfreqstr = SystemProperties.get("ro.cpumode.maxfreq","1200000,1500000,800000");

        // Use StringTokenizer because it is faster than split.
        StringTokenizer tokenizer = new StringTokenizer(maxfreqstr,",");
        int i = 0;
        for(i=0;i<3;i++) {
            mMaxfreqList[i] = tokenizer.nextToken();
        }
        
        if (mCpuMode != null) {
            String cpuModeStr = readSysfs(CPU_MODE_PATH, "conservative");
            String maxFreqStr = readSysfs(CPU_MAX_FREQ_PATH, "1200000");
            int cpuMode = 0;

            if ((maxFreqStr.equals(mMaxfreqList[0])) && (cpuModeStr.equals("conservative")))
                cpuMode = 0;
            if ((maxFreqStr.equals(mMaxfreqList[1]))&&(cpuModeStr.equals("performance2")))
                cpuMode = 1;
            else if ((maxFreqStr.equals(mMaxfreqList[2]))&&(cpuModeStr.equals("conservative")))
                cpuMode = 2;

            Settings.System.putInt(getActivity().getContentResolver(), Settings.System.CPU_MODE,
                    cpuMode);
        }
    }

    private void updateCpuModeOptions() {
        if (mCpuMode != null) {
            mCpuMode.setValue(String.valueOf(Settings.System.getInt(getActivity().getContentResolver(),
                    Settings.System.CPU_MODE, 0)));
            mCpuMode.setSummary(mCpuMode.getEntry());
        }
    }

    private static final String CPU_MAX_FREQ_PATH = "/sys/devices/system/cpu/cpu0/cpufreq/scaling_max_freq";
    private static final String CPU_MODE_PATH = "/sys/devices/system/cpu/cpu0/cpufreq/scaling_governor";    
    private static final String CPU_SAMPLEING_RATE_PATH = "/sys/devices/system/cpu/cpufreq/performance2/sampling_rate";    
    private static final String PERFORMANCE_UP_THRESHOLD_PATH = "/sys/devices/system/cpu/cpufreq/performance2/up_threshold";    
    private static final String PERFORMANCE_DOWN_THRESHOLD_PATH = "/sys/devices/system/cpu/cpufreq/performance2/down_threshold";    
    private static final String CONSERVATIVE_UP_THRESHOLD_PATH = "/sys/devices/system/cpu/cpufreq/conservative/up_threshold";    
    private static final String CONSERVATIVE_DOWN_THRESHOLD_PATH = "/sys/devices/system/cpu/cpufreq/conservative/down_threshold"; 

    private static int writeSysfs(String path, String val) {
        if (!new File(path).exists()) {
            Log.e("DevelopmentSettings", "File not found: " + path);
            return 1; 
        }
        
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(path), 64);
            try {
                writer.write(val);
            } finally {
                writer.close();
            }    		
            return 0;
        		
        } catch (IOException e) { 
            Log.e("DevelopmentSettings", "IO Exception when write: " + path, e);
            return 1;
        }                 
    }

	 private static String readSysfs(String path, String defVal) {
        String val;
        if (!new File(path).exists()) {
            Log.e("DevelopmentSettings", "File not found: " + path);
            return defVal; 
        }        
        try {
            BufferedReader reader = new BufferedReader(new FileReader(path), 32);
            try {
                val = reader.readLine();  
            } finally {
                reader.close();
            }    		
            return (val == null)? defVal : val;   	
        		
        } catch (IOException e) { 
            Log.e("DevelopmentSettings", "IO Exception when read: " + path, e);
            return defVal;
        }    
    }


    private void writeCpuModeOptions(Object newValue) {
        int cpuMode = Integer.parseInt((String) newValue);
        
        Settings.System.putInt(getActivity().getContentResolver(), Settings.System.CPU_MODE,
                cpuMode);
      
        if (cpuMode == 1) { //performance
            writeSysfs(CPU_MODE_PATH, "performance2");
            writeSysfs(CPU_MAX_FREQ_PATH,mMaxfreqList[1]);
            writeSysfs(CPU_SAMPLEING_RATE_PATH, "100000");
            writeSysfs(PERFORMANCE_UP_THRESHOLD_PATH, "60");
            writeSysfs(PERFORMANCE_DOWN_THRESHOLD_PATH, "30");
        } else if (cpuMode == 2) { //power saving
            writeSysfs(CPU_MODE_PATH, "conservative");
            writeSysfs(CPU_MAX_FREQ_PATH,mMaxfreqList[2]);
            writeSysfs(CPU_SAMPLEING_RATE_PATH, "100000");
            writeSysfs(CONSERVATIVE_UP_THRESHOLD_PATH, "80");
            writeSysfs(CONSERVATIVE_DOWN_THRESHOLD_PATH, "55");
        } else {//normal
            writeSysfs(CPU_MODE_PATH, "conservative");   
            writeSysfs(CPU_MAX_FREQ_PATH,mMaxfreqList[0]);
            writeSysfs(CPU_SAMPLEING_RATE_PATH, "100000");
            writeSysfs(CONSERVATIVE_UP_THRESHOLD_PATH, "80");
            writeSysfs(CONSERVATIVE_DOWN_THRESHOLD_PATH, "50");
        }
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
	 if (preference == mCpuMode) {
            writeCpuModeOptions(newValue);
            updateCpuModeOptions();           
            return true;
        }
        return false;
    }


}
