<template>
  <div class="text-corrector">
    <h1>文本纠错工具</h1>
    
    <el-form :model="formData" label-width="120px" v-loading="loading">
      <el-form-item label="原始文本">
        <el-input
          v-model="formData.originalText"
          type="textarea"
          :rows="8"
          placeholder="请输入需要纠错的文本"
        />
      </el-form-item>
      
      <el-form-item>
        <el-button type="primary" @click="correctText" :disabled="!formData.originalText">
          纠错
        </el-button>
        <el-button @click="clearText">清空</el-button>
      </el-form-item>
      
      <el-form-item label="纠错结果" v-if="formData.correctedText">
        <div class="correction-result">
          <div class="text-diff">
            <div class="original">
              <h4>原文</h4>
              <div v-html="highlightOriginal" class="text-content"></div>
            </div>
            <div class="corrected">
              <h4>纠错后</h4>
              <div v-html="highlightCorrected" class="text-content"></div>
            </div>
          </div>
          <div class="correction-stats">
            <p>纠错数量: {{ diffCount }}</p>
          </div>
        </div>
      </el-form-item>
    </el-form>
    
    <div class="tips">
      <h3>使用说明：</h3>
      <ul>
        <li>输入需要纠错的文本，点击"纠错"按钮进行文本纠错</li>
        <li>系统会自动检测并修正文本中的错别字、病句等问题</li>
        <li>纠错结果将以对比形式展示，方便查看修改前后的差异</li>
      </ul>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue';
import { ElMessage } from 'element-plus';
import axios from 'axios';
import { API_CONFIG } from '../../api/config';

const loading = ref(false);
const formData = ref({
  originalText: '',
  correctedText: ''
});

// 获取差异数量
const diffCount = computed(() => {
  if (!formData.value.originalText || !formData.value.correctedText) {
    return 0;
  }
  
  // 简单统计不同的字符数量
  let count = 0;
  const original = formData.value.originalText;
  const corrected = formData.value.correctedText;
  const maxLength = Math.max(original.length, corrected.length);
  
  for (let i = 0; i < maxLength; i++) {
    if (original[i] !== corrected[i]) {
      count++;
    }
  }
  
  return count;
});

// 高亮原文中的错误
const highlightOriginal = computed(() => {
  if (!formData.value.originalText || !formData.value.correctedText) {
    return '';
  }
  
  // 使用简单的字符对比
  let html = '';
  const original = formData.value.originalText;
  const corrected = formData.value.correctedText;
  const maxLength = Math.max(original.length, corrected.length);
  
  for (let i = 0; i < maxLength; i++) {
    if (i >= original.length) {
      break;
    }
    
    if (i < corrected.length && original[i] !== corrected[i]) {
      html += `<span class="diff-highlight">${original[i] || ''}</span>`;
    } else {
      html += original[i] || '';
    }
  }
  
  return html;
});

// 高亮纠错后的文本
const highlightCorrected = computed(() => {
  if (!formData.value.originalText || !formData.value.correctedText) {
    return '';
  }
  
  // 使用简单的字符对比
  let html = '';
  const original = formData.value.originalText;
  const corrected = formData.value.correctedText;
  const maxLength = Math.max(original.length, corrected.length);
  
  for (let i = 0; i < maxLength; i++) {
    if (i >= corrected.length) {
      break;
    }
    
    if (i < original.length && original[i] !== corrected[i]) {
      html += `<span class="diff-highlight">${corrected[i] || ''}</span>`;
    } else {
      html += corrected[i] || '';
    }
  }
  
  return html;
});

// 执行纠错
const correctText = async () => {
  if (!formData.value.originalText) {
    ElMessage.warning('请输入需要纠错的文本');
    return;
  }
  
  loading.value = true;
  try {
    const response = await axios.post(API_CONFIG.textCorrection.correct, {
      text: formData.value.originalText
    });
    
    formData.value.correctedText = response.data.correctedText || formData.value.originalText;
    
    if (formData.value.correctedText === formData.value.originalText) {
      ElMessage.info('文本没有需要纠正的错误');
    } else {
      ElMessage.success('文本纠错完成');
    }
  } catch (error) {
    console.error('纠错失败:', error);
    ElMessage.error('纠错失败，请稍后重试');
  } finally {
    loading.value = false;
  }
};

// 清空文本
const clearText = () => {
  formData.value.originalText = '';
  formData.value.correctedText = '';
};
</script>

<style scoped>
.text-corrector {
  max-width: 800px;
  margin: 0 auto;
  padding: 20px;
}

.correction-result {
  background-color: #f5f7fa;
  border-radius: 4px;
  padding: 15px;
}

.text-diff {
  display: flex;
  gap: 20px;
  margin-bottom: 15px;
}

.original, .corrected {
  flex: 1;
}

.text-content {
  white-space: pre-wrap;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  padding: 10px;
  background-color: #fff;
  min-height: 100px;
}

.diff-highlight {
  background-color: #ffeaea;
  color: #f56c6c;
  text-decoration: underline;
  padding: 0 2px;
}

.corrected .diff-highlight {
  background-color: #f0f9eb;
  color: #67c23a;
}

.correction-stats {
  text-align: right;
  color: #606266;
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