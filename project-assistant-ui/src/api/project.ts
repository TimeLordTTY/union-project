import api from './index';

// 项目数据接口
export interface Project {
  id?: number;
  name: string;
  reviewPeriod?: number;
  onlineDate?: string;
  registrationPeriod?: number;
  registrationEndDate?: string;
  expectedReviewTime?: string;
  expertReviewTime?: string;
  remark?: string;
  createTime?: string;
  updateTime?: string;
}

// 获取所有项目
export const getAllProjects = () => {
  return api.get<any, Project[]>('/projects');
};

// 获取项目详情
export const getProjectById = (id: number) => {
  return api.get<any, Project>(`/projects/${id}`);
};

// 创建项目
export const createProject = (project: Project) => {
  return api.post<any, Project>('/projects', project);
};

// 更新项目
export const updateProject = (id: number, project: Project) => {
  return api.put<any, Project>(`/projects/${id}`, project);
};

// 删除项目
export const deleteProject = (id: number) => {
  return api.delete<any, { deleted: boolean }>(`/projects/${id}`);
};