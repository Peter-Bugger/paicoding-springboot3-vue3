<template>
  <div class="side-card">
    <div class="side-join-header">
      <img
        v-if="side.icon"
        :src="side.icon"
        alt=""
        class="side-join-icon"
      />
      <span class="side-join-label">扫码进群</span>
    </div>
    <div class="side-join-title">{{side.title}}</div>
    <img
      :src="side.img"
      alt="QR Code"
      class="side-join-qr"
    />
    <p class="side-join-desc">{{side.content}}</p>
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
  text-align: center;
}

.side-join-header {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 0.4rem;
  margin-bottom: 0.3rem;
}

.side-join-icon {
  width: 20px;
  height: 20px;
  border-radius: 4px;
  object-fit: cover;
}

.side-join-label {
  font-size: 0.85rem;
  font-weight: 600;
  color: var(--pai-color-3-black, #1e2029);
  letter-spacing: 0.02em;
}

.side-join-title {
  font-size: 0.75rem;
  color: var(--pai-color-3-gray, #6b7084);
  margin-bottom: 0.75rem;
}

.side-join-qr {
  width: 140px;
  height: 140px;
  border-radius: 8px;
  border: 1px solid var(--pai-border-color-1, #d6dae6);
  object-fit: cover;
  margin-bottom: 0.5rem;
}

.side-join-desc {
  font-size: 0.72rem;
  color: var(--pai-color-999-gray, #8c8f9c);
  margin: 0;
  line-height: 1.5;
}
</style>
