// 这是一个简单的Node.js启动脚本，用于启动主程序并捕获输出
const fs = require('fs');
const path = require('path');
const { spawn } = require('child_process');

// 记录日志
function log(message) {
  const timestamp = new Date().toLocaleString();
  console.log(`[${timestamp}] ${message}`);
  
  try {
    fs.appendFileSync('startup.log', `[${timestamp}] ${message}\n`);
  } catch (err) {
    console.error('无法写入日志:', err.message);
  }
}

// 确保ffmpeg相关的DLL文件存在
function checkDLLFiles() {
  const rootDir = process.cwd();
  const serviceDataDir = path.join(rootDir, 'service_data');
  
  // 检查ffmpeg.dll
  const ffmpegPath = path.join(rootDir, 'ffmpeg.dll');
  const serviceFFmpegPath = path.join(serviceDataDir, 'ffmpeg.dll');
  
  if (!fs.existsSync(ffmpegPath) && fs.existsSync(serviceFFmpegPath)) {
    try {
      log(`在根目录中未找到ffmpeg.dll，正在从service_data目录复制`);
      fs.copyFileSync(serviceFFmpegPath, ffmpegPath);
      log(`已成功复制ffmpeg.dll到根目录`);
    } catch (err) {
      log(`复制ffmpeg.dll到根目录时出错: ${err.message}`);
    }
  }
  
  // 列出必要的DLL文件
  const requiredDLLs = [
    'avcodec-61.dll',
    'avdevice-61.dll',
    'avfilter-10.dll',
    'avformat-61.dll',
    'avutil-59.dll',
    'postproc-58.dll',
    'swresample-5.dll',
    'swscale-8.dll'
  ];
  
  // 检查service_data目录中的DLL文件
  const serviceDLLs = [];
  if (fs.existsSync(serviceDataDir)) {
    try {
      const files = fs.readdirSync(serviceDataDir);
      files.forEach(file => {
        if (file.toLowerCase().endsWith('.dll')) {
          serviceDLLs.push(file);
        }
      });
      
      log(`在service_data目录中找到 ${serviceDLLs.length} 个DLL文件`);
      
      // 检查缺失的重要文件
      const missingDLLs = requiredDLLs.filter(dll => !serviceDLLs.includes(dll));
      if (missingDLLs.length > 0) {
        log(`警告: 可能缺少以下DLL文件: ${missingDLLs.join(', ')}`);
      }
    } catch (err) {
      log(`读取service_data目录出错: ${err.message}`);
    }
  } else {
    log(`警告: service_data目录不存在`);
  }
}

// 启动应用
function launchApp() {
  log('启动项目管理小助手');
  
  // 检查DLL文件
  checkDLLFiles();
  
  // 确定主程序路径
  const appPath = path.join(process.cwd(), '项目管理小助手.exe');
  
  if (!fs.existsSync(appPath)) {
    log(`错误: 找不到主程序 ${appPath}`);
    return;
  }
  
  log(`主程序路径: ${appPath}`);
  
  try {
    // 启动主程序
    const app = spawn(appPath, [], {
      stdio: 'pipe',
      detached: true,
      env: {
        ...process.env,
        PATH: `${process.env.PATH};${process.cwd()};${path.join(process.cwd(), 'service_data')}`
      }
    });
    
    log(`进程已启动，PID: ${app.pid}`);
    
    // 捕获标准输出
    app.stdout.on('data', (data) => {
      const output = data.toString().trim();
      log(`输出: ${output}`);
    });
    
    // 捕获错误输出
    app.stderr.on('data', (data) => {
      const output = data.toString().trim();
      log(`错误: ${output}`);
    });
    
    // 进程结束
    app.on('close', (code) => {
      log(`进程已结束，退出码: ${code}`);
    });
    
    // 进程错误
    app.on('error', (err) => {
      log(`启动错误: ${err.message}`);
    });
    
    // 分离进程，让主程序独立运行
    app.unref();
    
  } catch (err) {
    log(`异常: ${err.message}`);
  }
}

// 清空旧日志
try {
  fs.writeFileSync('startup.log', `=== 启动时间: ${new Date().toLocaleString()} ===\n`);
} catch (err) {
  console.error('无法创建日志文件:', err.message);
}

// 启动应用
launchApp();

// 显示启动消息
console.log('项目管理小助手启动中，请稍候...');
console.log('(启动信息已记录到startup.log文件)');

// 3秒后退出启动器
setTimeout(() => {
  process.exit(0);
}, 3000); 