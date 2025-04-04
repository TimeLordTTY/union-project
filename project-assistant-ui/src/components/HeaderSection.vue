<template>
  <div class="header-section">
    <h1 class="title">项目管理小助手</h1>
    <p class="subtitle">欢迎进入你的专属日程世界 ✨</p>
  </div>
</template>

<script lang="ts">
export default {
  name: "HeaderSection",
};
</script>

<style scoped>
.header-section {
  padding: 40px;
  text-align: center;
  background-color: #ffeeff;
  border-radius: 1rem;
}

.title {
  font-size: 2.5rem;
  color: #d63384;
  margin-bottom: 10px;
}

.subtitle {
  color: #6c757d;
  font-size: 1.1rem;
}
</style>
