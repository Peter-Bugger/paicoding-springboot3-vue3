<template>
  <div class="conversation-item" @click="$emit('click')">
    <el-avatar :size="48" :src="item.targetUser?.photo" class="conversation-item-avatar" />
    <div class="conversation-item-content">
      <div class="conversation-item-top">
        <span class="conversation-item-name">{{ item.targetUser?.userName }}</span>
        <span class="conversation-item-time">{{ formatTime(item.lastMessage?.createTime) }}</span>
      </div>
      <div class="conversation-item-bottom">
        <span class="conversation-item-preview">{{ item.lastMessage?.content || '' }}</span>
        <el-badge v-if="item.unreadCount > 0" :value="item.unreadCount > 99 ? '99+' : item.unreadCount" class="conversation-item-badge" />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import type { ConversationItem } from '@/http/ResponseTypes/MsgTypes'
import { format } from 'date-fns'

defineProps<{
  item: ConversationItem
}>()

defineEmits<{
  click: []
}>()

function formatTime(dateStr: string | undefined): string {
  if (!dateStr) return ''
  try {
    const date = new Date(dateStr)
    const now = new Date()
    const isToday = date.toDateString() === now.toDateString()
    if (isToday) {
      return format(date, 'HH:mm')
    }
    const isYesterday = new Date(now.getTime() - 86400000).toDateString() === date.toDateString()
    if (isYesterday) {
      return '昨天'
    }
    return format(date, 'MM-dd')
  } catch {
    return ''
  }
}
</script>

<style scoped>
.conversation-item {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  padding: 0.75rem 1rem;
  cursor: pointer;
  transition: background 0.2s ease;
  border-bottom: 1px solid var(--pai-border-color-1, #eef1f7);
}

.conversation-item:hover {
  background: var(--pai-brand-7-light, rgba(45, 124, 246, 0.08));
}

.conversation-item:active {
  background: var(--pai-bg-light-1, #f4f6fa);
}

.conversation-item-avatar {
  flex-shrink: 0;
}

.conversation-item-content {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 0.25rem;
}

.conversation-item-top {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.conversation-item-name {
  font-size: 0.9rem;
  font-weight: 600;
  color: var(--pai-color-3-black, #1e2029);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.conversation-item-time {
  font-size: 0.72rem;
  color: var(--pai-color-999-gray, #8c8f9c);
  flex-shrink: 0;
  margin-left: 0.5rem;
}

.conversation-item-bottom {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 0.5rem;
}

.conversation-item-preview {
  font-size: 0.8rem;
  color: var(--pai-color-999-gray, #8c8f9c);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  flex: 1;
}

.conversation-item-badge {
  flex-shrink: 0;
}
</style>
