const { app, BrowserWindow } = require('electron');
const path = require('path');
const fs = require('fs');
const { spawn } = require('child_process');

// 日志文件路径
const LOG_FILE = path.join(process.cwd(), 'debug.log');

// 清空日志文件
try {
  fs.writeFileSync(LOG_FILE, '=== 应用启动 ===\n', { encoding: 'utf8' });
} catch (e) {
  // 忽略错误
}

// 简单日志函数
function log(message) {
  const time = new Date().toLocaleString();
  const logMessage = `[${time}] ${message}\n`;
  
  try {
    fs.appendFileSync(LOG_FILE, logMessage, { encoding: 'utf8' });
    console.log(message);
  } catch (e) {
    // 忽略错误
  }
}

// 记录基本信息
log(`应用路径: ${app.getPath('exe')}`);
log(`工作目录: ${process.cwd()}`);

// 设置应用根目录
const appRoot = path.dirname(app.getPath('exe'));
log(`应用根目录: ${appRoot}`);

// 关键路径
const serviceDataDir = path.join(appRoot, 'service_data');
const jreDir = path.join(serviceDataDir, 'jre');
const dataDir = path.join(appRoot, 'data');
const serviceDir = path.join(serviceDataDir, 'service');
const confDir = path.join(serviceDir, 'conf');
const logsDir = path.join(serviceDataDir, 'logs');

// 关键文件
const javaExe = path.join(jreDir, 'bin', 'java.exe');
const jarFile = path.join(serviceDir, 'project-assistant-service-1.0.0.jar');
const configFile = path.join(confDir, 'application.yml');

// 记录路径信息
log(`service_data目录: ${serviceDataDir}`);
log(`jre目录: ${jreDir}`);
log(`data目录: ${dataDir}`);
log(`服务目录: ${serviceDir}`);
log(`日志目录: ${logsDir}`);
log(`Java可执行文件: ${javaExe}`);
log(`JAR文件: ${jarFile}`);

// 创建目录
function ensureDir(dir) {
  try {
    if (!fs.existsSync(dir)) {
      fs.mkdirSync(dir, { recursive: true });
      log(`创建目录: ${dir}`);
    }
  } catch (e) {
    log(`创建目录失败: ${dir} - ${e.message}`);
  }
}

// 创建必要的目录
ensureDir(serviceDataDir);
ensureDir(dataDir);
ensureDir(logsDir);

// 全局变量
let mainWindow = null;
let javaProcess = null;

// 启动后端服务
async function startJavaService() {
  log('准备启动Java服务...');
  
  // 检查Java可执行文件
  if (!fs.existsSync(javaExe)) {
    log(`错误: Java可执行文件不存在: ${javaExe}`);
    return false;
  }
  
  // 检查JAR文件
  if (!fs.existsSync(jarFile)) {
    log(`错误: JAR文件不存在: ${jarFile}`);
    return false;
  }
  
  // 检查配置文件
  if (!fs.existsSync(configFile)) {
    log(`错误: 配置文件不存在: ${configFile}`);
    return false;
  }
  
  // 日志文件路径
  const logFile = path.join(logsDir, 'service.log');
  // 数据库路径
  const dbPath = path.join(dataDir, 'projectdb');
  
  // Java启动参数
  const javaArgs = [
    '-Xms256m',
    '-Xmx512m',
    '-jar',
    jarFile,
    `--spring.config.location=file:${configFile}`,
    `--spring.datasource.url=jdbc:h2:file:${dbPath};AUTO_SERVER=TRUE`,
    `--app.data.dir=${dataDir}`,
    `--logging.file.name=${logFile}`,
    `--server.port=8080`
  ];
  
  log(`Java命令: ${javaExe} ${javaArgs.join(' ')}`);
  
  try {
    // 启动Java进程
    javaProcess = spawn(javaExe, javaArgs, {
      windowsHide: false
    });
    
    if (javaProcess && javaProcess.pid) {
      log(`Java进程已启动，PID: ${javaProcess.pid}`);
      
      // 等待3秒以确保服务启动
      await new Promise(resolve => setTimeout(resolve, 3000));
      log('后端服务启动等待完成');
      return true;
    } else {
      log('无法获取Java进程PID');
      return false;
    }
  } catch (e) {
    log(`启动Java进程失败: ${e.message}`);
    return false;
  }
}

// 创建主窗口
function createWindow() {
  log('创建主窗口...');
  
  try {
    // 创建浏览器窗口
    mainWindow = new BrowserWindow({
      width: 1200,
      height: 800,
      show: false, // 先不显示
      webPreferences: {
        nodeIntegration: false,
        contextIsolation: true,
        preload: path.join(__dirname, 'preload.js')
      }
    });
    
    // 设置URL
    const url = 'http://localhost:8080/api/';
    log(`加载URL: ${url}`);
    
    // 加载URL
    mainWindow.loadURL(url);
    
    // 窗口准备好时显示
    mainWindow.once('ready-to-show', () => {
      log('窗口准备好显示');
      mainWindow.show();
    });
    
    // 窗口关闭时清理
    mainWindow.on('closed', () => {
      log('主窗口已关闭');
      mainWindow = null;
      
      // 关闭Java进程
      if (javaProcess) {
        log('关闭Java进程');
        javaProcess.kill();
        javaProcess = null;
      }
      
      app.quit();
    });
    
    return true;
  } catch (e) {
    log(`创建窗口失败: ${e.message}`);
    return false;
  }
}

// 处理未捕获的异常
process.on('uncaughtException', (error) => {
  log(`未捕获的异常: ${error.message}`);
  log(error.stack || 'No stack trace available');
});

// 应用准备好时
app.whenReady().then(async () => {
  log('应用准备就绪');
  
  // 启动后端服务
  const serviceStarted = await startJavaService();
  log(`后端服务启动: ${serviceStarted ? '成功' : '失败'}`);
  
  // 创建主窗口
  createWindow();
}).catch(e => {
  log(`应用启动过程出错: ${e.message}`);
  log(e.stack || 'No stack trace available');
});

// 所有窗口关闭时退出
app.on('window-all-closed', () => {
  log('所有窗口已关闭');
  app.quit();
}); 