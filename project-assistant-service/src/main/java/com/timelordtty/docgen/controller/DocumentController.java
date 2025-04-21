package com.timelordtty.docgen.controller;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.timelordtty.docgen.model.DocumentGenerationResult;
import com.timelordtty.docgen.model.TemplateRequest;
import com.timelordtty.docgen.service.DocumentGenerationService;
import com.timelordtty.docgen.service.impl.DocumentGenerationServiceImpl;

import lombok.extern.slf4j.Slf4j;

/**
 * 文档生成控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/document")
public class DocumentController {
    
    @Autowired
    private DocumentGenerationService documentService;
    
    @Value("${app.data.dir:./data}")
    private String dataDir;
    
    /**
     * 获取模板目录
     */
    private String getTemplateDir() {
        return Paths.get(dataDir, "templates").toString();
    }
    
    /**
     * 获取生成文档目录
     */
    private String getDocumentDir() {
        return Paths.get(dataDir, "documents").toString();
    }
    
    /**
     * 生成模板
     */
    @PostMapping("/createTemplate")
    public ResponseEntity<DocumentGenerationResult> createTemplate(@RequestBody TemplateRequest request) {
        try {
            // 确保目录存在
            File templateDir = new File(getTemplateDir());
            if (!templateDir.exists()) {
                templateDir.mkdirs();
            }
            
            // 生成时间戳
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
            
            // 构建输出文件路径
            String extension = "WORD".equalsIgnoreCase(request.getTemplateType()) ? ".docx" : ".xlsx";
            String outputFileName = request.getTemplateName() + "_" + timestamp + extension;
            Path outputPath = Paths.get(templateDir.getAbsolutePath(), outputFileName);
            
            // 调用服务创建模板
            DocumentGenerationServiceImpl serviceImpl = (DocumentGenerationServiceImpl) documentService;
            DocumentGenerationResult result = serviceImpl.createTemplate(request.getTemplateType(), request.getFields(), outputPath.toString());
            
            log.info("创建模板成功: {}", outputPath);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("创建模板失败", e);
            return ResponseEntity.badRequest().body(DocumentGenerationResult.error("创建模板失败: " + e.getMessage()));
        }
    }
    
    /**
     * 生成文档
     */
    @PostMapping("/generate")
    public ResponseEntity<DocumentGenerationResult> generateDocument(
            @RequestParam("templateFile") MultipartFile templateFile,
            @RequestParam("data") String jsonData) {
        try {
            // 解析JSON数据
            Map<String, Object> data = new org.springframework.boot.json.JacksonJsonParser().parseMap(jsonData);
            
            // 确保目录存在
            File documentDir = new File(getDocumentDir());
            if (!documentDir.exists()) {
                documentDir.mkdirs();
            }
            
            // 保存上传的模板文件
            String originalFilename = templateFile.getOriginalFilename();
            String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            String templateName = originalFilename.substring(0, originalFilename.lastIndexOf("."));
            
            // 临时存储模板文件
            File tempTemplateFile = File.createTempFile("template_", extension);
            templateFile.transferTo(tempTemplateFile);
            
            // 生成时间戳
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
            
            // 构建输出文件路径
            String outputFileName = templateName + "_doc_" + timestamp + extension;
            Path outputPath = Paths.get(documentDir.getAbsolutePath(), outputFileName);
            
            // 调用服务生成文档
            DocumentGenerationResult result = documentService.generateFromTemplate(tempTemplateFile, outputPath.toFile(), data);
            
            // 删除临时文件
            tempTemplateFile.delete();
            
            log.info("生成文档成功: {}", outputPath);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("生成文档失败", e);
            return ResponseEntity.badRequest().body(DocumentGenerationResult.error("生成文档失败: " + e.getMessage()));
        }
    }
    
    /**
     * 获取可用模板列表
     */
    @GetMapping("/templates")
    public ResponseEntity<List<Map<String, Object>>> getTemplates() {
        try {
            File templateDir = new File(getTemplateDir());
            if (!templateDir.exists()) {
                templateDir.mkdirs();
            }
            
            File[] files = templateDir.listFiles((dir, name) -> name.endsWith(".docx") || name.endsWith(".xlsx"));
            
            List<Map<String, Object>> templates = new java.util.ArrayList<>();
            if (files != null) {
                for (File file : files) {
                    Map<String, Object> template = new java.util.HashMap<>();
                    template.put("name", file.getName());
                    template.put("path", file.getAbsolutePath());
                    template.put("size", file.length());
                    template.put("type", file.getName().endsWith(".docx") ? "WORD" : "EXCEL");
                    template.put("lastModified", file.lastModified());
                    templates.add(template);
                }
            }
            
            return ResponseEntity.ok(templates);
        } catch (Exception e) {
            log.error("获取模板列表失败", e);
            return ResponseEntity.badRequest().body(null);
        }
    }
    
    /**
     * 获取生成的文档列表
     */
    @GetMapping("/documents")
    public ResponseEntity<List<Map<String, Object>>> getDocuments() {
        try {
            File documentDir = new File(getDocumentDir());
            if (!documentDir.exists()) {
                documentDir.mkdirs();
            }
            
            File[] files = documentDir.listFiles((dir, name) -> name.endsWith(".docx") || name.endsWith(".xlsx"));
            
            List<Map<String, Object>> documents = new java.util.ArrayList<>();
            if (files != null) {
                for (File file : files) {
                    Map<String, Object> document = new java.util.HashMap<>();
                    document.put("name", file.getName());
                    document.put("path", file.getAbsolutePath());
                    document.put("size", file.length());
                    document.put("type", file.getName().endsWith(".docx") ? "WORD" : "EXCEL");
                    document.put("lastModified", file.lastModified());
                    documents.add(document);
                }
            }
            
            return ResponseEntity.ok(documents);
        } catch (Exception e) {
            log.error("获取文档列表失败", e);
            return ResponseEntity.badRequest().body(null);
        }
    }
} 