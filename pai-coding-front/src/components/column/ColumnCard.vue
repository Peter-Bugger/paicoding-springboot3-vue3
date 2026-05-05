<template>
  <article class="column-card">
    <a :href="'/column/' + column.columnId + '/1'" class="column-card-link">
      <div class="column-card-cover">
        <img :src="column.cover" :alt="column.column" loading="lazy" />
        <span class="column-card-badge" v-if="column.type === 2">上新</span>
      </div>
      <div class="column-card-body">
        <h3 class="column-card-title">{{column.column}}</h3>
        <div class="column-card-author">
          <el-avatar :src="column.authorAvatar" size="small"></el-avatar>
          <span class="column-card-author-name">{{column.authorName}}</span>
          <span class="column-card-author-divider">|</span>
          <span class="column-card-author-profile">{{column.authorProfile || '这人很懒，还没留下简介~'}}</span>
        </div>
        <p class="column-card-desc">{{column.introduction}}</p>
        <div class="column-card-meta">
          <span class="column-card-meta-item">{{column.state == 2 ? '已完结' : '已更新 ' + column.count.articleCount + ' 小节'}}</span>
          <span class="column-card-meta-dot">·</span>
          <span class="column-card-meta-item">共 {{column.count.totalNums}} 节</span>
          <span class="column-card-meta-dot">·</span>
          <span class="column-card-meta-item">{{column.count.readCount}} 人阅读</span>
        </div>
      </div>
    </a>
  </article>
</template>

<script setup lang="ts">
import type { ColumnVoType } from '@/http/ResponseTypes/ColumnType/ColumnListVoType'

defineProps<{
  column: ColumnVoType
}>()
</script>

<style scoped>
.column-card {
  background: var(--pai-bg-white-fff, #ffffff);
  border-radius: 16px;
  overflow: hidden;
  box-shadow: 0 1px 3px rgba(26, 29, 39, 0.04);
  transition: all 0.3s ease;
}

.column-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 24px rgba(26, 29, 39, 0.08);
}

.column-card-link {
  display: flex;
  text-decoration: none;
  min-height: 180px;
}

.column-card-cover {
  position: relative;
  width: 240px;
  min-height: 180px;
  flex-shrink: 0;
  overflow: hidden;
}

.column-card-cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.5s ease;
}

.column-card:hover .column-card-cover img {
  transform: scale(1.04);
}

.column-card-badge {
  position: absolute;
  top: 0.75rem;
  left: 0.75rem;
  padding: 0.15rem 0.55rem;
  font-size: 0.7rem;
  font-weight: 600;
  color: #fff;
  background: var(--pai-brand-1-normal);
  border-radius: 4px;
}

.column-card-body {
  flex: 1;
  padding: 1.25rem 1.5rem;
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.column-card-title {
  font-size: 1.15rem;
  font-weight: 700;
  color: var(--pai-color-3-black, #1e2029);
  margin: 0 0 0.6rem;
  line-height: 1.4;
}

.column-card:hover .column-card-title {
  color: var(--pai-brand-1-normal);
}

.column-card-author {
  display: flex;
  align-items: center;
  gap: 0.4rem;
  margin-bottom: 0.6rem;
  flex-wrap: wrap;
}

.column-card-author-name {
  font-size: 0.8rem;
  font-weight: 500;
  color: var(--pai-color-4-gray, #484d5e);
}

.column-card-author-divider {
  font-size: 0.7rem;
  color: var(--pai-color-5-gray, #d0d3dd);
}

.column-card-author-profile {
  font-size: 0.75rem;
  color: var(--pai-color-999-gray, #8c8f9c);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  max-width: 300px;
}

.column-card-desc {
  font-size: 0.85rem;
  color: var(--pai-color-3-gray, #6b7084);
  line-height: 1.6;
  margin: 0 0 0.75rem;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.column-card-meta {
  display: flex;
  align-items: center;
  gap: 0.35rem;
  font-size: 0.78rem;
  color: var(--pai-color-999-gray, #8c8f9c);
}

.column-card-meta-dot {
  color: var(--pai-color-5-gray, #d0d3dd);
}

@media (max-width: 768px) {
  .column-card-link {
    flex-direction: column;
  }
  .column-card-cover {
    width: 100%;
    height: 160px;
  }
  .column-card-body {
    padding: 1rem;
  }
}
</style>
