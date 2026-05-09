<template>
  <div class="message-header">
    <button class="message-header-back" @click="$emit('back')">
      <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
        <path d="M19 12H5"/>
        <path d="m12 19-7-7 7-7"/>
      </svg>
    </button>
    <div class="message-header-user" @click="$emit('userClick')">
      <el-avatar :size="36" :src="targetUser.photo" />
      <div class="message-header-info">
        <span class="message-header-name">{{ targetUser.userName }}</span>
      </div>
    </div>
    <div class="message-header-actions">
      <button class="message-header-clear" title="清除会话" @click="$emit('clear')">
        <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
          <path d="M3 6h18"/>
          <path d="M19 6v14c0 1-1 2-2 2H7c-1 0-2-1-2-2V6"/>
          <path d="M8 6V4c0-1 1-2 2-2h4c1 0 2 1 2 2v2"/>
          <line x1="10" y1="11" x2="10" y2="17"/>
          <line x1="14" y1="11" x2="14" y2="17"/>
        </svg>
      </button>
    </div>
  </div>
</template>

<script setup lang="ts">
import type { SimpleUserInfo } from '@/http/ResponseTypes/MsgTypes'

defineProps<{
  targetUser: SimpleUserInfo
}>()

defineEmits<{
  back: []
  userClick: []
  clear: []
}>()
</script>

<style scoped>
.message-header {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.5rem 1rem;
  border-bottom: 1px solid var(--pai-border-color-1, #eef1f7);
  background: var(--pai-bg-white-fff, #ffffff);
  min-height: 56px;
}

.message-header-back {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 36px;
  height: 36px;
  border: none;
  background: transparent;
  color: var(--pai-color-4-gray, #484d5e);
  border-radius: 8px;
  cursor: pointer;
  transition: background 0.2s, color 0.2s;
  flex-shrink: 0;
}

.message-header-back:hover {
  background: var(--pai-brand-7-light, rgba(45, 124, 246, 0.12));
  color: var(--pai-brand-1-normal);
}

.message-header-user {
  display: flex;
  align-items: center;
  gap: 0.6rem;
  cursor: pointer;
  padding: 0.3rem 0.5rem;
  border-radius: 8px;
  transition: background 0.2s;
}

.message-header-user:hover {
  background: var(--pai-bg-light-1, #f4f6fa);
}

.message-header-info {
  display: flex;
  flex-direction: column;
}

.message-header-name {
  font-size: 0.9rem;
  font-weight: 600;
  color: var(--pai-color-3-black, #1e2029);
}

.message-header-actions {
  margin-left: auto;
  display: flex;
  align-items: center;
}

.message-header-clear {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 36px;
  height: 36px;
  border: none;
  background: transparent;
  color: var(--pai-color-4-gray, #484d5e);
  border-radius: 8px;
  cursor: pointer;
  transition: background 0.2s, color 0.2s;
  flex-shrink: 0;
}

.message-header-clear:hover {
  background: var(--pai-danger-bg, rgba(245, 108, 108, 0.12));
  color: var(--el-color-danger, #f56c6c);
}
</style>
