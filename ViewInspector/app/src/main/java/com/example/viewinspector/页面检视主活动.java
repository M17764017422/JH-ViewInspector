package com.example.viewinspector;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

/**
 * 页面检视主页面
 * 负责权限检查、控件展示、过滤筛选、悬浮窗启动
 */
public class 页面检视主活动 extends AppCompatActivity {

    private static final int 悬浮窗权限请求码 = 1001;

    // 视图控件
    private ScrollView 滚动视图;
    private TextView 信息文本框;
    private TextView 空状态视图;
    private EditText 文字过滤输入框;
    private CheckBox 仅可点击过滤复选框;
    private Button 刷新按钮;
    private Button 启动悬浮窗按钮;
    private androidx.appcompat.widget.Toolbar 工具栏;

    // 数据与状态
    private List<视图信息> 全部控件信息列表 = new ArrayList<>();
    private String 当前语言 = "zh";
    private static 页面检视主活动 实例;
    private BroadcastReceiver 刷新广播接收器;

    // 权限启动器
    private final ActivityResultLauncher<Intent> 无障碍设置启动器 =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), 结果 -> {
                检查无障碍服务();
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        实例 = this;
        加载语言设置();
        setContentView(R.layout.activity_main);

        初始化视图();
        设置监听器();
        检查所需权限();
        注册刷新广播();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu 菜单) {
        getMenuInflater().inflate(R.menu.menu_main, 菜单);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem 菜单项) {
        int 项ID = 菜单项.getItemId();
        if (项ID == R.id.action_language) {
            显示语言选择弹窗();
            return true;
        }
        return super.onOptionsItemSelected(菜单项);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (刷新广播接收器 != null) {
            unregisterReceiver(刷新广播接收器);
        }
        实例 = null;
    }

    @Override
    protected void onActivityResult(int 请求码, int 结果码, Intent 数据) {
        super.onActivityResult(请求码, 结果码, 数据);
        if (请求码 == 悬浮窗权限请求码) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && Settings.canDrawOverlays(this)) {
                Toast.makeText(this, R.string.overlay_permission_granted, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, R.string.overlay_permission_needed, Toast.LENGTH_SHORT).show();
            }
        }
    }

    // ====================== 初始化相关 ======================

    /**
     * 初始化视图控件
     */
    private void 初始化视图() {
        滚动视图 = findViewById(R.id.scroll_view);
        信息文本框 = findViewById(R.id.info_text_view);
        空状态视图 = findViewById(R.id.empty_view);
        文字过滤输入框 = findViewById(R.id.text_filter);
        仅可点击过滤复选框 = findViewById(R.id.clickable_filter);
        刷新按钮 = findViewById(R.id.refresh_button);
        启动悬浮窗按钮 = findViewById(R.id.start_floating_button);
        工具栏 = findViewById(R.id.toolbar);

        setSupportActionBar(工具栏);
    }

    /**
     * 设置所有控件的监听器
     */
    private void 设置监听器() {
        刷新按钮.setOnClickListener(v -> 刷新视图信息());
        启动悬浮窗按钮.setOnClickListener(v -> 启动悬浮窗服务());

        // 文字过滤监听
        文字过滤输入框.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                应用过滤条件();
            }
            @Override
            public void afterTextChanged(android.text.Editable s) {}
        });

        // 可点击过滤监听
        仅可点击过滤复选框.setOnCheckedChangeListener((buttonView, isChecked) -> 应用过滤条件());
    }

    /**
     * 注册刷新广播接收器
     */
    private void 注册刷新广播() {
        刷新广播接收器 = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if ("com.example.viewinspector.REFRESH_VIEW_INFO".equals(intent.getAction())) {
                    if (实例 != null) {
                        实例.runOnUiThread(() -> 刷新视图信息());
                    }
                }
            }
        };

        IntentFilter 过滤器 = new IntentFilter("com.example.viewinspector.REFRESH_VIEW_INFO");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(刷新广播接收器, 过滤器, Context.RECEIVER_EXPORTED);
        } else {
            registerReceiver(刷新广播接收器, 过滤器);
        }
    }

    // ====================== 权限相关 ======================

    /**
     * 检查所有所需权限
     */
    private void 检查所需权限() {
        检查无障碍服务();
        检查悬浮窗权限();
    }

    /**
     * 检查无障碍服务是否开启
     */
    private void 检查无障碍服务() {
        if (!页面检视.无障碍已开启(this)) {
            Toast.makeText(this, R.string.accessibility_service_required, Toast.LENGTH_LONG).show();
            Intent 意图 = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
            无障碍设置启动器.launch(意图);
        } else {
            Toast.makeText(this, R.string.please_click_refresh, Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 检查悬浮窗权限
     */
    private void 检查悬浮窗权限() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {
            Intent 意图 = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:" + getPackageName()));
            startActivityForResult(意图, 悬浮窗权限请求码);
        }
    }

    // ====================== 语言相关 ======================

    /**
     * 显示语言选择弹窗
     */
    private void 显示语言选择弹窗() {
        androidx.appcompat.app.AlertDialog.Builder 构建器 = new androidx.appcompat.app.AlertDialog.Builder(this);
        构建器.setTitle(R.string.language_settings);

        String[] 语言列表 = {getString(R.string.language_chinese), getString(R.string.language_english)};
        int 选中项 = 当前语言.equals("zh") ? 0 : 1;

        构建器.setSingleChoiceItems(语言列表, 选中项, (对话框, 选中索引) -> {
            String 选中语言 = 选中索引 == 0 ? "zh" : "en";
            if (!选中语言.equals(当前语言)) {
                当前语言 = 选中语言;
                设置应用语言(当前语言);
                recreate();
            }
            对话框.dismiss();
        });
        构建器.show();
    }

    /**
     * 设置应用语言
     */
    private void 设置应用语言(String 语言代码) {
        Resources 资源 = getResources();
        Configuration 配置项 = 资源.getConfiguration();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            配置项.setLocale(new java.util.Locale(语言代码));
        } else {
            配置项.locale = new java.util.Locale(语言代码);
        }
        资源.updateConfiguration(配置项, 资源.getDisplayMetrics());

        // 保存语言设置
        SharedPreferences.Editor 编辑器 = getSharedPreferences("AppSettings", MODE_PRIVATE).edit();
        编辑器.putString("app_language", 语言代码);
        编辑器.apply();
    }

    /**
     * 加载已保存的语言设置
     */
    private void 加载语言设置() {
        SharedPreferences 配置 = getSharedPreferences("AppSettings", MODE_PRIVATE);
        String 已保存语言 = 配置.getString("app_language", "zh");

        if (!已保存语言.equals(当前语言)) {
            当前语言 = 已保存语言;
            设置应用语言(当前语言);
        }
    }

    // ====================== 核心业务逻辑 ======================

    /**
     * 刷新当前页面的控件信息
     */
    private void 刷新视图信息() {
        if (!页面检视.无障碍已开启(this)) {
            Toast.makeText(this, R.string.accessibility_service_not_running, Toast.LENGTH_SHORT).show();
            显示空状态(R.string.empty_message);
            return;
        }

        页面检视服务 服务 = 页面检视服务.获取实例();
        if (服务 != null) {
            全部控件信息列表 = 页面检视.获取所有控件().取列表();
            应用过滤条件();

            if (全部控件信息列表.isEmpty()) {
                显示空状态(R.string.no_control_info_try_again);
            } else {
                显示内容视图();
            }
        } else {
            // 服务刚启动，延迟重试
            new android.os.Handler().postDelayed(() -> {
                页面检视服务 延迟获取服务 = 页面检视服务.获取实例();
                if (延迟获取服务 != null) {
                    全部控件信息列表 = 页面检视.获取所有控件().取列表();
                    应用过滤条件();
                    if (全部控件信息列表.isEmpty()) {
                        显示空状态(R.string.no_control_info_try_again);
                    } else {
                        显示内容视图();
                    }
                } else {
                    Toast.makeText(this, R.string.accessibility_service_connecting, Toast.LENGTH_SHORT).show();
                }
            }, 1000);
        }
    }

    /**
     * 应用过滤条件，筛选控件
     */
    private void 应用过滤条件() {
        String 过滤文字 = 文字过滤输入框.getText().toString().toLowerCase();
        boolean 仅显示可点击 = 仅可点击过滤复选框.isChecked();

        List<视图信息> 过滤后列表 = new ArrayList<>();
        for (视图信息 控件 : 全部控件信息列表) {
            // 文字匹配（支持文字/描述模糊搜索）
            boolean 文字匹配 = 过滤文字.isEmpty()
                    || (控件.显示文字 != null && 控件.显示文字.toLowerCase().contains(过滤文字))
                    || (控件.描述内容 != null && 控件.描述内容.toLowerCase().contains(过滤文字));

            // 可点击匹配
            boolean 可点击匹配 = !仅显示可点击 || 控件.可点击;

            if (文字匹配 && 可点击匹配) {
                过滤后列表.add(控件);
            }
        }

        显示控件信息(过滤后列表);
    }

    /**
     * 渲染控件信息到页面（树形结构+彩色高亮）
     */
    private void 显示控件信息(List<视图信息> 控件列表) {
        if (控件列表.isEmpty()) {
            显示空状态(R.string.no_control_info_try_again);
            return;
        }
        显示内容视图();

        StringBuilder 拼接 = new StringBuilder();
        拼接.append("<b>").append(getString(R.string.total_controls, 控件列表.size())).append("</b><br><br>");

        for (视图信息 控件 : 控件列表) {
            // 树形缩进
            String 缩进 = "";
            for (int i = 0; i < 控件.层级深度; i++) {
                缩进 += "&nbsp;&nbsp;&nbsp;&nbsp;";
            }

            // 树形连接线
            if (控件.层级深度 > 0) {
                拼接.append(缩进).append("└─ ");
            } else {
                拼接.append(缩进);
            }

            // 类名（黑色加粗）
            拼接.append("<font color='#000000'><b>").append(控件.控件类名).append("</b></font>");

            // 显示文字（蓝色）
            if (控件.显示文字 != null && !控件.显示文字.isEmpty()) {
                拼接.append(" <font color='#1976D2'>\"").append(控件.显示文字).append("\"</font>");
            }

            // 描述内容（紫色）
            if (控件.描述内容 != null && !控件.描述内容.isEmpty()) {
                拼接.append(" <font color='#7B1FA2'>[").append(控件.描述内容).append("]</font>");
            }

            // 控件ID（橙色）
            if (控件.控件ID != null) {
                拼接.append(" <font color='#FF6F00'>#").append(控件.控件ID).append("</font>");
            }

            // 状态属性（绿/红高亮）
            拼接.append(" <font color='").append(控件.可点击 ? "#4CAF50" : "#F44336").append("'>")
                    .append(getString(R.string.clickable)).append(":").append(控件.可点击 ? getString(R.string.yes) : getString(R.string.no))
                    .append("</font>");
            拼接.append(" <font color='").append(控件.可用 ? "#4CAF50" : "#F44336").append("'>")
                    .append(getString(R.string.enabled)).append(":").append(控件.可用 ? getString(R.string.yes) : getString(R.string.no))
                    .append("</font>");
            拼接.append(" <font color='").append(控件.可聚焦 ? "#4CAF50" : "#F44336").append("'>")
                    .append(getString(R.string.focusable)).append(":").append(控件.可聚焦 ? getString(R.string.yes) : getString(R.string.no))
                    .append("</font>");

            // 屏幕区域（青色）
            拼接.append(" <font color='#0097A7'>").append(控件.屏幕区域).append("</font>");
            拼接.append("<br>");
        }

        信息文本框.setText(android.text.Html.fromHtml(拼接.toString()));
    }

    /**
     * 启动悬浮窗服务
     */
    private void 启动悬浮窗服务() {
        // 权限检查
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {
            Toast.makeText(this, R.string.please_grant_overlay_permission, Toast.LENGTH_SHORT).show();
            return;
        }
        if (!页面检视.无障碍已开启(this)) {
            Toast.makeText(this, R.string.please_enable_accessibility_first, Toast.LENGTH_SHORT).show();
            return;
        }

        // 启动服务
        Intent 服务意图 = new Intent(this, 页面检视悬浮窗服务.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(服务意图);
        } else {
            startService(服务意图);
        }

        Toast.makeText(this, R.string.floating_window_service_started, Toast.LENGTH_SHORT).show();
    }

    // ====================== 视图状态切换 ======================
    private void 显示空状态(int 字符串资源ID) {
        滚动视图.setVisibility(View.GONE);
        空状态视图.setVisibility(View.VISIBLE);
        空状态视图.setText(字符串资源ID);
    }

    private void 显示内容视图() {
        空状态视图.setVisibility(View.GONE);
        滚动视图.setVisibility(View.VISIBLE);
    }
}
