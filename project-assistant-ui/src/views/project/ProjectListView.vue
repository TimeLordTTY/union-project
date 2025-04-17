<template>
  <div class="project-list-container">
    <el-card shadow="never">
      <template #header>
        <div class="card-header">
          <div class="header-left">
            <h2>项目列表</h2>
          </div>
          <div class="header-right">
            <el-input
              v-model="searchKeyword"
              placeholder="搜索项目"
              clearable
              @keyup.enter="handleSearch"
              class="search-input"
            >
              <template #prefix>
                <el-icon><Search /></el-icon>
              </template>
            </el-input>
            <el-button type="primary" @click="$router.push('/project/add')">
              <el-icon><Plus /></el-icon> 添加项目
            </el-button>
          </div>
        </div>
      </template>
      
      <el-tabs v-model="activeTab" @tab-click="handleTabChange">
        <el-tab-pane label="所有项目" name="all"></el-tab-pane>
        <el-tab-pane label="进行中" name="ACTIVE"></el-tab-pane>
        <el-tab-pane label="已完成" name="COMPLETED"></el-tab-pane>
        <el-tab-pane label="已取消" name="CANCELLED"></el-tab-pane>
        <el-tab-pane label="已过期" name="EXPIRED"></el-tab-pane>
      </el-tabs>
      
      <div v-loading="loading">
        <el-table :data="filteredProjects" style="width: 100%" border>
          <el-table-column type="expand">
            <template #default="props">
              <div class="project-details">
                <p><strong>项目备注：</strong> {{ props.row.remark || '无' }}</p>
                <p><strong>上网日期：</strong> {{ formatDate(props.row.onlineDate) }}</p>
                <p><strong>报名截止日期：</strong> {{ formatDate(props.row.registrationEndDate) }}</p>
                <p><strong>最早评审日期：</strong> {{ formatDate(props.row.earliestReviewDate) }}</p>
                <p><strong>开标时间：</strong> {{ formatDateTime(props.row.expectedReviewTime) }}</p>
                <p><strong>专家评审时间：</strong> {{ formatDateTime(props.row.expertReviewTime) }}</p>
              </div>
            </template>
          </el-table-column>
          
          <el-table-column prop="id" label="ID" width="80" />
          <el-table-column prop="name" label="项目名称" min-width="200" />
          
          <el-table-column label="评审周期" width="100">
            <template #default="scope">
              {{ scope.row.reviewPeriod }} 天
            </template>
          </el-table-column>
          
          <el-table-column label="上网日期" width="120">
            <template #default="scope">
              {{ formatDate(scope.row.onlineDate) }}
            </template>
          </el-table-column>
          
          <el-table-column label="状态" width="100">
            <template #default="scope">
              <el-tag :type="getStatusType(scope.row.status)">
                {{ getStatusText(scope.row.status) }}
              </el-tag>
            </template>
          </el-table-column>
          
          <el-table-column label="操作" width="200" fixed="right">
            <template #default="scope">
              <el-button-group>
                <el-button size="small" type="primary" @click="handleEdit(scope.row)">
                  编辑
                </el-button>
                <el-button size="small" type="danger" @click="handleDelete(scope.row)">
                  删除
                </el-button>
                <el-dropdown>
                  <el-button size="small">
                    <el-icon><More /></el-icon>
                  </el-button>
                  <template #dropdown>
                    <el-dropdown-menu>
                      <el-dropdown-item @click="handleChangeStatus(scope.row, ProjectStatus.COMPLETED)">
                        标记为已完成
                      </el-dropdown-item>
                      <el-dropdown-item @click="handleChangeStatus(scope.row, ProjectStatus.CANCELLED)">
                        标记为已取消
                      </el-dropdown-item>
                      <el-dropdown-item @click="handleChangeStatus(scope.row, ProjectStatus.ACTIVE)" v-if="scope.row.status !== 'ACTIVE'">
                        恢复为进行中
                      </el-dropdown-item>
                    </el-dropdown-menu>
                  </template>
                </el-dropdown>
              </el-button-group>
            </template>
          </el-table-column>
        </el-table>
        
        <div class="pagination-container">
          <el-pagination
            v-model:current-page="currentPage"
            v-model:page-size="pageSize"
            :page-sizes="[10, 20, 50, 100]"
            layout="total, sizes, prev, pager, next, jumper"
            :total="totalProjects"
            @size-change="handleSizeChange"
            @current-change="handleCurrentChange"
          />
        </div>
      </div>
    </el-card>
    
    <!-- 删除确认对话框 -->
    <el-dialog
      v-model="deleteDialogVisible"
      title="确认删除"
      width="30%"
      :before-close="handleCloseDeleteDialog"
    >
      <div>确定要删除项目"{{ projectToDelete?.name }}"吗？此操作不可撤销！</div>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="deleteDialogVisible = false">取消</el-button>
          <el-button type="danger" @click="confirmDelete">确定</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue';
import { useRouter } from 'vue-router';
import { useProjectStore } from '../../store/project';
import { Search, Plus, More } from '@element-plus/icons-vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import type { Project } from '../../types/project';
import { ProjectStatus } from '../../types/project';

const router = useRouter();
const projectStore = useProjectStore();

// 状态变量
const loading = ref(false);
const searchKeyword = ref('');
const activeTab = ref('all');
const currentPage = ref(1);
const pageSize = ref(10);
const deleteDialogVisible = ref(false);
const projectToDelete = ref<Project | null>(null);

// 获取项目数据
const fetchProjects = async () => {
  loading.value = true;
  try {
    await projectStore.fetchAllProjects();
  } finally {
    loading.value = false;
  }
};

// 根据标签页筛选项目
const filteredByTabProjects = computed(() => {
  if (activeTab.value === 'all') {
    return projectStore.projects;
  } else {
    return projectStore.projects.filter(p => p.status === activeTab.value);
  }
});

// 根据搜索关键字进一步筛选
const filteredProjects = computed(() => {
  const keyword = searchKeyword.value.toLowerCase().trim();
  if (!keyword) return paginatedProjects.value;
  
  return paginatedProjects.value.filter(p => 
    p.name.toLowerCase().includes(keyword) || 
    (p.remark && p.remark.toLowerCase().includes(keyword))
  );
});

// 分页处理
const totalProjects = computed(() => filteredByTabProjects.value.length);

const paginatedProjects = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value;
  const end = start + pageSize.value;
  return filteredByTabProjects.value.slice(start, end);
});

// 格式化日期
const formatDate = (dateStr: string | null) => {
  if (!dateStr) return '未设置';
  return new Date(dateStr).toLocaleDateString('zh-CN');
};

// 格式化日期时间
const formatDateTime = (dateTimeStr: string | null) => {
  if (!dateTimeStr) return '未设置';
  return new Date(dateTimeStr).toLocaleString('zh-CN');
};

// 获取状态标签类型
const getStatusType = (status: ProjectStatus) => {
  switch (status) {
    case 'ACTIVE': return 'primary';
    case 'COMPLETED': return 'success';
    case 'CANCELLED': return 'danger';
    case 'EXPIRED': return 'info';
    default: return '';
  }
};

// 获取状态文本
const getStatusText = (status: ProjectStatus) => {
  switch (status) {
    case 'ACTIVE': return '进行中';
    case 'COMPLETED': return '已完成';
    case 'CANCELLED': return '已取消';
    case 'EXPIRED': return '已过期';
    default: return '未知';
  }
};

// 事件处理方法
const handleSearch = () => {
  currentPage.value = 1;
};

const handleTabChange = () => {
  currentPage.value = 1;
};

const handleSizeChange = (val: number) => {
  pageSize.value = val;
  currentPage.value = 1;
};

const handleCurrentChange = (val: number) => {
  currentPage.value = val;
};

const handleEdit = (project: Project) => {
  router.push(`/project/edit/${project.id}`);
};

const handleDelete = (project: Project) => {
  projectToDelete.value = project;
  deleteDialogVisible.value = true;
};

const handleCloseDeleteDialog = () => {
  deleteDialogVisible.value = false;
  projectToDelete.value = null;
};

const confirmDelete = async () => {
  if (!projectToDelete.value) return;
  
  try {
    const success = await projectStore.removeProject(projectToDelete.value.id);
    if (success) {
      ElMessage.success('删除成功');
      deleteDialogVisible.value = false;
      projectToDelete.value = null;
    } else {
      ElMessage.error('删除失败');
    }
  } catch (error) {
    ElMessage.error('删除失败');
    console.error('删除项目出错:', error);
  }
};

const handleChangeStatus = async (project: Project, newStatus: ProjectStatus) => {
  try {
    const updatedProject = { ...project, status: newStatus };
    await projectStore.saveProject(updatedProject);
    ElMessage.success('状态更新成功');
  } catch (error) {
    ElMessage.error('状态更新失败');
    console.error('更新项目状态出错:', error);
  }
};

// 监听搜索条件变化
watch([searchKeyword], () => {
  currentPage.value = 1;
});

// 页面加载时获取数据
onMounted(() => {
  fetchProjects();
});
</script>

<style scoped>
.project-list-container {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-left h2 {
  margin: 0;
}

.header-right {
  display: flex;
  gap: 10px;
}

.search-input {
  width: 200px;
}

.project-details {
  padding: 10px 20px;
  background-color: #f8f8f8;
  border-radius: 4px;
}

.project-details p {
  margin: 8px 0;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}
</style> 