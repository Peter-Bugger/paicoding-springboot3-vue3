<template>
  <el-dropdown :hide-on-click="false" trigger="click" class="theme-toggle">
    <button class="theme-toggle-btn" :title="themeLabel">
      <!-- Sun icon -->
      <svg v-if="!themeStore.isDark" class="theme-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
        <circle cx="12" cy="12" r="5" />
        <path d="M12 1v2M12 21v2M4.22 4.22l1.42 1.42M18.36 18.36l1.42 1.42M1 12h2M21 12h2M4.22 19.78l1.42-1.42M18.36 5.64l1.42-1.42" />
      </svg>
      <!-- Moon icon -->
      <svg v-else class="theme-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
        <path d="M21 12.79A9 9 0 1 1 11.21 3 7 7 0 0 0 21 12.79z" />
      </svg>
    </button>
    <template #dropdown>
      <el-dropdown-menu>
        <el-dropdown-item :class="{ active: themeStore.mode === 'light' }" @click="themeStore.setMode('light')">
          <svg class="dropdown-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="5"/><path d="M12 1v2M12 21v2M4.22 4.22l1.42 1.42M18.36 18.36l1.42 1.42M1 12h2M21 12h2M4.22 19.78l1.42-1.42M18.36 5.64l1.42-1.42"/></svg>
          <span>亮色模式</span>
        </el-dropdown-item>
        <el-dropdown-item :class="{ active: themeStore.mode === 'dark' }" @click="themeStore.setMode('dark')">
          <svg class="dropdown-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M21 12.79A9 9 0 1 1 11.21 3 7 7 0 0 0 21 12.79z"/></svg>
          <span>暗色模式</span>
        </el-dropdown-item>
        <el-dropdown-item :class="{ active: themeStore.mode === 'auto' }" @click="themeStore.setMode('auto')">
          <svg class="dropdown-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M21 12a9 9 0 1 1-18 0 9 9 0 0 1 18 0z"/><path d="M3.6 9h16.8M3.6 15h16.8"/><path d="M12 3a15 15 0 0 1 0 18 15 15 0 0 1 0-18z"/></svg>
          <span>跟随系统</span>
        </el-dropdown-item>
      </el-dropdown-menu>
    </template>
  </el-dropdown>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useThemeStore } from '@/stores/theme'

const themeStore = useThemeStore()

const themeLabel = computed(() => {
  switch (themeStore.mode) {
    case 'dark': return '切换亮色模式'
    case 'light': return '切换暗色模式'
    case 'auto': return themeStore.isDark ? '暗色（跟随系统）' : '亮色（跟随系统）'
    default: return ''
  }
})
</script>

<style scoped>
.theme-toggle {
  display: inline-flex;
  align-items: center;
}

.theme-toggle-btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 34px;
  height: 34px;
  border: 1.5px solid var(--pai-border-color-1, #d6dae6);
  border-radius: 8px;
  background: transparent;
  cursor: pointer;
  color: var(--pai-color-4-gray, #484d5e);
  transition: all 0.2s ease;
}

.theme-toggle-btn:hover {
  border-color: var(--pai-brand-1-normal);
  color: var(--pai-brand-1-normal);
  background: var(--pai-brand-7-light, rgba(45, 124, 246, 0.12));
}

.theme-icon {
  width: 18px;
  height: 18px;
}

.dropdown-icon {
  width: 16px;
  height: 16px;
  margin-right: 0.5rem;
  flex-shrink: 0;
}

:deep(.el-dropdown-menu__item.active) {
  color: var(--pai-brand-1-normal);
  font-weight: 600;
}
</style>
