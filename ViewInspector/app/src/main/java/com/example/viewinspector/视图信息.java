package com.example.viewinspector;

public class 视图信息 {
    public int 层级深度;
    public String 控件类名;
    public String 显示文字;
    public String 描述内容;
    public String 控件ID;
    public boolean 可点击;
    public boolean 可用;
    public boolean 可聚焦;
    public boolean 已聚焦;
    public String 屏幕区域;
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 层级深度; i++) {
            sb.append("  ");
        }
        
        sb.append(控件类名);
        if (显示文字 != null) {
            sb.append(" - text: \"").append(显示文字).append("\"");
        }
        if (描述内容 != null) {
            sb.append(" - desc: \"").append(描述内容).append("\"");
        }
        if (控件ID != null) {
            sb.append(" - id: ").append(控件ID);
        }
        sb.append(" - 可点击: ").append(可点击);
        sb.append(" - 可用: ").append(可用);
        sb.append(" - 可聚焦: ").append(可聚焦);
        sb.append(" - 已聚焦: ").append(已聚焦);
        sb.append(" - 区域: ").append(屏幕区域);
        
        return sb.toString();
    }
}