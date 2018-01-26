package com.supersingledog.wechatjump;

import android.app.Application;
import android.os.Process;

import com.blankj.utilcode.util.Utils;

public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        Utils.init(this);
    }

    public void exitApp(){
        Process.killProcess(Process.myPid());
        System.exit(0);
    }
}
