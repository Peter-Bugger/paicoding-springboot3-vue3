<template>
  <!-- 文章的侧边栏点赞收藏评论浮窗 -->
  <div class="article-suspended-panel hidden-when-screen-small">
    <!-- 点赞 -->
    <div class="mb-4">
      <el-badge :offset="[-5, 5]" :type="praised? 'danger': 'primary'" :value="praiseCnt" class="item" :hidden="praiseCnt === 0" >
        <el-button circle round size="large" @click="likeArticle">
          <el-icon v-show="!btnLoading" size="20"><svg t="1719494245215" class="icon" viewBox="0 0 1024 1024" xmlns="http://www.w3.org/2000/svg" p-id="2214" id="mx_n_1719494245217" width="16" height="18"><path d="M621.674667 408.021333c16.618667-74.24 28.224-127.936 34.837333-161.194666C673.152 163.093333 629.941333 85.333333 544.298667 85.333333c-77.226667 0-116.010667 38.378667-138.88 115.093334l-0.586667 2.24c-13.728 62.058667-34.72 110.165333-62.506667 144.586666a158.261333 158.261333 0 0 1-119.733333 58.965334l-21.909333 0.469333C148.437333 407.808 106.666667 450.816 106.666667 503.498667V821.333333c0 64.8 52.106667 117.333333 116.394666 117.333334h412.522667c84.736 0 160.373333-53.568 189.12-133.92l85.696-239.584c21.802667-60.96-9.536-128.202667-70.005333-150.186667a115.552 115.552 0 0 0-39.488-6.954667H621.674667z" :fill="praised? '#2d7cf6': '#8a8a8a'" p-id="2215"></path></svg></el-icon>
          <el-icon v-show="btnLoading" class="is-loading" size="20"><Loading /></el-icon>
        </el-button>
      </el-badge>
    </div>
    <!-- 评论  -->
    <div class="mb-4 p-0">
      <el-badge :offset="[-5, 5]" :type="commented? 'danger': 'primary'" :value="commentCnt" :hidden="commentCnt === 0" class="item">
        <el-button circle round size="large">
          <el-icon v-show="!btnLoading" size="20" :color="commented? '#2d7cf6': '#8a8a8a'"><Comment/></el-icon>
          <el-icon v-show="btnLoading" class="is-loading" size="20"><Loading /></el-icon>
        </el-button>
      </el-badge>
    </div>
    <!-- 收藏 -->
    <div class="mb-4">
      <el-badge :offset="[-5, 5]" :type="collected? 'danger': 'primary'" :value="collectCnt" class="item" :hidden="collectCnt === 0">
        <el-button circle round size="large" @click="collectArticle">
          <template #default>
            <el-icon v-show="!btnLoading" size="20" :color="collected? '#2d7cf6': '#8a8a8a'"><StarFilled/></el-icon>
            <el-icon v-show="btnLoading" class="is-loading" size="20"><Loading /></el-icon>
          </template>
        </el-button>
      </el-badge>
    </div>
  </div>

  <!-- 文章内容 -->
  <section class="article-detail-section">
    <span class="article-original-badge">原创</span>

    <h1 class="article-detail-title">{{articleVo.article.title}}</h1>

    <div class="article-detail-meta">
      <div class="article-detail-author">
        <el-avatar :src="articleVo.article.authorAvatar" size="small"></el-avatar>
        <el-link
          v-if="articleVo.article.author"
          :href="'/user/' + articleVo.article.author"
          class="article-detail-author-name"
          type="primary"
        >
          {{articleVo.article.authorName}}
        </el-link>
      </div>
      <span class="article-detail-date">{{ format(new Date(Number(articleVo.article.createTime)), 'yyyy年MM月dd日')}}</span>
      <span class="article-detail-divider">|</span>
      <span class="article-detail-reads">{{'阅读 ' + articleVo.article.count.readCount}}</span>
      <span class="article-detail-status" v-if="articleVo.article.status !== 1" style="color: var(--pai-brand-6-mq)">{{articleVo.article.status == 0? '(草稿)' : '(审核中)'}}</span>
      <div class="article-detail-actions" v-if="global.isLogin && articleVo.article.author == global.user.id">
        <span @click="() => {router.push('/article/edit/'+articleVo.article.articleId)}" class="article-detail-action-btn">
          <el-icon :size="16"><Edit /></el-icon>
          <span>编辑</span>
        </span>
        <span @click="deleteDialog=true" class="article-detail-action-btn article-detail-action-btn--danger">
          <el-icon :size="16"><Delete /></el-icon>
          <span>删除</span>
        </span>
      </div>
    </div>

    <!-- 文章标签 -->
    <div class="article-detail-tags" v-if="articleVo.article.tags?.length">
      <el-tag class="article-detail-tag" v-for="tagItem in articleVo.article.tags" :key="tagItem.tagId" effect="plain" round>{{tagItem.tag}}</el-tag>
    </div>

    <div class="article-detail-content">
      <MdPreview :editor-id="'id'" :model-value="articleVo.article.content"></MdPreview>
    </div>

    <!-- 左右切换 -->
    <div class="article-detail-nav" v-if="articleVo.other && articleVo.other.flip">
      <a class="article-detail-nav-btn article-detail-nav-btn--prev"
         :href="articleVo.other.flip.prevHref"
         v-if="articleVo.other.flip.prevShow"
      >
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="m15 18-6-6 6-6"/></svg>
        <span>上一篇</span>
      </a>
      <a class="article-detail-nav-btn article-detail-nav-btn--next"
         :href="articleVo.other.flip.nextHref"
         v-if="articleVo.other.flip.nextShow"
      >
        <span>下一篇</span>
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="m9 18 6-6-6-6"/></svg>
      </a>
    </div>

    <div v-if="articleVo.other && articleVo.other.readType === 1 && !global.isLogin">
      <div class="article-detail-lock">
        <a class="article-detail-lock-btn">登录之后即可阅读全文</a>
      </div>
    </div>

    <div v-if="articleVo.other && articleVo.other.readType === 3 && !(global.user != null && global.user.starStatus == 'FORMAL')">
      <div class="article-detail-lock">
        <a class="article-detail-lock-btn">已加入二哥编程星球，即刻绑定星球编号解锁🔐</a>
      </div>
    </div>

    <!-- 底部点赞 -->
    <div class="article-detail-like">
      <el-button circle round size="large" @click="likeArticle">
        <el-icon v-show="!btnLoading" size="20">
          <svg viewBox="0 0 1024 1024" xmlns="http://www.w3.org/2000/svg" width="16" height="18">
            <path d="M621.674667 408.021333c16.618667-74.24 28.224-127.936 34.837333-161.194666C673.152 163.093333 629.941333 85.333333 544.298667 85.333333c-77.226667 0-116.010667 38.378667-138.88 115.093334l-0.586667 2.24c-13.728 62.058667-34.72 110.165333-62.506667 144.586666a158.261333 158.261333 0 0 1-119.733333 58.965334l-21.909333 0.469333C148.437333 407.808 106.666667 450.816 106.666667 503.498667V821.333333c0 64.8 52.106667 117.333333 116.394666 117.333334h412.522667c84.736 0 160.373333-53.568 189.12-133.92l85.696-239.584c21.802667-60.96-9.536-128.202667-70.005333-150.186667a115.552 115.552 0 0 0-39.488-6.954667H621.674667z" :fill="praised? '#2d7cf6': '#8a8a8a'"></path>
          </svg>
        </el-icon>
        <el-icon v-show="btnLoading" class="is-loading" size="20"><Loading /></el-icon>
      </el-button>
      <div class="article-detail-like-info">
        <p class="article-detail-like-text">{{praiseCnt > 0? praiseCnt + '人已点赞': '真诚点赞 诚不我欺'}}</p>
        <div class="article-detail-like-users">
          <a class="article-detail-like-user" :href="'/user/' + item.userId" v-for="(item, id) in praisedUsers" :key="id">
            <el-avatar :src="item.avatar" size="small"></el-avatar>
          </a>
        </div>
      </div>
    </div>
  </section>

  <!-- 删除确认弹窗 -->
  <el-dialog
    v-if="global.user && global.user.id == articleVo.article.author"
    v-model="deleteDialog"
    title="删除提醒"
    width="420"
    center
    class="article-delete-dialog"
  >
    <div class="article-delete-body">
      <strong>确定删除 《{{articleVo.article.title}}》 吗？</strong>
    </div>
    <template #footer>
      <div class="dialog-footer">
        <el-button type="danger" @click="deleteArticle">确认</el-button>
        <el-button @click="deleteDialog = false">取消</el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { format } from 'date-fns'
import '@/assets/md-preview.css'
import { inject, ref, watch } from 'vue'
import { Comment, Delete, Edit, Loading, StarFilled } from '@element-plus/icons-vue'
import type { ArticleDetailResponse } from '@/http/ResponseTypes/ArticleDetailResponseType'
import { MdPreview } from 'md-editor-v3'
import { useGlobalStore } from '@/stores/global'
import { doGet } from '@/http/BackendRequests'
import type { CommonResponse } from '@/http/ResponseTypes/CommonResponseType'
import { ARTICLE_DELETE_URL, ARTICLE_LIKE_COLLECT_URL } from '@/http/URL'
import { OperateTypeEnum } from '@/constants/OperateTypeConstants'
import type { SimpleUserInfo } from '@/http/ResponseTypes/UserInfoType/SimpleUserInfoType'
import { useRouter } from 'vue-router'

const globalStore = useGlobalStore()
const global = globalStore.global
const router = useRouter()

const showLoginDialog = inject<() => void>('loginDialogClicked')

const props = defineProps<{
  articleVo: ArticleDetailResponse,
}>()

const deleteDialog = ref(false)

const deleteArticle = () => {
  doGet<CommonResponse>(ARTICLE_DELETE_URL, {
    articleId: props.articleVo.article.articleId,
  })
    .then(() => {
      router.push('/')
    }).catch((error) => {
    console.error(error)
  })
}

const btnLoading = ref(false)
const praiseCnt = ref(props.articleVo.article.count.praiseCount)
const commentCnt = ref(props.articleVo.article.count.commentCount)
const collectCnt = ref(props.articleVo.article.count.collectionCount)
const praised = ref(props.articleVo.article.praised)
const commented = ref(props.articleVo.article.commented)
const collected = ref(props.articleVo.article.collected)
const praisedUsers = ref<SimpleUserInfo[]>(props.articleVo.article.praisedUsers || [])

watch(() => props.articleVo.article, (newVal) => {
  praiseCnt.value = newVal.count.praiseCount
  commentCnt.value = newVal.count.commentCount
  collectCnt.value = newVal.count.collectionCount
  praised.value = newVal.praised || false
  commented.value = newVal.commented || false
  collected.value = newVal.collected || false
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
    })
      .then(() => {
        praiseCnt.value--
        praised.value = false
        praisedUsers.value = praisedUsers.value?.filter((item) => item.userId !== global.user.id)
      }).catch((error) => console.error(error))
      .finally(() => { btnLoading.value = false })
  } else {
    doGet<CommonResponse>(ARTICLE_LIKE_COLLECT_URL, {
      articleId: props.articleVo.article.articleId,
      type: OperateTypeEnum.PRAISE,
    })
      .then(() => {
        praiseCnt.value++
        praised.value = true
        praisedUsers.value.push({
          userId: global.user.id,
          avatar: global.user.photo,
          profile: global.user.profile,
          name: global.user.userName,
        })
      })
      .catch((error) => console.error(error))
      .finally(() => { btnLoading.value = false })
  }
}

const collectArticle = () => {
  if (!global.isLogin) {
    if (showLoginDialog) showLoginDialog()
    return
  }
  btnLoading.value = true
  if (collected.value) {
    doGet<CommonResponse>(ARTICLE_LIKE_COLLECT_URL, {
      articleId: props.articleVo.article.articleId,
      type: OperateTypeEnum.CANCEL_COLLECTION,
    })
      .then(() => {
        collectCnt.value--
        collected.value = false
      }).catch((error) => console.error(error))
      .finally(() => { btnLoading.value = false })
  } else {
    doGet<CommonResponse>(ARTICLE_LIKE_COLLECT_URL, {
      articleId: props.articleVo.article.articleId,
      type: OperateTypeEnum.COLLECTION,
    })
      .then(() => {
        collectCnt.value++
        collected.value = true
      })
      .catch((error) => console.error(error))
      .finally(() => { btnLoading.value = false })
  }
}
</script>

<style scoped>
/* ── Suspended Action Panel ── */
.article-suspended-panel {
  position: fixed;
  left: max(calc((100vw - 1200px) / 2 - 70px), 8px);
  top: 50%;
  transform: translateY(-50%);
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 0.25rem;
  z-index: 10;
}

.article-suspended-panel :deep(.el-button) {
  border: 1.5px solid var(--pai-border-color-1, #d6dae6);
  background: var(--pai-bg-white-fff, #ffffff);
  transition: all 0.25s ease;
}

.article-suspended-panel :deep(.el-button:hover) {
  border-color: var(--pai-brand-1-normal);
  background: var(--pai-brand-7-light);
  transform: scale(1.08);
}

/* ── Article Detail Section ── */
.article-detail-section {
  position: relative;
  background: var(--pai-bg-white-fff, #ffffff);
  border-radius: 16px;
  padding: 2rem 2.5rem;
  box-shadow: 0 1px 3px rgba(26, 29, 39, 0.04);
}

.article-original-badge {
  display: inline-block;
  font-size: 0.7rem;
  font-weight: 600;
  color: #fff;
  background: var(--pai-brand-1-normal);
  padding: 0.15rem 0.6rem;
  border-radius: 4px;
  margin-bottom: 0.75rem;
  letter-spacing: 0.04em;
}

.article-detail-title {
  font-size: 1.75rem;
  font-weight: 800;
  line-height: 1.35;
  color: var(--pai-color-3-black, #1e2029);
  margin: 0 0 1rem;
  letter-spacing: -0.01em;
}

/* ── Meta ── */
.article-detail-meta {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  font-size: 0.85rem;
  color: var(--pai-color-999-gray, #8c8f9c);
  margin-bottom: 0.75rem;
  flex-wrap: wrap;
}

.article-detail-author {
  display: flex;
  align-items: center;
  gap: 0.4rem;
}

.article-detail-author-name {
  font-size: 0.85rem;
  font-weight: 500;
}

.article-detail-divider {
  color: var(--pai-color-5-gray, #d0d3dd);
}

.article-detail-actions {
  display: flex;
  gap: 0.5rem;
  margin-left: auto;
}

.article-detail-action-btn {
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

.article-detail-action-btn:hover {
  background: var(--pai-brand-7-light);
  color: var(--pai-brand-1-normal);
}

.article-detail-action-btn--danger:hover {
  background: rgba(248, 89, 89, 0.1);
  color: var(--pai-brand-6-mq);
}

/* ── Tags ── */
.article-detail-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 0.4rem;
  margin-bottom: 1.5rem;
  padding-bottom: 1.25rem;
  border-bottom: 1px solid var(--pai-bg-light-2, #eef1f7);
}

.article-detail-tag {
  font-size: 0.75rem;
  --el-tag-bg-color: var(--pai-brand-7-light);
  --el-tag-text-color: var(--pai-brand-1-normal);
  --el-tag-border-color: transparent;
  --el-tag-hover-color: var(--pai-brand-1-normal);
}

/* ── Content ── */
.article-detail-content {
  margin-bottom: 2rem;
}

/* ── Navigation ── */
.article-detail-nav {
  display: flex;
  justify-content: space-between;
  gap: 1rem;
  margin-bottom: 2rem;
}

.article-detail-nav-btn {
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

.article-detail-nav-btn svg {
  width: 18px;
  height: 18px;
}

.article-detail-nav-btn:hover {
  color: var(--pai-brand-1-normal);
  border-color: var(--pai-brand-1-normal);
  background: var(--pai-brand-7-light);
}

/* ── Lock ── */
.article-detail-lock {
  text-align: center;
  padding: 2rem;
  margin-bottom: 2rem;
  background: var(--pai-bg-light-1, #f4f6fa);
  border-radius: 12px;
}

.article-detail-lock-btn {
  font-size: 0.95rem;
  font-weight: 600;
  color: var(--pai-brand-1-normal);
  text-decoration: none;
  cursor: pointer;
}

/* ── Like Section ── */
.article-detail-like {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 0.75rem;
  padding: 2rem 0 0.5rem;
  border-top: 1px solid var(--pai-bg-light-2, #eef1f7);
  margin-top: 1rem;
}

.article-detail-like :deep(.el-button) {
  border: 1.5px solid var(--pai-border-color-1, #d6dae6);
  background: var(--pai-bg-white-fff, #ffffff);
  transition: all 0.25s ease;
}

.article-detail-like :deep(.el-button:hover) {
  border-color: var(--pai-brand-1-normal);
  background: var(--pai-brand-7-light);
  transform: scale(1.08);
}

.article-detail-like-info {
  text-align: center;
}

.article-detail-like-text {
  font-size: 0.85rem;
  color: var(--pai-color-999-gray, #8c8f9c);
  margin: 0 0 0.5rem;
}

.article-detail-like-users {
  display: flex;
  justify-content: center;
  gap: 0.3rem;
}

/* ── Responsive ── */
@media (max-width: 768px) {
  .article-detail-section {
    padding: 1.25rem;
  }
  .article-detail-title {
    font-size: 1.35rem;
  }
  .article-suspended-panel {
    display: none;
  }
}
</style>
