package com.timelordtty.amountconvert.controller;

import com.timelordtty.amountconvert.model.AmountConvertResult;
import com.timelordtty.amountconvert.service.AmountConvertService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 金额转换API控制器
 */
@RestController
@RequestMapping("/amount-convert")
@CrossOrigin
@Slf4j
public class AmountConvertController {

    private final AmountConvertService amountConvertService;

    @Autowired
    public AmountConvertController(AmountConvertService amountConvertService) {
        this.amountConvertService = amountConvertService;
    }

    /**
     * 将数字金额转换为中文大写金额
     * 
     * @param numericAmount 数字金额
     * @return 转换结果
     */
    @PostMapping("/to-chinese")
    public ResponseEntity<AmountConvertResult> convertToChinese(@RequestParam String numericAmount) {
        log.info("收到数字金额转中文金额请求: {}", numericAmount);
        AmountConvertResult result = amountConvertService.convertToChinese(numericAmount);
        return ResponseEntity.ok(result);
    }

    /**
     * 将中文大写金额转换为数字金额
     * 
     * @param chineseAmount 中文大写金额
     * @return 转换结果
     */
    @PostMapping("/to-numeric")
    public ResponseEntity<AmountConvertResult> convertToNumeric(@RequestParam String chineseAmount) {
        log.info("收到中文金额转数字金额请求: {}", chineseAmount);
        AmountConvertResult result = amountConvertService.convertToNumeric(chineseAmount);
        return ResponseEntity.ok(result);
    }
} 