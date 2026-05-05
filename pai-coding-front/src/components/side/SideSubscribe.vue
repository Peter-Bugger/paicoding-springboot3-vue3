<template>
  <div class="side-card">
    <h5 class="side-card-title">订阅</h5>
    <div class="side-sub-content">
      <p class="side-sub-desc">
        微信搜 <em class="side-sub-em">{{side.subTitle}}</em> 回复关键字 <em class="side-sub-em">110</em>
      </p>
      <div class="side-sub-prize">
        <span class="side-sub-prize-tag">将获得</span>
        <div class="side-sub-prize-row">
          <div class="side-sub-prize-info">
            <p class="side-sub-prize-text">{{side.content}}</p>
            <p class="side-sub-prize-desc">编程汇良心推荐</p>
          </div>
          <img v-if="side.img" :src="side.img" :alt="side.subTitle" class="side-sub-qr" />
        </div>
      </div>
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

.side-sub-content {
  display: flex;
  flex-direction: column;
  gap: 0.6rem;
}

.side-sub-desc {
  font-size: 0.75rem;
  color: var(--pai-color-3-gray, #6b7084);
  margin: 0;
  line-height: 1.5;
}

.side-sub-em {
  font-style: normal;
  font-weight: 600;
  color: var(--pai-brand-1-normal, #2d7cf6);
}

.side-sub-prize {
  background: var(--pai-bg-light-1, #f4f6fa);
  border-radius: 10px;
  padding: 0.75rem;
}

.side-sub-prize-tag {
  display: inline-block;
  font-size: 0.65rem;
  font-weight: 600;
  color: #fff;
  background: linear-gradient(135deg, #f59e0b, #d97706);
  padding: 0.15rem 0.5rem;
  border-radius: 4px;
  margin-bottom: 0.5rem;
}

.side-sub-prize-row {
  display: flex;
  gap: 0.75rem;
  align-items: center;
}

.side-sub-prize-info {
  flex: 1;
  min-width: 0;
}

.side-sub-prize-text {
  font-size: 0.78rem;
  font-weight: 500;
  color: var(--pai-color-4-gray, #484d5e);
  margin: 0 0 0.15rem;
}

.side-sub-prize-desc {
  font-size: 0.65rem;
  color: var(--pai-color-999-gray, #8c8f9c);
  margin: 0;
}

.side-sub-qr {
  width: 72px;
  height: 72px;
  border-radius: 8px;
  object-fit: cover;
  flex-shrink: 0;
  border: 1px solid var(--pai-border-color-1, #d6dae6);
}
</style>
