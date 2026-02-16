package com.example.viewinspector;

import android.content.ComponentName;
import android.content.Context;
import android.graphics.Rect;
import android.os.Build;
import android.util.Log;
import android.view.accessibility.AccessibilityNodeInfo;
import android.view.accessibility.AccessibilityWindowInfo;

import java.util.ArrayList;
import java.util.List;

public class 页面检视 {

   private static final String TAG = "页面检视";
   private final List<视图信息> 控件列表;

   private 页面检视(List<视图信息> 列表) {
       this.控件列表 = 列表;
   }

   // ====================== 入口 ======================
   public static 页面检视 获取所有控件() {
       List<视图信息> list = new ArrayList<>();
       页面检视服务 服务 = 页面检视服务.获取实例();
       if (服务 != null) {
           AccessibilityNodeInfo 根节点 = 服务.获取根节点();
           if (根节点 != null) {
               遍历节点(根节点, 0, list);
               根节点.recycle();
           }
       }
       return new 页面检视(list);
   }

   // ====================== 链式过滤 ======================
   public 页面检视 按ID精确(String id) {
       List<视图信息> res = new ArrayList<>();
       for (视图信息 info : 控件列表) {
           if (id.equals(info.控件ID)) res.add(info);
       }
       return new 页面检视(res);
   }

   public 页面检视 按ID包含(String idPart) {
       List<视图信息> res = new ArrayList<>();
       for (视图信息 info : 控件列表) {
           if (info.控件ID != null && info.控件ID.contains(idPart)) res.add(info);
       }
       return new 页面检视(res);
   }

   public 页面检视 按文字精确(String text) {
       List<视图信息> res = new ArrayList<>();
       for (视图信息 info : 控件列表) {
           if (text.equals(info.显示文字)) res.add(info);
       }
       return new 页面检视(res);
   }

   public 页面检视 按文字包含(String text) {
       List<视图信息> res = new ArrayList<>();
       for (视图信息 info : 控件列表) {
           if (info.显示文字 != null && info.显示文字.contains(text)) res.add(info);
       }
       return new 页面检视(res);
   }

   public 页面检视 按描述包含(String desc) {
       List<视图信息> res = new ArrayList<>();
       for (视图信息 info : 控件列表) {
           if (info.描述内容 != null && info.描述内容.contains(desc)) res.add(info);
       }
       return new 页面检视(res);
   }

   public 页面检视 按类名精确(String cls) {
       List<视图信息> res = new ArrayList<>();
       for (视图信息 info : 控件列表) {
           if (cls.equals(info.控件类名)) res.add(info);
       }
       return new 页面检视(res);
   }

   public 页面检视 按类名包含(String cls) {
       List<视图信息> res = new ArrayList<>();
       for (视图信息 info : 控件列表) {
           if (info.控件类名 != null && info.控件类名.contains(cls)) res.add(info);
       }
       return new 页面检视(res);
   }

   public 页面检视 按可点击(boolean clickable) {
       List<视图信息> res = new ArrayList<>();
       for (视图信息 info : 控件列表) {
           if (info.可点击 == clickable) res.add(info);
       }
       return new 页面检视(res);
   }

   public 页面检视 按可用(boolean enabled) {
       List<视图信息> res = new ArrayList<>();
       for (视图信息 info : 控件列表) {
           if (info.可用 == enabled) res.add(info);
       }
       return new 页面检视(res);
   }

   public 页面检视 按可聚焦(boolean focusable) {
       List<视图信息> res = new ArrayList<>();
       for (视图信息 info : 控件列表) {
           if (info.可聚焦 == focusable) res.add(info);
       }
       return new 页面检视(res);
   }

   public 页面检视 按已聚焦(boolean focused) {
       List<视图信息> res = new ArrayList<>();
       for (视图信息 info : 控件列表) {
           if (info.已聚焦 == focused) res.add(info);
       }
       return new 页面检视(res);
   }

   public 页面检视 按深度大于(int depth) {
       List<视图信息> res = new ArrayList<>();
       for (视图信息 info : 控件列表) {
           if (info.层级深度 > depth) res.add(info);
       }
       return new 页面检视(res);
   }

   public 页面检视 按深度小于(int depth) {
       List<视图信息> res = new ArrayList<>();
       for (视图信息 info : 控件列表) {
           if (info.层级深度 < depth) res.add(info);
       }
       return new 页面检视(res);
   }

   // 区域在屏幕内某个范围
   public 页面检视 按区域在(int left, int top, int right, int bottom) {
       List<视图信息> res = new ArrayList<>();
       Rect limit = new Rect(left, top, right, bottom);
       for (视图信息 info : 控件列表) {
           Rect r = 解析区域(info.屏幕区域);
           if (Rect.intersects(limit, r)) res.add(info);
       }
       return new 页面检视(res);
   }

   // 去重（按ID）
   public 页面检视 去重按ID() {
       List<视图信息> res = new ArrayList<>();
       List<String> ids = new ArrayList<>();
       for (视图信息 info : 控件列表) {
           String id = info.控件ID;
           if (id == null) {
               res.add(info);
           } else if (!ids.contains(id)) {
               ids.add(id);
               res.add(info);
           }
       }
       return new 页面检视(res);
   }

   // 反转顺序
   public 页面检视 反转() {
       List<视图信息> res = new ArrayList<>();
       for (int i = 控件列表.size() - 1; i >= 0; i--) {
           res.add(控件列表.get(i));
       }
       return new 页面检视(res);
   }

   // 取前N个
   public 页面检视 取前N(int n) {
       List<视图信息> res = new ArrayList<>();
       int count = Math.min(n, 控件列表.size());
       for (int i = 0; i < count; i++) {
           res.add(控件列表.get(i));
       }
       return new 页面检视(res);
   }

   // ====================== 输出 ======================
   public void 打印表格() {
       if (控件列表.isEmpty()) {
           Log.d(TAG, "[页面检视] 无匹配控件");
           return;
       }
       Log.d(TAG, "==================== 控件列表 ====================");
       for (视图信息 info : 控件列表) {
           String 缩进 = "  ".repeat(Math.max(0, info.层级深度));
           String line = 缩进
                   + 简短类名(info.控件类名)
                   + " | ID:" + (info.控件ID == null ? "-" : info.控件ID)
                   + " | 文字:" + (info.显示文字 == null ? "-" : info.显示文字)
                   + " | 可点击:" + info.可点击
                   + " | 区域:" + info.屏幕区域;
           Log.d(TAG, line);
       }
       Log.d(TAG, "==================================================");
   }

   public void 打印详情() {
       if (控件列表.isEmpty()) {
           Log.d(TAG, "[页面检视] 无匹配控件");
           return;
       }
       视图信息 info = 控件列表.get(0);
       Log.d(TAG, "==================== 控件详情 ====================");
       Log.d(TAG, "类名     : " + info.控件类名);
       Log.d(TAG, "ID       : " + info.控件ID);
       Log.d(TAG, "文字     : " + info.显示文字);
       Log.d(TAG, "描述     : " + info.描述内容);
       Log.d(TAG, "可点击   : " + info.可点击);
       Log.d(TAG, "可用     : " + info.可用);
       Log.d(TAG, "可聚焦   : " + info.可聚焦);
       Log.d(TAG, "已聚焦   : " + info.已聚焦);
       Log.d(TAG, "层级     : " + info.层级深度);
       Log.d(TAG, "区域     : " + info.屏幕区域);
       Log.d(TAG, "==================================================");
   }

   // 打印统计
   public void 打印统计() {
       Log.d(TAG, "[页面检视] 总数：" + 控件列表.size());
   }

   // ====================== 获取结果 ======================
   public List<视图信息> 取列表() {
       return new ArrayList<>(控件列表);
   }

   public 视图信息 取第一个() {
       return 控件列表.isEmpty() ? null : 控件列表.get(0);
   }

   public 视图信息 取最后一个() {
       return 控件列表.isEmpty() ? null : 控件列表.get(控件列表.size() - 1);
   }

   public int 数量() {
       return 控件列表.size();
   }

   public boolean 存在() {
       return !控件列表.isEmpty();
   }

   // ====================== 模拟点击（超强） ======================
   public boolean 点击第一个() {
       if (控件列表.isEmpty()) return false;
       return 执行点击(控件列表.get(0));
   }

   public boolean 点击最后一个() {
       if (控件列表.isEmpty()) return false;
       return 执行点击(控件列表.get(控件列表.size() - 1));
   }

   // 点击任意一个
   public boolean 点击任意() {
       return 点击第一个();
   }

   // ====================== 内部 ======================
   private static void 遍历节点(AccessibilityNodeInfo node, int 深度, List<视图信息> outList) {
       if (node == null) return;

       视图信息 info = new 视图信息();
       info.层级深度 = 深度;
       info.控件类名 = node.getClassName() != null ? node.getClassName().toString() : "未知";
       info.显示文字 = node.getText() != null ? node.getText().toString() : null;
       info.描述内容 = node.getContentDescription() != null ? node.getContentDescription().toString() : null;
       info.控件ID = node.getViewIdResourceName();
       info.可点击 = node.isClickable();
       info.可用 = node.isEnabled();
       info.可聚焦 = node.isFocusable();
       info.已聚焦 = node.isFocused();
       info.屏幕区域 = 获取区域(node);

       outList.add(info);

       for (int i = 0; i < node.getChildCount(); i++) {
           AccessibilityNodeInfo child = node.getChild(i);
           遍历节点(child, 深度 + 1, outList);
           if (child != null) child.recycle();
       }
   }

   private static String 获取区域(AccessibilityNodeInfo node) {
       Rect r = new Rect();
       node.getBoundsInScreen(r);
       return String.format("[%d,%d][%d,%d]", r.left, r.top, r.right, r.bottom);
   }

   private static Rect 解析区域(String boundsStr) {
       Rect r = new Rect();
       try {
           String s = boundsStr.replace("[", "").replace("]", ",");
           String[] arr = s.split(",");
                      r.left = Integer.parseInt(arr[0]);
           r.top = Integer.parseInt(arr[1]);
           r.right = Integer.parseInt(arr[2]);
           r.bottom = Integer.parseInt(arr[3]);
       } catch (Exception e) {
           r.setEmpty();
       }
       return r;
   }

   private static String 简短类名(String full) {
       if (full == null) return "未知";
       int last = full.lastIndexOf(".");
       return last == -1 ? full : full.substring(last + 1);
   }

   private static boolean 执行点击(视图信息 info) {
       if (info == null) return false;
       页面检视服务 service = 页面检视服务.获取实例();
       if (service == null) return false;

       AccessibilityNodeInfo root = service.获取根节点();
       if (root == null) return false;

       boolean ok = 递归查找并点击(root, info.控件ID, info.显示文字);
       root.recycle();
       return ok;
   }

   private static boolean 递归查找并点击(AccessibilityNodeInfo node, String id, String text) {
       if (node == null) return false;

       boolean matchId = (id == null || (node.getViewIdResourceName() != null && node.getViewIdResourceName().equals(id)));
       boolean matchText = (text == null || (node.getText() != null && node.getText().toString().equals(text)));

       if (matchId && matchText && node.isClickable()) {
           boolean res = node.performAction(AccessibilityNodeInfo.ACTION_CLICK);
           node.recycle();
           return res;
       }

       for (int i = 0; i < node.getChildCount(); i++) {
           AccessibilityNodeInfo child = node.getChild(i);
           if (递归查找并点击(child, id, text)) {
               node.recycle();
               return true;
           }
           if (child != null) child.recycle();
       }
       return false;
   }

   // ====================== 服务状态 ======================
   public static boolean 无障碍已开启(Context context) {
       android.app.ActivityManager am =
               (android.app.ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
       for (android.app.ActivityManager.RunningServiceInfo s : am.getRunningServices(Integer.MAX_VALUE)) {
           if (页面检视服务.class.getName().equals(s.service.getClassName())) return true;
       }
       String enabled = android.provider.Settings.Secure.getString(
               context.getContentResolver(),
               android.provider.Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
       );
       if (enabled == null) return false;
       ComponentName component = new ComponentName(context, 页面检视服务.class);
       return enabled.contains(component.flattenToString());
   }
}