package com.timelordtty.corrector.controller;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.concurrent.CompletableFuture;

import com.timelordtty.AppLogger;
import com.timelordtty.corrector.model.TextCorrection;
import com.timelordtty.corrector.util.BaiduTextCorrector;
import com.timelordtty.corrector.util.DeepSeekTextCorrector;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

/**
 * 文本校正控制器
 * 使用百度API或DeepSeek API进行文本校正功能
 */
public class TextCorrectorController implements Initializable {

    @FXML
    private TextArea inputTextArea;
    
    @FXML
    private TextFlow correctedTextFlow;
    
    @FXML
    private TextArea correctionDetailsTextArea;
    
    @FXML
    private Button correctButton;
    
    @FXML
    private ProgressIndicator progressIndicator;
    
    @FXML
    private TextArea resultTextArea;
    
    @FXML
    private TableView<TextCorrection> correctionTableView;
    
    @FXML
    private TableColumn<TextCorrection, String> originalColumn;
    
    @FXML
    private TableColumn<TextCorrection, String> correctedColumn;
    
    @FXML
    private TableColumn<TextCorrection, String> positionColumn;
    
    @FXML
    private CheckBox useDeepSeekCheckBox;
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        System.out.println("文本校正器界面已初始化");
        
        // 添加null检查以避免NullPointerException
        if (progressIndicator != null) {
            progressIndicator.setVisible(false);
        }
        
        // 初始化TableView列 - 添加null检查
        if (originalColumn != null && correctedColumn != null && positionColumn != null) {
            originalColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getOriginal()));
            correctedColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getCorrected()));
            positionColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getPosition()));
        }
        
        // 在界面加载完成后立即预加载百度API Token
        preloadBaiduToken();
    }
    
    /**
     * 预加载百度API Token
     * 在进入Tab时就开始获取Token，避免第一次校正时的延迟
     */
    private void preloadBaiduToken() {
        AppLogger.info("开始预加载百度API Token");
        
        // 在后台线程中执行，避免阻塞UI
        new Thread(() -> {
            try {
                // 调用预加载Token方法
                AppLogger.info("预加载Token: 尝试获取百度API访问令牌");
                BaiduTextCorrector.preloadToken();
                AppLogger.info("Token预加载请求已发送，将在后台处理");
                
                // 在UI线程中提示用户(可选)
                Platform.runLater(() -> {
                    // 这里可以更新UI，例如显示一个小图标表示API已准备好
                    // 但为了不打扰用户，这里不显示任何提示
                });
            } catch (Exception e) {
                AppLogger.error("预加载百度API Token时发生异常: " + e.getMessage(), e);
            }
        }, "baidu-api-token-preload-thread").start();
    }
    
    /**
     * 执行文本校正
     */
    @FXML
    private void correctText() {
        if (inputTextArea == null) {
            showAlert(Alert.AlertType.ERROR, "错误", "文本输入区域未初始化!");
            return;
        }
        
        String text = inputTextArea.getText();
        if (text.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "错误", "请输入需要校正的文本!");
            return;
        }
        
        // 清空之前的结果
        if (correctedTextFlow != null) {
            correctedTextFlow.getChildren().clear();
        }
        if (correctionDetailsTextArea != null) {
            correctionDetailsTextArea.clear();
        }
        if (resultTextArea != null) {
            resultTextArea.clear();
        }
        if (correctionTableView != null) {
            correctionTableView.getItems().clear();
        }
        
        // 显示进度指示器
        if (progressIndicator != null) {
            progressIndicator.setVisible(true);
        }
        
        // 确定使用哪个API
        boolean useDeepSeek = useDeepSeekCheckBox != null && useDeepSeekCheckBox.isSelected();
        
        // 记录用户操作和输入
        AppLogger.info("====== 用户文本纠错操作开始 ======");
        AppLogger.info("用户请求文本纠错，输入长度: " + text.length() + " 字符");
        AppLogger.info("输入文本前100字符: " + text.substring(0, Math.min(text.length(), 100)) + (text.length() > 100 ? "..." : ""));
        AppLogger.info("使用API: " + (useDeepSeek ? "DeepSeek" : "百度"));
        
        // 异步执行校正，避免UI冻结
        CompletableFuture.runAsync(() -> {
            try {
                AppLogger.info("开始调用" + (useDeepSeek ? "DeepSeek" : "百度") + " API进行文本纠错");
                long startTime = System.currentTimeMillis();
                
                // 调用API进行文本纠错
                Object result;
                if (useDeepSeek) {
                    // 对于DeepSeek API，如果文本过长，使用分块处理
                    final int LARGE_TEXT_THRESHOLD = 1000; // 定义长文本阈值
                    if (text.length() > LARGE_TEXT_THRESHOLD) {
                        AppLogger.info("文本长度超过" + LARGE_TEXT_THRESHOLD + "字符，将使用分块处理");
                        result = DeepSeekTextCorrector.correctLargeText(text);
                    } else {
                        result = DeepSeekTextCorrector.correct(text);
                    }
                } else {
                    result = BaiduTextCorrector.correct(text);
                }
                
                long duration = System.currentTimeMillis() - startTime;
                String correctedText;
                java.util.List<TextCorrection> correctionList;
                
                if (useDeepSeek) {
                    DeepSeekTextCorrector.CorrectionResult deepSeekResult = (DeepSeekTextCorrector.CorrectionResult) result;
                    correctedText = deepSeekResult.getCorrectedText();
                    correctionList = deepSeekResult.getCorrections();
                } else {
                    BaiduTextCorrector.CorrectionResult baiduResult = (BaiduTextCorrector.CorrectionResult) result;
                    correctedText = baiduResult.getCorrectedText();
                    correctionList = baiduResult.getCorrections();
                }
                
                AppLogger.info("文本纠错调用完成，耗时: " + duration + "ms");
                AppLogger.info("纠错结果: 原文本长度=" + text.length() + 
                             ", 纠正后长度=" + correctedText.length() + 
                             ", 纠正数量=" + correctionList.size());
                
                if (!correctionList.isEmpty()) {
                    AppLogger.info("纠正详情:");
                    for (int i = 0; i < correctionList.size(); i++) {
                        TextCorrection correction = correctionList.get(i);
                        AppLogger.info("  " + (i+1) + ". 原文: \"" + correction.getOriginal() + 
                                     "\" -> 纠正: \"" + correction.getCorrected() + 
                                     "\" " + correction.getPosition());
                    }
                } else {
                    AppLogger.info("未发现需要纠正的内容");
                }
                
                // 检查是否有实际的纠正
                boolean hasActualCorrections = false;
                if (!text.equals(correctedText)) {
                    hasActualCorrections = true;
                    AppLogger.info("原文与纠正后文本不同，存在纠正");
                }
                
                // 最终确定的纠正状态
                final boolean finalHasCorrections = !correctionList.isEmpty() || hasActualCorrections;
                final String finalCorrectedText = correctedText;
                final java.util.List<TextCorrection> finalCorrectionList = correctionList;
                
                // 在UI线程中更新界面
                Platform.runLater(() -> {
                    AppLogger.info("开始更新UI展示纠错结果");
                    
                    // 更新校正后文本
                    displayCorrectedText(text, finalCorrectedText);
                    
                    // 更新结果文本区域
                    if (resultTextArea != null) {
                        resultTextArea.setText(finalCorrectedText);
                    }
                    
                    // 更新TableView
                    if (correctionTableView != null) {
                        correctionTableView.getItems().clear();
                        if (!finalCorrectionList.isEmpty()) {
                            correctionTableView.getItems().addAll(finalCorrectionList);
                        }
                    }
                    
                    // 更新纠错详情文本区域
                    if (correctionDetailsTextArea != null) {
                        if (finalHasCorrections) {
                            StringBuilder sb = new StringBuilder();
                            if (!finalCorrectionList.isEmpty()) {
                                sb.append("发现 ").append(finalCorrectionList.size()).append(" 处需要纠正的内容：\n\n");
                                
                                for (TextCorrection correction : finalCorrectionList) {
                                    sb.append("原文: \"").append(correction.getOriginal())
                                      .append("\" -> 纠正: \"").append(correction.getCorrected())
                                      .append("\" ").append(correction.getPosition()).append("\n");
                                }
                            } else {
                                sb.append("文本已纠正，但可能采用了整体纠正方式，未获取到具体纠正项。");
                            }
                            correctionDetailsTextArea.setText(sb.toString());
                        } else {
                            correctionDetailsTextArea.setText("文本检查完成，未发现需要纠正的内容。");
                        }
                    }
                    
                    // 隐藏进度指示器
                    if (progressIndicator != null) {
                        progressIndicator.setVisible(false);
                    }
                    
                    AppLogger.info("UI更新完成，向用户显示结果对话框");
                    if (finalHasCorrections) {
                        showAlert(Alert.AlertType.INFORMATION, "完成", 
                             "文本校正已完成! " + (finalCorrectionList.isEmpty() ? 
                             "文本已修正，但未获取到具体纠正项。" : 
                             "发现 " + finalCorrectionList.size() + " 处错误。"));
                    } else {
                        showAlert(Alert.AlertType.INFORMATION, "完成", "文本校正已完成! 未发现错误。");
                    }
                    AppLogger.info("====== 用户文本纠错操作结束 ======");
                });
            } catch (Exception e) {
                AppLogger.error("文本纠错失败: " + e.getMessage(), e);
                AppLogger.error("异常堆栈信息: ", e);
                
                Platform.runLater(() -> {
                    if (progressIndicator != null) {
                        progressIndicator.setVisible(false);
                    }
                    AppLogger.info("向用户显示错误对话框: " + e.getMessage());
                    showAlert(Alert.AlertType.ERROR, "错误", "文本校正失败: " + e.getMessage());
                    AppLogger.info("====== 用户文本纠错操作结束(出错) ======");
                });
            }
        });
    }
    
    /**
     * 清空文本
     */
    @FXML
    private void clearText() {
        if (inputTextArea != null) {
            inputTextArea.clear();
        }
        if (resultTextArea != null) {
            resultTextArea.clear();
        }
        if (correctedTextFlow != null) {
            correctedTextFlow.getChildren().clear();
        }
        if (correctionDetailsTextArea != null) {
            correctionDetailsTextArea.clear();
        }
    }
    
    /**
     * 复制校正结果
     */
    @FXML
    private void copyResult() {
        String content = "";
        
        // 尝试从resultTextArea获取内容
        if (resultTextArea != null && !resultTextArea.getText().isEmpty()) {
            content = resultTextArea.getText();
        } 
        // 如果没有内容，从correctedTextFlow获取
        else if (correctedTextFlow != null && !correctedTextFlow.getChildren().isEmpty()) {
            StringBuilder sb = new StringBuilder();
            for (javafx.scene.Node node : correctedTextFlow.getChildren()) {
                if (node instanceof Text) {
                    sb.append(((Text) node).getText());
                }
            }
            content = sb.toString();
        }
        
        // 复制到剪贴板
        if (!content.isEmpty()) {
            final Clipboard clipboard = Clipboard.getSystemClipboard();
            final ClipboardContent clipboardContent = new ClipboardContent();
            clipboardContent.putString(content);
            clipboard.setContent(clipboardContent);
            
            showAlert(Alert.AlertType.INFORMATION, "成功", "已复制到剪贴板!");
        } else {
            showAlert(Alert.AlertType.WARNING, "警告", "没有可复制的内容!");
        }
    }
    
    /**
     * 在TextFlow中显示校正后的文本，并高亮显示修改的部分
     */
    private void displayCorrectedText(String original, String corrected) {
        // 如果correctedTextFlow为null，直接返回
        if (correctedTextFlow == null) {
            return;
        }
        
        correctedTextFlow.getChildren().clear();
        
        // 如果原文和纠正后的文本相同，直接显示原文
        if (original.equals(corrected)) {
            Text text = new Text(original);
            text.setFill(javafx.scene.paint.Color.BLACK);
            correctedTextFlow.getChildren().add(text);
            return;
        }
        
        // 如果有纠正列表，使用纠正列表来标记错误
        if (correctionTableView != null && !correctionTableView.getItems().isEmpty()) {
            // 获取所有纠正
            java.util.List<TextCorrection> corrections = correctionTableView.getItems();
            
            // 将纠正项按位置排序
            corrections.sort((c1, c2) -> {
                int pos1 = extractStartPosition(c1.getPosition());
                int pos2 = extractStartPosition(c2.getPosition());
                return Integer.compare(pos1, pos2);
            });
            
            int currentIndex = 0;
            for (TextCorrection correction : corrections) {
                int startPos = extractStartPosition(correction.getPosition());
                int endPos = extractEndPosition(correction.getPosition());
                
                // 确保位置有效
                if (startPos < 0 || endPos > original.length() || startPos >= endPos) {
                    continue;
                }
                
                // 添加错误前的正常文本
                if (startPos > currentIndex) {
                    Text normalText = new Text(original.substring(currentIndex, startPos));
                    normalText.setFill(javafx.scene.paint.Color.BLACK);
                    correctedTextFlow.getChildren().add(normalText);
                }
                
                // 添加错误文本（标红）
                Text errorText = new Text(original.substring(startPos, endPos));
                errorText.setFill(javafx.scene.paint.Color.RED);
                errorText.setUnderline(true);
                correctedTextFlow.getChildren().add(errorText);
                
                currentIndex = endPos;
            }
            
            // 添加最后一部分正常文本
            if (currentIndex < original.length()) {
                Text normalText = new Text(original.substring(currentIndex));
                normalText.setFill(javafx.scene.paint.Color.BLACK);
                correctedTextFlow.getChildren().add(normalText);
            }
        } else {
            // 没有详细的纠正列表，使用差异比较
            applySinglePassDiff(original, corrected);
        }
    }
    
    /**
     * 从位置字符串中提取开始位置
     */
    private int extractStartPosition(String position) {
        try {
            // 假设位置格式为"位置: 12-34"
            return Integer.parseInt(position.replaceAll("位置: (\\d+)-.*", "$1"));
        } catch (Exception e) {
            return -1;
        }
    }
    
    /**
     * 从位置字符串中提取结束位置
     */
    private int extractEndPosition(String position) {
        try {
            // 假设位置格式为"位置: 12-34"
            return Integer.parseInt(position.replaceAll("位置: \\d+-(\\d+)", "$1"));
        } catch (Exception e) {
            return -1;
        }
    }
    
    /**
     * 应用简单的差异比较并在TextFlow中显示
     */
    private void applySinglePassDiff(String original, String corrected) {
        // 如果原文和纠正后的文本长度差距较大，简单显示纠正后的文本
        if (Math.abs(original.length() - corrected.length()) > original.length() * 0.3) {
            Text text = new Text(corrected);
            text.setFill(javafx.scene.paint.Color.BLACK);
            correctedTextFlow.getChildren().add(text);
            return;
        }
        
        // 尝试使用更好的差异比较
        int[][] lcs = computeLCS(original, corrected);
        int i = 0, j = 0;
        StringBuilder currentNormal = new StringBuilder();
        
        while (i < original.length() && j < corrected.length()) {
            if (original.charAt(i) == corrected.charAt(j)) {
                // 相同字符
                currentNormal.append(original.charAt(i));
                i++;
                j++;
            } else {
                // 先添加累积的正常文本
                if (currentNormal.length() > 0) {
                    Text normalText = new Text(currentNormal.toString());
                    normalText.setFill(javafx.scene.paint.Color.BLACK);
                    correctedTextFlow.getChildren().add(normalText);
                    currentNormal = new StringBuilder();
                }
                
                // 处理差异
                int nextMatch = findNextMatch(original, corrected, i, j, lcs);
                if (nextMatch == -1) {
                    // 无法找到下一个匹配，添加剩余文本
                    Text endText = new Text(corrected.substring(j));
                    endText.setFill(javafx.scene.paint.Color.BLACK);
                    correctedTextFlow.getChildren().add(endText);
                    break;
                }
                
                // 添加错误文本
                Text errorText = new Text(original.substring(i, nextMatch));
                errorText.setFill(javafx.scene.paint.Color.RED);
                errorText.setUnderline(true);
                correctedTextFlow.getChildren().add(errorText);
                
                i = nextMatch;
            }
        }
        
        // 添加剩余的正常文本
        if (currentNormal.length() > 0) {
            Text normalText = new Text(currentNormal.toString());
            normalText.setFill(javafx.scene.paint.Color.BLACK);
            correctedTextFlow.getChildren().add(normalText);
        }
        
        // 添加剩余的纠正文本
        if (j < corrected.length()) {
            Text remainingText = new Text(corrected.substring(j));
            remainingText.setFill(javafx.scene.paint.Color.BLACK);
            correctedTextFlow.getChildren().add(remainingText);
        }
    }
    
    /**
     * 计算最长公共子序列
     */
    private int[][] computeLCS(String s1, String s2) {
        int[][] dp = new int[s1.length() + 1][s2.length() + 1];
        
        for (int i = 1; i <= s1.length(); i++) {
            for (int j = 1; j <= s2.length(); j++) {
                if (s1.charAt(i - 1) == s2.charAt(j - 1)) {
                    dp[i][j] = dp[i - 1][j - 1] + 1;
                } else {
                    dp[i][j] = Math.max(dp[i - 1][j], dp[i][j - 1]);
                }
            }
        }
        
        return dp;
    }
    
    /**
     * 在给定位置之后找到下一个匹配
     */
    private int findNextMatch(String s1, String s2, int i, int j, int[][] lcs) {
        if (i >= s1.length() || j >= s2.length()) {
            return -1;
        }
        
        // 简单实现：查找下一个相同字符
        for (int k = i; k < s1.length(); k++) {
            for (int l = j; l < s2.length(); l++) {
                if (s1.charAt(k) == s2.charAt(l)) {
                    return k;
                }
            }
        }
        
        return -1;
    }
    
    /**
     * 显示警告对话框
     */
    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
} 