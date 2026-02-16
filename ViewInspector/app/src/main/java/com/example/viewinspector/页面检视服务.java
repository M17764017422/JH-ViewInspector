package com.example.viewinspector;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.util.Log;
import android.view.accessibility.AccessibilityNodeInfo;

public class 页面检视服务 extends AccessibilityService {

    private static final String TAG = "页面检视服务";
    private static 页面检视服务 实例;

    public static 页面检视服务 获取实例() {
        return 实例;
    }

    public static boolean 无障碍已开启(android.content.Context context) {
        android.content.pm.PackageManager packageManager = context.getPackageManager();
        android.content.ComponentName componentName = new android.content.ComponentName(context, 页面检视服务.class);
        int state = packageManager.getComponentEnabledSetting(componentName);

        // 检查服务是否实际运行
        android.app.ActivityManager manager = (android.app.ActivityManager) context.getSystemService(android.content.Context.ACTIVITY_SERVICE);
        for (android.app.ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (页面检视服务.class.getName().equals(service.service.getClassName())) {
                return true;
            }
        }

        // 备用方法：检查无障碍服务状态
        android.content.ContentResolver contentResolver = context.getContentResolver();
        String enabledServices = android.provider.Settings.Secure.getString(contentResolver,
            android.provider.Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);

        return enabledServices != null && enabledServices.contains(context.getPackageName() + "/" + 页面检视服务.class.getName());
    }

    @Override
    public void onServiceConnected() {
        super.onServiceConnected();
        实例 = this;

        AccessibilityServiceInfo info = new AccessibilityServiceInfo();
        info.eventTypes = android.view.accessibility.AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED |
                         android.view.accessibility.AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED;
        info.feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC;
        info.flags = AccessibilityServiceInfo.FLAG_REPORT_VIEW_IDS |
                    AccessibilityServiceInfo.FLAG_RETRIEVE_INTERACTIVE_WINDOWS;

        setServiceInfo(info);
        Log.d(TAG, "无障碍服务已连接");
    }

    @Override
    public void onAccessibilityEvent(android.view.accessibility.AccessibilityEvent event) {
        // 处理窗口变化事件
    }

    @Override
    public void onInterrupt() {
        Log.d(TAG, "无障碍服务被中断");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        实例 = null;
        Log.d(TAG, "无障碍服务已销毁");
    }

    public AccessibilityNodeInfo 获取根节点() {
        return getRootInActiveWindow();
    }
}