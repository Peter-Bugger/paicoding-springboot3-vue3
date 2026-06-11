<template>
  <HeaderBar></HeaderBar>

  <div class="column-detail-page">
    <el-skeleton :rows="5" animated :loading="contentLoading">
      <template #template>
        <div class="column-detail-skeleton">
          <el-skeleton-item class="column-detail-skeleton-menu" v-for="(item, id) in 5" :key="id"></el-skeleton-item>
        </div>
      </template>

      <template #default>
        <div class="column-detail-layout">
          <ColumnMenu class="hidden-when-screen-small" :vo="vo"></ColumnMenu>
          <div class="column-detail-content">
            <div class="column-detail-article">
              <ColumnArticleDetail :article-vo="vo"></ColumnArticleDetail>
              <!-- @PRD: US1 — 文章AI解读组件：选中文本发送AI解读请求 -->
              <ArticleInterpreter :article-id="articleIdNum" />
              <div id="commentDiv">
                <CommentList :comments="vo.comments" :hot-comment="vo.hotComment" :article="vo.article"></CommentList>
              </div>
            </div>
          </div>
        </div>
      </template>
    </el-skeleton>
  </div>
  <Footer></Footer>
  <LoginDialog :clicked="loginDialogClicked"></LoginDialog>
</template>

<script setup lang="ts">
import HeaderBar from '@/components/layout/HeaderBar.vue'
import Footer from '@/components/layout/Footer.vue'
import ColumnMenu from '@/views/column-detail/ColumnMenu.vue'
import { computed, onMounted, provide, reactive, ref } from 'vue'
import { type ColumnArticlesResponseType, defaultColumnArticlesResponse } from '@/http/ResponseTypes/ColumnDetailType/ColumnArticlesResponseType'
import { doGet } from '@/http/BackendRequests'
import { COLUMN_DETAIL_URL } from '@/http/URL'
import { useRoute } from 'vue-router'
import type { CommonResponse } from '@/http/ResponseTypes/CommonResponseType'
import { useGlobalStore } from '@/stores/global'
import ColumnArticleDetail from '@/components/column/ColumnArticleDetail.vue'
import ArticleInterpreter from '@/components/ArticleInterpreter.vue'
import CommentList from '@/views/article-detail/CommentList.vue'
import LoginDialog from '@/components/dialog/LoginDialog.vue'

const globalStore = useGlobalStore()
const vo = reactive({...defaultColumnArticlesResponse})
const articleIdNum = computed(() => Number(vo.article.articleId))

const getArticleDetail = (response: ColumnArticlesResponseType) => {
  Object.assign(vo.comments, response.comments)
}

provide('updateArticleComment', getArticleDetail)

const route = useRoute()
onMounted(() => {
  doGet<CommonResponse>(COLUMN_DETAIL_URL + `/${route.params['columnId']}/${route.params['sectionId']}`, {})
    .then((res) => {
      globalStore.setGlobal(res.data.global)
      Object.assign(vo, res.data.result)
      contentLoading.value = false
    })
    .catch((err) => {
      console.log(err)
    })
})

const contentLoading = ref(true)

const changeClicked = () => {
  loginDialogClicked.value = !loginDialogClicked.value
}

provide('loginDialogClicked', changeClicked)
const loginDialogClicked = ref(false)
</script>

<style scoped>
.column-detail-page {
  background: var(--pai-bg-light-1, #f4f6fa);
  min-height: 100vh;
  padding-top: calc(var(--header-height, 60px) + 0.5rem);
}

.column-detail-layout {
  display: flex;
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 1.25rem;
  gap: 1.5rem;
  align-items: flex-start;
}

.column-detail-content {
  flex: 1;
  min-width: 0;
}

.column-detail-article {
  max-width: 800px;
}

.column-detail-skeleton {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 1.25rem;
  display: flex;
  gap: 1.5rem;
}

.column-detail-skeleton-menu {
  width: 315px;
  height: 400px;
  border-radius: 12px;
}

@media (max-width: 768px) {
  .column-detail-layout {
    padding: 0 0.75rem;
  }
}
</style>
