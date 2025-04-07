<template>
  <div class="app">
    <nav class="navbar">
      <div class="navbar-container">
        <router-link to="/" class="logo">
          <span class="logo-icon">📊</span>
          <span class="logo-text">项目管理小助手</span>
        </router-link>
        
        <div class="nav-links">
          <router-link to="/" class="nav-link" exact-active-class="active">首页</router-link>
          <router-link to="/money" class="nav-link" active-class="active">金额转换</router-link>
        </div>
      </div>
    </nav>
    
    <main class="main-content">
      <router-view v-slot="{ Component }">
        <transition name="fade" mode="out-in">
          <component :is="Component" />
        </transition>
      </router-view>
    </main>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import ProjectListItem from './components/ProjectListItem.vue'
import CalendarView from './components/CalendarView.vue'
import FinanceTool from './components/FinanceTool.vue'
import DocumentGenerator from './components/DocumentGenerator.vue'
import TextProcessor from './components/TextProcessor.vue'

// 激活的标签页
const activeTab = ref('')

// 模拟项目数据
const projects = ref([
  { 
    id: 1, 
    name: '测试', 
    reviewPeriod: 20, 
    onlineDate: '2025-04-02', 
    registrationPeriod: 5,
    registrationEndDate: '2025-04-11',
    expectedReviewTime: '2025-04-03 09:00',
    expertReviewTime: '2025-04-04 09:00',
    remark: ''
  },
  { 
    id: 2, 
    name: '044云容灾', 
    reviewPeriod: 20, 
    onlineDate: '2025-04-17', 
    registrationPeriod: 5,
    registrationEndDate: '2025-04-07',
    expectedReviewTime: '2025-04-17 09:00',
    expertReviewTime: '2025-04-17 09:00',
    remark: ''
  }
])

// 计算显示在底部状态栏的待处理项目信息
const pendingProjectInfo = computed(() => {
  const project = projects.value[0] // 简化处理，直接取第一个项目
  return `测试 - 报名截止: 2025-04-11 - 专家评审时间: 2025-04-04 09:00`
})
</script>

<style>
/* 全局样式 */
:root {
  --primary-color: #1e40af;
  --primary-hover: #1e3a8a;
  --secondary-color: #3b82f6;
  --text-color: #1f2937;
  --text-light: #6b7280;
  --bg-color: #f3f4f6;
  --bg-light: #ffffff;
  --border-color: #e5e7eb;
}

* {
  box-sizing: border-box;
  margin: 0;
  padding: 0;
}

body {
  font-family: 'Microsoft YaHei', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Oxygen,
    Ubuntu, Cantarell, 'Open Sans', 'Helvetica Neue', sans-serif;
  background-color: var(--bg-color);
  color: var(--text-color);
  line-height: 1.6;
}

/* App组件样式 */
.app {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

.navbar {
  background-color: var(--primary-color);
  color: white;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.navbar-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  height: 60px;
}

.logo {
  display: flex;
  align-items: center;
  text-decoration: none;
  color: white;
  font-weight: bold;
  font-size: 1.2rem;
}

.logo-icon {
  margin-right: 8px;
  font-size: 1.5rem;
}

.nav-links {
  display: flex;
  gap: 20px;
}

.nav-link {
  color: rgba(255, 255, 255, 0.8);
  text-decoration: none;
  padding: 8px 0;
  font-weight: 500;
  position: relative;
  transition: color 0.3s;
}

.nav-link:hover {
  color: white;
}

.nav-link.active {
  color: white;
}

.nav-link.active::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 0;
  width: 100%;
  height: 3px;
  background-color: white;
  border-radius: 3px 3px 0 0;
}

.main-content {
  flex: 1;
  padding: 20px;
}

/* 页面过渡动画 */
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
</style> 