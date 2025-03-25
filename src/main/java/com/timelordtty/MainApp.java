package com.timelordtty;

import java.io.IOException;
import java.net.URL;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * 多功能工具应用主类
 */
public class MainApp extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        try {
            // 加载主界面FXML
            URL fxmlUrl = getClass().getResource("/fxml/MainView.fxml");
            if (fxmlUrl == null) {
                System.err.println("找不到FXML资源: /fxml/MainView.fxml");
                // 尝试另一个路径
                fxmlUrl = getClass().getResource("/MainView.fxml");
                if (fxmlUrl == null) {
                    throw new IOException("无法加载FXML资源");
                }
            }
            
            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            Parent root = loader.load();
            
            // 设置标题和场景
            primaryStage.setTitle("多功能工具集");
            primaryStage.setScene(new Scene(root, 800, 600));
            primaryStage.setMinWidth(800);
            primaryStage.setMinHeight(600);
            primaryStage.show();
            
            System.out.println("应用启动成功!");
        } catch (Exception e) {
            System.err.println("应用启动失败: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * 应用入口点
     */
    public static void main(String[] args) {
        launch(args);
    }
} 