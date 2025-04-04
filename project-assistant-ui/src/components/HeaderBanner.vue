<template>
  <div class="header-banner">
    <div class="left">
      <button @click="prevMonth" class="nav-button">←</button>
    </div>

    <div class="center">
      <h2 class="month">{{ currentYear }} 年 {{ currentMonth + 1 }} 月</h2>
    </div>

    <div class="right">
      <button @click="nextMonth" class="nav-button">→</button>
    </div>
  </div>
</template>

<script lang="ts">
import { defineComponent, ref } from "vue";

export default defineComponent({
  name: "HeaderBanner",
  setup() {
    const today = new Date();
    const currentYear = ref(today.getFullYear());
    const currentMonth = ref(today.getMonth());

    const prevMonth = () => {
      if (currentMonth.value === 0) {
        currentMonth.value = 11;
        currentYear.value--;
      } else {
        currentMonth.value--;
      }
    };

    const nextMonth = () => {
      if (currentMonth.value === 11) {
        currentMonth.value = 0;
        currentYear.value++;
      } else {
        currentMonth.value++;
      }
    };

    return {
      currentYear,
      currentMonth,
      prevMonth,
      nextMonth,
    };
  },
});
</script>

<style scoped>
.header-banner {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background-color: #ffeaf0;
  padding: 12px 24px;
  border-radius: 10px;
  margin-bottom: 20px;
  box-shadow: 0 3px 5px rgba(0, 0, 0, 0.05);
}

.month {
  font-size: 1.8rem;
  color: #d63384;
  margin: 0;
}

.nav-button {
  background-color: #f8b8c6;
  color: white;
  font-size: 1.2rem;
  padding: 6px 12px;
  border: none;
  border-radius: 8px;
  cursor: pointer;
}

.nav-button:hover {
  background-color: #f17599;
}
</style>
