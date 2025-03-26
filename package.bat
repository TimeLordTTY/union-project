@echo off
title 多功能工具集打包和运行

echo ====================
echo 0. 设置路径
echo ====================
REM 设置Java路径
set JAVA_HOME=D:\Soft\Java\jdk-23
set PATH=%JAVA_HOME%\bin;%PATH%

REM 设置Maven路径 - 确保使用3.9.9版本
set MAVEN_HOME=E:\Apache\apache-maven-3.9.9
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
set APP_DIR=PersonalApps\多功能工具集
set LIB_DIR=%APP_DIR%\lib
set JRE_DIR=%APP_DIR%\jre
set JAVAFX_MODULES_DIR=%APP_DIR%\lib\javafx-modules
set TEMPLATES_DIR=%APP_DIR%\templates
set LOG_DIR=%APP_DIR%\logs

if exist "%APP_DIR%" (
    echo 清理现有目录...
    rd /s /q "%APP_DIR%"
)

echo 创建目录结构...
mkdir "%APP_DIR%"
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
mkdir "%APP_DIR%\conf"

echo 创建日志目录的说明文件...
echo 此目录存放应用程序日志文件 > "%LOG_DIR%\README.txt"
echo 应用程序运行时会自动在这里创建日志文件 >> "%LOG_DIR%\README.txt"
echo 如果遇到问题，请查看此处的日志以获取详细信息 >> "%LOG_DIR%\README.txt"

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

echo 检查EXE文件是否存在...
if exist "target\multi-tools.exe" (
    echo 复制EXE文件...
    copy /Y "target\multi-tools.exe" "%APP_DIR%\"
) else (
    echo 警告: 未找到EXE文件，将仅使用JAR文件启动
)

echo ====================
echo 7. 复制配置文件
echo ====================
echo 复制配置文件...

echo 复制日志配置文件...
copy /Y "src\main\resources\logging.properties" "%APP_DIR%\conf\"
copy /Y "src\main\resources\logback.xml" "%APP_DIR%\conf\"

echo 复制API配置文件...
mkdir "%APP_DIR%\config"
copy /Y "src\main\resources\api.properties" "%APP_DIR%\config\"

echo ====================
echo 8. 创建模板文件
echo ====================
echo 创建Word文档模板、Excel模板和JSON示例数据...

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
echo 创建Word文档模板...
cd /d "%PROJECT_ROOT%"
call "%JAVA_HOME%\bin\java.exe" -cp !CLASSPATH! com.timelordtty.docgen.util.DocxTemplateGenerator "%PROJECT_ROOT%\%TEMPLATES_DIR%\word\订单模板.docx"
if %ERRORLEVEL% NEQ 0 (
    echo 警告: Word模板创建失败，将使用备用模板
    echo 创建简单的Word模板...
    echo 这是一个示例Word模板文件 > "%TEMPLATES_DIR%\word\简单订单模板.docx"
)

REM 创建Excel模板
echo 创建Excel模板...
call "%JAVA_HOME%\bin\java.exe" -cp !CLASSPATH! com.timelordtty.docgen.util.ExcelTemplateGenerator "%PROJECT_ROOT%\%TEMPLATES_DIR%\excel\订单模板.xlsx"
if %ERRORLEVEL% NEQ 0 (
    echo 警告: Excel模板创建失败，将创建空的Excel文件
    copy NUL "%TEMPLATES_DIR%\excel\简单订单模板.xlsx"
)

REM 创建JSON示例数据
echo 创建JSON示例数据...
call "%JAVA_HOME%\bin\java.exe" -cp !CLASSPATH! com.timelordtty.docgen.util.JsonPlaceholderProcessor "%PROJECT_ROOT%\%TEMPLATES_DIR%\json\订单数据.json"
if %ERRORLEVEL% NEQ 0 (
    echo 警告: JSON示例数据创建失败，创建简单的JSON数据...
    echo { > "%TEMPLATES_DIR%\json\简单订单数据.json"
    echo   "client": { >> "%TEMPLATES_DIR%\json\简单订单数据.json"
    echo     "name": "张三", >> "%TEMPLATES_DIR%\json\简单订单数据.json"
    echo     "address": "北京市海淀区", >> "%TEMPLATES_DIR%\json\简单订单数据.json"
    echo     "phone": "13800138000" >> "%TEMPLATES_DIR%\json\简单订单数据.json"
    echo   }, >> "%TEMPLATES_DIR%\json\简单订单数据.json"
    echo   "order": { >> "%TEMPLATES_DIR%\json\简单订单数据.json"
    echo     "id": "ORD-2023-0001", >> "%TEMPLATES_DIR%\json\简单订单数据.json"
    echo     "delivery_date": "2023-12-31", >> "%TEMPLATES_DIR%\json\简单订单数据.json"
    echo     "items": [ >> "%TEMPLATES_DIR%\json\简单订单数据.json"
    echo       { >> "%TEMPLATES_DIR%\json\简单订单数据.json"
    echo         "name": "笔记本电脑", >> "%TEMPLATES_DIR%\json\简单订单数据.json"
    echo         "quantity": 1, >> "%TEMPLATES_DIR%\json\简单订单数据.json"
    echo         "price": 6999, >> "%TEMPLATES_DIR%\json\简单订单数据.json"
    echo         "subtotal": 6999 >> "%TEMPLATES_DIR%\json\简单订单数据.json"
    echo       }, >> "%TEMPLATES_DIR%\json\简单订单数据.json"
    echo       { >> "%TEMPLATES_DIR%\json\简单订单数据.json"
    echo         "name": "无线鼠标", >> "%TEMPLATES_DIR%\json\简单订单数据.json"
    echo         "quantity": 2, >> "%TEMPLATES_DIR%\json\简单订单数据.json"
    echo         "price": 99, >> "%TEMPLATES_DIR%\json\简单订单数据.json"
    echo         "subtotal": 198 >> "%TEMPLATES_DIR%\json\简单订单数据.json"
    echo       } >> "%TEMPLATES_DIR%\json\简单订单数据.json"
    echo     ], >> "%TEMPLATES_DIR%\json\简单订单数据.json"
    echo     "total_amount": 7197 >> "%TEMPLATES_DIR%\json\简单订单数据.json"
    echo   }, >> "%TEMPLATES_DIR%\json\简单订单数据.json"
    echo   "company": { >> "%TEMPLATES_DIR%\json\简单订单数据.json"
    echo     "name": "优秀科技有限公司", >> "%TEMPLATES_DIR%\json\简单订单数据.json"
    echo     "service_phone": "400-800-8888" >> "%TEMPLATES_DIR%\json\简单订单数据.json"
    echo   }, >> "%TEMPLATES_DIR%\json\简单订单数据.json"
    echo   "today": "2023-12-25" >> "%TEMPLATES_DIR%\json\简单订单数据.json"
    echo } >> "%TEMPLATES_DIR%\json\简单订单数据.json"
)

echo 检查模板目录中的文件...
dir "%TEMPLATES_DIR%\word"
dir "%TEMPLATES_DIR%\excel"
dir "%TEMPLATES_DIR%\json"

echo ====================
echo 8.1 验证关键依赖
echo ====================
echo 检查关键POI依赖是否存在...
if not exist "%LIB_DIR%\poi-ooxml-5.2.3.jar" (
    echo 警告: 未找到关键依赖 poi-ooxml-5.2.3.jar，正在尝试重新复制...
    copy /Y "%PROJECT_ROOT%\target\dependency\poi-ooxml-5.2.3.jar" "%LIB_DIR%\"
)

if not exist "%LIB_DIR%\poi-ooxml-lite-5.2.3.jar" (
    echo 警告: 未找到关键依赖 poi-ooxml-lite-5.2.3.jar，正在尝试重新复制...
    copy /Y "%PROJECT_ROOT%\target\dependency\poi-ooxml-lite-5.2.3.jar" "%LIB_DIR%\"
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

echo 检查lib目录中的关键依赖:
dir "%LIB_DIR%\poi-*.jar"
dir "%LIB_DIR%\jackson-*.jar"
dir "%LIB_DIR%\xmlbeans-*.jar"

echo ====================
echo 9. 创建启动脚本
echo ====================
echo 创建启动批处理文件...

(
echo @echo on
echo title 多功能工具集
echo.
echo cd /d %%~dp0
echo echo 正在启动多功能工具集...
echo.
echo set JAVA_PATH=jre\bin\java.exe
echo set JAR_PATH=multi-tools-1.0.jar
echo.
echo if not exist "%%JAVA_PATH%%" (
echo     echo 错误: 找不到Java运行环境，请确保jre目录完整
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
echo dir lib\javafx-modules\*.jar
echo.
echo echo 启动应用程序...
echo "%%JAVA_PATH%%" -Dfile.encoding=UTF-8 --class-path="%%JAR_PATH%%;lib\*;lib\javafx-modules\*" --module-path="lib\javafx-modules" --add-modules=javafx.controls,javafx.fxml,javafx.graphics -Djava.util.logging.config.file=conf/logging.properties -jar "%%JAR_PATH%%"
echo.
echo if %%ERRORLEVEL%% NEQ 0 (
echo     echo 应用程序启动失败，错误代码: %%ERRORLEVEL%%
echo     pause
echo ^)
) > "%APP_DIR%\启动多功能工具集.bat"

echo 创建调试版启动脚本...
(
echo @echo on
echo title 多功能工具集（调试模式）
echo.
echo cd /d %%~dp0
echo echo 正在调试模式下启动多功能工具集...
echo.
echo set JAVA_PATH=jre\bin\java.exe
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
echo dir lib\*.jar
echo echo 检查JavaFX模块...
echo dir lib\javafx-modules\*.jar
echo.
echo echo 以调试模式启动应用程序...
echo "%%JAVA_PATH%%" -Xmx512m -verbose:jni -verbose:class -Dfile.encoding=UTF-8 -Djava.util.logging.config.file=conf/logging.properties -Dlog.level=DEBUG --class-path="multi-tools-1.0.jar;lib\*;lib\javafx-modules\*" --module-path="lib\javafx-modules" --add-modules=javafx.controls,javafx.fxml,javafx.graphics -jar multi-tools-1.0.jar
echo.
echo echo 如果应用程序没有启动，请截图上面的错误信息并发送给开发人员
echo pause
) > "%APP_DIR%\调试启动.bat"

echo ====================
echo 10. 创建日志配置文件
echo ====================
(
echo # 日志配置文件
echo handlers=java.util.logging.FileHandler, java.util.logging.ConsoleHandler
echo .level=INFO
echo.
echo # 文件日志处理器
echo java.util.logging.FileHandler.pattern=logs/app_%%u_%%g.log
echo java.util.logging.FileHandler.limit=5000000
echo java.util.logging.FileHandler.count=10
echo java.util.logging.FileHandler.formatter=java.util.logging.SimpleFormatter
echo java.util.logging.FileHandler.level=ALL
echo.
echo # 控制台日志处理器
echo java.util.logging.ConsoleHandler.level=INFO
echo java.util.logging.ConsoleHandler.formatter=java.util.logging.SimpleFormatter
echo.
echo # 格式化器
echo java.util.logging.SimpleFormatter.format=[%%1$tF %%1$tT] [%%4$s] [%%2$s] %%5$s %%n
echo.
echo # 应用程序日志级别
echo com.timelordtty.level=INFO
) > "%APP_DIR%\conf\logging.properties"

echo ====================
echo 11. 创建自述文件
echo ====================
(
echo # 多功能工具集使用说明
echo.
echo ## 启动方法
echo.
echo 1. 双击"启动多功能工具集.bat"文件启动应用
echo 2. 如果启动失败，请双击"调试启动.bat"获取更详细的错误信息
echo.
echo ## 注意事项
echo.
echo - 此应用包含完整运行环境，无需安装Java
echo - 不要删除lib和jre文件夹，它们包含必要的运行库
echo - 日志文件保存在logs目录下，可以查看日志了解程序运行情况
echo - 程序附带了模板示例文件，存放在templates目录下
echo - 如果程序无法启动，请检查是否缺少文件
echo - 如果遇到问题，请使用调试启动脚本收集错误信息
) > "%APP_DIR%\使用说明.txt"

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
dir "%TEMPLATES_DIR%\json"

echo ====================
echo 打包完成!
echo ====================
echo 应用程序文件夹: %CD%\%APP_DIR%
echo.
echo 使用方法:
echo 1. 将整个"%APP_DIR%"文件夹复制到目标计算机
echo 2. 双击"启动多功能工具集.bat"运行程序
echo 3. 若有问题，运行"调试启动.bat"获取详细错误信息
echo.

set /p ANSWER=是否立即运行应用程序? (Y/N): 
if /i "%ANSWER%" == "Y" (
    echo 正在启动多功能工具集...
    cd /d "%APP_DIR%"
    call "调试启动.bat"
    cd /d "%PROJECT_ROOT%"
) else (
    echo 您可以稍后运行"%APP_DIR%\启动多功能工具集.bat"启动应用
)

echo.
echo 完成所有操作！按任意键退出...
pause 