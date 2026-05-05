<template>
  <div class="side-card">
    <h5 class="side-card-title">
      <a :href="'/rank/month'" class="side-rank-title-link">
        <span>{{side.title}}</span>
        <small class="side-rank-more"> &gt;&gt;&gt; </small>
      </a>
    </h5>
    <div class="side-rank-list">
      <a
        v-for="(item, id) in side.items"
        :key="id"
        :href="'/user/' + item.url"
        class="side-rank-item"
      >
        <span class="side-rank-num" :class="{ 'side-rank-num--gold': id === 0, 'side-rank-num--silver': id === 1, 'side-rank-num--bronze': id === 2 }">
          {{(id + 1).toString().padStart(2, '0')}}
        </span>
        <span class="side-rank-name">{{item.name}}</span>
        <span class="side-rank-time">{{item.time}}</span>
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

.side-rank-title-link {
  text-decoration: none;
  color: inherit;
  display: flex;
  align-items: center;
  gap: 0.25rem;
}

.side-rank-title-link:hover {
  color: var(--pai-brand-1-normal, #2d7cf6);
}

.side-rank-more {
  font-size: 0.65rem;
  color: var(--pai-color-999-gray, #8c8f9c);
}

.side-rank-list {
  display: flex;
  flex-direction: column;
  gap: 0.3rem;
}

.side-rank-item {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.35rem 0.5rem;
  border-radius: 8px;
  text-decoration: none;
  transition: background 0.2s ease;
}

.side-rank-item:hover {
  background: var(--pai-bg-light-1, #f4f6fa);
}

.side-rank-num {
  flex-shrink: 0;
  width: 24px;
  font-size: 0.7rem;
  font-weight: 700;
  color: var(--pai-color-999-gray, #8c8f9c);
  font-family: 'JetBrains Mono', monospace;
  text-align: center;
}

.side-rank-num--gold { color: #f59e0b; }
.side-rank-num--silver { color: #94a3b8; }
.side-rank-num--bronze { color: #d97706; }

.side-rank-name {
  flex: 1;
  font-size: 0.78rem;
  color: var(--pai-color-4-gray, #484d5e);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.side-rank-item:hover .side-rank-name {
  color: var(--pai-brand-1-normal, #2d7cf6);
}

.side-rank-time {
  flex-shrink: 0;
  font-size: 0.65rem;
  font-style: italic;
  color: var(--pai-color-999-gray, #8c8f9c);
}
</style>
