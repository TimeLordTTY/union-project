import { createRouter, createWebHistory, RouteRecordRaw } from 'vue-router'

// 路由配置
const routes: Array<RouteRecordRaw> = [
  {
    path: '/',
    name: 'Home',
    component: () => import('../views/HomeView.vue'),
    meta: {
      title: '项目管理小助手'
    }
  },
  {
    path: '/money',
    name: 'MoneyConverter',
    component: () => import('../views/MoneyConverterView.vue'),
    meta: {
      title: '金额转换工具'
    }
  }
  // ... 其他路由配置
]

// 创建路由实例
const router = createRouter({
  history: createWebHistory(),
  routes
})

// 全局前置守卫，设置页面标题
router.beforeEach((to, from, next) => {
  // 动态设置标题
  document.title = to.meta.title as string || '项目管理小助手'
  next()
})

export default router 