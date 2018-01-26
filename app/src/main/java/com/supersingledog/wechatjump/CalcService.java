package com.supersingledog.wechatjump;

import android.app.Service;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Environment;
import android.os.IBinder;
import android.os.SystemClock;
import android.support.annotation.Nullable;

import com.blankj.utilcode.util.ImageUtils;
import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.ServiceUtils;
import com.blankj.utilcode.util.ShellUtils;

import java.util.Random;

public class CalcService extends Service{

    private AutoThread mAutoThread;
    private int mTotal = 0;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mAutoThread = new AutoThread();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        mAutoThread.start();
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mTotal = 9999;
    }

    private class AutoThread extends Thread {
        private Bitmap mBitmap;
        private int mCenterHit = 0;
        private final double JUMP_RATIO = 1.390f;
        private double mJumpRatio = 0;
        private int imgNum = 0;

        @Override
        public void run() {
            for (int i = 0; i < 5000; i++) {
                if (mTotal == 9999) {
                    ServiceUtils.stopService(CalcService.class);
                    return;
                }
                mTotal++;
//                String imgPath = Environment.getExternalStorageDirectory().getPath() + "/IMG_CACHE/temp.png";
                String imgPath = Environment.getExternalStorageDirectory().getPath() + "/IMG_CACHE/img_" + imgNum++ % 10 + ".png";
                ShellUtils.execCmd("/system/bin/screencap -p " + imgPath, true, true);
                mBitmap = ImageUtils.getBitmap(imgPath);
                if (mBitmap != null) {
                    if (mJumpRatio == 0) {
                        mJumpRatio = JUMP_RATIO * 1080 / mBitmap.getWidth();
                    }
                    int[] myPos = CalcUtils.findMyPos(mBitmap);
                    LogUtils.w("myPos, x: " + myPos[0] + ", y: " + myPos[1]);

                    int[] nextCenter = CalcUtils.findNextCenter(mBitmap, myPos);
                    LogUtils.w("nextCenter, x: " + nextCenter[0] + ", y: " + nextCenter[1]);

                    if (nextCenter[0] == 0) {
                        LogUtils.w("find nextCenter, fail");
                        mTotal = 9999;
                    } else {
                        int centerX, centerY;
                        int[] whitePoint = CalcUtils.findWhitePoint(mBitmap, nextCenter[0] - 120, nextCenter[1], nextCenter[0] + 120, nextCenter[1] + 180);
                        if (whitePoint != null) {
                            centerX = whitePoint[0];
                            centerY = whitePoint[1];
                            mCenterHit++;
                            LogUtils.w("find whitePoint, succ, (" + centerX + ", " + centerY + "), mCenterHit: " + mCenterHit + ", mTotal: " + mTotal);
                        } else {
                            if (nextCenter[2] != Integer.MAX_VALUE && nextCenter[4] != Integer.MIN_VALUE) {
                                centerX = (nextCenter[2] + nextCenter[4]) / 2;
                                centerY = (nextCenter[3] + nextCenter[5]) / 2;
                            } else {
                                centerX = nextCenter[0];
                                centerY = nextCenter[1] + 48;
                            }
                        }
                        LogUtils.w("find nextCenter, succ, (" + centerX + ", " + centerY + ")");
                        int distance = (int) (Math.sqrt((centerX - myPos[0]) * (centerX - myPos[0]) + (centerY - myPos[1]) * (centerY - myPos[1])) * mJumpRatio);
                        LogUtils.w("distance: " + distance);
                        int pressX = 400 + new Random().nextInt(100);
                        int pressY = 500 + new Random().nextInt(100);
                        ShellUtils.execCmd(String.format("input swipe %d %d %d %d %d", pressX, pressY, pressX, pressY, distance), true, true);
                    }
                } else {
                    mTotal = 9999;
                }
            }
            SystemClock.sleep(1000 + new Random().nextInt(2000));
        }
    }

}
