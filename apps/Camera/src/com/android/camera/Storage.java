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

package com.android.camera;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.location.Location;
import android.net.Uri;
import android.os.Environment;
import android.os.StatFs;
import android.provider.MediaStore.Images;
import android.provider.MediaStore.Images.ImageColumns;
import android.util.Log;
import android.os.SystemProperties;
	
import java.io.File;
import java.io.FileOutputStream;

public class Storage {
    private static final String TAG = "CameraStorage";

    public static String DCIM =
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).toString();
            
    public static String DIRECTORY = DCIM + "/Camera";
    
    // Match the code in MediaProvider.computeBucketValues().
    public static String BUCKET_ID =
            String.valueOf(DIRECTORY.toLowerCase().hashCode());

    public static final long UNAVAILABLE = -1L;
    public static final long PREPARING = -2L;
    public static final long UNKNOWN_SIZE = -3L;
    public static final long LOW_STORAGE_THRESHOLD= 50000000;
    public static final long PICTURE_SIZE = 1500000;

    private static final int BUFSIZE = 4096;

    public static final int NAND_STORAGE = 0;
    public static final int SDCARD_STORAGE = 1;
    
    public static final int DCIM_PATH = SystemProperties.getInt("ro.camera.dcim", 0);
    
		private static File getInternalStoragePublicDirectory(String type) {
        return new File(Environment.getInternalStorageDirectory(), type);
    }
    
    public static Uri addImage(ContentResolver resolver, String title, long date,
                Location location, int orientation, byte[] jpeg, int width, int height) {
        // Save the image.
        String path = generateFilepath(title);
        FileOutputStream out = null;
        try {
            out = new FileOutputStream(path);
            out.write(jpeg);
        } catch (Exception e) {
            Log.e(TAG, "Failed to write image", e);
            return null;
        } finally {
            try {
                out.close();
            } catch (Exception e) {
            }
        }

        // Insert into MediaStore.
        ContentValues values = new ContentValues(9);
        values.put(ImageColumns.TITLE, title);
        values.put(ImageColumns.DISPLAY_NAME, title + ".jpg");
        values.put(ImageColumns.DATE_TAKEN, date);
        values.put(ImageColumns.MIME_TYPE, "image/jpeg");
        values.put(ImageColumns.ORIENTATION, orientation);
        values.put(ImageColumns.DATA, path);
        values.put(ImageColumns.SIZE, jpeg.length);
        values.put(ImageColumns.WIDTH, width);
        values.put(ImageColumns.HEIGHT, height);

        if (location != null) {
            values.put(ImageColumns.LATITUDE, location.getLatitude());
            values.put(ImageColumns.LONGITUDE, location.getLongitude());
        }

        Uri uri = null;
        try {
            uri = resolver.insert(Images.Media.EXTERNAL_CONTENT_URI, values);
        } catch (Throwable th)  {
            // This can happen when the external volume is already mounted, but
            // MediaScanner has not notify MediaProvider to add that volume.
            // The picture is still safe and MediaScanner will find it and
            // insert it into MediaProvider. The only problem is that the user
            // cannot click the thumbnail to review the picture.
            Log.e(TAG, "Failed to write MediaStore" + th);
        }
        return uri;
    }

    public static String generateFilepath(String title) {
        return DIRECTORY + '/' + title + ".jpg";
    }

    public static long getAvailableSpace() {
        String state = Environment.getExternalStorageState();
				String state_internal = Environment.getInternalStorageState();   
				        
        Log.d(TAG, "External storage state=" + state + "Internal storage state=" + state_internal);
        if(DCIM_PATH == 2){//sd first
	        if (Environment.MEDIA_CHECKING.equals(state)) {
	            return PREPARING;
	        }
	        if (!Environment.MEDIA_MOUNTED.equals(state)) {
		        if (!Environment.MEDIA_MOUNTED.equals(state_internal)) {		        	
		            return UNAVAILABLE;
		        } else {
			        DCIM = getInternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).toString();
			        DIRECTORY = DCIM + "/Camera"; 	
			        BUCKET_ID = String.valueOf(DIRECTORY.toLowerCase().hashCode());
		        }
	        }
        } else if (DCIM_PATH == 1) {//nand only
	        DCIM = getInternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).toString();
	        DIRECTORY = DCIM + "/Camera";
	        BUCKET_ID = String.valueOf(DIRECTORY.toLowerCase().hashCode());   	
					if (Environment.MEDIA_CHECKING.equals(state_internal)) {
	            return PREPARING;
	        }
	        if (!Environment.MEDIA_MOUNTED.equals(state_internal)) {
	            return UNAVAILABLE;
	        }
        } else { //sd only
	        if (Environment.MEDIA_CHECKING.equals(state)) {
	            return PREPARING;
	        }
	        if (!Environment.MEDIA_MOUNTED.equals(state)) {
	            return UNAVAILABLE;
	        }         	
        }


        File dir = new File(DIRECTORY);
        dir.mkdirs();
        if (!dir.isDirectory() || !dir.canWrite()) {
            return UNAVAILABLE;
        }

        try {
            StatFs stat = new StatFs(DIRECTORY);
            return stat.getAvailableBlocks() * (long) stat.getBlockSize();
        } catch (Exception e) {
            Log.i(TAG, "Fail to access external storage", e);
        }
        return UNKNOWN_SIZE;
    }

    /**
     * OSX requires plugged-in USB storage to have path /DCIM/NNNAAAAA to be
     * imported. This is a temporary fix for bug#1655552.
     */
    public static void ensureOSXCompatible() {
        File nnnAAAAA = new File(DCIM, "100ANDRO");
        if (!(nnnAAAAA.exists() || nnnAAAAA.mkdirs())) {
            Log.e(TAG, "Failed to create " + nnnAAAAA.getPath());
        }
    }

	public static String getStoragePath(int storage) {
		if(Environment.isExternalStorageBeSdcard() == true) {
			if(storage == NAND_STORAGE)
				return Environment.getInternalStorageDirectory().toString();
			else if(storage == SDCARD_STORAGE)
				return Environment.getExternalStorageDirectory().toString();
		} else {
           if(storage == NAND_STORAGE)
				return Environment.getExternalStorageDirectory().toString();
			else if(storage == SDCARD_STORAGE)
				return Environment.getExternalStorage2Directory().toString();
		}
		return null;
	}

	public static boolean chkStorageMount(int storage) {
		if(Environment.isExternalStorageBeSdcard() == true) {
			if(storage == NAND_STORAGE) {
				return Environment.getInternalStorageState().equals(Environment.MEDIA_MOUNTED)? true:false ;
			} else if(storage == SDCARD_STORAGE) {
				return Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)? true:false ;
			} 
		} else {
			if(storage == NAND_STORAGE) {
				return Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)? true:false ;
			} else if(storage == SDCARD_STORAGE) {
				return Environment.getExternalStorage2State().equals(Environment.MEDIA_MOUNTED)? true:false ;
			}

		}
		return false;
	}
}
