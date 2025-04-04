<template>
  <div class="main-layout">
    <!-- 左侧项目列表 -->
    <ProjectSidebar
      class="sidebar"
      :projects="projectList"
      :selectedProjectId="selectedProjectId"
      :reminders="reminders"
      @select="selectedProjectId = $event"
    />

    <!-- 中央内容：日历等主视图 -->
    <div class="main-content">
      <HeaderBanner />
      <keep-alive>
        <CalendarView
          v-if="selectedProjectId"
          :currentYear="currentYear"
          :currentMonth="currentMonth"
          :projectId="selectedProjectId"
          :reminders="reminders"
          @add="addReminder"
        />
      </keep-alive>
    </div>

    <!-- 底部提示栏 -->
    <FooterTip />
  </div>
</template>

<script lang="ts">
import { defineComponent, ref, computed } from "vue";
import ProjectSidebar from "@/components/ProjectSidebar.vue";
import FooterTip from "@/components/FooterTip.vue";
import CalendarView from "@/components/CalendarView.vue";

export default defineComponent({
  components: {
    ProjectSidebar,
    FooterTip,
    CalendarView,
  },
  setup() {
    const projectList = ref(["测试项目", "044云咨文案"]);
    const selectedProjectId = ref(projectList.value[0]); // 默认选中第一个项目
    const currentDate = new Date();
    const currentYear = ref(currentDate.getFullYear());
    const currentMonth = ref(currentDate.getMonth());

    const goPrevMonth = () => {
      if (currentMonth.value === 0) {
        currentMonth.value = 11;
        currentYear.value--;
      } else {
        currentMonth.value--;
      }
    };

    const goNextMonth = () => {
      if (currentMonth.value === 11) {
        currentMonth.value = 0;
        currentYear.value++;
      } else {
        currentMonth.value++;
      }
    };
    const reminders = ref<
      Array<{
        date: string;
        title: string;
        projectId: string;
      }>
    >([
      {
        date: "2025-04-04",
        title: "演示提醒",
        projectId: "044云咨文案",
      },
    ]);

    const addReminder = (reminder: {
      title: string;
      date: string;
      projectId: string;
    }) => {
      reminders.value.push(reminder);
    };
    return {
      projectList,
      selectedProjectId,
      currentYear,
      currentMonth,
      goPrevMonth,
      goNextMonth,
      reminders,
      addReminder,
    };
  },
});
</script>

<style scoped>
.main-layout {
  display: flex;
  flex-direction: row;
  height: 100vh;
  background-color: #fff0f5;
  font-family: "Segoe UI", sans-serif;
}

.sidebar {
  width: 260px;
  background-color: #ffe6f0;
  border-right: 1px solid #f8cce0;
  box-shadow: 2px 0 5px rgba(0, 0, 0, 0.05);
}

.main-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  padding: 20px;
  overflow-y: auto;
  background-color: #fff8fb;
}
</style>
