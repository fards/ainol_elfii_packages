package com.android.camera;

import android.util.Log;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.DialogInterface;
import android.os.Bundle;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.File;
import java.io.IOException;
import android.os.Handler;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.provider.MediaStore;
import android.os.SystemProperties;
import android.os.Debug;
import android.database.Cursor;
import android.content.ContentResolver;
import android.net.Uri;
import android.content.DialogInterface.OnCancelListener;


public class IfHdmi extends Activity {
	private static final String TAG = "camera_ifhdmi";
	private static final String MODE_PATH = "/sys/class/display/mode";
	private static boolean[] overlayFlagArray= {true,true,true};//max support 3 camera
	private static int mCurrentCamera = 0;
	public Handler mStartHandler ;
	public Runnable mCameraChk;
	public PlayerStateReceive playerreceiver;
	private boolean mChkEnd;
	private static int mediErrorId=-1;
	private Intent resultintent;
	public class camerachktsk implements Runnable
	{
		public void run()
		{
			mChkEnd = true;
			if(!isCameraValid(IfHdmi.this) && overlayFlagArray[mCurrentCamera])
	        {
	        	Log.d(TAG, "Camera can't work in HDMI MODE!");
	        	AlertDialog fi = new AlertDialog.Builder(IfHdmi.this)
	    				.setTitle(R.string.medi_error_warning)
	    				.setMessage(mediErrorId)
	    				.setPositiveButton(R.string.medi_error_ok,
							new DialogInterface.OnClickListener() {
					            public void onClick(DialogInterface dialog, int whichButton) {
					            	dialog.dismiss();
					            	finish();
					            }
					        })
						.setOnCancelListener(new OnCancelListener() {
							 public void onCancel(DialogInterface dialog) {
									dialog.dismiss();
									finish();
							}
						})
	    			    .show();
	        }
	        else
	        {
                Intent intent = getIntent();
                boolean bNeedResult = false;
                if(intent != null) {
                    String action = intent.getAction();
                    Log.d(TAG, "action: " + action);
                    if(action.equals(MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA) || action.equals(MediaStore.ACTION_IMAGE_CAPTURE)) {
                        intent.setClass(IfHdmi.this, com.android.camera.Camera.class);
                        bNeedResult = true;
                    }
                    else if(action.equals(MediaStore.INTENT_ACTION_VIDEO_CAMERA) || action.equals(MediaStore.ACTION_VIDEO_CAPTURE)){
                        intent.setClass(IfHdmi.this, com.android.camera.VideoCamera.class);
                        bNeedResult = true;
                    }
                    else {
                        intent = new Intent(action);
                        intent.setClass(IfHdmi.this, com.android.camera.Camera.class);
                    }

                    if(bNeedResult == true) {
                        startActivityForResult(intent,0);
                    } else {
                        startActivity(intent);
                        finish();
                    }
                }
                else {
                    intent = new Intent();
                    intent.setClass(IfHdmi.this, com.android.camera.Camera.class);
                    startActivity(intent);
                    finish();
                }
            }
        }
    }

	public void onCreate(Bundle savedInstanceState)
	{
        //Debug.startAllocCounting();
        //boolean prop = SystemProperties.getBoolean("ro.board.usb.camera", false);
        super.onCreate(savedInstanceState);
//        playerreceiver = new PlayerStateReceive();
//        this.registerReceiver(playerreceiver,new IntentFilter("com.android.music.playstatechanged"));
        /*if(prop){
        	try {
        		for(int i =0;i < CameraHolder.instance().getNumberOfCameras();i++){
        			overlayFlagArray[i] =  CameraHolder.instance().open(i).useOverlay();
        			CameraHolder.instance().release();
        		}
        	} catch (Throwable ex) {
        		throw new RuntimeException("CameraHolder open failed", ex);
        	}
        }*/
        ComboPreferences mPreferences = new ComboPreferences(this);
        CameraSettings.upgradeGlobalPreferences(mPreferences.getGlobal());
        mCurrentCamera = CameraSettings.readPreferredCameraId(mPreferences);
        mStartHandler = new Handler();
        mCameraChk = new camerachktsk();
        mChkEnd = false;
	}

	@Override
	public void onStart() {
        super.onStart();
        //stopMediaPlayer();
        mStartHandler.postDelayed(mCameraChk,1000);
    }

	public void onResume() {
		super.onResume();
        playerreceiver = new PlayerStateReceive();
        this.registerReceiver(playerreceiver,new IntentFilter("com.android.music.playstatechanged"));		
	}
	
    public void onPause() { 
        super.onPause();
        this.unregisterReceiver(playerreceiver);
    }

/*
	//public MediaPlaybackService playerservice = null;
    public void stopMediaPlayer()//stop the backgroun music player
    {
    	Intent intent = new Intent();
    	intent.setAction("com.android.music.musicservicecommand.pause");
    	intent.putExtra("command", "pause");
    	this.sendBroadcast(intent);
    }
*/

    private static final String HDMI_STATE_PATH = "/sys/class/switch/hdmi/state";
    /** check hdmi connection*/
    public static boolean isCameraValid(Context context) {
        boolean rtn = false;      
        try {
            BufferedReader reader = null;
            
            if (SystemProperties.getBoolean("ro.vout.dualdisplay", false)) {
                reader = new BufferedReader(new FileReader(HDMI_STATE_PATH), 32);
                try {
                    String mode = reader.readLine();
                    Log.d(TAG, "current hdmi state :" + mode);
                    if (( mode != null) && mode.equals("0"))
                        rtn = true;
                    else {
                        rtn = false;
                    }
                } finally {
                	reader.close();
                }                 
                
            } else {
                if(SystemProperties.getBoolean("ro.platform.has.mbxuimode", false)){
                    return true;
                }
                reader = new BufferedReader(new FileReader(MODE_PATH), 256);
                try {
                    String mode = reader.readLine();
                    Log.d(TAG, "current display mode :" + mode);
                    if (( mode == null) || mode.equals("panel") || overlayFlagArray[mCurrentCamera] == false)
                        rtn = true;
                    else {
                        rtn = false;
                    }
                } finally {
                	reader.close();
                }                
            }

            
        	Log.d(TAG,"rtn is: "+rtn);
    		if( false == rtn ) {
    			mediErrorId=R.string.medi_error_box;
    			return false;
    		}

    		if(chkMediaAttached(context) == false)
    		{	
                rtn = false;
                Log.d(TAG,"rtn is when chkMediaAttached: "+rtn);
                mediErrorId=R.string.medi_error_scan;
    		}
        } catch (IOException e) {
        	Log.e(TAG, "IO Exception when read: " + MODE_PATH, e);
        	mediErrorId=R.string.medi_error_default;
        	return false;
        }

        return rtn;
    }

	public class PlayerStateReceive extends BroadcastReceiver{
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			Log.d("IfHdmi","receive player state change");
			if(action.equals("com.android.music.playstatechanged") && (mChkEnd == false))
			{
				mStartHandler.removeCallbacks(mCameraChk);
				mStartHandler.post(mCameraChk);
			}
		}
	}

	public static boolean chkMediaAttached(Context context) {
		boolean rtn = false;
		ContentResolver cr = context.getContentResolver();
        final Cursor cursor = cr.query(Uri.parse("content://media/"), (String[])null, (String)null,
                (String[])null, (String)null);
        if (cursor != null) {
            if (cursor.getCount() >= 1) {
				cursor.moveToFirst();
				boolean sd_mounted = false;
				boolean nand_mounted = false;
				
				do{
					Log.d(TAG,"isMediaScannerScanning get "+cursor.getString(0));
					Log.d(TAG,"nand storage path  "+Storage.getStoragePath(Storage.NAND_STORAGE));
					Log.d(TAG,"sdcard storage path  "+Storage.getStoragePath(Storage.SDCARD_STORAGE));
					if(new File(cursor.getString(0)).compareTo(new File(Storage.getStoragePath(Storage.NAND_STORAGE))) == 0){
						nand_mounted = true;
					} else if(new File(cursor.getString(0)).compareTo(new File(Storage.getStoragePath(Storage.SDCARD_STORAGE))) == 0) {
						sd_mounted = true;
					}
				}while(cursor.moveToNext()==true);

				if(Storage.DCIM_PATH == 0) {//external sdcard ONLY 
					if(sd_mounted) rtn = true;
				}else if(Storage.DCIM_PATH == 1) {//internal nand flash only
					if(nand_mounted) rtn = true;
				}else if(Storage.DCIM_PATH == 2) { // sdcard first
					if(sd_mounted||nand_mounted) rtn = true;
				}
            }
            cursor.close();
        }
        return rtn;
    }
	
    protected void onActivityResult(
            int requestCode, int resultCode, Intent data) {
        resultintent = data;
        setResult(resultCode,resultintent);
        finish();
	}
}
