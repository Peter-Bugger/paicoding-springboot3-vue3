<template>
  <div class="message-input-area">
    <el-input
      v-model="text"
      type="textarea"
      :rows="3"
      :maxlength="maxLength"
      resize="none"
      :disabled="disabled"
      placeholder="输入消息..."
      class="message-input-field"
      @keydown.enter.exact.prevent="handleSend"
      @keydown.enter.shift.exact="() => {}"
    />
    <div class="message-input-footer">
      <span class="message-input-count">{{ text.length }}/{{ maxLength }}</span>
      <el-button
        type="primary"
        size="small"
        :disabled="text.trim().length === 0 || loading"
        :loading="loading"
        @click="handleSend"
      >
        发送
      </el-button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'

const props = defineProps<{
  maxLength?: number
  loading?: boolean
  disabled?: boolean
}>()

const emit = defineEmits<{
  send: [content: string]
}>()

const text = ref('')

function handleSend() {
  const content = text.value.trim()
  if (content.length === 0 || props.loading || props.disabled) return
  emit('send', content)
  text.value = ''
}
</script>

<style scoped>
.message-input-area {
  border-top: 1px solid var(--pai-border-color-1, #eef1f7);
  padding: 0.75rem 1rem;
  background: var(--pai-bg-white-fff, #ffffff);
}

.message-input-field :deep(.el-textarea__inner) {
  border-radius: 10px;
  border: 1.5px solid var(--pai-border-color-1, #d6dae6);
  font-size: 0.88rem;
  font-family: inherit;
  resize: none;
  transition: border-color 0.2s, box-shadow 0.2s;
  background: var(--pai-bg-light-1, #f4f6fa);
  color: var(--pai-color-3-black, #1e2029);
}

.message-input-field :deep(.el-textarea__inner:focus) {
  border-color: var(--pai-brand-1-normal);
  box-shadow: 0 0 0 3px rgba(45, 124, 246, 0.1);
}

.message-input-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 0.5rem;
}

.message-input-count {
  font-size: 0.72rem;
  color: var(--pai-color-999-gray, #8c8f9c);
}
</style>
