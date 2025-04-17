<template>
  <div class="project-edit">
    <h1>{{ isEdit ? '编辑项目' : '添加项目' }}</h1>
    
    <el-form :model="projectForm" label-width="120px" v-loading="loading">
      <el-form-item label="项目名称">
        <el-input v-model="projectForm.name" placeholder="请输入项目名称" />
      </el-form-item>
      
      <el-form-item label="评审周期">
        <el-input-number v-model="projectForm.reviewPeriod" :min="1" placeholder="自然日" />
      </el-form-item>
      
      <el-form-item label="上网日期">
        <el-date-picker 
          v-model="projectForm.onlineDate" 
          type="datetime" 
          placeholder="选择日期和时间" 
          format="YYYY-MM-DD HH:mm:ss"
          value-format="YYYY-MM-DD HH:mm:ss"
        />
      </el-form-item>
      
      <el-form-item label="报名期限">
        <el-input-number v-model="projectForm.registrationPeriod" :min="1" placeholder="工作日" />
      </el-form-item>
      
      <el-form-item label="项目状态">
        <el-select v-model="projectForm.status">
          <el-option 
            v-for="(value, key) in ProjectStatus" 
            :key="key" 
            :label="value" 
            :value="key" 
          />
        </el-select>
      </el-form-item>
      
      <el-form-item label="备注">
        <el-input 
          v-model="projectForm.remark" 
          type="textarea" 
          rows="3" 
          placeholder="请输入备注信息"
        />
      </el-form-item>
      
      <el-form-item>
        <el-button type="primary" @click="saveProject">保存</el-button>
        <el-button @click="$router.back()">取消</el-button>
      </el-form-item>
    </el-form>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { useProjectStore } from '../../store/project';
import { ProjectStatus } from '../../types/project';
import type { Project } from '../../types/project';
import { ElMessage } from 'element-plus';

const route = useRoute();
const router = useRouter();
const projectStore = useProjectStore();
const loading = ref(false);

// 是否为编辑模式
const isEdit = computed(() => route.params.id !== undefined);

// 项目表单数据
const projectForm = ref<Partial<Project>>({
  name: '',
  reviewPeriod: 30,
  onlineDate: new Date().toISOString().slice(0, 16),
  registrationPeriod: 10,
  registrationEndDate: '',
  earliestReviewDate: '',
  expectedReviewTime: '',
  expertReviewTime: '',
  remark: '',
  status: ProjectStatus.ACTIVE
});

// 加载项目数据
const loadProject = async (id: number) => {
  loading.value = true;
  try {
    const project = await projectStore.fetchProjectById(id);
    if (project) {
      projectForm.value = { ...project };
    } else {
      ElMessage.error('找不到项目数据');
      router.push('/projects');
    }
  } catch (error) {
    console.error('加载项目数据失败:', error);
    ElMessage.error('加载项目数据失败');
  } finally {
    loading.value = false;
  }
};

// 保存项目
const saveProject = async () => {
  loading.value = true;
  try {
    await projectStore.saveProject(projectForm.value);
    ElMessage.success('保存成功');
    router.push('/projects');
  } catch (error) {
    console.error('保存项目失败:', error);
    ElMessage.error('保存项目失败');
  } finally {
    loading.value = false;
  }
};

// 组件加载时，如果是编辑模式则加载项目数据
onMounted(() => {
  if (isEdit.value && route.params.id) {
    loadProject(Number(route.params.id));
  }
});
</script>

<style scoped>
.project-edit {
  padding: 20px;
  max-width: 800px;
  margin: 0 auto;
}
</style> 