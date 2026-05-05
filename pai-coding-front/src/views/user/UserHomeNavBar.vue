<template>
  <div class="user-tabs">
    <el-tabs
      v-model="activeName"
      class="user-tabs-nav"
      @tab-change="handleChange"
    >
      <el-tab-pane label="文章" name="articlesTab" lazy>
        <template #default>
          <UserHomeNavBarArticleList :articles="articles" />
          <el-pagination
            :page-sizes="[10, 20]"
            hide-on-single-page
            v-model:current-page="currentArticlesPage"
            v-model:page-size="articlesPageSize"
            layout="sizes, prev, pager, next"
            :page-count="totalArticlesPage"
            :default-current-page="1"
            @update:page-size="onArticlesPageSizeChange"
            @update:current-page="onArticlesCurrentPageChange"
          />
        </template>
      </el-tab-pane>
      <el-tab-pane v-if="global.user.id == userId" label="浏览记录" name="historyTab" lazy>
        <UserHomeNavBarArticleList :articles="historyArticles" />
        <el-pagination
          :page-sizes="[10, 20]"
          hide-on-single-page
          v-model:current-page="currentHistoryArticlesPage"
          v-model:page-size="historyArticlesPageSize"
          layout="sizes, prev, pager, next"
          :page-count="totalHistoryArticlesPage"
          :default-current-page="1"
          @update:page-size="onHistoryArticlesPageSizeChange"
          @update:current-page="onHistoryArticlesCurrentPageChange"
        />
      </el-tab-pane>
      <el-tab-pane label="关注列表" name="followTab" lazy>
        <UserFollowedList :user="followUsers" />
        <el-pagination
          :page-sizes="[10, 20]"
          hide-on-single-page
          v-model:current-page="currentFollowersPage"
          v-model:page-size="followersPageSize"
          layout="sizes, prev, pager, next"
          :page-count="totalFollowersPage"
          :default-current-page="1"
          @update:page-size="onFollowersPageSizeChange"
          @update:current-page="onFollowersCurrentPageChange"
        />
      </el-tab-pane>
      <el-tab-pane label="粉丝列表" name="fansTab" lazy>
        <UserFollowedList :user="fans" />
        <el-pagination
          :page-sizes="[10, 20]"
          hide-on-single-page
          v-model:current-page="currentFansPage"
          v-model:page-size="fansPageSize"
          layout="sizes, prev, pager, next"
          :page-count="totalFansPage"
          :default-current-page="1"
          @update:page-size="onFansPageSizeChange"
          @update:current-page="onFansCurrentPageChange"
        />
      </el-tab-pane>
      <el-tab-pane label="收藏" name="starsTab" lazy>
        <UserHomeNavBarArticleList :articles="starsArticles" />
        <el-pagination
          :page-sizes="[10, 20]"
          hide-on-single-page
          v-model:current-page="currentStarArticlesPage"
          v-model:page-size="starArticlesPageSize"
          layout="sizes, prev, pager, next"
          :page-count="totalStarArticlesPage"
          :default-current-page="1"
          @update:page-size="onStarArticlesPageSizeChange"
          @update:current-page="onStarArticlesCurrentPageChange"
        />
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script lang="ts" setup>
import { onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import UserHomeNavBarArticleList from '@/views/user/nav-bar/UserHomeNavBarArticleList.vue'
import { doGet } from '@/http/BackendRequests'
import type { CommonResponse } from '@/http/ResponseTypes/CommonResponseType'
import {
  USER_ARTICLE_LIST_URL,
  USER_FANS_LIST_URL,
  USER_FOLLOW_LIST_URL,
  USER_HISTORY_LIST_URL,
  USER_STAR_LIST_URL
} from '@/http/URL'
import { type BasicPageType, defaultBasicPage } from '@/http/ResponseTypes/PageType/BasicPageType'
import type { ArticleType } from '@/http/ResponseTypes/ArticleType/ArticleType'
import { useGlobalStore } from '@/stores/global'
import type { FollowUserInfoType } from '@/http/ResponseTypes/UserInfoType/FollowUserInfoType'
import UserFollowedList from '@/views/user/UserFollowedList.vue'

const route = useRoute()
const router = useRouter()
const globalStore = useGlobalStore()
const global = globalStore.global
const userId = route.params.userId

const activeName = ref(route.params.typeName || 'articlesTab')

const currentArticlesPage = ref(1)
const totalArticlesPage = ref(0)
const articlesPageSize = ref(10)
const articles = ref<BasicPageType<ArticleType>>({...defaultBasicPage})

const getArticles = () => {
  doGet<CommonResponse>(USER_ARTICLE_LIST_URL, {
    userId: userId,
    currentPage: currentArticlesPage.value,
    pageSize: articlesPageSize.value
  })
    .then((res) => {
      Object.assign(articles.value, res.data.result)
      currentArticlesPage.value = Number(res.data.result.current)
      totalArticlesPage.value = Number(res.data.result.pages)
    })
    .catch((err) => { console.log(err) })
}

const onArticlesCurrentPageChange = (newCurrentPage: number) => {
  currentArticlesPage.value = newCurrentPage
  getArticles()
}

const onArticlesPageSizeChange = (newPageSize: number) => {
  articlesPageSize.value = newPageSize
  getArticles()
}

const currentHistoryArticlesPage = ref(1)
const totalHistoryArticlesPage = ref(0)
const historyArticlesPageSize = ref(10)
const historyArticles = ref<BasicPageType<ArticleType>>({...defaultBasicPage})

const getHistoryArticles = () => {
  doGet<CommonResponse>(USER_HISTORY_LIST_URL, {
    userId: userId,
    currentPage: currentHistoryArticlesPage.value,
    pageSize: historyArticlesPageSize.value
  })
    .then((res) => {
      Object.assign(historyArticles.value, res.data.result)
      currentHistoryArticlesPage.value = Number(res.data.result.current)
      totalHistoryArticlesPage.value = Number(res.data.result.pages)
    })
    .catch((err) => { console.log(err) })
}

const onHistoryArticlesCurrentPageChange = (newCurrentPage: number) => {
  currentHistoryArticlesPage.value = newCurrentPage
  getHistoryArticles()
}

const onHistoryArticlesPageSizeChange = (newPageSize: number) => {
  historyArticlesPageSize.value = newPageSize
  getHistoryArticles()
}

const currentStarArticlesPage = ref(1)
const totalStarArticlesPage = ref(0)
const starArticlesPageSize = ref(10)
const starsArticles = ref<BasicPageType<ArticleType>>({...defaultBasicPage})

const getStarsArticles = () => {
  doGet<CommonResponse>(USER_STAR_LIST_URL, {
    userId: userId,
    currentPage: currentStarArticlesPage.value,
    pageSize: starArticlesPageSize.value
  })
    .then((res) => {
      Object.assign(starsArticles.value, res.data.result)
      currentStarArticlesPage.value = Number(res.data.result.current)
      totalStarArticlesPage.value = Number(res.data.result.pages)
    })
    .catch((err) => { console.log(err) })
}

const onStarArticlesCurrentPageChange = (newCurrentPage: number) => {
  currentStarArticlesPage.value = newCurrentPage
  getStarsArticles()
}

const onStarArticlesPageSizeChange = (newPageSize: number) => {
  starArticlesPageSize.value = newPageSize
  getStarsArticles()
}

const currentFollowersPage = ref(1)
const totalFollowersPage = ref(0)
const followersPageSize = ref(10)
const followUsers = ref<BasicPageType<FollowUserInfoType>>({...defaultBasicPage})

const getFollowUsers = () => {
  doGet<CommonResponse>(USER_FOLLOW_LIST_URL, {
    userId: userId,
    currentPage: currentFollowersPage.value,
    pageSize: followersPageSize.value
  })
    .then((res) => {
      Object.assign(followUsers.value, res.data.result)
      currentFollowersPage.value = Number(res.data.result.current)
      totalFollowersPage.value = Number(res.data.result.pages)
    })
    .catch((err) => { console.log(err) })
}

const onFollowersCurrentPageChange = (newCurrentPage: number) => {
  currentFollowersPage.value = newCurrentPage
  getFollowUsers()
}

const onFollowersPageSizeChange = (newPageSize: number) => {
  followersPageSize.value = newPageSize
  getFollowUsers()
}

const currentFansPage = ref(1)
const totalFansPage = ref(0)
const fansPageSize = ref(10)
const fans = ref<BasicPageType<FollowUserInfoType>>({...defaultBasicPage})

const getFans = () => {
  doGet<CommonResponse>(USER_FANS_LIST_URL, {
    userId: userId,
    currentPage: currentFansPage.value,
    pageSize: fansPageSize.value
  })
    .then((res) => {
      Object.assign(fans.value, res.data.result)
      currentFansPage.value = Number(res.data.result.current)
      totalFansPage.value = Number(res.data.result.pages)
    })
    .catch((err) => { console.log(err) })
}

const onFansCurrentPageChange = (newCurrentPage: number) => {
  currentFansPage.value = newCurrentPage
  getFans()
}

const onFansPageSizeChange = (newPageSize: number) => {
  fansPageSize.value = newPageSize
  getFans()
}

const handleChange = (val: string) => {
  if (val == 'articlesTab') {
    router.push(`/user/${userId}/articlesTab`)
    getArticles()
  } else if (val == 'historyTab') {
    router.push(`/user/${userId}/historyTab`)
    getHistoryArticles()
  } else if (val == 'starsTab') {
    router.push(`/user/${userId}/starsTab`)
    getStarsArticles()
  } else if (val == 'followTab') {
    router.push(`/user/${userId}/followTab`)
    getFollowUsers()
  } else if (val == 'fansTab') {
    router.push(`/user/${userId}/fansTab`)
    getFans()
  }
}

onMounted(() => {
  if (activeName.value === 'articlesTab') {
    getArticles()
  } else if (activeName.value === 'historyTab') {
    getHistoryArticles()
  } else if (activeName.value === 'starsTab') {
    getStarsArticles()
  } else if (activeName.value === 'followTab') {
    getFollowUsers()
  } else if (activeName.value === 'fansTab') {
    getFans()
  }
})
</script>

<style scoped>
.user-tabs {
  background: var(--pai-bg-white-fff, #ffffff);
  border-radius: 16px;
  padding: 0.5rem 1.5rem 1.5rem;
  box-shadow: 0 1px 3px rgba(26, 29, 39, 0.04);
}

.user-tabs-nav :deep(.el-tabs__header) {
  margin: 0 0 1rem;
  border-bottom: 1px solid var(--pai-bg-light-2, #eef1f7);
}

.user-tabs-nav :deep(.el-tabs__item) {
  font-size: 0.88rem;
  font-weight: 500;
  color: var(--pai-color-999-gray, #8c8f9c);
  height: 44px;
  line-height: 44px;
  padding: 0 1rem;
  transition: color 0.2s;
}

.user-tabs-nav :deep(.el-tabs__item:hover) {
  color: var(--pai-color-4-gray, #484d5e);
}

.user-tabs-nav :deep(.el-tabs__item.is-active) {
  color: var(--pai-brand-1-normal);
  font-weight: 600;
}

.user-tabs-nav :deep(.el-tabs__active-bar) {
  background: var(--pai-brand-1-normal);
  height: 2px;
}

.user-tabs-nav :deep(.el-pagination) {
  margin-top: 1rem;
  justify-content: center;
}
</style>
