import { contextBridge, ipcRenderer } from 'electron'

contextBridge.exposeInMainWorld('electronAPI', {
  // 从渲染进程发送消息到主进程
  sendMessage: (message: string) => ipcRenderer.send('message-from-renderer', message),
  
  // 监听来自主进程的消息
  onMessageFromMain: (callback: (message: string) => void) => {
    ipcRenderer.on('message-from-main', (_event, message) => callback(message))
    return () => {
      ipcRenderer.removeAllListeners('message-from-main')
    }
  }
}) 