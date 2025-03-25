# 多功能工具集

一个集成了多种实用功能的JavaFX桌面应用，包括金额转换、文档生成、文本纠错和文本替换等功能。

## 功能特点

1. **金额转换**：实现数字金额与中文大写金额的互相转换，支持复制结果。
2. **文档生成**：根据Word或Excel模板和数据文件生成新的文档，支持选择生成目录。
3. **文本纠错**：使用百度API进行文本纠错，显示纠错前后的内容对比和详细信息。
4. **文本替换**：支持添加多组替换规则，对文本进行批量替换处理。

## 环境要求

- Java版本：JDK 23（已内置在发布包中）
- 操作系统：Windows

## 安装与使用

1. 从发布页面下载最新的发布包。
2. 解压后，直接双击「多功能工具集.exe」运行程序。
3. 如果EXE文件无法启动，可以使用备用的「启动多功能工具集.bat」脚本启动应用。
4. 如果系统没有安装Java，可以使用「使用内置JRE启动.bat」脚本启动应用。

## 开发指南

### 项目结构

```
union-project/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/
│   │   │       └── timelordtty/
│   │   │           ├── controller/  # 功能控制器
│   │   │           ├── model/       # 数据模型
│   │   │           ├── utils/       # 工具类
│   │   │           └── MainApp.java # 应用入口
│   │   └── resources/
│   │       └── fxml/                # JavaFX 视图文件
├── PersonalApps/                    # 应用打包输出目录
├── package.bat                      # 打包脚本
└── pom.xml                          # Maven配置文件
```

### 编译与打包

1. 确保已安装JDK 23和Maven 3.9.9。
2. 在项目根目录执行以下命令编译项目：

```bash
mvn clean package
```

3. 执行打包脚本创建可分发的应用：

```bash
package.bat
```

4. 打包完成后，可在PersonalApps/多功能工具集目录找到可分发的应用程序。

## 技术栈

- JavaFX：用于构建桌面用户界面
- Apache POI：用于Word和Excel文档处理
- OkHttp：用于调用百度API进行文本纠错
- Jackson：用于JSON数据处理
- Launch4j：用于将Jar打包为Exe

## 使用的API

- 百度文本纠错API：用于实现文本纠错功能
  - API Key: CsdvxQbBbwYREpS2iy7cukmr
  - Secret Key: VzfeFhNb4DzVxXWKW2J2aMJM7uHgeUpg

## 开发者

- 开发者：TimeLordTTY
- 创建于：2023年
- 编码：UTF-8 