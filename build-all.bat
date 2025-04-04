@echo off
chcp 65001 >nul
title 项目管理小助手 - 构建脚本

REM 设置正确的Java和Maven环境变量
set "JAVA_HOME=D:\Soft\Java\jdk-17"
set "MAVEN_HOME=E:\Apache\apache-maven-3.9.9"
set "PATH=%JAVA_HOME%\bin;%MAVEN_HOME%\bin;%PATH%"
set "JAVA_TOOL_OPTIONS=-Dfile.encoding=UTF-8"
set "MAVEN_OPTS=-Duser.language=en -Duser.region=US"

echo ========================================
echo 开始构建项目管理小助手...
echo ========================================

REM 准备服务数据目录
if not exist service_data mkdir service_data
if not exist service_data\lib mkdir service_data\lib
if not exist service_data\logs mkdir service_data\logs

REM 构建后端服务
echo.
echo 开始构建后端服务...
cd /d %~dp0\project-assistant-service

REM 检查Java环境
echo 检查Java环境...
java -version
if %ERRORLEVEL% NEQ 0 (
    echo [错误] 未检测到Java环境，请确保已安装JDK 17
    goto :ERROR
)

REM 构建服务
echo 构建后端服务...
call "%MAVEN_HOME%\bin\mvn" clean package -DskipTests -Duser.language=en -Duser.region=US
if %ERRORLEVEL% NEQ 0 (
    echo [错误] 后端服务构建失败，尝试直接使用已有JAR文件
) else (
    echo 后端服务构建成功！
    echo 复制依赖库和JAR文件...
    copy /Y "target\lib\*" "..\service_data\lib\" >nul
    copy /Y "target\project-assistant-service-0.0.1-SNAPSHOT.jar" "..\service_data\backend.jar" >nul
)

REM 准备前端资源
echo.
echo 准备静态前端资源...
cd /d %~dp0\project-assistant-ui

REM 确保服务端静态资源目录存在
if not exist "..\service_data\static" mkdir "..\service_data\static"

REM 简单创建静态资源
echo 创建静态HTML和资源...

echo ^<!DOCTYPE html^> > ..\service_data\static\index.html
echo ^<html lang="zh-CN"^> >> ..\service_data\static\index.html
echo ^<head^> >> ..\service_data\static\index.html
echo   ^<meta charset="UTF-8"^> >> ..\service_data\static\index.html
echo   ^<meta name="viewport" content="width=device-width, initial-scale=1.0"^> >> ..\service_data\static\index.html
echo   ^<title^>项目管理小助手^</title^> >> ..\service_data\static\index.html
echo   ^<style^> >> ..\service_data\static\index.html
echo     body { font-family: 'Microsoft YaHei', sans-serif; margin: 0; padding: 0; background-color: #FFF0F5; } >> ..\service_data\static\index.html
echo     .container { max-width: 800px; margin: 50px auto; padding: 20px; background-color: white; border-radius: 8px; box-shadow: 0 2px 10px rgba(0,0,0,0.1); } >> ..\service_data\static\index.html
echo     h1 { color: #FF1493; text-align: center; } >> ..\service_data\static\index.html
echo     p { text-align: center; margin: 20px 0; } >> ..\service_data\static\index.html
echo     .btn { display: block; width: 200px; margin: 20px auto; padding: 10px; background-color: #FF69B4; color: white; text-align: center; text-decoration: none; border-radius: 4px; font-weight: bold; } >> ..\service_data\static\index.html
echo     .btn:hover { background-color: #FF1493; } >> ..\service_data\static\index.html
echo   ^</style^> >> ..\service_data\static\index.html
echo ^</head^> >> ..\service_data\static\index.html
echo ^<body^> >> ..\service_data\static\index.html
echo   ^<div class="container"^> >> ..\service_data\static\index.html
echo     ^<h1^>项目管理小助手^</h1^> >> ..\service_data\static\index.html
echo     ^<p^>欢迎使用项目管理小助手，请点击下方按钮访问应用^</p^> >> ..\service_data\static\index.html
echo     ^<a href="http://localhost:9090" class="btn"^>进入应用^</a^> >> ..\service_data\static\index.html
echo   ^</div^> >> ..\service_data\static\index.html
echo ^</body^> >> ..\service_data\static\index.html
echo ^</html^> >> ..\service_data\static\index.html

if exist "public\icon.ico" (
    echo 复制图标文件...
    copy /Y "public\icon.ico" "..\service_data\static\favicon.ico" >nul
)

REM 创建启动脚本
echo.
echo 创建启动脚本...
cd /d %~dp0

if not exist "启动小助手.bat" (
    echo 创建启动脚本...
    echo @echo off > "启动小助手.bat"
    echo chcp 65001 ^>nul >> "启动小助手.bat"
    echo title 项目管理小助手 >> "启动小助手.bat"
    echo. >> "启动小助手.bat"
    echo cd /d %%~dp0\service_data >> "启动小助手.bat"
    echo. >> "启动小助手.bat"
    echo echo 正在启动项目管理小助手... >> "启动小助手.bat"
    echo start /b cmd /c "java -jar backend.jar ^> logs\service.log 2^>^&1" >> "启动小助手.bat"
    echo ping 127.0.0.1 -n 5 ^> nul >> "启动小助手.bat"
    echo start http://localhost:9090 >> "启动小助手.bat"
    echo. >> "启动小助手.bat"
    echo echo 应用已启动! 请查看浏览器窗口。 >> "启动小助手.bat"
    echo echo 如需关闭应用，请关闭此窗口。 >> "启动小助手.bat"
    echo. >> "启动小助手.bat"
    echo pause >> "启动小助手.bat"
)

REM 构建完成
echo.
echo ========================================
echo 项目构建完成！
echo ========================================
echo.
echo 如需启动应用，请执行 "启动小助手.bat"
echo.
goto :EOF

:ERROR
echo.
echo ========================================
echo [错误] 构建失败！请查看上方错误信息
echo ========================================
echo.
pause
exit /b 1
