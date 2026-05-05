<template>
  <div class="side-card">
    <h5 class="side-card-title">{{side.title}}</h5>
    <div class="side-article-list">
      <a
        v-for="(item, id) in side.items"
        :key="id"
        :href="item.url"
        class="side-article-item"
      >
        <span class="side-article-num" :class="{ 'side-article-num--hot': id < 3 }">
          {{(id + 1).toString(10)}}
        </span>
        <span class="side-article-title">{{item.title}}</span>
      </a>
    </div>
  </div>
</template>

<script setup lang="ts">
import type { SideBarItem } from '@/http/ResponseTypes/SideBarItemType'

defineProps<{
  side: SideBarItem
}>()
</script>

<style scoped>
.side-card {
  background: var(--pai-bg-white-fff, #ffffff);
  border-radius: 12px;
  padding: 1.25rem;
  box-shadow: var(--card-shadow, 0 2px 8px rgba(26, 29, 39, 0.06));
}

.side-card-title {
  font-size: 0.85rem;
  font-weight: 600;
  color: var(--pai-color-3-black, #1e2029);
  margin: 0 0 0.75rem;
  padding-bottom: 0.6rem;
  border-bottom: 2px solid var(--pai-brand-7-light, rgba(45, 124, 246, 0.12));
  letter-spacing: 0.02em;
}

.side-article-list {
  display: flex;
  flex-direction: column;
  gap: 0.35rem;
}

.side-article-item {
  display: flex;
  align-items: center;
  gap: 0.6rem;
  padding: 0.4rem 0.5rem;
  border-radius: 8px;
  text-decoration: none;
  transition: background 0.2s ease;
}

.side-article-item:hover {
  background: var(--pai-bg-light-1, #f4f6fa);
}

.side-article-num {
  flex-shrink: 0;
  width: 20px;
  height: 20px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 0.7rem;
  font-weight: 600;
  color: var(--pai-color-999-gray, #8c8f9c);
  background: var(--pai-color-6-gray, #f0f2f6);
  border-radius: 4px;
  font-family: 'JetBrains Mono', monospace;
}

.side-article-num--hot {
  color: #fff;
  background: var(--pai-brand-1-normal, #2d7cf6);
}

.side-article-title {
  font-size: 0.78rem;
  color: var(--pai-color-4-gray, #484d5e);
  line-height: 1.4;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.side-article-item:hover .side-article-title {
  color: var(--pai-brand-1-normal, #2d7cf6);
}
</style>
