<template>
  <div class="project-sidebar">
    <h2>项目列表</h2>
    <ul>
      <li
        v-for="project in projects"
        :key="project"
        :class="{ selected: project === selectedProjectId }"
        @click="$emit('select', project)"
      >
        {{ project }}

        <!-- 显示该项目的提醒 -->
        <ul class="reminder-list">
          <li
            v-for="reminder in getRemindersForProject(project)"
            :key="reminder.date + reminder.title"
            class="reminder-item"
          >
            📌 {{ reminder.title }}（{{ reminder.date }}）
          </li>
        </ul>
      </li>
    </ul>
  </div>
</template>

<script lang="ts">
import { defineComponent, PropType } from "vue";

export default defineComponent({
  name: "ProjectSidebar",
  props: {
    projects: {
      type: Array as PropType<string[]>,
      required: true,
    },
    selectedProjectId: {
      type: [String, Number],
      required: true,
    },
    reminders: {
      type: Array as PropType<
        Array<{ title: string; date: string; projectId: string | number }>
      >,
      required: true,
    },
  },
  methods: {
    getRemindersForProject(projectId: string | number) {
      return this.reminders.filter((r) => r.projectId === projectId);
    },
  },
});
</script>

<style scoped>
.project-sidebar {
  padding: 16px;
}

ul {
  list-style: none;
  padding-left: 0;
}

.selected {
  font-weight: bold;
  background-color: #ffe0f0;
  padding: 4px;
  border-radius: 4px;
}

.reminder-list {
  padding-left: 16px;
  font-size: 0.85em;
  color: #6c757d;
}
</style>
