import axios from 'axios';

/**
 * 金额转换相关API
 */
const moneyApi = {
  /**
   * 将数字金额转换为中文金额
   * @param {number|string} amount - 数字金额
   * @returns {Promise<{result: string}>} - 中文金额
   */
  toChineseAmount: async (amount) => {
    try {
      const response = await axios.post('/api/money/toChineseAmount', { amount });
      return response.data.data;
    } catch (error) {
      console.error('转换数字金额失败:', error);
      // 如果服务器返回了错误信息，使用它
      if (error.response && error.response.data && error.response.data.message) {
        throw new Error(error.response.data.message);
      }
      // 否则使用通用错误信息
      throw new Error('数字金额转换失败，请检查输入格式或网络连接');
    }
  },

  /**
   * 将中文金额转换为数字金额
   * @param {string} chineseAmount - 中文金额
   * @returns {Promise<{result: string}>} - 数字金额
   */
  toDigitalAmount: async (chineseAmount) => {
    try {
      const response = await axios.post('/api/money/toDigitalAmount', { chineseAmount });
      return response.data.data;
    } catch (error) {
      console.error('转换中文金额失败:', error);
      // 如果服务器返回了错误信息，使用它
      if (error.response && error.response.data && error.response.data.message) {
        throw new Error(error.response.data.message);
      }
      // 否则使用通用错误信息
      throw new Error('中文金额转换失败，请检查输入格式或网络连接');
    }
  }
};

export default moneyApi; 