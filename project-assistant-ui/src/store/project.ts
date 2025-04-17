import { defineStore } from 'pinia';
import { ref, computed } from 'vue';
import type { Project } from '../types/project';
import { ProjectStatus } from '../types/project';
import * as projectApi from '../api/project';

export const useProjectStore = defineStore('project', () => {
  // 状态
  const projects = ref<Project[]>([]);
  const loading = ref(false);
  const error = ref<string | null>(null);
  const currentProject = ref<Project | null>(null);

  // 计算属性
  const activeProjects = computed(() => 
    projects.value.filter(p => p.status === ProjectStatus.ACTIVE)
  );
  
  const completedProjects = computed(() => 
    projects.value.filter(p => p.status === ProjectStatus.COMPLETED)
  );
  
  const cancelledProjects = computed(() => 
    projects.value.filter(p => p.status === ProjectStatus.CANCELLED)
  );
  
  const expiredProjects = computed(() => 
    projects.value.filter(p => p.status === ProjectStatus.EXPIRED)
  );
  
  const projectStatistics = computed(() => ({
    total: projects.value.length,
    active: activeProjects.value.length,
    completed: completedProjects.value.length,
    cancelled: cancelledProjects.value.length,
    expired: expiredProjects.value.length
  }));

  // 方法
  const fetchAllProjects = async () => {
    loading.value = true;
    error.value = null;
    
    try {
      const response = await projectApi.getAllProjects();
      projects.value = response.data;
    } catch (err) {
      console.error('获取项目失败:', err);
      error.value = '获取项目失败，请稍后再试';
    } finally {
      loading.value = false;
    }
  };
  
  const fetchProjectById = async (id: number) => {
    loading.value = true;
    error.value = null;
    
    try {
      const response = await projectApi.getProjectById(id);
      currentProject.value = response.data;
      return response.data;
    } catch (err) {
      console.error(`获取项目ID=${id}失败:`, err);
      error.value = '获取项目详情失败，请稍后再试';
      return null;
    } finally {
      loading.value = false;
    }
  };
  
  const fetchProjectsByStatus = async (status: ProjectStatus) => {
    loading.value = true;
    error.value = null;
    
    try {
      const response = await projectApi.getProjectsByStatus(status);
      return response.data;
    } catch (err) {
      console.error(`获取${status}状态的项目失败:`, err);
      error.value = '获取项目失败，请稍后再试';
      return [];
    } finally {
      loading.value = false;
    }
  };
  
  const searchProjects = async (keyword: string) => {
    loading.value = true;
    error.value = null;
    
    try {
      const response = await projectApi.searchProjects(keyword);
      return response.data;
    } catch (err) {
      console.error('搜索项目失败:', err);
      error.value = '搜索项目失败，请稍后再试';
      return [];
    } finally {
      loading.value = false;
    }
  };

  const saveProject = async (project: Partial<Project>) => {
    loading.value = true;
    error.value = null;
    
    try {
      let response;
      
      if (project.id) {
        // 更新项目
        response = await projectApi.updateProject(project.id, project);
      } else {
        // 创建新项目
        response = await projectApi.createProject(project as Omit<Project, 'id'>);
      }
      
      // 更新本地的项目列表
      await fetchAllProjects();
      
      return response.data;
    } catch (err) {
      console.error('保存项目失败:', err);
      error.value = '保存项目失败，请稍后再试';
      return null;
    } finally {
      loading.value = false;
    }
  };
  
  const removeProject = async (id: number) => {
    loading.value = true;
    error.value = null;
    
    try {
      await projectApi.deleteProject(id);
      
      // 从本地列表中移除
      projects.value = projects.value.filter(p => p.id !== id);
      
      return true;
    } catch (err) {
      console.error(`删除项目ID=${id}失败:`, err);
      error.value = '删除项目失败，请稍后再试';
      return false;
    } finally {
      loading.value = false;
    }
  };

  return {
    projects,
    loading,
    error,
    currentProject,
    activeProjects,
    completedProjects,
    cancelledProjects,
    expiredProjects,
    projectStatistics,
    fetchAllProjects,
    fetchProjectById,
    fetchProjectsByStatus,
    searchProjects,
    saveProject,
    removeProject
  };
}); 