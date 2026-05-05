<template>
    <HeaderBar />

    <div class="home-page">
      <!-- 顶部装饰条 -->
      <div class="home-accent-bar">
        <div class="home-accent-bar-inner"></div>
      </div>

      <!-- 骨架屏: 推荐区域 -->
      <el-skeleton class="hidden-when-screen-small" :loading="contentLoading" animated :throttle="200">
        <template #template>
          <div class="home-container">
            <div class="hero-skeleton">
              <el-skeleton-item variant="image" style="width: 100%; height: 400px; border-radius: 20px;" />
            </div>
          </div>
        </template>
        <template #default>
          <!-- 分类导航 -->
          <div class="home-container">
            <NavBar :categories="vo.categories"></NavBar>
          </div>

          <!-- 推荐文章 -->
          <div class="home-container" v-if="!contentLoading && vo.topArticles.length > 0">
            <RecommendArticle :top-articles="vo.topArticles" id="recommend-article-component"></RecommendArticle>
          </div>
        </template>
      </el-skeleton>

      <!-- 文章区域分隔 -->
      <div class="home-container">
        <div class="home-section-divider">
          <span class="home-section-divider-line"></span>
          <span class="home-section-divider-label">最新文章</span>
          <span class="home-section-divider-line"></span>
        </div>
      </div>

      <!-- 骨架屏: 文章列表 -->
      <el-skeleton :loading="articlesLoading" animated :throttle="200">
        <template #template>
          <div class="home-container">
            <div class="article-list-skeleton-grid">
              <div v-for="(item, id) in 6" :key="id" class="article-list-skeleton-item">
                <el-skeleton-item variant="image" style="width: 100%; height: 190px; border-radius: 16px 16px 0 0;" />
                <div style="padding: 1.25rem;">
                  <el-skeleton-item variant="text" style="width: 30%; margin-bottom: 0.75rem;" />
                  <el-skeleton-item variant="text" style="width: 100%; margin-bottom: 0.4rem;" />
                  <el-skeleton-item variant="text" style="width: 75%; margin-bottom: 1rem;" />
                  <el-skeleton-item variant="text" style="width: 50%;" />
                </div>
              </div>
            </div>
          </div>
        </template>
        <template #default>
          <div class="home-container">
            <div id="articleList">
              <ArticleList :articles="articles.records"></ArticleList>
            </div>

            <!-- 分页 -->
            <div class="pagination-wrap">
              <el-pagination
                :page-sizes="[10, 20]"
                hide-on-single-page
                v-model:current-page="currentPage"
                v-model:page-size="pageSize"
                layout="sizes, prev, pager, next"
                :page-count="totalPage"
                :default-current-page="1"
                @update:page-size="onPageSizeChange"
                @update:current-page="onCurrentPageChange"
              />
            </div>
          </div>
        </template>
      </el-skeleton>

      <Footer />
    </div>
    <LoginDialog :clicked="loginDialogClicked"></LoginDialog>
</template>

<script setup lang="ts">
import HeaderBar from '@/components/layout/HeaderBar.vue'
import { onMounted, provide, reactive, ref } from 'vue'
import { doGet } from '@/http/BackendRequests'
import { CATEGORY_ARTICLE_LIST_URL } from '@/http/URL'
import {
  type CommonResponse, type GlobalResponse, defaultGlobalResponse
} from '@/http/ResponseTypes/CommonResponseType'
import NavBar from '@/views/home/navbar/NavBar.vue'
import { defaultIndexVoResponse, type IndexVoResponse } from '@/http/ResponseTypes/IndexVoType'
import RecommendArticle from '@/views/home/recommend/RecommendArticle.vue'
import ArticleList from '@/views/home/article/ArticleList.vue'
import Footer from '@/components/layout/Footer.vue'
import { useGlobalStore } from '@/stores/global'
import { type BasicPageType, defaultBasicPage } from '@/http/ResponseTypes/PageType/BasicPageType'
import type { ArticleType } from '@/http/ResponseTypes/ArticleType/ArticleType'
import { useRoute } from 'vue-router'
import LoginDialog from '@/components/dialog/LoginDialog.vue'
const globalStore = useGlobalStore()
const route = useRoute()

let global = reactive<GlobalResponse>({...defaultGlobalResponse})
let vo = reactive<IndexVoResponse>({...defaultIndexVoResponse})
let articles = reactive<BasicPageType<ArticleType>>({...defaultBasicPage})
onMounted(() => {
  doGet<CommonResponse>(CATEGORY_ARTICLE_LIST_URL, {
    category: route.query['category']
  })
    .then((response) => {
      if(response.data){
        globalStore.setGlobal(response.data.global)
        Object.assign(vo.topArticles, response.data.result.topArticles)
        Object.assign(vo.categories, response.data.result.categories)
        // @ts-ignore
        Object.assign(articles, response.data.result.articles)
        totalPage.value = Number(response.data.result.articles.pages)
        currentPage.value = Number(response.data.result.articles.current)
        articlesLoading.value = false
        contentLoading.value = false
      }
    })
})

const currentPage = ref(1)
const totalPage = ref(0)
const pageSize = ref(10)

const onPageSizeChange = () => {
  doGet<CommonResponse>(CATEGORY_ARTICLE_LIST_URL, {
    category: route.query['category'],
    currentPage: currentPage.value,
    pageSize: pageSize.value
  })
    .then((response) => {
      if(response.data){
        globalStore.setGlobal(response.data.global)
        Object.assign(vo.topArticles, response.data.result.topArticles)
        Object.assign(vo.categories, response.data.result.categories)
        Object.assign(articles, response.data.result.articles)
        totalPage.value = Number(response.data.result.articles.pages)
        currentPage.value = Number(response.data.result.articles.current)
      }
    })
}

const onCurrentPageChange = (newCurrentPage: number) => {
  doGet<CommonResponse>(CATEGORY_ARTICLE_LIST_URL, {
    category: route.query['category'],
    currentPage: newCurrentPage,
    pageSize: pageSize.value
  })
    .then((response) => {
      if(response.data){
        globalStore.setGlobal(response.data.global)
        Object.assign(vo.topArticles, response.data.result.topArticles)
        Object.assign(vo.categories, response.data.result.categories)
        Object.assign(articles, response.data.result.articles)
        totalPage.value = Number(response.data.result.articles.pages)
        currentPage.value = Number(response.data.result.articles.current)
      }
    })
}

const contentLoading = ref(true)
const articlesLoading = ref(true)

const changeClicked = () => {
  loginDialogClicked.value = !loginDialogClicked.value
}

provide('loginDialogClicked', changeClicked)
const loginDialogClicked = ref(false)

</script>

<style scoped>
.home-page {
  background: var(--pai-bg-light-1, #f4f6fa);
  min-height: 100vh;
  padding-top: calc(var(--header-height, 60px) + 0.5rem);
  padding-bottom: 2rem;
  position: relative;
}

/* ── 顶部装饰条 ── */
.home-accent-bar {
  height: 3px;
  background: linear-gradient(90deg, transparent, var(--pai-brand-1-normal), var(--pai-brand-2-hover), var(--pai-brand-1-normal), transparent);
  opacity: 0.3;
  margin-bottom: 1.75rem;
}

.home-accent-bar-inner {
  height: 100%;
  width: 100%;
}

/* ── 容器 ── */
.home-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 1.25rem;
}

/* ── 文章区域分隔线 ── */
.home-section-divider {
  display: flex;
  align-items: center;
  gap: 1rem;
  margin: 1rem 0 1.5rem;
  animation: dividerFadeIn 0.6s ease-out 0.3s both;
}

.home-section-divider-line {
  flex: 1;
  height: 1px;
  background: linear-gradient(90deg, transparent, var(--pai-border-color-1, #d6dae6), transparent);
}

.home-section-divider-label {
  font-family: 'JetBrains Mono', monospace;
  font-size: 0.78rem;
  font-weight: 600;
  color: var(--pai-color-999-gray, #8c8f9c);
  letter-spacing: 0.12em;
  text-transform: uppercase;
  white-space: nowrap;
}

/* ── 骨架屏 ── */
.hero-skeleton {
  margin-bottom: 2rem;
}

.article-list-skeleton-grid {
  display: grid;
  grid-template-columns: 1fr;
  gap: 1.25rem;
}

@media (min-width: 768px) {
  .article-list-skeleton-grid {
    grid-template-columns: 1fr 1fr;
    gap: 1.5rem;
  }
}

.article-list-skeleton-item {
  background: var(--pai-bg-white-fff, #ffffff);
  border-radius: 16px;
  overflow: hidden;
}

/* ── 分页 ── */
.pagination-wrap {
  display: flex;
  justify-content: center;
  padding: 3rem 0 1.5rem;
}

.pagination-wrap :deep(.el-pagination) {
  --el-pagination-button-color: var(--pai-color-999-gray, #8c8f9c);
  --el-pagination-hover-color: var(--pai-brand-1-normal);
  --el-pagination-font-size: 14px;
  font-weight: 500;
}

.pagination-wrap :deep(.el-pagination .el-pager li) {
  border-radius: 8px;
  min-width: 36px;
  height: 36px;
  line-height: 36px;
  transition: all 0.25s ease;
}

.pagination-wrap :deep(.el-pagination .el-pager li.active) {
  color: #fff;
  background: var(--pai-brand-1-normal);
  font-weight: 700;
}

.pagination-wrap :deep(.el-pagination .el-pager li:not(.active):hover) {
  color: var(--pai-brand-1-normal);
  background: var(--pai-brand-7-light);
}

.pagination-wrap :deep(.el-pagination .btn-prev),
.pagination-wrap :deep(.el-pagination .btn-next) {
  color: var(--pai-color-999-gray, #8c8f9c);
  min-width: 36px;
  height: 36px;
  border-radius: 8px;
  transition: all 0.25s ease;
}

.pagination-wrap :deep(.el-pagination .btn-prev:hover),
.pagination-wrap :deep(.el-pagination .btn-next:hover) {
  color: var(--pai-brand-1-normal);
  background: var(--pai-brand-7-light);
}

.pagination-wrap :deep(.el-pagination .el-select .el-input .el-input__wrapper) {
  border-radius: 8px;
}

/* ── Animations ── */
@keyframes dividerFadeIn {
  from { opacity: 0; transform: translateY(4px); }
  to { opacity: 1; transform: translateY(0); }
}

/* ── Responsive ── */
@media (max-width: 768px) {
  .home-container {
    padding: 0 1rem;
  }
  .home-accent-bar {
    margin-bottom: 1.25rem;
  }
  .home-section-divider {
    margin: 0.5rem 0 1rem;
  }
}
</style>
