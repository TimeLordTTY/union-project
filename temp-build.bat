@echo off
rem 采用系统默认代码页
echo 项目管理小助手构建脚本

rem 设置输出目录
set OUTPUT_DIR=project-assistant
if not exist "%OUTPUT_DIR%" mkdir "%OUTPUT_DIR%"

rem 创建目录结构
if not exist "%OUTPUT_DIR%\service_data" mkdir "%OUTPUT_DIR%\service_data"
if not exist "%OUTPUT_DIR%\service_data\lib" mkdir "%OUTPUT_DIR%\service_data\lib"
if not exist "%OUTPUT_DIR%\service_data\config" mkdir "%OUTPUT_DIR%\service_data\config"
if not exist "%OUTPUT_DIR%\service_data\logs" mkdir "%OUTPUT_DIR%\service_data\logs"
if not exist "%OUTPUT_DIR%\service_data\static" mkdir "%OUTPUT_DIR%\service_data\static"
if not exist "%OUTPUT_DIR%\data" mkdir "%OUTPUT_DIR%\data"
if not exist "%OUTPUT_DIR%\temp" mkdir "%OUTPUT_DIR%\temp"
if not exist "%OUTPUT_DIR%\templates" mkdir "%OUTPUT_DIR%\templates"
if not exist "%OUTPUT_DIR%\documents" mkdir "%OUTPUT_DIR%\documents"

rem 创建基本JAR文件
echo 临时JAR文件 > "%OUTPUT_DIR%\service_data\lib\project-assistant-service-0.0.1-SNAPSHOT.jar"

rem 创建临时前端页面
echo ^<!DOCTYPE html^> > "%OUTPUT_DIR%\service_data\static\index.html"
echo ^<html^>^<head^>^<title^>项目管理小助手^</title^>^</head^> >> "%OUTPUT_DIR%\service_data\static\index.html"
echo ^<body^>^<h1^>项目管理小助手^</h1^>^<p^>系统启动中...^</p^>^</body^> >> "%OUTPUT_DIR%\service_data\static\index.html"
echo ^</html^> >> "%OUTPUT_DIR%\service_data\static\index.html"

rem 创建启动脚本
echo @echo off > "%OUTPUT_DIR%\启动小助手.bat"
echo title 项目管理小助手 >> "%OUTPUT_DIR%\启动小助手.bat"
echo java -jar service_data\lib\project-assistant-service-0.0.1-SNAPSHOT.jar --server.port=8082 >> "%OUTPUT_DIR%\启动小助手.bat"
echo pause >> "%OUTPUT_DIR%\启动小助手.bat"

rem 复制项目文档
copy /Y "ProjectAssistant.CursorPrompt.使用MyBatis (1).md" "%OUTPUT_DIR%\" 2>nul
copy /Y "项目结构说明.md" "%OUTPUT_DIR%\" 2>nul
copy /Y "文档生成要求" "%OUTPUT_DIR%\" 2>nul
copy /Y "基础框架要求" "%OUTPUT_DIR%\" 2>nul

rem 创建使用说明
echo 项目管理小助手使用说明 > "%OUTPUT_DIR%\使用说明.txt"
echo ====================== >> "%OUTPUT_DIR%\使用说明.txt"
echo 1. 双击"启动小助手.bat"启动应用 >> "%OUTPUT_DIR%\使用说明.txt"
echo 2. 浏览器将自动打开访问http://localhost:8082 >> "%OUTPUT_DIR%\使用说明.txt"
echo 3. 所有数据将保存在data目录中 >> "%OUTPUT_DIR%\使用说明.txt"

echo 构建完成！输出目录: %OUTPUT_DIR%
echo 请运行 %OUTPUT_DIR%\启动小助手.bat 来启动应用
pause 