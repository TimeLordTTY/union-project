# 项目管理小助手 (重构版)

一个用于项目管理和日期规划的应用，主要功能是项目管理，同时集成了金额转换、文档生成和文本纠错等小工具。本项目是[原始项目](https://github.com/TimeLordTTY/union-project)的重构版，采用前后端分离架构。

## 系统架构

系统采用前后端分离架构：

- **后端**：Spring Boot + MyBatis + H2 数据库
- **前端**：Vue 3 + TypeScript + Pinia + Element Plus

## 功能特点

1. **项目管理**：项目管理的核心功能，支持项目创建、编辑、删除，以及日历视图展示。
   - 支持项目上网日期、报名截止日期、最早评审日期和预计评审日期的设置和自动计算。
   - 日历视图清晰展示各项目的关键日期，支持按月/周查看。
   - 节假日和非工作日自动标记，方便项目规划。
   - 项目提醒功能，自动提示近期关键日期。
   - 过期项目灰显，便于整理和跟进。

2. **集成工具**：页面右上角快捷访问以下小工具：
   - **金额转换**：实现数字金额与中文大写金额的互相转换。
   - **文档生成**：根据Word或Excel模板和数据文件生成新的文档。
   - **文本纠错**：使用百度API进行文本纠错，显示纠错前后的内容对比。

## 项目结构

```
project-assistant/
├── project-assistant-service/       # 后端服务
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/
│   │   │   │   └── com/timelordtty/
│   │   │   │       ├── projectcalendar/    # 项目管理模块
│   │   │   │       │   ├── controller/     # 控制器
│   │   │   │       │   ├── service/        # 服务层
│   │   │   │       │   ├── mapper/         # MyBatis映射
│   │   │   │       │   ├── model/          # 数据模型
│   │   │   │       │   └── util/           # 工具类
│   │   │   │       ├── amountconvert/      # 金额转换工具
│   │   │   │       ├── docgen/             # 文档生成工具
│   │   │   │       ├── corrector/          # 文本纠错工具
│   │   │   │       └── ProjectAssistantApplication.java
│   │   │   └── resources/
│   │   │       ├── mapper/                 # MyBatis XML映射文件
│   │   │       └── application.yml         # 应用配置
│   └── pom.xml                         # Maven配置
│
└── project-assistant-ui/              # 前端应用
    ├── src/
    │   ├── api/                       # API调用
    │   ├── assets/                    # 静态资源
    │   ├── components/                # 组件
    │   ├── router/                    # 路由配置
    │   ├── store/                     # Pinia状态
    │   ├── types/                     # TypeScript类型
    │   ├── views/                     # 视图组件
    │   ├── App.vue                    # 应用入口组件
    │   └── main.ts                    # 应用入口
    ├── index.html                     # HTML模板
    ├── package.json                   # NPM配置
    ├── tsconfig.json                  # TypeScript配置
    └── vite.config.ts                 # Vite配置
```

## 打包后的目录结构

打包后的应用位于 `ProjectAssistant` 目录，结构如下：

```
ProjectAssistant/
├── 启动小助手.bat                     # 一键启动脚本
├── 调试小助手.bat                     # 调试模式启动脚本
├── ~❤~宝宝专属项目管理小助手の甜蜜指引~❤~.txt   # 使用说明文档
├── data/                           # 数据目录（存储H2数据库文件）
├── web/                            # 前端静态文件目录
│   ├── index.html                  # 前端入口页面
│   ├── assets/                     # 前端资源文件
│   └── ...                         # 其他前端文件
└── service_data/                   # 后端服务数据目录
    ├── project-assistant-service-1.0.0.jar  # 后端服务JAR包
    ├── lib/                        # 依赖库目录
    ├── jre/                        # 内置Java运行环境
    ├── conf/                       # 配置文件目录
    │   └── application.yml         # 应用配置
    └── logs/                       # 日志目录
```

## 环境要求

### 开发环境
- JDK 17
- Maven 3.x
- Node.js 14.x 或更高版本
- NPM 6.x 或更高版本

### 运行环境
打包后的应用程序不需要额外环境配置，已内置JRE和所有必要的依赖项。

## 安装与运行

### 开发模式
#### 后端服务
```bash
cd project-assistant-service
mvn clean install
mvn spring-boot:run
```

#### 前端应用
```bash
cd project-assistant-ui
npm install
npm run dev
```

### 一键打包（免环境依赖）
项目提供了打包脚本：

```bash
cd project-assistant
package.bat
```

打包后的应用位于 `ProjectAssistant` 目录，双击 `启动小助手.bat` 即可运行，无需安装任何环境。

### 分发给用户
将整个 `ProjectAssistant` 目录复制给用户即可，用户只需双击 `启动小助手.bat` 文件，完全不需要安装任何环境或具备编程知识。

## 开发者

- 开发者：TimeLordTTY
- 代码重构：Claude 3.7
- 创建于：2023年
- 重构于：2025年

## 许可证

未指定 