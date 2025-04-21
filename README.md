# 项目管理小助手（桌面版）

这是一个基于Electron + Vue.js + Spring Boot的完整桌面应用程序，专为项目管理需求打造。该应用集成了项目管理、金额转换、文档生成和文本纠错等功能，无需用户安装任何开发环境即可使用。

## 主要特点

- **真正的桌面应用** - 基于Electron打包，使用独立窗口运行，非浏览器页面
- **无环境依赖** - 内置JRE和所有必要组件，用户无需安装任何运行环境
- **一键安装** - 提供标准Windows安装程序，自动创建桌面和开始菜单快捷方式
- **数据本地存储** - 所有数据存储在本地，保证数据安全
- **简单易用** - 专为非技术用户设计的友好界面

## 功能模块

- **项目管理** - 项目创建、跟踪和关键日期管理
  - 支持项目上网日期、报名截止日期、最早评审日期和预计评审日期的设置和自动计算
  - 日历视图清晰展示各项目的关键日期，支持按月/周查看
  - 节假日和非工作日自动标记，方便项目规划
  - 项目提醒功能，自动提示近期关键日期
  - 过期项目灰显，便于整理和跟进
- **金额转换** - 数字金额与中文大写金额的互相转换
- **文档生成** - 根据模板和数据文件生成标准化文档
  - 支持Word和Excel两种模板格式
  - 提供模板创建模式和数据录入模式
  - 智能模板生成功能
  - 表格式数据录入界面
- **文本纠错** - 通过百度API进行文本纠错，对比纠错前后的内容

## 开发环境

- JDK 17
- Maven 3.9.9
- Node.js 18+
- Vue 3 + TypeScript + Vite
- Electron 30+

## 打包步骤

项目使用`new_package.bat`脚本进行一键打包，该脚本会：

1. 编译后端Spring Boot应用
2. 复制必要的资源文件（包括JRE、ffmpeg等）
3. 创建项目目录结构
4. 生成说明文档
5. 设置文件权限

如需完整打包（包括前端），请按照以下步骤操作：

1. 确保已安装JDK 17和Maven 3.9.9
2. 确保已安装Node.js 18+
3. 进入`project-assistant-ui`目录，运行：
   ```
   npm install
   npm run electron:build
   ```
4. 返回项目根目录，运行：
   ```
   new_package.bat
   ```

## 项目结构

```
project-assistant/
├── project-assistant-ui/           # 前端代码
│   ├── src/                        # Vue源代码
│   ├── electron/                   # Electron主进程代码
│   └── dist_electron/              # 打包输出目录
├── project-assistant-service/      # 后端代码
│   ├── src/                        # Java源代码
│   │   ├── main/java/com/timelordtty/
│   │   │   ├── projectcalendar/    # 项目管理模块
│   │   │   ├── docgen/             # 文档生成模块
│   │   │   ├── amountconvert/      # 金额转换模块
│   │   │   └── corrector/          # 文本纠错模块
│   │   └── resources/              # 配置文件
│   └── target/                     # 编译输出目录
├── ProjectAssistant/               # 打包输出目录
├── fix_package.bat                 # 修复打包脚本
├── new_package.bat                 # 新版打包脚本
└── launcher.js                     # 启动脚本
```

## 疑难解答

### 打包问题

1. **找不到JRE或ffmpeg.dll**
   - 确保JDK 17已安装在`D:\Soft\Java\jdk-17`，或修改`new_package.bat`中的路径
   - 确保ffmpeg.dll存在，或从互联网下载

2. **前端无法编译**
   - 确保Node.js版本兼容（推荐18+）
   - 运行`npm install`更新依赖
   - 检查`package.json`中的依赖版本

3. **后端无法启动**
   - 检查JRE是否正确打包
   - 查看`startup.log`和`debug.log`获取错误信息
   - 确保端口8080未被占用

## 开发指南

如需二次开发，请参考以下文件：

- `/project-assistant-ui/electron/` - Electron主进程代码
- `/project-assistant-ui/src/` - Vue前端代码
- `/project-assistant-service/` - Spring Boot后端代码

### 前端开发

1. 进入`project-assistant-ui`目录
2. 安装依赖：`npm install`
3. 开发模式：`npm run electron:dev`
4. 打包：`npm run electron:build`

### 后端开发

1. 进入`project-assistant-service`目录
2. 使用Maven编译：`mvn clean package -DskipTests`
3. 可以单独启动后端服务进行测试 