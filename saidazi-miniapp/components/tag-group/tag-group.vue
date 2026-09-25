<template>
  <view class="tag-group">
    <text
      v-for="(t, i) in options"
      :key="i"
      class="tag-selectable"
      :class="{ active: isActive(t) }"
      @tap="$emit('select', t)"
    >{{ t }}</text>
  </view>
</template>

<script>
/**
 * 可选标签组（筛选用）
 * props: options 选项数组（字符串或 {label, value}） / modelValue 当前选中值
 * 事件: select(option)
 */
export default {
  name: 'TagGroup',
  props: {
    options: { type: Array, default: () => [] },
    modelValue: { type: [String, Number], default: '' }
  },
  methods: {
    labelOf(t) { return typeof t === 'object' ? t.label : t },
    valueOf(t) { return typeof t === 'object' ? t.value : t },
    isActive(t) { return this.valueOf(t) === this.modelValue }
  }
}
</script>

<style scoped>
.tag-group { display: flex; flex-wrap: wrap; }
</style>
