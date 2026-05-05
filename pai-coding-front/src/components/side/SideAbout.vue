<template>
  <div class="side-card">
    <h5 class="side-card-title">{{side.title}}</h5>
    <p class="side-about-content">{{side.content}}</p>
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

.side-about-content {
  font-size: 0.8rem;
  line-height: 1.6;
  color: var(--pai-color-3-gray, #6b7084);
  margin: 0;
}
</style>
