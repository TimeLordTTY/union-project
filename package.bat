@echo off
title ProjectAssistant打包和运行
setlocal EnableDelayedExpansion

echo ====================
echo 0. 设置路径
echo ====================
REM 设置Java路径
set JAVA_HOME=D:\Soft\Java\jdk-17
set PATH=%JAVA_HOME%\bin;%PATH%

REM 设置Maven路径 - 确保使用3.9.9版本
set MAVEN_HOME=D:\Soft\Maven\apache-maven-3.9.9
set PATH=%MAVEN_HOME%\bin;%PATH%
set MVN_CMD=%MAVEN_HOME%\bin\mvn

REM 设置项目根目录
set PROJECT_ROOT=%~dp0

REM 设置JavaFX版本和路径 - 使用Maven仓库中的版本
set JAVAFX_VERSION=21.0.1
set JAVAFX_DIR=%MAVEN_HOME%\Repository\org\openjfx

echo ====================
echo 1. 检查Maven版本
echo ====================
call "%MVN_CMD%" --version | findstr "3.9.9"
if %ERRORLEVEL% NEQ 0 (
    echo 错误: 必须使用Maven 3.9.9版本！
    echo 当前使用的Maven版本不是3.9.9，请确保正确设置MAVEN_HOME环境变量
    echo 当前MAVEN_HOME: %MAVEN_HOME%
    pause
    exit /b 1
)
echo Maven版本检查通过，继续执行...

echo ====================
echo 2. 创建应用目录结构
echo ====================
set APP_DIR=%~dp0ProjectAssistant
set SERVICE_DATA_DIR=%APP_DIR%\service_data
set LIB_DIR=%SERVICE_DATA_DIR%\lib
set JRE_DIR=%SERVICE_DATA_DIR%\jre
set JAVAFX_MODULES_DIR=%SERVICE_DATA_DIR%\lib\javafx-modules
set TEMPLATES_DIR=%APP_DIR%\templates
set LOG_DIR=%SERVICE_DATA_DIR%\logs
set CONF_DIR=%SERVICE_DATA_DIR%\conf
set DATA_DIR=%APP_DIR%\data
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
    echo 清理ProjectAssistant目录...
    rd /s /q "%~dp0ProjectAssistant"
)

echo 创建目录结构...
mkdir "%APP_DIR%"
mkdir "%SERVICE_DATA_DIR%"
mkdir "%LIB_DIR%"
mkdir "%JRE_DIR%"
mkdir "%JRE_DIR%\bin"
mkdir "%JRE_DIR%\lib"
mkdir "%JAVAFX_MODULES_DIR%"
mkdir "%TEMPLATES_DIR%"
mkdir "%TEMPLATES_DIR%\word"
mkdir "%TEMPLATES_DIR%\excel"
mkdir "%TEMPLATES_DIR%\json"
mkdir "%LOG_DIR%"
mkdir "%CONF_DIR%"
mkdir "%DATA_DIR%"

echo 还原数据目录（如果存在备份）...
if exist "%DATA_BACKUP_DIR%" (
    echo 正在还原数据目录...
    xcopy /E /I /Y "%DATA_BACKUP_DIR%" "%DATA_DIR%"
    rd /s /q "%DATA_BACKUP_DIR%"
    echo 数据目录已还原
)

echo ====================
echo 3. 编译项目
echo ====================
cd /d "%PROJECT_ROOT%"
call "%MVN_CMD%" clean package -DskipTests
if %ERRORLEVEL% NEQ 0 (
    echo 编译失败，错误代码: %ERRORLEVEL%
    pause
    exit /b %ERRORLEVEL%
)

echo ====================
echo 4. 复制JavaFX库文件（从Maven仓库）
echo ====================
echo 复制JavaFX库文件...
if exist "%JAVAFX_DIR%" (
    copy /Y "%JAVAFX_DIR%\javafx-graphics\%JAVAFX_VERSION%\javafx-graphics-%JAVAFX_VERSION%.jar" "%JAVAFX_MODULES_DIR%\"
    copy /Y "%JAVAFX_DIR%\javafx-graphics\%JAVAFX_VERSION%\javafx-graphics-%JAVAFX_VERSION%-win.jar" "%JAVAFX_MODULES_DIR%\"
    copy /Y "%JAVAFX_DIR%\javafx-controls\%JAVAFX_VERSION%\javafx-controls-%JAVAFX_VERSION%.jar" "%JAVAFX_MODULES_DIR%\"
    copy /Y "%JAVAFX_DIR%\javafx-controls\%JAVAFX_VERSION%\javafx-controls-%JAVAFX_VERSION%-win.jar" "%JAVAFX_MODULES_DIR%\"
    copy /Y "%JAVAFX_DIR%\javafx-base\%JAVAFX_VERSION%\javafx-base-%JAVAFX_VERSION%.jar" "%JAVAFX_MODULES_DIR%\"
    copy /Y "%JAVAFX_DIR%\javafx-base\%JAVAFX_VERSION%\javafx-base-%JAVAFX_VERSION%-win.jar" "%JAVAFX_MODULES_DIR%\"
    copy /Y "%JAVAFX_DIR%\javafx-fxml\%JAVAFX_VERSION%\javafx-fxml-%JAVAFX_VERSION%.jar" "%JAVAFX_MODULES_DIR%\"
    copy /Y "%JAVAFX_DIR%\javafx-fxml\%JAVAFX_VERSION%\javafx-fxml-%JAVAFX_VERSION%-win.jar" "%JAVAFX_MODULES_DIR%\"
    
    echo JavaFX库文件复制完成，检查结果...
    dir "%JAVAFX_MODULES_DIR%\*.jar"
) else (
    echo 警告: 未在Maven仓库找到JavaFX依赖，尝试从项目依赖目录复制...
    
    echo 从Maven下载JavaFX依赖...
    call "%MVN_CMD%" dependency:copy-dependencies -DoutputDirectory=target/dependency
    
    echo 复制依赖到模块目录...
    copy /Y "target\dependency\javafx-*.jar" "%JAVAFX_MODULES_DIR%\"
    
    echo 检查复制结果...
    dir "%JAVAFX_MODULES_DIR%\*.jar"
    
    if not exist "%JAVAFX_MODULES_DIR%\javafx-controls*.jar" (
        echo 错误: JavaFX依赖复制失败，应用程序将无法运行！
        echo 请手动下载JavaFX SDK并将lib目录中的jar放到 %JAVAFX_MODULES_DIR%
        pause
        exit /b 1
    )
)

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
echo 复制JAR文件...
copy /Y "target\multi-tools-1.0.jar" "%APP_DIR%\"
if %ERRORLEVEL% NEQ 0 (
    echo 错误: 无法复制JAR文件，请确保编译成功
    pause
    exit /b 1
)

echo ====================
echo 7. 复制配置文件
echo ====================
echo 复制配置文件...

REM 检查配置文件是否重复，如果logback.xml已经包含所有功能，使用它代替logging.properties
REM 删除logging.properties的复制
REM 现在只使用logback.xml作为唯一的日志配置文件
REM copy /Y "src\main\resources\logging.properties" "%CONF_DIR%\"

REM 仅复制logback.xml作为唯一的日志配置文件
copy /Y "src\main\resources\logback.xml" "%CONF_DIR%\"

REM 复制API配置文件到conf目录（合并config到conf）
copy /Y "src\main\resources\api.properties" "%CONF_DIR%\"

echo ====================
echo 8. 创建模板文件
echo ====================
echo 创建Word文档模板、Excel模板和测试数据...

echo 下载所有依赖到target/dependency目录...
call "%MVN_CMD%" dependency:copy-dependencies -DoutputDirectory=target/dependency

echo 复制JAR依赖到LIB目录，以便模板生成...
mkdir "%LIB_DIR%"
copy /Y "target\dependency\*.jar" "%LIB_DIR%\"

REM 构建完整的类路径字符串，包括所有依赖
setlocal EnableDelayedExpansion
set CLASSPATH="%PROJECT_ROOT%\target\classes"
for %%i in ("%PROJECT_ROOT%\target\dependency\*.jar") do set CLASSPATH=!CLASSPATH!;%%i

echo 使用的类路径: !CLASSPATH!

REM 创建Word模板
echo 创建Word项目报告模板...
cd /d "%PROJECT_ROOT%"
if not exist "%TEMPLATES_DIR%\word" mkdir "%TEMPLATES_DIR%\word"
call "%JAVA_HOME%\bin\java.exe" -cp !CLASSPATH! com.timelordtty.docgen.utils.DocxTemplateGenerator "%TEMPLATES_DIR%\word\项目报告模板.docx"
if %ERRORLEVEL% NEQ 0 (
    echo 警告: Word项目报告模板创建失败，将使用备用模板
    echo 创建简单的Word模板...
    echo 这是一个示例Word模板文件 > "%TEMPLATES_DIR%\word\简单项目报告模板.docx"
)

REM 创建Excel模板
echo 创建Excel项目模板...
if not exist "%TEMPLATES_DIR%\excel" mkdir "%TEMPLATES_DIR%\excel"
call "%JAVA_HOME%\bin\java.exe" -cp !CLASSPATH! com.timelordtty.docgen.utils.ExcelTemplateGenerator "%TEMPLATES_DIR%\excel\项目模板.xlsx"
if %ERRORLEVEL% NEQ 0 (
    echo 警告: Excel项目模板创建失败，将创建空的Excel文件
    copy NUL "%TEMPLATES_DIR%\excel\简单项目模板.xlsx"
)

echo 创建Excel测试数据...
call "%JAVA_HOME%\bin\java.exe" -cp !CLASSPATH! com.timelordtty.docgen.utils.ExcelDataGenerator "%TEMPLATES_DIR%\excel\客户项目数据.xlsx"
if %ERRORLEVEL% NEQ 0 (
    echo 警告: Excel测试数据创建失败，创建简单的Excel文件
    copy NUL "%TEMPLATES_DIR%\excel\简单客户数据.xlsx"
)

echo 检查模板目录中的文件...
dir "%TEMPLATES_DIR%\word"
dir "%TEMPLATES_DIR%\excel"

echo ====================
echo 8.1 验证关键依赖
echo ====================
echo 检查关键POI依赖是否存在...
if not exist "%LIB_DIR%\poi-ooxml-5.2.2.jar" (
    echo 警告: 未找到关键依赖 poi-ooxml-5.2.2.jar，正在尝试重新复制...
    copy /Y "%PROJECT_ROOT%\target\dependency\poi-ooxml-5.2.2.jar" "%LIB_DIR%\"
)

if not exist "%LIB_DIR%\poi-ooxml-lite-5.2.2.jar" (
    echo 警告: 未找到关键依赖 poi-ooxml-lite-5.2.2.jar，正在尝试重新复制...
    copy /Y "%PROJECT_ROOT%\target\dependency\poi-ooxml-lite-5.2.2.jar" "%LIB_DIR%\"
)

if not exist "%LIB_DIR%\poi-ooxml-schemas-4.1.2.jar" (
    echo 警告: 未找到关键依赖 poi-ooxml-schemas-4.1.2.jar，正在尝试重新复制...
    copy /Y "%PROJECT_ROOT%\target\dependency\poi-ooxml-schemas-4.1.2.jar" "%LIB_DIR%\"
)

if not exist "%LIB_DIR%\xmlbeans-5.1.1.jar" (
    echo 警告: 未找到关键依赖 xmlbeans-5.1.1.jar，正在尝试重新复制...
    copy /Y "%PROJECT_ROOT%\target\dependency\xmlbeans-5.1.1.jar" "%LIB_DIR%\"
)

if not exist "%LIB_DIR%\jackson-databind-2.15.2.jar" (
    echo 警告: 未找到关键依赖 jackson-databind-2.15.2.jar，正在尝试重新复制...
    copy /Y "%PROJECT_ROOT%\target\dependency\jackson-databind-2.15.2.jar" "%LIB_DIR%\"
)

if not exist "%LIB_DIR%\jackson-core-2.15.2.jar" (
    echo 警告: 未找到关键依赖 jackson-core-2.15.2.jar，正在尝试重新复制...
    copy /Y "%PROJECT_ROOT%\target\dependency\jackson-core-2.15.2.jar" "%LIB_DIR%\"
)

echo 检查H2数据库依赖是否存在...
if not exist "%LIB_DIR%\h2-2.2.224.jar" (
    echo 警告: 未找到H2数据库依赖，正在尝试重新复制...
    copy /Y "%PROJECT_ROOT%\target\dependency\h2-2.2.224.jar" "%LIB_DIR%\"
    if %ERRORLEVEL% NEQ 0 (
        echo 错误: 未找到H2数据库依赖，ProjectAssistant功能可能无法正常工作！
    )
)

echo 检查lib目录中的关键依赖:
dir "%LIB_DIR%\poi-*.jar"
dir "%LIB_DIR%\jackson-*.jar"
dir "%LIB_DIR%\xmlbeans-*.jar"
dir "%LIB_DIR%\h2-*.jar"

echo ====================
echo 9. 创建启动脚本
echo ====================
echo 创建启动批处理文件...

(
echo @echo off
echo title ProjectAssistant
echo.
echo cd /d %%~dp0
echo echo 正在启动ProjectAssistant...
echo.
echo set JAVA_PATH=service_data\jre\bin\java.exe
echo set JAR_PATH=multi-tools-1.0.jar
echo.
echo if not exist "%%JAVA_PATH%%" (
echo     echo 错误: 找不到Java运行环境，请确保service_data\jre目录完整
echo     pause
echo     exit /b 1
echo ^)
echo.
echo if not exist "%%JAR_PATH%%" (
echo     echo 错误: 找不到应用程序JAR文件
echo     pause
echo     exit /b 1
echo ^)
echo.
echo echo 检查JavaFX模块...
echo if not exist "service_data\lib\javafx-controls*.jar" (
echo     echo 错误: 找不到JavaFX模块，程序可能无法正常运行
echo     pause
echo ^)
echo.
echo echo 启动应用程序...
echo "%%JAVA_PATH%%" -Dfile.encoding=UTF-8 -Dh2.bindAddress=127.0.0.1 --class-path="%%JAR_PATH%%;service_data\lib\*;service_data\lib\*;resources" --module-path="service_data\lib\javafx-modules" --add-modules=javafx.controls,javafx.fxml,javafx.graphics -Djava.util.logging.config.file=service_data/conf/logback.xml -Duser.timezone=Asia/Shanghai -jar "%%JAR_PATH%%"
echo.
echo if %%ERRORLEVEL%% NEQ 0 (
echo     echo 应用程序启动失败，错误代码: %%ERRORLEVEL%%
echo     pause
echo ^)
) > "%APP_DIR%\启动小助手.bat"

echo 创建调试版启动脚本...
(
echo @echo on
echo title ProjectAssistant（调试模式）
echo.
echo cd /d %%~dp0
echo echo 正在调试模式下启动ProjectAssistant...
echo.
echo set JAVA_PATH=service_data\jre\bin\java.exe
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
echo if exist "multi-tools-1.0.jar" (
echo     echo JAR文件正常
echo ^) else (
echo     echo 错误: 找不到JAR文件
echo     pause
echo     exit /b 1
echo ^)
echo.
echo echo 检查依赖库...
echo dir service_data\lib\*.jar
echo echo 检查JavaFX模块...
echo dir service_data\lib\*.jar
echo echo 检查H2数据库依赖...
echo dir service_data\lib\h2-*.jar
echo.
echo echo 以调试模式启动应用程序...
echo "%%JAVA_PATH%%" -Xmx512m -verbose:jni -verbose:class -Dfile.encoding=UTF-8 -Dh2.bindAddress=127.0.0.1 -Djava.util.logging.config.file=service_data/conf/logback.xml -Dlog.level=DEBUG -Duser.timezone=Asia/Shanghai --class-path="multi-tools-1.0.jar;service_data\lib\*;service_data\lib\*;resources" --module-path="service_data\lib\javafx-modules" --add-modules=javafx.controls,javafx.fxml,javafx.graphics -jar "multi-tools-1.0.jar"
echo.
echo echo 如果宝宝的小助手没有成功运行起来，就截一下上面的图，发给宝宝的专属工程师哦~
echo pause
) > "%APP_DIR%\调试小助手.bat"

echo ====================
echo 11. 创建自述文件
echo ====================

echo ================ ❤ ================== >> "%APP_DIR%\~❤~宝宝专属项目管理小助手の甜蜜指引~❤~.txt"
echo ========== 给最可爱宝宝的专属指引 ========== >> "%APP_DIR%\~❤~宝宝专属项目管理小助手の甜蜜指引~❤~.txt"
echo. >> "%APP_DIR%\~❤~宝宝专属项目管理小助手の甜蜜指引~❤~.txt"

echo 🎀🌸🎀🌸🎀 暖暖的启动指引 🎀🌸🎀🌸🎀 >> "%APP_DIR%\~❤~宝宝专属项目管理小助手の甜蜜指引~❤~.txt"
echo 双击「启动小助手.bat」开启小助手~ >> "%APP_DIR%\~❤~宝宝专属项目管理小助手の甜蜜指引~❤~.txt"
echo 遇到小调皮时，点「调试小助手.bat」后把日志发给宝宝的专属工程师就好啦 >> "%APP_DIR%\~❤~宝宝专属项目管理小助手の甜蜜指引~❤~.txt"
echo. >> "%APP_DIR%\~❤~宝宝专属项目管理小助手の甜蜜指引~❤~.txt"

echo 🌸❀•°*" 专属小助手功能 "*°•❀🌸 >> "%APP_DIR%\~❤~宝宝专属项目管理小助手の甜蜜指引~❤~.txt"
echo →💌 智能项目管理（藏着我们的小秘密） >> "%APP_DIR%\~❤~宝宝专属项目管理小助手の甜蜜指引~❤~.txt"
echo →💰 金额转换（有特别小彩蛋💖） >> "%APP_DIR%\~❤~宝宝专属项目管理小助手の甜蜜指引~❤~.txt"
echo →📜 一键（三键）生成超专业文档（这个功能需要下个版本进行升级哦） >> "%APP_DIR%\~❤~宝宝专属项目管理小助手の甜蜜指引~❤~.txt"
echo →✎ 贴心错别字校正与文本替换（偷偷内置了一些不会生效的替换规则哦😘） >> "%APP_DIR%\~❤~宝宝专属项目管理小助手の甜蜜指引~❤~.txt"
echo. >> "%APP_DIR%\~❤~宝宝专属项目管理小助手の甜蜜指引~❤~.txt"
echo 💌💕 小文档生成器の专属使用指南 💕💌 >> "%APP_DIR%\~❤~宝宝专属项目管理小助手の甜蜜指引~❤~.txt"
echo 宝宝~这个小工具可以帮你把Word模板和Excel模板变成超专业的文档哦~ >> "%APP_DIR%\~❤~宝宝专属项目管理小助手の甜蜜指引~❤~.txt"
echo 使用方式超简单的，宝宝只需要三步就能变出美美的文档啦~ >> "%APP_DIR%\~❤~宝宝专属项目管理小助手の甜蜜指引~❤~.txt"
echo ① 选择文档类型（Word还是Excel呢？随宝宝喜欢~） >> "%APP_DIR%\~❤~宝宝专属项目管理小助手の甜蜜指引~❤~.txt"
echo ② 选择模板文件和数据文件（宝宝可以用我们准备好的模板哦，就在templates文件夹里~） >> "%APP_DIR%\~❤~宝宝专属项目管理小助手の甜蜜指引~❤~.txt"
echo ③ 点击生成按钮，文档就会像变魔术一样出现啦！(≧▽≦) >> "%APP_DIR%\~❤~宝宝专属项目管理小助手の甜蜜指引~❤~.txt"
echo. >> "%APP_DIR%\~❤~宝宝专属项目管理小助手の甜蜜指引~❤~.txt"
echo 🎀 小秘密：模板里面用${客户.姓名}这样的占位符，就可以自动替换成数据文件中的内容啦~ >> "%APP_DIR%\~❤~宝宝专属项目管理小助手の甜蜜指引~❤~.txt"
echo 🎀 偷偷告诉宝宝：我在每个模板里都藏了一个小小的爱心，宝宝找找看在哪里呢？💕 >> "%APP_DIR%\~❤~宝宝专属项目管理小助手の甜蜜指引~❤~.txt"

echo 💝✨💝✨💝 给宝宝的爱心小贴士 💝✨💝✨💝 >> "%APP_DIR%\~❤~宝宝专属项目管理小助手の甜蜜指引~❤~.txt"
echo ❤ 程序已打包完整环境，不用费心配置（宝宝只管用，技术问题交给我💖）>> "%APP_DIR%\~❤~宝宝专属项目管理小助手の甜蜜指引~❤~.txt"
echo ❤ service_data 是程序的核心，宝宝可以不用去管它哦~（修改也不是不行，毕竟我这里都有备份😉）>> "%APP_DIR%\~❤~宝宝专属项目管理小助手の甜蜜指引~❤~.txt"
echo ❤ data 文件夹装着宝宝的重要成果 >> "%APP_DIR%\~❤~宝宝专属项目管理小助手の甜蜜指引~❤~.txt"
echo ❤ 日志在 service_data\logs 里，藏着小助手的工作内容，有问题就把它发给24小时在线的专属客服吧 >> "%APP_DIR%\~❤~宝宝专属项目管理小助手の甜蜜指引~❤~.txt"
echo ❤ 从这个版本开始，项目管理小助手会包含很多彩蛋，宝宝可以慢慢探索哦 >> "%APP_DIR%\~❤~宝宝专属项目管理小助手の甜蜜指引~❤~.txt"
echo. >> "%APP_DIR%\~❤~宝宝专属项目管理小助手の甜蜜指引~❤~.txt"

echo 🌈☁️🌈☁️🌈 升级小锦囊 ☁️🌈☁️🌈☁️ >> "%APP_DIR%\~❤~宝宝专属项目管理小助手の甜蜜指引~❤~.txt"
echo → 右键对"data"文件夹进行一下复制备份 >> "%APP_DIR%\~❤~宝宝专属项目管理小助手の甜蜜指引~❤~.txt"
echo → 直接用新版本的压缩包全部内容覆盖全部文件，不需要删除内容哦 >> "%APP_DIR%\~❤~宝宝专属项目管理小助手の甜蜜指引~❤~.txt"
echo → 将备份好的"data"文件夹覆盖到新版本的相同位置就好啦 ~ >> "%APP_DIR%\~❤~宝宝专属项目管理小助手の甜蜜指引~❤~.txt"
echo → 启动时将看到：「欢迎回来，最棒的宝宝！」>> "%APP_DIR%\~❤~宝宝专属项目管理小助手の甜蜜指引~❤~.txt"

echo ====================
echo 12. 测试应用程序启动
echo ====================
echo 测试JavaFX模块...
echo JavaFX模块目录内容:
dir "%JAVAFX_MODULES_DIR%\*.jar"

echo 测试JRE安装...
if exist "%JRE_DIR%\bin\java.exe" (
    echo JRE安装正常
    "%JRE_DIR%\bin\java.exe" -version
) else (
    echo 警告: JRE可能不完整，应用程序可能无法启动
)

echo 测试模板文件...
echo 模板目录内容:
dir "%TEMPLATES_DIR%\word"
dir "%TEMPLATES_DIR%\excel"

echo 检查H2数据库依赖...
if exist "%LIB_DIR%\h2-*.jar" (
    echo H2数据库依赖正常
) else (
    echo 警告: H2数据库依赖缺失，ProjectAssistant功能可能无法正常工作
)

echo ====================
echo 打包完成!
echo ====================
echo 应用程序文件夹: %APP_DIR%
echo.
echo 文件夹结构说明:
echo - 根目录: 启动脚本、JAR文件、使用说明
echo - data目录: 存放ProjectAssistant数据（升级时应保留）
echo - templates目录: 存放文档模板
echo - service_data目录: 存放所有服务相关文件（lib、jre、conf、logs）
echo.
echo 使用方法:
echo 1. 将整个"%APP_DIR%"文件夹复制给使用者
echo 2. 双击"start.bat"运行程序
echo 3. 若有问题，运行"debug.bat"获取详细错误信息
echo.
echo ProjectAssistant功能特别说明:
echo - 数据存储在data目录中，使用H2数据库自动保存
echo - 支持2024和2025年法定节假日信息
echo - 所有日期计算自动考虑工作日和节假日因素
echo.

set /p ANSWER=是否立即运行应用程序? (Y/N): 
if /i "%ANSWER%" == "Y" (
    echo 正在启动ProjectAssistant...
    cd /d "%APP_DIR%"
    call "启动小助手.bat"
    cd /d "%PROJECT_ROOT%"
) else (
    echo 您可以稍后运行"%APP_DIR%\启动小助手.bat"启动应用
)

echo.
echo 完成所有操作！按任意键退出...
pause