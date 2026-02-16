package com.example.viewinspector;

import android.util.Log;

public class 使用示例 {
    private static final String TAG = "使用示例";

    public static void 演示API用法() {
        // 获取所有控件
        页面检视 检视器 = 页面检视.获取所有控件();

        // 按ID精确查找
        检视器.按ID精确("com.example:id/button_ok")
            .点击第一个(); // 点击找到的第一个控件

        // 按文字查找并过滤可点击项
        检视器.按文字包含("提交")
            .按可点击(true)
            .打印表格(); // 打印找到的控件信息

        // 按类名查找
        检视器.按类名包含("Button")
            .取前N(5) // 取前5个结果
            .打印统计(); // 打印结果数量

        // 复合过滤
        检视器.按文字包含("设置")
            .按可用(true)
            .按可聚焦(true)
            .反转() // 反转结果顺序
            .点击任意(); // 点击任意一个符合条件的控件

        // 按屏幕区域查找
        检视器.按区域在(100, 200, 300, 400) // 在指定屏幕坐标范围内的控件
            .打印详情(); // 打印第一个控件的详细信息
    }
}