/*
 * Copyright (C) 2011 The Android Open Source Project
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

import android.app.Activity;
import android.app.StatusBarManager;
import android.content.ComponentName;
import android.content.Context;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;


public class DiskRecovery extends Activity {
    private static final String TAG = "DiskRecovery";
    
    /** Used to communicate with recovery.  See bootable/recovery/recovery.c. */
    private static File RECOVERY_DIR = new File("/cache/recovery");
    private static File COMMAND_FILE = new File(RECOVERY_DIR, "command");
    private static File LOG_FILE = new File(RECOVERY_DIR, "log");

    PowerManager.WakeLock mWakeLock = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        
        // Disable the status bar
        StatusBarManager sbm = (StatusBarManager) getSystemService(Context.STATUS_BAR_SERVICE);
        sbm.disable(StatusBarManager.DISABLE_EXPAND
                | StatusBarManager.DISABLE_NOTIFICATION_ICONS
                | StatusBarManager.DISABLE_NOTIFICATION_ALERTS
                | StatusBarManager.DISABLE_SYSTEM_INFO
                | StatusBarManager.DISABLE_HOME
                | StatusBarManager.DISABLE_RECENT
                | StatusBarManager.DISABLE_BACK);
        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        mWakeLock = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK, TAG);
        if (mWakeLock != null) {
            mWakeLock.acquire();
        }
    }

    /**
     * Note, we defer the state check and screen setup to onStart() because this will be
     * re-run if the user clicks the power button (sleeping/waking the screen), and this is
     * especially important if we were to lose the wakelock for any reason.
     */
    @Override
    public void onStart() {
        super.onStart();
	Log.d(TAG, "DiskRecovery Start");
        setupUi();
    }

    /**
     * Initializes the UI based on the current state of encryption.
     * This is idempotent - calling repeatedly will simply re-initialize the UI.
     */
    private void setupUi() {
    	setContentView(R.layout.crypt_keeper_progress);
    	showFactoryReset();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (mWakeLock != null) {
            Log.d(TAG, "Releasing and destroying wakelock");
            mWakeLock.release();
            mWakeLock = null;
        }
    }

    private void showFactoryReset() {
        // Hide the encryption-bot to make room for the "factory reset" button
        findViewById(R.id.encroid).setVisibility(View.GONE);

        // Show the reset button, failure text, and a divider
        Button button = (Button) findViewById(R.id.factory_reset);
        button.setVisibility(View.VISIBLE);
        button.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                // Factory reset the device.
                try {
                	rebootWipeMediaData();
                } catch(Exception e) {
			Log.e(TAG, "Reboot failed");
		}
            }
        });
        TextView tv = (TextView) findViewById(R.id.title);
        tv.setText(R.string.media_disk_mount_failed_title);

        tv = (TextView) findViewById(R.id.status);
        tv.setText(R.string.media_disk_mount_failed_summary);

        View view = findViewById(R.id.bottom_divider);
        if (view != null) {
            view.setVisibility(View.VISIBLE);
        }
    }

    private void rebootWipeMediaData()
        throws IOException {
        bootCommand("--wipe_media");
    }
    
    /**
     * Reboot into the recovery system with the supplied argument.
     * @param arg to pass to the recovery utility.
     * @throws IOException if something goes wrong.
     */
    private void bootCommand(String arg) throws IOException {
        RECOVERY_DIR.mkdirs();  // In case we need it
        COMMAND_FILE.delete();  // In case it's not writable
        LOG_FILE.delete();

        FileWriter command = new FileWriter(COMMAND_FILE);
        try {
            command.write(arg);
            command.write("\n");
        } finally {
            command.close();
        }

        // Having written the command file, go ahead and reboot
        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        pm.reboot("factory_reset_reboot");

        throw new IOException("Reboot failed (no permissions?)");
    }
}
