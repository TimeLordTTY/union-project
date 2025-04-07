<template>
  <div class="finance-tool-container">
    <h2 class="section-title">金额转换 <span class="collapse-btn" @click="isCollapsed = !isCollapsed">{{ isCollapsed ? '展开 ▼' : '收起 ▲' }}</span></h2>
    
    <div v-show="!isCollapsed" class="finance-panels">
      <!-- 数字金额转大写 -->
      <div class="finance-panel">
        <h3 class="panel-title">▼ 数字金额转大写</h3>
        <div class="conversion-form">
          <div class="form-group">
            <label>数字金额:</label>
            <div class="input-group">
              <input 
                type="text" 
                v-model="digitalAmount" 
                placeholder="请输入数字金额, 例如: 123456.78" 
                class="form-input"
              />
              <button @click="convertToChineseAmount" class="action-btn" :disabled="isLoading">
                {{ isLoading ? '转换中...' : '转换' }}
              </button>
            </div>
          </div>
          
          <div class="form-group">
            <label>中文金额:</label>
            <div class="input-group">
              <input 
                type="text" 
                v-model="chineseAmount" 
                readonly 
                class="form-input"
              />
              <button @click="copyToClipboard(chineseAmount)" class="action-btn">复制</button>
            </div>
          </div>
        </div>
      </div>
      
      <!-- 中文金额转数字 -->
      <div class="finance-panel">
        <h3 class="panel-title">▼ 中文金额转数字</h3>
        <div class="conversion-form">
          <div class="form-group">
            <label>中文金额:</label>
            <div class="input-group">
              <input 
                type="text" 
                v-model="chineseInput" 
                placeholder="请输入中文金额, 例如: 壹拾贰万叁仟肆佰伍拾陆元柒角捌分" 
                class="form-input"
              />
              <button @click="convertToDigital" class="action-btn" :disabled="isLoading">
                {{ isLoading ? '转换中...' : '转换' }}
              </button>
            </div>
          </div>
          
          <div class="form-group">
            <label>数字金额:</label>
            <div class="input-group">
              <input 
                type="text" 
                v-model="digitalResult" 
                readonly 
                class="form-input"
              />
              <button @click="copyToClipboard(digitalResult)" class="action-btn">复制</button>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { toChineseAmount, toDigitalAmount } from '../api/money';
import { ElNotification } from 'element-plus';

// 是否折叠面板
const isCollapsed = ref(false);

// 数字转中文
const digitalAmount = ref('');
const chineseAmount = ref('');

// 中文转数字
const chineseInput = ref('');
const digitalResult = ref('');

// 加载状态
const isLoading = ref(false);

// 数字转中文金额
const convertToChineseAmount = async () => {
  if (!digitalAmount.value) {
    ElNotification({
      title: '提示',
      message: '请输入数字金额',
      type: 'warning'
    });
    return;
  }
  
  try {
    isLoading.value = true;
    const result = await toChineseAmount(digitalAmount.value);
    chineseAmount.value = result.chineseAmount;
    
    ElNotification({
      title: '成功',
      message: '转换成功',
      type: 'success'
    });
  } catch (error: any) {
    ElNotification({
      title: '错误',
      message: error.message || '转换失败，请检查输入',
      type: 'error'
    });
    chineseAmount.value = '';
  } finally {
    isLoading.value = false;
  }
};

// 中文金额转数字
const convertToDigital = async () => {
  if (!chineseInput.value) {
    ElNotification({
      title: '提示',
      message: '请输入中文金额',
      type: 'warning'
    });
    return;
  }
  
  try {
    isLoading.value = true;
    const result = await toDigitalAmount(chineseInput.value);
    digitalResult.value = result.amount;
    
    ElNotification({
      title: '成功',
      message: '转换成功',
      type: 'success'
    });
  } catch (error: any) {
    ElNotification({
      title: '错误',
      message: error.message || '转换失败，请检查输入',
      type: 'error'
    });
    digitalResult.value = '';
  } finally {
    isLoading.value = false;
  }
};

// 复制到剪贴板
const copyToClipboard = (text: string) => {
  if (!text) return;
  
  navigator.clipboard.writeText(text)
    .then(() => {
      ElNotification({
        title: '成功',
        message: '已复制到剪贴板',
        type: 'success'
      });
    })
    .catch(err => {
      console.error('复制失败:', err);
      ElNotification({
        title: '错误',
        message: '复制失败',
        type: 'error'
      });
    });
};
</script>

<style scoped>
.finance-tool-container {
  padding: 15px;
  border-radius: 8px;
  background-color: #fff;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.section-title {
  color: #d32f2f;
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
  padding-bottom: 10px;
  border-bottom: 2px solid #ffcdd2;
}

.collapse-btn {
  cursor: pointer;
  font-size: 14px;
  color: #666;
}

.finance-panels {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.finance-panel {
  background-color: #ffebee;
  border-radius: 8px;
  padding: 15px;
}

.panel-title {
  color: #d32f2f;
  margin-bottom: 15px;
  font-size: 16px;
}

.conversion-form {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 5px;
}

.form-group label {
  font-weight: bold;
  color: #333;
}

.input-group {
  display: flex;
  gap: 10px;
}

.form-input {
  flex: 1;
  padding: 8px 12px;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 14px;
}

.action-btn {
  padding: 8px 16px;
  background-color: #ef5350;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  transition: background-color 0.3s;
}

.action-btn:hover {
  background-color: #e53935;
}

.action-btn:disabled {
  background-color: #bdbdbd;
  cursor: not-allowed;
}
</style> 