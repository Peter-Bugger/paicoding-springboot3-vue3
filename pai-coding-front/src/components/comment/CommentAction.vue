<template>
  <!-- 一级评论回复 -->
  <div class="comment-actions">
    <div :class="{'comment-action': true, 'comment-action--liked': commentPraised}" @click="likeComment">
      <el-button text :loading="btnLoading" class="comment-action-btn">
        <svg width="15" height="15"><use xlink:href="#icon-zan"></use></svg>
        <span>{{praiseCnt > 0 ? praiseCnt : '点赞'}}</span>
      </el-button>
    </div>
    <div class="comment-action" @click="replyStatusChange">
      <el-button text class="comment-action-btn">
        <svg width="15" height="15"><use xlink:href="#icon-comment"></use></svg>
        <span v-if="!replyEnabled">回复{{comment.commentCount > 0 ? ' ' + comment.commentCount : ''}}</span>
        <span v-else>取消回复</span>
      </el-button>
    </div>
  </div>
  <div v-if="replyEnabled" class="comment-reply-form">
    <el-input
      v-model="textarea"
      :rows="2"
      resize="none"
      type="textarea"
      :placeholder="'回复@' + comment.userName + (comment.userId == article.author ? '（作者）' : '')"
    />
    <div class="comment-reply-action">
      <el-button @click="commentSubmit" :disabled="textarea.length === 0 || isCommenting" type="primary" size="small">
        评论
      </el-button>
    </div>
  </div>
</template>

<script setup lang="ts">
import type { ArticleDetailResponse } from '@/http/ResponseTypes/ArticleDetailResponseType'
import { inject, ref } from 'vue'
import { useGlobalStore } from '@/stores/global'
import { doGet, doPost } from '@/http/BackendRequests'
import type { CommonResponse } from '@/http/ResponseTypes/CommonResponseType'
import { COMMENT_LIKE_URL, COMMENT_SUBMIT_URL } from '@/http/URL'
import { OperateTypeEnum } from '@/constants/OperateTypeConstants'
import { messageTip } from '@/util/utils'
import type { ArticleType } from '@/http/ResponseTypes/ArticleType/ArticleType'
import type { ArticleCommentType } from '@/http/ResponseTypes/CommentType/ArticleCommentType'

const globalStore = useGlobalStore()
const global = globalStore.global
const showLoginDialog = inject<() => void>('loginDialogClicked')

const props = defineProps<{
  comment: ArticleCommentType,
  article: ArticleType,
}>()

const commentPraised = ref(props.comment.praised)
const textarea = ref('')
const replyEnabled = ref(false)
const btnLoading = ref(false)
const praiseCnt = ref(props.comment.praiseCount)

const replyStatusChange = () => {
  replyEnabled.value = !replyEnabled.value
}

const likeComment = () => {
  if (!global.isLogin) {
    if (showLoginDialog) showLoginDialog()
    return
  }
  btnLoading.value = true
  if (commentPraised.value) {
    doGet<CommonResponse>(COMMENT_LIKE_URL, {
      commentId: props.comment.commentId,
      type: OperateTypeEnum.CANCEL_PRAISE,
    }).then(() => {
      praiseCnt.value--
      commentPraised.value = false
    }).catch((error) => console.error(error))
      .finally(() => { btnLoading.value = false })
  } else {
    doGet<CommonResponse>(COMMENT_LIKE_URL, {
      commentId: props.comment.commentId,
      type: OperateTypeEnum.PRAISE,
    }).then(() => {
      praiseCnt.value++
      commentPraised.value = true
    }).catch((error) => console.error(error))
      .finally(() => { btnLoading.value = false })
  }
}

const updateArticleComment = inject<(response: ArticleDetailResponse) => void>('updateArticleComment')
const isCommenting = ref(false)

const commentSubmit = () => {
  if (!global.isLogin) {
    if (showLoginDialog) showLoginDialog()
    return
  }
  doPost<CommonResponse>(COMMENT_SUBMIT_URL, {
    articleId: props.article.articleId,
    commentContent: textarea.value,
    parentCommentId: Number(props.comment.commentId),
    topCommentId: Number(props.comment.commentId),
  }).then((response) => {
    messageTip('评论成功', 'success')
    textarea.value = ''
    replyEnabled.value = false
    if (updateArticleComment) {
      updateArticleComment(response.data.result)
    }
  }).catch(() => {
    messageTip('评论失败', 'error')
  })
}
</script>

<style scoped>
.comment-actions {
  display: flex;
  gap: 0.25rem;
  margin-top: 0.3rem;
}

.comment-action-btn {
  font-size: 0.78rem;
  color: var(--pai-color-999-gray, #8c8f9c);
  padding: 2px 6px;
  border-radius: 6px;
  transition: all 0.2s ease;
}

.comment-action-btn:hover {
  color: var(--pai-brand-1-normal);
  background: var(--pai-brand-7-light);
}

.comment-action--liked .comment-action-btn {
  color: var(--pai-brand-1-normal);
}

.comment-reply-form {
  margin-top: 0.5rem;
  padding-left: 0.5rem;
}

.comment-reply-form :deep(.el-textarea__inner) {
  border-radius: 10px;
  border: 1.5px solid var(--pai-border-color-1, #d6dae6);
  box-shadow: none;
  font-size: 0.85rem;
}

.comment-reply-form :deep(.el-textarea__inner:focus) {
  border-color: var(--pai-brand-1-normal);
  box-shadow: 0 0 0 3px rgba(45, 124, 246, 0.1);
}

.comment-reply-action {
  display: flex;
  justify-content: flex-end;
  margin-top: 0.4rem;
}

.comment-reply-action :deep(.el-button) {
  border-radius: 6px;
}
</style>
