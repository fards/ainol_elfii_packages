package com.android.settings;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.os.Environment;
import android.os.storage.IMountService;
import android.os.ServiceManager;
import android.os.storage.StorageResultCode;
import android.os.SystemProperties;

public class DiskCorruptReceiver extends BroadcastReceiver {
	private final static String Tag = "DiskCorruptReceiver";
	private final static String ACTION_CRYPTKEEPER = "com.android.settings.DiskRecovery.start";
	private String media_mount_point;
	@Override
	public void onReceive(Context context, Intent intent) {
		Log.d(Tag, "start DiskCorruptReceiver warning dialog");
		media_mount_point = SystemProperties.get("ro.media.mountpoint");
		Log.d(Tag, "media_mount_point=" + media_mount_point);
		if ("".equals(media_mount_point) || "start".equals(SystemProperties.get("CryptKeeper.state")))
		    return;
		IMountService mMountService = IMountService.Stub.asInterface(ServiceManager.getService("mount"));
		try {
			if (mMountService.getVolumeState(media_mount_point).equals(Environment.MEDIA_UNMOUNTED)
			    && "true".equals(SystemProperties.get("ro.media.recoveryenable"))){
				String format_url = intent.getData().toString();
				Log.d(Tag, "get the format url is "+format_url);
				if(format_url.equals("file:///mnt/sdcard"))
				{
					Log.d(Tag, "flash disk corrupted, it will start the foramt dialog");
					Intent my_intent = new Intent(ACTION_CRYPTKEEPER);
					my_intent.putExtra("FormatUrl", format_url);
            				my_intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					context.startActivity(my_intent);	
				}
			}
		} catch(Exception e) {
			Log.e(Tag, "get volume state failed");
		}
		
	}

}
