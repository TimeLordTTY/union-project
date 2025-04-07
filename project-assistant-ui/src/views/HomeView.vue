<template>
  <div class="home-view">
    <header class="header">
      <h1 class="title">项目管理小助手</h1>
      <p class="subtitle">专为项目经理设计的多功能辅助工具</p>
    </header>
    
    <div class="features">
      <router-link to="/money" class="feature-card">
        <div class="feature-icon">💰</div>
        <div class="feature-content">
          <h2>金额转换</h2>
          <p>阿拉伯数字与中文大写金额互转，支持小数点和格式校验</p>
        </div>
        <div class="arrow">→</div>
      </router-link>
      
      <div class="feature-card coming-soon">
        <div class="feature-icon">📅</div>
        <div class="feature-content">
          <h2>项目日历</h2>
          <p>管理项目时间线、截止日期和重要里程碑</p>
          <span class="badge">开发中</span>
        </div>
      </div>
      
      <div class="feature-card coming-soon">
        <div class="feature-icon">✏️</div>
        <div class="feature-content">
          <h2>文本处理</h2>
          <p>自定义文本替换规则，批量处理错别字和格式问题</p>
          <span class="badge">开发中</span>
        </div>
      </div>
      
      <div class="feature-card coming-soon">
        <div class="feature-icon">📄</div>
        <div class="feature-content">
          <h2>文档生成</h2>
          <p>基于模板快速生成项目文档，支持多种格式</p>
          <span class="badge">开发中</span>
        </div>
      </div>
    </div>
    
    <div class="info-section">
      <div class="quick-start">
        <h2>快速开始</h2>
        <ol>
          <li>点击上方功能卡片进入对应工具</li>
          <li>阅读工具页面的使用说明</li>
          <li>开始使用工具提高您的工作效率</li>
        </ol>
      </div>
      
      <div class="updates">
        <h2>最新动态</h2>
        <div class="update-item">
          <span class="update-date">2025/04/04</span>
          <span class="update-content">金额转换工具上线，支持数字与中文金额互转</span>
        </div>
        <div class="update-item">
          <span class="update-date">2025/04/01</span>
          <span class="update-content">项目管理小助手正式启动开发</span>
        </div>
      </div>
    </div>
    
    <footer class="footer">
      <p>© 2025 项目管理小助手 | 版本 1.0.0</p>
    </footer>
  </div>
</template>

<script setup>
// 组件逻辑可以在这里添加
</script>

<style scoped>
.home-view {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

.header {
  text-align: center;
  margin-bottom: 40px;
}

.title {
  font-size: 2.5rem;
  color: #1e40af;
  margin-bottom: 10px;
}

.subtitle {
  font-size: 1.2rem;
  color: #6b7280;
}

.features {
  display: grid;
  grid-template-columns: repeat(1, 1fr);
  gap: 20px;
  margin-bottom: 40px;
}

.feature-card {
  display: flex;
  align-items: center;
  background-color: white;
  border-radius: 10px;
  padding: 20px;
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
  transition: transform 0.3s, box-shadow 0.3s;
  cursor: pointer;
  text-decoration: none;
  color: inherit;
}

.feature-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 6px 12px rgba(0, 0, 0, 0.15);
}

.feature-card.coming-soon {
  opacity: 0.7;
  cursor: default;
}

.feature-card.coming-soon:hover {
  transform: none;
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
}

.feature-icon {
  font-size: 2.5rem;
  margin-right: 20px;
  width: 60px;
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.feature-content {
  flex: 1;
}

.feature-content h2 {
  margin: 0 0 8px 0;
  color: #1f2937;
  font-size: 1.5rem;
}

.feature-content p {
  margin: 0;
  color: #6b7280;
}

.arrow {
  font-size: 1.5rem;
  color: #3b82f6;
}

.badge {
  display: inline-block;
  background-color: #e5e7eb;
  color: #4b5563;
  font-size: 0.75rem;
  padding: 4px 8px;
  border-radius: 4px;
  margin-top: 8px;
}

.info-section {
  display: flex;
  flex-direction: column;
  gap: 20px;
  margin-bottom: 40px;
}

.quick-start, .updates {
  background-color: #f9fafb;
  border-radius: 10px;
  padding: 20px;
}

.quick-start h2, .updates h2 {
  color: #1f2937;
  margin-top: 0;
  margin-bottom: 16px;
  font-size: 1.3rem;
}

.quick-start ol {
  margin: 0;
  padding-left: 20px;
}

.quick-start li {
  margin-bottom: 8px;
  color: #4b5563;
}

.update-item {
  display: flex;
  margin-bottom: 12px;
}

.update-date {
  min-width: 100px;
  color: #6b7280;
  font-weight: 500;
}

.update-content {
  color: #4b5563;
}

.footer {
  text-align: center;
  color: #9ca3af;
  padding-top: 20px;
  border-top: 1px solid #e5e7eb;
}

@media (min-width: 640px) {
  .features {
    grid-template-columns: repeat(2, 1fr);
  }
  
  .info-section {
    flex-direction: row;
  }
  
  .quick-start, .updates {
    flex: 1;
  }
}

@media (min-width: 1024px) {
  .features {
    grid-template-columns: repeat(4, 1fr);
  }
}
</style> 