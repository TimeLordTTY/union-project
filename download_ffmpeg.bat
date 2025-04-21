@echo off
echo ===========================
echo FFMPEG 下载工具
echo ===========================
echo.

set DOWNLOAD_DIR=%~dp0temp_download
set OUTPUT_DIR=%~dp0ProjectAssistant

if not exist "%DOWNLOAD_DIR%" mkdir "%DOWNLOAD_DIR%"

echo [信息] 正在检查现有DLL文件...
set DLL_DIR=%OUTPUT_DIR%\service_data
set DLL_BACKUP_DIR=%~dp0dll_backup_temp

if exist "%DLL_DIR%" (
    rem 备份已存在的DLL文件
    if exist "%DLL_BACKUP_DIR%" rd /s /q "%DLL_BACKUP_DIR%" 2>nul
    mkdir "%DLL_BACKUP_DIR%" 2>nul
    
    for %%F in ("%DLL_DIR%\*.dll") do (
        echo [信息] 备份: %%~nxF
        copy /Y "%%F" "%DLL_BACKUP_DIR%\" >nul
    )
    echo [信息] DLL文件备份完成
)

echo [信息] 正在下载ffmpeg相关文件...
echo [信息] 这可能需要几分钟时间，请耐心等待...

echo [方法1] 尝试从GitHub下载ffmpeg压缩包...
powershell -Command "Invoke-WebRequest -Uri 'https://github.com/BtbN/FFmpeg-Builds/releases/download/latest/ffmpeg-master-latest-win64-gpl-shared.zip' -OutFile '%DOWNLOAD_DIR%\ffmpeg.zip'"

if not exist "%DOWNLOAD_DIR%\ffmpeg.zip" (
    echo [方法2] 尝试从Gyan下载ffmpeg压缩包...
    powershell -Command "Invoke-WebRequest -Uri 'https://www.gyan.dev/ffmpeg/builds/packages/ffmpeg-5.1.2-essentials_build.zip' -OutFile '%DOWNLOAD_DIR%\ffmpeg.zip'"
)

if exist "%DOWNLOAD_DIR%\ffmpeg.zip" (
    echo [信息] 下载完成，正在解压...
    powershell -Command "Expand-Archive -Path '%DOWNLOAD_DIR%\ffmpeg.zip' -DestinationPath '%DOWNLOAD_DIR%\ffmpeg' -Force"
    
    rem 查找和复制DLL文件
    echo [信息] 正在查找所需的DLL文件...
    
    rem 检查第一个可能的目录路径
    if exist "%DOWNLOAD_DIR%\ffmpeg\ffmpeg-master-latest-win64-gpl-shared\bin" (
        echo [信息] 找到DLL文件目录: ffmpeg-master-latest-win64-gpl-shared\bin
        
        rem 创建目标目录
        if not exist "%OUTPUT_DIR%\service_data" mkdir "%OUTPUT_DIR%\service_data" 2>nul
        
        rem 复制所有DLL文件
        echo [信息] 复制DLL文件到service_data目录...
        copy /Y "%DOWNLOAD_DIR%\ffmpeg\ffmpeg-master-latest-win64-gpl-shared\bin\*.dll" "%OUTPUT_DIR%\service_data\" >nul
        
        rem 复制ffmpeg.dll到根目录
        if exist "%DOWNLOAD_DIR%\ffmpeg\ffmpeg-master-latest-win64-gpl-shared\bin\ffmpeg.dll" (
            echo [信息] 复制ffmpeg.dll到根目录...
            copy /Y "%DOWNLOAD_DIR%\ffmpeg\ffmpeg-master-latest-win64-gpl-shared\bin\ffmpeg.dll" "%OUTPUT_DIR%\" >nul
        )
        
        echo [完成] DLL文件已成功复制
        goto :restore_backup
    )
    
    rem 通用搜索
    echo [信息] 在解压目录中搜索DLL文件...
    
    rem 创建目标目录
    if not exist "%OUTPUT_DIR%\service_data" mkdir "%OUTPUT_DIR%\service_data" 2>nul
    
    set FOUND_DLL=false
    for /r "%DOWNLOAD_DIR%\ffmpeg" %%F in (*.dll) do (
        echo [信息] 找到DLL: %%F
        copy /Y "%%F" "%OUTPUT_DIR%\service_data\" >nul
        set FOUND_DLL=true
        
        rem 如果是ffmpeg.dll，同时复制到根目录
        if "%%~nxF"=="ffmpeg.dll" (
            copy /Y "%%F" "%OUTPUT_DIR%\" >nul
        )
    )
    
    if "!FOUND_DLL!"=="true" (
        echo [完成] DLL文件已成功复制
    ) else (
        echo [警告] 解压后未找到DLL文件，尝试其他方法...
        goto :try_direct_download
    )
) else (
    :try_direct_download
    echo [方法3] 尝试直接下载单个DLL文件...
    
    rem 确保目标目录存在
    if not exist "%OUTPUT_DIR%\service_data" mkdir "%OUTPUT_DIR%\service_data" 2>nul
    
    rem 直接下载常用的ffmpeg相关DLL文件
    echo [信息] 下载ffmpeg.dll...
    powershell -Command "Invoke-WebRequest -Uri 'https://raw.githubusercontent.com/timelordtty/ffmpeg-binaries/main/ffmpeg.dll' -OutFile '%DOWNLOAD_DIR%\ffmpeg.dll'"
    
    echo [信息] 下载avcodec-61.dll...
    powershell -Command "Invoke-WebRequest -Uri 'https://raw.githubusercontent.com/timelordtty/ffmpeg-binaries/main/avcodec-61.dll' -OutFile '%DOWNLOAD_DIR%\avcodec-61.dll'"
    
    echo [信息] 下载avdevice-61.dll...
    powershell -Command "Invoke-WebRequest -Uri 'https://raw.githubusercontent.com/timelordtty/ffmpeg-binaries/main/avdevice-61.dll' -OutFile '%DOWNLOAD_DIR%\avdevice-61.dll'"
    
    echo [信息] 下载avfilter-10.dll...
    powershell -Command "Invoke-WebRequest -Uri 'https://raw.githubusercontent.com/timelordtty/ffmpeg-binaries/main/avfilter-10.dll' -OutFile '%DOWNLOAD_DIR%\avfilter-10.dll'"
    
    echo [信息] 下载avformat-61.dll...
    powershell -Command "Invoke-WebRequest -Uri 'https://raw.githubusercontent.com/timelordtty/ffmpeg-binaries/main/avformat-61.dll' -OutFile '%DOWNLOAD_DIR%\avformat-61.dll'"
    
    echo [信息] 下载avutil-59.dll...
    powershell -Command "Invoke-WebRequest -Uri 'https://raw.githubusercontent.com/timelordtty/ffmpeg-binaries/main/avutil-59.dll' -OutFile '%DOWNLOAD_DIR%\avutil-59.dll'"
    
    echo [信息] 下载postproc-58.dll...
    powershell -Command "Invoke-WebRequest -Uri 'https://raw.githubusercontent.com/timelordtty/ffmpeg-binaries/main/postproc-58.dll' -OutFile '%DOWNLOAD_DIR%\postproc-58.dll'"
    
    echo [信息] 下载swresample-5.dll...
    powershell -Command "Invoke-WebRequest -Uri 'https://raw.githubusercontent.com/timelordtty/ffmpeg-binaries/main/swresample-5.dll' -OutFile '%DOWNLOAD_DIR%\swresample-5.dll'"
    
    echo [信息] 下载swscale-8.dll...
    powershell -Command "Invoke-WebRequest -Uri 'https://raw.githubusercontent.com/timelordtty/ffmpeg-binaries/main/swscale-8.dll' -OutFile '%DOWNLOAD_DIR%\swscale-8.dll'"
    
    rem 复制下载的DLL文件
    set FOUND_DLL=false
    for %%F in ("%DOWNLOAD_DIR%\*.dll") do (
        if exist "%%F" (
            echo [信息] 复制: %%~nxF
            copy /Y "%%F" "%OUTPUT_DIR%\service_data\" >nul
            set FOUND_DLL=true
            
            rem 如果是ffmpeg.dll，同时复制到根目录
            if "%%~nxF"=="ffmpeg.dll" (
                copy /Y "%%F" "%OUTPUT_DIR%\" >nul
            )
        )
    )
    
    if "!FOUND_DLL!"=="false" (
        echo [警告] 未能下载到任何DLL文件
        goto :restore_backup
    )
)

:restore_backup
rem 恢复备份的DLL文件（追加，不覆盖新下载的）
if exist "%DLL_BACKUP_DIR%" (
    echo [信息] 正在恢复备份的DLL文件...
    
    for %%F in ("%DLL_BACKUP_DIR%\*.dll") do (
        rem 检查文件是否不存在于目标目录
        if not exist "%OUTPUT_DIR%\service_data\%%~nxF" (
            echo [信息] 恢复: %%~nxF
            copy /Y "%%F" "%OUTPUT_DIR%\service_data\" >nul
            
            rem 如果是ffmpeg.dll，同时复制到根目录
            if "%%~nxF"=="ffmpeg.dll" (
                if not exist "%OUTPUT_DIR%\ffmpeg.dll" (
                    copy /Y "%%F" "%OUTPUT_DIR%\" >nul
                )
            )
        )
    )
    
    echo [信息] 备份恢复完成
)

:cleaning
echo.
echo [清理] 清理临时文件...
rd /s /q "%DOWNLOAD_DIR%" 2>nul
if exist "%DLL_BACKUP_DIR%" rd /s /q "%DLL_BACKUP_DIR%" 2>nul

:end
echo.
echo ===========================
echo 处理完成!
echo ===========================
echo.
echo 所有ffmpeg相关的DLL文件已保存到: %OUTPUT_DIR%\service_data
echo.
echo 如果程序仍然报告找不到dll文件，请确认以下事项:
echo 1. 文件已成功复制到ProjectAssistant目录
echo 2. 如果你自己已经有这些DLL文件，可以直接手动复制到service_data目录中
echo.

set /p ANSWER=是否打开输出目录? (Y/N): 
if /i "%ANSWER%" == "Y" (
    explorer "%OUTPUT_DIR%\service_data"
)

pause 
