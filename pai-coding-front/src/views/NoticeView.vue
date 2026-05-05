<template>
  <HeaderBar></HeaderBar>

  <div class="notice-page">
    <div class="notice-container">
      <div class="notice-nav">
        <el-badge :value="unreadCount[NoticeTypeEnum.COMMENT_TYPE]" :offset="[-16, 8]" :show-zero="false">
          <el-link @click="changeNoticeType(NoticeTypeEnum.COMMENT_TYPE)">
            <span :class="{ 'notice-tab-active': currentTab == NoticeTypeEnum.COMMENT_TYPE }" class="notice-tab">评论</span>
          </el-link>
        </el-badge>
        <el-badge :value="unreadCount[NoticeTypeEnum.REPLY_TYPE]" :offset="[-16, 8]" :show-zero="false">
          <el-link @click="changeNoticeType(NoticeTypeEnum.REPLY_TYPE)">
            <span :class="{ 'notice-tab-active': currentTab == NoticeTypeEnum.REPLY_TYPE }" class="notice-tab">回复</span>
          </el-link>
        </el-badge>
        <el-badge :value="unreadCount['praise']" :offset="[-16, 8]" :show-zero="false">
          <el-link @click="changeNoticeType(NoticeTypeEnum.PRAISE_TYPE)">
            <span :class="{ 'notice-tab-active': currentTab == NoticeTypeEnum.PRAISE_TYPE }" class="notice-tab">点赞</span>
          </el-link>
        </el-badge>
        <el-badge :value="unreadCount['collect']" :offset="[-16, 8]" :show-zero="false">
          <el-link @click="changeNoticeType(NoticeTypeEnum.COLLECT_TYPE)">
            <span :class="{ 'notice-tab-active': currentTab == NoticeTypeEnum.COLLECT_TYPE }" class="notice-tab">收藏</span>
          </el-link>
        </el-badge>
        <el-badge :value="unreadCount['follow']" :offset="[-16, 8]" :show-zero="false">
          <el-link @click="changeNoticeType(NoticeTypeEnum.FOLLOW_TYPE)">
            <span :class="{ 'notice-tab-active': currentTab == NoticeTypeEnum.FOLLOW_TYPE }" class="notice-tab">关注消息</span>
          </el-link>
        </el-badge>
        <el-badge :value="unreadCount['system']" :offset="[-16, 8]" :show-zero="false">
          <el-link @click="changeNoticeType(NoticeTypeEnum.SYSTEM_TYPE)">
            <span :class="{ 'notice-tab-active': currentTab == NoticeTypeEnum.SYSTEM_TYPE }" class="notice-tab">系统消息</span>
          </el-link>
        </el-badge>
      </div>

      <div class="notice-content">
        <div v-if="currentTab === NoticeTypeEnum.COMMENT_TYPE">
          <el-skeleton :loading="loading" :throttle="200">
            <template #template>
              <div v-for="(item, id) in 5" :key="id" class="notice-skeleton-item">
                <el-skeleton-item class="notice-skeleton-block" />
              </div>
            </template>
            <template #default>
              <div v-if="!loading && (!noticeData.comment.list || noticeData.comment.list.records.length == 0)" class="notice-empty">暂无评论消息</div>
              <NoticeComment v-else :list="noticeData.comment.list" />
              <el-pagination :page-sizes="[10, 20]" hide-on-single-page v-model:current-page="currentPage[NoticeTypeEnum.COMMENT_TYPE]" v-model:page-size="pageSize[NoticeTypeEnum.COMMENT_TYPE]" layout="sizes, prev, pager, next" :page-count="totalPage[NoticeTypeEnum.COMMENT_TYPE]" @update:page-size="onPageSizeChange" @update:current-page="onCurrentPageChange" />
            </template>
          </el-skeleton>
        </div>

        <div v-if="currentTab === NoticeTypeEnum.REPLY_TYPE">
          <el-skeleton :loading="loading">
            <template #template>
              <div v-for="(item, id) in 5" :key="id" class="notice-skeleton-item">
                <el-skeleton-item class="notice-skeleton-block" />
              </div>
            </template>
            <template #default>
              <div v-if="!loading && (!noticeData.reply.list || noticeData.reply.list.records.length == 0)" class="notice-empty">暂无回复消息</div>
              <NoticeReply v-else :list="noticeData.reply.list" />
              <el-pagination :page-sizes="[10, 20]" hide-on-single-page v-model:current-page="currentPage[NoticeTypeEnum.REPLY_TYPE]" v-model:page-size="pageSize[NoticeTypeEnum.REPLY_TYPE]" layout="sizes, prev, pager, next" :page-count="totalPage[NoticeTypeEnum.REPLY_TYPE]" @update:page-size="onPageSizeChange" @update:current-page="onCurrentPageChange" />
            </template>
          </el-skeleton>
        </div>

        <div v-if="currentTab === NoticeTypeEnum.PRAISE_TYPE">
          <el-skeleton :loading="loading">
            <template #template>
              <div v-for="(item, id) in 5" :key="id" class="notice-skeleton-item">
                <el-skeleton-item class="notice-skeleton-block" />
              </div>
            </template>
            <template #default>
              <div v-if="!loading && (!noticeData.praise.list || noticeData.praise.list.records.length == 0)" class="notice-empty">暂无点赞消息</div>
              <NoticePraise v-else :list="noticeData.praise.list" />
              <el-pagination :page-sizes="[10, 20]" hide-on-single-page v-model:current-page="currentPage[NoticeTypeEnum.PRAISE_TYPE]" v-model:page-size="pageSize[NoticeTypeEnum.PRAISE_TYPE]" layout="sizes, prev, pager, next" :page-count="totalPage[NoticeTypeEnum.PRAISE_TYPE]" @update:page-size="onPageSizeChange" @update:current-page="onCurrentPageChange" />
            </template>
          </el-skeleton>
        </div>

        <div v-if="currentTab === NoticeTypeEnum.COLLECT_TYPE">
          <el-skeleton :loading="loading">
            <template #template>
              <div v-for="(item, id) in 5" :key="id" class="notice-skeleton-item">
                <el-skeleton-item class="notice-skeleton-block" />
              </div>
            </template>
            <template #default>
              <div v-if="!loading && (!noticeData.collect.list || noticeData.collect.list.records.length == 0)" class="notice-empty">暂无收藏消息</div>
              <NoticeCollect v-else :list="noticeData.collect.list" />
              <el-pagination :page-sizes="[10, 20]" hide-on-single-page v-model:current-page="currentPage[NoticeTypeEnum.COLLECT_TYPE]" v-model:page-size="pageSize[NoticeTypeEnum.COLLECT_TYPE]" layout="sizes, prev, pager, next" :page-count="totalPage[NoticeTypeEnum.COLLECT_TYPE]" @update:page-size="onPageSizeChange" @update:current-page="onCurrentPageChange" />
            </template>
          </el-skeleton>
        </div>

        <div v-if="currentTab === NoticeTypeEnum.FOLLOW_TYPE">
          <el-skeleton :loading="loading">
            <template #template>
              <div v-for="(item, id) in 5" :key="id" class="notice-skeleton-item">
                <el-skeleton-item class="notice-skeleton-block" />
              </div>
            </template>
            <template #default>
              <div v-if="!loading && (!noticeData.follow.list || noticeData.follow.list.records.length == 0)" class="notice-empty">暂无关注消息</div>
              <NoticeFollow v-else :list="noticeData.follow.list" />
              <el-pagination :page-sizes="[10, 20]" hide-on-single-page v-model:current-page="currentPage[NoticeTypeEnum.FOLLOW_TYPE]" v-model:page-size="pageSize[NoticeTypeEnum.FOLLOW_TYPE]" layout="sizes, prev, pager, next" :page-count="totalPage[NoticeTypeEnum.FOLLOW_TYPE]" @update:page-size="onPageSizeChange" @update:current-page="onCurrentPageChange" />
            </template>
          </el-skeleton>
        </div>

        <div v-if="currentTab === NoticeTypeEnum.SYSTEM_TYPE">
          <el-skeleton :loading="loading">
            <template #template>
              <div v-for="(item, id) in 5" :key="id" class="notice-skeleton-item">
                <el-skeleton-item class="notice-skeleton-block" />
              </div>
            </template>
            <template #default>
              <div v-if="!loading && (!noticeData.system.list || noticeData.system.list.records.length == 0)" class="notice-empty">暂无系统消息</div>
              <NoticeSystem v-else :list="noticeData.system.list" />
              <el-pagination :page-sizes="[10, 20]" hide-on-single-page v-model:current-page="currentPage[NoticeTypeEnum.SYSTEM_TYPE]" v-model:page-size="pageSize[NoticeTypeEnum.SYSTEM_TYPE]" layout="sizes, prev, pager, next" :page-count="totalPage[NoticeTypeEnum.SYSTEM_TYPE]" @update:page-size="onPageSizeChange" @update:current-page="onCurrentPageChange" />
            </template>
          </el-skeleton>
        </div>
      </div>
    </div>
    <Footer></Footer>
    <LoginDialog :clicked="loginDialogClicked"></LoginDialog>
  </div>
</template>

<script setup lang="ts">
import Footer from '@/components/layout/Footer.vue'
import HeaderBar from '@/components/layout/HeaderBar.vue'
import { useGlobalStore } from '@/stores/global'
import { computed, onMounted, provide, reactive, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { doGet } from '@/http/BackendRequests'
import type { CommonResponse } from '@/http/ResponseTypes/CommonResponseType'
import { defaultNoticeMsgResponse, type NoticeMsgResponseType } from '@/http/ResponseTypes/NoticeMsgResponseType'
import { UNREAD_NOTICE_URL } from '@/http/URL'
import LoginDialog from '@/components/dialog/LoginDialog.vue'
import NoticeComment from '@/views/notice/NoticeComment.vue'
import { NoticeTypeEnum } from '@/constants/NoticeTypeConstants'
import NoticeReply from '@/views/notice/NoticeReply.vue'
import NoticeSystem from '@/views/notice/NoticeSystem.vue'
import NoticeFollow from '@/views/notice/NoticeFollow.vue'
import NoticeCollect from '@/views/notice/NoticeCollect.vue'
import NoticePraise from '@/views/notice/NoticePraise.vue'

const globalStore = useGlobalStore()
const route = useRoute()
const router = useRouter()

const loading = ref(true)

const currentPage = reactive<Record<string, number>>({
  comment: 1, reply: 1, praise: 1, collect: 1, follow: 1, system: 1
})
const pageSize = reactive<Record<string, number>>({
  comment: 10, reply: 10, praise: 10, collect: 10, follow: 10, system: 10
})
const totalPage = reactive<Record<string, number>>({
  comment: 0, reply: 0, praise: 0, collect: 0, follow: 0, system: 0
})
const noticeData = reactive<Record<string, NoticeMsgResponseType>>({
  comment: { ...defaultNoticeMsgResponse },
  reply: { ...defaultNoticeMsgResponse },
  praise: { ...defaultNoticeMsgResponse },
  collect: { ...defaultNoticeMsgResponse },
  follow: { ...defaultNoticeMsgResponse },
  system: { ...defaultNoticeMsgResponse }
})
const unreadCount = reactive<Record<string, number>>({
  comment: 0, reply: 0, praise: 0, collect: 0, follow: 0, system: 0
})

onMounted(() => {
  getNotices()
  unreadCount[String(route.params.noticeType)] = 0
})

watch(() => route.params.noticeType, () => {
  getNotices()
  unreadCount[String(route.params.noticeType)] = 0
})

const getNotices = () => {
  const type = String(route.params.noticeType)
  doGet<CommonResponse<NoticeMsgResponseType>>(UNREAD_NOTICE_URL + '/' + type, {
    currentPage: currentPage[type],
    pageSize: pageSize[type]
  })
    .then((res) => {
      globalStore.setGlobal(res.data.global)
      Object.assign(noticeData[type], res.data.result)
      Object.assign(unreadCount, res.data.result.unreadCountMap)
      totalPage[type] = Number(res.data.result.list.pages)
    })
    .finally(() => { loading.value = false })
}

const currentTab = computed(() => route.params.noticeType)

const onCurrentPageChange = (newCurrentPage: number) => {
  currentPage[String(route.params.noticeType)] = newCurrentPage
  getNotices()
}

const onPageSizeChange = (newPageSize: number) => {
  pageSize[String(route.params.noticeType)] = newPageSize
  getNotices()
}

const changeNoticeType = (targetNoticeType: string) => {
  if (currentTab.value == targetNoticeType) {
    window.location.reload()
  } else {
    router.push('/notice/' + targetNoticeType)
  }
}

const changeClicked = () => {
  loginDialogClicked.value = !loginDialogClicked.value
}

provide('loginDialogClicked', changeClicked)
const loginDialogClicked = ref(false)
</script>

<style scoped>
.notice-page {
  background: var(--pai-bg-light-1, #f4f6fa);
  min-height: calc(100vh - var(--header-height, 60px));
  padding-top: calc(var(--header-height, 60px));
}

.notice-container {
  max-width: 800px;
  margin: 0 auto;
  padding: 1.25rem;
}

.notice-nav {
  display: flex;
  gap: 0.25rem;
  padding: 0.75rem 1.25rem;
  background: var(--pai-bg-white-fff, #ffffff);
  border-radius: 16px 16px 0 0;
  box-shadow: 0 1px 3px rgba(26, 29, 39, 0.04);
  flex-wrap: wrap;
  border-bottom: 1px solid var(--pai-bg-light-2, #eef1f7);
}

.notice-tab {
  font-size: 0.9rem;
  font-weight: 600;
  padding: 0.4rem 0.75rem;
  border-radius: 8px;
  color: var(--pai-color-999-gray, #8c8f9c);
  transition: all 0.2s;
  cursor: pointer;
}

.notice-tab:hover {
  color: var(--pai-color-4-gray, #484d5e);
  background: var(--pai-bg-light-1, #f4f6fa);
}

.notice-tab-active {
  color: var(--pai-brand-1-normal);
  background: var(--pai-brand-7-light, rgba(45, 124, 246, 0.12));
}

.notice-content {
  background: var(--pai-bg-white-fff, #ffffff);
  border-radius: 0 0 16px 16px;
  padding: 1rem 1.5rem 1.5rem;
  box-shadow: 0 1px 3px rgba(26, 29, 39, 0.04);
}

.notice-skeleton-item {
  display: flex;
  gap: 0.75rem;
  padding: 1rem;
}

.notice-skeleton-block {
  width: 100%;
  height: 60px;
  border-radius: 8px;
}

.notice-empty {
  text-align: center;
  font-size: 0.88rem;
  color: var(--pai-color-999-gray, #8c8f9c);
  padding: 2rem 0;
}

.notice-content :deep(.el-pagination) {
  margin-top: 1rem;
  justify-content: center;
}

@media (max-width: 768px) {
  .notice-container {
    padding: 0.75rem;
  }
}
</style>
