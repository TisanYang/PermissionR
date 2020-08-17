package com.shuaiqi;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.Settings;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements FlowerPermission.PermissionLister {

    private TextView mTv_ask_permission;
    private ImageView mIv;
    private static final int PERMISSON_REQUESTCODE = 0;
    private static final int SETTING_FOR_PERMISSON = 358;


    private static String[] needPermissions = {
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.INTERNET,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.CAMERA
    };
    private FlowerPermission.Builder mBuilder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mTv_ask_permission = findViewById(R.id.tv_ask_permission);
        mIv = findViewById(R.id.iv_show);

        mTv_ask_permission.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                askPermission();
            }
        });
    }

    private void askPermission() {


        mBuilder = new FlowerPermission.Builder();
        mBuilder.init(MainActivity.this).setPermissionListener(this).checkPermission(needPermissions).build();


        //判断是否是高版本
        /*if (Build.VERSION.SDK_INT >= 23 && getApplicationInfo().targetSdkVersion >= 23) {
            checkPermissions(needPermissions);
        } else {
           //直接使用 -- 判断权限是否被禁止
        }*/
        //首先判断有没有这些权限 - 不区分高版本低版本
        //checkPermissions(needPermissions);

        /*PackageManager pm = getPackageManager();
        String appName = AppUtils.getPackageName(MainActivity.this);
        //int RECORD_AUDIO = ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CAMERA);
        assert appName != null;
        //用这种方式可以判断用户是否真正拒绝了权限，但是低版本的手机拒绝无效，还是可以直接跳转
        int RECORD_AUDIO = pm.checkPermission("Manifest.permission.ACCESS_COARSE_LOCATION", appName);
        if (RECORD_AUDIO != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(MainActivity.this, "用户拒绝权限" + "CAMERA", Toast.LENGTH_SHORT).show();
        }

        Intent intent = new Intent();
        intent.setAction(MediaStore.ACTION_IMAGE_CAPTURE);   //拍照界面的隐式意图
        startActivityForResult(intent,200);*/
        /*Glide.with(MainActivity.this).load("https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1597497024256&di=225e212f9ab9b346dbb671436bd5c7b9&imgtype=0&src=http%3A%2F%2Fa3.att.hudong.com%2F14%2F75%2F01300000164186121366756803686.jpg")
                .into(mIv);*/

    }

    /**
     * 忽略APP的版本高低，通杀一遍，全部查询一次，避免有些用户在低版本手机上把原本已经
     * 默认开启的权限关闭掉了。
     */
    private void checkPermissions(String[] needPermissions) {
        try {

            List<String> needRequestPermissonList = findDeniedPermissions(needPermissions);

            if (null != needRequestPermissonList && needRequestPermissonList.size() > 0) {
                String[] array = needRequestPermissonList.toArray(new String[needRequestPermissonList.size()]);
                Method method = getClass().getMethod("requestPermissions", String[].class, int.class);
                method.invoke(this, array, PERMISSON_REQUESTCODE);
            } else {
                Toast.makeText(MainActivity.this, "用户同意了权限-继续操作", Toast.LENGTH_SHORT).show();
                if (mLister != null) {
                    mLister.onPermissionGetted();
                }
            }
            //}
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }


    /**
     * 集中判断需要申请权限的列表
     *
     * @param needPermissions
     * @return
     * @since 2.5.0
     */
    private List<String> judgePermission(String[] needPermissions) {
        List<String> needRequestPermissonList = new ArrayList();

        PackageManager pm = getPackageManager();
        String appName = AppUtils.getPackageName(MainActivity.this);
        assert appName != null;

        for (String needPermission : needPermissions) {
            //String replace = needPermission.replace("android", "Manifest");
            int RECORD_AUDIO = pm.checkPermission(needPermission, appName);
            if (RECORD_AUDIO != PackageManager.PERMISSION_GRANTED) {
                needRequestPermissonList.add(needPermission);
            }
        }

        return needRequestPermissonList;
    }

    /**
     * 获取权限集中需要申请权限的列表
     *
     * @param permissions
     * @return
     * @since 2.5.0
     */
    private List<String> findDeniedPermissions(String[] permissions) {
        List<String> needRequestPermissonList = new ArrayList<String>();
        //if (Build.VERSION.SDK_INT >= 23 && getApplicationInfo().targetSdkVersion >= 23) {
        //如果是低版本的手机，手动拒绝了，进不到这里来 -- 这种基于反射判断权限是否获取到的方式在低版本上不合适
        try {
            for (String perm : permissions) {
                Method checkSelfMethod = getClass().getMethod("checkSelfPermission", String.class);
                Method shouldShowRequestPermissionRationaleMethod = getClass().getMethod("shouldShowRequestPermissionRationale", String.class);
                if ((Integer) checkSelfMethod.invoke(this, perm) != PackageManager.PERMISSION_GRANTED
                        || (Boolean) shouldShowRequestPermissionRationaleMethod.invoke(this, perm)) {
                    needRequestPermissonList.add(perm);
                }
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
        //}
        return needRequestPermissonList;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);


        if (mBuilder != null) {
            mBuilder.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }

        /*switch (requestCode) {
            case FlowerPermission.PERMISSON_REQUESTCODE:
                int perLength = 0;
                for (int grantResult : grantResults) {
                    if (grantResult == PackageManager.PERMISSION_GRANTED) {
                        perLength++;
                    }
                }

                if (grantResults.length > 0 && perLength == grantResults.length) {
                    Toast.makeText(MainActivity.this, "用户同意了权限", Toast.LENGTH_SHORT).show();
                    if (mLister != null) {
                        mLister.onPermissionGetted();
                    }
                } else {
                    // 没有获取到权限，做特殊处理
                    //showMissingPermissionDialog();
                    Toast.makeText(MainActivity.this, "用户拒绝了权限", Toast.LENGTH_SHORT).show();
                    if (mLister != null) {
                        mLister.onPermissionNoGetted();
                    }
                }
                break;


        }*/

    }


    private void showMissingPermissionDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.notifyTitle);
        builder.setMessage(R.string.notifyMsg);

        // 拒绝, 退出应用
        builder.setNegativeButton(R.string.cancel,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                });

        //开启手机设置页面;
        builder.setPositiveButton(R.string.setting,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        startAppSettings();
                    }
                });

        builder.setCancelable(false);
        builder.show();
    }

    /**
     * 启动应用的设置
     *
     * @since 2.5.9
     */
    private void startAppSettings() {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        intent.setData(Uri.parse("package:" + getPackageName()));
        startActivityForResult(intent, SETTING_FOR_PERMISSON);
    }


   private PermissionRequestLister mLister;

    public void setPermissonRequestLister(PermissionRequestLister lister) {
        mLister = lister;
    }

    @Override
    public void onPermissionGetted() {

    }

    @Override
    public void onPermissionNoGetted() {

    }
    private interface PermissionRequestLister {

        void onPermissionGetted();

        void onPermissionNoGetted();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mBuilder.onActivityResult(requestCode, resultCode, data);
       /* if (requestCode == SETTING_FOR_PERMISSON) {
            checkPermissions(needPermissions);
        }*/
    }
}
