#include <string.h>
#include <jni.h>
#include <stdio.h>
#include <stdlib.h>
#include <fcntl.h>
#include <unistd.h>
#include <termios.h>
#include <sys/mman.h>
#include <android/log.h>
#include <errno.h>



jint
Java_com_myApplication_segment_SegmentActivity_SegmentControl (JNIEnv* env, jobject thiz, jint data )
{

    int dev, ret ;
    dev = open("/dev/segment",O_RDWR | O_SYNC);

    if(dev != -1) {
        ret = write(dev,&data,4);
        close(dev);
    } else {
        //__android_log_print(ANDROID_LOG_ERROR, "SegmentActivity", "Device Open ERROR!\n");
        exit(1);
    }
    return 0;
}

jint
Java_com_myApplication_segment_SegmentActivity_SegmentIOControl (JNIEnv* env,  jobject thiz, jint data )
{
    int dev, ret ;
    dev = open("/dev/segment",O_RDWR | O_SYNC);

    if(dev != -1) {
        ret = ioctl(dev, data, NULL, NULL);
        close(dev);
    } else {
        //__android_log_print(ANDROID_LOG_ERROR, "SegmentActivity", "Device Open ERROR!\n");
        exit(1);
    }
    return 0;
}

jint
Java_com_myApplication_segment_SegmentActivity_LEDControl( JNIEnv* env,
                                                           jobject thiz, jint data )
{
    int fd,ret;

    fd = open("/dev/led",O_WRONLY);
    if(fd < 0) return -errno;
    if(fd > 0) {
        data &= 0xff;
        ret = write(fd,&data,1);
        close(fd);
    } else return fd;

    if(ret == 1) return 0;

    return -1;
}


