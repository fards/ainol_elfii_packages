/*
 * Copyright (C) 2010 The Android Open Source Project
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

import static android.provider.Settings.System.SCREEN_OFF_TIMEOUT;

import android.app.ActivityManagerNative;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.ContentObserver;
import android.os.Bundle;
import android.os.Handler;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceScreen;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.util.Log;
import android.view.IWindowManager;
import android.view.Surface;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import android.content.Intent;
import android.os.SystemProperties;
import android.app.Activity;

public class DisplaySettings extends SettingsPreferenceFragment implements
        Preference.OnPreferenceChangeListener {
    private static final String TAG = "DisplaySettings";

    /** If there is output mode option, use this. */
	private static final String STR_OUTPUT_VAR="ubootenv.var.outputmode";
    private ListPreference  mOutputmode;
    private CharSequence[] mEntryValues;
    private int sel_index;
	private int index_entry;
	private static final int GET_USER_OPERATION=1;

	private static final String KEY_Hide_StatusBar = "statusbar_hide";
	private CheckBoxPreference mHideStatusBar;
	
    /** If there is no setting in the provider, use this. */
    private static final int FALLBACK_SCREEN_TIMEOUT_VALUE = 30000;

    private static final String KEY_SCREEN_TIMEOUT = "screen_timeout";
    private static final String KEY_ACCELEROMETER = "accelerometer";
    private static final String KEY_OUTPUTMODE = "output_mode";
    private static final String KEY_FONT_SIZE = "font_size";
    private static final String KEY_NOTIFICATION_PULSE = "notification_pulse";
    private static final String KEY_Brightness = "brightness";
    private static final String KEY_DISPLAY_POSITION = "display_position";
	private static final String STR_OUTPUT_MODE = "ubootenv.var.outputmode";

    private CheckBoxPreference mAccelerometer;
    private ListPreference mFontSizePref;
    private CheckBoxPreference mNotificationPulse;

    private final Configuration mCurConfig = new Configuration();
    
    private ListPreference mScreenTimeoutPreference;

    /** If there is display position option, use this. */    
    private static String VideoDisbaleFile= "/sys/class/video/disable_video";
	private static String FreeScaleOsd0File= "/sys/class/graphics/fb0/free_scale";
	private static String FreeScaleOsd1File= "/sys/class/graphics/fb1/free_scale";
	private static String VideoAxisFile= "/sys/class/video/axis";
	private static String TVMode= "/sys/class/display/mode";
	private static String PpscalerFile= "/sys/class/ppmgr/ppscaler";
	private final static String sel_480ioutput_x = "ubootenv.var.480ioutputx";
	private final static String sel_480ioutput_y = "ubootenv.var.480ioutputy";
	private final static String sel_480ioutput_width = "ubootenv.var.480ioutputwidth";
	private final static String sel_480ioutput_height = "ubootenv.var.480ioutputheight";
	private final static String sel_480poutput_x = "ubootenv.var.480poutputx";
	private final static String sel_480poutput_y = "ubootenv.var.480poutputy";
	private final static String sel_480poutput_width = "ubootenv.var.480poutputwidth";
	private final static String sel_480poutput_height = "ubootenv.var.480poutputheight";
	private final static String sel_576ioutput_x = "ubootenv.var.576ioutputx";
	private final static String sel_576ioutput_y = "ubootenv.var.576ioutputy";
	private final static String sel_576ioutput_width = "ubootenv.var.576ioutputwidth";
	private final static String sel_576ioutput_height = "ubootenv.var.576ioutputheight";
	private final static String sel_576poutput_x = "ubootenv.var.576poutputx";
	private final static String sel_576poutput_y = "ubootenv.var.576poutputy";
	private final static String sel_576poutput_width = "ubootenv.var.576poutputwidth";
	private final static String sel_576poutput_height = "ubootenv.var.576poutputheight";
	private final static String sel_720poutput_x = "ubootenv.var.720poutputx";
	private final static String sel_720poutput_y = "ubootenv.var.720poutputy";
	private final static String sel_720poutput_width = "ubootenv.var.720poutputwidth";
	private final static String sel_720poutput_height = "ubootenv.var.720poutputheight";
	private final static String sel_1080ioutput_x = "ubootenv.var.1080ioutputx";
	private final static String sel_1080ioutput_y = "ubootenv.var.1080ioutputy";
	private final static String sel_1080ioutput_width = "ubootenv.var.1080ioutputwidth";
	private final static String sel_1080ioutput_height = "ubootenv.var.1080ioutputheight";
	private final static String sel_1080poutput_x = "ubootenv.var.1080poutputx";
	private final static String sel_1080poutput_y = "ubootenv.var.1080poutputy";
	private final static String sel_1080poutput_width = "ubootenv.var.1080poutputwidth";
	private final static String sel_1080poutput_height = "ubootenv.var.1080poutputheight";
	private static final int OUTPUT480_FULL_WIDTH = 720;
	private static final int OUTPUT480_FULL_HEIGHT = 480;
	private static final int OUTPUT576_FULL_WIDTH = 720;
	private static final int OUTPUT576_FULL_HEIGHT = 576;
	private static final int OUTPUT720_FULL_WIDTH = 1280;
	private static final int OUTPUT720_FULL_HEIGHT = 720;
	private static final int OUTPUT1080_FULL_WIDTH = 1920;
	private static final int OUTPUT1080_FULL_HEIGHT = 1080;
    private Preference mDisplayposition;
    private int selectedItemPosition;
	String curOutputmode = SystemProperties.get(STR_OUTPUT_MODE);

	Handler requestFocusHandler=new Handler();
	Runnable requestFocusHandler_runnable=new Runnable(){
	@Override
	public void run() {
	// TODO Auto-generated method stub
		if((Utils.platformHas1080Scale() == 0) 
				|| ((Utils.platformHas1080Scale() == 1) && (!curOutputmode.equals("1080i")) && (!curOutputmode.equals("1080p")) && (!curOutputmode.equals("720p")))){
			setVideoDisable(0);
		}
		//getListView().setSelection(selectedItemPosition);
		selectedItemPosition = -1;
	}
	};
	
    private ContentObserver mAccelerometerRotationObserver = new ContentObserver(new Handler()) {
        @Override
        public void onChange(boolean selfChange) {
            updateAccelerometerRotationCheckbox();
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ContentResolver resolver = getActivity().getContentResolver();

        addPreferencesFromResource(R.xml.display_settings);

        mAccelerometer = (CheckBoxPreference) findPreference(KEY_ACCELEROMETER);
        mAccelerometer.setPersistent(false);

        mScreenTimeoutPreference = (ListPreference) findPreference(KEY_SCREEN_TIMEOUT);
        long currentTimeout = Settings.System.getLong(resolver, SCREEN_OFF_TIMEOUT,
                FALLBACK_SCREEN_TIMEOUT_VALUE);
        if(currentTimeout==-1)
        {	
        	currentTimeout=1;
      	}
        mScreenTimeoutPreference.setValue(String.valueOf(currentTimeout));
        mScreenTimeoutPreference.setOnPreferenceChangeListener(this);
        disableUnusableTimeouts(mScreenTimeoutPreference);
        updateTimeoutPreferenceDescription(currentTimeout);

        mFontSizePref = (ListPreference) findPreference(KEY_FONT_SIZE);
        mFontSizePref.setOnPreferenceChangeListener(this);
        mNotificationPulse = (CheckBoxPreference) findPreference(KEY_NOTIFICATION_PULSE);
        if (mNotificationPulse != null
                && getResources().getBoolean(
                        com.android.internal.R.bool.config_intrusiveNotificationLed) == false) {
            getPreferenceScreen().removePreference(mNotificationPulse);
        } else {
            try {
                mNotificationPulse.setChecked(Settings.System.getInt(resolver,
                        Settings.System.NOTIFICATION_LIGHT_PULSE) == 1);
                mNotificationPulse.setOnPreferenceChangeListener(this);
            } catch (SettingNotFoundException snfe) {
                Log.e(TAG, Settings.System.NOTIFICATION_LIGHT_PULSE + " not found");
            }
        }
				
				mHideStatusBar = (CheckBoxPreference)findPreference(KEY_Hide_StatusBar);        
     		mHideStatusBar.setOnPreferenceChangeListener(this);
				
        if(!Utils.platformHasScreenBrightness()){
        	getPreferenceScreen().removePreference(findPreference(KEY_Brightness));
        }

        if(!Utils.hwHasAccelerometer()){
        	getPreferenceScreen().removePreference(mAccelerometer);
        }
        
        if(!Utils.platformHasScreenTimeout()){
        	getPreferenceScreen().removePreference(mScreenTimeoutPreference);
        }

        if(!Utils.platformHasScreenFontSize()){
        	getPreferenceScreen().removePreference(mFontSizePref);
        }

		if (Utils.platformHasTvOutput()) {
            mOutputmode = (ListPreference) findPreference(KEY_OUTPUTMODE);
            mOutputmode.setOnPreferenceChangeListener(this);

			String valOutputmode = SystemProperties.get(STR_OUTPUT_VAR);
            mEntryValues = getResources().getStringArray(R.array.outputmode_entries);
            index_entry = findIndexOfEntry(valOutputmode, mEntryValues);
            mOutputmode.setValueIndex(index_entry);
        }
        else {
            getPreferenceScreen().removePreference(findPreference(KEY_OUTPUTMODE));

        }
        if (Utils.platformHasTvOutput()) {
	        mDisplayposition = findPreference(KEY_DISPLAY_POSITION);
        	mDisplayposition.setPersistent(false);
        }
        else {
        	getPreferenceScreen().removePreference(findPreference(KEY_DISPLAY_POSITION));
        }
    	try{
    		Bundle bundle = new Bundle();
    		//bundle = this.getIntent().getExtras();
    		selectedItemPosition = bundle.getInt("selectedItemPosition");
    		requestFocusHandler.postDelayed(requestFocusHandler_runnable, 50);
    	}
    	catch (Exception e) {
	    	e.printStackTrace();
    	}
    }

    private void updateTimeoutPreferenceDescription(long currentTimeout) {
        ListPreference preference = mScreenTimeoutPreference;
        String summary;
        if (currentTimeout < 0) {
            // Unsupported value
            summary = "";
        } else {
            final CharSequence[] entries = preference.getEntries();
            final CharSequence[] values = preference.getEntryValues();
            int best = 0;
            for (int i = 0; i < values.length; i++) {
                long timeout = Long.parseLong(values[i].toString());
				if(timeout==1&&currentTimeout==1){
					best=i;
					break;
				}
				else if (currentTimeout >= timeout&&timeout!=1) {
                    best = i;					
				}
				
            }		
			if(currentTimeout==1){
				summary = entries[best].toString();
			}else{
            summary = preference.getContext().getString(R.string.screen_timeout_summary,
                    entries[best]);
        }
			
        }
        preference.setSummary(summary);
    }

    private void disableUnusableTimeouts(ListPreference screenTimeoutPreference) {
        final DevicePolicyManager dpm =
                (DevicePolicyManager) getActivity().getSystemService(
                Context.DEVICE_POLICY_SERVICE);
        final long maxTimeout = dpm != null ? dpm.getMaximumTimeToLock(null) : 0;
        if (maxTimeout == 0) {
            return; // policy not enforced
        }
        final CharSequence[] entries = screenTimeoutPreference.getEntries();
        final CharSequence[] values = screenTimeoutPreference.getEntryValues();
        ArrayList<CharSequence> revisedEntries = new ArrayList<CharSequence>();
        ArrayList<CharSequence> revisedValues = new ArrayList<CharSequence>();
        for (int i = 0; i < values.length; i++) {
            long timeout = Long.parseLong(values[i].toString());
            if (timeout <= maxTimeout) {
                revisedEntries.add(entries[i]);
                revisedValues.add(values[i]);
            }
        }
        if (revisedEntries.size() != entries.length || revisedValues.size() != values.length) {
            screenTimeoutPreference.setEntries(
                    revisedEntries.toArray(new CharSequence[revisedEntries.size()]));
            screenTimeoutPreference.setEntryValues(
                    revisedValues.toArray(new CharSequence[revisedValues.size()]));
            final int userPreference = Integer.parseInt(screenTimeoutPreference.getValue());
            if (userPreference <= maxTimeout) {
                screenTimeoutPreference.setValue(String.valueOf(userPreference));
            } else {
                // There will be no highlighted selection since nothing in the list matches
                // maxTimeout. The user can still select anything less than maxTimeout.
                // TODO: maybe append maxTimeout to the list and mark selected.
            }
        }
        screenTimeoutPreference.setEnabled(revisedEntries.size() > 0);
    }

    int floatToIndex(float val) {
        String[] indices = getResources().getStringArray(R.array.entryvalues_font_size);
        float lastVal = Float.parseFloat(indices[0]);
        for (int i=1; i<indices.length; i++) {
            float thisVal = Float.parseFloat(indices[i]);
            if (val < (lastVal + (thisVal-lastVal)*.5f)) {
                return i-1;
            }
            lastVal = thisVal;
        }
        return indices.length-1;
    }
    
    public void readFontSizePreference(ListPreference pref) {
        try {
            mCurConfig.updateFrom(ActivityManagerNative.getDefault().getConfiguration());
        } catch (RemoteException e) {
            Log.w(TAG, "Unable to retrieve font size");
        }

        // mark the appropriate item in the preferences list
        int index = floatToIndex(mCurConfig.fontScale);
        pref.setValueIndex(index);

        // report the current size in the summary text
        final Resources res = getResources();
        String[] fontSizeNames = res.getStringArray(R.array.entries_font_size);
        pref.setSummary(String.format(res.getString(R.string.summary_font_size),
                fontSizeNames[index]));
    }
    
    @Override
    public void onResume() {
        super.onResume();
		mHideStatusBar.setChecked(SystemProperties.getBoolean("statusbar.hide.setting",false));

        updateState();
        getContentResolver().registerContentObserver(
                Settings.System.getUriFor(Settings.System.ACCELEROMETER_ROTATION), true,
                mAccelerometerRotationObserver);
    }

    @Override
    public void onPause() {
        super.onPause();

        getContentResolver().unregisterContentObserver(mAccelerometerRotationObserver);
    }

    private void updateState() {
        updateAccelerometerRotationCheckbox();
        readFontSizePreference(mFontSizePref);
    }

    private void updateAccelerometerRotationCheckbox() {
        mAccelerometer.setChecked(Settings.System.getInt(
                getContentResolver(),
                Settings.System.ACCELEROMETER_ROTATION, 0) != 0);
    }

    public void writeFontSizePreference(Object objValue) {
        try {
            mCurConfig.fontScale = Float.parseFloat(objValue.toString());
            ActivityManagerNative.getDefault().updatePersistentConfiguration(mCurConfig);
        } catch (RemoteException e) {
            Log.w(TAG, "Unable to save font size");
        }
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        if (preference == mAccelerometer) {
            try {
                IWindowManager wm = IWindowManager.Stub.asInterface(
                        ServiceManager.getService(Context.WINDOW_SERVICE));
                if (mAccelerometer.isChecked()) {
                    wm.thawRotation();
                } else {
                    //wm.freezeRotation(Surface.ROTATION_0);
					wm.freezeRotation(-1);//use current orientation
                }
            } catch (RemoteException exc) {
                Log.w(TAG, "Unable to save auto-rotate setting");
            }
        } else if (preference == mNotificationPulse) {
            boolean value = mNotificationPulse.isChecked();
            Settings.System.putInt(getContentResolver(), Settings.System.NOTIFICATION_LIGHT_PULSE,
                    value ? 1 : 0);
            return true;
        }else if(preference == mDisplayposition){
			Intent intent = new Intent();
			Bundle bundle = new Bundle();
			//bundle.putInt("selectedItemPosition", this.getListView().getSelectedItemPosition());
			//this.setVisible(false);
			this.setMenuVisibility(false);
			if((Utils.platformHas1080Scale() == 0) 
					|| ((Utils.platformHas1080Scale() == 1) && (!curOutputmode.equals("1080i")) && (!curOutputmode.equals("1080p")) && (!curOutputmode.equals("720p")))){
		    	setVideoDisable(1);
		    }
			intent .setComponent(new ComponentName("com.android.settings", "com.android.settings.PositionSetting"));
			intent.putExtras(bundle);
			startActivity(intent);
			DisplaySettings.this.finish();
        }
        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }

    public boolean onPreferenceChange(Preference preference, Object objValue) {
        final String key = preference.getKey();
        if (KEY_SCREEN_TIMEOUT.equals(key)) {
            int value = Integer.parseInt((String) objValue);
            try {
                Settings.System.putInt(getContentResolver(), SCREEN_OFF_TIMEOUT, (value==1)?(-1):value);
			    updateTimeoutPreferenceDescription(value);
            } catch (NumberFormatException e) {
                Log.e(TAG, "could not persist screen timeout setting", e);
            }
        }
        if (KEY_FONT_SIZE.equals(key)) {
            writeFontSizePreference(objValue);
        }
		if (KEY_OUTPUTMODE.equals(key)) {
            try {
                sel_index = Integer.parseInt((String) objValue);
				if(index_entry!=sel_index){
					Intent intent = new Intent(getActivity(), com.android.settings.OutputSetConfirm.class);
					intent.putExtra("pre_mode", index_entry);
					intent.putExtra("set_mode", sel_index);
					startActivityForResult(intent, GET_USER_OPERATION);
				}
			}
            catch (NumberFormatException e) {
                Log.e(TAG, "could not persist output mode setting", e);
            }
        }
			if (KEY_Hide_StatusBar.equals(key)){
						
						SystemProperties.set("statusbar.hide.setting",objValue.toString());
					//	if(mHideStatusBar.isChecked())
						 refresh();
						return true;
			  }
        return true;
    }
	
	   public void setVideoDisable(int value){
    	//VideoDisable:value=1;VideoEnable:value=0
    	File videoDisbaleFile = new File(VideoDisbaleFile);
        try {
        	BufferedWriter out = new BufferedWriter(new FileWriter(videoDisbaleFile), 32);
       		try {
    			Log.d(TAG,"setVideoDisable:"+value);
    			out.write(""+value);
        	}
    		finally {
    			out.close();
   			}
       	}
       	catch (IOException e) {
   		// TODO Auto-generated catch block
    		Log.e(TAG, "IOException when write "+videoDisbaleFile);
    	}
    }
    
    public void setFreeScale(int value, int osd){
    	//on:value=1;off:value=0
    	File freeScaleFile = null;
    	if(osd == 0)
    		freeScaleFile = new File(FreeScaleOsd0File);
    	else  		
    		freeScaleFile = new File(FreeScaleOsd1File);
    	try {
        	BufferedWriter out = new BufferedWriter(new FileWriter(freeScaleFile), 32);
       		try {
    			Log.d(TAG,"setFreeScale:"+value);
    			out.write(""+value);
        	}
    		finally {
    			out.close();
   			}
       	}
       	catch (IOException e) {
   		// TODO Auto-generated catch block
    		Log.e(TAG, "IOException when write "+freeScaleFile);
    	}
    }
    
	public void setVideoAxis(int xStart, int yStart, int xEnd, int yEnd){
	 	File videoAxisFile = new File(VideoAxisFile);
        try {
        	BufferedWriter out = new BufferedWriter(new FileWriter(videoAxisFile), 32);
        	try {
        		Log.d(TAG,"setVideoAxis: "+xStart+" "+yStart+" "+xEnd+" "+yEnd);
        		out.write(""+xStart+" "+yStart+" "+xEnd+" "+yEnd);
        	}
    		finally {
    			out.close();
    		}
        }
        catch (IOException e) {
    	// TODO Auto-generated catch block
        	Log.e(TAG, "IOException when write "+videoAxisFile);
        }
	}    
	
	public void setTVMode(String mode){
	 	File TVModeFile = new File(TVMode);
        try {
        	BufferedWriter out = new BufferedWriter(new FileWriter(TVModeFile), 32);
        	try {
        		Log.d(TAG,"setTVMode: "+mode);
        		out.write(""+mode);
        	}
    		finally {
    			out.close();
    		}
        }
        catch (IOException e) {
    	// TODO Auto-generated catch block
        	Log.e(TAG, "IOException when write "+TVModeFile);
        }
	}	
	
	public void writeFile(String file, String value){
		File OutputFile = new File(file);
		if(!OutputFile.exists()) {        	
        	return;
        }
    	try {
			BufferedWriter out = new BufferedWriter(new FileWriter(OutputFile), 32);
    		try {
				Log.d(TAG, "set" + file + ": " + value);
    			out.write(value);    
    		} 
			finally {
				out.close();
			}
		}
		catch (IOException e) {
			// TODO Auto-generated catch block
			Log.e(TAG, "IOException when write "+OutputFile);
		}
	}
    
    public void enableFreeScale(int newMode){
    	String pre_output_x="";
    	String pre_output_y="";
    	String pre_output_width="";
    	String pre_output_height="";
    	int output_x = 0;
    	int output_y = 0;
    	int output_width = 0;
    	int output_height = 0;
    	int output_right = 0;
    	int output_bottom = 0;
    	
    	switch(newMode){
		case 0:		//480i
			pre_output_x = SystemProperties.get(sel_480ioutput_x);
			pre_output_y = SystemProperties.get(sel_480ioutput_y);
			pre_output_width = SystemProperties.get(sel_480ioutput_width);
			pre_output_height = SystemProperties.get(sel_480ioutput_height);
			if(pre_output_x.equals(""))
				pre_output_x = "0";
			if(pre_output_y.equals(""))
				pre_output_y = "0";
			if(pre_output_width.equals(""))
				pre_output_width = String.valueOf(OUTPUT480_FULL_WIDTH);
			if(pre_output_height.equals(""))
				pre_output_height = String.valueOf(OUTPUT480_FULL_HEIGHT);
			break;
		case 1:		//480p
			pre_output_x = SystemProperties.get(sel_480poutput_x);
			pre_output_y = SystemProperties.get(sel_480poutput_y);
			pre_output_width = SystemProperties.get(sel_480poutput_width);
			pre_output_height = SystemProperties.get(sel_480poutput_height);	
			if(pre_output_x.equals(""))
				pre_output_x = "0";
			if(pre_output_y.equals(""))
				pre_output_y = "0";
			if(pre_output_width.equals(""))
				pre_output_width = String.valueOf(OUTPUT480_FULL_WIDTH);
			if(pre_output_height.equals(""))
				pre_output_height = String.valueOf(OUTPUT480_FULL_HEIGHT);	
			break;
		case 2:		//576i
			pre_output_x = SystemProperties.get(sel_576ioutput_x);
			pre_output_y = SystemProperties.get(sel_576ioutput_y);
			pre_output_width = SystemProperties.get(sel_576ioutput_width);
			pre_output_height = SystemProperties.get(sel_576ioutput_height);
			if(pre_output_x.equals(""))
				pre_output_x = "0";
			if(pre_output_y.equals(""))
				pre_output_y = "0";
			if(pre_output_width.equals(""))
				pre_output_width = String.valueOf(OUTPUT576_FULL_WIDTH);
			if(pre_output_height.equals(""))
				pre_output_height = String.valueOf(OUTPUT576_FULL_HEIGHT);
			break;
		case 3:		//576p
			pre_output_x = SystemProperties.get(sel_576poutput_x);
			pre_output_y = SystemProperties.get(sel_576poutput_y);
			pre_output_width = SystemProperties.get(sel_576poutput_width);
			pre_output_height = SystemProperties.get(sel_576poutput_height);	
			if(pre_output_x.equals(""))
				pre_output_x = "0";
			if(pre_output_y.equals(""))
				pre_output_y = "0";
			if(pre_output_width.equals(""))
				pre_output_width = String.valueOf(OUTPUT576_FULL_WIDTH);
			if(pre_output_height.equals(""))
				pre_output_height = String.valueOf(OUTPUT576_FULL_HEIGHT);	
			break;
		case 4:		//720p
	    	pre_output_x = SystemProperties.get(sel_720poutput_x);
	    	pre_output_y = SystemProperties.get(sel_720poutput_y);
	    	pre_output_width = SystemProperties.get(sel_720poutput_width);
	    	pre_output_height = SystemProperties.get(sel_720poutput_height);
			if(pre_output_x.equals(""))
				pre_output_x = "0";
			if(pre_output_y.equals(""))
				pre_output_y = "0";
			if(pre_output_width.equals(""))
				pre_output_width = String.valueOf(OUTPUT720_FULL_WIDTH);
			if(pre_output_height.equals(""))
				pre_output_height = String.valueOf(OUTPUT720_FULL_HEIGHT);
			break;
		case 5:		//1080i
			pre_output_x = SystemProperties.get(sel_1080ioutput_x);
			pre_output_y = SystemProperties.get(sel_1080ioutput_y);
			pre_output_width = SystemProperties.get(sel_1080ioutput_width);
			pre_output_height = SystemProperties.get(sel_1080ioutput_height);
			if(pre_output_x.equals(""))
				pre_output_x = "0";
			if(pre_output_y.equals(""))
				pre_output_y = "0";
			if(pre_output_width.equals(""))
				pre_output_width = String.valueOf(OUTPUT1080_FULL_WIDTH);
			if(pre_output_height.equals(""))
				pre_output_height = String.valueOf(OUTPUT1080_FULL_HEIGHT);
			break;
		case 6:		//1080p
			pre_output_x = SystemProperties.get(sel_1080poutput_x);
			pre_output_y = SystemProperties.get(sel_1080poutput_y);
			pre_output_width = SystemProperties.get(sel_1080poutput_width);
			pre_output_height = SystemProperties.get(sel_1080poutput_height);	
			if(pre_output_x.equals(""))
				pre_output_x = "0";
			if(pre_output_y.equals(""))
				pre_output_y = "0";
			if(pre_output_width.equals(""))
				pre_output_width = String.valueOf(OUTPUT1080_FULL_WIDTH);
			if(pre_output_height.equals(""))
				pre_output_height = String.valueOf(OUTPUT1080_FULL_HEIGHT);	
			break;
		default:	//720p
	    	pre_output_x = SystemProperties.get(sel_720poutput_x);
	    	pre_output_y = SystemProperties.get(sel_720poutput_y);
	    	pre_output_width = SystemProperties.get(sel_720poutput_width);
	    	pre_output_height = SystemProperties.get(sel_720poutput_height);
			if(pre_output_x.equals(""))
				pre_output_x = "0";
			if(pre_output_y.equals(""))
				pre_output_y = "0";
			if(pre_output_width.equals(""))
				pre_output_width = String.valueOf(OUTPUT720_FULL_WIDTH);
			if(pre_output_height.equals(""))
				pre_output_height = String.valueOf(OUTPUT720_FULL_HEIGHT);
			break;
    	}

    	output_x = Integer.valueOf(pre_output_x).intValue();
    	output_y = Integer.valueOf(pre_output_y).intValue();
    	output_width = Integer.valueOf(pre_output_width).intValue();
    	output_height = Integer.valueOf(pre_output_height).intValue();
    	output_right = output_x + output_width;
    	output_bottom = output_y + output_height;
    	
		String []values = getResources().getStringArray(R.array.outputmode_entries);
        String tv_outputmode = values[sel_index];
        SystemProperties.set(STR_OUTPUT_MODE, tv_outputmode);
        setTVMode(tv_outputmode);
		writeFile(PpscalerFile,"0");
    	setVideoAxis(output_x, output_y, output_right, output_bottom);
		writeFile(PpscalerFile,"1");
    }
	@Override
	public  void onActivityResult(int requestCode,int resultCode,Intent data)
		{
		super.onActivityResult(requestCode,resultCode,data);
		switch(requestCode)
			{
			case (GET_USER_OPERATION):
				if(resultCode==Activity.RESULT_OK)
				{
					String []values = getResources().getStringArray(R.array.outputmode_entries);
                    String tv_outputmode = values[sel_index];
                    SystemProperties.set(STR_OUTPUT_VAR, tv_outputmode);
					if(Utils.platformHas1080Scale() != 2){
						writeFile(PpscalerFile,"0");
						setFreeScale(0,0);
						setFreeScale(0,1);
                    SystemProperties.set("ctl.start", "display_reset");
                    String ret = SystemProperties.get("init.svc.display_reset", "");
                    if (ret != null && ret.equals("stopped")) 
						{
	                        Log.i(TAG,"reboot android");
						}
					}
					else{
			            index_entry = sel_index;
			            mOutputmode.setValueIndex(index_entry);
						writeFile(PpscalerFile,"1");
					}
				}
				else if(resultCode==Activity.RESULT_CANCELED)
					{
					mOutputmode.setValueIndex(index_entry);
					}
				
			}
		}
	
	private int findIndexOfEntry(String value, CharSequence[] entry) {
        if (value != null && entry != null) {
            for (int i = entry.length - 1; i >= 0; i--) {
                if (entry[i].equals(value)) {
                    return i;
                }
            }
        }
		return 4;  //set 720p as default
    }
    private void refresh() {  
   		 try {
     
							//Configuration config = new Configuration();
              ActivityManagerNative.getDefault().updateConfiguration(mCurConfig);
        } catch (RemoteException e) {
            Log.w(TAG, "Unable to retrieve font size");
        }
    }
}
