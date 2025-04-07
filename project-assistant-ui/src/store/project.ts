import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { Project, getAllProjects, getProjectById, createProject, updateProject, deleteProject } from '../api/project'

export const useProjectStore = defineStore('project', () => {
  // 状态
  const projects = ref<Project[]>([])
  const currentProject = ref<Project | null>(null)
  const loading = ref(false)
  const error = ref('')

  // 计算属性
  const projectCount = computed(() => projects.value.length)
  const sortedProjects = computed(() => {
    return [...projects.value].sort((a, b) => {
      // 根据创建时间降序排序
      const dateA = a.createTime ? new Date(a.createTime).getTime() : 0
      const dateB = b.createTime ? new Date(b.createTime).getTime() : 0
      return dateB - dateA
    })
  })

  // 方法
  // 加载所有项目
  const fetchProjects = async () => {
    loading.value = true
    error.value = ''
    try {
      projects.value = await getAllProjects()
    } catch (err: any) {
      error.value = err.message || '加载项目失败'
      console.error('获取项目失败:', err)
    } finally {
      loading.value = false
    }
  }

  // 获取单个项目
  const fetchProject = async (id: number) => {
    loading.value = true
    error.value = ''
    try {
      currentProject.value = await getProjectById(id)
    } catch (err: any) {
      error.value = err.message || '加载项目详情失败'
      console.error('获取项目详情失败:', err)
    } finally {
      loading.value = false
    }
  }

  // 新增项目
  const addProject = async (project: Project) => {
    loading.value = true
    error.value = ''
    try {
      const newProject = await createProject(project)
      projects.value.push(newProject)
      return newProject
    } catch (err: any) {
      error.value = err.message || '创建项目失败'
      console.error('创建项目失败:', err)
      return null
    } finally {
      loading.value = false
    }
  }

  // 更新项目
  const editProject = async (id: number, project: Project) => {
    loading.value = true
    error.value = ''
    try {
      const updatedProject = await updateProject(id, project)
      const index = projects.value.findIndex(p => p.id === id)
      if (index !== -1) {
        projects.value[index] = updatedProject
      }
      return updatedProject
    } catch (err: any) {
      error.value = err.message || '更新项目失败'
      console.error('更新项目失败:', err)
      return null
    } finally {
      loading.value = false
    }
  }

  // 删除项目
  const removeProject = async (id: number) => {
    loading.value = true
    error.value = ''
    try {
      const result = await deleteProject(id)
      if (result.deleted) {
        projects.value = projects.value.filter(p => p.id !== id)
        return true
      }
      return false
    } catch (err: any) {
      error.value = err.message || '删除项目失败'
      console.error('删除项目失败:', err)
      return false
    } finally {
      loading.value = false
    }
  }

  return {
    // 状态
    projects,
    currentProject,
    loading,
    error,
    
    // 计算属性
    projectCount,
    sortedProjects,
    
    // 方法
    fetchProjects,
    fetchProject,
    addProject,
    editProject,
    removeProject
  }
}) 