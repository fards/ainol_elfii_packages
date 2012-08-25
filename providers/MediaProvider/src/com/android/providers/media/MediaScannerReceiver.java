/* //device/content/providers/media/src/com/android/providers/media/MediaScannerReceiver.java
**
** Copyright 2007, The Android Open Source Project
**
** Licensed under the Apache License, Version 2.0 (the "License"); 
** you may not use this file except in compliance with the License. 
** You may obtain a copy of the License at 
**
**     http://www.apache.org/licenses/LICENSE-2.0 
**
** Unless required by applicable law or agreed to in writing, software 
** distributed under the License is distributed on an "AS IS" BASIS, 
** WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
** See the License for the specific language governing permissions and 
** limitations under the License.
*/

package com.android.providers.media;

import android.content.Context;
import android.content.Intent;
import android.content.BroadcastReceiver;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.SystemProperties;
import android.util.Log;

import java.io.File;


public class MediaScannerReceiver extends BroadcastReceiver
{
    private final static String TAG = "MediaScannerReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        Uri uri = intent.getData();
        if (action.equals(Intent.ACTION_BOOT_COMPLETED)) {
            // scan internal storage
            scan(context, MediaProvider.INTERNAL_VOLUME, null);
        } else {
            if (uri.getScheme().equals("file")) {
                // handle intents related to external storage
                String path = uri.getPath();
                String externalStoragePath = Environment.getExternalStorageDirectory().getPath();

                Log.d(TAG, "action: " + action + " path: " + path);
                if (action.equals(Intent.ACTION_MEDIA_MOUNTED)) {
                    if (SystemProperties.getBoolean("sys.mediascanner.enable", true)) {
                        boolean system_send = intent.getBooleanExtra("system-send", false);
                        if (system_send) {
                            scan(context, MediaProvider.EXTERNAL_VOLUME, path);
                        } else {
                            //for app doesn't know there are internal flash exists,
                            //so when it force to scan, we force scan all devices.
                            scanAll(context, MediaProvider.EXTERNAL_VOLUME);
                        }
                    } else
                        Log.d(TAG, "Media scanner disabled");
                } else if (action.equals(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE) &&
                        path != null && path.startsWith(externalStoragePath + "/")) {
                    scanFile(context, path);
                }
            }
        }
    }

    private void scan(Context context, String volume, String path) {
        Bundle args = new Bundle();
        args.putString("volume", volume);
        args.putString("path", path);
        context.startService(
                new Intent(context, MediaScannerService.class).putExtras(args));
    }

    private void scanAll(Context context, String volume) {
        Bundle args = new Bundle();
        args.putString("volume", volume);
        args.putBoolean("all", true);
        context.startService(
                new Intent(context, MediaScannerService.class).putExtras(args));
    }

    private void scanFile(Context context, String path) {
        Bundle args = new Bundle();
        args.putString("filepath", path);
        context.startService(
                new Intent(context, MediaScannerService.class).putExtras(args));
    }    
}


