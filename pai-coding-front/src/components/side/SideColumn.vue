<template>
  <section class="side-card">
    <h5 class="side-card-title">{{side.title}}</h5>
    <ul class="side-column-list">
      <li v-for="(item, id) in side.items" :key="id" class="side-column-item">
        <a :href="item.url" class="side-column-link">
          <span
            class="side-column-thumb"
            :style="{ backgroundImage: 'url(' + item.img + ')' }"
          ></span>
          <div class="side-column-body">
            <div class="side-column-name">
              <span class="side-column-tag">限免</span>
              <span>{{item.name}}</span>
            </div>
            <p class="side-column-desc">{{item.title}}</p>
          </div>
        </a>
      </li>
    </ul>
  </section>
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

.side-column-list {
  list-style: none;
  margin: 0;
  padding: 0;
  display: flex;
  flex-direction: column;
  gap: 0.6rem;
}

.side-column-item {
  list-style: none;
}

.side-column-link {
  display: flex;
  gap: 0.65rem;
  text-decoration: none;
  padding: 0.5rem;
  border-radius: 10px;
  transition: background 0.2s ease;
}

.side-column-link:hover {
  background: var(--pai-bg-light-1, #f4f6fa);
}

.side-column-thumb {
  flex-shrink: 0;
  width: 48px;
  height: 48px;
  border-radius: 8px;
  background-size: cover;
  background-position: center;
  background-color: var(--pai-color-5-gray, #d0d3dd);
}

.side-column-body {
  flex: 1;
  min-width: 0;
}

.side-column-name {
  font-size: 0.8rem;
  font-weight: 500;
  color: var(--pai-color-4-gray, #484d5e);
  display: flex;
  align-items: center;
  gap: 0.35rem;
  margin-bottom: 0.15rem;
}

.side-column-tag {
  flex-shrink: 0;
  font-size: 0.6rem;
  padding: 0.1rem 0.35rem;
  border-radius: 3px;
  background: linear-gradient(135deg, #ff6b6b, #ee5a24);
  color: #fff;
  font-weight: 600;
}

.side-column-desc {
  font-size: 0.72rem;
  color: var(--pai-color-999-gray, #8c8f9c);
  margin: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
</style>
