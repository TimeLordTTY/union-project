package com.timelordtty.corrector.controller;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.CompletableFuture;
import java.util.ArrayList;

import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.extractor.WordExtractor;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;

import com.timelordtty.AppLogger;
import com.timelordtty.corrector.model.ReplaceRule;
import com.timelordtty.corrector.model.TextCorrection;
import com.timelordtty.corrector.util.DeepSeekTextCorrector;
import com.timelordtty.corrector.util.ReplaceOperation;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.concurrent.Task;
import javafx.scene.paint.Color;

/**
 * 文本校正控制器
 * 使用DeepSeek API进行文本校正功能
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
    private Button replaceRuleButton;
    
    @FXML
    private Button executeReplaceButton;
    
    // 替换规则列表
    private List<ReplaceRule> replaceRules = new ArrayList<>();
    
    // 表示操作类型（纠错或替换）
    private enum OperationType {
        CORRECTION, REPLACEMENT
    }
    
    // 当前操作类型
    private OperationType currentOperation = OperationType.CORRECTION;
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        System.out.println("文本校正器界面已初始化");
        AppLogger.info("文本校正器界面已初始化");
        
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
        
        // 初始化默认替换规则
        initDefaultReplaceRules();
        
        // 设置文件拖放功能
        setupFileDragAndDrop();
    }
    
    /**
     * 初始化默认替换规则 - 这些只会在界面中展示，不会自动生效
     */
    private void initDefaultReplaceRules() {
        if (replaceRules == null) {
            replaceRules = new ArrayList<>();
        }
        
        // 确保默认规则存在（即使之前已经有了规则列表）
        boolean hasRule1 = false, hasRule2 = false, hasRule3 = false;
        
        for (ReplaceRule rule : replaceRules) {
            if (rule.getOriginalText().equals("我恨你")) hasRule1 = true;
            if (rule.getOriginalText().equals("好讨厌你")) hasRule2 = true;
            if (rule.getOriginalText().equals("烦死了")) hasRule3 = true;
        }
        
        // 添加默认规则（如果不存在）
        if (!hasRule1) replaceRules.add(new ReplaceRule("我恨你", "我爱你"));
        if (!hasRule2) replaceRules.add(new ReplaceRule("好讨厌你", "好喜欢你"));
        if (!hasRule3) replaceRules.add(new ReplaceRule("烦死了", "真开心"));
        
        AppLogger.info("初始化了 " + replaceRules.size() + " 条替换规则（包含默认规则）");
    }
    
    /**
     * 设置文件拖放功能
     */
    private void setupFileDragAndDrop() {
        if (inputTextArea == null) {
            return;
        }
        
        // 设置拖拽事件处理
        inputTextArea.setOnDragOver(event -> {
            // 判断是否是文件
            if (event.getDragboard().hasFiles()) {
                // 允许拖入
                event.acceptTransferModes(javafx.scene.input.TransferMode.COPY);
            }
            event.consume();
        });
        
        // 设置拖入事件处理
        inputTextArea.setOnDragDropped(event -> {
            boolean success = false;
            javafx.scene.input.Dragboard dragboard = event.getDragboard();
            
            if (dragboard.hasFiles()) {
                // 获取拖入的第一个文件
                File file = dragboard.getFiles().get(0);
                success = true;
                
                // 处理文件
                processDroppedFile(file);
            }
            
            event.setDropCompleted(success);
            event.consume();
        });
        
        // 设置拖入提示
        inputTextArea.setOnDragEntered(event -> {
            if (event.getDragboard().hasFiles()) {
                inputTextArea.setStyle("-fx-border-color: #0078d7; -fx-border-width: 2px; -fx-border-style: dashed;");
            }
            event.consume();
        });
        
        inputTextArea.setOnDragExited(event -> {
            inputTextArea.setStyle("");
            event.consume();
        });
    }
    
    /**
     * 处理拖放的文件
     */
    private void processDroppedFile(File file) {
        if (file == null || !file.exists()) {
            AppLogger.warn("拖放的文件不存在或为空");
            showAlert(Alert.AlertType.WARNING, "警告", "无法处理拖放的文件");
            return;
        }
        
        AppLogger.info("用户拖放了文件: " + file.getAbsolutePath() + 
                       ", 大小: " + (file.length() / 1024) + " KB");
        
        // 判断文件类型是否支持
        String fileName = file.getName().toLowerCase();
        if (!(fileName.endsWith(".txt") || fileName.endsWith(".doc") || fileName.endsWith(".docx"))) {
            AppLogger.warn("不支持的文件类型: " + fileName);
            showAlert(Alert.AlertType.WARNING, "不支持的文件类型", 
                    "目前仅支持TXT文本文件和Word文档(DOC/DOCX)。\n您拖放的文件: " + fileName);
            return;
        }
        
        // 显示进度指示器
        if (progressIndicator != null) {
            progressIndicator.setVisible(true);
        }
        
        // 禁用按钮，避免重复操作
        if (correctButton != null) {
            correctButton.setDisable(true);
        }
        
        // 异步加载文件内容，避免阻塞UI
        CompletableFuture.supplyAsync(() -> {
            try {
                // 读取文件内容
                AppLogger.info("开始读取拖放的文件内容，这可能需要一些时间...");
                String fileContent = readFileContent(file);
                AppLogger.info("拖放文件内容读取完成，文本长度: " + fileContent.length() + " 字符");
                return fileContent;
            } catch (Exception e) {
                AppLogger.error("读取拖放文件内容失败: " + e.getMessage(), e);
                throw new RuntimeException(e);
            }
        }).thenAccept(fileContent -> {
            // 在UI线程中更新文本区域
            Platform.runLater(() -> {
                try {
                    // 清空当前内容
                    if (inputTextArea != null) {
                        inputTextArea.clear();
                    }
                    if (resultTextArea != null) {
                        resultTextArea.clear();
                    }
                    if (correctedTextFlow != null) {
                        correctedTextFlow.getChildren().clear();
                    }
                    
                    // 检查文件内容大小，显示警告
                    if (fileContent.length() > 50000) {
                        AppLogger.warn("拖放文件内容很大 (" + fileContent.length() + " 字符)，纠错可能需要较长时间");
                        showAlert(Alert.AlertType.WARNING, "大文件提示", 
                                "您拖放的文件内容较大 (" + (fileContent.length() / 1000) + " KB)，" +
                                "纠错处理可能需要较长时间。\n\n" +
                                "建议选择使用DeepSeek API以获得更好的大文本处理能力。");
                    }
                    
                    // 设置文件内容到输入区域
                    inputTextArea.setText(fileContent);
                    
                    // 使文本区域获得焦点
                    inputTextArea.requestFocus();
                    
                    AppLogger.info("拖放文件内容已加载到输入框");
                } finally {
                    // 隐藏进度指示器
                    if (progressIndicator != null) {
                        progressIndicator.setVisible(false);
                    }
                    
                    // 重新启用按钮
                    if (correctButton != null) {
                        correctButton.setDisable(false);
                    }
                    
                    // 清除拖放样式
                    if (inputTextArea != null) {
                        inputTextArea.setStyle("");
                    }
                }
            });
        }).exceptionally(ex -> {
            Platform.runLater(() -> {
                // 发生异常时，隐藏进度指示器并重新启用按钮
                if (progressIndicator != null) {
                    progressIndicator.setVisible(false);
                }
                if (correctButton != null) {
                    correctButton.setDisable(false);
                }
                if (inputTextArea != null) {
                    inputTextArea.setStyle("");
                }
                
                showAlert(Alert.AlertType.ERROR, "错误", "无法读取拖放文件内容: " + ex.getMessage());
            });
            return null;
        });
    }
    
    /**
     * 从原文和修正文本中提取纠错项
     */
    private List<TextCorrection> extractCorrections(String original, String corrected) {
        List<TextCorrection> corrections = new ArrayList<>();
        
        try {
            if (original == null || corrected == null || original.equals(corrected)) {
                return corrections;
            }
            
            // 如果原文和纠正文本不同但没有具体纠错项，创建一个整体纠错项
            TextCorrection wholeCorrection = new TextCorrection(
                original.substring(0, Math.min(50, original.length())) + (original.length() > 50 ? "..." : ""),
                corrected.substring(0, Math.min(50, corrected.length())) + (corrected.length() > 50 ? "..." : ""),
                "整体修改"
            );
            corrections.add(wholeCorrection);
            
            AppLogger.info("创建了一个整体纠错项，因为没有具体纠错详情");
            
        } catch (Exception e) {
            AppLogger.error("提取纠错项时出错: " + e.getMessage(), e);
        }
        
        return corrections;
    }
    
    /**
     * 执行文本校正
     */
    @FXML
    public void correctText() {
        AppLogger.info("开始文本纠正");
        
        // 设置当前操作类型为纠错
        currentOperation = OperationType.CORRECTION;
        
        // 检查输入文本区域是否初始化
        if (inputTextArea == null) {
            showAlert(Alert.AlertType.ERROR, "错误", "输入文本区域未初始化");
            return;
        }

        // 获取输入文本
        String text = inputTextArea.getText();
        
        // 检查文本是否为空
        if (text == null || text.trim().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "警告", "请输入需要纠正的文本");
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
        
        AppLogger.info("使用DeepSeek API进行文本纠正，长度: " + text.length());
        
        // 创建纠错任务
        Task<DeepSeekTextCorrector.CorrectionResult> correctorTask = new Task<DeepSeekTextCorrector.CorrectionResult>() {
            @Override
            protected DeepSeekTextCorrector.CorrectionResult call() throws Exception {
                // 使用DeepSeek API进行纠错
                try {
                    AppLogger.info("开始调用DeepSeek API进行纠错");
                    DeepSeekTextCorrector deepSeekCorrector = new DeepSeekTextCorrector();
                    
                    // 对于长文本使用分块处理
                    if (text.length() > 3000) {
                        AppLogger.info("文本长度超过3000字符，使用分块处理");
                        return deepSeekCorrector.correctLargeText(text);
                    } else {
                        return deepSeekCorrector.correct(text);
                    }
                } catch (Exception e) {
                    AppLogger.error("调用DeepSeek API时出错: " + e.getMessage(), e);
                    throw e;
                }
            }
        };

        // 设置纠错任务成功完成后的操作
        correctorTask.setOnSucceeded(event -> {
            DeepSeekTextCorrector.CorrectionResult result = correctorTask.getValue();
            
            // 检查纠正结果是否为空
            if (result == null || result.getCorrectedText() == null) {
                showAlert(Alert.AlertType.ERROR, "错误", "获取纠正结果失败");
                if (progressIndicator != null) {
                    progressIndicator.setVisible(false);
                }
                return;
            }
            
            String correctedText = result.getCorrectedText();
            List<TextCorrection> correctionList = result.getCorrections();

            AppLogger.info("DeepSeek API纠错成功，结果长度: " + correctedText.length() + 
                         ", 发现 " + (correctionList != null ? correctionList.size() : 0) + " 处错误");

            // 更新纠正后的文本区域
            if (resultTextArea != null) {
                resultTextArea.setText(correctedText);
                AppLogger.info("已更新纠正后的文本区域");
            }
            
            // 更新纠错表格
            if (correctionTableView != null) {
                correctionTableView.getItems().clear();
                if (correctionList != null && !correctionList.isEmpty()) {
                    correctionTableView.getItems().addAll(correctionList);
                }
            }

            // 显示纠正后的文本，但只显示错误统计信息，不显示完整文本
            displayCorrectedText(text, correctedText, correctionList);

            // 更新进度指示器
            if (progressIndicator != null) {
                progressIndicator.setVisible(false);
            }
        });
        
        // 设置任务执行失败后的操作
        correctorTask.setOnFailed(event -> {
            Throwable exception = correctorTask.getException();
            AppLogger.error("纠错任务执行失败: " + (exception != null ? exception.getMessage() : "未知错误"), exception);

            // 更新进度指示器
            if (progressIndicator != null) {
                progressIndicator.setVisible(false);
            }
            
            // 显示错误提示
            showAlert(Alert.AlertType.ERROR, "纠错失败", "文本纠正过程中出错: " + 
                      (exception != null ? exception.getMessage() : "未知错误"));
        });
        
        // 启动纠错任务
        new Thread(correctorTask).start();
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
     * 显示纠正的文本
     */
    private void displayCorrectedText(String originalText, String correctedText, List<TextCorrection> corrections) {
        AppLogger.info("显示纠正后的文本");
        
        if (originalText == null || correctedText == null) {
            showAlert(Alert.AlertType.ERROR, "错误", "原文或纠正文本为空");
            return;
        }
        
        // 显示进度指示器
        progressIndicator.setVisible(false);
        
        // 清空之前的结果
        if (correctedTextFlow != null) correctedTextFlow.getChildren().clear();
        if (correctionTableView != null) correctionTableView.getItems().clear();
        
        try {
            // 不再显示完整文本，只在resultTextArea中显示
            // 只显示错误列表和统计信息
            if (corrections != null && !corrections.isEmpty()) {
                if (correctionTableView != null) {
                    correctionTableView.getItems().addAll(corrections);
                }
                
                // 显示错误数量统计
                int correctionCount = corrections.size();
                String summary = String.format("共发现 %d 处错误", correctionCount);
                
                // 添加统计信息到TextFlow，但不添加完整文本
                if (correctedTextFlow != null) {
                    // 只添加错误统计和详情按钮
                    Text summaryText = new Text(summary);
                    summaryText.setStyle("-fx-font-weight: bold;");
                    correctedTextFlow.getChildren().add(summaryText);
                    
                    // 添加查看详情按钮
                    addViewDetailsButton();
                }
                
                AppLogger.info("显示了 " + correctionCount + " 处错误");
            } else {
                // 没有发现错误
                if (correctedTextFlow != null) {
                    Text noErrorText = new Text("未发现需要纠正的内容");
                    noErrorText.setStyle("-fx-font-style: italic; -fx-fill: green;");
                    correctedTextFlow.getChildren().add(noErrorText);
                }
                
                AppLogger.info("未发现错误，原文和纠正后的文本相同");
            }
            
        } catch (OutOfMemoryError e) {
            AppLogger.error("显示纠正文本时内存溢出: " + e.getMessage(), e);
            handleMemoryOverflow();
        } catch (Exception e) {
            AppLogger.error("显示纠正文本时出错: " + e.getMessage(), e);
            showAlert(Alert.AlertType.ERROR, "错误", "显示纠正文本时出错: " + e.getMessage());
        }
    }
    
    /**
     * 处理内存溢出异常
     */
    private void handleMemoryOverflow() {
        AppLogger.error("显示文本时发生内存溢出");
        
        // 清空显示区域
        if (correctedTextFlow != null) correctedTextFlow.getChildren().clear();
        
        // 显示错误提示
        if (correctedTextFlow != null) {
            Text errorText = new Text("内存溢出：文本过大，无法显示完整差异\n\n");
            errorText.setFill(javafx.scene.paint.Color.RED);
            errorText.setStyle("-fx-font-weight: bold;");
            
            Text suggestionText = new Text(
                "建议：\n" +
                "1. 减少文本长度，分段处理\n" +
                "2. 增加JVM内存设置 (-Xmx512m或更高)\n" +
                "3. 尝试使用导出功能保存结果"
            );
            suggestionText.setFill(javafx.scene.paint.Color.BLACK);
            
            correctedTextFlow.getChildren().addAll(errorText, suggestionText);
        }
        
        // 显示弹窗提示
        showAlert(
            Alert.AlertType.ERROR, 
            "内存溢出", 
            "文本过大，无法显示完整差异。请减少文本长度或增加JVM内存设置。"
        );
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

    /**
     * 根据文件类型读取内容
     */
    private String readFileContent(File file) throws Exception {
        String fileName = file.getName().toLowerCase();
        StringBuilder content = new StringBuilder();
        
        // 检查文件大小，过大会记录警告
        long fileSizeKB = file.length() / 1024;
        if (fileSizeKB > 1024) {
            AppLogger.warn("文件较大: " + fileSizeKB + " KB，读取可能需要较长时间");
        }
        
        if (fileName.endsWith(".txt")) {
            // 读取TXT文件
            AppLogger.info("读取TXT文件内容");
            try (BufferedReader reader = new BufferedReader(
                     new InputStreamReader(new FileInputStream(file), detectCharset(file)))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    content.append(line).append("\n");
                }
            }
        } else if (fileName.endsWith(".docx")) {
            // 读取DOCX文件 (新格式Word)
            AppLogger.info("读取DOCX(新格式Word)文件内容");
            try (FileInputStream fis = new FileInputStream(file);
                 XWPFDocument document = new XWPFDocument(fis);
                 XWPFWordExtractor extractor = new XWPFWordExtractor(document)) {
                content.append(extractor.getText());
            }
        } else if (fileName.endsWith(".doc")) {
            // 读取DOC文件 (旧格式Word)
            AppLogger.info("读取DOC(旧格式Word)文件内容");
            try (FileInputStream fis = new FileInputStream(file);
                 HWPFDocument document = new HWPFDocument(fis);
                 WordExtractor extractor = new WordExtractor(document)) {
                content.append(extractor.getText());
            }
        } else {
            // 尝试作为文本文件读取
            AppLogger.info("尝试作为普通文本文件读取");
            try (BufferedReader reader = new BufferedReader(
                     new InputStreamReader(new FileInputStream(file), detectCharset(file)))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    content.append(line).append("\n");
                }
            }
        }
        
        return content.toString();
    }
    
    /**
     * 尝试检测文本文件的字符编码
     */
    private java.nio.charset.Charset detectCharset(File file) {
        // 默认使用UTF-8
        java.nio.charset.Charset charset = StandardCharsets.UTF_8;
        
        // 尝试检测编码
        byte[] buf = new byte[4096];
        try (FileInputStream fis = new FileInputStream(file)) {
            int read = fis.read(buf, 0, buf.length);
            if (read > 0) {
                // 检查BOM标记
                if (buf[0] == (byte)0xEF && buf[1] == (byte)0xBB && buf[2] == (byte)0xBF) {
                    charset = StandardCharsets.UTF_8;
                    AppLogger.info("检测到UTF-8 BOM标记");
                } else if (buf[0] == (byte)0xFE && buf[1] == (byte)0xFF) {
                    charset = StandardCharsets.UTF_16BE;
                    AppLogger.info("检测到UTF-16BE BOM标记");
                } else if (buf[0] == (byte)0xFF && buf[1] == (byte)0xFE) {
                    charset = StandardCharsets.UTF_16LE;
                    AppLogger.info("检测到UTF-16LE BOM标记");
                } else {
                    // 尝试判断是否为GBK
                    boolean maybeGBK = false;
                    for (int i = 0; i < read; i++) {
                        // GBK编码的特征
                        if (buf[i] < 0) {
                            if (i + 1 < read) {
                                int c = (buf[i] & 0xff) << 8 | (buf[i + 1] & 0xff);
                                if (c >= 0x8140 && c <= 0xFEFE) {
                                    maybeGBK = true;
                                    break;
                                }
                            }
                        }
                    }
                    
                    if (maybeGBK) {
                        try {
                            charset = java.nio.charset.Charset.forName("GBK");
                            AppLogger.info("可能是GBK编码，尝试使用GBK解码");
                        } catch (Exception e) {
                            AppLogger.warn("不支持GBK编码，将使用UTF-8");
                        }
                    } else {
                        AppLogger.info("未检测到特殊编码标记，使用默认UTF-8");
                    }
                }
            }
        } catch (IOException e) {
            AppLogger.warn("检测字符编码时出错: " + e.getMessage() + "，使用默认UTF-8");
        }
        
        return charset;
    }
    
    /**
     * 上传文件（Word或TXT）进行纠错
     */
    @FXML
    private void uploadFile() {
        AppLogger.info("用户点击上传文件按钮");
        
        // 创建文件选择器
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("选择文件");
        
        // 设置文件过滤器
        fileChooser.getExtensionFilters().addAll(
            new ExtensionFilter("文本文件", "*.txt"),
            new ExtensionFilter("Word文档", "*.doc", "*.docx"),
            new ExtensionFilter("所有文件", "*.*")
        );
        
        // 显示文件选择对话框
        Stage stage = (Stage) inputTextArea.getScene().getWindow();
        File selectedFile = fileChooser.showOpenDialog(stage);
        
        if (selectedFile != null) {
            // 处理选择的文件
            processDroppedFile(selectedFile);
        } else {
            AppLogger.info("用户取消了文件选择");
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
     * 打开替换规则对话框
     */
    @FXML
    public void openReplaceRuleDialog() {
        try {
            // 加载替换规则对话框FXML
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/ReplaceRuleDialog.fxml"));
            VBox dialogRoot = loader.load();
            
            // 获取控制器
            ReplaceRuleDialogController controller = loader.getController();
            
            // 设置已有的替换规则
            controller.setRuleList(replaceRules);
            
            // 创建对话框
            Stage dialogStage = new Stage();
            dialogStage.setTitle("文本替换规则管理");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(inputTextArea.getScene().getWindow());
            
            // 明确设置对话框尺寸
            dialogStage.setMinWidth(800);
            dialogStage.setMinHeight(650);
            dialogStage.setWidth(900);  // 设置初始宽度比最小值大一些
            dialogStage.setHeight(700); // 设置初始高度比最小值大一些
            
            // 允许用户调整对话框大小
            dialogStage.setResizable(true);
            
            // 设置场景
            Scene scene = new Scene(dialogRoot);
            dialogStage.setScene(scene);
            
            // 显示对话框并等待关闭
            dialogStage.showAndWait();
            
            // 获取更新后的替换规则列表
            replaceRules = controller.getRuleList();
            
            AppLogger.info("替换规则对话框已关闭，当前有 " + replaceRules.size() + " 条规则");
            
        } catch (Exception e) {
            AppLogger.error("打开替换规则对话框失败: " + e.getMessage(), e);
            showAlert(Alert.AlertType.ERROR, "错误", "打开替换规则对话框失败: " + e.getMessage());
        }
    }
    
    /**
     * 执行文本替换
     */
    @FXML
    public void executeReplace() {
        if (replaceRules == null || replaceRules.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "警告", "请先添加替换规则");
            return;
        }
        
        // 获取输入文本
        String text = inputTextArea.getText();
        if (text == null || text.trim().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "警告", "请输入需要替换的文本");
            return;
        }
        
        AppLogger.info("开始执行文本替换，文本长度: " + text.length());
        
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
        
        // 设置当前操作类型为替换
        currentOperation = OperationType.REPLACEMENT;
        
        // 创建替换任务
        Task<ReplaceOperation.ReplaceResult> replaceTask = new Task<>() {
            @Override
            protected ReplaceOperation.ReplaceResult call() throws Exception {
                return ReplaceOperation.replace(text, replaceRules);
            }
        };
        
        // 替换任务成功完成后的操作
        replaceTask.setOnSucceeded(event -> {
            ReplaceOperation.ReplaceResult result = replaceTask.getValue();
            String replacedText = result.getReplacedText();
            List<TextCorrection> replacements = result.getReplacements();
            
            AppLogger.info("文本替换完成，替换后文本长度: " + replacedText.length() + 
                          ", 执行了 " + replacements.size() + " 处替换");
            
            // 更新文本区域
            if (resultTextArea != null) {
                resultTextArea.setText(replacedText);
            }
            
            // 更新替换表格
            if (correctionTableView != null) {
                correctionTableView.getItems().clear();
                if (!replacements.isEmpty()) {
                    correctionTableView.getItems().addAll(replacements);
                }
            }
            
            // 显示替换结果
            displayReplacementResults(text, replacedText, replacements);
            
            // 隐藏进度指示器
            if (progressIndicator != null) {
                progressIndicator.setVisible(false);
            }
        });
        
        // 替换任务执行失败后的操作
        replaceTask.setOnFailed(event -> {
            Throwable exception = replaceTask.getException();
            AppLogger.error("替换任务执行失败: " + (exception != null ? exception.getMessage() : "未知错误"), exception);
            
            // 隐藏进度指示器
            if (progressIndicator != null) {
                progressIndicator.setVisible(false);
            }
            
            // 显示错误提示
            showAlert(Alert.AlertType.ERROR, "替换失败", "文本替换过程中出错: " + 
                     (exception != null ? exception.getMessage() : "未知错误"));
        });
        
        // 启动替换任务
        new Thread(replaceTask).start();
    }
    
    /**
     * 显示替换结果
     */
    private void displayReplacementResults(String originalText, String replacedText, List<TextCorrection> replacements) {
        AppLogger.info("显示替换结果");
        
        // 显示进度指示器
        progressIndicator.setVisible(false);
        
        // 清空之前的结果
        if (correctedTextFlow != null) correctedTextFlow.getChildren().clear();
        
        try {
            // 不再显示完整文本，只在resultTextArea中显示
            // 只显示错误列表和统计信息
            if (replacements != null && !replacements.isEmpty()) {
                // 显示替换数量统计
                int replacementCount = replacements.size();
                String summary = String.format("共执行了 %d 处替换", replacementCount);
                
                // 添加统计信息到TextFlow，但不添加完整文本
                if (correctedTextFlow != null) {
                    // 只添加替换统计和详情按钮
                    Text summaryText = new Text(summary);
                    summaryText.setStyle("-fx-font-weight: bold;");
                    correctedTextFlow.getChildren().add(summaryText);
                    
                    // 添加查看详情按钮
                    addViewDetailsButton();
                }
                
                AppLogger.info("显示了 " + replacementCount + " 处替换");
            } else {
                // 没有执行替换
                if (correctedTextFlow != null) {
                    Text noReplaceText = new Text("未执行任何替换操作");
                    noReplaceText.setStyle("-fx-font-style: italic; -fx-fill: green;");
                    correctedTextFlow.getChildren().add(noReplaceText);
                }
                
                AppLogger.info("未执行替换，替换前后文本相同");
            }
            
        } catch (Exception e) {
            AppLogger.error("显示替换结果时出错: " + e.getMessage(), e);
            showAlert(Alert.AlertType.ERROR, "错误", "显示替换结果时出错: " + e.getMessage());
        }
    }
    
    /**
     * 显示纠错详情，包含错误原因等更多信息
     */
    private void showCorrectionDetails() {
        if (correctionTableView == null || correctionTableView.getItems().isEmpty()) {
            showAlert(Alert.AlertType.INFORMATION, "详情", "未发现需要修改的内容");
            return;
        }
        
        try {
            // 加载替换规则对话框FXML（使用自定义布局而不是加载FXML）
            
            // 创建表格视图
            TableView<TextCorrection> detailsTable = new TableView<>();
            detailsTable.setEditable(false);
            
            // 创建表格列
            TableColumn<TextCorrection, String> indexCol = new TableColumn<>("序号");
            indexCol.setCellValueFactory(p -> {
                int index = correctionTableView.getItems().indexOf(p.getValue()) + 1;
                return new javafx.beans.property.SimpleStringProperty(String.valueOf(index));
            });
            indexCol.setPrefWidth(50);
            
            TableColumn<TextCorrection, String> originalCol = new TableColumn<>("原文本");
            originalCol.setCellValueFactory(p -> 
                new javafx.beans.property.SimpleStringProperty(p.getValue().getOriginal()));
            originalCol.setPrefWidth(250);
            
            TableColumn<TextCorrection, String> correctedCol = new TableColumn<>("修改后");
            correctedCol.setCellValueFactory(p -> 
                new javafx.beans.property.SimpleStringProperty(p.getValue().getCorrected()));
            correctedCol.setPrefWidth(250);
            
            TableColumn<TextCorrection, String> positionCol = new TableColumn<>("位置");
            positionCol.setCellValueFactory(p -> 
                new javafx.beans.property.SimpleStringProperty(p.getValue().getPosition()));
            positionCol.setPrefWidth(100);
            
            // 添加列到表格
            detailsTable.getColumns().addAll(indexCol, originalCol, correctedCol, positionCol);
            
            // 添加数据
            detailsTable.getItems().addAll(correctionTableView.getItems());
            
            // 为表格添加样式
            detailsTable.setStyle("-fx-font-size: 12px;");
            
            // 添加描述文本
            String operation = currentOperation == OperationType.REPLACEMENT ? "替换" : "错误";
            Label descriptionLabel = new Label("发现 " + correctionTableView.getItems().size() + " 处" + operation + "，详情如下：");
            descriptionLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");
            
            // 创建导出按钮
            Button exportButton = new Button("导出" + operation + "报告");
            exportButton.setOnAction(e -> exportCorrectionReport(detailsTable.getItems()));
            
            // 创建布局
            VBox content = new VBox(10);
            content.setPadding(new javafx.geometry.Insets(10));
            content.getChildren().addAll(descriptionLabel, detailsTable, exportButton);
            
            // 设置表格可以填充整个空间
            VBox.setVgrow(detailsTable, javafx.scene.layout.Priority.ALWAYS);
            
            // 创建一个Stage而不是Dialog，以便更好地控制大小调整
            Stage dialogStage = new Stage();
            
            // 根据当前操作类型设置标题
            if (currentOperation == OperationType.REPLACEMENT) {
                dialogStage.setTitle("文本替换详情");
            } else {
                dialogStage.setTitle("错误纠正详情");
            }
            
            // 设置模态
            dialogStage.initModality(Modality.APPLICATION_MODAL);
            dialogStage.initOwner(inputTextArea.getScene().getWindow());
            
            // 设置对话框尺寸
            dialogStage.setMinWidth(700);
            dialogStage.setMinHeight(500);
            dialogStage.setWidth(750);  // 设置初始宽度比最小值大一些
            dialogStage.setHeight(550); // 设置初始高度比最小值大一些
            
            // 允许用户调整对话框大小
            dialogStage.setResizable(true);
            
            // 创建场景
            Scene scene = new Scene(content);
            dialogStage.setScene(scene);
            
            // 添加关闭按钮的事件处理
            Button closeButton = new Button("关闭");
            closeButton.setOnAction(e -> dialogStage.close());
            closeButton.setPrefWidth(80);
            
            // 创建按钮布局
            HBox buttonBox = new HBox(10);
            buttonBox.setAlignment(javafx.geometry.Pos.CENTER_RIGHT);
            buttonBox.getChildren().add(closeButton);
            
            // 重新组织内容布局，将按钮放在底部
            content.getChildren().add(buttonBox);
            
            // 显示对话框
            dialogStage.showAndWait();
            
        } catch (Exception e) {
            AppLogger.error("显示纠错详情时出错: " + e.getMessage(), e);
            showAlert(Alert.AlertType.ERROR, "错误", "无法显示纠错详情: " + e.getMessage());
        }
    }
    
    /**
     * 导出错误报告到文件
     */
    private void exportCorrectionReport(java.util.List<TextCorrection> corrections) {
        if (corrections == null || corrections.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "导出", "没有可导出的纠错信息");
            return;
        }
        
        // 创建文件选择器
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("保存错误报告");
        fileChooser.getExtensionFilters().addAll(
            new ExtensionFilter("文本文件", "*.txt"),
            new ExtensionFilter("所有文件", "*.*")
        );
        fileChooser.setInitialFileName("错误报告_" + java.time.LocalDateTime.now().format(
            java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".txt");
        
        // 显示保存对话框
        Stage stage = (Stage) inputTextArea.getScene().getWindow();
        File file = fileChooser.showSaveDialog(stage);
        
        if (file != null) {
            try {
                // 构建报告内容
                StringBuilder report = new StringBuilder();
                report.append("文本纠错报告\n");
                report.append("生成时间: ").append(java.time.LocalDateTime.now().format(
                    java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))).append("\n\n");
                report.append("共发现 ").append(corrections.size()).append(" 处错误:\n\n");
                
                for (int i = 0; i < corrections.size(); i++) {
                    TextCorrection correction = corrections.get(i);
                    report.append(i+1).append(". 原文: \"").append(correction.getOriginal())
                          .append("\" -> 纠正: \"").append(correction.getCorrected())
                          .append("\" ").append(correction.getPosition()).append("\n");
                }
                
                // 写入文件
                java.nio.file.Files.write(file.toPath(), report.toString().getBytes(StandardCharsets.UTF_8));
                
                showAlert(Alert.AlertType.INFORMATION, "导出成功", "错误报告已保存到: " + file.getAbsolutePath());
                
            } catch (Exception e) {
                AppLogger.error("导出错误报告失败: " + e.getMessage(), e);
                showAlert(Alert.AlertType.ERROR, "导出失败", "无法保存错误报告: " + e.getMessage());
            }
        }
    }
    
    /**
     * 添加查看详情按钮
     */
    public void addViewDetailsButton() {
        if (correctionTableView != null && !correctionTableView.getItems().isEmpty()) {
            String buttonText = currentOperation == OperationType.REPLACEMENT ? "查看替换详情" : "查看错误详情";
            Button viewDetailsButton = new Button(buttonText);
            viewDetailsButton.setStyle("-fx-font-weight: bold; -fx-base: #4a86e8;");
            viewDetailsButton.setOnAction(e -> showCorrectionDetails());
            
            // 如果correctedTextFlow不为空，添加按钮
            if (correctedTextFlow != null) {
                // 在末尾添加按钮
                int lastIndex = correctedTextFlow.getChildren().size() - 1;
                if (lastIndex >= 0) {
                    correctedTextFlow.getChildren().add(new Text(" "));
                    correctedTextFlow.getChildren().add(viewDetailsButton);
                } else {
                    correctedTextFlow.getChildren().add(viewDetailsButton);
                }
            }
        }
    }

    /**
     * 调整布局，使其自适应
     */
    public void adjustLayout() {
        try {
            // 获取当前场景和窗口大小
            if (inputTextArea == null || resultTextArea == null) return;
            
            double sceneWidth = inputTextArea.getScene().getWidth();
            double sceneHeight = inputTextArea.getScene().getHeight();
            
            // 调整文本区域的大小
            inputTextArea.setPrefWidth(sceneWidth * 0.9);
            inputTextArea.setPrefHeight(sceneHeight * 0.4);
            
            resultTextArea.setPrefWidth(sceneWidth * 0.9);
            resultTextArea.setPrefHeight(sceneHeight * 0.4);
            
            // 调整纠错结果流的大小
            if (correctedTextFlow != null) {
                correctedTextFlow.setPrefWidth(sceneWidth * 0.9);
            }
        } catch (Exception e) {
            // 忽略调整布局时的异常
            System.err.println("调整文本纠错器布局时出错: " + e.getMessage());
        }
    }
} 