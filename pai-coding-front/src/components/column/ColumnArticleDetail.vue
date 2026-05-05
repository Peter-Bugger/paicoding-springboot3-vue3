<template>
  <section class="column-article-section">
    <span class="column-article-badge">原创</span>
    <h1 class="column-article-title">{{articleVo.article.title}}</h1>

    <div class="column-article-meta">
      <span class="column-article-date">{{ format(new Date(Number(articleVo.article.createTime)), 'yyyy年MM月dd日')}}</span>
      <span class="column-article-divider">|</span>
      <span class="column-article-reads">{{'阅读 ' + articleVo.article.count.readCount}}</span>
      <div class="column-article-actions" v-if="global.isLogin && articleVo.article.author == global.user.id">
        <span class="column-article-action-btn">
          <el-icon :size="16"><Edit /></el-icon>
          <span>编辑</span>
        </span>
        <span class="column-article-action-btn column-article-action-btn--danger">
          <el-icon :size="16"><Delete /></el-icon>
          <span>删除</span>
        </span>
      </div>
    </div>

    <!-- 文章标签 -->
    <div class="column-article-tags" v-if="articleVo.article.tags?.length">
      <el-tag class="column-article-tag" v-for="tagItem in articleVo.article.tags" :key="tagItem.tagId" effect="plain" round>{{tagItem.tag}}</el-tag>
    </div>

    <div class="column-article-content">
      <MdPreview :editor-id="'id'" :model-value="articleVo.article.content"></MdPreview>
    </div>

    <!-- 左右切换 -->
    <div class="column-article-nav" v-if="articleVo.other && articleVo.other.flip">
      <a class="column-article-nav-btn column-article-nav-btn--prev"
         :href="articleVo.other.flip.prevHref"
         v-if="articleVo.other.flip.prevShow"
      >
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="m15 18-6-6 6-6"/></svg>
        <span>上一篇</span>
      </a>
      <a class="column-article-nav-btn column-article-nav-btn--next"
         :href="articleVo.other.flip.nextHref"
         v-if="articleVo.other.flip.nextShow"
      >
        <span>下一篇</span>
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="m9 18 6-6-6-6"/></svg>
      </a>
    </div>

    <div v-if="articleVo.other && articleVo.other.readType === 1 && !global.isLogin">
      <div class="column-article-lock">
        <a class="column-article-lock-btn">登录之后即可阅读全文</a>
      </div>
    </div>

    <div v-if="articleVo.other && articleVo.other.readType === 3 && !(global.user != null && global.user.starStatus == 'FORMAL')">
      <div class="column-article-lock">
        <a class="column-article-lock-btn">已加入二哥编程星球，即刻绑定星球编号解锁🔐</a>
      </div>
    </div>

    <!-- 点赞 -->
    <div class="column-article-like">
      <el-button circle round size="large" @click="likeArticle">
        <el-icon v-show="!btnLoading" size="20">
          <svg viewBox="0 0 1024 1024" width="16" height="18">
            <path d="M621.674667 408.021333c16.618667-74.24 28.224-127.936 34.837333-161.194666C673.152 163.093333 629.941333 85.333333 544.298667 85.333333c-77.226667 0-116.010667 38.378667-138.88 115.093334l-0.586667 2.24c-13.728 62.058667-34.72 110.165333-62.506667 144.586666a158.261333 158.261333 0 0 1-119.733333 58.965334l-21.909333 0.469333C148.437333 407.808 106.666667 450.816 106.666667 503.498667V821.333333c0 64.8 52.106667 117.333333 116.394666 117.333334h412.522667c84.736 0 160.373333-53.568 189.12-133.92l85.696-239.584c21.802667-60.96-9.536-128.202667-70.005333-150.186667a115.552 115.552 0 0 0-39.488-6.954667H621.674667z" :fill="praised? '#2d7cf6': '#8a8a8a'"></path>
          </svg>
        </el-icon>
        <el-icon v-show="btnLoading" class="is-loading" size="20"><Loading /></el-icon>
      </el-button>
      <div class="column-article-like-info">
        <p class="column-article-like-text">{{praiseCnt > 0 ? praiseCnt + '人已点赞' : '真诚点赞 诚不我欺'}}</p>
        <div class="column-article-like-users">
          <a class="column-article-like-user" :href="'/user/' + item.userId" v-for="(item, id) in praisedUsers" :key="id">
            <img :src="item.avatar" />
          </a>
        </div>
      </div>
    </div>
  </section>
</template>

<script setup lang="ts">
import { format } from 'date-fns'
import '@/assets/md-preview.css'
import { inject, ref, watch } from 'vue'
import { Delete, Edit, Loading } from '@element-plus/icons-vue'
import { MdPreview } from 'md-editor-v3'
import { useGlobalStore } from '@/stores/global'
import { doGet } from '@/http/BackendRequests'
import type { CommonResponse } from '@/http/ResponseTypes/CommonResponseType'
import { ARTICLE_LIKE_COLLECT_URL } from '@/http/URL'
import { OperateTypeEnum } from '@/constants/OperateTypeConstants'
import type { SimpleUserInfo } from '@/http/ResponseTypes/UserInfoType/SimpleUserInfoType'
import type { ColumnArticlesResponseType } from '@/http/ResponseTypes/ColumnDetailType/ColumnArticlesResponseType'

const globalStore = useGlobalStore()
const global = globalStore.global

const showLoginDialog = inject<() => void>('loginDialogClicked')

const props = defineProps<{
  articleVo: ColumnArticlesResponseType,
}>()

const btnLoading = ref(false)
const praiseCnt = ref(0)
const praised = ref(false)
const praisedUsers = ref<SimpleUserInfo[]>([])

watch(() => props.articleVo.article, (newVal) => {
  praiseCnt.value = newVal.count.praiseCount
  praised.value = newVal.praised || false
  praisedUsers.value = newVal.praisedUsers || []
})

const likeArticle = () => {
  if (!global.isLogin) {
    if (showLoginDialog) showLoginDialog()
    return
  }
  btnLoading.value = true
  if (praised.value) {
    doGet<CommonResponse>(ARTICLE_LIKE_COLLECT_URL, {
      articleId: props.articleVo.article.articleId,
      type: OperateTypeEnum.CANCEL_PRAISE,
    }).then(() => {
      praiseCnt.value--
      praised.value = false
      praisedUsers.value = praisedUsers.value?.filter((item) => item.userId !== global.user.id)
    }).catch((error) => console.error(error))
      .finally(() => { btnLoading.value = false })
  } else {
    doGet<CommonResponse>(ARTICLE_LIKE_COLLECT_URL, {
      articleId: props.articleVo.article.articleId,
      type: OperateTypeEnum.PRAISE,
    }).then(() => {
      praiseCnt.value++
      praised.value = true
      praisedUsers.value.push({
        userId: global.user.id,
        avatar: global.user.photo,
        profile: global.user.profile,
        name: global.user.userName,
      })
    }).catch((error) => console.error(error))
      .finally(() => { btnLoading.value = false })
  }
}
</script>

<style scoped>
.column-article-section {
  background: var(--pai-bg-white-fff, #ffffff);
  border-radius: 16px;
  padding: 2rem 2.5rem;
  box-shadow: 0 1px 3px rgba(26, 29, 39, 0.04);
}

.column-article-badge {
  display: inline-block;
  font-size: 0.7rem;
  font-weight: 600;
  color: #fff;
  background: var(--pai-brand-1-normal);
  padding: 0.15rem 0.6rem;
  border-radius: 4px;
  margin-bottom: 0.75rem;
}

.column-article-title {
  font-size: 1.6rem;
  font-weight: 800;
  line-height: 1.35;
  color: var(--pai-color-3-black, #1e2029);
  margin: 0 0 1rem;
  letter-spacing: -0.01em;
}

.column-article-meta {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  font-size: 0.85rem;
  color: var(--pai-color-999-gray, #8c8f9c);
  margin-bottom: 0.75rem;
  flex-wrap: wrap;
}

.column-article-divider {
  color: var(--pai-color-5-gray, #d0d3dd);
}

.column-article-actions {
  display: flex;
  gap: 0.5rem;
  margin-left: auto;
}

.column-article-action-btn {
  display: inline-flex;
  align-items: center;
  gap: 0.25rem;
  padding: 0.2rem 0.6rem;
  font-size: 0.78rem;
  color: var(--pai-color-4-gray, #484d5e);
  cursor: pointer;
  border-radius: 6px;
  transition: all 0.2s ease;
}

.column-article-action-btn:hover {
  background: var(--pai-brand-7-light);
  color: var(--pai-brand-1-normal);
}

.column-article-action-btn--danger:hover {
  background: rgba(248, 89, 89, 0.1);
  color: var(--pai-brand-6-mq);
}

.column-article-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 0.4rem;
  margin-bottom: 1.5rem;
  padding-bottom: 1.25rem;
  border-bottom: 1px solid var(--pai-bg-light-2, #eef1f7);
}

.column-article-tag {
  font-size: 0.75rem;
}

.column-article-content {
  margin-bottom: 2rem;
}

.column-article-nav {
  display: flex;
  justify-content: space-between;
  gap: 1rem;
  margin-bottom: 2rem;
}

.column-article-nav-btn {
  display: inline-flex;
  align-items: center;
  gap: 0.4rem;
  padding: 0.5rem 1rem;
  font-size: 0.85rem;
  font-weight: 500;
  color: var(--pai-color-4-gray, #484d5e);
  text-decoration: none;
  border: 1.5px solid var(--pai-border-color-1, #d6dae6);
  border-radius: 10px;
  transition: all 0.25s ease;
}

.column-article-nav-btn svg {
  width: 18px;
  height: 18px;
}

.column-article-nav-btn:hover {
  color: var(--pai-brand-1-normal);
  border-color: var(--pai-brand-1-normal);
  background: var(--pai-brand-7-light);
}

.column-article-lock {
  text-align: center;
  padding: 2rem;
  margin-bottom: 2rem;
  background: var(--pai-bg-light-1, #f4f6fa);
  border-radius: 12px;
}

.column-article-lock-btn {
  font-size: 0.95rem;
  font-weight: 600;
  color: var(--pai-brand-1-normal);
  text-decoration: none;
  cursor: pointer;
}

.column-article-like {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 0.75rem;
  padding: 2rem 0 0.5rem;
  border-top: 1px solid var(--pai-bg-light-2, #eef1f7);
  margin-top: 1rem;
}

.column-article-like :deep(.el-button) {
  border: 1.5px solid var(--pai-border-color-1, #d6dae6);
  transition: all 0.25s ease;
}

.column-article-like :deep(.el-button:hover) {
  border-color: var(--pai-brand-1-normal);
  background: var(--pai-brand-7-light);
  transform: scale(1.08);
}

.column-article-like-info {
  text-align: center;
}

.column-article-like-text {
  font-size: 0.85rem;
  color: var(--pai-color-999-gray, #8c8f9c);
  margin: 0 0 0.5rem;
}

.column-article-like-users {
  display: flex;
  justify-content: center;
  gap: 0.3rem;
}

.column-article-like-user img {
  width: 28px;
  height: 28px;
  border-radius: 50%;
  object-fit: cover;
}

@media (max-width: 768px) {
  .column-article-section {
    padding: 1.25rem;
  }
  .column-article-title {
    font-size: 1.35rem;
  }
}
</style>
