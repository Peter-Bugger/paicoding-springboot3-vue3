<template>
  <div class="message-bubble-row" :class="{ 'message-bubble-row--own': isOwn }">
    <!-- 对方头像（左侧） -->
    <template v-if="!isOwn">
      <img
        v-if="message.fromUserPhoto"
        class="message-avatar"
        :src="message.fromUserPhoto"
        :alt="message.fromUserName"
      />
      <div v-else class="message-avatar message-avatar--placeholder" />
    </template>

    <div class="message-bubble-wrapper" :class="{ 'message-bubble-wrapper--own': isOwn }">
      <span v-if="!isOwn && message.fromUserName" class="message-nickname">
        {{ message.fromUserName }}
      </span>

      <div class="message-bubble" :class="{ 'message-bubble--own': isOwn, 'message-bubble--other': !isOwn, 'message-bubble--failed': message.status === 'FAILED' }">
        <span class="message-bubble-text">{{ message.content }}</span>
      </div>
      <span class="message-bubble-time">{{ formatTime(message.createTime) }}</span>
      <span v-if="isOwn && message.status === 'FAILED'" class="message-failed-tag">
        <el-icon :size="14"><WarningFilled /></el-icon> 发送失败
      </span>
    </div>

    <!-- 自己头像（右侧） -->
    <template v-if="isOwn">
      <img
        v-if="message.fromUserPhoto"
        class="message-avatar"
        :src="message.fromUserPhoto"
        :alt="message.fromUserName"
      />
      <div v-else class="message-avatar message-avatar--placeholder" />
    </template>

  </div>
</template>

<script setup lang="ts">
import type { MessageItem } from '@/http/ResponseTypes/MsgTypes'
import { format } from 'date-fns'
import { WarningFilled } from '@element-plus/icons-vue'

defineProps<{
  message: MessageItem
  isOwn: boolean
}>()

function formatTime(dateStr: string): string {
  try {
    return format(new Date(dateStr), 'HH:mm')
  } catch {
    return ''
  }
}
</script>

<style scoped>
.message-bubble-row {
  display: flex;
  align-items: flex-start;
  padding: 0.25rem 1rem;
  gap: 0.5rem;
}

.message-bubble-row--own {
  justify-content: flex-end;
}

.message-bubble-row--own .message-avatar {
  margin-top: 0.15rem;
}

.message-avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  object-fit: cover;
  flex-shrink: 0;
  margin-top: 0.15rem;
}

.message-avatar--placeholder {
  background: var(--pai-bg-light-2, #eef1f7);
  flex-shrink: 0;
}

.message-bubble-wrapper {
  max-width: calc(70% - 36px);
  display: flex;
  flex-direction: column;
  gap: 0.2rem;
}

.message-bubble-wrapper--own {
  max-width: 70%;
  align-items: flex-end;
}

.message-nickname {
  font-size: 0.75rem;
  color: var(--pai-color-999-gray, #8c8f9c);
  padding: 0 0.25rem;
}

.message-bubble {
  padding: 0.6rem 1rem;
  border-radius: 12px;
  font-size: 0.88rem;
  line-height: 1.5;
  word-break: break-word;
  white-space: pre-wrap;
}

.message-bubble--own {
  background: var(--pai-brand-1-normal);
  color: #fff;
  border-bottom-right-radius: 4px;
}

.message-bubble--other {
  background: var(--pai-bg-light-1, #f4f6fa);
  color: var(--pai-color-3-black, #1e2029);
  border-bottom-left-radius: 4px;
}

/* [已修复] 发送失败状态样式 */
.message-bubble--failed {
  border: 1.5px solid #f56c6c !important;
  box-shadow: 0 0 0 1px rgba(245, 108, 108, 0.15);
}

.message-failed-tag {
  display: flex;
  align-items: center;
  gap: 3px;
  font-size: 0.7rem;
  color: #f56c6c;
  padding: 0 0.25rem;
  margin-top: 0.1rem;
}

.message-bubble-text {
  display: block;
}

.message-bubble-time {
  font-size: 0.65rem;
  color: var(--pai-color-999-gray, #8c8f9c);
  padding: 0 0.25rem;
}
</style>
