<template>
  <div class="calendar-view">
    <table class="calendar-table">
      <thead>
        <tr>
          <th v-for="(day, index) in weekdays" :key="index">{{ day }}</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="(week, wIndex) in calendar" :key="wIndex">
          <td
            v-for="(date, dIndex) in week"
            :key="dIndex"
            :class="[
              'calendar-cell',
              {
                today: isToday(date),
                empty: !date,
                'has-event': date && hasReminder(date),
              },
            ]"
            @click="date && onDateClick(date)"
          >
            <div v-if="date">
              <div class="date-number">{{ date.getDate() }}</div>
              <div v-if="getHolidayName(date)" class="holiday-label">
                {{ getHolidayName(date) }}
              </div>
            </div>
          </td>
        </tr>
      </tbody>
    </table>
  </div>
  <ReminderDialog
    v-if="selectedDate"
    :date="selectedDate"
    :projectId="projectId"
    :reminders="reminders"
    @close="selectedDate = null"
    @add="onAddReminder"
  />
</template>

<script lang="ts">
import {
  defineComponent,
  computed,
  ref,
  PropType,
  onMounted,
  watch,
} from "vue";
import ReminderDialog from "./ReminderDialog.vue";
import { HolidayInfo, fetchHolidays } from "@/services/holidayService";

// 页面初始化时请求节假日
const yearCache = new Map<number, Record<string, string>>();
export default defineComponent({
  name: "CalendarView",
  components: {
    ReminderDialog,
  },
  props: {
    currentYear: { type: Number, required: true },
    currentMonth: { type: Number, required: true },
    projectId: { type: [String, Number], required: true },
    reminders: {
      type: Array as PropType<
        Array<{ date: string; title: string; projectId: string | number }>
      >,
      default: () => [],
    },
  },
  emits: ["add"],

  setup(props, { emit }) {
    const selectedDate = ref<Date | null>(null);
    const weekdays = ["日", "一", "二", "三", "四", "五", "六"];

    const calendar = computed(() => {
      const firstDay = new Date(props.currentYear, props.currentMonth, 1);
      const lastDay = new Date(props.currentYear, props.currentMonth + 1, 0);

      const calendar: (Date | null)[][] = [];
      let week: (Date | null)[] = [];

      // 补前空格
      for (let i = 0; i < firstDay.getDay(); i++) {
        week.push(null);
      }

      for (let d = 1; d <= lastDay.getDate(); d++) {
        const day = new Date(props.currentYear, props.currentMonth, d);
        week.push(day);

        if (week.length === 7) {
          calendar.push(week);
          week = [];
        }
      }

      // 补尾空格
      if (week.length > 0) {
        while (week.length < 7) {
          week.push(null);
        }
        calendar.push(week);
      }

      return calendar;
    });

    const isToday = (date: Date | null) => {
      if (!date) return false;
      const today = new Date();
      // ✅ 打印当前系统识别到的“今天”
      console.log("当前系统时间是：", today.toISOString());
      return (
        date.getFullYear() === today.getFullYear() &&
        date.getMonth() === today.getMonth() &&
        date.getDate() === today.getDate()
      );
    };

    const hasReminder = (date: Date | null) => {
      if (!date) return false;
      const dateStr = date.toISOString().split("T")[0];
      return props.reminders.some(
        (reminder) =>
          reminder.date === dateStr && reminder.projectId === props.projectId
      );
    };

    const onDateClick = (date: Date) => {
      console.log("点击日期：", date);
      selectedDate.value = date;
    };

    const onAddReminder = (reminder: {
      title: string;
      date: string;
      projectId: string | number;
    }) => {
      emit("add", reminder);
    };

    // 节假日数据存储
    const holidays = ref<Record<string, string>>({});

    let hasLoaded = false;

    watch(
      () => props.currentYear,
      async (year) => {
        if (hasLoaded || yearCache.has(year)) {
          holidays.value = yearCache.get(year)!;
          return;
        }

        const data = await fetchHolidays(year);
        const holidayMap: Record<string, string> = {};
        for (const day in data.holiday) {
          holidayMap[day] = data.holiday[day].name;
        }
        holidays.value = holidayMap;
        yearCache.set(year, holidayMap);
        hasLoaded = true;
      },
      { immediate: true }
    );

    // 判断某天是否是节假日
    const getHolidayName = (date: Date | null): string | null => {
      if (!date) return null;
      const dateStr = date.toISOString().split("T")[0];
      return holidays.value[dateStr] || null;
    };
    return {
      weekdays,
      calendar,
      isToday,
      hasReminder,
      onDateClick,
      selectedDate,
      onAddReminder,
      getHolidayName,
    };
  },
});
</script>

<style scoped>
.calendar-view {
  padding: 10px;
  background-color: #fff0f6;
}

.calendar-table {
  width: 100%;
  border-collapse: collapse;
}

.calendar-table th,
.calendar-table td {
  width: 14.28%;
  height: 60px;
  text-align: center;
  border: 1px solid #ffe0ec;
  font-weight: normal;
}
.calendar-cell {
  /* 基本样式 */
  width: 14.28%;
  height: 60px;
  text-align: center;
  border: 1px solid #ffe0ec;
  font-weight: normal;
}

.today {
  background-color: #ffdbe6;
  font-weight: bold;
  border: 2px solid #ff5fa2;
}

.has-event {
  background-color: #ffedf5;
  position: relative;
}

.has-event::after {
  content: "";
  position: absolute;
  bottom: 6px;
  left: 50%;
  width: 6px;
  height: 6px;
  background-color: #d63384;
  border-radius: 50%;
  transform: translateX(-50%);
}
.date-number {
  font-size: 16px;
  font-weight: bold;
}

.holiday-label {
  font-size: 12px;
  color: #d63384;
  margin-top: 4px;
  font-weight: 500;
}
</style>
