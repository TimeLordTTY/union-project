<template>
  <div class="reminder-dialog">
    <div class="dialog-content">
      <h3>添加提醒</h3>
      <p>日期：{{ date }}</p>
      <input v-model="title" placeholder="请输入提醒内容" />
      <div class="buttons">
        <button @click="onAdd">添加</button>
        <button @click="$emit('close')">取消</button>
      </div>
    </div>
  </div>
</template>

<script lang="ts">
import { defineComponent, ref } from "vue";

export default defineComponent({
  name: "ReminderDialog",
  props: {
    date: { type: Date, required: true },
    projectId: { type: [String, Number], required: true },
    reminders: { type: Array, required: true },
  },
  emits: ["close", "add"],
  setup(props, { emit }) {
    const title = ref("");

    const onAdd = () => {
      if (!title.value.trim()) return;
      const reminder = {
        date: props.date.toISOString().split("T")[0],
        title: title.value,
        projectId: props.projectId,
      };
      emit("add", reminder);
      emit("close");
    };

    return {
      title,
      onAdd,
    };
  },
});
</script>

<style scoped>
.reminder-dialog {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-color: rgba(0, 0, 0, 0.3);
  display: flex;
  justify-content: center;
  align-items: center;
}

.dialog-content {
  background: white;
  padding: 20px;
  border-radius: 8px;
  min-width: 300px;
}

input {
  width: 100%;
  margin-top: 10px;
  padding: 8px;
}

.buttons {
  margin-top: 10px;
  text-align: right;
}

button {
  margin-left: 10px;
  padding: 6px 12px;
  cursor: pointer;
}
</style>
