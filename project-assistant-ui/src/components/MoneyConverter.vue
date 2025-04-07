<template>
  <div class="money-converter">
    <h2 class="title">金额转换工具</h2>
    
    <div class="converter-container">
      <div class="input-group">
        <label for="digitalAmount">数字金额:</label>
        <div class="input-wrapper">
          <input 
            id="digitalAmount" 
            v-model="digitalAmount" 
            @input="convertToChineseAmount" 
            type="text" 
            placeholder="请输入数字金额" 
            class="input-field"
          />
          <span class="currency-symbol">¥</span>
        </div>
      </div>
      
      <div class="arrows">
        <button @click="switchDirection" class="switch-btn">
          <span class="arrow up">↑</span>
          <span class="arrow down">↓</span>
        </button>
      </div>
      
      <div class="input-group">
        <label for="chineseAmount">中文金额:</label>
        <input 
          id="chineseAmount" 
          v-model="chineseAmount" 
          @input="convertToDigitalAmount" 
          type="text" 
          placeholder="请输入中文金额" 
          class="input-field"
        />
      </div>
    </div>
    
    <div class="buttons">
      <button @click="clearAll" class="clear-btn">清空</button>
      <button @click="copyToClipboard(chineseAmount)" class="copy-btn">复制中文金额</button>
      <button @click="copyToClipboard(digitalAmount)" class="copy-btn">复制数字金额</button>
    </div>
    
    <div v-if="showEasterEgg" class="easter-egg">
      <div class="heart">❤️</div>
      <p>{{ easterEggMessage }}</p>
    </div>
    
    <div class="history-section">
      <h3>转换历史</h3>
      <div class="history-list" v-if="conversionHistory.length > 0">
        <div v-for="(item, index) in conversionHistory" :key="index" class="history-item">
          <span>{{ item.digital }}</span>
          <span class="arrow-symbol">⇄</span>
          <span>{{ item.chinese }}</span>
          <button @click="removeHistoryItem(index)" class="remove-btn">×</button>
        </div>
      </div>
      <p v-else class="empty-history">暂无转换记录</p>
    </div>
  </div>
</template>

<script setup>
import { ref, watch } from 'vue';
import moneyApi from '../api/moneyApi';

// 状态变量
const digitalAmount = ref('');
const chineseAmount = ref('');
const conversionHistory = ref([]);
const showEasterEgg = ref(false);
const easterEggMessage = ref('');
const specialNumbers = {
  '520': '我爱你',
  '1314': '一生一世',
  '9999': '天长地久',
  '7777777': '七夕, 七巧节',
  '5201314': '我爱你一生一世'
};

// 转换防抖计时器
let convertTimer = null;

// 数字金额转中文金额
const convertToChineseAmount = async () => {
  if (!digitalAmount.value) {
    chineseAmount.value = '';
    return;
  }
  
  // 检查是否存在彩蛋
  checkForEasterEgg(digitalAmount.value);
  
  // 使用防抖，避免频繁API调用
  clearTimeout(convertTimer);
  convertTimer = setTimeout(async () => {
    try {
      // 格式化数字，移除非数字字符（保留小数点）
      const cleanValue = digitalAmount.value.replace(/[^\d.]/g, '');
      
      if (cleanValue === '') {
        chineseAmount.value = '';
        return;
      }
      
      // 验证数字格式
      if (!/^\d+(\.\d{0,2})?$/.test(cleanValue)) {
        chineseAmount.value = '格式有误，请输入正确的金额';
        return;
      }
      
      // 调用API进行转换
      const data = await moneyApi.toChineseAmount(cleanValue);
      chineseAmount.value = data.result;
      
      // 更新转换历史
      addToHistory(cleanValue, data.result);
    } catch (error) {
      console.error('转换出错:', error);
      chineseAmount.value = error.message || '转换失败，请重试';
      
      // 如果API调用失败，使用前端实现的简单转换作为后备
      if (digitalAmount.value) {
        chineseAmount.value = simpleFrontendConvert(digitalAmount.value);
        addToHistory(digitalAmount.value, chineseAmount.value);
      }
    }
  }, 500); // 500ms防抖
};

// 中文金额转数字金额
const convertToDigitalAmount = async () => {
  if (!chineseAmount.value) {
    digitalAmount.value = '';
    return;
  }
  
  // 使用防抖，避免频繁API调用
  clearTimeout(convertTimer);
  convertTimer = setTimeout(async () => {
    try {
      // 调用API进行转换
      const data = await moneyApi.toDigitalAmount(chineseAmount.value);
      digitalAmount.value = data.result;
      
      // 检查是否有彩蛋（根据转换后的数字）
      checkForEasterEgg(digitalAmount.value);
      
      // 更新转换历史
      addToHistory(data.result, chineseAmount.value);
    } catch (error) {
      console.error('转换出错:', error);
      digitalAmount.value = error.message || '转换失败，请重试';
    }
  }, 500); // 500ms防抖
};

// 前端简单转换函数（仅用于API调用失败时的后备方案）
const simpleFrontendConvert = (amount) => {
  const CN_NUMS = ['零', '壹', '贰', '叁', '肆', '伍', '陆', '柒', '捌', '玖'];
  const CN_UNITS = ['分', '角', '元', '拾', '佰', '仟', '万', '拾', '佰', '仟', '亿'];
  
  // 简单处理，仅支持整数部分
  if (parseFloat(amount) === 0) return '零元整';
  
  // 分割整数和小数部分
  const parts = amount.split('.');
  const intPart = parts[0];
  const decPart = parts.length > 1 ? parts[1].padEnd(2, '0').substring(0, 2) : '00';
  
  // 处理整数部分
  let result = '';
  for (let i = 0; i < intPart.length; i++) {
    const digit = parseInt(intPart[i]);
    const unit = CN_UNITS[intPart.length - i + 1];
    if (digit !== 0) {
      result += CN_NUMS[digit] + unit;
    }
  }
  
  // 添加"元"
  result += '元';
  
  // 处理小数部分
  if (decPart[0] === '0' && decPart[1] === '0') {
    result += '整';
  } else {
    if (decPart[0] !== '0') {
      result += CN_NUMS[parseInt(decPart[0])] + '角';
    }
    if (decPart[1] !== '0') {
      result += CN_NUMS[parseInt(decPart[1])] + '分';
    }
  }
  
  return result;
};

// 切换转换方向
const switchDirection = () => {
  const tempDigital = digitalAmount.value;
  const tempChinese = chineseAmount.value;
  
  // 清空输入
  digitalAmount.value = '';
  chineseAmount.value = '';
  
  // 延迟切换，以避免输入框切换时的闪烁
  setTimeout(() => {
    if (tempDigital && !tempChinese) {
      // 如果只有数字金额，则将其设置为中文金额输入框
      chineseAmount.value = tempDigital;
      convertToDigitalAmount();
    } else if (!tempDigital && tempChinese) {
      // 如果只有中文金额，则将其设置为数字金额输入框
      digitalAmount.value = tempChinese;
      convertToChineseAmount();
    } else if (tempDigital && tempChinese) {
      // 如果两者都有值，交换它们的位置
      digitalAmount.value = tempChinese;
      chineseAmount.value = tempDigital;
    }
  }, 100);
};

// 清空所有输入
const clearAll = () => {
  digitalAmount.value = '';
  chineseAmount.value = '';
  showEasterEgg.value = false;
};

// 复制到剪贴板
const copyToClipboard = (text) => {
  if (!text) return;
  
  navigator.clipboard.writeText(text)
    .then(() => {
      // 使用更友好的提示方式而不是alert
      const message = document.createElement('div');
      message.textContent = '已复制到剪贴板';
      message.style.position = 'fixed';
      message.style.top = '10%';
      message.style.left = '50%';
      message.style.transform = 'translateX(-50%)';
      message.style.padding = '10px 20px';
      message.style.backgroundColor = 'rgba(0, 0, 0, 0.7)';
      message.style.color = 'white';
      message.style.borderRadius = '4px';
      message.style.zIndex = '9999';
      document.body.appendChild(message);
      
      // 2秒后移除提示
      setTimeout(() => {
        document.body.removeChild(message);
      }, 2000);
    })
    .catch(err => {
      console.error('复制失败:', err);
      alert('复制失败，请手动复制。');
    });
};

// 添加到历史记录
const addToHistory = (digital, chinese) => {
  // 检查是否已存在相同的记录
  const exists = conversionHistory.value.some(
    item => item.digital === digital && item.chinese === chinese
  );
  
  if (!exists) {
    // 限制历史记录数量为10条
    if (conversionHistory.value.length >= 10) {
      conversionHistory.value.pop();
    }
    
    // 添加新记录到开头
    conversionHistory.value.unshift({ digital, chinese });
    
    // 保存到localStorage
    try {
      localStorage.setItem('moneyConverterHistory', JSON.stringify(conversionHistory.value));
    } catch (e) {
      console.error('保存历史记录失败:', e);
    }
  }
};

// 删除历史记录
const removeHistoryItem = (index) => {
  conversionHistory.value.splice(index, 1);
  
  // 更新localStorage
  try {
    localStorage.setItem('moneyConverterHistory', JSON.stringify(conversionHistory.value));
  } catch (e) {
    console.error('保存历史记录失败:', e);
  }
};

// 检查特殊数字彩蛋
const checkForEasterEgg = (value) => {
  const cleanValue = value.replace(/[^\d]/g, '');
  
  if (specialNumbers[cleanValue]) {
    showEasterEgg.value = true;
    easterEggMessage.value = specialNumbers[cleanValue];
    setTimeout(() => {
      showEasterEgg.value = false;
    }, 3000);
  } else {
    showEasterEgg.value = false;
  }
};

// 从localStorage加载历史记录
const loadHistoryFromStorage = () => {
  try {
    const savedHistory = localStorage.getItem('moneyConverterHistory');
    if (savedHistory) {
      conversionHistory.value = JSON.parse(savedHistory);
    }
  } catch (e) {
    console.error('加载历史记录失败:', e);
  }
};

// 监听数字金额变化
watch(digitalAmount, (newValue) => {
  if (!newValue) {
    chineseAmount.value = '';
  }
});

// 监听中文金额变化
watch(chineseAmount, (newValue) => {
  if (!newValue) {
    digitalAmount.value = '';
  }
});

// 组件初始化时加载历史记录
loadHistoryFromStorage();
</script>

<style scoped>
.money-converter {
  max-width: 800px;
  margin: 0 auto;
  padding: 20px;
  background-color: #fff;
  border-radius: 10px;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.1);
}

.title {
  text-align: center;
  color: #1e40af;
  margin-bottom: 20px;
  font-size: 1.8rem;
}

.converter-container {
  display: flex;
  flex-direction: column;
  gap: 16px;
  margin-bottom: 20px;
}

.input-group {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.input-group label {
  font-weight: 600;
  color: #374151;
}

.input-wrapper {
  position: relative;
  width: 100%;
}

.currency-symbol {
  position: absolute;
  left: 12px;
  top: 50%;
  transform: translateY(-50%);
  color: #6b7280;
}

.input-field {
  width: 100%;
  padding: 12px;
  padding-left: 28px;
  border: 1px solid #d1d5db;
  border-radius: 6px;
  font-size: 1rem;
  transition: border-color 0.3s;
  box-sizing: border-box;
}

.input-field:focus {
  outline: none;
  border-color: #3b82f6;
  box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.25);
}

.input-field::placeholder {
  color: #9ca3af;
}

.arrows {
  display: flex;
  justify-content: center;
  margin: 6px 0;
}

.switch-btn {
  background-color: #f3f4f6;
  border: 1px solid #e5e7eb;
  border-radius: 50%;
  width: 40px;
  height: 40px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.3s;
}

.switch-btn:hover {
  background-color: #e5e7eb;
}

.arrow {
  font-size: 14px;
  line-height: 1;
  color: #6b7280;
}

.buttons {
  display: flex;
  gap: 10px;
  margin-bottom: 20px;
}

.clear-btn, .copy-btn {
  padding: 10px 15px;
  border: none;
  border-radius: 6px;
  font-weight: 500;
  cursor: pointer;
  transition: background-color 0.3s;
}

.clear-btn {
  background-color: #f3f4f6;
  color: #4b5563;
  flex: 1;
}

.clear-btn:hover {
  background-color: #e5e7eb;
}

.copy-btn {
  background-color: #3b82f6;
  color: white;
  flex: 2;
}

.copy-btn:hover {
  background-color: #2563eb;
}

.easter-egg {
  text-align: center;
  margin: 20px 0;
  padding: 15px;
  background-color: #fef2f2;
  border-radius: 8px;
  animation: fadeIn 0.5s ease-in-out;
  position: relative;
}

.heart {
  font-size: 24px;
  animation: heartbeat 1.5s infinite;
  display: inline-block;
  margin-right: 8px;
}

.easter-egg p {
  margin: 10px 0 0;
  font-weight: 600;
  color: #ef4444;
}

.history-section {
  margin-top: 30px;
  border-top: 1px solid #e5e7eb;
  padding-top: 20px;
}

.history-section h3 {
  margin-bottom: 15px;
  color: #374151;
  font-size: 1.2rem;
}

.history-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
  max-height: 250px;
  overflow-y: auto;
}

.history-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 10px;
  background-color: #f9fafb;
  border-radius: 6px;
  font-size: 0.9rem;
}

.arrow-symbol {
  color: #6b7280;
  margin: 0 8px;
}

.remove-btn {
  background: none;
  border: none;
  color: #9ca3af;
  cursor: pointer;
  font-size: 1.2rem;
  padding: 0 5px;
}

.remove-btn:hover {
  color: #ef4444;
}

.empty-history {
  text-align: center;
  color: #9ca3af;
  padding: 15px;
}

@keyframes fadeIn {
  from { opacity: 0; transform: translateY(-10px); }
  to { opacity: 1; transform: translateY(0); }
}

@keyframes heartbeat {
  0% { transform: scale(1); }
  25% { transform: scale(1.1); }
  50% { transform: scale(1); }
  75% { transform: scale(1.1); }
  100% { transform: scale(1); }
}

@media (min-width: 640px) {
  .converter-container {
    flex-direction: row;
    align-items: center;
  }
  
  .input-group {
    flex: 1;
  }
  
  .arrows {
    margin: 0 15px;
  }
}
</style> 