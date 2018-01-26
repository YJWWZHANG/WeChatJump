package com.supersingledog.wechatjump;

import android.content.Intent;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.blankj.utilcode.util.AppUtils;
import com.blankj.utilcode.util.ImageUtils;
import com.blankj.utilcode.util.ServiceUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.blankj.utilcode.util.Utils;
import com.yanzhenjie.permission.AndPermission;
import com.yanzhenjie.permission.Permission;
import com.yanzhenjie.permission.PermissionListener;
import com.yanzhenjie.permission.Rationale;
import com.yanzhenjie.permission.RationaleListener;
import com.yw.game.floatmenu.FloatItem;
import com.yw.game.floatmenu.FloatLogoMenu;
import com.yw.game.floatmenu.FloatMenuView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private ArrayList<FloatItem> mFloatItems = new ArrayList<FloatItem>() {
        {
            add((new FloatItem("开始", 0x99000000, 0x00000000, ImageUtils.drawable2Bitmap(Utils.getApp().getResources().getDrawable(R.color.material_deep_orange_500)))));
            add((new FloatItem("停止", 0x99000000, 0x00000000, ImageUtils.drawable2Bitmap(Utils.getApp().getResources().getDrawable(R.color.material_deep_orange_500)))));
            add((new FloatItem("退出", 0x99000000, 0x00000000, ImageUtils.drawable2Bitmap(Utils.getApp().getResources().getDrawable(R.color.material_deep_orange_500)))));
        }
    };
    private FloatLogoMenu mFloatLogoMenu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        AndPermission.with(this)
                .requestCode(200)
                .permission(Permission.PHONE, Permission.STORAGE, Permission.CAMERA)
                .rationale(new RationaleListener() {
                    @Override
                    public void showRequestPermissionRationale(int requestCode, Rationale rationale) {
                        AndPermission.rationaleDialog(MainActivity.this, rationale).show();
                    }
                })
                .callback(new PermissionListener() {
                    @Override
                    public void onSucceed(int requestCode, @NonNull List<String> grantPermissions) {
                        if(requestCode == 200) {

                        }
                    }

                    @Override
                    public void onFailed(int requestCode, @NonNull List<String> deniedPermissions) {
                    }
                }).start();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
        ((App)Utils.getApp()).exitApp();
    }

    public void onClickStart(View view) {
        if (AppUtils.isAppRoot()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (!Settings.canDrawOverlays(MainActivity.this)) {
                    Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                            Uri.parse("package:" + getPackageName()));
                    startActivityForResult(intent,10);
                }
            }
            mFloatLogoMenu = new FloatLogoMenu.Builder()
                    .withContext(getApplicationContext())
                    .logo(BitmapFactory.decodeResource(getResources(), R.mipmap.timg))
                    .drawCicleMenuBg(true)
                    .backMenuColor(0xe51c23)
                    .setBgDrawable(getResources().getDrawable(R.color.material_white50))
                    .setFloatItems(mFloatItems)
                    .defaultLocation(FloatLogoMenu.RIGHT)
                    .drawRedPointNum(false)
                    .showWithListener(new FloatMenuView.OnMenuClickListener() {
                        @Override
                        public void onItemClick(int position, String title) {
                            mFloatLogoMenu.hide();
                            mFloatLogoMenu.show();
                            switch (title) {
                                case "开始":
                                    if (!ServiceUtils.isServiceRunning("com.supersingledog.wechatjump.CalcService")) {
                                        ServiceUtils.startService(CalcService.class);
                                        ToastUtils.showLong("超级瞄准已部署");
                                    } else {
                                        ToastUtils.showLong("无须重复部署");
                                    }
                                    break;
                                case "停止":
                                    if (ServiceUtils.isServiceRunning("com.supersingledog.wechatjump.CalcService")) {
                                        ServiceUtils.stopService(CalcService.class);
                                        ToastUtils.showLong("执行完下一步超级瞄准将关闭");
                                    } else {
                                        ToastUtils.showLong("超级瞄准未部署");
                                    }
                                    break;
                                case "退出":
                                    ToastUtils.showLong("超级瞄准已退出");
                                    onBackPressed();
                                    break;
                            }
                        }

                        @Override
                        public void dismiss() {
                        }
                    });
            finish();
        } else {
            ToastUtils.showLong("手机没有ROOT权限，超级瞄准无法使用");
        }
    }

}


