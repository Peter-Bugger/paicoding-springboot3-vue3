<template>
  <!-- @PRD: US3 — AC3.1~3.8: 面板状态路由：loading/streaming/done/error/timeout -->
  <!-- @PRD: US5 — AC5.2: 移动端 bottom drawer 适配（<=768px） -->

  <!-- 移动端：el-drawer 底部滑入 -->
  <el-drawer
    v-if="isMobile"
    :model-value="visible"
    direction="btt"
    :size="'80vh'"
    :with-header="false"
    @close="handleClose"
    class="ai-panel-drawer"
  >
    <div class="ai-panel ai-panel-mobile">
      <!-- 头部 -->
      <div class="ai-panel-header">
        <div class="ai-panel-header-left">
          <svg class="ai-panel-header-icon" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <path d="M12 2L2 7l10 5 10-5-10-5z" />
            <path d="M2 17l10 5 10-5" />
            <path d="M2 12l10 5 10-5" />
          </svg>
          <span class="ai-panel-header-title">AI 解读</span>
        </div>
        <div class="ai-panel-selected-text" :title="selectedText">
          <span class="ai-panel-selected-label">选中:</span>
          {{ textSummary }}
        </div>
        <button class="ai-panel-close-btn" @click="handleClose" title="关闭">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <line x1="18" y1="6" x2="6" y2="18" />
            <line x1="6" y1="6" x2="18" y2="18" />
          </svg>
        </button>
      </div>
      <div class="ai-panel-body">
        <!-- Loading 状态 -->
        <div v-if="state === 'loading'" class="ai-state-container">
          <div class="ai-state-loading-icon">
            <el-icon :size="28" class="is-loading"><Loading /></el-icon>
          </div>
          <p class="ai-state-text">AI 正在解读...</p>
          <el-skeleton :rows="6" animated />
        </div>

        <!-- Streaming 状态 -->
        <div v-else-if="state === 'streaming'" class="ai-state-container">
          <div class="ai-state-streaming-indicator">
            <span class="ai-streaming-dot"></span>
            <span class="ai-state-text ai-state-text-small">AI 正在生成...</span>
          </div>
          <div class="ai-markdown-wrap">
            <MdPreview :model-value="content" />
          </div>
        </div>

        <!-- Done 状态 -->
        <div v-else-if="state === 'done'" class="ai-state-container">
          <div class="ai-state-done-actions">
            <el-button size="small" type="primary" plain @click="copyContent">
              <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" style="margin-right:4px">
                <rect x="9" y="9" width="13" height="13" rx="2" ry="2" />
                <path d="M5 15H4a2 2 0 0 1-2-2V4a2 2 0 0 1 2-2h9a2 2 0 0 1 2 2v1" />
              </svg>
              复制结果
            </el-button>
          </div>
          <div class="ai-markdown-wrap">
            <MdPreview :model-value="content" />
          </div>
        </div>

        <!-- Error 状态 -->
        <div v-else-if="state === 'error'" class="ai-state-container">
          <el-alert
            title="AI 解读失败"
            :description="errorMessage || '服务异常，请稍后重试'"
            type="error"
            show-icon
            :closable="false"
          />
          <div class="ai-state-retry">
            <el-button type="primary" @click="handleRetry">重新解读</el-button>
          </div>
        </div>

        <!-- Timeout 状态 -->
        <div v-else-if="state === 'timeout'" class="ai-state-container">
          <el-alert
            title="AI 响应超时"
            :description="errorMessage || 'AI 响应超过 30 秒，请稍后重试'"
            type="warning"
            show-icon
            :closable="false"
          />
          <div class="ai-state-retry">
            <el-button type="primary" @click="handleRetry">重新解读</el-button>
          </div>
        </div>
      </div>
      <!-- Footer -->
      <div class="ai-panel-footer">
        AI 解读基于原文，不编造不杜撰，仅供参考
      </div>
    </div>
  </el-drawer>

  <!-- 桌面端：自定义右侧滑入面板，支持拖拽调整宽度 -->
  <Transition name="ai-panel-slide">
    <div v-if="!isMobile && visible" class="ai-panel ai-panel-desktop" :style="{ width: panelWidth + 'px' }">
      <!-- 拖拽手柄：左侧边缘 -->
      <div
        class="ai-panel-resize-handle"
        @mousedown.prevent="startResize"
      >
        <div class="ai-panel-resize-handle-line"></div>
      </div>
      <!-- 头部 -->
      <div class="ai-panel-header">
        <div class="ai-panel-header-left">
          <svg class="ai-panel-header-icon" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <path d="M12 2L2 7l10 5 10-5-10-5z" />
            <path d="M2 17l10 5 10-5" />
            <path d="M2 12l10 5 10-5" />
          </svg>
          <span class="ai-panel-header-title">AI 解读</span>
        </div>
        <button class="ai-panel-close-btn" @click="handleClose" title="关闭">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <line x1="18" y1="6" x2="6" y2="18" />
            <line x1="6" y1="6" x2="18" y2="18" />
          </svg>
        </button>
      </div>

      <!-- 选中文本摘要 -->
      <div class="ai-panel-selected" :title="selectedText">
        <span class="ai-panel-selected-label">选中文本:</span>
        {{ textSummary }}
      </div>

      <!-- 主体 -->
      <div class="ai-panel-body">
        <!-- Loading 状态 -->
        <div v-if="state === 'loading'" class="ai-state-container">
          <div class="ai-state-loading-icon">
            <el-icon :size="28" class="is-loading"><Loading /></el-icon>
          </div>
          <p class="ai-state-text">AI 正在解读...</p>
          <el-skeleton :rows="6" animated />
        </div>

        <!-- Streaming 状态 -->
        <div v-else-if="state === 'streaming'" class="ai-state-container">
          <div class="ai-state-streaming-indicator">
            <span class="ai-streaming-dot"></span>
            <span class="ai-state-text ai-state-text-small">AI 正在生成...</span>
          </div>
          <div class="ai-markdown-wrap">
            <MdPreview :model-value="content" />
          </div>
        </div>

        <!-- Done 状态 -->
        <div v-else-if="state === 'done'" class="ai-state-container">
          <div class="ai-state-done-actions">
            <el-button size="small" type="primary" plain @click="copyContent">
              <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" style="margin-right:4px">
                <rect x="9" y="9" width="13" height="13" rx="2" ry="2" />
                <path d="M5 15H4a2 2 0 0 1-2-2V4a2 2 0 0 1 2-2h9a2 2 0 0 1 2 2v1" />
              </svg>
              复制结果
            </el-button>
          </div>
          <div class="ai-markdown-wrap">
            <MdPreview :model-value="content" />
          </div>
        </div>

        <!-- Error 状态 -->
        <div v-else-if="state === 'error'" class="ai-state-container">
          <el-alert
            title="AI 解读失败"
            :description="errorMessage || '服务异常，请稍后重试'"
            type="error"
            show-icon
            :closable="false"
          />
          <div class="ai-state-retry">
            <el-button type="primary" @click="handleRetry">重新解读</el-button>
          </div>
        </div>

        <!-- Timeout 状态 -->
        <div v-else-if="state === 'timeout'" class="ai-state-container">
          <el-alert
            title="AI 响应超时"
            :description="errorMessage || 'AI 响应超过 30 秒，请稍后重试'"
            type="warning"
            show-icon
            :closable="false"
          />
          <div class="ai-state-retry">
            <el-button type="primary" @click="handleRetry">重新解读</el-button>
          </div>
        </div>
      </div>

      <!-- Footer 免责声明 -->
      <div class="ai-panel-footer">
        AI 解读基于原文，不编造不杜撰，仅供参考
      </div>
    </div>
  </Transition>
</template>

<script setup lang="ts">
// @PRD: US3 — AC3.1~3.8: 面板状态管理：loading/streaming/done/error/timeout
// @PRD: US3 — AC3.7: 复制功能
// @PRD: US3 — AC3.8: 关闭功能

import { ref, computed, watch, onMounted, onUnmounted } from 'vue'
import { MdPreview } from 'md-editor-v3'
import { Loading } from '@element-plus/icons-vue'
import { messageTip } from '@/util/utils'

const props = defineProps<{
  visible: boolean;
  state: 'loading' | 'streaming' | 'done' | 'error' | 'timeout';
  content: string;
  errorMessage: string;
  selectedText: string;
}>();

const emit = defineEmits<{
  (e: 'close'): void;
  (e: 'retry'): void;
}>();

// 选中文本摘要：最多显示 50 字符
const textSummary = computed(() => {
  if (!props.selectedText) return '';
  const text = props.selectedText.replace(/\s+/g, ' ').trim();
  return text.length > 50 ? text.slice(0, 50) + '...' : text;
});

const handleClose = () => {
  emit('close');
};

const handleRetry = () => {
  emit('retry');
};

// 复制功能（含降级方案）
// @PRD: US3 — AC3.7: 复制按钮将解读结果复制到剪贴板
const copyContent = async () => {
  try {
    await navigator.clipboard.writeText(props.content);
    messageTip('已复制到剪贴板', 'success');
  } catch {
    // 降级：使用 textarea 复制
    const textarea = document.createElement('textarea');
    textarea.value = props.content;
    textarea.style.position = 'fixed';
    textarea.style.opacity = '0';
    document.body.appendChild(textarea);
    textarea.select();
    try {
      document.execCommand('copy');
      messageTip('已复制到剪贴板', 'success');
    } catch {
      messageTip('复制失败，请手动复制', 'warning');
    }
    document.body.removeChild(textarea);
  }
};

// 移动端检测（<=768px 为移动端）
const isMobile = ref(false);

const checkMobile = () => {
  isMobile.value = window.innerWidth <= 768;
};

// 面板拖拽调整宽度
const PANEL_WIDTH_KEY = 'ai_interpreter_panel_width'
const MIN_PANEL_WIDTH = 300
const MAX_PANEL_WIDTH_RATIO = 0.9

const getDefaultWidth = (): number => {
  const saved = localStorage.getItem(PANEL_WIDTH_KEY)
  if (saved) {
    const parsed = parseInt(saved, 10)
    if (!isNaN(parsed) && parsed >= MIN_PANEL_WIDTH) return parsed
  }
  return 420
}

const panelWidth = ref(getDefaultWidth())
const isDragging = ref(false)

const startResize = (e: MouseEvent) => {
  isDragging.value = true
  document.body.style.userSelect = 'none'
  document.body.style.cursor = 'col-resize'

  const startX = e.clientX
  const startWidth = panelWidth.value

  const onMouseMove = (moveEvent: MouseEvent) => {
    const deltaX = startX - moveEvent.clientX
    const newWidth = Math.min(
      Math.max(startWidth + deltaX, MIN_PANEL_WIDTH),
      window.innerWidth * MAX_PANEL_WIDTH_RATIO
    )
    panelWidth.value = newWidth
  }

  const onMouseUp = () => {
    isDragging.value = false
    document.body.style.userSelect = ''
    document.body.style.cursor = ''
    document.removeEventListener('mousemove', onMouseMove)
    document.removeEventListener('mouseup', onMouseUp)
    localStorage.setItem(PANEL_WIDTH_KEY, String(panelWidth.value))
  }

  document.addEventListener('mousemove', onMouseMove)
  document.addEventListener('mouseup', onMouseUp)
}

// 面板关闭时重置为默认宽度
watch(() => props.visible, (visible) => {
  if (!visible) {
    panelWidth.value = getDefaultWidth()
  }
})

onMounted(() => {
  checkMobile();
  window.addEventListener('resize', checkMobile);
});

onUnmounted(() => {
  window.removeEventListener('resize', checkMobile);
});
</script>

<style scoped>
/* ── 面板容器 ── */
.ai-panel {
  display: flex;
  flex-direction: column;
  height: 100%;
  background: var(--pai-bg-white-fff, #ffffff);
  font-size: 14px;
  color: var(--pai-color-3-black, #1e2029);
}

/* 桌面端面板：宽度由 JS 动态控制 */
.ai-panel-desktop {
  position: fixed;
  top: 0;
  right: 0;
  min-width: 300px;
  max-width: 90vw;
  height: 100vh;
  z-index: 1500;
  box-shadow: -4px 0 24px rgba(26, 29, 39, 0.12);
  border-left: 1px solid var(--pai-border-color-1, #d6dae6);
}

/* 拖拽手柄：面板左边缘 */
.ai-panel-resize-handle {
  position: absolute;
  top: 0;
  left: -4px;
  width: 8px;
  height: 100%;
  cursor: col-resize;
  z-index: 10;
  display: flex;
  align-items: center;
  justify-content: center;
}

.ai-panel-resize-handle:hover .ai-panel-resize-handle-line {
  background: var(--pai-brand-1-normal, #6366f1);
  opacity: 1;
}

.ai-panel-resize-handle-line {
  width: 3px;
  height: 48px;
  border-radius: 2px;
  background: var(--pai-border-color-1, #d6dae6);
  opacity: 0;
  transition: opacity 0.2s, background 0.2s;
}

.ai-panel-desktop:hover .ai-panel-resize-handle-line {
  opacity: 0.6;
}

/* ── 头部 ── */
.ai-panel-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 14px 20px;
  border-bottom: 1px solid var(--pai-bg-light-2, #eef1f7);
  flex-shrink: 0;
  gap: 8px;
}

.ai-panel-header-left {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-shrink: 0;
}

.ai-panel-header-icon {
  color: var(--pai-brand-1-normal, #6366f1);
}

.ai-panel-header-title {
  font-size: 15px;
  font-weight: 600;
  white-space: nowrap;
}

/* 选中文本摘要（移动端头部内嵌） */
.ai-panel-selected-text {
  flex: 1;
  font-size: 11px;
  color: var(--pai-color-999-gray, #8c8f9c);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  min-width: 0;
}

.ai-panel-selected-label {
  font-weight: 500;
  margin-right: 2px;
}

/* 选中文本摘要（桌面端单独行） */
.ai-panel-selected {
  padding: 8px 20px;
  font-size: 12px;
  color: var(--pai-color-999-gray, #8c8f9c);
  background: var(--pai-bg-light-1, #f4f6fa);
  border-bottom: 1px solid var(--pai-bg-light-2, #eef1f7);
  flex-shrink: 0;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  word-break: break-all;
}

.ai-panel-close-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 28px;
  height: 28px;
  border: none;
  background: var(--pai-bg-light-1, #f4f6fa);
  border-radius: 6px;
  cursor: pointer;
  color: var(--pai-color-4-gray, #484d5e);
  transition: background 0.15s;
  flex-shrink: 0;
}

.ai-panel-close-btn:hover {
  background: var(--pai-bg-light-2, #eef1f7);
  color: var(--pai-color-3-black, #1e2029);
}

/* ── 主体 ── */
.ai-panel-body {
  flex: 1;
  overflow-y: auto;
  overflow-x: auto;
  padding: 20px;
}

/* ── 状态容器 ── */
.ai-state-container {
  min-height: 200px;
}

/* Loading 状态 */
.ai-state-loading-icon {
  display: flex;
  justify-content: center;
  margin-bottom: 12px;
  color: var(--pai-brand-1-normal, #6366f1);
}

.ai-state-text {
  text-align: center;
  font-size: 14px;
  color: var(--pai-color-4-gray, #484d5e);
  margin: 0 0 16px;
}

.ai-state-text-small {
  font-size: 12px;
  margin: 0;
}

/* Streaming 状态 */
.ai-state-streaming-indicator {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 12px;
  padding: 6px 12px;
  background: var(--pai-bg-light-1, #f4f6fa);
  border-radius: 8px;
}

.ai-streaming-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #22c55e;
  animation: ai-pulse 1.5s ease-in-out infinite;
}

@keyframes ai-pulse {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.4; }
}

/* Done 状态 - 操作栏 */
.ai-state-done-actions {
  display: flex;
  justify-content: flex-end;
  margin-bottom: 12px;
}

/* Error / Timeout 重试按钮 */
.ai-state-retry {
  margin-top: 16px;
  display: flex;
  justify-content: center;
}

/* Markdown 预览容器 */
.ai-markdown-wrap {
  width: 100%;
  min-width: fit-content;
  overflow-wrap: break-word;
}

/* Footer */
.ai-panel-footer {
  padding: 10px 20px;
  border-top: 1px solid var(--pai-bg-light-2, #eef1f7);
  font-size: 11px;
  color: var(--pai-color-5-gray, #d0d3dd);
  text-align: center;
  flex-shrink: 0;
}

/* ── Slide transition ── */
.ai-panel-slide-enter-active {
  transition: transform 0.25s ease;
}

.ai-panel-slide-leave-active {
  transition: transform 0.2s ease;
}

.ai-panel-slide-enter-from,
.ai-panel-slide-leave-to {
  transform: translateX(100%);
}

/* ── Mobile drawer ── */
.ai-panel-mobile {
  height: 100%;
}

.ai-panel-drawer :deep(.el-drawer__body) {
  padding: 0;
}
</style>

<style>
/* 全局样式：确保 MdPreview 在面板内无额外边距 */
.ai-markdown-wrap .md-editor-preview {
  padding: 0 !important;
}
</style>
