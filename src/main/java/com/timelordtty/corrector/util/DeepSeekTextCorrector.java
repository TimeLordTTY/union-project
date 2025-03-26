package com.timelordtty.corrector.util;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.timelordtty.AppLogger;
import com.timelordtty.corrector.model.TextCorrection;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * DeepSeek API服务，处理所有与DeepSeek API的交互
 * 使其返回格式与百度API一致
 * @author tianyu.tang
 */
public class DeepSeekTextCorrector {
    private static final ObjectMapper MAPPER = new ObjectMapper();
    
    // DeepSeek API URL
    private static final String DEEPSEEK_API_URL = "https://api.deepseek.com/chat/completions";
    
    // 增加超时时间，大型语言模型处理可能需要较长时间
    private static final OkHttpClient CLIENT = new OkHttpClient.Builder()
        .connectTimeout(90, TimeUnit.SECONDS)  // 增加到90秒
        .readTimeout(120, TimeUnit.SECONDS)    // 增加到120秒
        .writeTimeout(60, TimeUnit.SECONDS)    // 增加到60秒
        .retryOnConnectionFailure(true)        // 启用连接失败重试
        // 指定仅使用 HTTP/1.1，避免 HTTP/2 的 stream reset 问题
        .protocols(java.util.Arrays.asList(okhttp3.Protocol.HTTP_1_1))
        .addInterceptor(chain -> {
            // 自定义拦截器，用于记录请求和响应信息
            okhttp3.Request request = chain.request();
            
            // 添加额外的连接控制头
            request = request.newBuilder()
                    .header("Connection", "close")  // 使用短连接，避免连接池复用问题
                    .build();
            
            // 记录请求开始
            long startTime = System.currentTimeMillis();
            AppLogger.debug("OkHttp发送请求: " + request.url());
            
            // 尝试执行请求，并记录详细信息
            try {
                okhttp3.Response response = chain.proceed(request);
                long endTime = System.currentTimeMillis();
                AppLogger.debug("OkHttp请求完成，耗时: " + (endTime - startTime) + "ms, 状态码: " + response.code());
                return response;
            } catch (IOException e) {
                long endTime = System.currentTimeMillis();
                AppLogger.error("OkHttp请求失败，耗时: " + (endTime - startTime) + "ms, 错误: " + e.getMessage(), e);
                throw e;
            }
        })
        .build();
    
    // 配置信息 - 从配置文件读取
    private static String apiKey;
    
    // DeepSeek模型ID
    private static final String DEEPSEEK_MODEL_ID = "deepseek-chat";
    
    // 文本纠错提示词
    private static final String CORRECTION_PROMPT = "请检查以下文本中的错误并修正。仅输出JSON格式结果，不要添加任何其他内容。输出格式必须严格按照以下结构：\n" +
            "{\n" +
            "  \"item\": {\n" +
            "    \"text\": \"原文本\",\n" +
            "    \"error_num\": 错误数量,\n" +
            "    \"correct_query\": \"修正后的文本\",\n" +
            "    \"content_len\": 文本长度,\n" +
            "    \"details\": [\n" +
            "      {\n" +
            "        \"sentence\": \"原句子\",\n" +
            "        \"sentence_fixed\": \"修正后的句子\",\n" +
            "        \"sentence_id\": 句子ID,\n" +
            "        \"begin_sentence_offset\": 句子开始偏移量,\n" +
            "        \"end_sentence_offset\": 句子结束偏移量,\n" +
            "        \"begin_psent_cont_offset\": 段落中句子开始偏移量,\n" +
            "        \"end_psent_cont_offset\": 段落中句子结束偏移量,\n" +
            "        \"vec_fragment\": [\n" +
            "          {\n" +
            "            \"explain\": \"修改原因\",\n" +
            "            \"operation\": 2,\n" +
            "            \"score\": 得分,\n" +
            "            \"begin_pos\": 错误在句子中的开始位置,\n" +
            "            \"end_pos\": 错误在句子中的结束位置,\n" +
            "            \"ori_frag\": \"原文片段\",\n" +
            "            \"correct_frag\": \"修正片段\",\n" +
            "            \"explain_long\": \"\",\n" +
            "            \"label\": \"010200\",\n" +
            "            \"explain_structure\": \"\"\n" +
            "          }\n" +
            "        ]\n" +
            "      }\n" +
            "    ]\n" +
            "  },\n" +
            "  \"log_id\": 日志ID\n" +
            "}\n" +
            "\n" +
            "以下是需要检查的文本:\n";
    
    // API响应的JSON节点
    private static JsonNode apiResponse;
    
    static {
        // 加载API配置
        loadApiConfig();
    }
    
    /**
     * 从配置文件加载API配置
     */
    private static void loadApiConfig() {
        AppLogger.info("开始加载DeepSeek API配置");
        
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
                String propsApiKey = props.getProperty("deepseek.api.key");
                
                if (isValidApiKey(propsApiKey)) {
                    apiKey = propsApiKey;
                    AppLogger.info("成功从配置文件加载DeepSeek API凭证: " + configFile);
                    return;
                }
            }
        }
        
        // 如果未能从文件加载，使用默认值
        if (apiKey == null) {
            apiKey = "YOUR_DEEPSEEK_API_KEY"; // 默认值应该替换为实际的默认值
            AppLogger.warn("未能从配置文件加载DeepSeek API凭证，使用默认值");
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
        return key != null && !key.isEmpty() && !key.equals("${deepseek.api.key}");
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
     */
    public static void setCredentials(String apiKeyValue) {
        // 验证API密钥的有效性
        if (apiKeyValue == null || apiKeyValue.trim().isEmpty()) {
            AppLogger.warn("提供的API密钥为空");
        }
        
        apiKey = apiKeyValue;
    }
    
    /**
     * 使用DeepSeek API纠正文本
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
        
        // 重试次数和当前重试计数
        final int MAX_RETRIES = 3;
        int retryCount = 0;
        Exception lastException = null;
        
        while (retryCount < MAX_RETRIES) {
            try {
                if (retryCount > 0) {
                    AppLogger.info("第 " + retryCount + " 次重试DeepSeek API请求...");
                    // 在重试前稍微等待一下，避免立即重试
                    Thread.sleep(2000 * retryCount);
                }
                
                AppLogger.info("==== 开始DeepSeek文本纠错请求 ====");
                AppLogger.info("输入文本长度: " + text.length() + " 字符");
                AppLogger.info("输入文本前100字符: " + text.substring(0, Math.min(text.length(), 100)) + (text.length() > 100 ? "..." : ""));
                
                // 验证API密钥是否可用
                if (apiKey == null || apiKey.isEmpty()) {
                    throw new IOException("DeepSeek API密钥未设置，无法进行文本纠错");
                }
                
                // 安全显示API密钥的一部分用于调试
                String maskedApiKey = maskApiKey(apiKey);
                AppLogger.info("使用DeepSeek API密钥: " + maskedApiKey);
                
                // 构建请求体 - 火山引擎格式
                ObjectNode requestBody = MAPPER.createObjectNode();
                requestBody.put("model", DEEPSEEK_MODEL_ID);
                
                ArrayNode messagesArray = MAPPER.createArrayNode();
                
                // 系统消息
                ObjectNode systemMessage = MAPPER.createObjectNode();
                systemMessage.put("role", "system");
                systemMessage.put("content", "你是一个专业的文本纠错助手，你需要检查文本中的拼写、语法和用词错误，然后按照要求的JSON格式返回结果。");
                messagesArray.add(systemMessage);
                
                // 用户消息
                ObjectNode userMessage = MAPPER.createObjectNode();
                userMessage.put("role", "user");
                userMessage.put("content", CORRECTION_PROMPT + text);
                messagesArray.add(userMessage);
                
                requestBody.set("messages", messagesArray);
                
                // 设置生成参数
                requestBody.put("temperature", 0.0); // 使用最确定性的输出
                requestBody.put("max_tokens", 4000); // 设置足够的输出长度
                requestBody.put("stream", false);
                
                String jsonBody = MAPPER.writeValueAsString(requestBody);
                
                // 记录请求体的摘要，而不是完整内容，避免日志过大
                String logJsonBody = jsonBody;
                if (logJsonBody.length() > 300) {
                    logJsonBody = logJsonBody.substring(0, 300) + "... [截断，完整长度:" + jsonBody.length() + "]";
                }
                AppLogger.info("DeepSeek请求体摘要: " + logJsonBody);
                
                // 构建请求
                RequestBody body = RequestBody.create(MediaType.parse("application/json"), jsonBody);
                String authHeader = "Bearer " + apiKey;
                Request request = new Request.Builder()
                    .url(DEEPSEEK_API_URL)
                    .post(body)
                    .addHeader("Authorization", authHeader)
                    .addHeader("Content-Type", "application/json")
                    .build();
                
                AppLogger.info("DeepSeek请求头: Authorization: Bearer " + maskedApiKey + "\nContent-Type: application/json");
                
                // 发送请求
                try (Response response = CLIENT.newCall(request).execute()) {
                    long duration = System.currentTimeMillis() - startTime;
                    int statusCode = response.code();
                    
                    // 记录API响应基本信息
                    AppLogger.info("DeepSeek响应状态码: " + statusCode);
                    AppLogger.info("DeepSeek响应头: " + response.headers().toString());
                    AppLogger.info("DeepSeek响应耗时: " + duration + "ms");
                    
                    // 安全地获取响应体，添加额外错误处理
                    String responseBody = "";
                    try {
                        if (response.body() != null) {
                            responseBody = response.body().string();
                            // 记录完整的响应体，不再截断
                            AppLogger.info("DeepSeek完整响应体: " + responseBody);
                        } else {
                            AppLogger.warn("DeepSeek返回了空响应体");
                        }
                    } catch (Exception e) {
                        AppLogger.error("读取DeepSeek响应体时发生异常: " + e.getMessage(), e);
                        throw new IOException("读取响应体失败: " + e.getMessage(), e);
                    }
                    
                    if (!response.isSuccessful()) {
                        String errorMsg = "DeepSeek API请求失败，状态码: " + response.code() + ", 响应: " + responseBody;
                        AppLogger.error(errorMsg);
                        throw new Exception(errorMsg);
                    }
                    
                    // 从DeepSeek响应中提取文本内容
                    JsonNode responseNode;
                    try {
                        responseNode = MAPPER.readTree(responseBody);
                    } catch (Exception e) {
                        AppLogger.error("解析DeepSeek JSON响应失败: " + e.getMessage() + ", 原始响应: " + responseBody, e);
                        throw new Exception("解析DeepSeek JSON响应失败: " + e.getMessage());
                    }
                    
                    String content = extractJsonFromResponse(responseNode);
                    AppLogger.info("从DeepSeek响应中提取的完整JSON: " + content);
                    
                    // 解析提取的JSON内容，使用安全解析方法
                    CorrectionResult result = safeParseResponse(text, content);
                    
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
                    
                    // 成功获取结果，返回
                    return result;
                }
            } catch (java.net.SocketTimeoutException e) {
                // 只有超时异常才重试
                lastException = e;
                retryCount++;
                AppLogger.warn("DeepSeek API请求超时，这是第 " + retryCount + " 次超时 (" + e.getMessage() + ")");
                
                if (retryCount >= MAX_RETRIES) {
                    AppLogger.error("已达到最大重试次数 (" + MAX_RETRIES + ")，放弃重试");
                }
                // 继续循环尝试重试
            } catch (okhttp3.internal.http2.StreamResetException e) {
                // 处理HTTP/2流重置异常，这通常是网络或服务端问题
                lastException = e;
                retryCount++;
                AppLogger.warn("DeepSeek API请求HTTP流被重置，错误: " + e.getMessage() + "，这是第 " + retryCount + " 次重试");
                
                if (retryCount >= MAX_RETRIES) {
                    AppLogger.error("已达到最大重试次数 (" + MAX_RETRIES + ")，放弃重试");
                } else {
                    // 更长的等待时间，给服务器更多恢复时间
                    try {
                        Thread.sleep(3000 * retryCount);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                    }
                }
                // 继续循环尝试重试
            } catch (IOException e) {
                // 网络相关的其他异常也可以重试
                lastException = e;
                retryCount++;
                AppLogger.warn("DeepSeek API请求网络错误: " + e.getMessage() + "，这是第 " + retryCount + " 次重试");
                
                if (retryCount >= MAX_RETRIES) {
                    AppLogger.error("已达到最大重试次数 (" + MAX_RETRIES + ")，放弃重试");
                } else {
                    // 延迟后重试
                    try {
                        Thread.sleep(2000 * retryCount);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                    }
                }
                // 继续循环尝试重试
            } catch (Exception e) {
                // 其他类型的异常直接抛出
                AppLogger.error("DeepSeek API请求发生非网络异常: " + e.getMessage(), e);
                throw e;
            }
        }
        
        // 如果达到这里，说明所有重试都失败了
        AppLogger.error("文本纠错过程中发生异常，所有重试均失败: " + (lastException != null ? lastException.getMessage() : "未知错误"));
        throw lastException != null ? lastException : new Exception("DeepSeek API请求失败，达到最大重试次数");
    }
    
    /**
     * 从DeepSeek响应中提取JSON内容
     */
    private static String extractJsonFromResponse(JsonNode responseNode) throws Exception {
        try {
            // 检查是否有choices字段
            if (responseNode.has("choices") && responseNode.get("choices").isArray()) {
                JsonNode choices = responseNode.get("choices");
                if (choices.size() > 0) {
                    JsonNode firstChoice = choices.get(0);
                    if (firstChoice.has("message") && firstChoice.get("message").has("content")) {
                        String content = firstChoice.get("message").get("content").asText();
                        
                        // 尝试从返回内容中提取JSON
                        Pattern jsonPattern = Pattern.compile("\\{.*\\}", Pattern.DOTALL);
                        Matcher matcher = jsonPattern.matcher(content);
                        
                        if (matcher.find()) {
                            return matcher.group(0);
                        } else {
                            AppLogger.warn("无法从DeepSeek响应中提取JSON内容");
                            return content; // 返回原始内容
                        }
                    }
                }
            }
            
            throw new Exception("DeepSeek响应格式不符合预期");
        } catch (Exception e) {
            AppLogger.error("解析DeepSeek响应时发生异常: " + e.getMessage(), e);
            throw e;
        }
    }
    
    /**
     * 异步使用DeepSeek API纠正文本
     * @param text 需要纠正的文本
     * @param callback 回调函数
     */
    public static void correctAsync(String text, final CorrectionCallback callback) {
        // 设置请求跟踪ID
        String traceId = AppLogger.setTrackingId();
        AppLogger.textCorrectionStart(text.length());
        AppLogger.info("==== 开始异步DeepSeek文本纠错处理 ====");
        AppLogger.info("输入文本长度：" + text.length() + " 字符");
        AppLogger.info("输入文本前100字符: " + text.substring(0, Math.min(text.length(), 100)) + (text.length() > 100 ? "..." : ""));
        
        // 实现异步重试
        final int MAX_RETRIES = 3;
        final int[] retryCount = {0}; // 使用数组以便在匿名内部类中修改
        
        // 定义一个可以递归调用自身的方法，用于实现重试
        class AsyncRetry {
            void execute() {
                try {
                    // 验证API密钥是否可用
                    if (apiKey == null || apiKey.isEmpty()) {
                        throw new IOException("DeepSeek API密钥未设置，无法进行文本纠错");
                    }
                    
                    // 安全显示API密钥的一部分用于调试
                    String maskedApiKey = maskApiKey(apiKey);
                    AppLogger.info("使用DeepSeek API密钥: " + maskedApiKey);
                    
                    // 构建请求体 - 火山引擎格式
                    ObjectNode requestBody = MAPPER.createObjectNode();
                    requestBody.put("model", DEEPSEEK_MODEL_ID);
                    
                    ArrayNode messagesArray = MAPPER.createArrayNode();
                    
                    // 系统消息
                    ObjectNode systemMessage = MAPPER.createObjectNode();
                    systemMessage.put("role", "system");
                    systemMessage.put("content", "你是一个专业的文本纠错助手，你需要检查文本中的拼写、语法和用词错误，然后按照要求的JSON格式返回结果。");
                    messagesArray.add(systemMessage);
                    
                    // 用户消息
                    ObjectNode userMessage = MAPPER.createObjectNode();
                    userMessage.put("role", "user");
                    userMessage.put("content", CORRECTION_PROMPT + text);
                    messagesArray.add(userMessage);
                    
                    requestBody.set("messages", messagesArray);
                    
                    // 设置生成参数
                    requestBody.put("temperature", 0.0); // 使用最确定性的输出
                    requestBody.put("max_tokens", 4000); // 设置足够的输出长度
                    requestBody.put("stream", false);
                    
                    String jsonBody = MAPPER.writeValueAsString(requestBody);
                    
                    // 记录请求体的摘要，而不是完整内容
                    String logJsonBody = jsonBody;
                    if (logJsonBody.length() > 300) {
                        logJsonBody = logJsonBody.substring(0, 300) + "... [截断，完整长度:" + jsonBody.length() + "]";
                    }
                    AppLogger.info("异步DeepSeek请求体摘要: " + logJsonBody);
                    
                    // 构建请求
                    String authHeader = "Bearer " + apiKey;
                    Request request = new Request.Builder()
                        .url(DEEPSEEK_API_URL)
                        .post(RequestBody.create(MediaType.parse("application/json"), jsonBody))
                        .addHeader("Authorization", authHeader)
                        .addHeader("Content-Type", "application/json")
                        .build();
                    
                    AppLogger.info("异步DeepSeek请求头: Authorization: Bearer " + maskedApiKey + "\nContent-Type: application/json");
                    
                    if (retryCount[0] > 0) {
                        AppLogger.info("第 " + retryCount[0] + " 次重试异步DeepSeek API请求");
                    }
                    
                    // 发送请求
                    long startTime = System.currentTimeMillis();
                    CLIENT.newCall(request).enqueue(new Callback() {
                        @Override
                        public void onFailure(Call call, IOException e) {
                            // 判断是否是超时异常
                            if (e instanceof java.net.SocketTimeoutException && retryCount[0] < MAX_RETRIES) {
                                retryCount[0]++;
                                AppLogger.warn("异步DeepSeek API请求超时，这是第 " + retryCount[0] + " 次超时 (" + e.getMessage() + ")");
                                
                                // 延迟一段时间后重试
                                try {
                                    Thread.sleep(2000 * retryCount[0]);
                                    AppLogger.info("准备重试异步DeepSeek API请求...");
                                    execute(); // 递归调用自己进行重试
                                } catch (InterruptedException ie) {
                                    AppLogger.error("重试延迟被中断: " + ie.getMessage());
                                    callback.onFailure(e); // 如果延迟被中断，也算失败
                                    AppLogger.clearTrackingId();
                                }
                            } 
                            // 处理HTTP/2流重置异常
                            else if (e instanceof okhttp3.internal.http2.StreamResetException && retryCount[0] < MAX_RETRIES) {
                                retryCount[0]++;
                                AppLogger.warn("异步DeepSeek API请求HTTP流被重置，错误: " + e.getMessage() + "，这是第 " + retryCount[0] + " 次重试");
                                
                                // 更长的等待时间，给服务器更多恢复时间
                                try {
                                    Thread.sleep(3000 * retryCount[0]);
                                    AppLogger.info("准备重试异步DeepSeek API请求...");
                                    execute(); // 递归调用自己进行重试
                                } catch (InterruptedException ie) {
                                    AppLogger.error("重试延迟被中断: " + ie.getMessage());
                                    callback.onFailure(e);
                                    AppLogger.clearTrackingId();
                                }
                            }
                            // 处理其他网络IO异常
                            else if (e instanceof IOException && retryCount[0] < MAX_RETRIES) {
                                retryCount[0]++;
                                AppLogger.warn("异步DeepSeek API请求网络错误: " + e.getMessage() + "，这是第 " + retryCount[0] + " 次重试");
                                
                                // 延迟后重试
                                try {
                                    Thread.sleep(2000 * retryCount[0]);
                                    AppLogger.info("准备重试异步DeepSeek API请求...");
                                    execute(); // 递归调用自己进行重试
                                } catch (InterruptedException ie) {
                                    AppLogger.error("重试延迟被中断: " + ie.getMessage());
                                    callback.onFailure(e);
                                    AppLogger.clearTrackingId();
                                }
                            }
                            else {
                                if (retryCount[0] >= MAX_RETRIES) {
                                    AppLogger.error("异步DeepSeek API请求已达到最大重试次数 (" + MAX_RETRIES + ")，放弃重试");
                                } else {
                                    AppLogger.error("异步DeepSeek API请求失败（无法重试的异常）: " + e.getMessage(), e);
                                }
                                callback.onFailure(e);
                                AppLogger.clearTrackingId();
                            }
                        }
                        
                        @Override
                        public void onResponse(Call call, Response response) throws IOException {
                            long duration = System.currentTimeMillis() - startTime;
                            int statusCode = response.code();
                            
                            // 记录API响应基本信息
                            AppLogger.info("异步DeepSeek响应状态码: " + statusCode);
                            AppLogger.info("异步DeepSeek响应头: " + response.headers().toString());
                            AppLogger.info("异步DeepSeek响应耗时: " + duration + "ms");
                            
                            // 安全地获取响应体，添加额外错误处理
                            String responseBody = "";
                            try {
                                if (response.body() != null) {
                                    responseBody = response.body().string();
                                    // 记录完整的响应体，不再截断
                                    AppLogger.info("异步DeepSeek完整响应体: " + responseBody);
                                } else {
                                    AppLogger.warn("异步DeepSeek返回了空响应体");
                                }
                            } catch (Exception e) {
                                AppLogger.error("读取异步DeepSeek响应体时发生异常: " + e.getMessage(), e);
                                callback.onFailure(new IOException("读取响应体失败: " + e.getMessage(), e));
                                AppLogger.clearTrackingId();
                                return;
                            }
                            
                            if (!response.isSuccessful()) {
                                String errorMsg = "DeepSeek API请求失败，状态码: " + response.code() + ", 响应: " + responseBody;
                                AppLogger.error(errorMsg);
                                callback.onFailure(new Exception(errorMsg));
                                AppLogger.clearTrackingId();
                                return;
                            }
                            
                            try {
                                // 从DeepSeek响应中提取文本内容
                                JsonNode responseNode;
                                try {
                                    responseNode = MAPPER.readTree(responseBody);
                                } catch (Exception e) {
                                    AppLogger.error("解析异步DeepSeek JSON响应失败: " + e.getMessage() + ", 原始响应: " + responseBody, e);
                                    callback.onFailure(new Exception("解析DeepSeek JSON响应失败: " + e.getMessage()));
                                    AppLogger.clearTrackingId();
                                    return;
                                }
                                
                                String content = extractJsonFromResponse(responseNode);
                                AppLogger.info("从异步DeepSeek响应中提取的完整JSON: " + content);
                                
                                AppLogger.info("开始解析异步纠错响应");
                                // 使用安全解析方法
                                CorrectionResult result = safeParseResponse(text, content);
                                
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
                                AppLogger.error("解析异步DeepSeek响应异常：" + e.getMessage(), e);
                                callback.onFailure(e);
                            } finally {
                                AppLogger.info("==== 异步DeepSeek文本纠错请求结束 ====");
                                AppLogger.clearTrackingId();
                            }
                        }
                    });
                } catch (Exception e) {
                    AppLogger.error("异步DeepSeek文本纠错准备过程中发生异常: " + e.getMessage(), e);
                    callback.onFailure(e);
                    AppLogger.clearTrackingId();
                }
            }
        }
        
        // 执行第一次尝试
        new AsyncRetry().execute();
    }
    
    /**
     * 异步纠错回调接口
     */
    public interface CorrectionCallback {
        void onSuccess(CorrectionResult result);
        void onFailure(Exception e);
    }
    
    /**
     * 掩盖API密钥，只显示前4位和后4位
     */
    private static String maskApiKey(String apiKey) {
        if (apiKey == null || apiKey.length() <= 8) {
            return "[密钥已隐藏]";
        }
        return apiKey.substring(0, 4) + "****" + apiKey.substring(apiKey.length() - 4);
    }
    
    /**
     * 解析API响应，提取纠错信息
     * @param originalText 原始文本
     * @param correctedText 纠正后的文本
     * @return 纠错结果对象
     */
    public static CorrectionResult parseResponse(String originalText, String correctedText) {
        AppLogger.info("开始解析API响应，提取纠错信息");
        
        List<TextCorrection> corrections = new ArrayList<>();
        
        try {
            // 如果原文与校正后文本相同，直接返回
            if (originalText.equals(correctedText)) {
                AppLogger.info("原文与纠正后文本相同，无需纠正");
                return new CorrectionResult(correctedText, corrections);
            }
            
            // 解析API响应中的错误项
            if (apiResponse != null && apiResponse.has("item") && !apiResponse.get("item").isNull()) {
                JsonNode itemNode = apiResponse.get("item");
                
                // 提取details数组
                if (itemNode.has("details") && itemNode.get("details").isArray()) {
                    JsonNode detailsArray = itemNode.get("details");
                    int totalErrors = 0;
                    
                    // 遍历每个句子的详情
                    for (int i = 0; i < detailsArray.size(); i++) {
                        JsonNode sentenceNode = detailsArray.get(i);
                        
                        // 提取sentence和vec_fragment
                        if (sentenceNode.has("sentence") && sentenceNode.has("vec_fragment") && 
                            sentenceNode.get("vec_fragment").isArray()) {
                            
                            String sentence = sentenceNode.get("sentence").asText();
                            JsonNode fragments = sentenceNode.get("vec_fragment");
                            
                            if (fragments.size() > 0) {
                                AppLogger.info("句子 #" + (i+1) + " 发现 " + fragments.size() + " 处错误");
                                
                                // 找到句子在原文中的位置
                                int sentenceStartPos = originalText.indexOf(sentence);
                                if (sentenceStartPos == -1) {
                                    // 如果找不到完全匹配，尝试使用模糊匹配
                                    sentenceStartPos = findApproximatePosition(originalText, sentence);
                                }
                                
                                // 遍历每个错误片段
                                for (int j = 0; j < fragments.size(); j++) {
                                    JsonNode fragment = fragments.get(j);
                                    
                                    if (fragment.has("ori_frag") && fragment.has("correct_frag") && 
                                        fragment.has("begin_pos") && fragment.has("end_pos")) {
                                        
                                        String originalFragment = fragment.get("ori_frag").asText();
                                        String correctedFragment = fragment.get("correct_frag").asText();
                                        int beginPos = fragment.get("begin_pos").asInt();
                                        int endPos = fragment.get("end_pos").asInt();
                                        
                                        // 计算在整个文本中的位置
                                        int globalBeginPos = sentenceStartPos + beginPos;
                                        int globalEndPos = sentenceStartPos + endPos;
                                        
                                        // 生成位置字符串
                                        String position = "位置: " + globalBeginPos + "-" + globalEndPos;
                                        
                                        // 创建TextCorrection对象
                                        TextCorrection correction = new TextCorrection(
                                            originalFragment, correctedFragment, position);
                                        
                                        // 如果有错误类型说明，添加到TextCorrection
                                        if (fragment.has("explain") && !fragment.get("explain").isNull()) {
                                            String explain = fragment.get("explain").asText();
                                            correction.setErrorType(explain);
                                        }
                                        
                                        corrections.add(correction);
                                        totalErrors++;
                                        
                                        AppLogger.debug("错误 #" + totalErrors + ": 原文=\"" + 
                                                     originalFragment + "\", 纠正=\"" + 
                                                     correctedFragment + "\", " + position);
                                    }
                                }
                            }
                        }
                    }
                    
                    AppLogger.info("从API响应中提取了 " + totalErrors + " 处错误");
                }
            }
            
            // 如果没有从API响应中提取到错误项，但文本确实被修改了
            if (corrections.isEmpty() && !originalText.equals(correctedText)) {
                AppLogger.info("API响应中未包含具体错误项，但文本已被修改");
                
                // 添加一个整体性的纠错项
                TextCorrection wholeCorrection = new TextCorrection(
                    originalText.length() > 50 ? 
                        originalText.substring(0, 50) + "..." : originalText,
                    correctedText.length() > 50 ? 
                        correctedText.substring(0, 50) + "..." : correctedText,
                    "整体纠正"
                );
                corrections.add(wholeCorrection);
            }
            
        } catch (Exception e) {
            AppLogger.error("解析API响应时出错: " + e.getMessage(), e);
        }
        
        return new CorrectionResult(correctedText, corrections);
    }
    
    /**
     * 处理大文本
     * 如果文本超过指定长度，将其分成多个块进行处理
     * 
     * @param text 需要纠正的文本
     * @return 纠正结果
     * @throws Exception 纠正过程中的异常
     */
    public static CorrectionResult correctLargeText(String text) throws Exception {
        if (text == null || text.trim().isEmpty()) {
            return new CorrectionResult(text, new ArrayList<>());
        }
        
        // 如果文本长度小于阈值，直接使用标准方法
        final int MAX_CHUNK_SIZE = 3000; // 每块最大字符数
        if (text.length() <= MAX_CHUNK_SIZE) {
            return correct(text);
        }
        
        AppLogger.info("文本过长（" + text.length() + " 字符），将分块处理");
        
        // 分割文本为多个块
        List<String> chunks = splitTextIntoChunks(text, MAX_CHUNK_SIZE);
        AppLogger.info("文本已分为 " + chunks.size() + " 个块");
        
        StringBuilder correctedTextBuilder = new StringBuilder();
        List<TextCorrection> allCorrections = new ArrayList<>();
        
        // 处理每个块
        int chunkOffset = 0;
        for (int i = 0; i < chunks.size(); i++) {
            String chunk = chunks.get(i);
            AppLogger.info("处理第 " + (i+1) + "/" + chunks.size() + " 块文本，长度: " + chunk.length() + " 字符");
            
            try {
                // 纠正当前块
                CorrectionResult chunkResult = correct(chunk);
                String correctedChunk = chunkResult.getCorrectedText();
                
                // 调整当前块的纠正项的位置
                List<TextCorrection> adjustedCorrections = new ArrayList<>();
                for (TextCorrection correction : chunkResult.getCorrections()) {
                    String posStr = correction.getPosition().replaceAll("位置: (\\d+)-(\\d+)", "$1,$2");
                    String[] positions = posStr.split(",");
                    if (positions.length == 2) {
                        int beginPos = Integer.parseInt(positions[0]) + chunkOffset;
                        int endPos = Integer.parseInt(positions[1]) + chunkOffset;
                        adjustedCorrections.add(new TextCorrection(
                            correction.getOriginal(),
                            correction.getCorrected(),
                            "位置: " + beginPos + "-" + endPos
                        ));
                    } else {
                        // 如果无法解析位置，使用原始纠正项
                        adjustedCorrections.add(correction);
                    }
                }
                
                // 添加到结果中
                correctedTextBuilder.append(correctedChunk);
                allCorrections.addAll(adjustedCorrections);
                
                // 更新偏移量
                chunkOffset += chunk.length();
                
                AppLogger.info("第 " + (i+1) + " 块处理完成，找到 " + chunkResult.getCorrections().size() + " 处纠正");
            } catch (Exception e) {
                AppLogger.error("处理第 " + (i+1) + " 块时出错: " + e.getMessage(), e);
                throw new Exception("处理文本分块时出错（块 " + (i+1) + "/" + chunks.size() + "）: " + e.getMessage(), e);
            }
        }
        
        // 返回完整结果
        return new CorrectionResult(correctedTextBuilder.toString(), allCorrections);
    }
    
    /**
     * 将文本分割为多个小块，尽量在句子边界处分割
     * 
     * @param text 原始文本
     * @param maxChunkSize 每块最大字符数
     * @return 文本块列表
     */
    private static List<String> splitTextIntoChunks(String text, int maxChunkSize) {
        List<String> chunks = new ArrayList<>();
        
        // 使用正则表达式找到所有句子的结束位置
        Pattern sentenceEndPattern = Pattern.compile("[.!?。！？\\n]+");
        Matcher matcher = sentenceEndPattern.matcher(text);
        
        int startIndex = 0;
        int lastSentenceEnd = 0;
        
        while (startIndex < text.length()) {
            // 寻找当前位置之后的下一个句子结束位置
            boolean found = false;
            int endIndex = Math.min(startIndex + maxChunkSize, text.length());
            
            // 如果当前块大小已经达到最大值，尝试在句子结束处分割
            if (endIndex < text.length()) {
                matcher.region(startIndex, Math.min(startIndex + maxChunkSize * 2, text.length()));
                
                while (matcher.find()) {
                    int sentenceEnd = matcher.end();
                    if (sentenceEnd > startIndex && sentenceEnd <= startIndex + maxChunkSize) {
                        lastSentenceEnd = sentenceEnd;
                    } else if (sentenceEnd > startIndex + maxChunkSize) {
                        break;
                    }
                }
                
                // 如果找到了合适的句子结束位置，使用它
                if (lastSentenceEnd > startIndex && lastSentenceEnd <= startIndex + maxChunkSize) {
                    endIndex = lastSentenceEnd;
                    found = true;
                }
            }
            
            // 如果没有找到合适的句子结束位置，但仍需要分割
            if (!found && endIndex < text.length()) {
                // 尝试在空格、标点等处分割
                for (int i = endIndex; i > startIndex; i--) {
                    char c = text.charAt(i);
                    if (Character.isWhitespace(c) || ",.;:!?，。；：！？".indexOf(c) >= 0) {
                        endIndex = i + 1;
                        found = true;
                        break;
                    }
                }
            }
            
            // 添加当前块
            chunks.add(text.substring(startIndex, endIndex));
            startIndex = endIndex;
        }
        
        return chunks;
    }
    
    /**
     * 在原文中查找句子的近似位置
     * 当无法精确匹配句子时使用
     * @param text 原始文本
     * @param sentence 要查找的句子
     * @return 找到的位置，如果找不到则返回0
     */
    private static int findApproximatePosition(String text, String sentence) {
        if (text == null || sentence == null || text.isEmpty() || sentence.isEmpty()) {
            return 0;
        }
        
        // 如果句子较长，尝试使用前一部分进行匹配
        if (sentence.length() > 20) {
            String prefix = sentence.substring(0, 20);
            int prefixPos = text.indexOf(prefix);
            if (prefixPos != -1) {
                return prefixPos;
            }
        }
        
        // 尝试寻找句子中的关键词
        String[] words = sentence.split("\\s+|[,.!?;，。！？；]");
        for (String word : words) {
            if (word.length() > 3) {  // 只考虑较长的词
                int wordPos = text.indexOf(word);
                if (wordPos != -1) {
                    return wordPos;
                }
            }
        }
        
        // 若无法找到，返回0
        return 0;
    }
    
    /**
     * 安全地解析API响应，如果解析失败则返回原文
     * 这确保即使API响应有问题，应用程序也不会崩溃
     * 
     * @param originalText 原始文本
     * @param jsonContent API返回的JSON内容
     * @return 纠错结果对象
     */
    private static CorrectionResult safeParseResponse(String originalText, String jsonContent) {
        if (originalText == null) {
            return new CorrectionResult("", new ArrayList<>());
        }
        
        try {
            // 尝试解析API响应
            ObjectMapper mapper = new ObjectMapper();
            JsonNode rootNode = mapper.readTree(jsonContent);
            
            // 确保返回了正确格式的响应
            if (rootNode.has("item") && rootNode.get("item").has("correct_query")) {
                String correctedText = rootNode.get("item").get("correct_query").asText();
                
                // 解析详情
                List<TextCorrection> corrections = new ArrayList<>();
                if (rootNode.get("item").has("details") && rootNode.get("item").get("details").isArray()) {
                    JsonNode detailsArray = rootNode.get("item").get("details");
                    
                    for (JsonNode detail : detailsArray) {
                        // 提取错误详情
                        if (detail.has("vec_fragment") && detail.get("vec_fragment").isArray()) {
                            JsonNode fragments = detail.get("vec_fragment");
                            
                            for (JsonNode fragment : fragments) {
                                if (fragment.has("ori_frag") && fragment.has("correct_frag")) {
                                    String original = fragment.get("ori_frag").asText();
                                    String corrected = fragment.get("correct_frag").asText();
                                    String position = "位置: 未知";
                                    
                                    if (fragment.has("begin_pos") && fragment.has("end_pos")) {
                                        int beginPos = fragment.get("begin_pos").asInt();
                                        int endPos = fragment.get("end_pos").asInt();
                                        position = "位置: " + beginPos + "-" + endPos;
                                    }
                                    
                                    TextCorrection correction = new TextCorrection(original, corrected, position);
                                    
                                    if (fragment.has("explain")) {
                                        correction.setErrorType(fragment.get("explain").asText());
                                    }
                                    
                                    corrections.add(correction);
                                }
                            }
                        }
                    }
                }
                
                // 如果没有具体的错误项但文本已修改，添加一个整体性纠错
                if (corrections.isEmpty() && !originalText.equals(correctedText)) {
                    corrections.add(new TextCorrection(
                        originalText.length() > 50 ? originalText.substring(0, 50) + "..." : originalText,
                        correctedText.length() > 50 ? correctedText.substring(0, 50) + "..." : correctedText,
                        "整体纠正"
                    ));
                }
                
                return new CorrectionResult(correctedText, corrections);
            } else {
                // 响应格式不正确，返回原文
                AppLogger.warn("API响应格式不正确，返回原文");
                return new CorrectionResult(originalText, new ArrayList<>());
            }
        } catch (Exception e) {
            // 解析异常，记录错误并返回原文
            AppLogger.error("解析API响应时发生异常: " + e.getMessage() + ", 返回原文", e);
            return new CorrectionResult(originalText, new ArrayList<>());
        }
    }
} 