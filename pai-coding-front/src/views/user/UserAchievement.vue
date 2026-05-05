<template>
  <div class="achievement-card">
    <h3 class="achievement-title">
      <span class="achievement-user">{{user.userHome.userName}}</span> 的个人成就
    </h3>
    <div class="achievement-list">
      <div class="achievement-item">
        <div class="achievement-item-left">
          <svg class="achievement-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M12 2L2 7l10 5 10-5-10-5z"/><path d="M2 17l10 5 10-5"/><path d="M2 12l10 5 10-5"/></svg>
          <span>已发布文章</span>
        </div>
        <span class="achievement-value">{{user.userHome.articleCount + ' 篇'}}</span>
      </div>
      <div class="achievement-item">
        <div class="achievement-item-left">
          <svg class="achievement-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M14 9V5a3 3 0 0 0-3-3l-4 9v11h11.28a2 2 0 0 0 2-1.7l1.38-9a2 2 0 0 0-2-2.3H14zM7 22H4a2 2 0 0 1-2-2v-7a2 2 0 0 1 2-2h3"/></svg>
          <span>文章被点赞</span>
        </div>
        <span class="achievement-value">{{user.userHome.praiseCount + ' 次'}}</span>
      </div>
      <div class="achievement-item">
        <div class="achievement-item-left">
          <svg class="achievement-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"/><circle cx="12" cy="12" r="3"/></svg>
          <span>文章被阅读</span>
        </div>
        <span class="achievement-value">{{user.userHome.readCount + ' 次'}}</span>
      </div>
      <div class="achievement-item">
        <div class="achievement-item-left">
          <svg class="achievement-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M19 21l-7-5-7 5V5a2 2 0 0 1 2-2h10a2 2 0 0 1 2 2v16z"/></svg>
          <span>文章被收藏</span>
        </div>
        <span class="achievement-value">{{user.userHome.collectionCount + ' 篇'}}</span>
      </div>
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
.achievement-card {
  background: var(--pai-bg-white-fff, #ffffff);
  border-radius: 16px;
  padding: 1.25rem 1.5rem;
  box-shadow: 0 1px 3px rgba(26, 29, 39, 0.04);
}

.achievement-title {
  font-size: 0.9rem;
  font-weight: 700;
  color: var(--pai-color-3-black, #1e2029);
  margin: 0 0 1rem;
  padding-bottom: 0.75rem;
  border-bottom: 1px solid var(--pai-bg-light-2, #eef1f7);
}

.achievement-user {
  color: var(--pai-brand-1-normal);
}

.achievement-list {
  display: flex;
  flex-direction: column;
  gap: 0.6rem;
}

.achievement-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 0.82rem;
}

.achievement-item-left {
  display: flex;
  align-items: center;
  gap: 0.4rem;
  color: var(--pai-color-4-gray, #484d5e);
}

.achievement-icon {
  width: 16px;
  height: 16px;
  color: var(--pai-color-999-gray, #8c8f9c);
  flex-shrink: 0;
}

.achievement-value {
  font-family: 'JetBrains Mono', monospace;
  font-size: 0.8rem;
  font-weight: 600;
  color: var(--pai-color-3-black, #1e2029);
}
</style>
