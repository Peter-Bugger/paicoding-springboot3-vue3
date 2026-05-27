<template>
  <!-- @PRD: US1 — AC1.1~1.7: 文本选中→浮动按钮→AI解读→结果展示完整流程 -->
  <!-- @PRD: US5 — AC5.1: 浮动按钮位置跟随选中区域 -->

  <!-- 浮动按钮 -->
  <FloatingButton
    :show="showButton"
    :x="buttonPosition.x"
    :y="buttonPosition.y"
    :placement="buttonPlacement"
    @interpret="handleInterpret"
  />

  <!-- 解读结果面板 -->
  <InterpreterPanel
    :visible="showPanel"
    :state="interpretState"
    :content="interpretContent"
    :error-message="errorMessage"
    :selected-text="selectedText || ''"
    @close="handlePanelClose"
    @retry="handleRetry"
  />
</template>

<script setup lang="ts">
// @PRD: US1 — 选中文本后发起AI解读请求，支持流式返回
// @PRD: US1 — AC1.1: 正常选中显示浮动按钮
// @PRD: US1 — AC1.2: 点击按钮发送请求
// @PRD: US1 — AC1.4: 超长文本（>2000字符）截断处理
// @PRD: US1 — AC1.5: 未选中（空/空白）不显示按钮
// @PRD: US1 — AC1.6: 未登录时触发登录弹窗
// @PRD: US1 — AC1.7: 解读中可重新选择

import { ref, inject, onMounted, onUnmounted, type Ref } from 'vue'
import { useGlobalStore } from '@/stores/global'
import { interpretArticleStream } from '@/api/interpretApi'
import FloatingButton from '@/components/interpreter/FloatingButton.vue'
import InterpreterPanel from '@/components/interpreter/InterpreterPanel.vue'

const props = defineProps<{
  articleId: number;
}>();

const globalStore = useGlobalStore();

// 注入登录弹窗触发函数（由父组件提供）
// @PRD: US1 — AC1.6: 未登录触发登录弹窗
const loginDialogClicked = inject<(() => void) | undefined>('loginDialogClicked');

// ── 状态 ──
const selectedText = ref<string | null>(null);
const buttonPosition = ref({ x: 0, y: 0 });
const buttonPlacement = ref<'top' | 'bottom'>('top');
const showButton = ref(false);
const showPanel = ref(false);
type InterpretState = 'loading' | 'streaming' | 'done' | 'error' | 'timeout';
const interpretState = ref<InterpretState>('loading');
const interpretContent = ref('');
const errorMessage = ref('');

// 防抖定时器
let debounceTimer: ReturnType<typeof setTimeout> | null = null;
// SSE AbortController
let abortController: AbortController | null = null;

// ── 300ms 防抖处理文本选中 ──
// @PRD: US5 — AC5.1: 防抖 300ms 避免频繁触发
const handleMouseUp = (event: MouseEvent) => {
  // 忽略点击 AI 按钮本身的 mouseup（由 handleInterpret 处理）
  if ((event.target as HTMLElement)?.closest('.ai-interpret-btn')) return;

  // 如果面板已打开，点击面板内部不触发选中
  if (showPanel.value) {
    const panelEl = (event.target as HTMLElement)?.closest('.ai-panel, .el-drawer');
    if (panelEl) return;
  }

  if (debounceTimer) {
    clearTimeout(debounceTimer);
  }

  debounceTimer = setTimeout(() => {
    const selection = window.getSelection();
    if (!selection || selection.isCollapsed || !selection.toString().trim()) {
      // @PRD: US1 — AC1.5: 空选中/空白不显示按钮
      showButton.value = false;
      return;
    }

    const text = selection.toString().trim();
    // @PRD: US1 — AC1.4: 超长截断（最多 2000 字符）
    selectedText.value = text.length > 2000 ? text.slice(0, 2000) : text;

    const range = selection.getRangeAt(0);
    const rect = range.getBoundingClientRect();

    // 计算按钮位置：选中区域上方居中，约 8px 间距
    // 按钮实际渲染位置由 CSS transform 处理：translate(-50%, -100%) translateY(-8px)
    // 按钮高度约 30px，所以按钮顶部 = rect.top - 30 - 8
    const BUTTON_ESTIMATED_HEIGHT = 34
    const GAP = 8
    let x = rect.left + rect.width / 2
    let y = rect.top

    // 边界保护：确保按钮不超出视口
    const vw = window.innerWidth
    const minX = 60  // 按钮半宽 + 安全边距
    const maxX = vw - 60
    x = Math.max(minX, Math.min(x, maxX))

    // 如果按钮会超出视口顶部，改为显示在选中区域下方
    if (y - BUTTON_ESTIMATED_HEIGHT - GAP < 0) {
      y = rect.bottom + GAP  // 下方弹出
      buttonPlacement.value = 'bottom'
    } else {
      buttonPlacement.value = 'top'
    }

    buttonPosition.value = { x, y };
    showButton.value = true;
  }, 300);
};

// ── 点击页面空白处隐藏按钮 ──
// @PRD: US1 — AC1.3: 点击空白处取消选中
const handleDocumentClick = (event: MouseEvent) => {
  const target = event.target as HTMLElement;
  // 如果不点击浮动按钮且不点击面板区域
  if (
    !target.closest('.ai-interpret-btn') &&
    !target.closest('.ai-panel') &&
    !target.closest('.el-drawer')
  ) {
    showButton.value = false;
  }
};

// ── 点击"AI解读"按钮 ──
// @PRD: US1 — AC1.2: 点击按钮发送请求
// @PRD: US1 — AC1.6: 未登录触发登录
const handleInterpret = async () => {
  showButton.value = false;

  // 检查登录状态
  if (!globalStore.global.isLogin) {
    if (loginDialogClicked) {
      loginDialogClicked();
    }
    return;
  }

  if (!selectedText.value) return;

  // 打开面板，进入 loading 状态
  showPanel.value = true;
  interpretState.value = 'loading';
  interpretContent.value = '';
  errorMessage.value = '';

  // 发起 SSE 流式请求
  await interpretArticleStream(
    {
      articleId: props.articleId,
      selectedText: selectedText.value,
      startPos: 0,
      endPos: selectedText.value.length
    },
    // onMessage: streaming 状态
    (content: string) => {
      interpretState.value = 'streaming';
      interpretContent.value = content;
    },
    // onComplete: done 状态
    (fullContent: string) => {
      interpretState.value = 'done';
      interpretContent.value = fullContent;
    },
    // onError: error / timeout 状态
    (message: string) => {
      if (message.includes('超时')) {
        interpretState.value = 'timeout';
      } else {
        interpretState.value = 'error';
      }
      errorMessage.value = message;
    }
  );
};

// ── 关闭面板 ──
// @PRD: US3 — AC3.8: 关闭面板
const handlePanelClose = () => {
  showPanel.value = false;
  interpretState.value = 'loading';
  interpretContent.value = '';
  errorMessage.value = '';
  selectedText.value = null;
  showButton.value = false;
};

// ── 重试 ──
// @PRD: US3 — AC3.6: 重试按钮重新发送请求
const handleRetry = () => {
  // 重置状态并重新调用 handleInterpret
  interpretState.value = 'loading';
  interpretContent.value = '';
  errorMessage.value = '';
  handleInterpret();
};

// ── 生命周期 ──
// 使用 capture phase 注册 mouseup，确保在 md-editor-v3 等组件内部
// stopPropagation 之前就能捕获到事件，避免选中文本后按钮不显示
onMounted(() => {
  document.addEventListener('mouseup', handleMouseUp, true);
  document.addEventListener('click', handleDocumentClick, true);
});

onUnmounted(() => {
  document.removeEventListener('mouseup', handleMouseUp, true);
  document.removeEventListener('click', handleDocumentClick, true);
  if (debounceTimer) {
    clearTimeout(debounceTimer);
  }
});
</script>

<style scoped>
/* ArticleInterpreter 本身是浮层组件，不需要布局样式 */
</style>
