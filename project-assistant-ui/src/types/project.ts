// 项目状态枚举
export enum ProjectStatus {
  ACTIVE = 'ACTIVE',
  COMPLETED = 'COMPLETED',
  CANCELLED = 'CANCELLED',
  EXPIRED = 'EXPIRED'
}

// 项目类型定义
export interface Project {
  id: number;
  name: string;
  reviewPeriod: number; // 评审周期（自然日）
  onlineDate: string; // 项目上网日期
  registrationPeriod: number; // 报名期限（工作日）
  registrationEndDate: string; // 报名截止日期
  earliestReviewDate: string; // 最早评审日期
  expectedReviewTime: string; // 开标时间
  expertReviewTime: string; // 专家评审时间
  remark: string; // 项目备注
  status: ProjectStatus;
}

// 项目统计信息
export interface ProjectStatistics {
  total: number;
  active: number;
  completed: number;
  cancelled: number;
  expired: number;
}

// 日期事件类型
export interface DateEvent {
  id: number;
  title: string;
  date: string;
  type: 'onlineDate' | 'registrationEndDate' | 'earliestReviewDate' | 'expectedReviewTime' | 'expertReviewTime';
  projectId: number;
  projectName: string;
} 