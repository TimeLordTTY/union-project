package com.timelordtty.corrector.controller;

import com.timelordtty.corrector.model.TextCorrectionResult;
import com.timelordtty.corrector.service.TextCorrectionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 文本纠错API控制器
 */
@RestController
@RequestMapping("/text-correction")
@CrossOrigin
@Slf4j
public class TextCorrectionController {
    
    private final TextCorrectionService textCorrectionService;
    
    @Autowired
    public TextCorrectionController(TextCorrectionService textCorrectionService) {
        this.textCorrectionService = textCorrectionService;
    }
    
    /**
     * 纠正文本错误
     * 
     * @param text 需要纠正的文本
     * @return 纠错结果
     */
    @PostMapping("/correct")
    public ResponseEntity<TextCorrectionResult> correctText(@RequestParam String text) {
        log.info("接收到文本纠错请求: {}", text);
        TextCorrectionResult result = textCorrectionService.correctText(text);
        return ResponseEntity.ok(result);
    }
} 