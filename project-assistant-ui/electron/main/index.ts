import { app, BrowserWindow, shell, ipcMain } from 'electron'
import { release } from 'node:os'
import { join } from 'node:path'

// 禁用Windows 7的GPU加速
if (release().startsWith('6.1')) app.disableHardwareAcceleration()

// 设置Windows 10+通知的应用名称
if (process.platform === 'win32') app.setAppUserModelId('项目管理小助手')

process.env.DIST_ELECTRON = join(__dirname, '../')
process.env.DIST = join(process.env.DIST_ELECTRON, '../dist')
process.env.PUBLIC = process.env.VITE_DEV_SERVER_URL
  ? join(process.env.DIST_ELECTRON, '../public')
  : process.env.DIST

// 这里是主窗口
function createWindow() {
  const mainWindow = new BrowserWindow({
    title: '项目管理小助手',
    icon: join(process.env.PUBLIC, 'favicon.ico'),
    width: 1280,
    height: 800,
    webPreferences: {
      preload: join(__dirname, '../preload/index.js'),
      nodeIntegration: true,
      contextIsolation: false,
    },
  })

  // 测试环境直接加载本地服务
  if (process.env.VITE_DEV_SERVER_URL) {
    mainWindow.loadURL(process.env.VITE_DEV_SERVER_URL)
    mainWindow.webContents.openDevTools()
  } else {
    // 生产环境加载打包后的文件
    mainWindow.loadFile(join(process.env.DIST, 'index.html'))
  }

  // 使用默认浏览器打开外部链接
  mainWindow.webContents.setWindowOpenHandler(({ url }) => {
    shell.openExternal(url)
    return { action: 'deny' }
  })
}

// 当Electron完成初始化时创建窗口
app.whenReady().then(createWindow)

// 关闭所有窗口时退出应用，macOS除外
app.on('window-all-closed', () => {
  if (process.platform !== 'darwin') {
    app.quit()
  }
})

app.on('activate', () => {
  // 在macOS上点击dock图标时，如果没有窗口则创建新窗口
  if (BrowserWindow.getAllWindows().length === 0) {
    createWindow()
  }
})

// 在这里可以监听来自渲染进程的IPC消息
ipcMain.on('message-from-renderer', (event, arg) => {
  console.log(arg)
  event.reply('message-from-main', 'Hello from Main Process')
}) 