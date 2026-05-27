<template>
  <!-- @PRD: US5 — AC5.1: 文本选中后浮动按钮展示在选中区域上/下侧 -->
  <Transition name="ai-btn-fade">
    <div
      v-if="show"
      class="ai-interpret-btn"
      :class="{ 'ai-interpret-btn--below': placement === 'bottom' }"
      :style="{ left: x + 'px', top: y + 'px' }"
      @mousedown.prevent
      @click.stop="handleClick"
    >
      <svg
        class="ai-interpret-btn-icon"
        viewBox="0 0 24 24"
        fill="none"
        stroke="currentColor"
        stroke-width="2"
        stroke-linecap="round"
        stroke-linejoin="round"
      >
        <path d="M12 2L2 7l10 5 10-5-10-5z" />
        <path d="M2 17l10 5 10-5" />
        <path d="M2 12l10 5 10-5" />
      </svg>
      <span class="ai-interpret-btn-text">AI 解读</span>
    </div>
  </Transition>
</template>

<script setup lang="ts">
// @PRD: US1 — AC1.1: 选中文本后显示"AI解读"浮动按钮

defineProps<{
  show: boolean;
  x: number;
  y: number;
  placement?: 'top' | 'bottom';
}>();

const emit = defineEmits<{
  (e: 'interpret'): void;
}>();

const handleClick = () => {
  emit('interpret');
};
</script>

<style scoped>
.ai-interpret-btn {
  position: fixed;
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 6px 14px;
  background: linear-gradient(135deg, #6366f1, #8b5cf6);
  color: #fff;
  border: none;
  border-radius: 20px;
  font-size: 13px;
  font-weight: 500;
  cursor: pointer;
  z-index: 2000;
  box-shadow: 0 4px 14px rgba(99, 102, 241, 0.4);
  transition: transform 0.15s ease, box-shadow 0.15s ease;
  white-space: nowrap;
  user-select: none;
  transform: translate(-50%, -100%) translateY(-8px);
}

/* 下方弹出：选中区域太靠上时，按钮显示在选中区域下方 */
.ai-interpret-btn--below {
  transform: translate(-50%, 8px);
}

.ai-interpret-btn:hover {
  transform: translate(-50%, -100%) translateY(-8px) scale(1.05);
  box-shadow: 0 6px 20px rgba(99, 102, 241, 0.5);
}

.ai-interpret-btn--below:hover {
  transform: translate(-50%, 8px) scale(1.05);
}

.ai-interpret-btn:active {
  transform: translate(-50%, -100%) translateY(-8px) scale(0.97);
}

.ai-interpret-btn--below:active {
  transform: translate(-50%, 8px) scale(0.97);
}

.ai-interpret-btn-icon {
  width: 16px;
  height: 16px;
  flex-shrink: 0;
}

.ai-interpret-btn-text {
  line-height: 1;
}

/* 过渡动画：淡入淡出 */
.ai-btn-fade-enter-active {
  transition: opacity 0.2s ease, transform 0.2s ease;
}

.ai-btn-fade-leave-active {
  transition: opacity 0.15s ease, transform 0.15s ease;
}

.ai-btn-fade-enter-from,
.ai-btn-fade-leave-to {
  opacity: 0;
  transform: translate(-50%, -100%) translateY(-4px) scale(0.9);
}
</style>
