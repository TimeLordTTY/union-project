<template>
  <div class="money-converter-view">
    <div class="header">
      <h1>金额转换工具</h1>
      <p class="description">精确转换数字金额和中文大写金额，支持金额校验和自定义格式</p>
    </div>
    
    <div class="content">
      <MoneyConverter />
      
      <div class="instructions">
        <h2>使用说明</h2>
        <div class="instruction-card">
          <div class="instruction-icon">📝</div>
          <div class="instruction-text">
            <h3>数字转中文</h3>
            <p>在上方输入框中输入数字金额，系统将自动转换为中文大写金额。支持小数点后两位，符合人民币书写规范。</p>
          </div>
        </div>
        
        <div class="instruction-card">
          <div class="instruction-icon">🔄</div>
          <div class="instruction-text">
            <h3>中文转数字</h3>
            <p>在下方输入框中输入中文大写金额，系统将自动转换为阿拉伯数字金额。支持"零"、"壹"至"玖"等中文数字。</p>
          </div>
        </div>
        
        <div class="instruction-card">
          <div class="instruction-icon">💾</div>
          <div class="instruction-text">
            <h3>历史记录</h3>
            <p>系统会自动保存您最近10次的转换记录，方便您查看和重用。记录会保存在浏览器本地，关闭页面不会丢失。</p>
          </div>
        </div>
        
        <div class="instruction-card">
          <div class="instruction-icon">📋</div>
          <div class="instruction-text">
            <h3>复制功能</h3>
            <p>转换完成后，点击"复制"按钮可以快速复制结果，方便粘贴到其他文档或应用中使用。</p>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import MoneyConverter from '../components/MoneyConverter.vue';
</script>

<style scoped>
.money-converter-view {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

.header {
  text-align: center;
  margin-bottom: 30px;
}

.header h1 {
  color: #1e40af;
  font-size: 2.5rem;
  margin-bottom: 10px;
}

.description {
  color: #6b7280;
  font-size: 1.2rem;
  max-width: 600px;
  margin: 0 auto;
}

.content {
  display: flex;
  flex-direction: column;
  gap: 30px;
}

.instructions {
  background-color: #f9fafb;
  border-radius: 10px;
  padding: 20px;
  margin-top: 20px;
}

.instructions h2 {
  color: #374151;
  text-align: center;
  margin-bottom: 20px;
  font-size: 1.5rem;
}

.instruction-card {
  display: flex;
  background-color: white;
  border-radius: 8px;
  padding: 16px;
  margin-bottom: 16px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
}

.instruction-icon {
  font-size: 2rem;
  margin-right: 16px;
  color: #3b82f6;
  display: flex;
  align-items: center;
  justify-content: center;
}

.instruction-text h3 {
  margin-top: 0;
  margin-bottom: 8px;
  color: #1f2937;
}

.instruction-text p {
  margin: 0;
  color: #4b5563;
  line-height: 1.5;
}

@media (min-width: 768px) {
  .instructions {
    display: grid;
    grid-template-columns: repeat(2, 1fr);
    gap: 16px;
  }
  
  .instructions h2 {
    grid-column: span 2;
  }
  
  .instruction-card {
    margin-bottom: 0;
  }
}
</style> 