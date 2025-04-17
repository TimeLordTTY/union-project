import axios from 'axios';
import type { Project, ProjectStatus } from '../types/project';

// 创建axios实例
const api = axios.create({
  baseURL: 'http://localhost:8080/api',
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json'
  }
});

// 获取所有项目
export const getAllProjects = () => {
  return api.get<Project[]>('/projects');
};

// 获取项目详情
export const getProjectById = (id: number) => {
  return api.get<Project>(`/projects/${id}`);
};

// 按状态获取项目
export const getProjectsByStatus = (status: ProjectStatus) => {
  return api.get<Project[]>(`/projects/status/${status}`);
};

// 搜索项目
export const searchProjects = (keyword: string) => {
  return api.get<Project[]>('/projects/search', {
    params: { keyword }
  });
};

// 按日期范围获取项目
export const getProjectsByDateRange = (startDate: string, endDate: string) => {
  return api.get<Project[]>('/projects/date-range', {
    params: { startDate, endDate }
  });
};

// 创建项目
export const createProject = (project: Omit<Project, 'id'>) => {
  return api.post<Project>('/projects', project);
};

// 更新项目
export const updateProject = (id: number, project: Partial<Project>) => {
  return api.put<Project>(`/projects/${id}`, project);
};

// 删除项目
export const deleteProject = (id: number) => {
  return api.delete(`/projects/${id}`);
}; 