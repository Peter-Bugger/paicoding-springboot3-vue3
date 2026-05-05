<template>
  <el-menu
    :default-active="`/column/` + vo.column + '/' + vo.section"
    class="column-menu"
    @open="handleOpen"
    @close="handleClose"
    active-text-color="var(--pai-brand-1-normal)"
  >
    <a :href="`/column/` + vo.column + '/' + `${id+1}`" v-for="(menu, id) in vo.articleList" :key="id">
      <el-menu-item :index="`/column/` + vo.column + '/' + `${id+1}`">
        <template #default>
          <div class="column-menu-item">
            <span class="column-menu-num">{{id + 1}}</span>
            <span class="column-menu-title">{{menu.title.length > 15 ? menu.title.substring(0, 15) + '...' : menu.title}}</span>
          </div>
          <div class="column-menu-tag-wrap">
            <span class="column-menu-tag" v-if="menu.readType === 0">免费</span>
            <span class="column-menu-tag column-menu-tag--login" v-else-if="menu.readType === 1 || !global.isLogin">登录</span>
            <span class="column-menu-tag column-menu-tag--free" v-else-if="menu.readType === 2">限免</span>
            <span class="column-menu-tag column-menu-tag--star" v-else-if="menu.readType === 3">星球</span>
            <span class="column-menu-tag column-menu-tag--free" v-else>限免</span>
          </div>
        </template>
      </el-menu-item>
      <p class="column-menu-date">{{menu.createTime?.substring(0, menu.createTime.indexOf('T'))}}</p>
    </a>
  </el-menu>
</template>

<script setup lang="ts">
import type { ColumnArticlesResponseType } from '@/http/ResponseTypes/ColumnDetailType/ColumnArticlesResponseType'
import { useGlobalStore } from '@/stores/global'

const globalStore = useGlobalStore()
const global = globalStore.global

defineProps<{
  vo: ColumnArticlesResponseType
}>()

const handleOpen = (key: string, keyPath: string[]) => {
  console.log(key, keyPath)
}
const handleClose = (key: string, keyPath: string[]) => {
  console.log(key, keyPath)
}
</script>

<style scoped>
.column-menu {
  width: 315px;
  padding: 1rem 0.5rem;
  border-radius: 16px;
  background: var(--pai-bg-white-fff, #ffffff);
  box-shadow: 0 1px 3px rgba(26, 29, 39, 0.04);
  border: none;
  overflow: auto;
  max-height: calc(100vh - 80px);
  position: sticky;
  top: calc(var(--header-height, 60px) + 1rem);
}

.column-menu :deep(.el-menu-item) {
  height: auto;
  min-height: 44px;
  padding: 0.5rem 0.75rem;
  margin-bottom: 0.15rem;
  border-radius: 10px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  transition: all 0.2s ease;
}

.column-menu :deep(.el-menu-item:hover) {
  background: var(--pai-brand-7-light, rgba(45, 124, 246, 0.12));
}

.column-menu :deep(.el-menu-item.is-active) {
  background: var(--pai-brand-7-light, rgba(45, 124, 246, 0.12));
  color: var(--pai-brand-1-normal);
}

.column-menu-item {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  min-width: 0;
}

.column-menu-num {
  font-family: 'JetBrains Mono', monospace;
  font-size: 0.8rem;
  font-weight: 700;
  color: var(--pai-color-999-gray, #8c8f9c);
  min-width: 1.2rem;
  flex-shrink: 0;
}

.column-menu-title {
  font-size: 0.88rem;
  color: var(--pai-color-4-gray, #484d5e);
  line-height: 1.4;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  max-width: 180px;
}

.column-menu-tag-wrap {
  display: flex;
  gap: 0.2rem;
  flex-shrink: 0;
}

.column-menu-tag {
  font-size: 0.6rem;
  font-weight: 600;
  padding: 0.1rem 0.45rem;
  border-radius: 3px;
  color: var(--pai-color-4-gray, #484d5e);
  background: var(--pai-bg-light-2, #eef1f7);
}

.column-menu-tag--login {
  color: var(--pai-brand-1-normal);
  background: var(--pai-brand-7-light);
}

.column-menu-tag--free {
  color: #059669;
  background: rgba(5, 150, 105, 0.1);
}

.column-menu-tag--star {
  color: #d97706;
  background: rgba(217, 119, 6, 0.1);
}

.column-menu-date {
  font-size: 0.68rem;
  color: var(--pai-color-999-gray, #8c8f9c);
  margin: 0 0 0.25rem 2.2rem;
  padding: 0 0.75rem;
}
</style>
