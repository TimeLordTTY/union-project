<template>
  <div class="doc-generator">
    <h1>文档生成工具</h1>
    
    <el-form :model="formData" label-width="120px" v-loading="loading">
      <el-form-item label="模板文件">
        <el-upload
          action="#"
          :http-request="uploadTemplate"
          :on-remove="handleTemplateRemove"
          :file-list="templateFileList"
          :limit="1"
          accept=".docx,.xlsx,.xls"
        >
          <el-button type="primary">选择模板文件</el-button>
          <template #tip>
            <div class="el-upload__tip">
              支持的格式：Word (.docx) 或 Excel (.xlsx/.xls)
            </div>
          </template>
        </el-upload>
      </el-form-item>
      
      <el-form-item label="数据文件">
        <el-upload
          action="#"
          :http-request="uploadData"
          :on-remove="handleDataRemove"
          :file-list="dataFileList"
          :limit="1"
          accept=".json,.xml,.csv"
        >
          <el-button type="primary">选择数据文件</el-button>
          <template #tip>
            <div class="el-upload__tip">
              支持的格式：JSON (.json)、XML (.xml) 或 CSV (.csv)
            </div>
          </template>
        </el-upload>
      </el-form-item>
      
      <el-form-item label="输出文件名">
        <el-input v-model="formData.outputFilename" placeholder="输出文件名" />
      </el-form-item>
      
      <el-form-item>
        <el-button type="primary" @click="generateDocument" :disabled="!canGenerate">
          生成文档
        </el-button>
      </el-form-item>
    </el-form>
    
    <div class="tips">
      <h3>使用说明：</h3>
      <ul>
        <li>上传Word或Excel模板文件，模板中使用 {"{"{varName}"}"} 格式的占位符</li>
        <li>上传包含数据的JSON、XML或CSV文件，文件中的数据将用于替换模板中的占位符</li>
        <li>支持批量生成，数据文件中可包含多组数据</li>
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
  templateFile: null as File | null,
  dataFile: null as File | null,
  outputFilename: ''
});

const templateFileList = ref<any[]>([]);
const dataFileList = ref<any[]>([]);

// 判断是否可以生成文档
const canGenerate = computed(() => {
  return formData.value.templateFile && formData.value.dataFile;
});

// 上传模板文件
const uploadTemplate = (options: any) => {
  formData.value.templateFile = options.file;
  templateFileList.value = [{ name: options.file.name, url: '' }];
};

// 移除模板文件
const handleTemplateRemove = () => {
  formData.value.templateFile = null;
  templateFileList.value = [];
};

// 上传数据文件
const uploadData = (options: any) => {
  formData.value.dataFile = options.file;
  dataFileList.value = [{ name: options.file.name, url: '' }];
};

// 移除数据文件
const handleDataRemove = () => {
  formData.value.dataFile = null;
  dataFileList.value = [];
};

// 生成文档
const generateDocument = async () => {
  if (!canGenerate.value) {
    ElMessage.warning('请先上传模板文件和数据文件');
    return;
  }
  
  loading.value = true;
  try {
    const formDataObj = new FormData();
    formDataObj.append('templateFile', formData.value.templateFile as File);
    formDataObj.append('dataFile', formData.value.dataFile as File);
    
    if (formData.value.outputFilename) {
      formDataObj.append('outputFilename', formData.value.outputFilename);
    }
    
    const response = await axios.post(API_CONFIG.docGenerator.generate, formDataObj, {
      responseType: 'blob'
    });
    
    // 创建下载链接
    const url = window.URL.createObjectURL(new Blob([response.data]));
    const link = document.createElement('a');
    link.href = url;
    link.setAttribute('download', formData.value.outputFilename || '生成的文档.zip');
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
    
    ElMessage.success('文档生成成功');
  } catch (error) {
    console.error('生成文档失败:', error);
    ElMessage.error('生成文档失败，请检查模板和数据文件');
  } finally {
    loading.value = false;
  }
};
</script>

<style scoped>
.doc-generator {
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