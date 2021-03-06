package com.android.settings;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.os.SystemProperties;

public class PositionSetting extends Activity {
	private final String TAG = "Settings_PositionSetting";
	private static int zoom_pixel = 2;
	private int width_t = 0; 
	private int height_t = 0;
	private boolean zoom_flag = false; //zoom_flag is true: zoom in;zoom_flag is false: zoom out
	private Rect rect = null;
	private static double outputsize_per = 0.08; 
	private Outputsize outputsize = new Outputsize();
	private PositionCoor position_per = new PositionCoor();
	private PositionCoor position_cur = new PositionCoor();
	private boolean videoDisable_flag = true;
    private int selectedItemPosition;
	private boolean visible_flag = true;
	
	//private String prefix = SystemProperties.get("ro.ubootenv.prefix");
	private static final String STR_OUTPUT_MODE = "ubootenv.var.outputmode";
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
	private String curOutputmode= "";
	private String pre_output_x="";
	private String pre_output_y="";
	private String pre_output_width="";
	private String pre_output_height="";
	private String []outputmode_array;

	private ImageButton mchangeZoomBtn;
	private ImageButton mleftBtn;
	private ImageButton mrightBtn;
	private ImageButton mtopBtn;
	private ImageButton mbottomBtn;
	private static final int GET_USER_OPERATION=1;
	private static final int GET_DEFAULT_OPERATION=2;

	private static final int OUTPUT480_FULL_WIDTH = 720;
	private static final int OUTPUT480_FULL_HEIGHT = 480;
	private static final int OUTPUT576_FULL_WIDTH = 720;
	private static final int OUTPUT576_FULL_HEIGHT = 576;
	private static final int OUTPUT720_FULL_WIDTH = 1280;
	private static final int OUTPUT720_FULL_HEIGHT = 720;
	private static final int OUTPUT1080_FULL_WIDTH = 1920;
	private static final int OUTPUT1080_FULL_HEIGHT = 1080;

	private static String VideoDisbaleFile= "/sys/class/video/disable_video";
	private static String VideoEnableFile= "/sys/class/display/wr_reg";
	private static String FreeScaleAxisFile= "/sys/class/graphics/fb0/free_scale_axis";
	private static String FreeScaleOsd0File= "/sys/class/graphics/fb0/free_scale";
	private static String FreeScaleOsd1File= "/sys/class/graphics/fb1/free_scale";
	private static String VideoAxisFile= "/sys/class/video/axis";
	private static String DisplayAxisFile= "/sys/class/display/axis";
	private static String PpscalerFile= "/sys/class/ppmgr/ppscaler";

	private static String displayAxisFile_init = null;
	private static String freeScaleAxisFile_init = null;
	private static String videoEnableFile_init = null;
	private static String videoAxisFile_init = "0 0 0 0";

	Handler exitHandler=new Handler();
	Runnable exit_runnable=new Runnable(){
	@Override
	public void run() {
	// TODO Auto-generated method stub
		if((Utils.platformHas1080Scale() == 0) 
				|| ((Utils.platformHas1080Scale() == 1) && (!curOutputmode.equals("1080i")) && (!curOutputmode.equals("1080p")) && (!curOutputmode.equals("720p")))){
			setFreeScale(0,0);
			setFreeScale(0,1);
			setDisplayAxis(displayAxisFile_init);
		    setFreeScaleAxis(freeScaleAxisFile_init);
		}
		else{
			writeFile(PpscalerFile,"1");
		}
		setVideoAxis(videoAxisFile_init);
		Intent intent = new Intent();
		Bundle bundle = new Bundle();
		bundle.putInt("selectedItemPosition", selectedItemPosition);
		intent.setClass(PositionSetting.this, DisplaySettings.class);
		intent.putExtras(bundle);
		startActivity(intent);
		PositionSetting.this.finish();
	}
	};

	Handler exitNoHandler=new Handler();
	Runnable exitNo_runnable=new Runnable(){
	@Override
	public void run() {
	// TODO Auto-generated method stub
		if((Utils.platformHas1080Scale() == 0) 
				|| ((Utils.platformHas1080Scale() == 1) && (!curOutputmode.equals("1080i")) && (!curOutputmode.equals("1080p")) && (!curOutputmode.equals("720p")))){
			setFreeScale(0,0);
			setFreeScale(0,1);
			setDisplayAxis(displayAxisFile_init);
		    setFreeScaleAxis(freeScaleAxisFile_init);
		}
		setVideoAxis(videoAxisFile_init);
    	Intent intent = new Intent(PositionSetting.this, DisplayPositionSetConfirm.class);
    	Bundle bundle = new Bundle();
    	bundle.putInt("get_operation", GET_USER_OPERATION);
    	intent.putExtras(bundle);
		startActivityForResult(intent, GET_USER_OPERATION);
	}
	};

	Handler exitDefaultHandler=new Handler();
	Runnable exitDefault_runnable=new Runnable(){
	@Override
	public void run() {
	// TODO Auto-generated method stub
		if((Utils.platformHas1080Scale() == 0) 
				|| ((Utils.platformHas1080Scale() == 1) && (!curOutputmode.equals("1080i")) && (!curOutputmode.equals("1080p")) && (!curOutputmode.equals("720p")))){
			setFreeScale(0,0);
			setFreeScale(0,1);
			setDisplayAxis(displayAxisFile_init);
		    setFreeScaleAxis(freeScaleAxisFile_init);
		}
		setVideoAxis(videoAxisFile_init);
	    Intent intent = new Intent(PositionSetting.this, DisplayPositionSetConfirm.class);
    	Bundle bundle = new Bundle();
    	bundle.putInt("get_operation", GET_DEFAULT_OPERATION);
    	intent.putExtras(bundle);
		startActivityForResult(intent, GET_DEFAULT_OPERATION);
	}
	};
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,  WindowManager.LayoutParams.FLAG_FULLSCREEN);
		requestWindowFeature(Window.FEATURE_NO_TITLE); 
		setContentView(R.xml.position_setting);
		if(Utils.platformHas1080Scale() == 2){
			TextView mHelp = (TextView)findViewById(R.id.positionsetting_help);
			mHelp.setText(R.string.position_help_noreboot);
		}
		mchangeZoomBtn = (ImageButton)findViewById(R.id.btn_position_changeZoom);
		mchangeZoomBtn.setOnTouchListener(new mpositionChangeZoomBtnOnTouchistener());
		mchangeZoomBtn.setOnKeyListener(new mpositionChangeZoomBtnOnKeyistener());
		mleftBtn = (ImageButton)findViewById(R.id.btn_position_left);
		mleftBtn.setOnTouchListener(new mpositionLeftBtnOnTouchListener());
		mrightBtn = (ImageButton)findViewById(R.id.btn_position_right);
		mrightBtn.setOnTouchListener(new mpositionRightBtnOnTouchListener());
		mtopBtn = (ImageButton)findViewById(R.id.btn_position_top);
		mtopBtn.setOnTouchListener(new mpositionTopBtnOnTouchListener());
		mbottomBtn = (ImageButton)findViewById(R.id.btn_position_bottom);
		mbottomBtn.setOnTouchListener(new mpositionBottomBtnOnTouchListener());
		
	    curOutputmode = SystemProperties.get(STR_OUTPUT_MODE);
		outputmode_array = getResources().getStringArray(R.array.position_entries);
		getOutputsize();
	    getOutput(curOutputmode);
		if((Utils.platformHas1080Scale() == 0) 
				|| ((Utils.platformHas1080Scale() == 1) && (!curOutputmode.equals("1080i")) && (!curOutputmode.equals("1080p")) && (!curOutputmode.equals("720p")))){
		    position_per.width = Integer.valueOf(pre_output_width).intValue();
		    position_per.height = Integer.valueOf(pre_output_height).intValue(); 
		    position_per.left = Integer.valueOf(pre_output_x).intValue();
		    position_per.top = Integer.valueOf(pre_output_y).intValue();
		    position_per.right = position_per.left + position_per.width - 1;
		    position_per.bottom = position_per.top + position_per.height - 1;
		    position_cur.width = position_per.width;
		    position_cur.height = position_per.height;
		    position_cur.left = 0;
		    position_cur.top = 0;
		    position_cur.right = position_per.right - position_per.left+1;
		    position_cur.bottom = position_per.bottom - position_per.top+1;
	    }
	    else{
		    position_per.width = 0;
		    position_per.height = 0; 
		    position_per.left = 0;
		    position_per.top = 0;
		    position_per.right = 0;
		    position_per.bottom = 0;
		    position_cur.width = Integer.valueOf(pre_output_width).intValue();
		    position_cur.height = Integer.valueOf(pre_output_height).intValue(); 
		    position_cur.left = Integer.valueOf(pre_output_x).intValue();
		    position_cur.top = Integer.valueOf(pre_output_y).intValue();
		    position_cur.right = position_cur.width + position_cur.left-1;
		    position_cur.bottom = position_cur.height + position_cur.top-1;
	    }
	    
	    displayAxisFile_init = getDevice(DisplayAxisFile);
	    freeScaleAxisFile_init = getDevice(FreeScaleAxisFile);
	    videoEnableFile_init = getDevice(VideoEnableFile);
	    //setVideoDisable(1);
	    videoDisable_flag = true;
		if((Utils.platformHas1080Scale() == 0) 
				|| ((Utils.platformHas1080Scale() == 1) && (!curOutputmode.equals("1080i")) && (!curOutputmode.equals("1080p")) && (!curOutputmode.equals("720p")))){
		    setFreeScaleAxis(position_per.left, 0, position_per.right, (position_per.height - 1));
		    setVideoAxis(0, 0, (position_per.width), (position_per.height));
		    setDisplayAxis(0, 0, position_per.width, position_per.height);
	    }
		else{
			writeFile(PpscalerFile,"0");
		    videoAxisFile_init = getDevice(VideoAxisFile);
		}
		setFreeScale(1,0);
		setFreeScale(1,1);
	    try{
	    	Bundle bundle = new Bundle();
    		bundle = this.getIntent().getExtras();
    		selectedItemPosition = bundle.getInt("selectedItemPosition");
	    }
    	catch (Exception e) {
	    	e.printStackTrace();
    	}
	}

	class mpositionChangeZoomBtnOnTouchistener implements OnTouchListener{   
        public boolean onTouch(View v, MotionEvent event) {   
        	if(event.getAction() == MotionEvent.ACTION_DOWN){
        		mchangeZoomBtn.setBackgroundResource(R.drawable.position_button_zoom_hl);
    			if(zoom_flag == true)
    			{
    				//mchangeZoomBtn.setBackgroundResource(R.drawable.position_button_zoomout);
    				mleftBtn.setBackgroundResource(R.drawable.position_button_right);
    				mrightBtn.setBackgroundResource(R.drawable.position_button_left);
    				mtopBtn.setBackgroundResource(R.drawable.position_button_down);
    				mbottomBtn.setBackgroundResource(R.drawable.position_button_up);
    				zoom_flag = false;
    			}
    			else
    			{
    				//mchangeZoomBtn.setBackgroundResource(R.drawable.position_button_zoomin);
    				mleftBtn.setBackgroundResource(R.drawable.position_button_left);
    				mrightBtn.setBackgroundResource(R.drawable.position_button_right);
    				mtopBtn.setBackgroundResource(R.drawable.position_button_up);
    				mbottomBtn.setBackgroundResource(R.drawable.position_button_down);
    				zoom_flag = true;
    			}
        	}
        	else if(event.getAction() == MotionEvent.ACTION_UP){
        		mchangeZoomBtn.setBackgroundResource(R.drawable.position_button_zoom);
        	}
        	return false;   
        }   
	}
	
	class mpositionChangeZoomBtnOnKeyistener implements OnKeyListener{   
		public boolean onKey(View v, int keyCode, KeyEvent event)  {   
        	if(event.getAction() == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_DPAD_CENTER){
        		mchangeZoomBtn.setBackgroundResource(R.drawable.position_button_zoom_hl);
    			if(zoom_flag == true)
    			{
    				//mchangeZoomBtn.setBackgroundResource(R.drawable.position_button_zoomout);
    				mleftBtn.setBackgroundResource(R.drawable.position_button_right);
    				mrightBtn.setBackgroundResource(R.drawable.position_button_left);
    				mtopBtn.setBackgroundResource(R.drawable.position_button_down);
    				mbottomBtn.setBackgroundResource(R.drawable.position_button_up);
    				zoom_flag = false;
    			}
    			else
    			{
    				//mchangeZoomBtn.setBackgroundResource(R.drawable.position_button_zoomin);
    				mleftBtn.setBackgroundResource(R.drawable.position_button_left);
    				mrightBtn.setBackgroundResource(R.drawable.position_button_right);
    				mtopBtn.setBackgroundResource(R.drawable.position_button_up);
    				mbottomBtn.setBackgroundResource(R.drawable.position_button_down);
    				zoom_flag = true;
    			}
        	}
        	else if(event.getAction() == KeyEvent.ACTION_UP && keyCode == KeyEvent.KEYCODE_DPAD_CENTER){
        		mchangeZoomBtn.setBackgroundResource(R.drawable.position_button_zoom);
        	}
        	return false;   
        }   
	}
	
	class mpositionLeftBtnOnTouchListener implements OnTouchListener{
		//@Override
		public boolean onTouch(View v, MotionEvent event) {
			// TODO Auto-generated method stub
			if(event.getAction() == MotionEvent.ACTION_DOWN){
				if(videoDisable_flag){
					videoDisable_flag = false;
				}
				if(zoom_flag){
					mleftBtn.setBackgroundResource(R.drawable.position_button_left_hl);
	    		}
	    		else{
					mleftBtn.setBackgroundResource(R.drawable.position_button_right_hl);
	    		}
			}
			else if(event.getAction() == MotionEvent.ACTION_UP){
				if(zoom_flag){
					mleftBtn.setBackgroundResource(R.drawable.position_button_left);
	    			if(position_cur.left > (-position_per.left)){
	    				position_cur.left -= zoom_pixel;
	    				if(position_cur.left < (-position_per.left)){
	    					position_cur.left = -position_per.left;
	    				}
		    			setVideoAxis(position_cur.left, position_cur.top, position_cur.right, position_cur.bottom);
	    			}
				}
				else{
					mleftBtn.setBackgroundResource(R.drawable.position_button_right);
	    			if(position_cur.left < (outputsize.width_min - position_per.left)){
		    			position_cur.left += zoom_pixel;
		    			if(position_cur.left > (outputsize.width_min - position_per.left)){
		    				position_cur.left = outputsize.width_min - position_per.left;
		    			}
		    			setVideoAxis(position_cur.left, position_cur.top, position_cur.right, position_cur.bottom);
	    			}
				}
			}
        	return false;   
		}	
	}
	
	class mpositionRightBtnOnTouchListener implements OnTouchListener{
		//@Override
		public boolean onTouch(View v, MotionEvent event) {
			// TODO Auto-generated method stub
			if(event.getAction() == MotionEvent.ACTION_DOWN){
				if(videoDisable_flag){
					videoDisable_flag = false;
				}
				if(zoom_flag){
					mrightBtn.setBackgroundResource(R.drawable.position_button_right_hl);
	    		}
	    		else{
	    			mrightBtn.setBackgroundResource(R.drawable.position_button_left_hl);
	    		}
			}
			else if(event.getAction() == MotionEvent.ACTION_UP){
				if(zoom_flag){
					mrightBtn.setBackgroundResource(R.drawable.position_button_right);
	    			if(position_cur.right < (outputsize.width - position_per.left)){
	    				position_cur.right += zoom_pixel;
	    				if(position_cur.right > (outputsize.width - position_per.left)){
	    					position_cur.right = outputsize.width - position_per.left;
	    				}
		    			setVideoAxis(position_cur.left, position_cur.top, position_cur.right, position_cur.bottom);
	    			}
				}
				else{
					mrightBtn.setBackgroundResource(R.drawable.position_button_left);
	    			if(position_cur.right > (outputsize.width - position_per.left - outputsize.width_min)){
		    			position_cur.right -= zoom_pixel;
		    			if(position_cur.right < (outputsize.width - position_per.left - outputsize.width_min)){
		    				position_cur.right = outputsize.width - position_per.left - outputsize.width_min;
		    			}
		    			setVideoAxis(position_cur.left, position_cur.top, position_cur.right, position_cur.bottom);
	    			}
				}
			}
        	return false;   
		}	
	}
	
	class mpositionTopBtnOnTouchListener implements OnTouchListener{
		//@Override
		public boolean onTouch(View v, MotionEvent event) {
			// TODO Auto-generated method stub
			if(event.getAction() == MotionEvent.ACTION_DOWN){
				if(videoDisable_flag){
					videoDisable_flag = false;
				}
				if(zoom_flag){
					mtopBtn.setBackgroundResource(R.drawable.position_button_up_hl);
	    		}
	    		else{
	    			mtopBtn.setBackgroundResource(R.drawable.position_button_down_hl);
	    		}
			}
			else if(event.getAction() == MotionEvent.ACTION_UP){
				if(zoom_flag){
					mtopBtn.setBackgroundResource(R.drawable.position_button_up);
	    			if(position_cur.top > (-position_per.top)){
	    				position_cur.top -= zoom_pixel;
	    				if(position_cur.top < (-position_per.top)){
	    					position_cur.top = -position_per.top;
	    				}
		    			setVideoAxis(position_cur.left, position_cur.top, position_cur.right, position_cur.bottom);
	    			}
				}
				else{
					mtopBtn.setBackgroundResource(R.drawable.position_button_down);
	    			if(position_cur.top < (outputsize.height_min - position_per.top)){
		    			position_cur.top += zoom_pixel;
		    			if(position_cur.top > (outputsize.height_min - position_per.top)){
		    				position_cur.top = outputsize.height_min - position_per.top;
		    			}
		    			setVideoAxis(position_cur.left, position_cur.top, position_cur.right, position_cur.bottom);
	    			}
				}
			}
        	return false;   
		}	
	}
	
	class mpositionBottomBtnOnTouchListener implements OnTouchListener{
		//@Override
		public boolean onTouch(View v, MotionEvent event) {
			// TODO Auto-generated method stub
			if(event.getAction() == MotionEvent.ACTION_DOWN){
				if(videoDisable_flag){
					videoDisable_flag = false;
				}
				if(zoom_flag){
					mbottomBtn.setBackgroundResource(R.drawable.position_button_down_hl);
	    		}
	    		else{
	    			mbottomBtn.setBackgroundResource(R.drawable.position_button_up_hl);
	    		}
			}
			else if(event.getAction() == MotionEvent.ACTION_UP){
				if(zoom_flag){
					mbottomBtn.setBackgroundResource(R.drawable.position_button_down);
	    			if(position_cur.bottom < (outputsize.height - position_per.top)){
	    				position_cur.bottom += zoom_pixel;
	    				if(position_cur.bottom > (outputsize.height - position_per.top)){
	    					position_cur.bottom = outputsize.height - position_per.top;
	    				}
		    			setVideoAxis(position_cur.left, position_cur.top, position_cur.right, position_cur.bottom);
	    			}
				}
				else{
					mbottomBtn.setBackgroundResource(R.drawable.position_button_up);
	    			if(position_cur.bottom > (outputsize.height - position_per.top - outputsize.height_min)){
		    			position_cur.bottom -= zoom_pixel;
		    			if(position_cur.bottom < (outputsize.height - position_per.top - outputsize.height_min)){
		    				position_cur.bottom = outputsize.height - position_per.top - outputsize.height_min;
		    			}
		    			setVideoAxis(position_cur.left, position_cur.top, position_cur.right, position_cur.bottom);
	    			}
				}
			}
        	return false;   
		}	
	}

	public boolean onKeyUp(int keyCode, KeyEvent msg) {
		switch(keyCode){
		case KeyEvent.KEYCODE_DPAD_UP:
		case KeyEvent.KEYCODE_DPAD_DOWN:
		case KeyEvent.KEYCODE_DPAD_LEFT:
		case KeyEvent.KEYCODE_DPAD_RIGHT:
		case KeyEvent.KEYCODE_DPAD_CENTER:
			if(zoom_flag == true)
			{
				mchangeZoomBtn.setBackgroundResource(R.drawable.position_button_zoom);
				mleftBtn.setBackgroundResource(R.drawable.position_button_left);
				mrightBtn.setBackgroundResource(R.drawable.position_button_right);
				mtopBtn.setBackgroundResource(R.drawable.position_button_up);
				mbottomBtn.setBackgroundResource(R.drawable.position_button_down);
			}
			else
			{
				mchangeZoomBtn.setBackgroundResource(R.drawable.position_button_zoom);
				mleftBtn.setBackgroundResource(R.drawable.position_button_right);
				mrightBtn.setBackgroundResource(R.drawable.position_button_left);
				mtopBtn.setBackgroundResource(R.drawable.position_button_down);
				mbottomBtn.setBackgroundResource(R.drawable.position_button_up);
			}
			break;
		}
		return true;
	}
	public boolean onKeyDown(int keyCode, KeyEvent msg) {
		mchangeZoomBtn.requestFocus();
		mchangeZoomBtn.requestFocusFromTouch();
	    if (keyCode == KeyEvent.KEYCODE_BACK) {
	    	int x,y;
    		this.setVisible(false);
    		visible_flag = false;
    		x = position_cur.left+position_per.left;
			if(x < 0)
		    	x = 0;
			y = position_cur.top+position_per.top;
			if(y < 0)
		   		y = 0;
			position_cur.width = position_cur.right - position_cur.left + 1;
			position_cur.height = position_cur.bottom - position_cur.top + 1;
			if((position_cur.width%2) == 1){
				position_cur.width--;
	    	}
	    	if((position_cur.height%2) == 1){
	    		position_cur.height--;
	    	}
	    	if((String.valueOf(x).equals(pre_output_x)) 
	    			&& (String.valueOf(y).equals(pre_output_y)) 
	    			&& (String.valueOf(position_cur.width).equals(pre_output_width)) 
	    			&& (String.valueOf(position_cur.height).equals(pre_output_height))){
	    	    exitHandler.postDelayed(exit_runnable, 800);
	    	}
	    	else{
				if(Utils.platformHas1080Scale() == 2){
	            	Intent intent = new Intent(PositionSetting.this, DisplayPositionSetConfirm.class);
	            	Bundle bundle = new Bundle();
	            	bundle.putInt("get_operation", GET_USER_OPERATION);
	            	intent.putExtras(bundle);
	        		startActivityForResult(intent, GET_USER_OPERATION);
				}
				else{
					exitNoHandler.postDelayed(exitNo_runnable, 800);
				}
	    	}
		}
    	else if (keyCode == KeyEvent.KEYCODE_DPAD_UP) {
    		if(zoom_flag){
				mtopBtn.setBackgroundResource(R.drawable.position_button_up_hl);
    			if(position_cur.top > (-position_per.top)){
    				position_cur.top -= zoom_pixel;
    				if(position_cur.top < (-position_per.top)){
    					position_cur.top = -position_per.top;
    				}
	    			setVideoAxis(position_cur.left, position_cur.top, position_cur.right, position_cur.bottom);
    			}
    		}
    		else{
				mtopBtn.setBackgroundResource(R.drawable.position_button_down_hl);
    			if(position_cur.top < (outputsize.height_min - position_per.top)){
	    			position_cur.top += zoom_pixel;
	    			if(position_cur.top > (outputsize.height_min - position_per.top)){
	    				position_cur.top = outputsize.height_min - position_per.top;
	    			}
	    			setVideoAxis(position_cur.left, position_cur.top, position_cur.right, position_cur.bottom);
    			}
    		}
    	}
    	else if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {
    		if(zoom_flag){
				mbottomBtn.setBackgroundResource(R.drawable.position_button_down_hl);
    			if(position_cur.bottom < (outputsize.height - position_per.top)){
    				position_cur.bottom += zoom_pixel;
    				if(position_cur.bottom > (outputsize.height - position_per.top)){
    					position_cur.bottom = outputsize.height - position_per.top;
    				}
	    			setVideoAxis(position_cur.left, position_cur.top, position_cur.right, position_cur.bottom);
    			}
    		}
    		else{
    			mbottomBtn.setBackgroundResource(R.drawable.position_button_up_hl);
    			if(position_cur.bottom > (outputsize.height - position_per.top - outputsize.height_min)){
	    			position_cur.bottom -= zoom_pixel;
	    			if(position_cur.bottom < (outputsize.height - position_per.top - outputsize.height_min)){
	    				position_cur.bottom = outputsize.height - position_per.top - outputsize.height_min;
	    			}
	    			setVideoAxis(position_cur.left, position_cur.top, position_cur.right, position_cur.bottom);
    			}
    		}
    	}
    	else if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
    		if(zoom_flag){
				mleftBtn.setBackgroundResource(R.drawable.position_button_left_hl);
    			if(position_cur.left > (-position_per.left)){
    				position_cur.left -= zoom_pixel;
    				if(position_cur.left < (-position_per.left)){
    					position_cur.left = -position_per.left;
    				}
	    			setVideoAxis(position_cur.left, position_cur.top, position_cur.right, position_cur.bottom);
    			}
    		}
    		else{
    			mleftBtn.setBackgroundResource(R.drawable.position_button_right_hl);
    			if(position_cur.left < (outputsize.width_min - position_per.left)){
	    			position_cur.left += zoom_pixel;
	    			if(position_cur.left > (outputsize.width_min - position_per.left)){
	    				position_cur.left = outputsize.width_min - position_per.left;
	    			}
	    			setVideoAxis(position_cur.left, position_cur.top, position_cur.right, position_cur.bottom);
    			}
    		}
    	}
    	else if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {
    		if(zoom_flag){
				mrightBtn.setBackgroundResource(R.drawable.position_button_right_hl);
    			if(position_cur.right < (outputsize.width - position_per.left)){
    				position_cur.right += zoom_pixel;
    				if(position_cur.right > (outputsize.width - position_per.left)){
    					position_cur.right = outputsize.width - position_per.left;
    				}
	    			setVideoAxis(position_cur.left, position_cur.top, position_cur.right, position_cur.bottom);
    			}
    		}
    		else{
				mrightBtn.setBackgroundResource(R.drawable.position_button_left_hl);
    			if(position_cur.right > (outputsize.width - position_per.left - outputsize.width_min)){
	    			position_cur.right -= zoom_pixel;
	    			if(position_cur.right < (outputsize.width - position_per.left - outputsize.width_min)){
	    				position_cur.right = outputsize.width - position_per.left - outputsize.width_min;
	    			}
	    			setVideoAxis(position_cur.left, position_cur.top, position_cur.right, position_cur.bottom);
    			}
    		}
    	}
    	else if (keyCode == KeyEvent.KEYCODE_DPAD_CENTER){
    		mchangeZoomBtn.setBackgroundResource(R.drawable.position_button_zoom_hl);
    		if(zoom_flag == true)
    		{
				//mchangeZoomBtn.setBackgroundResource(R.drawable.position_button_zoomout);
				mleftBtn.setBackgroundResource(R.drawable.position_button_right);
				mrightBtn.setBackgroundResource(R.drawable.position_button_left);
				mtopBtn.setBackgroundResource(R.drawable.position_button_down);
				mbottomBtn.setBackgroundResource(R.drawable.position_button_up);
    			zoom_flag = false;
    		}
    		else
    		{
    			//mchangeZoomBtn.setBackgroundResource(R.drawable.position_button_zoomin);
				mleftBtn.setBackgroundResource(R.drawable.position_button_left);
				mrightBtn.setBackgroundResource(R.drawable.position_button_right);
				mtopBtn.setBackgroundResource(R.drawable.position_button_up);
				mbottomBtn.setBackgroundResource(R.drawable.position_button_down);
    			zoom_flag = true;
    		}
    	}
    	else if (keyCode == KeyEvent.KEYCODE_MENU){
    		this.setVisible(false);
			if(Utils.platformHas1080Scale() == 2){
            	Intent intent = new Intent(PositionSetting.this, DisplayPositionSetConfirm.class);
            	Bundle bundle = new Bundle();
            	bundle.putInt("get_operation", GET_DEFAULT_OPERATION);
            	intent.putExtras(bundle);
        		startActivityForResult(intent, GET_DEFAULT_OPERATION);
			}
			else{
				exitDefaultHandler.postDelayed(exitDefault_runnable, 500);
			}
    	}
		return true;
	}

	private class PositionCoor {   
		  private int left;   
		  private int top;   
		  private int right;   
		  private int bottom;   
		  private int width;   
		  private int height;   
	}
	
	private class Outputsize {   
		  private int width_min;   
		  private int height_min; 
		  private int width_max;   
		  private int height_max; 
		  private int width;   
		  private int height;
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
	
	public void setVideoAxis(String videoAxis){
	 	File videoAxisFile = new File(VideoAxisFile);
        try {
        	BufferedWriter out = new BufferedWriter(new FileWriter(videoAxisFile), 32);
        	try {
        		Log.d(TAG,"setVideoAxis: "+videoAxis);
        		out.write(""+videoAxis);
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

	public void setDisplayAxis(int xStart, int yStart, int width, int height){
	 	File displayAxisFile = new File(DisplayAxisFile);
        try {
        	BufferedWriter out = new BufferedWriter(new FileWriter(displayAxisFile), 32);
        	try {
    			Log.d(TAG,"setDisplayAxis: "+xStart+" "+yStart+" "+width+" "+height+" "+xStart+" "+yStart+" 18 18");
    			out.write(""+xStart+" "+yStart+" "+width+" "+height+" "+xStart+" "+yStart+" 18 18");
        	} 
    		finally {
    			out.close();
    		}
        }
        catch (IOException e) {
    	// TODO Auto-generated catch block
        	Log.e(TAG, "IOException when write "+displayAxisFile);
        }
	}
	
	public void setDisplayAxis(String displayAxis){
	 	File displayAxisFile = new File(DisplayAxisFile);
        try {
        	BufferedWriter out = new BufferedWriter(new FileWriter(displayAxisFile), 32);
        	try {
    			Log.d(TAG,"setDisplayAxis: "+displayAxis);
    			out.write(""+displayAxis);
        	} 
    		finally {
    			out.close();
    		}
        }
        catch (IOException e) {
    	// TODO Auto-generated catch block
        	Log.e(TAG, "IOException when write "+displayAxisFile);
        }
	}

	public void setFreeScaleAxis(int xStart, int yStart, int xEnd, int yEnd){
		File freeScaleAxisFile = new File(FreeScaleAxisFile);
        try {
        	BufferedWriter out = new BufferedWriter(new FileWriter(FreeScaleAxisFile), 32);
        	try {
        		Log.d(TAG,"setFreeScaleAxis: "+xStart+" "+yStart+" "+xEnd+" "+yEnd);
        		out.write(""+xStart+" "+yStart+" "+xEnd+" "+yEnd);
        	}
    		finally {
    			out.close();
    		}
       	}
       	catch (IOException e) {
   		// TODO Auto-generated catch block
    		Log.e(TAG, "IOException when write "+freeScaleAxisFile);
    	}
	}

	public void setFreeScaleAxis(String freeScaleAxis){
		File freeScaleAxisFile = new File(FreeScaleAxisFile);
        try {
        	BufferedWriter out = new BufferedWriter(new FileWriter(FreeScaleAxisFile), 32);
        	try {
        		Log.d(TAG,"setFreeScaleAxis: "+freeScaleAxis);
        		out.write(""+freeScaleAxis);
        	}
    		finally {
    			out.close();
    		}
       	}
       	catch (IOException e) {
   		// TODO Auto-generated catch block
    		Log.e(TAG, "IOException when write "+freeScaleAxisFile);
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

    public void setVideoEnable(boolean enable, boolean mdefault){
    	File videoEnbaleFile = new File(VideoEnableFile);
        try {
        	BufferedWriter out = new BufferedWriter(new FileWriter(videoEnbaleFile), 32);
       		if(enable){
	        	try {
	        		if(mdefault){
		    			Log.d(TAG,"setVideoEnable: "+videoEnableFile_init);
		    			out.write(""+videoEnableFile_init);
	        		}
	        		else{
		    			Log.d(TAG,"setVideoEnable: m 0x1d26 0x144f1");
		    			out.write("m 0x1d26 0x144f1");
	        		}
	        	}
	    		finally {
	    			out.close();
	   			}
       		}
       		else{
	        	try {
	    			Log.d(TAG,"setVideoEnable: m 0x1d26 0x104f1");
	    			out.write("m 0x1d26 0x104f1");
	        	}
	    		finally {
	    			out.close();
	   			}
       		}
       	}
       	catch (IOException e) {
   		// TODO Auto-generated catch block
    		Log.e(TAG, "IOException when write "+videoEnbaleFile);
    	}
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
	
    public String getDevice(String device){
    	//read
		String deviceContent = null;
		try {
			BufferedReader displayAxisFile_in = new BufferedReader(new FileReader(device), 32);
			try {
				deviceContent = displayAxisFile_in.readLine();
				Log.d(TAG, ""+device+" content:"+deviceContent);
			} 
			finally {
				displayAxisFile_in.close();
    		} 
		}
		catch(IOException e) {
			// TODO Auto-generated catch block
			Log.e(TAG, "IOException when read "+device);
		}
		return deviceContent;
    }
    
	private void getOutputsize(){
		if((curOutputmode.equals(outputmode_array[0])) || (curOutputmode.equals(outputmode_array[1]))){
			outputsize.width_min = (int)(720*outputsize_per);
			outputsize.width_max = (int)(720*(1 + outputsize_per));
			outputsize.width = OUTPUT480_FULL_WIDTH;
			outputsize.height_min = (int)(480*outputsize_per);
			outputsize.height_max = (int)(480*(1 + outputsize_per));
			outputsize.height = OUTPUT480_FULL_HEIGHT;
		}
		else if((curOutputmode.equals(outputmode_array[2])) || (curOutputmode.equals(outputmode_array[3]))){
			outputsize.width_min = (int)(720*outputsize_per);
			outputsize.width_max = (int)(720*(1 + outputsize_per));
			outputsize.width = OUTPUT576_FULL_WIDTH;
			outputsize.height_min = (int)(576*outputsize_per);
			outputsize.height_max = (int)(576*(1 + outputsize_per));
			outputsize.height = OUTPUT576_FULL_HEIGHT;
		}
		else if(curOutputmode.equals(outputmode_array[4])){
			outputsize.width_min = (int)(1280*outputsize_per);
			outputsize.width_max = (int)(1280*(1 + outputsize_per));
			outputsize.width = OUTPUT720_FULL_WIDTH;
			outputsize.height_min = (int)(720*outputsize_per);
			outputsize.height_max = (int)(720*(1 + outputsize_per));
			outputsize.height = OUTPUT720_FULL_HEIGHT;
		}
		else if((curOutputmode.equals(outputmode_array[5])) || (curOutputmode.equals(outputmode_array[6]))){
			outputsize.width_min = (int)(1920*outputsize_per);
			outputsize.width_max = (int)(1920*(1 + outputsize_per));
			outputsize.width = OUTPUT1080_FULL_WIDTH;
			outputsize.height_min = (int)(1080*outputsize_per);
			outputsize.height_max = (int)(1080*(1 + outputsize_per));
			outputsize.height = OUTPUT1080_FULL_HEIGHT;
		}
	}
	
	private void getOutput(String get_outputmode){
		if(get_outputmode.equals(outputmode_array[0])){
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
		}
		else if(get_outputmode.equals(outputmode_array[1])){
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
		}
		else if(get_outputmode.equals(outputmode_array[2])){
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
		}
		else if(get_outputmode.equals(outputmode_array[3])){
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
		}
		else if(get_outputmode.equals(outputmode_array[4])){
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
		}
		else if(get_outputmode.equals(outputmode_array[5])){
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
		}
		else if(get_outputmode.equals(outputmode_array[6])){
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
		}
		else{
	/*		pre_output_x = SystemProperties.get(sel_720poutput_x);
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
				pre_output_height = String.valueOf(OUTPUT720_FULL_HEIGHT);*/
		}
	}
	
	public void setOutput(String set_outputmode, String set_outputx, String set_outputy, String set_outputwidth, String set_outputheight){
		if(set_outputmode.equals(outputmode_array[0])){
			SystemProperties.set(sel_480ioutput_x, set_outputx);
			SystemProperties.set(sel_480ioutput_y, set_outputy);
			SystemProperties.set(sel_480ioutput_width, set_outputwidth);
			SystemProperties.set(sel_480ioutput_height, set_outputheight);
		}
		else if(set_outputmode.equals(outputmode_array[1])){
			SystemProperties.set(sel_480poutput_x, set_outputx);
			SystemProperties.set(sel_480poutput_y, set_outputy);
			SystemProperties.set(sel_480poutput_width, set_outputwidth);
			SystemProperties.set(sel_480poutput_height, set_outputheight);		
		}
		else if(set_outputmode.equals(outputmode_array[2])){
			SystemProperties.set(sel_576ioutput_x, set_outputx);
			SystemProperties.set(sel_576ioutput_y, set_outputy);
			SystemProperties.set(sel_576ioutput_width, set_outputwidth);
			SystemProperties.set(sel_576ioutput_height, set_outputheight);
		}
		else if(set_outputmode.equals(outputmode_array[3])){
			SystemProperties.set(sel_576poutput_x, set_outputx);
			SystemProperties.set(sel_576poutput_y, set_outputy);
			SystemProperties.set(sel_576poutput_width, set_outputwidth);
			SystemProperties.set(sel_576poutput_height, set_outputheight);		
		}
		else if(set_outputmode.equals(outputmode_array[4])){
			SystemProperties.set(sel_720poutput_x, set_outputx);
			SystemProperties.set(sel_720poutput_y, set_outputy);
			SystemProperties.set(sel_720poutput_width, set_outputwidth);
			SystemProperties.set(sel_720poutput_height, set_outputheight);
		}	
		else if(set_outputmode.equals(outputmode_array[5])){
			SystemProperties.set(sel_1080ioutput_x, set_outputx);
			SystemProperties.set(sel_1080ioutput_y, set_outputy);
			SystemProperties.set(sel_1080ioutput_width, set_outputwidth);
			SystemProperties.set(sel_1080ioutput_height, set_outputheight);
		}
		else if(set_outputmode.equals(outputmode_array[6])){
			SystemProperties.set(sel_1080poutput_x, set_outputx);
			SystemProperties.set(sel_1080poutput_y, set_outputy);
			SystemProperties.set(sel_1080poutput_width, set_outputwidth);
			SystemProperties.set(sel_1080poutput_height, set_outputheight);			
		}
	}
	
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
	}
	
    @Override
	protected  void onActivityResult(int requestCode,int resultCode,Intent data){
		super.onActivityResult(requestCode,resultCode,data);
		int x,y;
	   	x = position_cur.left+position_per.left;
		if(x < 0)
	   		x = 0;
		y = position_cur.top+position_per.top;
		if(y < 0)
	   		y = 0;
		if((position_cur.width%2) == 1){
			position_cur.width--;
    	}
    	if((position_cur.height%2) == 1){
    		position_cur.height--;
    	}
		switch(requestCode){
			case (GET_USER_OPERATION):
				if(resultCode==Activity.RESULT_OK)
					{
		        	try{
		        		setOutput(curOutputmode, String.valueOf(x), String.valueOf(y), String.valueOf(position_cur.width), String.valueOf(position_cur.height));
        				Log.i(TAG,"--------------------------------position Set");
        				Log.d(TAG,"--------------------------------set display axis x = "+x);
        				Log.d(TAG,"--------------------------------set display axis y = "+y);
        				Log.d(TAG,"--------------------------------set display axis width = "+position_cur.width);
        				Log.d(TAG,"--------------------------------set display axis height = "+position_cur.height);
	
		        		if(Utils.platformHas1080Scale() != 2){
	        			    setVideoDisable(0);
	        				SystemProperties.set("ctl.start", "display_reset");
	        				String ret = SystemProperties.get("init.svc.display_reset", "");
	        				if(ret != null && ret.equals("stopped"))
	        				{
	        					  Log.i(TAG,"--------------------------------reboot android");
	        					  //return true;
	        				}
		        		}
		        		else{
		        			Intent intent = new Intent();
			        		Bundle bundle = new Bundle();
			        		bundle.putInt("selectedItemPosition", selectedItemPosition);
			        		intent.setClass(PositionSetting.this, DisplaySettings.class);
			        		intent.putExtras(bundle);
			        		startActivity(intent);
			        		PositionSetting.this.finish();
		        		}
        			}
        			catch(Exception e){
        				Log.i(TAG,"--------------------------------setOutput_position No set");
                        Log.e(TAG, "Exception Occured: Trying to add set setflag : " +
                                e.toString());
        			    Intent intent = new Intent();
        			    Bundle bundle = new Bundle();
        				bundle.putInt("selectedItemPosition", selectedItemPosition);
        				intent.setClass(PositionSetting.this, DisplaySettings.class);
        				intent.putExtras(bundle);
        				startActivity(intent);
        				PositionSetting.this.finish();
                        Log.e(TAG, "Finishing the Application");
        			}
                	Log.i(TAG,"----------------------yes");
					
					}
				else if(resultCode==Activity.RESULT_CANCELED)
					{
						setVideoAxis(videoAxisFile_init);
				    	Intent intent = new Intent();
				    	Bundle bundle = new Bundle();
						bundle.putInt("selectedItemPosition", selectedItemPosition);
						intent.setClass(PositionSetting.this, DisplaySettings.class);
						intent.putExtras(bundle);
				    	startActivity(intent);
				    	PositionSetting.this.finish();
                		Log.i(TAG,"----------------------no");
					}
				break;
			case (GET_DEFAULT_OPERATION):
				if(resultCode==Activity.RESULT_OK)
				{
	        	try{
	        		String width_d,height_d; 
	        		if((curOutputmode.equals(outputmode_array[0])) || (curOutputmode.equals(outputmode_array[1]))){
	        			width_d = String.valueOf(OUTPUT480_FULL_WIDTH);
	        			height_d = String.valueOf(OUTPUT480_FULL_HEIGHT);
	        		}
	        		else if((curOutputmode.equals(outputmode_array[2])) || (curOutputmode.equals(outputmode_array[3]))){
	        			width_d = String.valueOf(OUTPUT576_FULL_WIDTH);
	        			height_d = String.valueOf(OUTPUT576_FULL_HEIGHT);
	        		}
	        		else if(curOutputmode.equals(outputmode_array[4])){
	        			width_d = String.valueOf(OUTPUT720_FULL_WIDTH);
	        			height_d = String.valueOf(OUTPUT720_FULL_HEIGHT);
	        		}
	        		else if((curOutputmode.equals(outputmode_array[5])) || (curOutputmode.equals(outputmode_array[6]))){
	        			width_d = String.valueOf(OUTPUT1080_FULL_WIDTH);
	        			height_d = String.valueOf(OUTPUT1080_FULL_HEIGHT);
	        		}
	        		else{
	        			width_d = String.valueOf(OUTPUT720_FULL_WIDTH);
	        			height_d = String.valueOf(OUTPUT720_FULL_HEIGHT);
	        		}
	        		setOutput(curOutputmode, "0", "0", width_d, height_d);
    				Log.i(TAG,"--------------------------------default position Set");

	        		if(Utils.platformHas1080Scale() != 2){
	    			    setVideoDisable(0);
	    				SystemProperties.set("ctl.start", "display_reset");
	    				String ret = SystemProperties.get("init.svc.display_reset", "");
	    				if(ret != null && ret.equals("stopped"))
	    				{
	    					  Log.i(TAG,"--------------------------------reboot android");
	    					  //return true;
	    				}
	        		}
	        		else{
	        			setVideoAxis(0,0,Integer.valueOf(width_d).intValue()-1,Integer.valueOf(height_d).intValue()-1);
	        			Intent intent = new Intent();
		        		Bundle bundle = new Bundle();
		        		bundle.putInt("selectedItemPosition", selectedItemPosition);
		        		intent.setClass(PositionSetting.this, DisplaySettings.class);
		        		intent.putExtras(bundle);
		        		startActivity(intent);
		        		PositionSetting.this.finish();
	        		}
    			}
    			catch(Exception e){
    				Log.i(TAG,"--------------------------------setOutput_position No set");
                    Log.e(TAG, "Exception Occured: Trying to add set setflag : " +
                            e.toString());
    			    Intent intent = new Intent();
    			    Bundle bundle = new Bundle();
    				bundle.putInt("selectedItemPosition", selectedItemPosition);
    				intent.setClass(PositionSetting.this, DisplaySettings.class);
    				intent.putExtras(bundle);
    				startActivity(intent);
    				PositionSetting.this.finish();
                    Log.e(TAG, "Finishing the Application");
    			}
            	Log.i(TAG,"----------------------yes");
				
				}
			else if(resultCode==Activity.RESULT_CANCELED)
				{
					setVideoAxis(videoAxisFile_init);
			    	Intent intent = new Intent();
			    	Bundle bundle = new Bundle();
					bundle.putInt("selectedItemPosition", selectedItemPosition);
					intent.setClass(PositionSetting.this, DisplaySettings.class);
					intent.putExtras(bundle);
			    	startActivity(intent);
			    	PositionSetting.this.finish();
            		Log.i(TAG,"----------------------no");
				}
			break;
		}
		if((Utils.platformHas1080Scale() == 0) 
				|| ((Utils.platformHas1080Scale() == 1) && (!curOutputmode.equals("1080i")) && (!curOutputmode.equals("1080p")) && (!curOutputmode.equals("720p")))){
			Log.i(TAG,"no freescale mode!\n");
		}
		else{
			writeFile(PpscalerFile,"1");
		}
	}
}