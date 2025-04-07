# 🧠 Cursor 提示词：宝宝专属项目管理小助手开发全指导

本文件是为 AI 编码助手 Cursor 准备的提示词文档。  
请按照以下提示构建一个完整的、可运行、可打包部署的前后端分离项目，项目名称为：

> **ProjectAssistant（项目管理小助手）**

---

## 🧱 项目架构与结构规范

整个项目统一放在根目录：`union-project`，包含以下两部分：

```
union-project/
├── project-assistant-ui/         # 前端：Vue3 + Electron 桌面应用
├── project-assistant-service/    # 后端：Java 17 + Spring Boot + H2 + MyBatis + lombok
```

---

## 💻 前端（project-assistant-ui）

- 使用 Vue 3 + Vite + TypeScript + Pinia
- 使用 Electron 打包成桌面应用（含图标、窗口管理）
- 打包后文件名为 `ProjectAssistant.exe`
- 与后端通过 `http://localhost:8082` 进行接口通信
- 所有资源、配置与日志写入 `../project-assistant/service_data/`

### 前端模块要求：

| 功能模块           | 说明                                                                 |
|--------------------|----------------------------------------------------------------------|
| 📅 项目日历         | 展示所有项目的起止时间、周期、提醒，支持月/周/日视图切换            |
| 🔔 提醒系统         | 每日启动自动提醒今日事项，可手动添加提醒项                          |
| 📋 项目详情         | 项目编号、标题、负责人、周期、阶段、备注等                          |
| ✏️ 错别字校正       | 支持自定义文本替换规则（支持词库导入导出）                           |
| 💰 金额转换         | 数字 ↔ 中文金额互转，支持粘贴板复制                                 |
| 📄 文档生成         | Word/Excel 模板的创建，编辑，数据填充，变量形式如 `${客户.姓名}`                      |
| 🧩 模块插件系统     | 每个模块封装为组件，可热加载/懒加载                                |

---

## 🧰 后端（project-assistant-service）

- 技术栈：Java 17 + Spring Boot 3.x + MyBatis（**注意：不是 MyBatis Plus**）
- maven 路径为 E:\Apache\apache-maven-3.9.9
- jdk 路径为：D:\Soft\Java\jdk-17
- 使用嵌入式 H2 数据库，数据库文件路径为 `../project-assistant/data/projectCalendar.mv.db`
- 打包后生成 `backend.jar`，运行路径固定为 `service_data/`
- 注意要有详细的日志和注释，注释里作者名称是timelordtty
- 包名是com.timelordtty

### 核心接口模块：

| 接口模块         | 路径                        | 功能描述                                  |
|------------------|-----------------------------|-------------------------------------------|
| 项目管理         | `/api/project`              | 增删改查项目信息                           |
| 日历数据         | `/api/calendar`             | 读取和保存日历项，生成提醒                |
| 文本替换规则     | `/api/rules`                | 管理错别字和替换规则                      |
| 金额转换         | `/api/amount`               | 数值 ↔ 中文金额的转换服务                 |
| 文档生成         | `/api/document`             | Word/Excel 填充模板生成导出文档           |
| 静态资源         | `/static/**`                | 提供文档模板、资源下载                    |
| 健康检查         | `/api/health`               | 启动检测与前端连接确认                    |

---

## 🗃 数据持久化说明
- 目前数据库文件已存在，名为projectCalendar.mv.db
- 所有数据保存在 `project-assistant/data/` 下
- 使用嵌入式 H2 数据库，兼容 `.mv.db` 文件格式
- 使用标准 MyBatis 作为 ORM（非 MyBatis Plus）
- 禁止自动建表（`spring.jpa.hibernate.ddl-auto=none`），确保兼容已有结构
- 支持 JSON 文件导入导出（可扩展）
- 日志文件统一输出至：`service_data/logs/`

---

## 🧪 构建打包脚本（build-all.bat）

作用：自动打包前后端程序，并生成部署目录 `project-assistant`

### 脚本功能：

1. 构建前端并打包成 `ProjectAssistant.exe`
2. 构建后端为 `backend.jar`
3. 创建部署目录结构：
   - `data/`
   - `service_data/`
   - 将 `projectCalendar.mv.db` 拷贝至 `data/`
   - 拷贝启动脚本与说明文档

---

## 🚀 启动脚本（启动小助手.bat）

```bat
@echo off
title 宝宝专属项目管理小助手正在启动中~
cd /d %~dp0service_data
start "" java -jar backend.jar
timeout /t 2 >nul
start "" ProjectAssistant.exe
exit
```

---

## 📘 说明文档（~❤~宝宝专属项目管理小助手の甜蜜指引~❤~.txt）

请自动生成温馨提示，包括以下内容：

- 如何启动程序
- 功能介绍（以 emoji 风格列举）
- 文档生成器使用指南（支持变量替换的占位符）
- 数据备份与升级方式
- 特别提醒：无需安装任何环境
- 彩蛋说明：模板里有 ❤️、无效规则是故意为之

---

## ✅ 附加要求

- 打包后产物即可部署，无需任何依赖（JDK、Node.js、数据库）
- 所有配置、模板、资源统一打包入 `service_data/` 中
- Electron 应用带有图标（参考项目图标样式）
- 所有模块需兼容截图中展示的功能与样式

---

请根据以上提示，为我生成完整可运行项目模板、打包脚本、启动脚本、Spring 配置、Electron 配置、API 文件结构、前端视图结构，确保功能完整、部署一致、结构清晰。


---

## 📌 项目日历模块数据结构：Project

以下是项目日历（Calendar）模块中的核心数据模型 Project，字段及其含义如下：

| 字段名                | 类型              | 描述                         |
|-----------------------|-------------------|------------------------------|
| `id`                  | Long              | 项目唯一标识                 |
| `name`                | String            | 项目名称                     |
| `reviewPeriod`        | int               | 评审周期（单位：自然日）     |
| `onlineDate`          | LocalDate         | 项目上网日期                 |
| `registrationPeriod`  | int               | 报名期限（单位：工作日）     |
| `registrationEndDate` | LocalDate         | 报名截止日期                 |
| `earliestReviewDate`  | LocalDate         | 最早可评审日期               |
| `expectedReviewTime`  | LocalDateTime     | 预计开标时间                 |
| `expertReviewTime`    | LocalDateTime     | 专家评审时间                 |
| `remark`              | String            | 项目备注信息                 |
| `statusText`          | String（派生）    | 当前项目状态文本（如进行中）|


此外，支持同时提供日期与时间版本的 Getter/Setter，以便兼容不同模块逻辑。
