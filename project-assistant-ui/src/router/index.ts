import { createRouter, createWebHistory } from 'vue-router';
import type { RouteRecordRaw } from 'vue-router';

const routes: Array<RouteRecordRaw> = [
  {
    path: '/',
    name: 'Home',
    component: () => import('../views/HomeView.vue'),
    meta: {
      title: '首页'
    }
  },
  {
    path: '/projects',
    name: 'ProjectList',
    component: () => import('../views/project/ProjectListView.vue'),
    meta: {
      title: '项目列表'
    }
  },
  {
    path: '/calendar',
    name: 'Calendar',
    component: () => import('../views/project/CalendarView.vue'),
    meta: {
      title: '项目日历'
    }
  },
  {
    path: '/project/add',
    name: 'ProjectAdd',
    component: () => import('../views/project/ProjectEditView.vue'),
    meta: {
      title: '添加项目'
    }
  },
  {
    path: '/project/edit/:id',
    name: 'ProjectEdit',
    component: () => import('../views/project/ProjectEditView.vue'),
    meta: {
      title: '编辑项目'
    }
  },
  {
    path: '/tools/amount-convert',
    name: 'AmountConvert',
    component: () => import('../views/tools/AmountConvertView.vue'),
    meta: {
      title: '金额转换'
    }
  },
  {
    path: '/tools/doc-generator',
    name: 'DocGenerator',
    component: () => import('../views/tools/DocGeneratorView.vue'),
    meta: {
      title: '文档生成'
    }
  },
  {
    path: '/tools/text-corrector',
    name: 'TextCorrector',
    component: () => import('../views/tools/TextCorrectorView.vue'),
    meta: {
      title: '文本纠错'
    }
  },
  {
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    component: () => import('../views/NotFoundView.vue'),
    meta: {
      title: '页面不存在'
    }
  }
];

const router = createRouter({
  history: createWebHistory(),
  routes
});

// 路由拦截器配置
router.beforeEach((to, _from, next) => {
  // 设置页面标题
  if (to.meta.title) {
    document.title = `${to.meta.title} - 项目管理小助手`;
  }
  next();
});

export default router; 