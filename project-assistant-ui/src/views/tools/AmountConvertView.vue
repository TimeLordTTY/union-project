<template>
  <div class="amount-convert">
    <h1>金额转换工具</h1>
    
    <el-form :model="formData" label-width="120px" v-loading="loading">
      <el-form-item label="数字金额">
        <el-input
          v-model="formData.numericAmount"
          placeholder="请输入数字金额"
          @keyup.enter="convertToChinese"
        >
          <template #append>
            <el-button @click="convertToChinese">转换为大写</el-button>
          </template>
        </el-input>
      </el-form-item>
      
      <el-form-item label="中文大写">
        <el-input
          v-model="formData.chineseAmount"
          placeholder="请输入中文大写金额"
          @keyup.enter="convertToNumeric"
        >
          <template #append>
            <el-button @click="convertToNumeric">转换为数字</el-button>
          </template>
        </el-input>
      </el-form-item>
    </el-form>
    
    <div class="tips">
      <h3>使用说明：</h3>
      <ul>
        <li>输入数字金额后点击"转换为大写"按钮或按回车，可将数字金额转换为中文大写</li>
        <li>输入中文大写金额后点击"转换为数字"按钮或按回车，可将中文大写转换为数字金额</li>
        <li>支持的格式：123.45 或 ¥123.45 或 123元45角</li>
      </ul>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import { ElMessage } from 'element-plus';
import axios from 'axios';
import { API_CONFIG } from '../../api/config';

const loading = ref(false);
const formData = ref({
  numericAmount: '',
  chineseAmount: ''
});

// 将数字金额转换为中文大写
const convertToChinese = async () => {
  if (!formData.value.numericAmount) {
    ElMessage.warning('请输入数字金额');
    return;
  }
  
  loading.value = true;
  try {
    const response = await axios.post(API_CONFIG.amountConvert.toChinese, {
      amount: formData.value.numericAmount
    });
    
    formData.value.chineseAmount = response.data;
  } catch (error) {
    console.error('转换失败:', error);
    ElMessage.error('转换失败，请检查输入格式');
  } finally {
    loading.value = false;
  }
};

// 将中文大写转换为数字金额
const convertToNumeric = async () => {
  if (!formData.value.chineseAmount) {
    ElMessage.warning('请输入中文大写金额');
    return;
  }
  
  loading.value = true;
  try {
    const response = await axios.post(API_CONFIG.amountConvert.toNumeric, {
      amount: formData.value.chineseAmount
    });
    
    formData.value.numericAmount = response.data;
  } catch (error) {
    console.error('转换失败:', error);
    ElMessage.error('转换失败，请检查输入格式');
  } finally {
    loading.value = false;
  }
};
</script>

<style scoped>
.amount-convert {
  max-width: 800px;
  margin: 0 auto;
  padding: 20px;
}

.tips {
  margin-top: 30px;
  padding: 15px;
  background-color: #f8f9fa;
  border-radius: 4px;
}

.tips h3 {
  margin-top: 0;
}

.tips ul {
  padding-left: 20px;
}
</style> 