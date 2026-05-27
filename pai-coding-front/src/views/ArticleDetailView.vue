<template>
  <!-- 阅读进度条 -->
  <div class="reading-progress" :style="{ width: progressPercent + '%' }"></div>

  <HeaderBar />

  <div class="article-detail-page" v-if="ifUsualArticle">
    <div class="article-detail-body">
      <div class="article-detail-layout">
        <div class="article-detail-main">
          <ArticleDetail :articleVo="articleVo"></ArticleDetail>
          <CommentList :comments="articleVo.comments" :hot-comment="articleVo.hotComment" :article="articleVo.article"></CommentList>
          <div class="article-related" id="relatedRecommend">
            <h4 class="article-related-title">相关推荐</h4>
            <div id="articleList"></div>
          </div>
        </div>

        <div class="article-detail-side hidden-when-screen-small">
          <UserCard :global="global" :user="articleVo.author"></UserCard>
          <SideRecommendBar :sidebar-bar-items="articleVo.sideBarItems"></SideRecommendBar>

          <div class="article-toc" id="content-menu">
            <el-scrollbar>
              <h5 class="article-toc-title">目录</h5>
              <el-divider></el-divider>
              <MdCatalog :editor-id="'id'" :scroll-element="scrollElement"></MdCatalog>
            </el-scrollbar>
          </div>
        </div>
      </div>
    </div>

    <Footer></Footer>
  </div>
  <LoginDialog :clicked="clicked"></LoginDialog>
  <!-- @PRD: US1 — 文章AI解读组件：选中文本发送AI解读请求 -->
  <ArticleInterpreter :article-id="articleIdNum" />
</template>

<script setup lang="ts">
import Footer from '@/components/layout/Footer.vue'
import HeaderBar from '@/components/layout/HeaderBar.vue'
import { computed, onMounted, provide, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { type CommonResponse } from '@/http/ResponseTypes/CommonResponseType'
import { doGet } from '@/http/BackendRequests'
import { ARTICLE_DETAIL_URL } from '@/http/URL'
import {
  type ArticleDetailResponse,
  defaultArticleDetailResponse
} from '@/http/ResponseTypes/ArticleDetailResponseType'
import ArticleDetail from '@/components/article/ArticleDetail.vue'
import { MdCatalog } from 'md-editor-v3'
import UserCard from '@/components/user/UserCard.vue'
import SideRecommendBar from '@/views/article-detail/SideRecommendBar.vue'
import LoginDialog from '@/components/dialog/LoginDialog.vue'
import { useGlobalStore } from '@/stores/global'
import CommentList from '@/views/article-detail/CommentList.vue'
import ArticleInterpreter from '@/components/ArticleInterpreter.vue'
import { setTitle } from '@/util/utils'

const progressPercent = ref(0)

const handleScroll = () => {
  const scrollTop = window.scrollY
  const docHeight = document.documentElement.scrollHeight - window.innerHeight
  if (docHeight > 0) {
    progressPercent.value = Math.min((scrollTop / docHeight) * 100, 100)
  }
}

const route = useRoute()
const globalStore = useGlobalStore()
const global = globalStore.global
let articleVo = reactive<ArticleDetailResponse>({...defaultArticleDetailResponse})
const articleId = route.params.articleId
const articleIdNum = computed(() => Number(articleId))
const scrollElement = document.documentElement
const clicked = ref(false)

const changeClicked = () => {
  clicked.value = !clicked.value
}

provide('loginDialogClicked', changeClicked)

const getArticleDetail = (response: ArticleDetailResponse) => {
  Object.assign(articleVo.comments, response.comments)
}

provide('updateArticleComment', getArticleDetail)

const ifUsualArticle = ref(false)
const router = useRouter()

onMounted(async () => {
  window.addEventListener('scroll', handleScroll, { passive: true })
  doGet<CommonResponse>(ARTICLE_DETAIL_URL + `/${articleId}`, {})
    .then((response) => {
      if (!response.data.redirect) {
        globalStore.setGlobal(response.data.global)
        Object.assign(articleVo, response.data.result)
        setTitle(articleVo.article.title)
        ifUsualArticle.value = true
      } else {
        router.replace("/column/" + response.data.result.columnId + '/' + response.data.result.sectionId)
      }
    })
})
</script>

<style scoped>
/* ── 阅读进度条 ── */
.reading-progress {
  position: fixed;
  top: 0;
  left: 0;
  height: 3px;
  background: linear-gradient(90deg, var(--pai-brand-1-normal), var(--pai-brand-2-hover));
  z-index: 1001;
  transition: width 0.1s linear;
  border-radius: 0 2px 2px 0;
}

.article-detail-page {
  background: var(--pai-bg-light-1, #f4f6fa);
  min-height: 100vh;
  padding-top: calc(var(--header-height, 60px) + 0.5rem);
  padding-bottom: 2rem;
}

.article-detail-body {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 1.25rem;
}

.article-detail-layout {
  display: grid;
  grid-template-columns: 1fr 300px;
  gap: 1.5rem;
  align-items: start;
}

.article-detail-main {
  min-width: 0;
}

.article-detail-side {
  display: flex;
  flex-direction: column;
  gap: 1rem;
  position: sticky;
  top: calc(var(--header-height, 60px) + 1rem);
}

.article-related {
  background: var(--pai-bg-white-fff, #ffffff);
  border-radius: 16px;
  padding: 1.5rem 2rem;
  margin-top: 1.5rem;
  box-shadow: 0 1px 3px rgba(26, 29, 39, 0.04);
}

.article-related-title {
  font-size: 0.95rem;
  font-weight: 600;
  color: var(--pai-color-3-black, #1e2029);
  margin: 0 0 1rem;
  padding-bottom: 0.75rem;
  border-bottom: 1px solid var(--pai-bg-light-2, #eef1f7);
}

/* ── TOC ── */
.article-toc {
  background: var(--pai-bg-white-fff, #ffffff);
  border-radius: 12px;
  padding: 1.25rem;
  box-shadow: 0 1px 3px rgba(26, 29, 39, 0.04);
  max-height: calc(100vh - 80px);
  overflow-y: auto;
}

.article-toc-title {
  font-size: 0.85rem;
  font-weight: 600;
  color: var(--pai-color-3-black, #1e2029);
  margin: 0 0 0.5rem;
  padding-bottom: 0.5rem;
  border-bottom: 2px solid var(--pai-brand-7-light);
}

/* ── Responsive ── */
@media (max-width: 768px) {
  .article-detail-layout {
    grid-template-columns: 1fr;
  }
  .article-detail-body {
    padding: 0 0.75rem;
  }
  .article-detail-side {
    display: none;
  }
  .article-related {
    padding: 1rem;
  }
}
</style>
