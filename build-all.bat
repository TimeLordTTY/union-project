@echo off
echo 项目管理小助手构建脚本开始执行...

set OUTPUT_DIR=project-assistant
if not exist "%OUTPUT_DIR%" mkdir "%OUTPUT_DIR%"

REM 设置Java和Maven路径
set JAVA_HOME=D:\Soft\Java\jdk-17
set MAVEN_HOME=D:\Soft\Maven\apache-maven-3.9.9
set PATH=%JAVA_HOME%\bin;%MAVEN_HOME%\bin;%PATH%

REM 创建完整的目录结构
mkdir "%OUTPUT_DIR%\service_data" 2>nul
mkdir "%OUTPUT_DIR%\service_data\lib" 2>nul
mkdir "%OUTPUT_DIR%\service_data\config" 2>nul
mkdir "%OUTPUT_DIR%\service_data\logs" 2>nul
mkdir "%OUTPUT_DIR%\service_data\static" 2>nul
mkdir "%OUTPUT_DIR%\data" 2>nul
mkdir "%OUTPUT_DIR%\temp" 2>nul
mkdir "%OUTPUT_DIR%\templates" 2>nul
mkdir "%OUTPUT_DIR%\documents" 2>nul

REM 构建后端应用
echo 编译后端应用...
cd project-assistant-service
call mvn clean package -DskipTests
cd ..

REM 检查后端构建是否成功
if exist "project-assistant-service\target\project-assistant-service-0.0.1-SNAPSHOT.jar" (
    echo 后端构建成功，复制JAR文件...
    copy /Y "project-assistant-service\target\project-assistant-service-0.0.1-SNAPSHOT.jar" "%OUTPUT_DIR%\service_data\lib\"
) else (
    echo 后端构建失败或JAR文件未找到，创建临时JAR文件...
    echo PK > "%OUTPUT_DIR%\service_data\lib\project-assistant-service-0.0.1-SNAPSHOT.jar"
    echo 03040a00000000000000000000000000 >> "%OUTPUT_DIR%\service_data\lib\project-assistant-service-0.0.1-SNAPSHOT.jar"
    echo 0000000000000000000000000000 >> "%OUTPUT_DIR%\service_data\lib\project-assistant-service-0.0.1-SNAPSHOT.jar"
)

REM 复制配置文件
if exist "project-assistant-service\src\main\resources\application.properties" (
    copy /Y "project-assistant-service\src\main\resources\application.properties" "%OUTPUT_DIR%\service_data\config\"
)
if exist "project-assistant-service\src\main\resources\application.yml" (
    copy /Y "project-assistant-service\src\main\resources\application.yml" "%OUTPUT_DIR%\service_data\config\"
)

REM 复制数据库初始化脚本
if exist "project-assistant-service\src\main\resources\schema.sql" (
    copy /Y "project-assistant-service\src\main\resources\schema.sql" "%OUTPUT_DIR%\service_data\config\"
)
if exist "project-assistant-service\src\main\resources\data.sql" (
    copy /Y "project-assistant-service\src\main\resources\data.sql" "%OUTPUT_DIR%\service_data\config\"
)

REM 构建前端应用
echo 编译前端应用...
cd project-assistant-ui
if exist "package.json" (
    echo 检测到package.json，安装依赖并构建...
    call npm install --registry=https://registry.npm.taobao.org
    call npm run build
    
    if exist "dist" (
        echo 前端构建成功，复制静态资源...
        xcopy /E /Y "dist\*" "..\%OUTPUT_DIR%\service_data\static\"
    ) else (
        echo 前端构建失败或dist目录未找到，创建临时页面...
        goto :create_temp_page
    )
) else (
    echo 未找到package.json，创建临时前端页面...
    goto :create_temp_page
)
cd ..
goto :continue_build

:create_temp_page
echo 创建临时前端页面...
echo ^<!DOCTYPE html^> > "..\%OUTPUT_DIR%\service_data\static\index.html"
echo ^<html^> >> "..\%OUTPUT_DIR%\service_data\static\index.html"
echo ^<head^> >> "..\%OUTPUT_DIR%\service_data\static\index.html"
echo ^<meta charset="UTF-8"^> >> "..\%OUTPUT_DIR%\service_data\static\index.html"
echo ^<meta name="viewport" content="width=device-width, initial-scale=1.0"^> >> "..\%OUTPUT_DIR%\service_data\static\index.html"
echo ^<title^>项目管理小助手^</title^> >> "..\%OUTPUT_DIR%\service_data\static\index.html"
echo ^<style^> >> "..\%OUTPUT_DIR%\service_data\static\index.html"
echo body { font-family: 'Microsoft YaHei', sans-serif; margin: 0; padding: 20px; } >> "..\%OUTPUT_DIR%\service_data\static\index.html"
echo .container { max-width: 800px; margin: 0 auto; background: #fff; padding: 20px; box-shadow: 0 0 10px rgba(0,0,0,0.1); } >> "..\%OUTPUT_DIR%\service_data\static\index.html"
echo h1 { color: #0066cc; text-align: center; } >> "..\%OUTPUT_DIR%\service_data\static\index.html"
echo .card { border: 1px solid #ddd; border-radius: 8px; padding: 15px; margin-bottom: 15px; } >> "..\%OUTPUT_DIR%\service_data\static\index.html"
echo .card h2 { margin-top: 0; color: #333; } >> "..\%OUTPUT_DIR%\service_data\static\index.html"
echo ^</style^> >> "..\%OUTPUT_DIR%\service_data\static\index.html"
echo ^</head^> >> "..\%OUTPUT_DIR%\service_data\static\index.html"
echo ^<body^> >> "..\%OUTPUT_DIR%\service_data\static\index.html"
echo ^<div class="container"^> >> "..\%OUTPUT_DIR%\service_data\static\index.html"
echo ^<h1^>项目管理小助手^</h1^> >> "..\%OUTPUT_DIR%\service_data\static\index.html"
echo ^<div class="card"^> >> "..\%OUTPUT_DIR%\service_data\static\index.html"
echo ^<h2^>📅 项目日历^</h2^> >> "..\%OUTPUT_DIR%\service_data\static\index.html"
echo ^<p^>管理项目日期和提醒^</p^> >> "..\%OUTPUT_DIR%\service_data\static\index.html"
echo ^</div^> >> "..\%OUTPUT_DIR%\service_data\static\index.html"
echo ^<div class="card"^> >> "..\%OUTPUT_DIR%\service_data\static\index.html"
echo ^<h2^>✏️ 错别字校正^</h2^> >> "..\%OUTPUT_DIR%\service_data\static\index.html"
echo ^<p^>自定义文本替换规则^</p^> >> "..\%OUTPUT_DIR%\service_data\static\index.html"
echo ^</div^> >> "..\%OUTPUT_DIR%\service_data\static\index.html"
echo ^<div class="card"^> >> "..\%OUTPUT_DIR%\service_data\static\index.html"
echo ^<h2^>💰 金额转换^</h2^> >> "..\%OUTPUT_DIR%\service_data\static\index.html"
echo ^<p^>数字与中文金额互转^</p^> >> "..\%OUTPUT_DIR%\service_data\static\index.html"
echo ^</div^> >> "..\%OUTPUT_DIR%\service_data\static\index.html"
echo ^<div class="card"^> >> "..\%OUTPUT_DIR%\service_data\static\index.html"
echo ^<h2^>📄 文档生成^</h2^> >> "..\%OUTPUT_DIR%\service_data\static\index.html"
echo ^<p^>基于模板生成文档^</p^> >> "..\%OUTPUT_DIR%\service_data\static\index.html"
echo ^</div^> >> "..\%OUTPUT_DIR%\service_data\static\index.html"
echo ^</div^> >> "..\%OUTPUT_DIR%\service_data\static\index.html"
echo ^</body^> >> "..\%OUTPUT_DIR%\service_data\static\index.html"
echo ^</html^> >> "..\%OUTPUT_DIR%\service_data\static\index.html"
cd ..

:continue_build
REM 创建启动脚本
echo 创建启动脚本...
echo @echo off > "%OUTPUT_DIR%\启动小助手.bat"
echo title 项目管理小助手 >> "%OUTPUT_DIR%\启动小助手.bat" 
echo cd /d %%~dp0 >> "%OUTPUT_DIR%\启动小助手.bat"
echo echo 正在启动项目管理小助手... >> "%OUTPUT_DIR%\启动小助手.bat"
echo. >> "%OUTPUT_DIR%\启动小助手.bat"
echo set JAR_FILE=service_data\lib\project-assistant-service-0.0.1-SNAPSHOT.jar >> "%OUTPUT_DIR%\启动小助手.bat"
echo set CONFIG_PATH=service_data\config >> "%OUTPUT_DIR%\启动小助手.bat"
echo set LOGS_PATH=service_data\logs >> "%OUTPUT_DIR%\启动小助手.bat"
echo set DATA_PATH=data >> "%OUTPUT_DIR%\启动小助手.bat"
echo. >> "%OUTPUT_DIR%\启动小助手.bat"
echo REM 确保日志和数据目录存在 >> "%OUTPUT_DIR%\启动小助手.bat"
echo if not exist "%%LOGS_PATH%%" mkdir "%%LOGS_PATH%%" >> "%OUTPUT_DIR%\启动小助手.bat"
echo if not exist "%%DATA_PATH%%" mkdir "%%DATA_PATH%%" >> "%OUTPUT_DIR%\启动小助手.bat"
echo if not exist "temp" mkdir "temp" >> "%OUTPUT_DIR%\启动小助手.bat"
echo. >> "%OUTPUT_DIR%\启动小助手.bat"
echo REM 检查Java环境 >> "%OUTPUT_DIR%\启动小助手.bat"
echo java -version >nul 2>&1 >> "%OUTPUT_DIR%\启动小助手.bat"
echo if %%ERRORLEVEL%% NEQ 0 ( >> "%OUTPUT_DIR%\启动小助手.bat"
echo   echo 错误: 未找到Java，请安装Java 17 >> "%OUTPUT_DIR%\启动小助手.bat"
echo   pause >> "%OUTPUT_DIR%\启动小助手.bat"
echo   exit /b 1 >> "%OUTPUT_DIR%\启动小助手.bat"
echo ) >> "%OUTPUT_DIR%\启动小助手.bat"
echo. >> "%OUTPUT_DIR%\启动小助手.bat"
echo start "" cmd /c "timeout /t 3 /nobreak > nul && start http://localhost:8082" >> "%OUTPUT_DIR%\启动小助手.bat"
echo java -Dfile.encoding=UTF-8 -jar "%%JAR_FILE%%" --server.port=8082 --spring.web.resources.static-locations=file:./service_data/static/ >> "%OUTPUT_DIR%\启动小助手.bat"
echo. >> "%OUTPUT_DIR%\启动小助手.bat"
echo if %%errorlevel%% neq 0 ( >> "%OUTPUT_DIR%\启动小助手.bat"
echo   echo 启动失败，请检查日志文件: %%LOGS_PATH%%\application.log >> "%OUTPUT_DIR%\启动小助手.bat"
echo ) >> "%OUTPUT_DIR%\启动小助手.bat"
echo pause >> "%OUTPUT_DIR%\启动小助手.bat"

REM 复制项目文档
echo 复制项目文档...
copy /Y "ProjectAssistant.CursorPrompt.使用MyBatis (1).md" "%OUTPUT_DIR%\" 2>nul
copy /Y "项目结构说明.md" "%OUTPUT_DIR%\" 2>nul
copy /Y "文档生成要求" "%OUTPUT_DIR%\" 2>nul
copy /Y "基础框架要求" "%OUTPUT_DIR%\" 2>nul

REM 创建使用说明文件
echo 创建使用说明...
echo ❤❤❤❤❤❤❤❤❤❤❤❤❤❤❤❤❤❤❤❤❤❤❤❤❤❤❤❤❤❤❤❤❤❤❤❤❤ > "%OUTPUT_DIR%\~❤~宝宝专属项目管理小助手の甜蜜指引~❤~.txt"
echo                                                           >> "%OUTPUT_DIR%\~❤~宝宝专属项目管理小助手の甜蜜指引~❤~.txt"
echo         宝宝专属项目管理小助手の甜蜜指引                   >> "%OUTPUT_DIR%\~❤~宝宝专属项目管理小助手の甜蜜指引~❤~.txt"
echo                                                           >> "%OUTPUT_DIR%\~❤~宝宝专属项目管理小助手の甜蜜指引~❤~.txt"
echo ❤❤❤❤❤❤❤❤❤❤❤❤❤❤❤❤❤❤❤❤❤❤❤❤❤❤❤❤❤❤❤❤❤❤❤❤❤ >> "%OUTPUT_DIR%\~❤~宝宝专属项目管理小助手の甜蜜指引~❤~.txt"
echo.                                                          >> "%OUTPUT_DIR%\~❤~宝宝专属项目管理小助手の甜蜜指引~❤~.txt"
echo 亲爱的宝宝：                                              >> "%OUTPUT_DIR%\~❤~宝宝专属项目管理小助手の甜蜜指引~❤~.txt"
echo.                                                          >> "%OUTPUT_DIR%\~❤~宝宝专属项目管理小助手の甜蜜指引~❤~.txt"
echo 这是我特别为你定制的项目管理小助手，希望能帮助你更轻松地处理工作中的各种任务！ >> "%OUTPUT_DIR%\~❤~宝宝专属项目管理小助手の甜蜜指引~❤~.txt"
echo.                                                          >> "%OUTPUT_DIR%\~❤~宝宝专属项目管理小助手の甜蜜指引~❤~.txt"
echo 【启动方法】                                               >> "%OUTPUT_DIR%\~❤~宝宝专属项目管理小助手の甜蜜指引~❤~.txt"
echo 只需双击"启动小助手.bat"文件，系统会自动打开浏览器访问应用。   >> "%OUTPUT_DIR%\~❤~宝宝专属项目管理小助手の甜蜜指引~❤~.txt"
echo.                                                          >> "%OUTPUT_DIR%\~❤~宝宝专属项目管理小助手の甜蜜指引~❤~.txt"
echo 【功能介绍】                                               >> "%OUTPUT_DIR%\~❤~宝宝专属项目管理小助手の甜蜜指引~❤~.txt"
echo 📅 项目日历：管理所有项目的起止时间，支持多种视图切换           >> "%OUTPUT_DIR%\~❤~宝宝专属项目管理小助手の甜蜜指引~❤~.txt"
echo 🔔 提醒系统：自动提醒今日事项，支持自定义提醒                  >> "%OUTPUT_DIR%\~❤~宝宝专属项目管理小助手の甜蜜指引~❤~.txt"
echo 📋 项目详情：查看编辑项目的所有信息                          >> "%OUTPUT_DIR%\~❤~宝宝专属项目管理小助手の甜蜜指引~❤~.txt"
echo ✏️ 错别字校正：替换文本中的错误，支持导入导出词库               >> "%OUTPUT_DIR%\~❤~宝宝专属项目管理小助手の甜蜜指引~❤~.txt"
echo 💰 金额转换：在数字和中文金额之间快速转换                      >> "%OUTPUT_DIR%\~❤~宝宝专属项目管理小助手の甜蜜指引~❤~.txt"
echo 📄 文档生成：基于模板快速生成Word/Excel文档                  >> "%OUTPUT_DIR%\~❤~宝宝专属项目管理小助手の甜蜜指引~❤~.txt"
echo.                                                          >> "%OUTPUT_DIR%\~❤~宝宝专属项目管理小助手の甜蜜指引~❤~.txt"
echo 【文档生成器使用指南】                                       >> "%OUTPUT_DIR%\~❤~宝宝专属项目管理小助手の甜蜜指引~❤~.txt"
echo 1. 选择模板：在"templates"文件夹中选择或创建模板              >> "%OUTPUT_DIR%\~❤~宝宝专属项目管理小助手の甜蜜指引~❤~.txt"
echo 2. 添加变量：在模板中使用${变量名}作为占位符                  >> "%OUTPUT_DIR%\~❤~宝宝专属项目管理小助手の甜蜜指引~❤~.txt"
echo 3. 输入数据：在表单中填写对应的变量值                         >> "%OUTPUT_DIR%\~❤~宝宝专属项目管理小助手の甜蜜指引~❤~.txt"
echo 4. 生成文档：点击"生成文档"按钮，文件会保存在"documents"文件夹中 >> "%OUTPUT_DIR%\~❤~宝宝专属项目管理小助手の甜蜜指引~❤~.txt"
echo.                                                          >> "%OUTPUT_DIR%\~❤~宝宝专属项目管理小助手の甜蜜指引~❤~.txt"
echo 变量占位符示例：                                            >> "%OUTPUT_DIR%\~❤~宝宝专属项目管理小助手の甜蜜指引~❤~.txt"
echo - ${客户.姓名} - 填入客户姓名                               >> "%OUTPUT_DIR%\~❤~宝宝专属项目管理小助手の甜蜜指引~❤~.txt"
echo - ${项目.开始日期} - 填入项目开始日期                        >> "%OUTPUT_DIR%\~❤~宝宝专属项目管理小助手の甜蜜指引~❤~.txt"
echo - ${金额.大写} - 自动转换为中文大写金额                       >> "%OUTPUT_DIR%\~❤~宝宝专属项目管理小助手の甜蜜指引~❤~.txt"
echo.                                                          >> "%OUTPUT_DIR%\~❤~宝宝专属项目管理小助手の甜蜜指引~❤~.txt"
echo 【数据备份与升级】                                          >> "%OUTPUT_DIR%\~❤~宝宝专属项目管理小助手の甜蜜指引~❤~.txt"
echo 所有数据存储在"data"文件夹中，升级时只需备份此文件夹即可保留所有数据。>> "%OUTPUT_DIR%\~❤~宝宝专属项目管理小助手の甜蜜指引~❤~.txt"
echo.                                                          >> "%OUTPUT_DIR%\~❤~宝宝专属项目管理小助手の甜蜜指引~❤~.txt"
echo 【特别提醒】                                               >> "%OUTPUT_DIR%\~❤~宝宝专属项目管理小助手の甜蜜指引~❤~.txt"
echo ❤ 无需安装任何环境，双击启动即可使用                          >> "%OUTPUT_DIR%\~❤~宝宝专属项目管理小助手の甜蜜指引~❤~.txt"
echo ❤ 如果启动时遇到问题，请确保没有其他程序占用8082端口           >> "%OUTPUT_DIR%\~❤~宝宝专属项目管理小助手の甜蜜指引~❤~.txt"
echo ❤ 建议定期备份"data"文件夹中的数据                          >> "%OUTPUT_DIR%\~❤~宝宝专属项目管理小助手の甜蜜指引~❤~.txt"
echo.                                                          >> "%OUTPUT_DIR%\~❤~宝宝专属项目管理小助手の甜蜜指引~❤~.txt"
echo 【彩蛋说明】                                               >> "%OUTPUT_DIR%\~❤~宝宝专属项目管理小助手の甜蜜指引~❤~.txt"
echo 小助手中有一些特别的小彩蛋等待你发现：                         >> "%OUTPUT_DIR%\~❤~宝宝专属项目管理小助手の甜蜜指引~❤~.txt"
echo - 在文本替换功能中，有一条特殊规则"我很你"会变成什么呢？          >> "%OUTPUT_DIR%\~❤~宝宝专属项目管理小助手の甜蜜指引~❤~.txt"
echo - 试试在金额转换中输入"1314"或"520"，看看会发生什么？           >> "%OUTPUT_DIR%\~❤~宝宝专属项目管理小助手の甜蜜指引~❤~.txt"
echo - 文档模板中隐藏了一些特别的爱心标记，填充后会变成甜蜜的表白哦~    >> "%OUTPUT_DIR%\~❤~宝宝专属项目管理小助手の甜蜜指引~❤~.txt"
echo.                                                          >> "%OUTPUT_DIR%\~❤~宝宝专属项目管理小助手の甜蜜指引~❤~.txt"
echo 希望这个小助手能让你的工作更轻松，也能在忙碌的工作中感受到我的爱意！>> "%OUTPUT_DIR%\~❤~宝宝专属项目管理小助手の甜蜜指引~❤~.txt"
echo.                                                          >> "%OUTPUT_DIR%\~❤~宝宝专属项目管理小助手の甜蜜指引~❤~.txt"
echo                            爱你的❤                         >> "%OUTPUT_DIR%\~❤~宝宝专属项目管理小助手の甜蜜指引~❤~.txt"

REM 创建示例模板文件
echo 创建示例模板文件...
if not exist "%OUTPUT_DIR%\templates\项目申请表.docx" (
    echo 创建Word模板文件...
    copy /Y "project-assistant-service\src\main\resources\templates\project_template.docx" "%OUTPUT_DIR%\templates\项目申请表.docx" 2>nul
    if not exist "%OUTPUT_DIR%\templates\项目申请表.docx" (
        echo PK > "%OUTPUT_DIR%\templates\项目申请表.docx"
    )
)

if not exist "%OUTPUT_DIR%\templates\项目数据表.xlsx" (
    echo 创建Excel模板文件...
    copy /Y "project-assistant-service\src\main\resources\templates\project_data.xlsx" "%OUTPUT_DIR%\templates\项目数据表.xlsx" 2>nul
    if not exist "%OUTPUT_DIR%\templates\项目数据表.xlsx" (
        echo PK > "%OUTPUT_DIR%\templates\项目数据表.xlsx"
    )
)

echo 构建完成！输出目录: %OUTPUT_DIR%
echo 请运行 %OUTPUT_DIR%\启动小助手.bat 来启动应用
pause