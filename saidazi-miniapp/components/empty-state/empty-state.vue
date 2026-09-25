<template>
  <view class="empty">
    <text class="empty-icon">{{ icon }}</text>
    <text class="empty-text">{{ text }}</text>
    <text class="empty-sub" v-if="sub">{{ sub }}</text>
    <view class="empty-btn" v-if="btnText" @tap="$emit('action')">{{ btnText }}</view>
  </view>
</template>

<script>
/**
 * 空状态占位组件
 * props: icon 图标字符 / text 主文案 / sub 副文案 / btnText 按钮文案（可选）
 * 事件: action 点击按钮
 */
export default {
  name: 'EmptyState',
  props: {
    icon: { type: String, default: '📭' },
    text: { type: String, default: '暂无数据' },
    sub: { type: String, default: '' },
    btnText: { type: String, default: '' }
  }
}
</script>

<style scoped>
.empty-btn {
  display: inline-block;
  margin-top: 28rpx;
  padding: 14rpx 48rpx;
  background: #ECF0FF;
  color: #4A6FE3;
  border-radius: 40rpx;
  font-size: 26rpx;
}
</style>
