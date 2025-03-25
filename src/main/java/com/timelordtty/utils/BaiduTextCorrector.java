package com.timelordtty.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
 * 使用百度文本纠错API进行文本纠错
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
     * 文本纠错结果类
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
        
        // 构建请求
        Request request = new Request.Builder()
                .url(url)
                .addHeader("Content-Type", "application/json")
                .post(RequestBody.create(MediaType.parse("application/json"), jsonBody))
                .build();
        
        // 发送请求并获取响应
        try (Response response = CLIENT.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new Exception("请求失败，状态码: " + response.code());
            }
            
            String responseBody = response.body().string();
            return parseResponse(text, responseBody);
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
            return accessToken;
        }
        
        // 构建请求URL
        HttpUrl url = HttpUrl.parse(ACCESS_TOKEN_URL)
                .newBuilder()
                .addQueryParameter("grant_type", "client_credentials")
                .addQueryParameter("client_id", API_KEY)
                .addQueryParameter("client_secret", API_SECRET)
                .build();
        
        // 构建请求
        Request request = new Request.Builder()
                .url(url)
                .build();
        
        // 发送请求并获取响应
        try (Response response = CLIENT.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new Exception("获取访问令牌失败，状态码: " + response.code());
            }
            
            String responseBody = response.body().string();
            JsonNode jsonNode = MAPPER.readTree(responseBody);
            
            if (jsonNode.has("error")) {
                throw new Exception("获取访问令牌失败: " + jsonNode.get("error_description").asText());
            }
            
            accessToken = jsonNode.get("access_token").asText();
            int expiresIn = jsonNode.get("expires_in").asInt();
            
            // 设置令牌过期时间（提前10分钟）
            tokenExpireTime = currentTime + (expiresIn - 600) * 1000L;
            
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
        JsonNode rootNode = MAPPER.readTree(responseBody);
        
        // 检查是否有错误
        if (rootNode.has("error_code")) {
            throw new Exception("API错误: " + rootNode.get("error_msg").asText());
        }
        
        // 获取纠正后的文本
        String correctedText = rootNode.path("text").asText(originalText);
        List<TextCorrection> corrections = new ArrayList<>();
        
        // 解析item字段，获取详细的纠错信息
        JsonNode itemNode = rootNode.path("item");
        if (!itemNode.isMissingNode() && itemNode.isObject()) {
            JsonNode vecItemNode = itemNode.path("vec_fragment");
            if (!vecItemNode.isMissingNode() && vecItemNode.isArray()) {
                for (JsonNode fragment : vecItemNode) {
                    String ori_frag = fragment.path("ori_frag").asText();
                    String correct_frag = fragment.path("correct_frag").asText();
                    int begin_pos = fragment.path("begin_pos").asInt();
                    int end_pos = fragment.path("end_pos").asInt();
                    
                    String position = "位置: " + begin_pos + "-" + end_pos;
                    corrections.add(new TextCorrection(ori_frag, correct_frag, position));
                }
            }
        }
        
        return new CorrectionResult(correctedText, corrections);
    }
} 