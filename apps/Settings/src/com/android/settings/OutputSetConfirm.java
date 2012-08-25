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

import java.util.ArrayList;

//import android.app.admin.DevicePolicyManager;
import android.content.ContentResolver;
import android.content.Context;
import android.os.Bundle;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.provider.Settings;
import android.util.Log;
import android.view.KeyEvent;
import android.app.Activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Message;
import android.os.Bundle;

public class OutputSetConfirm extends Activity {
    private static final String TAG = "OutputSetConfirm";

    /** If there is output mode option, use this. */

    private int sel_index;
	private AlertDialog OutPutSetConfirmDiag=null;
	private Handler mWaitHandler;
	private final static long set_delay = 15*1000;
	private Handler mProgressHandler;
	private int index_entry;
	private int old_mode;
	static {
    	System.loadLibrary("outputswitchjni");
    }
	public native static int freeScaleSetModeJni(int mode);
	public native static int DisableFreeScaleJni(int mode);
	public native static int EnableFreeScaleJni(int new_mode, int old_mode);


    @Override
    protected void onCreate(Bundle savedInstanceState) {

	super.onCreate(savedInstanceState);

	Intent intent =this.getIntent();
	int new_mode = intent.getIntExtra("set_mode", 4);
	old_mode = intent.getIntExtra("pre_mode", 4);
	Log.e(TAG,"----------------------set preview mode"+new_mode);
	EnableFreeScaleJni(new_mode, old_mode);
    showDispmodeSetMsg();  
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

	private class SetconfirmHandler extends Handler {
		@Override
		public void handleMessage(Message msg) 
		   {
		   super.handleMessage(msg);
		   DisableFreeScaleJni(old_mode);
		   Log.e(TAG,"----------------------timeout");
		   setResult(RESULT_CANCELED,null);
		   OutPutSetConfirmDiag.dismiss();
		   finish();
		   }
   }

   private void showDispmodeSetMsg() {

	mProgressHandler = new SetconfirmHandler();		  
	mProgressHandler.sendEmptyMessageDelayed(0, set_delay);

        OutPutSetConfirmDiag = new AlertDialog.Builder(this)
            .setTitle(R.string.tv_output_mode_dialog_title)
            .setMessage(R.string.tv_output_mode_set_confirm_dialog)
            .setPositiveButton(R.string.yes, 
                            new DialogInterface.OnClickListener()
                            {
                                public void onClick(DialogInterface dialoginterface, int i) {
									Log.e(TAG,"----------------------yes"+old_mode);
									mProgressHandler.removeMessages(0);
									if(Utils.platformHas1080Scale() != 2){
										DisableFreeScaleJni(old_mode);
									}
									setResult(RESULT_OK,null);
									finish();
                                    }
                            }
                            )
            .setNegativeButton(R.string.display_position_dialog_no,
                            new DialogInterface.OnClickListener()
                            {
                                public void onClick(DialogInterface dialoginterface, int i) {
									Log.e(TAG,"----------------------no"+old_mode);
									mProgressHandler.removeMessages(0);
									DisableFreeScaleJni(old_mode);
									setResult(RESULT_CANCELED,null);
									finish();
									}
								}
                            )
            .setOnKeyListener(new DialogInterface.OnKeyListener() {
            	@Override
            	public boolean onKey (DialogInterface dialog, int keyCode, KeyEvent event) {
            		if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_UP && !event.isCanceled()) {
            			dialog.cancel(); 
            			Log.e(TAG,"----------------------back "+old_mode);
            			mProgressHandler.removeMessages(0);
            			DisableFreeScaleJni(old_mode);
            			setResult(RESULT_CANCELED,null);
            			finish();
            			return true;
            		}
            		return false;
            	}
            })                 
            .show();
        OutPutSetConfirmDiag.getButton(-2).requestFocus();
    }
}
