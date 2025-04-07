<template>
  <div class="calendar-container">
    <div class="calendar-header">
      <button @click="prevMonth" class="nav-btn">&lt;</button>
      <div class="month-year">{{ currentYearMonth }}</div>
      <button @click="nextMonth" class="nav-btn">&gt;</button>
    </div>
    
    <div class="weekdays-header">
      <div class="weekday" v-for="weekday in weekdays" :key="weekday">{{ weekday }}</div>
    </div>
    
    <div class="calendar-grid">
      <div 
        v-for="day in calendarDays" 
        :key="day.date" 
        class="calendar-day"
        :class="{ 
          'current-month': day.currentMonth, 
          'other-month': !day.currentMonth,
          'today': day.isToday,
          'holiday': day.isHoliday
        }"
      >
        <div class="day-number">{{ day.dayNumber }}</div>
        <div v-if="day.isHoliday" class="holiday-marker">{{ day.holidayName }}</div>
        
        <div class="day-events">
          <div 
            v-for="event in day.events" 
            :key="event.id" 
            class="day-event"
            :class="event.type"
            @click="showEventDetails(event)"
          >
            {{ event.name }}
          </div>
        </div>
      </div>
    </div>
    
    <div class="calendar-footer">
      <div class="legend">
        <div class="legend-item">
          <div class="legend-color today"></div>
          <span>今天</span>
        </div>
        <div class="legend-item">
          <div class="legend-color upload"></div>
          <span>上网日期</span>
        </div>
        <div class="legend-item">
          <div class="legend-color registration"></div>
          <span>报名截止</span>
        </div>
        <div class="legend-item">
          <div class="legend-color review"></div>
          <span>最早评审</span>
        </div>
        <div class="legend-item">
          <div class="legend-color bidding"></div>
          <span>开标时间</span>
        </div>
        <div class="legend-item">
          <div class="legend-color expert"></div>
          <span>专家评审</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import dayjs from 'dayjs'

// 当前日期
const currentDate = ref(dayjs())

// 星期名称
const weekdays = ['周一', '周二', '周三', '周四', '周五', '周六', '周日']

// 假期数据
const holidays = {
  '2025-04-05': '清明节',
  '2025-04-06': '清明节',
  '2025-05-01': '劳动节',
  '2025-05-02': '劳动节',
  '2025-05-03': '劳动节',
  '2025-05-04': '劳动节',
}

// 模拟项目数据
const projectEvents = [
  { 
    id: 1, 
    name: '测试', 
    type: 'test',
    dates: {
      '2025-04-02': { type: 'upload', name: '上网' },
      '2025-04-03': { type: 'bidding', name: '开标' },
      '2025-04-04': { type: 'expert', name: '专家评审' },
      '2025-04-11': { type: 'registration', name: '报名截止' },
      '2025-04-23': { type: 'review', name: '最早评审' },
    }
  },
  { 
    id: 2, 
    name: '044云容灾', 
    type: 'disaster-recovery',
    dates: {
      '2025-04-07': { type: 'registration', name: '报名截止' },
      '2025-04-11': { type: 'registration', name: '报名截止' },
      '2025-04-17': { type: 'upload', name: '上网' },
      '2025-04-17': { type: 'bidding', name: '开标' },
      '2025-04-17': { type: 'expert', name: '专家评审' },
    }
  }
]

// 当前年月显示
const currentYearMonth = computed(() => {
  return currentDate.value.format('YYYY年MM月')
})

// 计算日历数据
const calendarDays = computed(() => {
  const year = currentDate.value.year()
  const month = currentDate.value.month()
  
  // 当月第一天
  const firstDayOfMonth = dayjs(new Date(year, month, 1))
  // 当月最后一天
  const lastDayOfMonth = dayjs(new Date(year, month + 1, 0))
  
  // 日历起始日期（从当月第一天所在周的周一开始）
  const startDate = firstDayOfMonth.day() === 0 
    ? firstDayOfMonth.subtract(6, 'day') 
    : firstDayOfMonth.subtract(firstDayOfMonth.day() - 1, 'day')
  
  const days = []
  const today = dayjs()
  
  // 生成6周的日历数据
  for (let i = 0; i < 42; i++) {
    const date = startDate.add(i, 'day')
    const dateStr = date.format('YYYY-MM-DD')
    
    // 收集当天的事件
    const events = []
    projectEvents.forEach(project => {
      const event = project.dates[dateStr]
      if (event) {
        events.push({
          id: `${project.id}-${event.type}`,
          name: `${project.name} ${event.name}`,
          type: event.type,
          projectId: project.id
        })
      }
    })
    
    days.push({
      date: dateStr,
      dayNumber: date.date(),
      currentMonth: date.month() === month,
      isToday: date.format('YYYY-MM-DD') === today.format('YYYY-MM-DD'),
      isHoliday: !!holidays[dateStr],
      holidayName: holidays[dateStr],
      events
    })
  }
  
  return days
})

// 上一个月
const prevMonth = () => {
  currentDate.value = currentDate.value.subtract(1, 'month')
}

// 下一个月
const nextMonth = () => {
  currentDate.value = currentDate.value.add(1, 'month')
}

// 显示事件详情
const showEventDetails = (event: any) => {
  console.log('Event clicked:', event)
}

// 组件挂载时设置为当前日期
onMounted(() => {
  currentDate.value = dayjs('2025-04-01')
})
</script>

<style scoped>
.calendar-container {
  font-family: 'Microsoft YaHei', Arial, sans-serif;
  padding: 10px;
}

.calendar-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
  background-color: #ffcdd2;
  padding: 10px;
  border-radius: 4px;
}

.month-year {
  font-size: 18px;
  font-weight: bold;
  color: #d32f2f;
}

.nav-btn {
  background-color: white;
  border: none;
  width: 30px;
  height: 30px;
  border-radius: 15px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
}

.weekdays-header {
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  text-align: center;
  background-color: #ffcdd2;
  font-weight: bold;
  padding: 10px 0;
  border-top-left-radius: 4px;
  border-top-right-radius: 4px;
}

.weekday {
  padding: 5px;
}

.calendar-grid {
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  grid-gap: 1px;
  background-color: #ef9a9a;
  border: 1px solid #ef9a9a;
}

.calendar-day {
  min-height: 100px;
  padding: 5px;
  background-color: white;
  position: relative;
}

.calendar-day.other-month {
  background-color: #f5f5f5;
  color: #999;
}

.calendar-day.today {
  background-color: #e3f2fd;
}

.calendar-day.holiday {
  color: #d32f2f;
}

.day-number {
  font-weight: bold;
  text-align: right;
  margin-bottom: 5px;
}

.holiday-marker {
  color: #d32f2f;
  font-size: 12px;
  position: absolute;
  top: 5px;
  left: 5px;
}

.day-events {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.day-event {
  font-size: 12px;
  padding: 2px 4px;
  border-radius: 2px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  cursor: pointer;
}

.day-event.upload {
  background-color: #bbdefb;
  color: #0d47a1;
}

.day-event.bidding {
  background-color: #c8e6c9;
  color: #1b5e20;
}

.day-event.expert {
  background-color: #e1bee7;
  color: #4a148c;
}

.day-event.registration {
  background-color: #ffccbc;
  color: #bf360c;
}

.day-event.review {
  background-color: #f0f4c3;
  color: #827717;
}

.calendar-footer {
  margin-top: 10px;
  padding: 10px;
  background-color: #ffebee;
  border-radius: 4px;
}

.legend {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.legend-item {
  display: flex;
  align-items: center;
  gap: 5px;
}

.legend-color {
  width: 15px;
  height: 15px;
  border-radius: 3px;
}

.legend-color.today {
  background-color: #e3f2fd;
  border: 1px solid #90caf9;
}

.legend-color.upload {
  background-color: #bbdefb;
}

.legend-color.bidding {
  background-color: #c8e6c9;
}

.legend-color.expert {
  background-color: #e1bee7;
}

.legend-color.registration {
  background-color: #ffccbc;
}

.legend-color.review {
  background-color: #f0f4c3;
}
</style> 