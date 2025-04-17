<template>
  <div class="home-container">
    <el-row :gutter="20">
      <el-col :span="24">
        <el-card shadow="hover" class="welcome-card">
          <div class="welcome-content">
            <h1>欢迎使用项目管理小助手</h1>
            <p>一个用于项目管理和日期规划的应用，集成了项目管理、金额转换、文档生成和文本纠错等功能。</p>
          </div>
        </el-card>
      </el-col>
    </el-row>
    
    <el-row :gutter="20" class="mt-20">
      <el-col :span="12">
        <el-card shadow="hover" class="stats-card">
          <template #header>
            <div class="card-header">
              <h3>项目统计</h3>
            </div>
          </template>
          <div v-if="loading" class="loading-container">
            <el-skeleton :rows="4" animated />
          </div>
          <div v-else class="stats-content">
            <el-row :gutter="20">
              <el-col :span="12">
                <div class="stat-item">
                  <div class="stat-value">{{ projectStatistics.total }}</div>
                  <div class="stat-label">总项目数</div>
                </div>
              </el-col>
              <el-col :span="12">
                <div class="stat-item status-active">
                  <div class="stat-value">{{ projectStatistics.active }}</div>
                  <div class="stat-label">进行中</div>
                </div>
              </el-col>
            </el-row>
            <el-row :gutter="20" class="mt-10">
              <el-col :span="8">
                <div class="stat-item status-completed">
                  <div class="stat-value">{{ projectStatistics.completed }}</div>
                  <div class="stat-label">已完成</div>
                </div>
              </el-col>
              <el-col :span="8">
                <div class="stat-item status-cancelled">
                  <div class="stat-value">{{ projectStatistics.cancelled }}</div>
                  <div class="stat-label">已取消</div>
                </div>
              </el-col>
              <el-col :span="8">
                <div class="stat-item status-expired">
                  <div class="stat-value">{{ projectStatistics.expired }}</div>
                  <div class="stat-label">已过期</div>
                </div>
              </el-col>
            </el-row>
          </div>
        </el-card>
      </el-col>
      
      <el-col :span="12">
        <el-card shadow="hover">
          <template #header>
            <div class="card-header">
              <h3>近期项目</h3>
              <el-button type="primary" size="small" @click="$router.push('/projects')">
                查看全部
              </el-button>
            </div>
          </template>
          <div v-if="loading" class="loading-container">
            <el-skeleton :rows="5" animated />
          </div>
          <div v-else>
            <el-empty v-if="activeProjects.length === 0" description="暂无进行中的项目" />
            <el-table v-else :data="activeProjects.slice(0, 5)" style="width: 100%">
              <el-table-column prop="name" label="项目名称" />
              <el-table-column prop="onlineDate" label="上网日期" width="120" />
              <el-table-column fixed="right" label="操作" width="120">
                <template #default="scope">
                  <el-button link type="primary" @click="$router.push(`/project/edit/${scope.row.id}`)">
                    查看
                  </el-button>
                </template>
              </el-table-column>
            </el-table>
          </div>
        </el-card>
      </el-col>
    </el-row>
    
    <el-row :gutter="20" class="mt-20">
      <el-col :span="24">
        <el-card shadow="hover">
          <template #header>
            <div class="card-header">
              <h3>快捷工具</h3>
            </div>
          </template>
          <div class="tools-container">
            <el-row :gutter="20">
              <el-col :xs="24" :sm="12" :md="8">
                <el-card shadow="hover" class="tool-card" @click="$router.push('/tools/amount-convert')">
                  <el-icon class="tool-icon"><Money /></el-icon>
                  <div class="tool-title">金额转换</div>
                  <div class="tool-desc">实现数字金额与中文大写金额的互相转换</div>
                </el-card>
              </el-col>
              <el-col :xs="24" :sm="12" :md="8">
                <el-card shadow="hover" class="tool-card" @click="$router.push('/tools/doc-generator')">
                  <el-icon class="tool-icon"><Document /></el-icon>
                  <div class="tool-title">文档生成</div>
                  <div class="tool-desc">根据Word或Excel模板和数据文件生成新的文档</div>
                </el-card>
              </el-col>
              <el-col :xs="24" :sm="12" :md="8">
                <el-card shadow="hover" class="tool-card" @click="$router.push('/tools/text-corrector')">
                  <el-icon class="tool-icon"><Edit /></el-icon>
                  <div class="tool-title">文本纠错</div>
                  <div class="tool-desc">使用百度API进行文本纠错，显示纠错前后的内容对比</div>
                </el-card>
              </el-col>
            </el-row>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref, computed } from 'vue';
import { Money, Document, Edit } from '@element-plus/icons-vue';
import { useProjectStore } from '../store/project';
import type { Project, ProjectStatistics } from '../types/project';

const projectStore = useProjectStore();
const loading = ref(true);

// 项目统计信息
const projectStatistics = computed<ProjectStatistics>(() => projectStore.projectStatistics);

// 活跃项目列表
const activeProjects = computed<Project[]>(() => projectStore.activeProjects);

// 加载项目数据
onMounted(async () => {
  loading.value = true;
  try {
    await projectStore.fetchAllProjects();
  } finally {
    loading.value = false;
  }
});
</script>

<style scoped>
.home-container {
  padding: 10px;
}

.welcome-card {
  background: linear-gradient(135deg, #42b983 0%, #2f9bdb 100%);
  color: white;
}

.welcome-content {
  padding: 20px;
  text-align: center;
}

.welcome-content h1 {
  font-size: 24px;
  margin-bottom: 10px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.stats-content {
  padding: 10px 0;
}

.stat-item {
  text-align: center;
  padding: 10px;
  border-radius: 4px;
  background-color: #f5f7fa;
}

.stat-value {
  font-size: 24px;
  font-weight: bold;
}

.stat-label {
  font-size: 14px;
  color: #606266;
}

.loading-container {
  padding: 20px 0;
}

.tools-container {
  padding: 10px 0;
}

.tool-card {
  text-align: center;
  padding: 20px;
  cursor: pointer;
  transition: all 0.3s;
}

.tool-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 10px 20px rgba(0, 0, 0, 0.1);
}

.tool-icon {
  font-size: 40px;
  color: #409eff;
  margin-bottom: 15px;
}

.tool-title {
  font-size: 18px;
  font-weight: bold;
  margin-bottom: 10px;
}

.tool-desc {
  color: #606266;
  font-size: 14px;
}
</style> 