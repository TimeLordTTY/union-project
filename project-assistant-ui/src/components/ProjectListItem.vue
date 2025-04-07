<template>
  <div class="project-item">
    <div class="project-name">{{ project.name }}</div>
    <div class="project-dates">
      <div class="date-item">开标时间: {{ project.expectedReviewTime }}</div>
      <div class="date-item">专家评审: {{ project.expertReviewTime }}</div>
    </div>
  </div>
</template>

<script setup lang="ts">
// 定义组件接收的props
defineProps<{
  project: {
    id: number
    name: string
    reviewPeriod: number
    onlineDate: string
    registrationPeriod: number
    registrationEndDate: string
    expectedReviewTime: string
    expertReviewTime: string
    remark: string
  }
}>()
</script>

<style scoped>
.project-item {
  padding: 10px;
  border-bottom: 1px solid #ef9a9a;
  cursor: pointer;
  transition: background-color 0.2s;
}

.project-item:hover {
  background-color: #ffebee;
}

.project-name {
  font-weight: bold;
  margin-bottom: 5px;
}

.project-dates {
  font-size: 12px;
  color: #666;
}

.date-item {
  display: flex;
  align-items: center;
  margin-bottom: 2px;
}
</style> 