<template>
  <div class="history-card">
    <h3 class="history-title">创作历程</h3>
    <div v-if="user.userHome.yearArticleList.length > 0" class="history-list">
      <div class="history-item" v-for="(year, id) in user.userHome.yearArticleList" :key="id">
        <span class="history-year">{{year.year}}</span>
        <span class="history-bar">
          <span class="history-bar-fill" :style="{ width: Math.min(year.articleCount / 20 * 100, 100) + '%' }"></span>
        </span>
        <span class="history-count">{{year.articleCount}} 篇</span>
      </div>
    </div>
    <div v-else class="history-empty">
      暂未发布任何内容哦~
    </div>
  </div>
</template>

<script setup lang="ts">
import type { UserHomeInfoResponseType } from '@/http/ResponseTypes/UserHomeInfoResponseType'

defineProps<{
  user: UserHomeInfoResponseType
}>()
</script>

<style scoped>
.history-card {
  background: var(--pai-bg-white-fff, #ffffff);
  border-radius: 16px;
  padding: 1.25rem 1.5rem;
  box-shadow: 0 1px 3px rgba(26, 29, 39, 0.04);
}

.history-title {
  font-size: 0.9rem;
  font-weight: 700;
  color: var(--pai-color-3-black, #1e2029);
  margin: 0 0 1rem;
  padding-bottom: 0.75rem;
  border-bottom: 1px solid var(--pai-bg-light-2, #eef1f7);
}

.history-list {
  display: flex;
  flex-direction: column;
  gap: 0.6rem;
}

.history-item {
  display: flex;
  align-items: center;
  gap: 0.6rem;
  font-size: 0.82rem;
}

.history-year {
  font-family: 'JetBrains Mono', monospace;
  font-weight: 600;
  color: var(--pai-color-4-gray, #484d5e);
  min-width: 3rem;
}

.history-bar {
  flex: 1;
  height: 6px;
  background: var(--pai-bg-light-2, #eef1f7);
  border-radius: 3px;
  overflow: hidden;
}

.history-bar-fill {
  display: block;
  height: 100%;
  background: linear-gradient(90deg, var(--pai-brand-1-normal), var(--pai-brand-2-hover, #4a8ff7));
  border-radius: 3px;
  transition: width 0.6s ease;
}

.history-count {
  font-family: 'JetBrains Mono', monospace;
  font-size: 0.78rem;
  color: var(--pai-color-999-gray, #8c8f9c);
  min-width: 3.5rem;
  text-align: right;
}

.history-empty {
  font-size: 0.82rem;
  color: var(--pai-color-999-gray, #8c8f9c);
  text-align: center;
  padding: 1rem 0;
}
</style>
