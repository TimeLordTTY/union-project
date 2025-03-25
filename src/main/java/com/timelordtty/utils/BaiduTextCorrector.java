package com.timelordtty.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.timelordtty.model.TextCorrection;

import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * 百度文本纠错工具类
 * 使用百度文本纠错API进行文本纠正
 * 
 * @author tianyu.tang
 * @version 1.0
 */
public class BaiduTextCorrector {
    
    // 百度API密钥
    private static final String API_KEY = "CsdvxQbBbwYREpS2iy7cukmr";
    private static final String API_SECRET = "VzfeFhNb4DzVxXWKW2J2aMJM7uHgeUpg";
    
    // 百度API请求URL
    private static final String ACCESS_TOKEN_URL = "https://aip.baidubce.com/oauth/2.0/token";
    private static final String TEXT_CORRECTION_URL = "https://aip.baidubce.com/rpc/2.0/nlp/v1/ecnet";
    
    // HTTP客户端
    private static final OkHttpClient CLIENT = new OkHttpClient();
    
    // JSON解析器
    private static final ObjectMapper MAPPER = new ObjectMapper();
    
    // 缓存的访问令牌
    private static String accessToken;
    private static long tokenExpireTime;
    
    /**
     * 文本纠正结果类
     */
    public static class CorrectionResult {
        private String correctedText;
        private List<TextCorrection> corrections;
        
        public CorrectionResult(String correctedText, List<TextCorrection> corrections) {
            this.correctedText = correctedText;
            this.corrections = corrections;
        }
        
        public String getCorrectedText() {
            return correctedText;
        }
        
        public List<TextCorrection> getCorrections() {
            return corrections;
        }
    }
    
    /**
     * 使用百度API纠正文本
     * @param text 需要纠正的文本
     * @return 纠正结果
     * @throws Exception 纠正过程中的异常
     */
    public static CorrectionResult correct(String text) throws Exception {
        // 记录到原始日志
        LogUtils.logTextCorrectionStart(text);
        LogUtils.info("开始百度文本纠错处理，输入文本长度：" + text.length() + " 字符");
        
        // 使用新的API日志记录器
        String traceId = ApiLogger.logRequestStart(ApiLogger.API_TYPE_BAIDU, "文本纠错", 
                TEXT_CORRECTION_URL, "输入文本长度: " + text.length() + " 字符");
        
        long startTime = System.currentTimeMillis();
        try {
            // 获取访问令牌
            String token = getAccessToken();
            
            // 构建请求URL
            HttpUrl url = HttpUrl.parse(TEXT_CORRECTION_URL)
                    .newBuilder()
                    .addQueryParameter("access_token", token)
                    .build();
            
            // 构建请求体
            Map<String, String> params = new HashMap<>();
            params.put("text", text);
            String jsonBody = MAPPER.writeValueAsString(params);
            
            // 记录API请求
            LogUtils.logBaiduApiRequest(text);
            LogUtils.apiRequest(url.toString(), "POST", jsonBody);
            
            // 使用ApiLogger记录请求
            String requestTraceId = ApiLogger.logRequestStart(ApiLogger.API_TYPE_BAIDU, "POST", 
                    url.toString(), jsonBody);
            
            // 构建请求
            Request request = new Request.Builder()
                    .url(url)
                    .addHeader("Content-Type", "application/json")
                    .post(RequestBody.create(MediaType.parse("application/json"), jsonBody))
                    .build();
            
            // 发送请求并获取响应
            long requestStartTime = System.currentTimeMillis();
            try (Response response = CLIENT.newCall(request).execute()) {
                long requestEndTime = System.currentTimeMillis();
                String responseBody = response.body().string();
                
                // 记录API响应
                LogUtils.apiResponse(url.toString(), response.code(), responseBody);
                LogUtils.logBaiduApiResponse(responseBody);
                
                // 使用ApiLogger记录响应
                ApiLogger.logRequestEnd(requestTraceId, response.code(), responseBody, 
                        requestEndTime - requestStartTime);
                
                if (!response.isSuccessful()) {
                    String errorMsg = "百度API请求失败，状态码: " + response.code();
                    LogUtils.error(errorMsg);
                    LogUtils.apiError(url.toString(), errorMsg, null);
                    ApiLogger.logError(traceId, errorMsg, null);
                    throw new Exception(errorMsg);
                }
                
                CorrectionResult result = parseResponse(text, responseBody);
                
                // 记录纠错结果
                LogUtils.logTextCorrectionResult(text, result.getCorrectedText(), result.getCorrections().size());
                
                long totalDuration = System.currentTimeMillis() - startTime;
                // 使用ApiLogger记录API使用统计
                ApiLogger.logBaiduApiUsage("文本纠错", text.length(), 
                        result.getCorrectedText().length(), totalDuration);
                
                // 使用ApiLogger记录请求结束
                ApiLogger.logRequestEnd(traceId, 200, 
                        "纠正结果: 原文本长度=" + text.length() + 
                        ", 纠正后长度=" + result.getCorrectedText().length() + 
                        ", 纠正项数=" + result.getCorrections().size(), 
                        totalDuration);
                
                return result;
            }
        } catch (Exception e) {
            long errorDuration = System.currentTimeMillis() - startTime;
            LogUtils.error("文本纠错过程中发生异常: " + e.getMessage(), e);
            ApiLogger.logError(traceId, "文本纠错失败: " + e.getMessage(), e);
            ApiLogger.logRequestEnd(traceId, 500, "发生异常: " + e.getMessage(), errorDuration);
            throw e;
        }
    }
    
    /**
     * 获取百度API访问令牌
     * @return 访问令牌
     * @throws Exception 获取令牌过程中的异常
     */
    private static String getAccessToken() throws Exception {
        // 检查缓存的令牌是否有效
        long currentTime = System.currentTimeMillis();
        if (accessToken != null && currentTime < tokenExpireTime) {
            LogUtils.info("使用缓存的百度API访问令牌，剩余有效期: " + ((tokenExpireTime - currentTime) / 1000) + " 秒");
            ApiLogger.logTokenOperation("使用缓存", ApiLogger.API_TYPE_BAIDU, 
                    "剩余有效期: " + ((tokenExpireTime - currentTime) / 1000) + " 秒");
            return accessToken;
        }
        
        LogUtils.logBaiduApiTokenRequest();
        LogUtils.info("请求新的百度API访问令牌");
        
        // 使用ApiLogger记录令牌操作
        ApiLogger.logTokenOperation("请求新令牌", ApiLogger.API_TYPE_BAIDU, "令牌缓存过期或不存在");
        
        // 构建请求URL
        HttpUrl url = HttpUrl.parse(ACCESS_TOKEN_URL)
                .newBuilder()
                .addQueryParameter("grant_type", "client_credentials")
                .addQueryParameter("client_id", API_KEY)
                .addQueryParameter("client_secret", API_SECRET)
                .build();
        
        // 记录API请求
        LogUtils.apiRequest(url.toString(), "GET", null);
        
        // 使用ApiLogger记录请求
        String traceId = ApiLogger.logRequestStart(ApiLogger.API_TYPE_BAIDU, "GET", 
                url.toString(), null);
        
        // 构建请求
        Request request = new Request.Builder()
                .url(url)
                .build();
        
        // 发送请求并获取响应
        long requestStartTime = System.currentTimeMillis();
        try (Response response = CLIENT.newCall(request).execute()) {
            long requestEndTime = System.currentTimeMillis();
            String responseBody = response.body().string();
            
            // 记录API响应
            LogUtils.apiResponse(url.toString(), response.code(), responseBody);
            
            // 使用ApiLogger记录响应
            ApiLogger.logRequestEnd(traceId, response.code(), responseBody, 
                    requestEndTime - requestStartTime);
            
            if (!response.isSuccessful()) {
                String errorMsg = "获取百度API访问令牌失败，状态码: " + response.code();
                LogUtils.error(errorMsg);
                LogUtils.apiError(url.toString(), errorMsg, null);
                ApiLogger.logError(traceId, errorMsg, null);
                throw new Exception(errorMsg);
            }
            
            JsonNode jsonNode = MAPPER.readTree(responseBody);
            
            if (jsonNode.has("error")) {
                String errorMsg = "获取百度API访问令牌失败: " + jsonNode.get("error_description").asText();
                LogUtils.error(errorMsg);
                LogUtils.apiError(url.toString(), errorMsg, null);
                ApiLogger.logError(traceId, errorMsg, null);
                throw new Exception(errorMsg);
            }
            
            accessToken = jsonNode.get("access_token").asText();
            int expiresIn = jsonNode.get("expires_in").asInt();
            
            // 设置令牌过期时间（提前10分钟）
            tokenExpireTime = currentTime + (expiresIn - 600) * 1000L;
            
            LogUtils.logBaiduApiTokenResponse(responseBody, tokenExpireTime);
            LogUtils.info("成功获取百度API访问令牌，有效期: " + expiresIn + " 秒");
            
            // 使用ApiLogger记录令牌操作
            ApiLogger.logTokenOperation("获取成功", ApiLogger.API_TYPE_BAIDU, 
                    "有效期: " + expiresIn + "秒，过期时间: " + new java.util.Date(tokenExpireTime));
            
            return accessToken;
        }
    }
    
    /**
     * 解析API响应
     * @param originalText 原始文本
     * @param responseBody 响应体
     * @return 纠正结果
     * @throws Exception 解析过程中的异常
     */
    private static CorrectionResult parseResponse(String originalText, String responseBody) throws Exception {
        LogUtils.info("开始解析百度API响应");
        
        String traceId = ApiLogger.logRequestStart(ApiLogger.API_TYPE_BAIDU, "解析", 
                "解析百度API响应", "响应体长度: " + responseBody.length() + " 字符");
        
        long startTime = System.currentTimeMillis();
        try {
            JsonNode rootNode = MAPPER.readTree(responseBody);
            
            // 检查是否有错误
            if (rootNode.has("error_code")) {
                String errorCode = rootNode.get("error_code").asText();
                String errorMsg = "百度API错误: " + rootNode.get("error_msg").asText();
                LogUtils.error(errorMsg);
                ApiLogger.logError(traceId, errorMsg + "(错误码: " + errorCode + ")", null);
                throw new Exception(errorMsg);
            }
            
            // 获取纠正后的文本
            JsonNode resultNode = rootNode.path("result");
            String correctedText = resultNode.path("correct_query").asText(originalText);
            List<TextCorrection> corrections = new ArrayList<>();
            
            // 解析item字段，获取详细的纠错信息
            JsonNode itemNode = resultNode.path("item");
            if (itemNode != null && !itemNode.isMissingNode()) {
                // 处理不同的响应格式
                processItemsNode(itemNode, originalText, corrections);
            }
            
            LogUtils.info("百度API响应解析完成，共有 " + corrections.size() + " 处纠正");
            
            long duration = System.currentTimeMillis() - startTime;
            ApiLogger.logRequestEnd(traceId, 200, 
                    "解析结果: 发现 " + corrections.size() + " 处纠正", duration);
            
            return new CorrectionResult(correctedText, corrections);
            
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            LogUtils.error("解析百度API响应时出错: " + e.getMessage(), e);
            ApiLogger.logError(traceId, "解析响应失败: " + e.getMessage(), e);
            ApiLogger.logRequestEnd(traceId, 500, "解析失败: " + e.getMessage(), duration);
            throw e;
        }
    }
    
    /**
     * 处理响应中的items节点
     */
    private static void processItemsNode(JsonNode itemNode, String originalText, List<TextCorrection> corrections) {
        // 如果是数组类型，直接处理
        if (itemNode.isArray()) {
            for (JsonNode item : itemNode) {
                processItemNode(item, originalText, corrections);
            }
        } 
        // 如果是对象类型，检查是否有vec_fragment字段
        else if (itemNode.isObject()) {
            JsonNode vecItemNode = itemNode.path("vec_fragment");
            if (!vecItemNode.isMissingNode() && vecItemNode.isArray()) {
                for (JsonNode fragment : vecItemNode) {
                    processFragmentNode(fragment, corrections);
                }
            }
            // 尝试其他可能的格式
            else if (itemNode.has("ori_word") && itemNode.has("correct_word")) {
                processItemNode(itemNode, originalText, corrections);
            }
        }
    }
    
    /**
     * 处理item节点（直接类型）
     */
    private static void processItemNode(JsonNode item, String originalText, List<TextCorrection> corrections) {
        if (item.has("ori_word") && item.has("correct_word")) {
            String oriWord = item.path("ori_word").asText();
            String correctWord = item.path("correct_word").asText();
            
            // 查找原文本中该词的位置
            List<Integer> positions = findAllPositions(originalText, oriWord);
            
            for (int pos : positions) {
                String position = "位置: " + pos + "-" + (pos + oriWord.length());
                TextCorrection correction = new TextCorrection(oriWord, correctWord, position);
                corrections.add(correction);
                
                LogUtils.info("纠正词语: '" + oriWord + "' -> '" + correctWord + "' " + position);
            }
        }
    }
    
    /**
     * 处理fragment节点
     */
    private static void processFragmentNode(JsonNode fragment, List<TextCorrection> corrections) {
        String ori_frag = fragment.path("ori_frag").asText();
        String correct_frag = fragment.path("correct_frag").asText();
        int begin_pos = fragment.path("begin_pos").asInt();
        int end_pos = fragment.path("end_pos").asInt();
        
        String position = "位置: " + begin_pos + "-" + end_pos;
        TextCorrection correction = new TextCorrection(ori_frag, correct_frag, position);
        corrections.add(correction);
        
        LogUtils.info("纠正片段: '" + ori_frag + "' -> '" + correct_frag + "' " + position);
    }

    /**
     * 查找文本中所有匹配词的位置
     *
     * @param text 原文本
     * @param word 待查找的词
     * @return 位置列表
     */
    private static List<Integer> findAllPositions(String text, String word) {
        List<Integer> positions = new ArrayList<>();
        if (word == null || word.isEmpty()) {
            return positions;
        }
        
        Pattern pattern = Pattern.compile(Pattern.quote(word));
        Matcher matcher = pattern.matcher(text);
        
        while (matcher.find()) {
            positions.add(matcher.start());
        }
        
        return positions;
    }
    
    /**
     * 设置API密钥
     *
     * @param apiKey API Key
     * @param apiSecret API Secret
     */
    public static void setApiConfig(String apiKey, String apiSecret) {
        // 注意：这里使用反射方式修改final字段，仅用于开发/测试目的
        try {
            java.lang.reflect.Field apiKeyField = BaiduTextCorrector.class.getDeclaredField("API_KEY");
            java.lang.reflect.Field apiSecretField = BaiduTextCorrector.class.getDeclaredField("API_SECRET");
            
            apiKeyField.setAccessible(true);
            apiSecretField.setAccessible(true);
            
            apiKeyField.set(null, apiKey);
            apiSecretField.set(null, apiSecret);
            
            // 重置令牌缓存
            accessToken = null;
            tokenExpireTime = 0;
            
            LogUtils.info("已设置百度API配置: APIKey=" + apiKey + ", Secret=***");
            ApiLogger.logTokenOperation("更新配置", ApiLogger.API_TYPE_BAIDU, 
                    "API密钥已更新，令牌缓存已清除");
        } catch (Exception e) {
            LogUtils.error("设置API密钥失败: " + e.getMessage(), e);
        }
    }
} 