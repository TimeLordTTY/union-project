const { contextBridge, ipcRenderer } = require('electron');

// 暴露安全的API到渲染进程
contextBridge.exposeInMainWorld('electronAPI', {
  // 获取用户数据路径
  getUserDataPath: () => ipcRenderer.invoke('get-user-data-path'),
  
  // 获取操作系统信息
  getOSInfo: () => {
    return {
      platform: process.platform,
      arch: process.arch
    };
  },
  
  // 打开日志目录
  openLogsDirectory: () => ipcRenderer.invoke('open-logs-directory'),
  
  // 检查后端服务状态
  checkServiceStatus: () => ipcRenderer.invoke('check-service-status'),
  
  // 重启后端服务
  restartService: () => ipcRenderer.invoke('restart-service')
});

// 注入自定义CSS以增强桌面应用体验
window.addEventListener('DOMContentLoaded', () => {
  // 添加自定义样式，使其更像桌面应用
  const style = document.createElement('style');
  style.textContent = `
    ::-webkit-scrollbar {
      width: 8px;
      height: 8px;
    }
    ::-webkit-scrollbar-track {
      background: #f1f1f1;
    }
    ::-webkit-scrollbar-thumb {
      background: #888;
      border-radius: 4px;
    }
    ::-webkit-scrollbar-thumb:hover {
      background: #555;
    }
    
    /* 应用边框样式，更像Windows应用 */
    html, body {
      overflow: hidden;
      user-select: none;
    }
    
    /* 阻止右键菜单 */
    body {
      -webkit-user-select: none;
      -webkit-app-region: no-drag;
    }
  `;
  document.head.appendChild(style);
}); 