package com.timelordtty.corrector.util;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.timelordtty.AppLogger;
import com.timelordtty.corrector.model.TextCorrection;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * 百度API服务，处理所有与百度API的交互
 * 基于spelling-test项目实现
 * @author tianyu.tang
 */
public class BaiduTextCorrector {
    private static final ObjectMapper MAPPER = new ObjectMapper();
    
    private static final String TOKEN_URL = "https://aip.baidubce.com/oauth/2.0/token";
    private static final String CORRECTION_API_URL = "https://aip.baidubce.com/rpc/2.0/nlp/v2/text_correction?access_token=";
    
    private static final OkHttpClient CLIENT = new OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)  // 减少连接超时，避免UI等待太久
        .readTimeout(10, TimeUnit.SECONDS)     // 减少读取超时
        .writeTimeout(10, TimeUnit.SECONDS)    // 减少写入超时
        .retryOnConnectionFailure(true)        // 启用连接失败重试
        .build();
    
    // 配置信息 - 从配置文件读取
    private static String apiKey;
    private static String secretKey;
    
    /**
     * 缓存的访问令牌
     */
    private static String cachedAccessToken;
    
    static {
        // 加载API配置
        loadApiConfig();
        
        // 在后台预加载Token
        preloadToken();
    }
    
    /**
     * 从配置文件加载API配置
     */
    private static void loadApiConfig() {
        AppLogger.info("开始加载百度API配置");
        
        // 尝试多个可能的配置文件位置
        String[] configFiles = {
            "src/main/resources/api.properties",
            "./api.properties",
            "./config/api.properties",
            System.getProperty("user.home") + "/union-project/api.properties"
        };
        
        for (String configFile : configFiles) {
            Properties props = loadPropertiesFile(configFile);
            if (props != null) {
                String propsApiKey = props.getProperty("baidu.api.key");
                String propsSecretKey = props.getProperty("baidu.api.secret");
                
                if (isValidApiKey(propsApiKey) && isValidApiKey(propsSecretKey)) {
                    apiKey = propsApiKey;
                    secretKey = propsSecretKey;
                    AppLogger.info("成功从配置文件加载API凭证: " + configFile);
                    return;
                }
            }
        }
        
        // 如果未能从文件加载，使用默认值
        if (apiKey == null || secretKey == null) {
            apiKey = "CsdvxQbBbwYREpS2iy7cukmr";
            secretKey = "VzfeFhNb4DzVxXWKW2J2aMJM7uHgeUpg";
            AppLogger.warn("未能从配置文件加载API凭证，使用默认值");
        }
    }
    
    /**
     * 加载Properties文件
     */
    private static Properties loadPropertiesFile(String filePath) {
        try {
            Properties props = new Properties();
            InputStream input = new FileInputStream(filePath);
            props.load(input);
            input.close();
            AppLogger.info("成功加载配置文件: " + filePath);
            return props;
        } catch (IOException e) {
            AppLogger.debug("无法加载配置文件: " + filePath + " - " + e.getMessage());
            return null;
        }
    }
    
    /**
     * 验证API密钥是否有效
     */
    private static boolean isValidApiKey(String key) {
        return key != null && !key.isEmpty() && !key.equals("${baidu.api.key}") && !key.equals("${baidu.api.secret}");
    }
    
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
     * 设置API凭证
     * 如果凭证无效，会记录警告但不会阻止程序继续运行
     */
    public static void setCredentials(String apiKeyValue, String secretKeyValue) {
        // 验证API密钥的有效性
        if (apiKeyValue == null || apiKeyValue.trim().isEmpty()) {
            AppLogger.warn("提供的API密钥为空");
        }
        
        if (secretKeyValue == null || secretKeyValue.trim().isEmpty()) {
            AppLogger.warn("提供的Secret密钥为空");
        }
        
        apiKey = apiKeyValue;
        secretKey = secretKeyValue;
        
        // 清除缓存的令牌，强制重新获取
        cachedAccessToken = null;
        
        // 在后台预加载Token
        preloadToken();
    }
    
    /**
     * 在后台预加载Token，不阻塞主线程
     */
    public static void preloadToken() {
        new Thread(() -> {
            try {
                AppLogger.info("后台预加载Token开始");
                
                // 设置5秒超时
                CompletableFuture<String> future = 
                    CompletableFuture.supplyAsync(() -> {
                        try {
                            return getTokenInternal();
                        } catch (Exception e) {
                            AppLogger.warn("Token预加载失败: " + e.getMessage());
                            return null;
                        }
                    });
                
                // 添加5秒超时
                String token = future.get(5, TimeUnit.SECONDS);
                if (token != null) {
                    cachedAccessToken = token;
                    AppLogger.info("Token预加载成功: " + token.substring(0, Math.min(token.length(), 10)) + "...");
                }
            } catch (java.util.concurrent.TimeoutException e) {
                AppLogger.warn("Token预加载超时，将在需要时获取");
            } catch (Exception e) {
                AppLogger.warn("Token预加载异常: " + e.getMessage());
            }
        }, "token-preload-thread").start();
    }
    
    /**
     * 获取百度API访问令牌
     * 
     * @return 访问令牌
     * @throws IOException 网络请求错误
     */
    private static String getToken() throws IOException {
        // 如果已有缓存的令牌，直接返回
        if (cachedAccessToken != null && !cachedAccessToken.isEmpty()) {
            AppLogger.info("使用缓存的Token: " + cachedAccessToken.substring(0, Math.min(cachedAccessToken.length(), 10)) + "...");
            return cachedAccessToken;
        }
        
        // 验证API密钥是否可用
        if (apiKey == null || secretKey == null || apiKey.isEmpty() || secretKey.isEmpty()) {
            throw new IOException("API密钥或Secret密钥未设置，无法获取Token");
        }
        
        // 尝试获取Token并添加5秒超时
        try {
            AppLogger.info("开始获取新Token...");
            CompletableFuture<String> future = 
                CompletableFuture.supplyAsync(() -> getTokenInternal());
            
            // 添加5秒超时
            return future.get(5, TimeUnit.SECONDS);
        } catch (java.util.concurrent.TimeoutException e) {
            throw new IOException("获取Token超时，请检查网络连接", e);
        } catch (Exception e) {
            throw new IOException("获取Token失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 内部方法，实际执行Token获取
     */
    private static String getTokenInternal() {
        AppLogger.info("==== 开始获取百度API Token ====");
        AppLogger.info("API_KEY: " + apiKey.substring(0, Math.min(apiKey.length(), 3)) + "..." + 
                     (apiKey.length() > 6 ? apiKey.substring(apiKey.length() - 3) : ""));
        AppLogger.info("SECRET_KEY: " + secretKey.substring(0, Math.min(secretKey.length(), 3)) + "..." + 
                     (secretKey.length() > 6 ? secretKey.substring(secretKey.length() - 3) : ""));
        
        // 首先尝试使用HttpUrl构建方式
        try {
            AppLogger.info("=== 方法1：使用HttpUrl方式构建Token请求 ===");
            HttpUrl.Builder urlBuilder = HttpUrl.parse(TOKEN_URL).newBuilder();
            urlBuilder.addQueryParameter("client_id", apiKey);
            urlBuilder.addQueryParameter("client_secret", secretKey);
            urlBuilder.addQueryParameter("grant_type", "client_credentials");
            
            String url = urlBuilder.build().toString();
            AppLogger.info("Token请求URL: " + url);
            
            RequestBody requestBody = RequestBody.create(MediaType.parse("application/json"), "");
            Request request = new Request.Builder()
                .url(url)
                .post(requestBody)
                .build();
            
            AppLogger.info("Token请求头: " + request.headers().toString());
            AppLogger.info("Token请求体: 空");
                
            try (Response response = CLIENT.newCall(request).execute()) {
                int statusCode = response.code();
                String responseBody = response.body() != null ? response.body().string() : "";
                AppLogger.info("Token响应状态码: " + statusCode);
                AppLogger.info("Token响应头: " + response.headers().toString());
                AppLogger.info("Token响应体: " + responseBody);
                
                if (statusCode == 200) {
                    JsonNode json = MAPPER.readTree(responseBody);
                    if (json.has("access_token")) {
                        cachedAccessToken = json.get("access_token").asText();
                        AppLogger.info("成功获取Token: " + cachedAccessToken.substring(0, Math.min(cachedAccessToken.length(), 10)) + "...");
                        return cachedAccessToken;
                    } else {
                        AppLogger.warn("响应中未找到access_token字段: " + responseBody);
                    }
                } else {
                    AppLogger.error("Token请求失败: 状态码=" + statusCode + ", 响应体=" + responseBody);
                }
            }
        } catch (Exception e) {
            AppLogger.error("HttpUrl方式获取Token请求失败: " + e.getMessage(), e);
        }
        
        // 如果第一种方式失败，尝试使用表单提交方式
        try {
            AppLogger.info("=== 方法2：使用表单提交方式获取Token ===");
            
            String formBody = "grant_type=client_credentials" + 
                "&client_id=" + java.net.URLEncoder.encode(apiKey, "UTF-8") + 
                "&client_secret=" + java.net.URLEncoder.encode(secretKey, "UTF-8");
            
            AppLogger.info("Token表单请求URL: " + TOKEN_URL);
            AppLogger.info("Token表单请求体: " + formBody);
            
            RequestBody body = RequestBody.create(MediaType.parse("application/x-www-form-urlencoded"), formBody);
            Request request = new Request.Builder()
                .url(TOKEN_URL)
                .post(body)
                .build();
            
            AppLogger.info("Token表单请求头: " + request.headers().toString());
                
            try (Response response = CLIENT.newCall(request).execute()) {
                int statusCode = response.code();
                String responseBody = response.body() != null ? response.body().string() : "";
                AppLogger.info("Token表单响应状态码: " + statusCode);
                AppLogger.info("Token表单响应头: " + response.headers().toString());
                AppLogger.info("Token表单响应体: " + responseBody);
                
                if (statusCode == 200) {
                    JsonNode json = MAPPER.readTree(responseBody);
                    if (json.has("access_token")) {
                        cachedAccessToken = json.get("access_token").asText();
                        AppLogger.info("表单方式成功获取Token: " + cachedAccessToken);
                        return cachedAccessToken;
                    } else {
                        AppLogger.warn("表单方式响应中未找到access_token字段: " + responseBody);
                    }
                } else {
                    AppLogger.error("表单方式Token请求失败: 状态码=" + statusCode + ", 响应体=" + responseBody);
                }
            }
        } catch (Exception e) {
            AppLogger.error("表单方式获取Token失败: " + e.getMessage(), e);
        }
        
        // 如果所有方法都失败，可以尝试加载备用Token
        try {
            Properties props = loadPropertiesFile("backup-tokens.properties");
            if (props != null) {
                String backupToken = props.getProperty("baidu.api.backup.token");
                if (backupToken != null && !backupToken.isEmpty()) {
                    AppLogger.warn("使用备用Token");
                    return backupToken;
                }
            }
        } catch (Exception e) {
            AppLogger.error("加载备用Token失败: " + e.getMessage(), e);
        }
        
        // 如果方法失败，记录详细错误并返回模拟Token以避免UI阻塞
        AppLogger.warn("所有Token获取方式均失败，使用模拟Token继续运行。请检查API凭证是否正确，网络连接是否正常。");
        return "mock_token_for_testing_when_api_unavailable";
    }
    
    /**
     * 使用百度API纠正文本
     * @param text 需要纠正的文本
     * @return 纠正结果
     * @throws Exception 纠正过程中的异常
     */
    public static CorrectionResult correct(String text) throws Exception {
        if (text == null || text.trim().isEmpty()) {
            return new CorrectionResult(text, new ArrayList<>());
        }
        
        String trackingId = AppLogger.setTrackingId();
        AppLogger.textCorrectionStart(text.length());
        long startTime = System.currentTimeMillis();
        
        try {
            AppLogger.info("==== 开始文本纠错请求 ====");
            AppLogger.info("输入文本长度: " + text.length() + " 字符");
            AppLogger.info("输入文本前100字符: " + text.substring(0, Math.min(text.length(), 100)) + (text.length() > 100 ? "..." : ""));
            
            String token = getToken();
            String requestUrl = CORRECTION_API_URL + token;
            AppLogger.info("纠错请求URL: " + requestUrl);
            
            // 构建请求体
            ObjectNode requestBody = MAPPER.createObjectNode();
            requestBody.put("text", text);
            
            String jsonBody = MAPPER.writeValueAsString(requestBody);
            AppLogger.info("纠错请求体: " + jsonBody);
            
            // 构建请求
            RequestBody body = RequestBody.create(MediaType.parse("application/json"), jsonBody);
            Request request = new Request.Builder()
                .url(requestUrl)
                .post(body)
                .build();
            
            AppLogger.info("纠错请求头: " + request.headers().toString());
            
            // 发送请求
            try (Response response = CLIENT.newCall(request).execute()) {
                long duration = System.currentTimeMillis() - startTime;
                int statusCode = response.code();
                String responseBody = response.body().string();
                
                // 记录API响应
                AppLogger.info("纠错响应状态码: " + statusCode);
                AppLogger.info("纠错响应头: " + response.headers().toString());
                AppLogger.info("纠错响应耗时: " + duration + "ms");
                AppLogger.info("纠错响应体: " + responseBody);
                
                if (!response.isSuccessful()) {
                    String errorMsg = "百度API请求失败，状态码: " + response.code();
                    AppLogger.error(errorMsg);
                    AppLogger.apiError("BaiduAPI", errorMsg, null);
                    throw new Exception(errorMsg);
                }
                
                AppLogger.info("开始解析纠错响应");
                CorrectionResult result = parseResponse(text, responseBody);
                
                // 记录纠错结果
                AppLogger.info("纠错结果: 原文本长度=" + text.length() + 
                             ", 纠正后长度=" + result.getCorrectedText().length() + 
                             ", 纠正数量=" + result.getCorrections().size());
                
                if (!result.getCorrections().isEmpty()) {
                    AppLogger.info("纠正详情:");
                    for (int i = 0; i < result.getCorrections().size(); i++) {
                        TextCorrection correction = result.getCorrections().get(i);
                        AppLogger.info("  " + (i+1) + ". 原文: \"" + correction.getOriginal() + 
                                     "\" -> 纠正: \"" + correction.getCorrected() + 
                                     "\" " + correction.getPosition());
                    }
                }
                
                return result;
            }
        } catch (Exception e) {
            AppLogger.error("文本纠错过程中发生异常: " + e.getMessage(), e);
            throw e;
        } finally {
            AppLogger.info("==== 文本纠错请求结束 ====");
            AppLogger.clearTrackingId();
        }
    }
    
    /**
     * 异步使用百度API纠正文本
     * @param text 需要纠正的文本
     * @param callback 回调函数
     */
    public static void correctAsync(String text, final CorrectionCallback callback) {
        // 设置请求跟踪ID
        String traceId = AppLogger.setTrackingId();
        AppLogger.textCorrectionStart(text.length());
        AppLogger.info("==== 开始异步百度文本纠错处理 ====");
        AppLogger.info("输入文本长度：" + text.length() + " 字符");
        AppLogger.info("输入文本前100字符: " + text.substring(0, Math.min(text.length(), 100)) + (text.length() > 100 ? "..." : ""));
        
        try {
            // 异步获取访问令牌
            CompletableFuture.supplyAsync(() -> {
                try {
                    AppLogger.info("异步获取Token开始");
                    return getToken();
                } catch (Exception e) {
                    AppLogger.error("获取令牌异常：" + e.getMessage(), e);
                    callback.onFailure(e);
                    return null;
                }
            }).thenAccept(token -> {
                if (token == null) return;
                
                AppLogger.info("异步获取Token成功: " + token.substring(0, Math.min(token.length(), 10)) + "...");
                
                // 构建请求URL
                String requestUrl = CORRECTION_API_URL + token;
                AppLogger.info("异步纠错请求URL: " + requestUrl);
                
                // 构建请求体
                ObjectNode requestBody = MAPPER.createObjectNode();
                requestBody.put("text", text);
                String jsonBody;
                try {
                    jsonBody = MAPPER.writeValueAsString(requestBody);
                    AppLogger.info("异步纠错请求体: " + jsonBody);
                } catch (Exception e) {
                    AppLogger.error("JSON序列化异常：" + e.getMessage(), e);
                    callback.onFailure(e);
                    return;
                }
                
                // 构建请求
                Request request = new Request.Builder()
                        .url(requestUrl)
                        .post(RequestBody.create(MediaType.parse("application/json"), jsonBody))
                        .build();
                
                AppLogger.info("异步纠错请求头: " + request.headers().toString());
                
                // 发送请求
                long startTime = System.currentTimeMillis();
                CLIENT.newCall(request).enqueue(new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        AppLogger.error("异步API请求失败：" + e.getMessage(), e);
                        AppLogger.apiError("BaiduAPI-Async", "请求失败", e);
                        callback.onFailure(e);
                        AppLogger.clearTrackingId();
                    }
                    
                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        long duration = System.currentTimeMillis() - startTime;
                        int statusCode = response.code();
                        String responseBody = response.body().string();
                        
                        // 记录API响应
                        AppLogger.info("异步纠错响应状态码: " + statusCode);
                        AppLogger.info("异步纠错响应头: " + response.headers().toString());
                        AppLogger.info("异步纠错响应耗时: " + duration + "ms");
                        AppLogger.info("异步纠错响应体: " + responseBody);
                        
                        if (!response.isSuccessful()) {
                            String errorMsg = "百度API请求失败，状态码: " + response.code();
                            AppLogger.error(errorMsg);
                            AppLogger.apiError("BaiduAPI-Async", errorMsg, null);
                            callback.onFailure(new Exception(errorMsg));
                            AppLogger.clearTrackingId();
                            return;
                        }
                        
                        try {
                            AppLogger.info("开始解析异步纠错响应");
                            CorrectionResult result = parseResponse(text, responseBody);
                            
                            // 记录纠错结果
                            AppLogger.info("异步纠错结果: 原文本长度=" + text.length() + 
                                         ", 纠正后长度=" + result.getCorrectedText().length() + 
                                         ", 纠正数量=" + result.getCorrections().size());
                            
                            if (!result.getCorrections().isEmpty()) {
                                AppLogger.info("异步纠正详情:");
                                for (int i = 0; i < result.getCorrections().size(); i++) {
                                    TextCorrection correction = result.getCorrections().get(i);
                                    AppLogger.info("  " + (i+1) + ". 原文: \"" + correction.getOriginal() + 
                                                 "\" -> 纠正: \"" + correction.getCorrected() + 
                                                 "\" " + correction.getPosition());
                                }
                            }
                            
                            callback.onSuccess(result);
                        } catch (Exception e) {
                            AppLogger.error("解析异步响应异常：" + e.getMessage(), e);
                            callback.onFailure(e);
                        } finally {
                            AppLogger.info("==== 异步文本纠错请求结束 ====");
                            AppLogger.clearTrackingId();
                        }
                    }
                });
            });
        } catch (Exception e) {
            AppLogger.error("异步文本纠错准备过程中发生异常: " + e.getMessage(), e);
            callback.onFailure(e);
            AppLogger.clearTrackingId();
        }
    }
    
    /**
     * 异步纠错回调接口
     */
    public interface CorrectionCallback {
        void onSuccess(CorrectionResult result);
        void onFailure(Exception e);
    }
    
    /**
     * 解析API响应
     * @param originalText 原始文本
     * @param responseBody 响应体
     * @return 纠正结果
     * @throws Exception 解析过程中的异常
     */
    private static CorrectionResult parseResponse(String originalText, String responseBody) throws Exception {
        AppLogger.info("开始解析百度API响应");
        AppLogger.debug("响应内容: " + responseBody);
        
        try {
            JsonNode rootNode = MAPPER.readTree(responseBody);
            
            // 检查是否有错误
            if (rootNode.has("error_code")) {
                String errorMsg = "百度API错误: " + rootNode.get("error_msg").asText();
                AppLogger.error(errorMsg);
                throw new Exception(errorMsg);
            }
            
            // 获取纠正后的文本和原始文本
            // 注意：百度API返回的格式有多种可能
            String correctedText = originalText;
            List<TextCorrection> corrections = new ArrayList<>();
            
            // 方式1：直接使用text字段
            if (rootNode.has("text")) {
                correctedText = rootNode.path("text").asText(originalText);
            }
            
            // 方式2：从item中获取correct_query字段
            if (rootNode.has("item")) {
                JsonNode itemNode = rootNode.path("item");
                if (itemNode.has("correct_query")) {
                    correctedText = itemNode.path("correct_query").asText(correctedText);
                    AppLogger.info("从item.correct_query获取到纠正后的文本");
                }
                
                // 检查error_num和details字段
                if (itemNode.has("error_num") && itemNode.has("details")) {
                    int errorNum = itemNode.path("error_num").asInt(0);
                    AppLogger.info("检测到新的响应格式，错误数量: " + errorNum);
                    
                    // 从details数组中获取纠错信息
                    JsonNode detailsNode = itemNode.path("details");
                    if (detailsNode.isArray()) {
                        for (JsonNode detail : detailsNode) {
                            // 处理fragments
                            if (detail.has("vec_fragment") && detail.path("vec_fragment").isArray()) {
                                JsonNode fragments = detail.path("vec_fragment");
                                for (JsonNode fragment : fragments) {
                                    String oriText = fragment.path("ori_frag").asText("");
                                    String corrText = fragment.path("correct_frag").asText("");
                                    int beginPos = fragment.path("begin_pos").asInt(0);
                                    int endPos = fragment.path("end_pos").asInt(0);
                                    String explain = fragment.path("explain").asText("");
                                    
                                    if (!oriText.equals(corrText)) {
                                        String position = "位置: " + beginPos + "-" + endPos;
                                        TextCorrection correction = new TextCorrection(oriText, corrText, position);
                                        corrections.add(correction);
                                        
                                        AppLogger.debug("纠正项(details格式): '" + oriText + "' -> '" + 
                                                     corrText + "' " + position + " 原因: " + explain);
                                    }
                                }
                            }
                        }
                    }
                }
                
                // 如果还有text字段，也尝试获取
                if (itemNode.has("text")) {
                    if (correctedText.equals(originalText)) {
                        correctedText = itemNode.path("text").asText(correctedText);
                        AppLogger.info("从item.text获取到文本");
                    }
                }
            }
            
            // 检查items或item字段获取详细的纠错信息
            JsonNode itemsNode = null;
            if (rootNode.has("items")) {
                itemsNode = rootNode.path("items");
            } else if (rootNode.has("item") && !rootNode.path("item").has("details")) {
                // 只有当item中没有details时才尝试直接解析item
                itemsNode = rootNode.path("item");
            }
            
            if (itemsNode != null && !itemsNode.isMissingNode() && itemsNode.isArray()) {
                for (JsonNode item : itemsNode) {
                    String oriText = "";
                    String corrText = "";
                    int beginPos = 0;
                    int endPos = 0;
                    
                    // 尝试获取原始文本和纠正文本
                    if (item.has("ori_text")) {
                        oriText = item.path("ori_text").asText();
                    } else if (item.has("ori")) {
                        oriText = item.path("ori").asText();
                    }
                    
                    if (item.has("corr_text")) {
                        corrText = item.path("corr_text").asText();
                    } else if (item.has("correct")) {
                        corrText = item.path("correct").asText();
                    }
                    
                    // 尝试获取位置信息
                    if (item.has("begin_pos") && item.has("end_pos")) {
                        beginPos = item.path("begin_pos").asInt();
                        endPos = item.path("end_pos").asInt();
                    } else if (item.has("loc") && item.path("loc").has("offset")) {
                        beginPos = item.path("loc").path("offset").asInt();
                        endPos = beginPos + oriText.length();
                    }
                    
                    // 只有在实际不同时才添加纠正项
                    if (!oriText.equals(corrText)) {
                        String position = "位置: " + beginPos + "-" + endPos;
                        TextCorrection correction = new TextCorrection(oriText, corrText, position);
                        corrections.add(correction);
                        
                        AppLogger.debug("纠正项: '" + oriText + "' -> '" + corrText + "' " + position);
                    }
                }
            }
            
            // 如果没有从API获取到完整的纠正后文本，尝试自己构建
            if (correctedText.equals(originalText) && !corrections.isEmpty()) {
                correctedText = buildCorrectedText(originalText, corrections);
            }
            
            // 记录解析结果
            if (corrections.isEmpty()) {
                AppLogger.info("百度API响应解析完成，未发现纠正项");
            } else {
                AppLogger.info("百度API响应解析完成，共有 " + corrections.size() + " 处纠正");
            }
            
            return new CorrectionResult(correctedText, corrections);
        } catch (Exception e) {
            AppLogger.error("解析百度API响应失败: " + e.getMessage(), e);
            throw e;
        }
    }
    
    /**
     * 基于原文和错误项列表构建校正后的文本
     * 
     * @param originalText 原始文本
     * @param corrections 纠错项列表
     * @return 校正后的文本
     */
    private static String buildCorrectedText(String originalText, List<TextCorrection> corrections) {
        if (corrections.isEmpty()) {
            return originalText;
        }
        
        // 按照位置排序，从后向前替换，避免位置变化
        corrections.sort((c1, c2) -> {
            int pos1 = Integer.parseInt(c1.getPosition().replaceAll("位置: (\\d+)-.*", "$1"));
            int pos2 = Integer.parseInt(c2.getPosition().replaceAll("位置: (\\d+)-.*", "$1"));
            return Integer.compare(pos2, pos1); // 逆序，从后向前
        });
        
        StringBuilder result = new StringBuilder(originalText);
        
        for (TextCorrection correction : corrections) {
            String posStr = correction.getPosition().replaceAll("位置: (\\d+)-(\\d+)", "$1,$2");
            String[] positions = posStr.split(",");
            if (positions.length == 2) {
                int beginPos = Integer.parseInt(positions[0]);
                int endPos = Integer.parseInt(positions[1]);
                
                if (beginPos >= 0 && endPos <= result.length() && beginPos < endPos) {
                    result.replace(beginPos, endPos, correction.getCorrected());
                }
            }
        }
        
        return result.toString();
    }
} 