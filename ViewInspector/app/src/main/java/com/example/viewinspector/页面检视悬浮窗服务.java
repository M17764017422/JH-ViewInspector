package com.example.viewinspector;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.content.SharedPreferences;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.IBinder;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.example.viewinspector.R;

import java.util.List;

/**
 * 页面检视悬浮窗服务
 * 负责悬浮窗展示、控件统计、前台保活
 */
public class 页面检视悬浮窗服务 extends Service {

    private static final String 通知渠道ID = "ViewInspectorChannel";
    private WindowManager 窗口管理器;
    private View 悬浮窗视图;
    private WindowManager.LayoutParams 窗口布局参数;
    private TextView 信息文本框;
    private Button 刷新按钮;
    private Button 关闭按钮;

    // 悬浮窗拖动相关
    private int 初始X坐标;
    private int 初始Y坐标;
    private float 初始触摸X坐标;
    private float 初始触摸Y坐标;

    @Override
    public void onCreate() {
        super.onCreate();
        设置服务语言();
        启动前台保活();
        初始化悬浮窗();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (悬浮窗视图 != null && 窗口管理器 != null) {
            窗口管理器.removeView(悬浮窗视图);
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    // ====================== 核心方法 ======================

    /**
     * 设置服务语言（跟随应用设置）
     */
    private void 设置服务语言() {
        SharedPreferences 配置 = getSharedPreferences("AppSettings", MODE_PRIVATE);
        String 语言代码 = 配置.getString("app_language", "zh");

        Resources 资源 = getResources();
        Configuration 配置项 = 资源.getConfiguration();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            配置项.setLocale(new java.util.Locale(语言代码));
        } else {
            配置项.locale = new java.util.Locale(语言代码);
        }
        资源.updateConfiguration(配置项, 资源.getDisplayMetrics());
    }

    /**
     * 初始化悬浮窗布局与拖动逻辑
     */
    private void 初始化悬浮窗() {
        窗口管理器 = (WindowManager) getSystemService(WINDOW_SERVICE);
        LayoutInflater 布局加载器 = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        悬浮窗视图 = 布局加载器.inflate(R.layout.layout_floating_window, null);

        // 悬浮窗窗口参数配置
        窗口布局参数 = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.O ?
                        WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY :
                        WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT
        );
        窗口布局参数.gravity = Gravity.TOP | Gravity.START;
        窗口布局参数.x = 100;
        窗口布局参数.y = 300;

        窗口管理器.addView(悬浮窗视图, 窗口布局参数);

        // 绑定视图
        信息文本框 = 悬浮窗视图.findViewById(R.id.info_text);
        刷新按钮 = 悬浮窗视图.findViewById(R.id.refresh_button);
        关闭按钮 = 悬浮窗视图.findViewById(R.id.close_button);

        // 按钮点击事件
        刷新按钮.setOnClickListener(v -> 刷新视图信息());
        关闭按钮.setOnClickListener(v -> stopSelf());

        // 悬浮窗拖动逻辑
        悬浮窗视图.setOnTouchListener((v, 事件) -> {
            switch (事件.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    初始X坐标 = 窗口布局参数.x;
                    初始Y坐标 = 窗口布局参数.y;
                    初始触摸X坐标 = 事件.getRawX();
                    初始触摸Y坐标 = 事件.getRawY();
                    return true;
                case MotionEvent.ACTION_MOVE:
                    窗口布局参数.x = 初始X坐标 + (int) (事件.getRawX() - 初始触摸X坐标);
                    窗口布局参数.y = 初始Y坐标 + (int) (事件.getRawY() - 初始触摸Y坐标);
                    窗口管理器.updateViewLayout(悬浮窗视图, 窗口布局参数);
                    return true;
            }
            return false;
        });

        刷新视图信息();
    }

    /**
     * 刷新控件信息统计
     */
    private void 刷新视图信息() {
        List<视图信息> 控件列表 = 页面检视.获取所有控件().取列表();

        // 统计可点击控件数量
        int 可点击数量 = 0;
        for (视图信息 控件 : 控件列表) {
            if (控件.可点击) 可点击数量++;
        }

        // 拼接展示文本
        StringBuilder 拼接 = new StringBuilder();
        拼接.append(getString(R.string.total_controls_count, 控件列表.size()));
        拼接.append(" ");
        拼接.append(getString(R.string.clickable_controls_count, 可点击数量));
        拼接.append("\n");
        拼接.append(getString(R.string.return_to_app_for_details));

        信息文本框.setText(拼接.toString());

        // 发送广播通知主页面刷新
        Intent 刷新广播 = new Intent("com.example.viewinspector.REFRESH_VIEW_INFO");
        sendBroadcast(刷新广播);
    }

    /**
     * 启动前台服务保活
     */
    private void 启动前台保活() {
        创建通知渠道();

        Intent 跳转意图 = new Intent(this, 页面检视主活动.class);
        PendingIntent 延迟意图 = PendingIntent.getActivity(
                this, 0, 跳转意图, PendingIntent.FLAG_IMMUTABLE
        );

        Notification 通知 = new NotificationCompat.Builder(this, 通知渠道ID)
                .setContentTitle(getString(R.string.notification_title))
                .setContentText(getString(R.string.notification_text))
                .setSmallIcon(android.R.drawable.ic_menu_info_details)
                .setContentIntent(延迟意图)
                .build();

        startForeground(1, 通知);
    }

    /**
     * 创建通知渠道（Android 8.0+ 必需）
     */
    private void 创建通知渠道() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel 渠道 = new NotificationChannel(
                    通知渠道ID,
                    getString(R.string.notification_title),
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            NotificationManager 管理器 = getSystemService(NotificationManager.class);
            管理器.createNotificationChannel(渠道);
        }
    }
}
