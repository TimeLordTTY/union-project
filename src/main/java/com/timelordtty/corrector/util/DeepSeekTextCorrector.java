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
        .build();
    
    // 配置信息 - 从配置文件读取
    private static String apiKey;
    
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
                String maskedApiKey = apiKey.substring(0, Math.min(5, apiKey.length())) + "..." + 
                                     (apiKey.length() > 10 ? apiKey.substring(apiKey.length() - 3) : "");
                AppLogger.info("使用DeepSeek API密钥: " + maskedApiKey);
                
                // 构建请求体
                ObjectNode requestBody = MAPPER.createObjectNode();
                requestBody.put("model", "deepseek-chat");
                
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
                AppLogger.info("DeepSeek请求体: " + jsonBody);
                
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
                    String responseBody = response.body().string();
                    
                    // 记录API响应
                    AppLogger.info("DeepSeek响应状态码: " + statusCode);
                    AppLogger.info("DeepSeek响应头: " + response.headers().toString());
                    AppLogger.info("DeepSeek响应耗时: " + duration + "ms");
                    AppLogger.info("DeepSeek原始响应体: " + responseBody);
                    
                    if (!response.isSuccessful()) {
                        String errorMsg = "DeepSeek API请求失败，状态码: " + response.code();
                        AppLogger.error(errorMsg);
                        throw new Exception(errorMsg);
                    }
                    
                    // 从DeepSeek响应中提取文本内容
                    JsonNode responseNode = MAPPER.readTree(responseBody);
                    String content = extractJsonFromResponse(responseNode);
                    
                    AppLogger.info("从DeepSeek响应中提取的JSON: " + content);
                    
                    // 解析提取的JSON内容
                    CorrectionResult result = parseResponse(text, content);
                    
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
            } catch (Exception e) {
                // 其他类型的异常直接抛出
                AppLogger.error("DeepSeek API请求发生非超时异常: " + e.getMessage(), e);
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
                    String maskedApiKey = apiKey.substring(0, Math.min(5, apiKey.length())) + "..." + 
                                         (apiKey.length() > 10 ? apiKey.substring(apiKey.length() - 3) : "");
                    AppLogger.info("使用DeepSeek API密钥: " + maskedApiKey);
                    
                    // 构建请求体
                    ObjectNode requestBody = MAPPER.createObjectNode();
                    requestBody.put("model", "deepseek-chat");
                    
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
                    AppLogger.info("异步DeepSeek请求体: " + jsonBody);
                    
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
                            } else {
                                if (retryCount[0] >= MAX_RETRIES) {
                                    AppLogger.error("异步DeepSeek API请求已达到最大重试次数 (" + MAX_RETRIES + ")，放弃重试");
                                } else {
                                    AppLogger.error("异步DeepSeek API请求失败（非超时异常）: " + e.getMessage(), e);
                                }
                                callback.onFailure(e);
                                AppLogger.clearTrackingId();
                            }
                        }
                        
                        @Override
                        public void onResponse(Call call, Response response) throws IOException {
                            long duration = System.currentTimeMillis() - startTime;
                            int statusCode = response.code();
                            String responseBody = response.body().string();
                            
                            // 记录API响应
                            AppLogger.info("异步DeepSeek响应状态码: " + statusCode);
                            AppLogger.info("异步DeepSeek响应头: " + response.headers().toString());
                            AppLogger.info("异步DeepSeek响应耗时: " + duration + "ms");
                            AppLogger.info("异步DeepSeek原始响应体: " + responseBody);
                            
                            if (!response.isSuccessful()) {
                                String errorMsg = "DeepSeek API请求失败，状态码: " + response.code();
                                AppLogger.error(errorMsg);
                                callback.onFailure(new Exception(errorMsg));
                                AppLogger.clearTrackingId();
                                return;
                            }
                            
                            try {
                                // 从DeepSeek响应中提取文本内容
                                JsonNode responseNode = MAPPER.readTree(responseBody);
                                String content = extractJsonFromResponse(responseNode);
                                
                                AppLogger.info("从异步DeepSeek响应中提取的JSON: " + content);
                                
                                AppLogger.info("开始解析异步纠错响应");
                                CorrectionResult result = parseResponse(text, content);
                                
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
     * 解析API响应
     * @param originalText 原始文本
     * @param responseBody 响应体
     * @return 纠正结果
     * @throws Exception 解析过程中的异常
     */
    private static CorrectionResult parseResponse(String originalText, String responseBody) throws Exception {
        AppLogger.info("开始解析DeepSeek API响应");
        AppLogger.debug("响应内容: " + responseBody);
        
        try {
            JsonNode rootNode = MAPPER.readTree(responseBody);
            
            // 检查是否有错误
            if (rootNode.has("error_code")) {
                String errorMsg = "DeepSeek API错误: " + rootNode.get("error_msg").asText();
                AppLogger.error(errorMsg);
                throw new Exception(errorMsg);
            }
            
            // 获取纠正后的文本和原始文本
            String correctedText = originalText;
            List<TextCorrection> corrections = new ArrayList<>();
            
            // 从item中获取correct_query字段
            if (rootNode.has("item")) {
                JsonNode itemNode = rootNode.path("item");
                if (itemNode.has("correct_query")) {
                    correctedText = itemNode.path("correct_query").asText(correctedText);
                    AppLogger.info("从item.correct_query获取到纠正后的文本");
                }
                
                // 检查error_num和details字段
                if (itemNode.has("error_num") && itemNode.has("details")) {
                    int errorNum = itemNode.path("error_num").asInt(0);
                    AppLogger.info("检测到格式正确的响应，错误数量: " + errorNum);
                    
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
                if (itemNode.has("text") && correctedText.equals(originalText)) {
                    correctedText = itemNode.path("text").asText(correctedText);
                    AppLogger.info("从item.text获取到文本");
                }
            }
            
            // 如果没有从API获取到完整的纠正后文本，尝试自己构建
            if (correctedText.equals(originalText) && !corrections.isEmpty()) {
                correctedText = buildCorrectedText(originalText, corrections);
            }
            
            // 记录解析结果
            if (corrections.isEmpty()) {
                AppLogger.info("DeepSeek API响应解析完成，未发现纠正项");
            } else {
                AppLogger.info("DeepSeek API响应解析完成，共有 " + corrections.size() + " 处纠正");
            }
            
            return new CorrectionResult(correctedText, corrections);
        } catch (Exception e) {
            AppLogger.error("解析DeepSeek API响应失败: " + e.getMessage(), e);
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
} 