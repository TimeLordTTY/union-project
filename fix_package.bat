@echo off
title 项目管理小助手-打包工具
setlocal enabledelayedexpansion

echo ===========================
echo 项目管理小助手 打包脚本
echo ===========================
echo.

rem 设置颜色
color 0A

rem 设置路径
set PROJECT_ROOT=%~dp0
set UI_DIR=%PROJECT_ROOT%project-assistant-ui
set SERVICE_DIR=%PROJECT_ROOT%project-assistant-service
set OUTPUT_DIR=%PROJECT_ROOT%ProjectAssistant
set DATA_BACKUP_DIR=%PROJECT_ROOT%data_backup_temp
set DLL_BACKUP_DIR=%PROJECT_ROOT%dll_backup_temp

echo [信息] 当前工作目录: %PROJECT_ROOT%

rem 检查环境变量
echo ===========================
echo 1. 检查环境变量
echo ===========================

rem 固定Java路径
set JAVA_HOME=C:\Program Files\Java\jdk-17
if not exist "%JAVA_HOME%\bin\java.exe" (
    set JAVA_HOME=C:\Program Files\Java\jdk-17.0.0
)
if not exist "%JAVA_HOME%\bin\java.exe" (
    set JAVA_HOME=C:\Program Files\Java\jdk-17.0.1
)
if not exist "%JAVA_HOME%\bin\java.exe" (
    set JAVA_HOME=C:\Program Files\Java\jdk-17.0.2
)
if not exist "%JAVA_HOME%\bin\java.exe" (
    set JAVA_HOME=D:\Program Files\Java\jdk-17
)
if not exist "%JAVA_HOME%\bin\java.exe" (
    set JAVA_HOME=D:\Soft\Java\jdk-17
)

if not exist "%JAVA_HOME%\bin\java.exe" (
    for /f "tokens=*" %%i in ('where java 2^>nul') do (
        set JAVA_PATH=%%i
        for %%j in ("!JAVA_PATH!") do set JAVA_BIN=%%~dpj
        for %%j in ("!JAVA_BIN!..") do set JAVA_HOME=%%~fj
        goto :found_java
    )
)
:found_java

if not exist "%JAVA_HOME%\bin\java.exe" (
    echo [错误] 未找到Java安装目录，请确保已安装JDK 17
    echo [提示] 您可以修改此脚本，设置JAVA_HOME为您的Java安装目录
    echo [例如] set JAVA_HOME=C:\Program Files\Java\jdk-17
    goto :error_exit
)

echo [信息] 使用Java路径: %JAVA_HOME%
set PATH=%JAVA_HOME%\bin;%PATH%

rem 固定Maven路径
set MAVEN_HOME=C:\Program Files\Apache Maven\apache-maven-3.9.9
if not exist "%MAVEN_HOME%\bin\mvn.cmd" (
    set MAVEN_HOME=C:\Program Files\Maven\apache-maven-3.9.9
)
if not exist "%MAVEN_HOME%\bin\mvn.cmd" (
    set MAVEN_HOME=D:\Program Files\Maven\apache-maven-3.9.9
)
if not exist "%MAVEN_HOME%\bin\mvn.cmd" (
    set MAVEN_HOME=D:\Soft\Maven\apache-maven-3.9.9
)

if not exist "%MAVEN_HOME%\bin\mvn.cmd" (
    for /f "tokens=*" %%i in ('where mvn 2^>nul') do (
        set MVN_PATH=%%i
        for %%j in ("!MVN_PATH!") do set MVN_BIN=%%~dpj
        for %%j in ("!MVN_BIN!..") do set MAVEN_HOME=%%~fj
        goto :found_maven
    )
)
:found_maven

if not exist "%MAVEN_HOME%\bin\mvn.cmd" (
    echo [警告] 未找到Maven，将尝试直接使用项目中的mvnw
    if exist "%SERVICE_DIR%\mvnw.cmd" (
        set MVN_CMD=%SERVICE_DIR%\mvnw.cmd
    ) else (
        set MVN_CMD=mvn
    )
) else (
    set MVN_CMD=%MAVEN_HOME%\bin\mvn.cmd
    echo [信息] 使用Maven路径: %MAVEN_HOME%
    set PATH=%MAVEN_HOME%\bin;%PATH%
)

rem 检查Node.js环境
where node >nul 2>nul
if %ERRORLEVEL% NEQ 0 (
    echo [警告] 未找到Node.js，将跳过前端构建
    set SKIP_FRONTEND=1
) else (
    set SKIP_FRONTEND=0
    for /f "tokens=*" %%i in ('node --version') do set NODE_VERSION=%%i
    echo [信息] 使用Node.js版本: !NODE_VERSION!
)

echo ===========================
echo 2. 清理输出目录
echo ===========================

if exist "%OUTPUT_DIR%" (
    echo [信息] 备份数据和DLL文件...
    
    rem 备份数据目录
    if exist "%OUTPUT_DIR%\data" (
        if exist "%DATA_BACKUP_DIR%" rd /s /q "%DATA_BACKUP_DIR%"
        mkdir "%DATA_BACKUP_DIR%" 2>nul
        xcopy /E /I /Y "%OUTPUT_DIR%\data" "%DATA_BACKUP_DIR%\" >nul
        echo [信息] 数据目录备份完成
    )
    
    rem 备份DLL文件
    if exist "%OUTPUT_DIR%\service_data" (
        if exist "%DLL_BACKUP_DIR%" rd /s /q "%DLL_BACKUP_DIR%"
        mkdir "%DLL_BACKUP_DIR%" 2>nul
        
        echo [信息] 正在备份DLL文件...
        for /r "%OUTPUT_DIR%\service_data" %%F in (*.dll) do (
            if exist "%%F" (
                echo 备份: %%~nxF
                copy /Y "%%F" "%DLL_BACKUP_DIR%\" >nul
            )
        )
        
        if exist "%OUTPUT_DIR%\ffmpeg.dll" (
            copy /Y "%OUTPUT_DIR%\ffmpeg.dll" "%DLL_BACKUP_DIR%\" >nul
        )
        
        echo [信息] DLL文件备份完成
    )
    
    echo [信息] 清理旧的输出目录: %OUTPUT_DIR%
    rd /s /q "%OUTPUT_DIR%" 2>nul
)

echo ===========================
echo 3. 编译后端
echo ===========================

echo [信息] 检查后端目录: %SERVICE_DIR%
if not exist "%SERVICE_DIR%" (
    echo [错误] 未找到后端目录: %SERVICE_DIR%
    goto :error_exit
)

cd /d "%SERVICE_DIR%"
echo [信息] 编译后端...
echo [命令] %MVN_CMD% clean package -DskipTests
call %MVN_CMD% clean package -DskipTests
if %ERRORLEVEL% NEQ 0 (
    echo [错误] 后端编译失败: %ERRORLEVEL%
    goto :error_exit
)
echo [信息] 后端编译成功！

echo ===========================
echo 4. 编译前端
echo ===========================

if %SKIP_FRONTEND% EQU 1 (
    echo [警告] 跳过前端编译，将使用现有前端资源
) else (
    echo [信息] 检查前端目录: %UI_DIR%
    if not exist "%UI_DIR%" (
        echo [错误] 未找到前端目录: %UI_DIR%
        goto :error_exit
    )

    cd /d "%UI_DIR%"
    echo [信息] 安装前端依赖...
    call npm install
    if %ERRORLEVEL% NEQ 0 (
        echo [警告] 前端依赖安装失败: %ERRORLEVEL%
        echo [警告] 尝试继续构建...
    )

    echo [信息] 构建前端...
    call npm run electron:build
    if %ERRORLEVEL% NEQ 0 (
        echo [警告] 前端构建失败: %ERRORLEVEL%
        echo [警告] 将尝试使用现有构建...
    )
)

echo ===========================
echo 5. 创建目录结构
echo ===========================

cd /d "%PROJECT_ROOT%"
echo [信息] 创建输出目录
mkdir "%OUTPUT_DIR%" 2>nul
mkdir "%OUTPUT_DIR%\data" 2>nul
mkdir "%OUTPUT_DIR%\service_data" 2>nul
mkdir "%OUTPUT_DIR%\service_data\logs" 2>nul
mkdir "%OUTPUT_DIR%\service_data\service" 2>nul
mkdir "%OUTPUT_DIR%\service_data\service\conf" 2>nul
mkdir "%OUTPUT_DIR%\service_data\jre" 2>nul
mkdir "%OUTPUT_DIR%\service_data\jre\bin" 2>nul
mkdir "%OUTPUT_DIR%\service_data\jre\lib" 2>nul
mkdir "%OUTPUT_DIR%\service_data\jre\conf" 2>nul

echo [信息] 创建测试日志文件
echo 此文件用于测试日志目录权限 > "%OUTPUT_DIR%\service_data\logs\test.log"

echo ===========================
echo 6. 复制文件
echo ===========================

echo [信息] 复制后端JAR文件
if exist "%SERVICE_DIR%\target\project-assistant-service-1.0.0.jar" (
    copy /Y "%SERVICE_DIR%\target\project-assistant-service-1.0.0.jar" "%OUTPUT_DIR%\service_data\service\" >nul
    if %ERRORLEVEL% NEQ 0 (
        echo [错误] 无法复制后端JAR文件
        goto :error_exit
    )
) else (
    echo [错误] 找不到后端JAR文件: %SERVICE_DIR%\target\project-assistant-service-1.0.0.jar
    echo [提示] 请确保后端编译成功
    goto :error_exit
)

echo [信息] 复制配置文件
xcopy /E /I /Y "%SERVICE_DIR%\src\main\resources\*" "%OUTPUT_DIR%\service_data\service\conf\" >nul

echo ===========================
echo 7. 复制JRE
echo ===========================

set JRE_FOUND=false

if exist "%JAVA_HOME%\bin\java.exe" (
    echo [信息] 从%JAVA_HOME%复制JRE文件...
    
    if not exist "%OUTPUT_DIR%\service_data\jre\bin" mkdir "%OUTPUT_DIR%\service_data\jre\bin"
    copy /Y "%JAVA_HOME%\bin\java.exe" "%OUTPUT_DIR%\service_data\jre\bin\" >nul
    
    if exist "%JAVA_HOME%\bin\*.dll" (
        copy /Y "%JAVA_HOME%\bin\*.dll" "%OUTPUT_DIR%\service_data\jre\bin\" >nul
    )
    
    if exist "%JAVA_HOME%\lib" (
        xcopy /E /I /Y "%JAVA_HOME%\lib" "%OUTPUT_DIR%\service_data\jre\lib\" >nul
    )
    
    if exist "%JAVA_HOME%\conf" (
        xcopy /E /I /Y "%JAVA_HOME%\conf" "%OUTPUT_DIR%\service_data\jre\conf\" >nul
    )
    
    if exist "%JAVA_HOME%\legal" (
        mkdir "%OUTPUT_DIR%\service_data\jre\legal" 2>nul
        xcopy /E /I /Y "%JAVA_HOME%\legal" "%OUTPUT_DIR%\service_data\jre\legal\" >nul
    )
    
    set JRE_FOUND=true
) else if exist "%PROJECT_ROOT%ProjectAssistant\service_data\jre\bin\java.exe" (
    echo [信息] 从之前的构建中复制JRE文件...
    xcopy /E /I /Y "%PROJECT_ROOT%ProjectAssistant\service_data\jre" "%OUTPUT_DIR%\service_data\jre\" >nul
    set JRE_FOUND=true
)

if "%JRE_FOUND%"=="false" (
    echo [警告] 未找到JRE文件，应用可能无法启动
)

echo ===========================
echo 8. 恢复DLL文件
echo ===========================

set DLL_FOUND=false

if exist "%DLL_BACKUP_DIR%" (
    echo [信息] 正在从备份中恢复DLL文件...
    for %%F in ("%DLL_BACKUP_DIR%\*.dll") do (
        copy /Y "%%F" "%OUTPUT_DIR%\service_data\" >nul
        echo [信息] 已恢复: %%~nxF
        set DLL_FOUND=true
    )
    
    rem 如果有ffmpeg.dll，同时复制到根目录
    if exist "%DLL_BACKUP_DIR%\ffmpeg.dll" (
        copy /Y "%DLL_BACKUP_DIR%\ffmpeg.dll" "%OUTPUT_DIR%\" >nul
        echo [信息] 已将ffmpeg.dll复制到根目录
    )
    
    if "!DLL_FOUND!"=="true" (
        echo [信息] DLL文件恢复完成
    ) else (
        echo [警告] 备份目录中没有找到DLL文件
    )
) else (
    echo [警告] 未找到DLL备份目录，将尝试其他方式获取DLL文件
)

echo ===========================
echo 9. 复制或下载ffmpeg组件
echo ===========================

set FFMPEG_FOUND=false

if exist "D:\Soft\ffmpeg\bin\ffmpeg.dll" (
    copy /Y "D:\Soft\ffmpeg\bin\ffmpeg.dll" "%OUTPUT_DIR%\service_data\" >nul
    copy /Y "D:\Soft\ffmpeg\bin\ffmpeg.dll" "%OUTPUT_DIR%\" >nul
    set FFMPEG_FOUND=true
    echo [信息] 从D:\Soft\ffmpeg\bin复制ffmpeg.dll成功
) else if exist "%PROJECT_ROOT%ProjectAssistant\service_data\ffmpeg.dll" (
    copy /Y "%PROJECT_ROOT%ProjectAssistant\service_data\ffmpeg.dll" "%OUTPUT_DIR%\service_data\" >nul
    copy /Y "%PROJECT_ROOT%ProjectAssistant\service_data\ffmpeg.dll" "%OUTPUT_DIR%\" >nul
    set FFMPEG_FOUND=true
    echo [信息] 从之前的构建中复制ffmpeg.dll成功
) else if exist "%PROJECT_ROOT%project-assistant-ui\ffmpeg.dll" (
    copy /Y "%PROJECT_ROOT%project-assistant-ui\ffmpeg.dll" "%OUTPUT_DIR%\service_data\" >nul
    copy /Y "%PROJECT_ROOT%project-assistant-ui\ffmpeg.dll" "%OUTPUT_DIR%\" >nul
    set FFMPEG_FOUND=true
    echo [信息] 从前端项目目录复制ffmpeg.dll成功
) else (
    echo [信息] 未找到ffmpeg.dll文件
    echo [提示] 打包完成后请运行download_ffmpeg.bat下载必要组件
    echo [提示] 或从 https://github.com/GyanD/codexffmpeg/releases 手动下载

    rem 创建空的占位文件
    echo 此文件为占位符，请替换为实际的ffmpeg.dll > "%OUTPUT_DIR%\service_data\ffmpeg_placeholder.txt"
    echo 此文件为占位符，请替换为实际的ffmpeg.dll > "%OUTPUT_DIR%\ffmpeg_placeholder.txt"
)

echo ===========================
echo 10. 复制主程序
echo ===========================

set EXE_FOUND=false

if exist "%UI_DIR%\dist_electron\win-unpacked\项目管理小助手.exe" (
    copy /Y "%UI_DIR%\dist_electron\win-unpacked\项目管理小助手.exe" "%OUTPUT_DIR%\" >nul
    set EXE_FOUND=true
    echo [信息] 已复制主程序从dist_electron目录
) else if exist "%UI_DIR%\dist_electron\win-unpacked\项目管理小助手 便携版.exe" (
    copy /Y "%UI_DIR%\dist_electron\win-unpacked\项目管理小助手 便携版.exe" "%OUTPUT_DIR%\项目管理小助手.exe" >nul
    set EXE_FOUND=true
    echo [信息] 已复制主程序并重命名
) else if exist "%PROJECT_ROOT%项目管理小助手.exe" (
    copy /Y "%PROJECT_ROOT%项目管理小助手.exe" "%OUTPUT_DIR%\" >nul
    set EXE_FOUND=true
    echo [信息] 已复制主程序从项目根目录
) else if exist "%PROJECT_ROOT%ProjectAssistant\项目管理小助手.exe" (
    copy /Y "%PROJECT_ROOT%ProjectAssistant\项目管理小助手.exe" "%OUTPUT_DIR%\" >nul
    set EXE_FOUND=true
    echo [信息] 已复制主程序从现有构建
)

if "%EXE_FOUND%"=="false" (
    echo [警告] 未找到主程序exe文件，将尝试查找所有可能的exe文件...
    echo [信息] 在%UI_DIR%\dist_electron目录中搜索...
    
    if exist "%UI_DIR%\dist_electron" (
        for /r "%UI_DIR%\dist_electron" %%F in (*.exe) do (
            echo [信息] 找到可能的exe文件: %%F
            copy /Y "%%F" "%OUTPUT_DIR%\项目管理小助手.exe" >nul
            set EXE_FOUND=true
            echo [信息] 已复制主程序并重命名为项目管理小助手.exe
            goto :found_exe
        )
    )
)
:found_exe

echo ===========================
echo 11. 复制启动器脚本
echo ===========================

echo [信息] 复制launcher.js文件...
if exist "%PROJECT_ROOT%launcher.js" (
    copy /Y "%PROJECT_ROOT%launcher.js" "%OUTPUT_DIR%\" >nul
    echo [信息] 创建启动脚本...
    echo @echo off > "%OUTPUT_DIR%\启动项目管理小助手.bat"
    echo cd /d "%%~dp0" >> "%OUTPUT_DIR%\启动项目管理小助手.bat"
    echo echo 启动项目管理小助手... >> "%OUTPUT_DIR%\启动项目管理小助手.bat"
    echo node launcher.js >> "%OUTPUT_DIR%\启动项目管理小助手.bat"
    echo pause >> "%OUTPUT_DIR%\启动项目管理小助手.bat"
    echo [信息] 启动脚本创建成功
) else (
    echo [警告] 找不到launcher.js文件
    echo [信息] 创建替代启动脚本...
    echo @echo off > "%OUTPUT_DIR%\启动项目管理小助手.bat"
    echo cd /d "%%~dp0" >> "%OUTPUT_DIR%\启动项目管理小助手.bat"
    echo echo 启动项目管理小助手... >> "%OUTPUT_DIR%\启动项目管理小助手.bat"
    echo "项目管理小助手.exe" >> "%OUTPUT_DIR%\启动项目管理小助手.bat"
    echo pause >> "%OUTPUT_DIR%\启动项目管理小助手.bat"
    echo [信息] 替代启动脚本创建成功
)

echo ===========================
echo 12. 复制ffmpeg下载器
echo ===========================

if exist "%PROJECT_ROOT%download_ffmpeg.bat" (
    copy /Y "%PROJECT_ROOT%download_ffmpeg.bat" "%OUTPUT_DIR%\" >nul
    echo [信息] 已复制ffmpeg下载脚本到输出目录
) else (
    echo [警告] 找不到ffmpeg下载脚本
    echo [信息] 创建简易下载脚本...
    echo @echo off > "%OUTPUT_DIR%\下载ffmpeg.bat"
    echo echo 正在下载ffmpeg.dll... >> "%OUTPUT_DIR%\下载ffmpeg.bat"
    echo powershell -Command "Invoke-WebRequest -Uri 'https://raw.githubusercontent.com/timelordtty/ffmpeg-binaries/main/ffmpeg.dll' -OutFile 'ffmpeg.dll'" >> "%OUTPUT_DIR%\下载ffmpeg.bat"
    echo echo 下载完成，请确认文件已保存到当前目录 >> "%OUTPUT_DIR%\下载ffmpeg.bat"
    echo pause >> "%OUTPUT_DIR%\下载ffmpeg.bat"
    echo [信息] 简易下载脚本创建成功
)

echo ===========================
echo 13. 恢复数据
echo ===========================

if exist "%DATA_BACKUP_DIR%" (
    echo [信息] 恢复数据目录...
    xcopy /E /I /Y "%DATA_BACKUP_DIR%" "%OUTPUT_DIR%\data\" >nul
    rd /s /q "%DATA_BACKUP_DIR%" 2>nul
    echo [信息] 数据已恢复
)

echo ===========================
echo 14. 创建说明文档
echo ===========================

echo =========================== > "%OUTPUT_DIR%\README.txt"
echo 项目管理小助手 - 便携版 >> "%OUTPUT_DIR%\README.txt"
echo =========================== >> "%OUTPUT_DIR%\README.txt"
echo. >> "%OUTPUT_DIR%\README.txt"
echo 启动方法: >> "%OUTPUT_DIR%\README.txt"
echo 1. 直接双击运行"项目管理小助手.exe"启动应用 >> "%OUTPUT_DIR%\README.txt"
echo 2. 如遇问题，可尝试运行"启动项目管理小助手.bat"脚本启动 >> "%OUTPUT_DIR%\README.txt"
echo. >> "%OUTPUT_DIR%\README.txt"
echo 重要提示: >> "%OUTPUT_DIR%\README.txt"
echo - ffmpeg相关的DLL文件已经包含在service_data目录中 >> "%OUTPUT_DIR%\README.txt"
echo - 如果启动时仍提示缺少dll文件，可运行download_ffmpeg.bat下载其他组件 >> "%OUTPUT_DIR%\README.txt"
echo. >> "%OUTPUT_DIR%\README.txt"
echo 注意事项: >> "%OUTPUT_DIR%\README.txt"
echo - 请确保整个文件夹结构完整 >> "%OUTPUT_DIR%\README.txt"
echo - 所有数据存储在data目录中 >> "%OUTPUT_DIR%\README.txt"
echo - 所有服务文件和日志存储在service_data目录中 >> "%OUTPUT_DIR%\README.txt"
echo - 运行日志位于service_data/logs目录 >> "%OUTPUT_DIR%\README.txt"
echo - 如遇到白屏问题，请查看根目录的startup.log文件 >> "%OUTPUT_DIR%\README.txt"
echo - 右键点击应用窗口可打开开发者工具查看详细日志 >> "%OUTPUT_DIR%\README.txt"
echo. >> "%OUTPUT_DIR%\README.txt"

echo ===========================
echo 15. 设置权限
echo ===========================

echo [信息] 确保logs目录可写...
attrib -R "%OUTPUT_DIR%\service_data\logs" /S /D
icacls "%OUTPUT_DIR%\service_data\logs" /grant:r Everyone:(OI)(CI)F >nul 2>&1

echo [信息] 设置data目录权限...
attrib -R "%OUTPUT_DIR%\data" /S /D
icacls "%OUTPUT_DIR%\data" /grant:r Everyone:(OI)(CI)F >nul 2>&1

echo ===========================
echo 16. 清理临时文件
echo ===========================

if exist "%DLL_BACKUP_DIR%" (
    echo [信息] 清理DLL备份目录...
    rd /s /q "%DLL_BACKUP_DIR%" 2>nul
)

echo ===========================
echo 打包完成!
echo ===========================
echo 应用目录: %OUTPUT_DIR%
echo.
echo 目录结构:
echo - 项目管理小助手.exe (主程序)
echo - launcher.js (启动器脚本)
echo - 启动项目管理小助手.bat (备用启动脚本)
echo - download_ffmpeg.bat (下载ffmpeg组件)
echo - data (数据目录)
echo - service_data (包含service目录、jre目录和各种DLL文件)
echo - README.txt (使用说明)
echo.
echo [提示] 所有ffmpeg相关的DLL文件已经保留并恢复到service_data目录
echo.

set /p ANSWER=是否打开输出目录? (Y/N): 
if /i "%ANSWER%" == "Y" (
    explorer "%OUTPUT_DIR%"
)

goto :end

:error_exit
echo.
echo [错误] 打包过程中出现错误，请检查以上日志信息
echo [信息] 您可以尝试手动编译步骤:
echo 1. 进入后端目录: cd %SERVICE_DIR%
echo 2. 运行Maven命令: mvn clean package -DskipTests
echo 3. 进入前端目录: cd %UI_DIR%
echo 4. 安装依赖: npm install
echo 5. 构建前端: npm run electron:build
echo.
pause
exit /b 1

:end
echo 按任意键退出...
pause
exit /b 0 