<template>
  <div class="calendar-view">
    <h1>项目日历</h1>
    <div class="calendar-container">
      <div class="calendar-toolbar">
        <el-button-group>
          <el-button type="primary" @click="changeView('month')">月视图</el-button>
          <el-button type="primary" @click="changeView('week')">周视图</el-button>
          <el-button type="primary" @click="changeView('day')">日视图</el-button>
        </el-button-group>
        <div class="date-navigator">
          <el-button icon="el-icon-arrow-left" @click="prev"></el-button>
          <span class="current-date">{{ currentDateDisplay }}</span>
          <el-button icon="el-icon-arrow-right" @click="next"></el-button>
          <el-button @click="goToday">今天</el-button>
        </div>
        <div class="calendar-filter">
          <el-select v-model="filterStatus" placeholder="项目状态" clearable>
            <el-option
              v-for="(value, key) in ProjectStatus"
              :key="key"
              :label="value"
              :value="key"
            />
          </el-select>
        </div>
      </div>
      
      <div class="calendar-body" v-loading="loading">
        <div v-if="currentView === 'month'" class="month-view">
          <div class="weekday-header">
            <div v-for="day in weekdays" :key="day" class="weekday">{{ day }}</div>
          </div>
          <div class="month-grid">
            <div
              v-for="(day, index) in calendarDays"
              :key="index"
              :class="['day-cell', {
                'current-month': day.currentMonth,
                'today': day.isToday
              }]"
            >
              <div class="day-number">{{ day.date.getDate() }}</div>
              <div class="day-events">
                <div
                  v-for="event in day.events"
                  :key="event.id"
                  :class="['event-item', `status-${event.status.toLowerCase()}`]"
                  @click="showEventDetails(event)"
                >
                  {{ event.name }}
                </div>
              </div>
            </div>
          </div>
        </div>

        <div v-else-if="currentView === 'week'" class="week-view">
          <div class="weekday-header">
            <div class="hour-column"></div>
            <div v-for="(day, index) in weekViewDays" :key="index" 
                :class="['weekday', { 'today': day.isToday }]">
              {{ day.name }} {{ day.date.getDate() }}
            </div>
          </div>
          <div class="week-grid">
            <div v-for="hour in 24" :key="hour" class="hour-row">
              <div class="hour-label">{{ hour - 1 }}:00</div>
              <div v-for="(day, dayIndex) in weekViewDays" :key="`day-${dayIndex}-hour-${hour}`" class="day-column">
                <div
                  v-for="event in getEventsForHour(day.date, hour - 1)"
                  :key="event.id"
                  :class="['event-item', `status-${event.status.toLowerCase()}`]"
                  @click="showEventDetails(event)"
                >
                  {{ event.name }}
                </div>
              </div>
            </div>
          </div>
        </div>

        <div v-else class="day-view">
          <h3>{{ dayViewDate.toLocaleDateString('zh-CN', { year: 'numeric', month: 'long', day: 'numeric' }) }}</h3>
          <div class="day-schedule">
            <div v-for="hour in 24" :key="hour" class="hour-block">
              <div class="hour-label">{{ hour - 1 }}:00</div>
              <div class="hour-events">
                <div
                  v-for="event in getEventsForHour(dayViewDate, hour - 1)"
                  :key="event.id"
                  :class="['event-item', `status-${event.status.toLowerCase()}`]"
                  @click="showEventDetails(event)"
                >
                  {{ event.name }}
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 项目详情对话框 -->
    <el-dialog
      title="项目详情"
      v-model="dialogVisible"
      width="50%"
    >
      <div v-if="selectedEvent" class="event-details">
        <div class="detail-item">
          <span class="label">项目名称:</span>
          <span class="value">{{ selectedEvent.name }}</span>
        </div>
        <div class="detail-item">
          <span class="label">项目ID:</span>
          <span class="value">{{ selectedEvent.id }}</span>
        </div>
        <div class="detail-item">
          <span class="label">评审期:</span>
          <span class="value">{{ selectedEvent.reviewPeriod }}</span>
        </div>
        <div class="detail-item">
          <span class="label">上线日期:</span>
          <span class="value">{{ formatDate(selectedEvent.onlineDate) }}</span>
        </div>
        <div class="detail-item">
          <span class="label">状态:</span>
          <span class="value" :class="`status-${selectedEvent.status.toLowerCase()}`">
            {{ ProjectStatus[selectedEvent.status] }}
          </span>
        </div>
        <div class="detail-item" v-if="selectedEvent.remark">
          <span class="label">备注:</span>
          <span class="value">{{ selectedEvent.remark }}</span>
        </div>
      </div>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">关闭</el-button>
          <el-button type="primary" @click="editProject">编辑项目</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue';
import { useRouter } from 'vue-router';
import { useProjectStore } from '../../store/project';
import { ProjectStatus } from '../../types/project';
import type { Project } from '../../types/project';
import { ElMessage } from 'element-plus';

// 路由
const router = useRouter();
// 项目数据
const projectStore = useProjectStore();

// 日历状态
const currentView = ref('month');
const currentDate = ref(new Date());
const filterStatus = ref('');
const loading = ref(false);
const dialogVisible = ref(false);
const selectedEvent = ref<Project | null>(null);

// 计算当前显示的日期标题
const currentDateDisplay = computed(() => {
  const date = currentDate.value;
  
  if (currentView.value === 'month') {
    return date.toLocaleDateString('zh-CN', { year: 'numeric', month: 'long' });
  } else if (currentView.value === 'week') {
    const weekStart = new Date(date);
    const day = date.getDay();
    const diff = date.getDate() - day + (day === 0 ? -6 : 1); // 调整周日为一周的第一天
    weekStart.setDate(diff);
    
    const weekEnd = new Date(weekStart);
    weekEnd.setDate(weekStart.getDate() + 6);
    
    const startStr = weekStart.toLocaleDateString('zh-CN', { month: 'long', day: 'numeric' });
    const endStr = weekEnd.toLocaleDateString('zh-CN', { month: 'long', day: 'numeric' });
    return `${startStr} - ${endStr}`;
  } else {
    return date.toLocaleDateString('zh-CN', { year: 'numeric', month: 'long', day: 'numeric' });
  }
});

// 星期名称
const weekdays = ['周日', '周一', '周二', '周三', '周四', '周五', '周六'];

// 当前月的日历数据
const calendarDays = computed(() => {
  const date = new Date(currentDate.value);
  const year = date.getFullYear();
  const month = date.getMonth();
  
  // 获取当月第一天是星期几
  const firstDay = new Date(year, month, 1);
  const firstDayOfWeek = firstDay.getDay();
  
  // 获取当月天数
  const daysInMonth = new Date(year, month + 1, 0).getDate();
  
  // 获取上个月天数
  const daysInPrevMonth = new Date(year, month, 0).getDate();
  
  const today = new Date();
  const days = [];
  
  // 添加上个月的日期
  for (let i = firstDayOfWeek - 1; i >= 0; i--) {
    const day = new Date(year, month - 1, daysInPrevMonth - i);
    days.push({
      date: day,
      currentMonth: false,
      isToday: isSameDay(day, today),
      events: getEventsForDay(day)
    });
  }
  
  // 添加当月的日期
  for (let i = 1; i <= daysInMonth; i++) {
    const day = new Date(year, month, i);
    days.push({
      date: day,
      currentMonth: true,
      isToday: isSameDay(day, today),
      events: getEventsForDay(day)
    });
  }
  
  // 添加下个月的日期以填满6行日历
  const totalDays = 42; // 6行 x 7天
  const remainingDays = totalDays - days.length;
  for (let i = 1; i <= remainingDays; i++) {
    const day = new Date(year, month + 1, i);
    days.push({
      date: day,
      currentMonth: false,
      isToday: isSameDay(day, today),
      events: getEventsForDay(day)
    });
  }
  
  return days;
});

// 周视图数据
const weekViewDays = computed(() => {
  const date = new Date(currentDate.value);
  const day = date.getDay();
  const diff = date.getDate() - day + (day === 0 ? -6 : 1); // 调整周日为一周的第一天
  
  const today = new Date();
  const days = [];
  
  for (let i = 0; i < 7; i++) {
    const currentDate = new Date(date);
    currentDate.setDate(diff + i);
    
    days.push({
      date: currentDate,
      name: weekdays[currentDate.getDay()],
      isToday: isSameDay(currentDate, today),
      events: getEventsForDay(currentDate)
    });
  }
  
  return days;
});

// 日视图日期
const dayViewDate = computed(() => {
  return new Date(currentDate.value);
});

// 获取指定日期的所有项目
function getEventsForDay(date: Date) {
  const projectEvents = projectStore.projects.filter((project: Project) => {
    // 如果设置了状态过滤，则过滤出对应状态的项目
    if (filterStatus.value && project.status !== filterStatus.value) {
      return false;
    }
    
    // 检查上线日期是否在当天
    const onlineDate = new Date(project.onlineDate);
    return isSameDay(onlineDate, date);
  });
  
  return projectEvents;
}

// 获取指定小时的项目
function getEventsForHour(date: Date, hour: number) {
  return getEventsForDay(date).filter((project: Project) => {
    const onlineDate = new Date(project.onlineDate);
    return onlineDate.getHours() === hour;
  });
}

// 判断两个日期是否是同一天
function isSameDay(date1: Date, date2: Date) {
  return date1.getFullYear() === date2.getFullYear() &&
         date1.getMonth() === date2.getMonth() &&
         date1.getDate() === date2.getDate();
}

// 格式化日期
function formatDate(dateString: string) {
  const date = new Date(dateString);
  return date.toLocaleDateString('zh-CN', { 
    year: 'numeric', 
    month: 'long', 
    day: 'numeric',
    hour: '2-digit',
    minute: '2-digit'
  });
}

// 切换视图
function changeView(view: string) {
  currentView.value = view;
}

// 前一个周期
function prev() {
  const date = new Date(currentDate.value);
  if (currentView.value === 'month') {
    date.setMonth(date.getMonth() - 1);
  } else if (currentView.value === 'week') {
    date.setDate(date.getDate() - 7);
  } else {
    date.setDate(date.getDate() - 1);
  }
  currentDate.value = date;
}

// 后一个周期
function next() {
  const date = new Date(currentDate.value);
  if (currentView.value === 'month') {
    date.setMonth(date.getMonth() + 1);
  } else if (currentView.value === 'week') {
    date.setDate(date.getDate() + 7);
  } else {
    date.setDate(date.getDate() + 1);
  }
  currentDate.value = date;
}

// 跳转到今天
function goToday() {
  currentDate.value = new Date();
}

// 显示项目详情
function showEventDetails(event: Project) {
  selectedEvent.value = event;
  dialogVisible.value = true;
}

// 编辑项目
function editProject() {
  if (selectedEvent.value) {
    router.push(`/project/edit/${selectedEvent.value.id}`);
  }
  dialogVisible.value = false;
}

// 监听过滤状态变化，重新加载日历
watch(filterStatus, () => {
  // 触发重新计算
});

// 组件加载时获取项目数据
onMounted(async () => {
  loading.value = true;
  try {
    await projectStore.fetchAllProjects();
  } catch (error) {
    ElMessage.error('获取项目数据失败');
    console.error(error);
  } finally {
    loading.value = false;
  }
});
</script>

<style scoped>
.calendar-view {
  padding: 20px;
}

.calendar-toolbar {
  display: flex;
  justify-content: space-between;
  margin-bottom: 20px;
  align-items: center;
}

.date-navigator {
  display: flex;
  align-items: center;
  gap: 10px;
}

.current-date {
  font-size: 18px;
  font-weight: bold;
  min-width: 150px;
  text-align: center;
}

.weekday-header {
  display: flex;
}

.weekday {
  flex: 1;
  text-align: center;
  font-weight: bold;
  padding: 10px;
  background-color: #f5f7fa;
  border-bottom: 1px solid #ebeef5;
}

/* 月视图样式 */
.month-grid {
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  gap: 1px;
  background-color: #ebeef5;
}

.day-cell {
  background-color: white;
  min-height: 100px;
  padding: 5px;
  border: 1px solid #ebeef5;
}

.day-cell:not(.current-month) {
  background-color: #f8f8f8;
  color: #c0c4cc;
}

.day-cell.today {
  background-color: #f0f9eb;
}

.day-number {
  text-align: right;
  font-weight: bold;
  margin-bottom: 5px;
}

.day-events {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.event-item {
  padding: 2px 5px;
  border-radius: 3px;
  font-size: 0.8em;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  cursor: pointer;
  color: white;
}

/* 周视图样式 */
.week-grid {
  display: flex;
  flex-direction: column;
  border: 1px solid #ebeef5;
}

.hour-row {
  display: flex;
  min-height: 50px;
  border-bottom: 1px solid #ebeef5;
}

.hour-label {
  width: 50px;
  padding: 5px;
  text-align: right;
  color: #909399;
  font-size: 0.8em;
  border-right: 1px solid #ebeef5;
}

.day-column {
  flex: 1;
  padding: 5px;
  border-right: 1px solid #ebeef5;
}

/* 日视图样式 */
.day-schedule {
  display: flex;
  flex-direction: column;
  border: 1px solid #ebeef5;
}

.hour-block {
  display: flex;
  min-height: 50px;
  border-bottom: 1px solid #ebeef5;
}

.hour-events {
  flex: 1;
  padding: 5px;
}

/* 项目状态颜色 */
.status-active {
  background-color: #67c23a;
}

.status-completed {
  background-color: #409eff;
}

.status-cancelled {
  background-color: #f56c6c;
}

.status-expired {
  background-color: #e6a23c;
}

/* 详情对话框样式 */
.event-details {
  padding: 10px;
}

.detail-item {
  margin-bottom: 10px;
  display: flex;
}

.detail-item .label {
  font-weight: bold;
  margin-right: 10px;
  min-width: 80px;
}

.detail-item .value {
  flex: 1;
}
</style> 