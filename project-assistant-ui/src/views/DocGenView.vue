<template>
  <div class="doc-gen-container">
    <el-tabs v-model="activeTab" class="doc-gen-tabs">
      <el-tab-pane label="模板创建" name="template">
        <div class="template-panel">
          <div class="panel-header">
            <h3>创建文档模板</h3>
          </div>
          <div class="panel-body">
            <el-form :model="templateForm" label-width="100px">
              <el-form-item label="模板类型">
                <el-radio-group v-model="templateForm.templateType">
                  <el-radio label="WORD">Word文档</el-radio>
                  <el-radio label="EXCEL">Excel表格</el-radio>
                </el-radio-group>
              </el-form-item>
              
              <el-form-item label="模板名称">
                <el-input v-model="templateForm.templateName" placeholder="请输入模板名称"></el-input>
              </el-form-item>
              
              <el-form-item label="字段定义">
                <div class="field-input-group">
                  <el-input v-model="newField" placeholder="输入字段名称"></el-input>
                  <el-button type="primary" @click="addField" :disabled="!newField">添加字段</el-button>
                </div>
                
                <div class="field-list">
                  <el-tag
                    v-for="(field, index) in templateForm.fields"
                    :key="index"
                    closable
                    @close="removeField(index)"
                    class="field-tag"
                  >
                    {{ field }}
                  </el-tag>
                </div>
              </el-form-item>
              
              <el-form-item>
                <el-button type="primary" @click="createTemplate" :disabled="!canCreateTemplate">生成模板</el-button>
                <el-button @click="resetTemplateForm">重置</el-button>
              </el-form-item>
            </el-form>
          </div>
        </div>
        
        <div class="template-list-panel">
          <div class="panel-header">
            <h3>可用模板</h3>
            <el-button size="small" @click="loadTemplates">刷新</el-button>
          </div>
          <div class="panel-body">
            <el-table :data="templates" style="width: 100%">
              <el-table-column prop="name" label="模板名称"></el-table-column>
              <el-table-column prop="type" label="类型" width="100"></el-table-column>
              <el-table-column label="操作" width="150">
                <template #default="scope">
                  <el-button size="small" @click="useTemplate(scope.row)">使用此模板</el-button>
                </template>
              </el-table-column>
            </el-table>
          </div>
        </div>
      </el-tab-pane>
      
      <el-tab-pane label="数据录入" name="data">
        <div class="data-panel">
          <div class="panel-header">
            <h3>数据录入</h3>
          </div>
          <div class="panel-body">
            <div class="template-selection">
              <el-form :model="dataForm" label-width="100px">
                <el-form-item label="选择模板">
                  <el-select v-model="selectedTemplate" placeholder="请选择模板" @change="templateSelected">
                    <el-option
                      v-for="item in templates"
                      :key="item.path"
                      :label="item.name"
                      :value="item"
                    ></el-option>
                  </el-select>
                </el-form-item>
              </el-form>
            </div>
            
            <div v-if="selectedTemplate" class="data-input">
              <el-table :data="tableData" style="width: 100%" border>
                <el-table-column
                  v-for="field in extractedFields"
                  :key="field"
                  :prop="field"
                  :label="field"
                  min-width="150"
                >
                  <template #default="scope">
                    <el-input v-model="scope.row[field]" placeholder="输入数据"></el-input>
                  </template>
                </el-table-column>
                <el-table-column label="操作" width="150" fixed="right">
                  <template #default="scope">
                    <el-button size="small" type="danger" @click="removeRow(scope.$index)">删除</el-button>
                  </template>
                </el-table-column>
              </el-table>
              
              <div class="table-operations">
                <el-button type="primary" @click="addRow">添加行</el-button>
                <el-button @click="clearTable">清空表格</el-button>
                <el-button type="success" @click="generateDocument" :disabled="!canGenerateDocument">生成文档</el-button>
              </div>
            </div>
            
            <div v-else class="no-template">
              <el-empty description="请先选择模板文件"></el-empty>
            </div>
          </div>
        </div>
        
        <div class="document-list-panel">
          <div class="panel-header">
            <h3>生成的文档</h3>
            <el-button size="small" @click="loadDocuments">刷新</el-button>
          </div>
          <div class="panel-body">
            <el-table :data="documents" style="width: 100%">
              <el-table-column prop="name" label="文档名称"></el-table-column>
              <el-table-column prop="type" label="类型" width="100"></el-table-column>
              <el-table-column label="操作" width="150">
                <template #default="scope">
                  <el-button size="small" type="primary" @click="downloadDocument(scope.row)">下载</el-button>
                </template>
              </el-table-column>
            </el-table>
          </div>
        </div>
      </el-tab-pane>
    </el-tabs>
    
    <el-dialog v-model="dialogVisible" title="操作结果" width="500px">
      <div v-if="operationResult">
        <div v-if="operationResult.success" class="success-message">
          <i class="el-icon-check"></i>
          <span>{{ operationResult.message }}</span>
          <div>
            <p>文件保存路径: {{ operationResult.outputFilePath }}</p>
            <p>处理时间: {{ operationResult.processTime }}ms</p>
          </div>
        </div>
        <div v-else class="error-message">
          <i class="el-icon-close"></i>
          <span>{{ operationResult.message }}</span>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script lang="ts" setup>
import { ref, computed, onMounted } from 'vue';
import { ElMessage } from 'element-plus';
import type { UploadFile } from 'element-plus';
import axios from 'axios';

// 选项卡状态
const activeTab = ref('template');

// 模板创建表单
const templateForm = ref({
  templateType: 'WORD',
  templateName: '',
  fields: [] as string[]
});

// 字段输入
const newField = ref('');

// 可用模板列表
const templates = ref([] as any[]);

// 已生成文档列表
const documents = ref([] as any[]);

// 数据录入表单
const dataForm = ref({
  templateFile: null as File | null
});

// 选择的模板
const selectedTemplate = ref(null as any);

// 表格数据
const tableData = ref([] as any[]);

// 提取的字段
const extractedFields = ref([] as string[]);

// 操作结果弹窗
const dialogVisible = ref(false);
const operationResult = ref(null as any);

// 计算属性：是否可以创建模板
const canCreateTemplate = computed(() => {
  return templateForm.value.templateName && templateForm.value.fields.length > 0;
});

// 计算属性：是否可以生成文档
const canGenerateDocument = computed(() => {
  return selectedTemplate.value && tableData.value.length > 0;
});

// 生命周期：页面挂载时加载模板
onMounted(() => {
  loadTemplates();
  loadDocuments();
});

// 加载模板列表
async function loadTemplates() {
  try {
    const response = await axios.get('/api/document/templates');
    templates.value = response.data;
  } catch (error) {
    ElMessage.error('无法加载模板列表');
    console.error(error);
  }
}

// 加载文档列表
async function loadDocuments() {
  try {
    const response = await axios.get('/api/document/documents');
    documents.value = response.data;
  } catch (error) {
    ElMessage.error('无法加载文档列表');
    console.error(error);
  }
}

// 添加字段
function addField() {
  if (newField.value && !templateForm.value.fields.includes(newField.value)) {
    templateForm.value.fields.push(newField.value);
    newField.value = '';
  }
}

// 移除字段
function removeField(index: number) {
  templateForm.value.fields.splice(index, 1);
}

// 重置模板表单
function resetTemplateForm() {
  templateForm.value = {
    templateType: 'WORD',
    templateName: '',
    fields: []
  };
  newField.value = '';
}

// 创建模板
async function createTemplate() {
  if (!canCreateTemplate.value) {
    return;
  }
  
  try {
    const response = await axios.post('/api/document/createTemplate', templateForm.value);
    operationResult.value = response.data;
    dialogVisible.value = true;
    if (response.data.success) {
      ElMessage.success('模板创建成功');
      resetTemplateForm();
      loadTemplates();
    }
  } catch (error) {
    ElMessage.error('模板创建失败');
    console.error(error);
  }
}

// 使用模板
function useTemplate(template: any) {
  activeTab.value = 'data';
  selectedTemplate.value = template;
  templateSelected();
}

// 模板选择事件
function templateSelected() {
  if (selectedTemplate.value) {
    // 基于模板类型和名称推断字段
    const fields = [];
    
    if (selectedTemplate.value.name.includes('_')) {
      // 从模板名称中尝试提取字段名
      const nameParts = selectedTemplate.value.name.split('_');
      // 假设这是用我们的模板系统创建的
      if (templateForm.value.fields.length > 0) {
        fields.push(...templateForm.value.fields);
      } else {
        // 添加一些示例字段
        if (selectedTemplate.value.type === 'WORD') {
          fields.push('标题', '内容', '作者', '日期');
        } else {
          fields.push('姓名', '年龄', '部门', '职位');
        }
      }
    }
    
    extractedFields.value = fields;
    
    // 初始化表格数据
    if (tableData.value.length === 0) {
      addRow();
    }
  }
}

// 添加行
function addRow() {
  const newRow = {} as any;
  extractedFields.value.forEach(field => {
    newRow[field] = '';
  });
  tableData.value.push(newRow);
}

// 移除行
function removeRow(index: number) {
  tableData.value.splice(index, 1);
}

// 清空表格
function clearTable() {
  tableData.value = [];
}

// 生成文档
async function generateDocument() {
  if (!canGenerateDocument.value) {
    return;
  }
  
  try {
    // 准备表单数据
    const formData = new FormData();
    
    // 上传模板文件
    const response = await axios.get(selectedTemplate.value.path, { responseType: 'blob' });
    const templateFile = new File([response.data], selectedTemplate.value.name, {
      type: selectedTemplate.value.type === 'WORD' ? 'application/vnd.openxmlformats-officedocument.wordprocessingml.document' : 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet'
    });
    
    formData.append('templateFile', templateFile);
    
    // 构造JSON数据
    let jsonData;
    if (extractedFields.value.length === 0) {
      // 没有字段定义，将整个表格数据作为数据源
      jsonData = JSON.stringify(tableData.value[0]);
    } else {
      // 按照字段定义构造数据
      const dataObj = {} as any;
      
      // 单条数据
      dataObj.data = tableData.value[0];
      
      // 列表数据
      dataObj.items = tableData.value;
      
      jsonData = JSON.stringify(dataObj);
    }
    
    formData.append('data', jsonData);
    
    // 发送请求
    const generateResponse = await axios.post('/api/document/generate', formData, {
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    });
    
    operationResult.value = generateResponse.data;
    dialogVisible.value = true;
    
    if (generateResponse.data.success) {
      ElMessage.success('文档生成成功');
      loadDocuments();
    }
  } catch (error) {
    ElMessage.error('文档生成失败');
    console.error(error);
  }
}

// 下载文档
function downloadDocument(document: any) {
  window.open(document.path, '_blank');
}
</script>

<style scoped>
.doc-gen-container {
  height: 100%;
  padding: 20px;
}

.doc-gen-tabs {
  height: 100%;
}

.panel-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.panel-body {
  margin-bottom: 30px;
}

.field-input-group {
  display: flex;
  margin-bottom: 10px;
}

.field-input-group .el-input {
  margin-right: 10px;
}

.field-list {
  margin-top: 10px;
}

.field-tag {
  margin-right: 8px;
  margin-bottom: 8px;
}

.template-list-panel,
.document-list-panel {
  margin-top: 30px;
  border-top: 1px solid #eee;
  padding-top: 20px;
}

.data-input {
  margin-top: 20px;
}

.table-operations {
  margin-top: 15px;
  display: flex;
  gap: 10px;
}

.no-template {
  margin-top: 50px;
  text-align: center;
}

.success-message {
  color: #67c23a;
}

.error-message {
  color: #f56c6c;
}
</style> 