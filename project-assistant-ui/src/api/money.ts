import api from './index';

// 数字金额转中文金额
export interface ChineseAmountResult {
  amount: string;
  chineseAmount: string;
}

// 中文金额转数字金额
export interface DigitalAmountResult {
  chineseAmount: string;
  amount: string;
}

// 数字转中文金额
export const toChineseAmount = (amount: string | number) => {
  return api.post<any, ChineseAmountResult>('/money/toChineseAmount', { amount });
};

// 中文金额转数字
export const toDigitalAmount = (chineseAmount: string) => {
  return api.post<any, DigitalAmountResult>('/money/toDigitalAmount', { chineseAmount });
}; 