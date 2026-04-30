<template>
  <div class="nav-section">
    <div v-if="!ifSearchActive" class="nav-bar">
      <div class="nav-categories">
        <a
          v-for="subCategory in categories"
          :key="subCategory.categoryId"
          :href="'?category=' + subCategory.category"
          class="nav-pill"
          :class="{ 'nav-pill--active': subCategory.category === activeCategory }"
        >
          {{subCategory.category}}
        </a>
      </div>
      <button @click="ifSearchActive = true" class="nav-search-btn" aria-label="搜索">
        <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2" stroke-linecap="round" stroke-linejoin="round"><circle cx="11" cy="11" r="8"/><path d="m21 21-4.35-4.35"/></svg>
      </button>
    </div>
    <div v-if="ifSearchActive" class="nav-search-wrap">
      <div class="nav-search-inner">
        <el-select
          v-model="searchInput"
          filterable
          remote
          reserve-keyword
          placeholder="搜你想搜..."
          :loading="loading"
          :remote-method="fetchSearchData"
          :no-data-text="'嗯... 没有找到你想要的内容，换个关键字试试吧:'"
          fit-input-width
          class="nav-search-select"
        >
          <el-option
            v-for="item in options"
            :key="item.id"
            :value="item.title"
          >
            <template #default>
              <div class="flex justify-between">
                <span v-html="highlightKeyword(item.title)"></span>
              </div>
            </template>
          </el-option>
        </el-select>
      </div>
      <button class="nav-search-close" @click="ifSearchActive = false" aria-label="关闭搜索">
        <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M18 6 6 18"/><path d="m6 6 12 12"/></svg>
      </button>
    </div>
  </div>
</template>

<script setup lang="ts">

import { onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import { Close } from '@element-plus/icons-vue'
import { doGet } from '@/http/BackendRequests'
import type { CommonResponse } from '@/http/ResponseTypes/CommonResponseType'
import type { ArticleCategoryType } from '@/http/ResponseTypes/CategoryType/ArticleCategoryType'

const route = useRoute()

const ifSearchActive = ref(false)

defineProps<{
  categories: ArticleCategoryType[]
}>()

const activeCategory = ref('全部')

onMounted(() => {
  activeCategory.value = route.query.category as string || '全部'
})

// ==================== 搜索输入框 ====================
const loading = ref(false)
const searchInput = ref('')
const keyWord = ref('')
interface ListItem {
  column?: string,
  columnId?: number,
  id: number,
  readType?: string,
  sort?: string,
  title: string,
}
const options = ref<ListItem[]>([])

const fetchSearchData = (query : string) => {
  keyWord.value = query
  loading.value = true
  doGet<CommonResponse>('/search/api/hint', { key: query })
    .then((res) => {
      options.value = res.data.result?.items
    })
    .catch((err) => {
      console.log(err)
    })
    .finally(() => {
      loading.value = false
    })
}

/**
 * 高亮搜索得到的文章的结果
 * @param text
 */
const highlightKeyword = (text: string) => {
  if (!keyWord.value) return text
  const regex = new RegExp(`(${keyWord.value})`, 'gi')
  return text.replace(regex, '<b class="highlight" style="font-size: large">$1</b>')
}
</script>

<style scoped>
/* ── Section Container ── */
.nav-section {
  margin-bottom: 2rem;
  animation: navSlideIn 0.5s ease-out both;
}

.nav-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 1rem;
}

/* ── Category Pills ── */
.nav-categories {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 0.35rem;
}

.nav-pill {
  display: inline-flex;
  align-items: center;
  padding: 0.45rem 1.1rem;
  font-size: 0.85rem;
  font-weight: 500;
  color: #78746e;
  background: transparent;
  border-radius: 999px;
  text-decoration: none;
  line-height: 1.4;
  transition: all 0.3s cubic-bezier(0.2, 0, 0, 1);
  border: none;
  position: relative;
}

.nav-pill::after {
  content: '';
  position: absolute;
  inset: 0;
  border-radius: 999px;
  background: var(--pai-brand-7-light);
  opacity: 0;
  transition: opacity 0.3s ease;
  z-index: -1;
}

.nav-pill:hover {
  color: var(--pai-brand-1-normal);
}

.nav-pill:hover::after {
  opacity: 1;
}

.nav-pill--active {
  color: #fff;
  background: var(--pai-brand-1-normal);
  box-shadow: 0 2px 10px rgba(255, 105, 0, 0.25);
}

.nav-pill--active::after {
  display: none;
}

.nav-pill--active:hover {
  color: #fff;
  background: var(--pai-brand-2-hover);
  box-shadow: 0 3px 14px rgba(255, 105, 0, 0.35);
}

/* ── Search Button ── */
.nav-search-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 38px;
  height: 38px;
  border-radius: 50%;
  border: 1.5px solid #e5ddd5;
  background: transparent;
  color: #999;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.2, 0, 0, 1);
  flex-shrink: 0;
}

.nav-search-btn:hover {
  border-color: var(--pai-brand-1-normal);
  color: var(--pai-brand-1-normal);
  background: var(--pai-brand-7-light);
  transform: scale(1.05);
}

/* ── Search Panel ── */
.nav-search-wrap {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  animation: searchFadeIn 0.3s ease-out;
}

.nav-search-inner {
  flex: 1;
  max-width: 28rem;
}

.nav-search-select {
  width: 100%;
}

.nav-search-close {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 38px;
  height: 38px;
  border-radius: 50%;
  border: none;
  background: #f0ebe5;
  color: #887;
  cursor: pointer;
  transition: all 0.25s ease;
  flex-shrink: 0;
}

.nav-search-close:hover {
  background: #e5ddd5;
  color: #555;
  transform: rotate(90deg);
}

/* ── Animations ── */
@keyframes navSlideIn {
  from { opacity: 0; transform: translateY(-8px); }
  to { opacity: 1; transform: translateY(0); }
}

@keyframes searchFadeIn {
  from { opacity: 0; transform: scale(0.96); }
  to { opacity: 1; transform: scale(1); }
}
</style>
