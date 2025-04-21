package com.timelordtty.docgen.model;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 模板创建请求
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TemplateRequest {
    
    private String templateType;    // 模板类型 (WORD/EXCEL)
    private String templateName;    // 模板名称
    private List<String> fields;    // 字段列表
} 