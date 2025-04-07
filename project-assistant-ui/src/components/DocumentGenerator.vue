<template>
  <div class="document-generator-container">
    <h2 class="section-title">文档生成 <span class="collapse-btn" @click="isCollapsed = !isCollapsed">{{ isCollapsed ? '展开 ▼' : '收起 ▲' }}</span></h2>
    
    <div v-show="!isCollapsed" class="document-panel">
      <div class="editor-container">
        <div class="editor-section">
          <h3 class="section-subtitle">字段定义</h3>
          <div class="field-section">
            <div class="field-type-tabs">
              <button 
                :class="['tab-btn', activeFieldType === 'word' ? 'active' : '']" 
                @click="activeFieldType = 'word'"
              >
                Word
              </button>
              <button 
                :class="['tab-btn', activeFieldType === 'paragraph' ? 'active' : '']" 
                @click="activeFieldType = 'paragraph'"
              >
                导入模板
              </button>
            </div>
            
            <div class="field-definition" v-if="activeFieldType === 'word'">
              <div class="field-group">
                <input 
                  type="text" 
                  v-model="fieldName" 
                  placeholder="输入字段名称" 
                  class="field-input"
                />
                <button class="btn add-btn" @click="addField(fieldName)">+</button>
              </div>
              
              <div class="fields-list">
                <div 
                  v-for="(field, idx) in fields" 
                  :key="idx" 
                  class="field-item"
                >
                  <div class="field-name">{{ field }}</div>
                  <button class="btn remove-btn" @click="removeField(idx)">×</button>
                </div>
              </div>
            </div>
            
            <div class="field-definition" v-if="activeFieldType === 'paragraph'">
              <div class="upload-area">
                <div class="upload-prompt">
                  <div>请输入需要处理的文本...或者点击"上传文件"按钮上传Word/TXT文件...</div>
                  <div>也可以直接将文件拖放到文本框中...</div>
                </div>
                <button class="upload-btn">上传</button>
              </div>
            </div>
          </div>
        </div>
        
        <div class="editor-section">
          <h3 class="section-subtitle">编辑内容</h3>
          <div class="editor-content">
            <textarea 
              v-model="documentContent" 
              placeholder="在此输入文档内容，使用 ${字段名} 添加变量占位符"
              class="content-editor"
            ></textarea>
          </div>
        </div>
      </div>
      
      <div class="template-actions">
        <button class="action-btn save-btn" @click="saveTemplate">保存模板</button>
        <button class="action-btn generate-btn" @click="generateDocument">生成文档</button>
        <button class="action-btn rules-btn" @click="showRulesManager">规则管理</button>
        <button class="action-btn replace-btn" @click="showTextReplacer">执行替换</button>
      </div>
      
      <div class="result-section">
        <h3 class="section-subtitle">处理结果: <button class="copy-result-btn" @click="copyResult">复制结果</button></h3>
        <div class="result-content">
          <div class="result-display">处理后的文本将显示在这里...</div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'

// 控制面板折叠
const isCollapsed = ref(false)

// 字段相关
const activeFieldType = ref('word')
const fieldName = ref('')
const fields = ref<string[]>([])

// 文档内容
const documentContent = ref('')

// 添加字段
const addField = (name: string) => {
  if (!name.trim()) return
  fields.value.push(name.trim())
  fieldName.value = ''
}

// 删除字段
const removeField = (index: number) => {
  fields.value.splice(index, 1)
}

// 保存模板
const saveTemplate = () => {
  alert('模板已保存!')
}

// 生成文档
const generateDocument = () => {
  // 这里实现文档生成逻辑
  alert('文档已生成!')
}

// 显示规则管理器
const showRulesManager = () => {
  alert('规则管理功能暂未实现')
}

// 显示文本替换
const showTextReplacer = () => {
  alert('文本替换功能暂未实现')
}

// 复制结果
const copyResult = () => {
  alert('结果已复制到剪贴板!')
}
</script>

<style scoped>
.document-generator-container {
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

.document-panel {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.editor-container {
  display: flex;
  gap: 20px;
}

.editor-section {
  flex: 1;
  background-color: #ffebee;
  border-radius: 8px;
  padding: 15px;
  display: flex;
  flex-direction: column;
}

.section-subtitle {
  color: #d32f2f;
  margin-bottom: 10px;
  font-size: 16px;
}

.field-section {
  display: flex;
  flex-direction: column;
  gap: 10px;
  flex: 1;
}

.field-type-tabs {
  display: flex;
  gap: 10px;
  margin-bottom: 10px;
}

.tab-btn {
  padding: 8px 16px;
  border: none;
  border-radius: 4px;
  background-color: #fff;
  cursor: pointer;
}

.tab-btn.active {
  background-color: #ef5350;
  color: white;
}

.field-definition {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.field-group {
  display: flex;
  gap: 5px;
}

.field-input {
  flex: 1;
  padding: 8px 12px;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 14px;
}

.btn {
  width: 30px;
  height: 30px;
  border: none;
  border-radius: 4px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  font-size: 16px;
}

.add-btn {
  background-color: #66bb6a;
  color: white;
}

.fields-list {
  flex: 1;
  overflow-y: auto;
  border: 1px solid #ddd;
  border-radius: 4px;
  background-color: white;
  padding: 5px;
}

.field-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 5px;
  margin-bottom: 5px;
  background-color: #f5f5f5;
  border-radius: 4px;
}

.remove-btn {
  background-color: #ef5350;
  color: white;
}

.upload-area {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  gap: 15px;
  border: 2px dashed #ddd;
  border-radius: 4px;
  background-color: white;
  padding: 20px;
}

.upload-prompt {
  text-align: center;
  color: #999;
  line-height: 1.5;
}

.upload-btn {
  padding: 8px 16px;
  background-color: #2196f3;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
}

.editor-content {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.content-editor {
  flex: 1;
  padding: 10px;
  border: 1px solid #ddd;
  border-radius: 4px;
  resize: none;
  font-family: 'Microsoft YaHei', Arial, sans-serif;
  font-size: 14px;
}

.template-actions {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}

.action-btn {
  padding: 8px 16px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  color: white;
  font-weight: bold;
}

.save-btn {
  background-color: #4caf50;
}

.generate-btn {
  background-color: #2196f3;
}

.rules-btn {
  background-color: #ff9800;
}

.replace-btn {
  background-color: #9c27b0;
}

.result-section {
  background-color: #ffebee;
  border-radius: 8px;
  padding: 15px;
}

.copy-result-btn {
  padding: 4px 8px;
  background-color: #2196f3;
  color: white;
  border: none;
  border-radius: 4px;
  font-size: 12px;
  margin-left: 10px;
  cursor: pointer;
}

.result-content {
  background-color: white;
  border: 1px solid #ddd;
  border-radius: 4px;
  padding: 10px;
  min-height: 150px;
}

.result-display {
  color: #999;
  font-style: italic;
}
</style> 