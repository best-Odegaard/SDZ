<template>
  <view class="page">
    <!-- 月份切换 -->
    <view class="month-bar">
      <view class="m-btn" @tap="changeMonth(-1)">‹</view>
      <view class="m-title">{{ viewYear }} 年 {{ viewMonth }} 月</view>
      <view class="m-btn" @tap="changeMonth(1)">›</view>
    </view>

    <!-- 星期表头 -->
    <view class="week-head">
      <text v-for="w in weekDays" :key="w" class="wh-item">{{ w }}</text>
    </view>

    <!-- 日历网格 -->
    <view class="grid">
      <view
        v-for="(cell, i) in cells"
        :key="i"
        class="cell"
        :class="{
          'other-month': !cell.current,
          'is-today': cell.isToday,
          'is-selected': cell.date === selectedDate
        }"
        @tap="selectDate(cell)"
      >
        <text class="cell-day">{{ cell.day }}</text>
        <view class="dots" v-if="cell.events.length">
          <view class="dot" v-for="(e, j) in cell.events.slice(0, 3)" :key="j" :class="dotClass(e.type)"></view>
        </view>
      </view>
    </view>

    <!-- 图例 -->
    <view class="legend">
      <view class="lg-item"><view class="dot ing"></view><text>报名开始</text></view>
      <view class="lg-item"><view class="dot wait"></view><text>报名截止</text></view>
      <view class="lg-item"><view class="dot match"></view><text>比赛</text></view>
    </view>

    <!-- 当日事件列表 -->
    <view class="event-section">
      <view class="section-title">
        {{ selectedDate || '选择日期查看' }}
        <text class="more" v-if="selectedEvents.length">{{ selectedEvents.length }} 个节点</text>
      </view>

      <view v-if="selectedEvents.length">
        <view class="event-item card" v-for="(e, i) in selectedEvents" :key="i" @tap="goDetail(e.id)">
          <view class="ev-left">
            <text class="ev-type" :class="'ev-' + typeKey(e.type)">{{ e.type }}</text>
            <text class="ev-title ellipsis-2">{{ e.title }}</text>
          </view>
          <text class="arrow">›</text>
        </view>
      </view>

      <empty-state
        v-else
        icon="📅"
        :text="selectedDate ? '这天没有赛事节点' : '点击日期查看赛事节点'"
        sub="报名开始 / 报名截止 / 比赛时间"
      />
    </view>
  </view>
</template>

<script>
/**
 * 赛事日历页
 * 月视图展示赛事的报名开始 / 报名截止 / 比赛时间节点
 */
import { competitionApi } from '@/utils/api.js'
import EmptyState from '@/components/empty-state/empty-state.vue'

const WEEK = ['日', '一', '二', '三', '四', '五', '六']

export default {
  components: { EmptyState },
  data() {
    const now = new Date()
    return {
      weekDays: WEEK,
      viewYear: now.getFullYear(),
      viewMonth: now.getMonth() + 1,
      selectedDate: '',
      dayMap: {}      // { 'YYYY-MM-DD': [events] }
    }
  },
  computed: {
    cells() {
      return this.buildCells(this.viewYear, this.viewMonth)
    },
    selectedEvents() {
      return this.dayMap[this.selectedDate] || []
    }
  },
  onLoad() {
    this.selectedDate = this.pad(new Date())
    this.loadMonth()
  },
  methods: {
    pad(d) {
      const p = (n) => String(n).padStart(2, '0')
      return `${d.getFullYear()}-${p(d.getMonth() + 1)}-${p(d.getDate())}`
    },

    /** 生成 6*7 = 42 个格子，补齐上下月的空白 */
    buildCells(year, month) {
      const firstWeekday = new Date(year, month - 1, 1).getDay()
      const daysInMonth = new Date(year, month, 0).getDate()
      const prevMonthDays = new Date(year, month - 1, 0).getDate()
      const todayStr = this.pad(new Date())
      const cells = []

      for (let i = 0; i < 42; i++) {
        const idx = i - firstWeekday + 1
        let day, curYear = year, curMonth = month, current = true

        if (idx < 1) {
          day = prevMonthDays + idx
          curMonth = month - 1
          current = false
        } else if (idx > daysInMonth) {
          day = idx - daysInMonth
          curMonth = month + 1
          current = false
        } else {
          day = idx
        }
        // 跨年修正
        if (curMonth < 1) { curMonth = 12; curYear = year - 1 }
        if (curMonth > 12) { curMonth = 1; curYear = year + 1 }

        const dateStr = `${curYear}-${String(curMonth).padStart(2, '0')}-${String(day).padStart(2, '0')}`
        cells.push({
          day,
          date: dateStr,
          current,
          isToday: dateStr === todayStr,
          events: this.dayMap[dateStr] || []
        })
      }
      return cells
    },

    async loadMonth() {
      try {
        const monthStr = `${this.viewYear}-${String(this.viewMonth).padStart(2, '0')}`
        const data = await competitionApi.calendar(monthStr)
        this.dayMap = (data && data.days) || {}
      } catch (e) {
        console.error('加载日历失败', e)
        this.dayMap = {}
      }
    },

    changeMonth(delta) {
      let m = this.viewMonth + delta
      let y = this.viewYear
      if (m < 1) { m = 12; y-- }
      if (m > 12) { m = 1; y++ }
      this.viewMonth = m
      this.viewYear = y
      this.selectedDate = ''
      this.loadMonth()
    },

    selectDate(cell) {
      if (!cell.current) return
      this.selectedDate = cell.date
    },

    typeKey(type) {
      return { '报名开始': 'ing', '报名截止': 'wait', '比赛': 'match' }[type] || 'wait'
    },

    dotClass(type) { return this.typeKey(type) },

    goDetail(id) {
      uni.navigateTo({ url: `/pages/competition/detail?id=${id}` })
    }
  }
}
</script>

<style scoped>
.page { padding: 20rpx; }

.month-bar {
  display: flex; align-items: center; justify-content: space-between;
  background: #fff; border-radius: 16rpx; padding: 24rpx 32rpx; margin-bottom: 20rpx;
}
.m-btn {
  width: 64rpx; height: 64rpx; line-height: 60rpx; text-align: center;
  background: #F5F6FA; border-radius: 50%; font-size: 40rpx; color: #606266;
}
.m-title { font-size: 32rpx; font-weight: 600; color: #303133; }

.week-head { display: flex; background: #fff; padding: 16rpx 0; border-radius: 16rpx 16rpx 0 0; }
.wh-item { flex: 1; text-align: center; font-size: 24rpx; color: #909399; }

.grid {
  display: flex; flex-wrap: wrap; background: #fff;
  border-radius: 0 0 16rpx 16rpx; padding-bottom: 12rpx;
}
.cell {
  width: 14.28%; height: 110rpx;
  display: flex; flex-direction: column; align-items: center; justify-content: center;
}
.cell-day { font-size: 28rpx; color: #303133; }
.other-month .cell-day { color: #C0C4CC; }
.is-today .cell-day { color: #4A6FE3; font-weight: 700; }
.is-selected { background: #4A6FE3; border-radius: 12rpx; }
.is-selected .cell-day { color: #fff; font-weight: 600; }

.dots { display: flex; gap: 4rpx; margin-top: 8rpx; }
.dot { width: 10rpx; height: 10rpx; border-radius: 50%; background: #DCDFE6; }
.dot.ing { background: #52C41A; }
.dot.wait { background: #FA8C16; }
.dot.match { background: #4A6FE3; }
.is-selected .dot { background: #fff; }

.legend {
  display: flex; justify-content: center; gap: 40rpx;
  padding: 24rpx; font-size: 24rpx; color: #909399;
}
.lg-item { display: flex; align-items: center; gap: 10rpx; }
.lg-item .dot { width: 12rpx; height: 12rpx; }

.event-section { margin-top: 8rpx; }
.event-item {
  display: flex; align-items: center; justify-content: space-between;
  padding: 24rpx; margin-bottom: 16rpx;
}
.ev-left { display: flex; align-items: center; gap: 18rpx; flex: 1; min-width: 0; }
.ev-type {
  flex-shrink: 0; font-size: 22rpx; padding: 6rpx 14rpx; border-radius: 8rpx;
  background: #F2F3F5; color: #606266;
}
.ev-type.ev-ing { background: #E7F8EE; color: #52C41A; }
.ev-type.ev-wait { background: #FFF7E6; color: #FA8C16; }
.ev-type.ev-match { background: #ECF0FF; color: #4A6FE3; }
.ev-title { font-size: 27rpx; color: #303133; line-height: 1.4; }
.arrow { color: #C0C4CC; font-size: 32rpx; margin-left: 16rpx; }
</style>
