# 项目管理小助手

这是一个基于Vue 3和Electron的桌面应用程序，用于帮助管理项目进度、日期和文档生成。

## 项目功能

1. **项目管理**
   - 项目列表展示
   - 项目添加、编辑和删除
   - 项目日期管理（开标时间、专家评审时间等）

2. **日历视图**
   - 月历视图，显示所有项目相关日期
   - 支持节假日标记
   - 支持项目事件类型区分（上网、开标、专家评审等）

3. **金额数字小插件**
   - 数字金额转中文大写
   - 中文金额转数字

4. **文档生成小插件**
   - 文档模板管理
   - 字段定义和替换
   - Word文档生成

5. **文本处理小插件**
   - 文本编辑和处理
   - 自定义规则替换
   - 批量处理

## 技术栈

- Vue 3 + TypeScript
- Vite
- Electron
- Pinia (状态管理)
- Vue Router
- Element Plus

## 开发指南

### 环境准备

确保您已安装以下工具：
- Node.js (v14+)
- npm 或 yarn

### 安装依赖

```bash
cd project-assistant-ui
npm install
```

### 开发运行

```bash
npm run dev
```

### 构建应用

```bash
npm run build
```

## 项目结构

```
project-assistant-ui/
├── electron/             # Electron主进程代码
│   ├── main/             # 主进程入口
│   └── preload/          # 预加载脚本
├── src/
│   ├── assets/           # 静态资源
│   ├── components/       # Vue组件
│   │   ├── CalendarView.vue       # 日历视图组件
│   │   ├── FinanceTool.vue        # 金额转换工具
│   │   ├── DocumentGenerator.vue  # 文档生成器
│   │   ├── TextProcessor.vue      # 文本处理器
│   │   └── ProjectListItem.vue    # 项目列表项组件
│   ├── router/           # 路由配置
│   ├── store/            # Pinia状态管理
│   ├── App.vue           # 主组件
│   └── main.ts           # 应用入口
├── index.html            # HTML模板
├── package.json          # 项目依赖
└── tsconfig.json         # TypeScript配置
```

## 后端接口

本应用连接到项目管理后端服务，详见 `project-assistant-service` 项目。 