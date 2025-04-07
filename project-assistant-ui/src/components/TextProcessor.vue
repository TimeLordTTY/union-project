<template>
  <div class="text-processor-container">
    <h2 class="section-title">文本处理 <span class="collapse-btn" @click="isCollapsed = !isCollapsed">{{ isCollapsed ? '展开 ▼' : '收起 ▲' }}</span></h2>
    
    <div v-show="!isCollapsed" class="text-processor-content">
      <div class="text-input-section">
        <div class="text-area-container">
          <h3>输入文本</h3>
          <textarea 
            v-model="inputText" 
            placeholder="请输入需要处理的文本...或者点击'上传文件'按钮上传Word/TXT文件...也可以直接将文件拖放到文本框中..." 
            class="text-input"
          ></textarea>
          <div class="upload-actions">
            <button class="btn">编辑</button>
            <button class="btn">清空</button>
            <button class="btn">上传</button>
          </div>
        </div>
      </div>
      
      <div class="processing-options">
        <div class="options-section">
          <h3>处理规则</h3>
          <div class="rule-list">
            <div class="rule-item">
              <div class="rule-header">
                <input type="checkbox" checked />
                <span class="rule-name">规则管理</span>
                <button class="btn rule-btn">执行替换</button>
              </div>
            </div>
          </div>
        </div>
      </div>
      
      <div class="text-output-section">
        <div class="text-area-container">
          <h3>处理结果:</h3>
          <div class="output-display">
            <div class="text-output">处理后的文本将显示在这里...</div>
          </div>
          <div class="result-actions">
            <button class="btn result-btn">复制结果</button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'

// 控制面板折叠
const isCollapsed = ref(false)

// 文本处理
const inputText = ref('')
const outputText = ref('处理后的文本将显示在这里...')

// 处理文本
const processText = () => {
  // 文本处理逻辑
  outputText.value = inputText.value
  // 可以添加更多处理逻辑
}

// 执行规则替换
const executeRules = () => {
  processText()
  alert('规则替换已执行!')
}

// 复制结果
const copyResult = () => {
  navigator.clipboard.writeText(outputText.value)
    .then(() => {
      alert('已复制到剪贴板!')
    })
    .catch(err => {
      console.error('复制失败:', err)
    })
}
</script>

<style scoped>
.text-processor-container {
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

.text-processor-content {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.text-input-section, .text-output-section, .processing-options {
  background-color: #ffebee;
  border-radius: 8px;
  padding: 15px;
}

.text-area-container {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

h3 {
  color: #d32f2f;
  font-size: 16px;
  margin-bottom: 5px;
}

.text-input, .output-display {
  min-height: 200px;
  border: 1px solid #ddd;
  border-radius: 4px;
  padding: 10px;
  background-color: white;
}

.text-input {
  resize: vertical;
  font-family: 'Microsoft YaHei', Arial, sans-serif;
  font-size: 14px;
}

.output-display {
  overflow: auto;
}

.text-output {
  color: #666;
  font-style: italic;
}

.upload-actions, .result-actions {
  display: flex;
  gap: 10px;
  justify-content: flex-end;
}

.options-section {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.rule-list {
  border: 1px solid #ddd;
  border-radius: 4px;
  background-color: white;
  padding: 5px;
}

.rule-item {
  background-color: #f5f5f5;
  border-radius: 4px;
  margin-bottom: 5px;
  padding: 8px;
}

.rule-header {
  display: flex;
  align-items: center;
  gap: 10px;
}

.rule-name {
  flex: 1;
  font-weight: bold;
}

.btn {
  padding: 6px 12px;
  border: none;
  border-radius: 4px;
  background-color: #ef5350;
  color: white;
  cursor: pointer;
  font-size: 12px;
}

.rule-btn {
  background-color: #ff9800;
}

.result-btn {
  background-color: #2196f3;
}
</style>