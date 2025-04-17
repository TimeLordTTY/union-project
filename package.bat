@echo off
title 项目管理小助手打包和运行
setlocal EnableDelayedExpansion

echo ====================
echo 0. 设置路径
echo ====================
REM 设置Java路径
set JAVA_HOME=D:\Soft\Java\jdk-17
set PATH=%JAVA_HOME%\bin;%PATH%

REM 设置Maven路径
set MAVEN_HOME=D:\Soft\Maven\apache-maven-3.9.9
set PATH=%MAVEN_HOME%\bin;%PATH%
set MVN_CMD=%MAVEN_HOME%\bin\mvn

REM 设置Node.js路径
set NODE_HOME=D:\Soft\Node\node-v16.20.0
set PATH=%NODE_HOME%;%PATH%
set NPM_CMD=npm

REM 设置项目根目录
set PROJECT_ROOT=%~dp0

echo ====================
echo 1. 检查环境
echo ====================
call java -version 2>nul
if %ERRORLEVEL% NEQ 0 (
    echo 错误: 未检测到Java，请确保已安装JDK 17
    echo 当前JAVA_HOME: %JAVA_HOME%
    pause
    exit /b 1
)
echo Java检查通过，继续执行...

call "%MVN_CMD%" --version | findstr "Apache Maven"
if %ERRORLEVEL% NEQ 0 (
    echo 错误: 未检测到Maven，请确保已安装Maven
    echo 当前MAVEN_HOME: %MAVEN_HOME%
    pause
    exit /b 1
)
echo Maven检查通过，继续执行...

call node --version 2>nul
if %ERRORLEVEL% NEQ 0 (
    echo 错误: 未检测到Node.js，请确保已安装Node.js
    echo 当前NODE_HOME: %NODE_HOME%
    pause
    exit /b 1
)
echo Node.js检查通过，继续执行...

echo ====================
echo 2. 创建应用目录结构
echo ====================
set APP_DIR=%~dp0ProjectAssistant
set SERVICE_DATA_DIR=%APP_DIR%\service_data
set LIB_DIR=%SERVICE_DATA_DIR%\lib
set JRE_DIR=%SERVICE_DATA_DIR%\jre
set CONF_DIR=%SERVICE_DATA_DIR%\conf
set DATA_DIR=%APP_DIR%\data
set WEB_DIR=%APP_DIR%\web
set STATIC_DIR=%APP_DIR%\web\static
set LOG_DIR=%SERVICE_DATA_DIR%\logs
set DATA_BACKUP_DIR=%~dp0data_backup_temp
set APP_NAME=ProjectAssistant

echo 备份数据目录（如果存在）...
if exist "%~dp0ProjectAssistant\data" (
    echo 正在备份数据目录...
    mkdir "%DATA_BACKUP_DIR%"
    xcopy /E /I /Y "%~dp0ProjectAssistant\data" "%DATA_BACKUP_DIR%"
    echo 数据目录已备份到临时目录
)

REM 检查并清理整个APP目录
if exist "%~dp0ProjectAssistant" (
    echo 清理ProjectAssistant目录（保留数据）...
    rd /s /q "%~dp0ProjectAssistant"
)

echo 创建目录结构...
mkdir "%APP_DIR%"
mkdir "%SERVICE_DATA_DIR%"
mkdir "%LIB_DIR%"
mkdir "%JRE_DIR%"
mkdir "%JRE_DIR%\bin"
mkdir "%JRE_DIR%\lib"
mkdir "%CONF_DIR%"
mkdir "%LOG_DIR%"
mkdir "%DATA_DIR%"
mkdir "%WEB_DIR%"
mkdir "%STATIC_DIR%"

echo 还原数据目录（如果存在备份）...
if exist "%DATA_BACKUP_DIR%" (
    echo 正在还原数据目录...
    xcopy /E /I /Y "%DATA_BACKUP_DIR%" "%DATA_DIR%"
    rd /s /q "%DATA_BACKUP_DIR%"
    echo 数据目录已还原
)

echo ====================
echo 3. 编译后端项目
echo ====================
cd /d "%PROJECT_ROOT%\project-assistant-service"
call "%MVN_CMD%" clean package -DskipTests
if %ERRORLEVEL% NEQ 0 (
    echo 后端编译失败，错误代码: %ERRORLEVEL%
    pause
    exit /b %ERRORLEVEL%
)
echo 后端编译成功！

echo ====================
echo 4. 编译前端项目
echo ====================
cd /d "%PROJECT_ROOT%\project-assistant-ui"
call "%NPM_CMD%" install
if %ERRORLEVEL% NEQ 0 (
    echo 安装前端依赖失败，错误代码: %ERRORLEVEL%
    pause
    exit /b %ERRORLEVEL%
)

call "%NPM_CMD%" run build:prod
if %ERRORLEVEL% NEQ 0 (
    echo 前端编译失败，错误代码: %ERRORLEVEL%
    pause
    exit /b %ERRORLEVEL%
)
echo 前端编译成功！

echo ====================
echo 5. 复制JRE（运行时环境）
echo ====================
set JRE_HOME=D:\Soft\Java\jre-17
if exist "%JRE_HOME%" (
    echo 复制JRE运行时环境...
    xcopy /Y /E /I "%JRE_HOME%\bin" "%JRE_DIR%\bin\"
    xcopy /Y /E /I "%JRE_HOME%\lib" "%JRE_DIR%\lib\"
    xcopy /Y /E /I "%JRE_HOME%\conf" "%JRE_DIR%\conf\"
) else (
    echo 尝试从JDK复制最小运行环境...
    xcopy /Y /E /I "%JAVA_HOME%\bin" "%JRE_DIR%\bin\"
    xcopy /Y /E /I "%JAVA_HOME%\lib" "%JRE_DIR%\lib\"
    xcopy /Y /E /I "%JAVA_HOME%\conf" "%JRE_DIR%\conf\"
    
    echo 警告: 未找到单独的JRE，使用JDK替代。分发包可能较大。
)

echo ====================
echo 6. 复制应用文件
echo ====================
echo 复制后端JAR文件...
copy /Y "%PROJECT_ROOT%\project-assistant-service\target\project-assistant-service-1.0.0.jar" "%SERVICE_DATA_DIR%\"
if %ERRORLEVEL% NEQ 0 (
    echo 错误: 无法复制后端JAR文件，请确保编译成功
    pause
    exit /b 1
)

echo 复制前端文件...
xcopy /Y /E /I "%PROJECT_ROOT%\project-assistant-ui\dist\*" "%WEB_DIR%\"
if %ERRORLEVEL% NEQ 0 (
    echo 错误: 无法复制前端文件，请确保编译成功
    pause
    exit /b 1
)

echo ====================
echo 7. 复制依赖和配置文件
echo ====================
echo 复制依赖到lib目录...
cd /d "%PROJECT_ROOT%\project-assistant-service"
call "%MVN_CMD%" dependency:copy-dependencies -DoutputDirectory="%LIB_DIR%"

echo 复制配置文件...
copy /Y "%PROJECT_ROOT%\project-assistant-service\src\main\resources\application.yml" "%CONF_DIR%\"

REM 创建修改后的配置文件（修改数据库路径，禁用H2控制台)
(
echo server:
echo   port: 8080
echo   servlet:
echo     context-path: /api
echo.
echo spring:
echo   application:
echo     name: project-assistant-service
echo   datasource:
echo     url: jdbc:h2:file:./data/projectdb;DB_CLOSE_ON_EXIT=FALSE
echo     driver-class-name: org.h2.Driver
echo     username: sa
echo     password: password
echo   h2:
echo     console:
echo       enabled: false
echo.
echo # MyBatis配置
echo mybatis:
echo   mapper-locations: classpath:mapper/*.xml
echo   type-aliases-package: com.timelordtty.projectcalendar.model
echo   configuration:
echo     map-underscore-to-camel-case: true
echo     cache-enabled: false
echo.
echo # 日志配置
echo logging:
echo   level:
echo     root: INFO
echo     com.timelordtty: INFO
echo     org.mybatis: INFO
echo   file:
echo     name: ./service_data/logs/project-assistant.log
echo.
echo # 百度API配置
echo baidu:
echo   text-correction:
echo     api-key: your_api_key_here
echo     secret-key: your_secret_key_here
echo     url: https://aip.baidubce.com/rpc/2.0/nlp/v1/ecnet
) > "%CONF_DIR%\application.yml"

echo ====================
echo 8. 创建启动脚本
echo ====================
echo 创建启动批处理文件...

(
echo @echo off
echo title 项目管理小助手
echo.
echo cd /d %%~dp0
echo echo 正在启动项目管理小助手...
echo.
echo REM 启动参数设置
echo set JAVA_PATH=service_data\jre\bin\java.exe
echo set JAR_PATH=service_data\project-assistant-service-1.0.0.jar
echo set SERVER_PORT=8080
echo set WEB_PORT=80
echo.
echo REM 检查Java环境
echo if not exist "%%JAVA_PATH%%" (
echo     echo 错误: 找不到Java运行环境，请确保service_data\jre目录完整
echo     pause
echo     exit /b 1
echo ^)
echo.
echo REM 检查后端JAR文件
echo if not exist "%%JAR_PATH%%" (
echo     echo 错误: 找不到应用程序JAR文件
echo     pause
echo     exit /b 1
echo ^)
echo.
echo REM 启动后台服务
echo echo 正在启动后台服务...
echo start "项目管理小助手_后台服务" "%%JAVA_PATH%%" -Dfile.encoding=UTF-8 -Dserver.port=%%SERVER_PORT%% -Dh2.bindAddress=127.0.0.1 -Duser.timezone=Asia/Shanghai -Dspring.config.location=file:./service_data/conf/application.yml -jar "%%JAR_PATH%%"
echo.
echo REM 给后台服务几秒钟时间启动
echo echo 等待后台服务启动...
echo timeout /t 5 /nobreak
echo.
echo REM 启动Web服务器
echo echo 正在启动Web服务...
echo cd web
echo.
echo REM 使用http-server（如果有）或Python（如果有）或直接使用浏览器打开
echo if exist "%%~dp0service_data\node\node.exe" (
echo     start "项目管理小助手_前端" "%%~dp0service_data\node\node.exe" "%%~dp0service_data\node_modules\http-server\bin\http-server" -p %%WEB_PORT%% -o
echo ) else (
echo     start http://localhost:%%SERVER_PORT%%/api/web
echo ^)
echo.
echo echo 项目管理小助手已启动！请不要关闭此窗口，否则程序将停止运行。
echo echo 如需访问应用，请打开浏览器并访问: http://localhost:%%WEB_PORT%%
echo echo.
echo echo 按Ctrl+C可以停止程序
echo.
echo REM 等待用户按Ctrl+C
echo pause
echo REM 关闭所有相关进程
echo taskkill /f /fi "WINDOWTITLE eq 项目管理小助手_后台服务*"
echo taskkill /f /fi "WINDOWTITLE eq 项目管理小助手_前端*"
echo.
echo echo 项目管理小助手已关闭！
echo pause
) > "%APP_DIR%\启动小助手.bat"

echo 创建调试版启动脚本...
(
echo @echo on
echo title 项目管理小助手（调试模式）
echo.
echo cd /d %%~dp0
echo echo 正在调试模式下启动项目管理小助手...
echo.
echo set JAVA_PATH=service_data\jre\bin\java.exe
echo set JAR_PATH=service_data\project-assistant-service-1.0.0.jar
echo set SERVER_PORT=8080
echo set WEB_PORT=80
echo.
echo echo 检查Java路径...
echo if exist "%%JAVA_PATH%%" (
echo     echo Java路径正常: %%JAVA_PATH%%
echo ^) else (
echo     echo 错误: 找不到Java路径 %%JAVA_PATH%%
echo     pause
echo     exit /b 1
echo ^)
echo.
echo echo 检查JAR文件...
echo if exist "%%JAR_PATH%%" (
echo     echo JAR文件正常
echo ^) else (
echo     echo 错误: 找不到JAR文件
echo     pause
echo     exit /b 1
echo ^)
echo.
echo echo 检查web目录...
echo dir web\index.html
echo.
echo echo 以调试模式启动应用程序...
echo echo 启动后台服务...
echo "%%JAVA_PATH%%" -Xmx512m -Dfile.encoding=UTF-8 -Dserver.port=%%SERVER_PORT%% -Dh2.bindAddress=127.0.0.1 -Dspring.config.location=file:./service_data/conf/application.yml -Dlogging.level.com.timelordtty=DEBUG -jar "%%JAR_PATH%%"
echo.
echo echo 如果小助手没有成功运行起来，就截一下上面的图，发给宝宝的专属工程师哦~
echo pause
) > "%APP_DIR%\调试小助手.bat"

echo ====================
echo 9. 创建自述文件
echo ====================

echo ================ ❤ ================== > "%APP_DIR%\~❤~宝宝专属项目管理小助手の甜蜜指引~❤~.txt"
echo ========== 给最可爱宝宝的专属指引 ========== >> "%APP_DIR%\~❤~宝宝专属项目管理小助手の甜蜜指引~❤~.txt"
echo. >> "%APP_DIR%\~❤~宝宝专属项目管理小助手の甜蜜指引~❤~.txt"

echo 🎀🌸🎀🌸🎀 暖暖的启动指引 🎀🌸🎀🌸🎀 >> "%APP_DIR%\~❤~宝宝专属项目管理小助手の甜蜜指引~❤~.txt"
echo 双击「启动小助手.bat」开启小助手~ >> "%APP_DIR%\~❤~宝宝专属项目管理小助手の甜蜜指引~❤~.txt"
echo 遇到小调皮时，点「调试小助手.bat」后把日志发给宝宝的专属工程师就好啦 >> "%APP_DIR%\~❤~宝宝专属项目管理小助手の甜蜜指引~❤~.txt"
echo. >> "%APP_DIR%\~❤~宝宝专属项目管理小助手の甜蜜指引~❤~.txt"

echo 🌸❀•°*" 专属小助手功能 "*°•❀🌸 >> "%APP_DIR%\~❤~宝宝专属项目管理小助手の甜蜜指引~❤~.txt"
echo →💌 智能项目管理（项目日历与关键日期提醒） >> "%APP_DIR%\~❤~宝宝专属项目管理小助手の甜蜜指引~❤~.txt"
echo →💰 金额转换（超智能的中文大写金额转换器） >> "%APP_DIR%\~❤~宝宝专属项目管理小助手の甜蜜指引~❤~.txt"
echo →📜 文档生成（一键生成超专业的文档） >> "%APP_DIR%\~❤~宝宝专属项目管理小助手の甜蜜指引~❤~.txt"
echo →✎ 文本纠错（帮你找出那些讨厌的错别字） >> "%APP_DIR%\~❤~宝宝专属项目管理小助手の甜蜜指引~❤~.txt"
echo. >> "%APP_DIR%\~❤~宝宝专属项目管理小助手の甜蜜指引~❤~.txt"

echo 💌💕 项目管理小助手の专属使用指南 💕💌 >> "%APP_DIR%\~❤~宝宝专属项目管理小助手の甜蜜指引~❤~.txt"
echo 宝宝~这个小工具可以帮你轻松管理项目哦~ >> "%APP_DIR%\~❤~宝宝专属项目管理小助手の甜蜜指引~❤~.txt"
echo ① 【项目管理】 - 在主页面可以看到日历视图，显示所有项目的关键日期 >> "%APP_DIR%\~❤~宝宝专属项目管理小助手の甜蜜指引~❤~.txt"
echo ② 【新建项目】 - 点击"新建项目"按钮，填写项目名称和上网日期，其他日期会自动计算哦~ >> "%APP_DIR%\~❤~宝宝专属项目管理小助手の甜蜜指引~❤~.txt"
echo ③ 【工具箱】 - 右上角有超实用的小工具，金额转换、文档生成和文本纠错，都是为宝宝量身定制的呢~ >> "%APP_DIR%\~❤~宝宝专属项目管理小助手の甜蜜指引~❤~.txt"
echo. >> "%APP_DIR%\~❤~宝宝专属项目管理小助手の甜蜜指引~❤~.txt"

echo 🎀 小秘密：节假日已经自动设置好啦，项目日期会自动排除节假日和周末，超级智能~ >> "%APP_DIR%\~❤~宝宝专属项目管理小助手の甜蜜指引~❤~.txt"
echo 🎀 偷偷告诉宝宝：金额转换里藏了一个小彩蛋，宝宝可以试试转换特殊数字哦~ >> "%APP_DIR%\~❤~宝宝专属项目管理小助手の甜蜜指引~❤~.txt"

echo 💝✨💝✨💝 给宝宝的爱心小贴士 💝✨💝✨💝 >> "%APP_DIR%\~❤~宝宝专属项目管理小助手の甜蜜指引~❤~.txt"
echo ❤ 程序已打包完整环境，不用费心配置（宝宝只管用，技术问题交给我💖）>> "%APP_DIR%\~❤~宝宝专属项目管理小助手の甜蜜指引~❤~.txt"
echo ❤ service_data 是程序的核心，宝宝可以不用去管它哦~ >> "%APP_DIR%\~❤~宝宝专属项目管理小助手の甜蜜指引~❤~.txt"
echo ❤ data 文件夹存储着宝宝的重要数据，升级时要记得备份哦~ >> "%APP_DIR%\~❤~宝宝专属项目管理小助手の甜蜜指引~❤~.txt"
echo ❤ 日志在 service_data\logs 里，有问题就把它发给24小时在线的专属客服吧 >> "%APP_DIR%\~❤~宝宝专属项目管理小助手の甜蜜指引~❤~.txt"
echo. >> "%APP_DIR%\~❤~宝宝专属项目管理小助手の甜蜜指引~❤~.txt"

echo 🌈☁️🌈☁️🌈 升级小锦囊 ☁️🌈☁️🌈☁️ >> "%APP_DIR%\~❤~宝宝专属项目管理小助手の甜蜜指引~❤~.txt"
echo → 右键对"data"文件夹进行一下复制备份 >> "%APP_DIR%\~❤~宝宝专属项目管理小助手の甜蜜指引~❤~.txt"
echo → 直接用新版本的压缩包全部内容覆盖全部文件，不需要删除内容哦 >> "%APP_DIR%\~❤~宝宝专属项目管理小助手の甜蜜指引~❤~.txt"
echo → 将备份好的"data"文件夹覆盖到新版本的相同位置就好啦 ~ >> "%APP_DIR%\~❤~宝宝专属项目管理小助手の甜蜜指引~❤~.txt"
echo → 启动时将看到：「欢迎回来，最棒的宝宝！」>> "%APP_DIR%\~❤~宝宝专属项目管理小助手の甜蜜指引~❤~.txt"

echo ====================
echo 10. 测试应用程序
echo ====================
echo 测试JRE安装...
if exist "%JRE_DIR%\bin\java.exe" (
    echo JRE安装正常
    "%JRE_DIR%\bin\java.exe" -version
) else (
    echo 警告: JRE可能不完整，应用程序可能无法启动
)

echo 检查关键文件...
echo 检查后端JAR:
if exist "%SERVICE_DATA_DIR%\project-assistant-service-1.0.0.jar" (
    echo 后端JAR文件正常
) else (
    echo 警告: 后端JAR文件缺失，请检查编译过程
)

echo 检查前端文件:
if exist "%WEB_DIR%\index.html" (
    echo 前端文件正常
) else (
    echo 警告: 前端文件缺失，请检查编译过程
)

echo ====================
echo 打包完成!
echo ====================
echo 应用程序文件夹: %APP_DIR%
echo.
echo 文件夹结构说明:
echo - 根目录: 启动脚本、说明文档
echo - web目录: 前端静态文件
echo - data目录: 存放数据（升级时应保留）
echo - service_data目录: 后端服务、JRE、配置文件和日志
echo.
echo 使用方法:
echo 1. 将整个"%APP_DIR%"文件夹复制给使用者
echo 2. 双击"启动小助手.bat"运行程序
echo 3. 若有问题，运行"调试小助手.bat"获取详细错误信息
echo.

set /p ANSWER=是否立即运行应用程序? (Y/N): 
if /i "%ANSWER%" == "Y" (
    echo 正在启动项目管理小助手...
    cd /d "%APP_DIR%"
    call "启动小助手.bat"
    cd /d "%PROJECT_ROOT%"
) else (
    echo 您可以稍后运行"%APP_DIR%\启动小助手.bat"启动应用
)

echo.
echo 完成所有操作！按任意键退出...
pause 