<script setup lang="ts">
import { 
  HomeFilled, 
  Calendar, 
  Tools, 
  ArrowDown 
} from '@element-plus/icons-vue';
</script>

<template>
  <div class="app-container">
    <el-container>
      <el-aside width="220px">
        <div class="logo-container">
          <h2>项目管理小助手</h2>
        </div>
        <el-menu 
          router 
          :default-active="$route.path"
          class="el-menu-vertical"
          background-color="#304156"
          text-color="#bfcbd9"
          active-text-color="#409EFF">
          <el-menu-item index="/">
            <el-icon><HomeFilled /></el-icon>
            <template #title>首页</template>
          </el-menu-item>
          
          <el-sub-menu index="1">
            <template #title>
              <el-icon><Calendar /></el-icon>
              <span>项目管理</span>
            </template>
            <el-menu-item index="/projects">项目列表</el-menu-item>
            <el-menu-item index="/project/add">添加项目</el-menu-item>
            <el-menu-item index="/calendar">项目日历</el-menu-item>
          </el-sub-menu>
          
          <el-sub-menu index="2">
            <template #title>
              <el-icon><Tools /></el-icon>
              <span>工具箱</span>
            </template>
            <el-menu-item index="/tools/amount-convert">金额转换</el-menu-item>
            <el-menu-item index="/tools/doc-generator">文档生成</el-menu-item>
            <el-menu-item index="/tools/text-corrector">文本纠错</el-menu-item>
          </el-sub-menu>
        </el-menu>
      </el-aside>
      
      <el-container>
        <el-header>
          <div class="header-container">
            <div class="breadcrumb">
              <el-breadcrumb separator="/">
                <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
                <el-breadcrumb-item v-if="$route.meta.title">{{ $route.meta.title }}</el-breadcrumb-item>
              </el-breadcrumb>
            </div>
            <div class="user-info">
              <el-dropdown>
                <span class="dropdown-link">
                  管理员 <el-icon><ArrowDown /></el-icon>
                </span>
                <template #dropdown>
                  <el-dropdown-menu>
                    <el-dropdown-item>个人设置</el-dropdown-item>
                    <el-dropdown-item divided>退出登录</el-dropdown-item>
                  </el-dropdown-menu>
                </template>
              </el-dropdown>
            </div>
          </div>
        </el-header>
        
        <el-main>
          <router-view v-slot="{ Component }">
            <transition name="fade" mode="out-in">
              <component :is="Component" />
            </transition>
          </router-view>
        </el-main>
        
        <el-footer>
          <div class="footer-content">
            <p>项目管理小助手 &copy; 2023-2025 TimeLordTTY</p>
          </div>
        </el-footer>
      </el-container>
    </el-container>
  </div>
</template>

<style scoped>
.app-container {
  height: 100vh;
}

.el-container {
  height: 100%;
}

.el-aside {
  background-color: #304156;
  color: #fff;
  height: 100%;
  overflow-x: hidden;
}

.logo-container {
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-size: 18px;
  border-bottom: 1px solid #1f2d3d;
}

.el-menu {
  border-right: none;
}

.el-header {
  background-color: #fff;
  color: #333;
  line-height: 60px;
  box-shadow: 0 1px 4px rgba(0, 21, 41, 0.08);
}

.header-container {
  display: flex;
  justify-content: space-between;
  align-items: center;
  height: 100%;
}

.breadcrumb {
  margin-left: 10px;
}

.user-info {
  margin-right: 20px;
}

.dropdown-link {
  cursor: pointer;
  display: flex;
  align-items: center;
}

.el-main {
  background-color: #f5f7fa;
  padding: 20px;
  height: calc(100% - 120px);
  overflow-y: auto;
}

.el-footer {
  background-color: #fff;
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #909399;
  font-size: 14px;
}

.footer-content {
  text-align: center;
}

.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.2s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
</style>
