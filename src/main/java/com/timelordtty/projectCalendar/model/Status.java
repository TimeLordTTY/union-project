package com.timelordtty.projectCalendar.model;

/**
 * 项目状态枚举
 */
public enum Status {
    COMPLETED("已完成"),
    IN_PROGRESS("进行中"),
    UPCOMING("即将开始"),
    EXPIRED("已过期");
    
    private final String text;
    
    Status(String text) {
        this.text = text;
    }
    
    /**
     * 获取状态文本
     * @return 状态文本
     */
    public String getText() {
        return text;
    }
    
    /**
     * 根据文本获取状态枚举
     * @param text 状态文本
     * @return 状态枚举，如果没有匹配则返回null
     */
    public static Status fromText(String text) {
        for (Status status : Status.values()) {
            if (status.getText().equals(text)) {
                return status;
            }
        }
        return null;
    }
} 