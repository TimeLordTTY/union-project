package com.timelordtty.corrector.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.timelordtty.corrector.config.BaiduApiConfig;
import com.timelordtty.corrector.model.CorrectionDetail;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 百度文本纠错工具类
 */
@Component
@Slf4j
public class BaiduTextCorrectionUtil {

    private final BaiduApiConfig config;
    private final OkHttpClient client;
    private final ObjectMapper objectMapper;
    
    private static final MediaType JSON_MEDIA_TYPE = MediaType.parse("application/json; charset=utf-8");
    
    // 令牌缓存
    private String accessToken;
    private long tokenExpireTime;

    public BaiduTextCorrectionUtil(BaiduApiConfig config) {
        this.config = config;
        this.client = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build();
        this.objectMapper = new ObjectMapper();
    }

    /**
     * 获取访问令牌
     */
    private String getAccessToken() throws Exception {
        // 如果有缓存且未过期，直接返回
        if (accessToken != null && System.currentTimeMillis() < tokenExpireTime) {
            return accessToken;
        }
        
        // 获取新的访问令牌
        String tokenUrl = "https://aip.baidubce.com/oauth/2.0/token";
        HttpUrl.Builder urlBuilder = HttpUrl.parse(tokenUrl).newBuilder();
        urlBuilder.addQueryParameter("grant_type", "client_credentials");
        urlBuilder.addQueryParameter("client_id", config.getApiKey());
        urlBuilder.addQueryParameter("client_secret", config.getSecretKey());
        
        Request request = new Request.Builder()
                .url(urlBuilder.build())
                .post(RequestBody.create("", MediaType.parse("application/x-www-form-urlencoded")))
                .build();
        
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new Exception("获取访问令牌失败: " + response.code());
            }
            
            String responseBody = response.body().string();
            JsonNode jsonNode = objectMapper.readTree(responseBody);
            
            // 解析令牌和过期时间
            accessToken = jsonNode.get("access_token").asText();
            int expiresIn = jsonNode.get("expires_in").asInt();
            
            // 设置过期时间（提前5分钟过期）
            tokenExpireTime = System.currentTimeMillis() + (expiresIn - 300) * 1000L;
            
            return accessToken;
        }
    }

    /**
     * 调用百度API进行文本纠错
     */
    public List<CorrectionDetail> correctText(String text) throws Exception {
        if (text == null || text.trim().isEmpty()) {
            return new ArrayList<>();
        }
        
        // 获取访问令牌
        String token = getAccessToken();
        
        // 构建请求URL和参数
        HttpUrl.Builder urlBuilder = HttpUrl.parse(config.getUrl()).newBuilder();
        urlBuilder.addQueryParameter("access_token", token);
        
        // 构建请求体
        String requestJson = "{\"text\":\"" + text.replace("\"", "\\\"") + "\"}";
        RequestBody body = RequestBody.create(requestJson, JSON_MEDIA_TYPE);
        
        // 发送请求
        Request request = new Request.Builder()
                .url(urlBuilder.build())
                .post(body)
                .build();
        
        // 处理响应
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new Exception("文本纠错API调用失败: " + response.code());
            }
            
            String responseBody = response.body().string();
            JsonNode jsonNode = objectMapper.readTree(responseBody);
            
            // 检查错误码
            if (jsonNode.has("error_code")) {
                int errorCode = jsonNode.get("error_code").asInt();
                if (errorCode != 0) {
                    String errorMsg = jsonNode.has("error_msg") ? jsonNode.get("error_msg").asText() : "未知错误";
                    throw new Exception("文本纠错API返回错误: " + errorCode + " - " + errorMsg);
                }
            }
            
            // 解析纠错结果
            List<CorrectionDetail> details = new ArrayList<>();
            
            if (jsonNode.has("item") && jsonNode.get("item").has("vec_fragment")) {
                JsonNode fragments = jsonNode.get("item").get("vec_fragment");
                
                for (JsonNode fragment : fragments) {
                    if (fragment.has("ori_frag") && fragment.has("correct_frag")) {
                        String original = fragment.get("ori_frag").asText();
                        String corrected = fragment.get("correct_frag").asText();
                        
                        // 只有当原文和纠正后文本不同时才添加
                        if (!original.equals(corrected)) {
                            int position = fragment.has("begin_pos") ? fragment.get("begin_pos").asInt() : 0;
                            
                            CorrectionDetail detail = new CorrectionDetail();
                            detail.setOriginal(original);
                            detail.setCorrected(corrected);
                            detail.setPosition(position);
                            detail.setLength(original.length());
                            
                            details.add(detail);
                        }
                    }
                }
            }
            
            return details;
        }
    }

    /**
     * 应用纠错结果到原文本
     */
    public String applyCorrections(String originalText, List<CorrectionDetail> corrections) {
        if (corrections == null || corrections.isEmpty()) {
            return originalText;
        }
        
        // 排序修正，从后向前应用，避免位置偏移问题
        corrections.sort((c1, c2) -> Integer.compare(c2.getPosition(), c1.getPosition()));
        
        StringBuilder result = new StringBuilder(originalText);
        
        for (CorrectionDetail correction : corrections) {
            int position = correction.getPosition();
            int length = correction.getLength();
            
            // 替换文本
            result.replace(position, position + length, correction.getCorrected());
        }
        
        return result.toString();
    }
} 