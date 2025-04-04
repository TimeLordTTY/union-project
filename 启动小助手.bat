@echo off
chcp 65001 >nul
title 项目管理小助手

REM 设置颜色显示
color 0A

echo ========================================
echo        启动项目管理小助手...
echo ========================================

cd /d %~dp0\service_data

REM 检查后端服务是否存在
if not exist backend.jar (
    if exist project-assistant-service-0.0.1-SNAPSHOT.jar (
        copy "project-assistant-service-0.0.1-SNAPSHOT.jar" "backend.jar" >nul
    ) else (
        echo [错误] 未找到后端服务JAR文件，请确保已经成功构建项目
        echo 提示: 你可以先运行 build-all.bat 构建项目
        pause
        exit /b 1
    )
)

REM 检查lib目录是否存在
if not exist lib (
    mkdir lib 2>nul
    if exist ..\project-assistant-service\target\lib (
        echo [信息] 复制依赖库文件...
        copy /Y "..\project-assistant-service\target\lib\*" "lib\" >nul
    ) else (
        echo [警告] 未找到依赖库文件，可能会导致服务启动失败
    )
)

REM 检查数据目录
if not exist ..\data (
    mkdir ..\data 2>nul
)

REM 检查是否有桌面应用程序
set DESKTOP_APP_FOUND=0
if exist ProjectAssistant.exe (
    set DESKTOP_APP_FOUND=1
    echo [信息] 检测到桌面应用程序，将以独立应用模式启动
) else if exist ..\project-assistant-ui\dist-electron\win-unpacked\ProjectAssistant.exe (
    set DESKTOP_APP_FOUND=1
    echo [信息] 找到桌面应用程序，正在复制...
    copy /Y "..\project-assistant-ui\dist-electron\win-unpacked\ProjectAssistant.exe" "." >nul
    echo [信息] 复制完成，将以独立应用模式启动
) else (
    echo [信息] 未检测到桌面应用程序，将以浏览器模式启动
)

REM 启动后端服务
echo [信息] 正在启动后端服务...
start /b cmd /c "java -jar backend.jar > service_log.txt 2>&1"

REM 等待服务启动
echo [信息] 等待服务启动...
ping 127.0.0.1 -n 5 > nul

REM 根据桌面应用状态选择启动方式
if %DESKTOP_APP_FOUND%==1 (
    echo [信息] 启动桌面应用...
    start ProjectAssistant.exe
) else (
    echo [信息] 打开浏览器访问应用...
    start http://localhost:9090
)

echo ========================================
echo    项目管理小助手已成功启动!
echo    
echo    ★ 如需关闭，请关闭此窗口 ★
echo ========================================

pause 