// API配置

// 获取环境变量中配置的API地址，如果没有则根据环境使用不同的默认值
const API_URL = import.meta.env.VITE_API_URL || (import.meta.env.PROD ? '/api' : 'http://localhost:8080/api');

// 各模块API地址配置
export const API_CONFIG = {
  // 项目API
  project: {
    base: `${API_URL}/projects`,
    status: `${API_URL}/projects/status`,
    search: `${API_URL}/projects/search`,
    dateRange: `${API_URL}/projects/date-range`
  },
  
  // 金额转换API
  amountConvert: {
    toChinese: `${API_URL}/amount-convert/to-chinese`,
    toNumeric: `${API_URL}/amount-convert/to-numeric`
  },
  
  // 文本纠错API
  textCorrection: {
    correct: `${API_URL}/text-correction/correct`
  },
  
  // 文档生成API
  docGenerator: {
    generate: `${API_URL}/doc-generator/generate`
  }
};

// 全局请求超时时间（毫秒）
export const REQUEST_TIMEOUT = 30000; 