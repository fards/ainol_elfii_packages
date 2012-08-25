#include <string.h>
#include <jni.h>
#include <linux/fb.h>
#include <fcntl.h>
#include <stdlib.h>

#include <cutils/log.h>

#include <sys/system_properties.h>

#include "outputmodeswitchjni.h"

#define  FBIOPUT_OSD_FREE_SCALE_ENABLE	0x4504
#define  FBIOPUT_OSD_FREE_SCALE_WIDTH		0x4505
#define  FBIOPUT_OSD_FREE_SCALE_HEIGHT	0x4506

struct fb_var_screeninfo vinfo;
char vaxis_oldx_str[8];
char vaxis_oldy_str[8];
char vaxis_newx_str[8];
char vaxis_newy_str[8];
char vaxis_width_str[8];
char vaxis_height_str[8];
char vaxis_str[32];
char daxis_str[32];
char daxispre_str[32];
char scale1080_str[32];

int freeScale(int mode) {
	int fd0, fd1;
	int fd_daxis, fd_vaxis;
	int fd_fb;
	int fd_video;	
	int osd_width, osd_height;	
	int ret = -1;
	int fd_dmode;
	
	LOGI("freeScale: mode=%d", mode);
	if((fd0 = open("/dev/graphics/fb0", O_RDWR)) < 0) {
		LOGI("open /dev/graphics/fb0 fail.");
		goto exit;
	}
	if((fd1 = open("/dev/graphics/fb1", O_RDWR)) < 0) {
		LOGI("open /dev/graphics/fb1 fail.");
		goto exit;		
	}
	if((fd_vaxis = open("/sys/class/video/axis", O_RDWR)) < 0) {
		LOGI("open /sys/class/video/axis fail.");
		goto exit;	
	}
		
	if((fd_daxis = open("/sys/class/display/axis", O_RDWR)) < 0) {
		LOGI("open /sys/class/display/axis fail.");
		goto exit;	
	}	

	if((fd_fb = open("/dev/graphics/fb0", O_RDWR)) < 0) {
		LOGI("open /dev/graphics/fb0 fail.");
		goto exit;
	}
	
	if((fd_video = open("/sys/class/video/disable_video", O_RDWR)) < 0) {
		LOGI("open /sys/class/video/disable_video fail.");
	}
	
	if((fd_dmode = open("/sys/class/display/mode", O_RDWR)) < 0) {
		LOGI("open /sys/class/display/mode fail.");
	}
		
	memset(daxis_str,0,32);	
	if(ioctl(fd_fb, FBIOGET_VSCREENINFO, &vinfo) == 0) {
		osd_width = vinfo.xres;
		osd_height = vinfo.yres;
		sprintf(daxis_str, "0 0 %d %d 0 0 18 18", vinfo.xres, vinfo.yres);
																									
		//LOGI("osd_width = %d", osd_width);
		//LOGI("osd_height = %d", osd_height);
	} else {
		LOGI("get FBIOGET_VSCREENINFO fail.");
		goto exit;
	}

	LOGI("set mid mode=%d", mode);
	LOGI("set mid osd_width=%d", osd_width);
	LOGI("set mid osd_height=%d", osd_height);
	switch(mode) {
		case 0: //480p
		case 1: //480i
			write(fd_vaxis, "20 10 680 460", strlen("20 10 680 460"));
			write(fd_daxis, daxis_str, strlen(daxis_str));
			write(fd_dmode, "480p", strlen("480p"));
			ioctl(fd0,FBIOPUT_OSD_FREE_SCALE_ENABLE,0);
			ioctl(fd1,FBIOPUT_OSD_FREE_SCALE_ENABLE,0);
			ioctl(fd0,FBIOPUT_OSD_FREE_SCALE_WIDTH,osd_width);
			ioctl(fd0,FBIOPUT_OSD_FREE_SCALE_HEIGHT,osd_height); 
			ioctl(fd1,FBIOPUT_OSD_FREE_SCALE_WIDTH,osd_width);
			ioctl(fd1,FBIOPUT_OSD_FREE_SCALE_HEIGHT,osd_height);
			if (fd_video >= 0) 	write(fd_video, "1", strlen("1"));
			ioctl(fd0,FBIOPUT_OSD_FREE_SCALE_ENABLE,1);
			ioctl(fd1,FBIOPUT_OSD_FREE_SCALE_ENABLE,1);	
			
			ret = 0;
			break;
		case 2: //576p
		case 3: //576ii
			write(fd_vaxis, "20 10 680 556", strlen("20 10 680 556"));
			write(fd_daxis, daxis_str, strlen(daxis_str));
			write(fd_dmode, "576p", strlen("576p"));
			ioctl(fd0,FBIOPUT_OSD_FREE_SCALE_ENABLE,0);
			ioctl(fd1,FBIOPUT_OSD_FREE_SCALE_ENABLE,0);
			ioctl(fd0,FBIOPUT_OSD_FREE_SCALE_WIDTH,osd_width);
			ioctl(fd0,FBIOPUT_OSD_FREE_SCALE_HEIGHT,osd_height); 
			ioctl(fd1,FBIOPUT_OSD_FREE_SCALE_WIDTH,osd_width);
			ioctl(fd1,FBIOPUT_OSD_FREE_SCALE_HEIGHT,osd_height);
			if (fd_video >= 0)	write(fd_video, "1", strlen("1"));
			ioctl(fd0,FBIOPUT_OSD_FREE_SCALE_ENABLE,1);
			ioctl(fd1,FBIOPUT_OSD_FREE_SCALE_ENABLE,1); 
			
			ret = 0;
			break;

		case 4: //720p
			write(fd_vaxis, "20 15 1240 690", strlen("20 15 1240 690"));
			write(fd_daxis, daxis_str, strlen(daxis_str));
			write(fd_dmode, "720p", strlen("720p"));
			ioctl(fd0,FBIOPUT_OSD_FREE_SCALE_ENABLE,0);
			ioctl(fd1,FBIOPUT_OSD_FREE_SCALE_ENABLE,0);
			ioctl(fd0,FBIOPUT_OSD_FREE_SCALE_WIDTH,osd_width);
			ioctl(fd0,FBIOPUT_OSD_FREE_SCALE_HEIGHT,osd_height); 
			ioctl(fd1,FBIOPUT_OSD_FREE_SCALE_WIDTH,osd_width);
			ioctl(fd1,FBIOPUT_OSD_FREE_SCALE_HEIGHT,osd_height);
			if (fd_video >= 0) 	write(fd_video, "1", strlen("1"));	
			ioctl(fd0,FBIOPUT_OSD_FREE_SCALE_ENABLE,1);
			ioctl(fd1,FBIOPUT_OSD_FREE_SCALE_ENABLE,1);	
			
			ret = 0;
			break;
		case 5: //1080i			
		case 6: //1080p
			write(fd_vaxis, "20 10 1880 1060", strlen("20 10 1880 1060"));
			write(fd_daxis, daxis_str, strlen(daxis_str));
			write(fd_dmode, "1080i", strlen("1080i"));
			ioctl(fd0,FBIOPUT_OSD_FREE_SCALE_ENABLE,0);
			ioctl(fd1,FBIOPUT_OSD_FREE_SCALE_ENABLE,0);
			ioctl(fd0,FBIOPUT_OSD_FREE_SCALE_WIDTH,osd_width);
			ioctl(fd0,FBIOPUT_OSD_FREE_SCALE_HEIGHT,osd_height); 
			ioctl(fd1,FBIOPUT_OSD_FREE_SCALE_WIDTH,osd_width);
			ioctl(fd1,FBIOPUT_OSD_FREE_SCALE_HEIGHT,osd_height);
			if (fd_video >= 0) 	write(fd_video, "1", strlen("1"));	
			ioctl(fd0,FBIOPUT_OSD_FREE_SCALE_ENABLE,1);
			ioctl(fd1,FBIOPUT_OSD_FREE_SCALE_ENABLE,1);	
			
			ret = 0;
			break;	
		default:			
			break;		
			
	}
	
exit:	
	close(fd0);
	close(fd1);
	close(fd_vaxis);
	close(fd_daxis);	
	close(fd_fb);
	close(fd_video);
	close(fd_dmode);
	return ret;
}
	

JNIEXPORT jint JNICALL Java_com_android_settings_OutputSetConfirm_freeScaleSetModeJni( JNIEnv * env,
																									jobject thiz, jint mode )
{	
		return freeScale(mode);
}						

int DisableFreeScale(int mode) {
	int fd0, fd1;
	int fd_daxis, fd_vaxis, fb_ppscaler;
	int fd_fb;	
	int osd_width, osd_height;	
	int ret = -1;
	int fd_dmode;
	//LOGI("DisableFreeScale: mode=%d", mode);	
	//if(mode == 0) return 0;		
		
	if((fd0 = open("/dev/graphics/fb0", O_RDWR)) < 0) {
		LOGI("open /dev/graphics/fb0 fail.");
		goto exit;
	}
	if((fd1 = open("/dev/graphics/fb1", O_RDWR)) < 0) {
		LOGI("open /dev/graphics/fb1 fail.");
		goto exit;	
	}
	if((fd_vaxis = open("/sys/class/video/axis", O_RDWR)) < 0) {
		LOGI("open /sys/class/video/axis fail.");
		goto exit;	
	}
		
	if((fd_daxis = open("/sys/class/display/axis", O_RDWR)) < 0) {
		LOGI("open /sys/class/display/axis fail.");
		goto exit;
	}	
	
	if((fd_fb = open("/dev/graphics/fb0", O_RDWR)) < 0) {
		LOGI("open /dev/graphics/fb0 fail.");
		goto exit;
	}	
	
	if((fd_dmode = open("/sys/class/display/mode", O_RDWR)) < 0) {
		LOGI("open /sys/class/display/mode fail.");
	}
	
	if((fb_ppscaler = open("/sys/class/ppmgr/ppscaler", O_RDWR)) < 0) {
		LOGI("open /sys/class/display/mode fail.");
	}
	memset(daxis_str,0,32);	
	if(ioctl(fd_fb, FBIOGET_VSCREENINFO, &vinfo) == 0) {
		osd_width = vinfo.xres;
		osd_height = vinfo.yres;

		LOGI("osd_width = %d", osd_width);
		LOGI("osd_height = %d", osd_height);
	} else {
		LOGI("get FBIOGET_VSCREENINFO fail.");
		goto exit;
	}
	char output_array[7][32]={"480i","480p","576i","576p","720p","1080i","1080p"};
	LOGI("set mid mode=%d", mode);
	if((strcmp(scale1080_str,"1") == 0)&&((mode == 4)||(mode == 5)||(mode == 6)))
	{	
		switch(mode) 
		{
			case 4: //720p
			  property_get("ubootenv.var.720poutputx",vaxis_newx_str,"0");
			  property_get("ubootenv.var.720poutputy",vaxis_newy_str,"0");
			  property_get("ubootenv.var.720poutputwidth",vaxis_width_str,"1280");
			  property_get("ubootenv.var.720poutputheight",vaxis_height_str,"720");
				break;	
			case 5: //1080i
			  property_get("ubootenv.var.1080ioutputx",vaxis_newx_str,"0");
			  property_get("ubootenv.var.1080ioutputy",vaxis_newy_str,"0");
			  property_get("ubootenv.var.1080ioutputwidth",vaxis_width_str,"1920");
			  property_get("ubootenv.var.1080ioutputheight",vaxis_height_str,"1080");
				break;				
			case 6: //1080p
			  property_get("ubootenv.var.1080poutputx",vaxis_newx_str,"0");
			  property_get("ubootenv.var.1080poutputy",vaxis_newy_str,"0");
			  property_get("ubootenv.var.1080poutputwidth",vaxis_width_str,"1920");
			  property_get("ubootenv.var.1080poutputheight",vaxis_height_str,"1080");
				break;	
			default:			
				break;
		}
		int vaxis_newx, vaxis_newy, vaxis_width, vaxis_height;
		vaxis_newx = atoi(vaxis_newx_str);
		vaxis_newy = atoi(vaxis_newy_str);
		vaxis_width = atoi(vaxis_width_str);
		vaxis_height = atoi(vaxis_height_str);
		
		vaxis_width = vaxis_width + vaxis_newx;
		vaxis_height = vaxis_height + vaxis_newy;
		LOGI("set video axis:%d %d %d %d", vaxis_newx, vaxis_newy, vaxis_width, vaxis_height);
		sprintf(vaxis_str, "%d %d %d %d", vaxis_newx, vaxis_newy, vaxis_width, vaxis_height);
		write(fd_vaxis, vaxis_str, strlen(vaxis_str));
	  write(fb_ppscaler, "1", strlen("1"));
	}
	else if(strcmp(scale1080_str,"2") == 0)
	{
		switch(mode) 
		{
			case 0: //480i
			  property_get("ubootenv.var.480ioutputx",vaxis_newx_str,"0");
			  property_get("ubootenv.var.480ioutputy",vaxis_newy_str,"0");
			  property_get("ubootenv.var.480ioutputwidth",vaxis_width_str,"720");
			  property_get("ubootenv.var.480ioutputheight",vaxis_height_str,"480");
				break;
			case 1: //480p
			  property_get("ubootenv.var.480poutputx",vaxis_newx_str,"0");
			  property_get("ubootenv.var.480poutputy",vaxis_newy_str,"0");
			  property_get("ubootenv.var.480poutputwidth",vaxis_width_str,"720");
			  property_get("ubootenv.var.480poutputheight",vaxis_height_str,"480");
				break;
			case 2: //576i
			  property_get("ubootenv.var.576ioutputx",vaxis_newx_str,"0");
			  property_get("ubootenv.var.576ioutputy",vaxis_newy_str,"0");
			  property_get("ubootenv.var.576ioutputwidth",vaxis_width_str,"720");
			  property_get("ubootenv.var.576ioutputheight",vaxis_height_str,"576");
				break;
			case 3: //576p
			  property_get("ubootenv.var.576poutputx",vaxis_newx_str,"0");
			  property_get("ubootenv.var.576poutputy",vaxis_newy_str,"0");
			  property_get("ubootenv.var.576poutputwidth",vaxis_width_str,"720");
			  property_get("ubootenv.var.576poutputheight",vaxis_height_str,"576");
				break;
			case 4: //720p
			  property_get("ubootenv.var.720poutputx",vaxis_newx_str,"0");
			  property_get("ubootenv.var.720poutputy",vaxis_newy_str,"0");
			  property_get("ubootenv.var.720poutputwidth",vaxis_width_str,"1280");
			  property_get("ubootenv.var.720poutputheight",vaxis_height_str,"720");
				break;
			case 5: //1080i
			  property_get("ubootenv.var.1080ioutputx",vaxis_newx_str,"0");
			  property_get("ubootenv.var.1080ioutputy",vaxis_newy_str,"0");
			  property_get("ubootenv.var.1080ioutputwidth",vaxis_width_str,"1920");
			  property_get("ubootenv.var.1080ioutputheight",vaxis_height_str,"1080");
				break;				
			case 6: //1080p
			  property_get("ubootenv.var.1080poutputx",vaxis_newx_str,"0");
			  property_get("ubootenv.var.1080poutputy",vaxis_newy_str,"0");
			  property_get("ubootenv.var.1080poutputwidth",vaxis_width_str,"1920");
			  property_get("ubootenv.var.1080poutputheight",vaxis_height_str,"1080");
				break;	
			default:			
				break;
		}
		int vaxis_newx, vaxis_newy, vaxis_width, vaxis_height;
		vaxis_newx = atoi(vaxis_newx_str);
		vaxis_newy = atoi(vaxis_newy_str);
		vaxis_width = atoi(vaxis_width_str);
		vaxis_height = atoi(vaxis_height_str);
		
		vaxis_width = vaxis_width + vaxis_newx;
		vaxis_height = vaxis_height + vaxis_newy;
		LOGI("set video axis:%d %d %d %d", vaxis_newx, vaxis_newy, vaxis_width, vaxis_height);
		sprintf(vaxis_str, "%d %d %d %d", vaxis_newx, vaxis_newy, vaxis_width, vaxis_height);
		write(fd_vaxis, vaxis_str, strlen(vaxis_str));
	  write(fb_ppscaler, "1", strlen("1"));
	}
	else
	{
		ioctl(fd0,FBIOPUT_OSD_FREE_SCALE_ENABLE,0);
		ioctl(fd1,FBIOPUT_OSD_FREE_SCALE_ENABLE,0);
	}
	write(fd_dmode, output_array[mode], strlen(output_array[mode]));
	if(((strcmp(scale1080_str,"1") == 0)&&((mode == 4)||(mode == 5)||(mode == 6))) || (strcmp(scale1080_str,"2") == 0))
	{
		ioctl(fd0,FBIOPUT_OSD_FREE_SCALE_ENABLE,1);
		ioctl(fd1,FBIOPUT_OSD_FREE_SCALE_ENABLE,1);
	}
	else
	{
		write(fd_daxis, daxispre_str, strlen(daxispre_str));
	}
	ret = 0;
	
exit:	
	close(fd0);
	close(fd1);
	close(fd_vaxis);
	close(fd_daxis);	
	close(fd_fb);	
	close(fd_dmode);
	return ret;

}

int EnableFreeScale(int new_mode, int old_mode) {
	int fd0, fd1;
	int fd_daxis, fd_vaxis, fb_ppscaler;
	int fd_fb;	
	int osd_width, osd_height;	
	int ret = -1;
	int fd_dmode;
	//LOGI("EnableFreeScale: mode=%d", new_mode);	
	//if(new_mode == 0) return 0;		
		
	if((fd0 = open("/dev/graphics/fb0", O_RDWR)) < 0) {
		LOGI("open /dev/graphics/fb0 fail.");
		goto exit;
	}
	if((fd1 = open("/dev/graphics/fb1", O_RDWR)) < 0) {
		LOGI("open /dev/graphics/fb1 fail.");
		goto exit;	
	}
	if((fd_vaxis = open("/sys/class/video/axis", O_RDWR)) < 0) {
		LOGI("open /sys/class/video/axis fail.");
		goto exit;		
	}
		
	if((fd_daxis = open("/sys/class/display/axis", O_RDWR)) < 0) {
		LOGI("open /sys/class/display/axis fail.");
		goto exit;
	}		
	
	if((fd_fb = open("/dev/graphics/fb0", O_RDWR)) < 0) {
		LOGI("open /dev/graphics/fb0 fail.");
		goto exit;
	}

	if((fd_dmode = open("/sys/class/display/mode", O_RDWR)) < 0) {
		LOGI("open /sys/class/display/mode fail.");
	}

	if((fb_ppscaler = open("/sys/class/ppmgr/ppscaler", O_RDWR)) < 0) {
		LOGI("open /sys/class/display/mode fail.");
	}
	memset(daxis_str,0,32);	
	memset(daxispre_str,0,32);	
	read(fd_daxis, daxispre_str, 32);
	if(ioctl(fd_fb, FBIOGET_VSCREENINFO, &vinfo) == 0) {
		osd_width = vinfo.xres;
		osd_height = vinfo.yres;
		sprintf(daxis_str, "0 0 %d %d 0 0 18 18", vinfo.xres, vinfo.yres);
		LOGI("osd_width = %d", osd_width);
		LOGI("osd_height = %d", osd_height);
	} else {
		LOGI("get FBIOGET_VSCREENINFO fail.");
		goto exit;
	}
	char output_array[7][32]={"480i","480p","576i","576p","720p","1080i","1080p"};
	LOGI("set mediabox new_mode=%d", new_mode);
	property_get("ro.platform.has.1080scale",scale1080_str,"0");
	switch(new_mode) {
		case 0: //480i
		  property_get("ubootenv.var.480ioutputx",vaxis_newx_str,"0");
		  property_get("ubootenv.var.480ioutputy",vaxis_newy_str,"0");
		  property_get("ubootenv.var.480ioutputwidth",vaxis_width_str,"720");
		  property_get("ubootenv.var.480ioutputheight",vaxis_height_str,"480");
			break;
		case 1: //480p
		  property_get("ubootenv.var.480poutputx",vaxis_newx_str,"0");
		  property_get("ubootenv.var.480poutputy",vaxis_newy_str,"0");
		  property_get("ubootenv.var.480poutputwidth",vaxis_width_str,"720");
		  property_get("ubootenv.var.480poutputheight",vaxis_height_str,"480");
			break;
		case 2: //576i
		  property_get("ubootenv.var.576ioutputx",vaxis_newx_str,"0");
		  property_get("ubootenv.var.576ioutputy",vaxis_newy_str,"0");
		  property_get("ubootenv.var.576ioutputwidth",vaxis_width_str,"720");
		  property_get("ubootenv.var.576ioutputheight",vaxis_height_str,"576");
			break;
		case 3: //576p
		  property_get("ubootenv.var.576poutputx",vaxis_newx_str,"0");
		  property_get("ubootenv.var.576poutputy",vaxis_newy_str,"0");
		  property_get("ubootenv.var.576poutputwidth",vaxis_width_str,"720");
		  property_get("ubootenv.var.576poutputheight",vaxis_height_str,"576");
			break;
		case 4: //720p
		  property_get("ubootenv.var.720poutputx",vaxis_newx_str,"0");
		  property_get("ubootenv.var.720poutputy",vaxis_newy_str,"0");
		  property_get("ubootenv.var.720poutputwidth",vaxis_width_str,"1280");
		  property_get("ubootenv.var.720poutputheight",vaxis_height_str,"720");
			break;
		case 5: //1080i
		  property_get("ubootenv.var.1080ioutputx",vaxis_newx_str,"0");
		  property_get("ubootenv.var.1080ioutputy",vaxis_newy_str,"0");
		  property_get("ubootenv.var.1080ioutputwidth",vaxis_width_str,"1920");
		  property_get("ubootenv.var.1080ioutputheight",vaxis_height_str,"1080");
			break;				
		case 6: //1080p
		  property_get("ubootenv.var.1080poutputx",vaxis_newx_str,"0");
		  property_get("ubootenv.var.1080poutputy",vaxis_newy_str,"0");
		  property_get("ubootenv.var.1080poutputwidth",vaxis_width_str,"1920");
		  property_get("ubootenv.var.1080poutputheight",vaxis_height_str,"1080");
			break;	
		default:			
			LOGI("UNKNOW MODE:%d", new_mode);
			break;					
	}
	LOGI("old mediabox mode=%d", old_mode);
	switch(old_mode) {
		case 0: //480i
			if(strcmp(scale1080_str,"2") == 0)
			{
				strcpy(vaxis_oldx_str,"0");
				strcpy(vaxis_oldy_str,"0");
			}
			else
			{
		  	property_get("ubootenv.var.480ioutputx",vaxis_oldx_str,"0");
		  	property_get("ubootenv.var.480ioutputy",vaxis_oldy_str,"0");
		  }
			break;
		case 1: //480p
			if(strcmp(scale1080_str,"2") == 0)
			{
				strcpy(vaxis_oldx_str,"0");
				strcpy(vaxis_oldy_str,"0");
			}
			else
			{
			  property_get("ubootenv.var.480poutputx",vaxis_oldx_str,"0");
			  property_get("ubootenv.var.480poutputy",vaxis_oldy_str,"0");
			}
			break;
		case 2: //576i
			if(strcmp(scale1080_str,"2") == 0)
			{
				strcpy(vaxis_oldx_str,"0");
				strcpy(vaxis_oldy_str,"0");
			}
			else
			{
			  property_get("ubootenv.var.576ioutputx",vaxis_oldx_str,"0");
			  property_get("ubootenv.var.576ioutputy",vaxis_oldy_str,"0");
			}
			break;
		case 3: //576p
			if(strcmp(scale1080_str,"2") == 0)
			{
				strcpy(vaxis_oldx_str,"0");
				strcpy(vaxis_oldy_str,"0");
			}
			else
			{
			  property_get("ubootenv.var.576poutputx",vaxis_oldx_str,"0");
			  property_get("ubootenv.var.576poutputy",vaxis_oldy_str,"0");
			}
			break;
		case 4: //720p
			if((strcmp(scale1080_str,"2") == 0) || (strcmp(scale1080_str,"1") == 0))
			{
				strcpy(vaxis_oldx_str,"0");
				strcpy(vaxis_oldy_str,"0");
			}
			else
			{
    		  property_get("ubootenv.var.720poutputx",vaxis_oldx_str,"0");
    		  property_get("ubootenv.var.720poutputy",vaxis_oldy_str,"0");
    	}
			break;
		case 5: //1080i
			if((strcmp(scale1080_str,"2") == 0) || (strcmp(scale1080_str,"1") == 0))
			{
				strcpy(vaxis_oldx_str,"0");
				strcpy(vaxis_oldy_str,"0");
			}
			else
			{
			  property_get("ubootenv.var.1080ioutputx",vaxis_oldx_str,"0");
			  property_get("ubootenv.var.1080ioutputy",vaxis_oldy_str,"0");
			}
			break;				
		case 6: //1080p
			if((strcmp(scale1080_str,"2") == 0) || (strcmp(scale1080_str,"1") == 0))
			{
				strcpy(vaxis_oldx_str,"0");
				strcpy(vaxis_oldy_str,"0");
			}
			else
			{
			  property_get("ubootenv.var.1080poutputx",vaxis_oldx_str,"0");
			  property_get("ubootenv.var.1080poutputy",vaxis_oldy_str,"0");
			}
			break;	
		default:			
			LOGI("UNKNOW MODE:%d", old_mode);
			break;					
	}
	int vaxis_newx, vaxis_newy, vaxis_oldx, vaxis_oldy, vaxis_width, vaxis_height;
	vaxis_newx = atoi(vaxis_newx_str);
	vaxis_newy = atoi(vaxis_newy_str);
	vaxis_oldx = atoi(vaxis_oldx_str);
	vaxis_oldy = atoi(vaxis_oldy_str);
	vaxis_width = atoi(vaxis_width_str);
	vaxis_height = atoi(vaxis_height_str);
	
	vaxis_width = vaxis_width + vaxis_newx - vaxis_oldx - 1;
	vaxis_height = vaxis_height + vaxis_newy - vaxis_oldy - 1;
	vaxis_newx = vaxis_newx - vaxis_oldx;
	vaxis_newy = vaxis_newy - vaxis_oldy;
	LOGI("set video axis:%d %d %d %d", vaxis_newx, vaxis_newy, vaxis_width, vaxis_height);
	sprintf(vaxis_str, "%d %d %d %d", vaxis_newx, vaxis_newy, vaxis_width, vaxis_height);
	write(fd_dmode, output_array[new_mode], strlen(output_array[new_mode]));
	write(fb_ppscaler, "0", strlen("0"));
	write(fd_vaxis, vaxis_str, strlen(vaxis_str));
	if((strcmp(scale1080_str,"0") == 0) || ((strcmp(scale1080_str,"1") == 0) && (old_mode != 4)  && (old_mode != 5) && (old_mode != 6)))
	{
		write(fd_daxis, daxis_str, strlen(daxis_str));
		ioctl(fd0,FBIOPUT_OSD_FREE_SCALE_ENABLE,0);
		ioctl(fd1,FBIOPUT_OSD_FREE_SCALE_ENABLE,0);
		ioctl(fd0,FBIOPUT_OSD_FREE_SCALE_WIDTH,osd_width);
		ioctl(fd0,FBIOPUT_OSD_FREE_SCALE_HEIGHT,osd_height); 
		ioctl(fd1,FBIOPUT_OSD_FREE_SCALE_WIDTH,osd_width);
		ioctl(fd1,FBIOPUT_OSD_FREE_SCALE_HEIGHT,osd_height);	
	}
	ioctl(fd0,FBIOPUT_OSD_FREE_SCALE_ENABLE,1);
	ioctl(fd1,FBIOPUT_OSD_FREE_SCALE_ENABLE,1);	

	ret = 0;

exit:	
	close(fd0);
	close(fd1);
	close(fd_vaxis);
	close(fd_daxis);	
	close(fd_fb);	
	close(fd_dmode);
	return ret;

}
		       																			
JNIEXPORT jint JNICALL Java_com_android_settings_OutputSetConfirm_DisableFreeScaleJni( JNIEnv * env,
																									jobject thiz, jint mode )
{	
		return DisableFreeScale(mode);
}
JNIEXPORT jint JNICALL Java_com_android_settings_OutputSetConfirm_EnableFreeScaleJni( JNIEnv * env,
																									jobject thiz, jint new_mode, jint old_mode)
{	
		return EnableFreeScale(new_mode, old_mode);
}
