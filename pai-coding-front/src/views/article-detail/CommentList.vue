<template>
  <!-- 评论列表 -->
  <div class="comment-section" id="commentList">
    <svg xmlns="http://www.w3.org/2000/svg" style="display:none;">
      <symbol id="icon-comment" viewBox="0 0 20 20" xmlns="http://www.w3.org/2000/svg">
        <path fill-rule="evenodd" clip-rule="evenodd"
              d="M4.62739 1.25C2.9347 1.25 1.5625 2.6222 1.5625 4.31489L1.56396 12.643C1.56403 14.3356 2.9362 15.7078 4.62885 15.7078H6.48326L6.93691 17.6869L6.93884 17.6948C7.16894 18.6441 8.28598 19.0599 9.08073 18.4921L12.7965 15.7078H15.5001C17.1928 15.7078 18.565 14.3355 18.565 12.6428L18.5635 4.31477C18.5635 2.62213 17.1913 1.25 15.4986 1.25H4.62739ZM5.98265 9.89255C6.68783 9.89255 7.2595 9.32089 7.2595 8.61571C7.2595 7.91053 6.68783 7.33887 5.98265 7.33887C5.27747 7.33887 4.70581 7.91053 4.70581 8.61571C4.70581 9.32089 5.27747 9.89255 5.98265 9.89255ZM9.95604 9.89255C10.6612 9.89255 11.2329 9.32089 11.2329 8.61571C11.2329 7.91053 10.6612 7.33887 9.95604 7.33887C9.25086 7.33887 8.6792 7.91053 8.6792 8.61571C8.6792 9.32089 9.25086 9.89255 9.95604 9.89255ZM15.2124 8.61571C15.2124 9.32089 14.6407 9.89255 13.9355 9.89255C13.2304 9.89255 12.6587 9.32089 12.6587 8.61571C12.6587 7.91053 13.2304 7.33887 13.9355 7.33887C14.6407 7.33887 15.2124 7.91053 15.2124 8.61571Z"></path>
      </symbol>
      <symbol id="icon-zan" viewBox="0 0 20 20" xmlns="http://www.w3.org/2000/svg">
        <path fill-rule="evenodd" clip-rule="evenodd"
              d="M13.0651 3.25923C12.6654 2.21523 12.1276 1.60359 11.4633 1.40559C10.8071 1.21 10.2539 1.48626 9.97848 1.67918C9.43962 2.05668 9.17297 2.64897 9.0009 3.12662C8.93522 3.30893 8.87504 3.50032 8.82077 3.67291L8.82077 3.67292C8.80276 3.73019 8.78541 3.78539 8.76872 3.8375C8.6974 4.06017 8.63455 4.23905 8.56561 4.38315C8.07104 5.41687 7.64014 6.034 7.2617 6.43277C6.89154 6.8228 6.5498 7.0275 6.18413 7.21038C5.8887 7.35813 5.69369 7.66144 5.69365 8.00211L5.69237 17.3908C5.6923 17.8783 6.08754 18.2736 6.57511 18.2736H14.8382C15.2621 18.2736 15.5829 18.1393 15.8149 17.9421C15.9234 17.8497 15.9985 17.7554 16.0484 17.6856C16.0695 17.6561 16.088 17.6282 16.0983 17.6126L16.1017 17.6075L16.1033 17.6051L16.1194 17.5857L16.1428 17.5478C16.913 16.3019 17.4472 15.3088 17.8659 14.1183C18.3431 12.7613 18.5849 11.5853 18.6874 10.6685C18.7871 9.77617 18.7612 9.07318 18.6558 8.68779C18.5062 8.14118 18.138 7.82653 17.7668 7.66617C17.4231 7.51771 17.0763 7.49836 16.8785 7.49807L13.1134 7.44551C13.662 5.19751 13.31 3.89889 13.0651 3.25923ZM1.251 8.0848C1.22726 7.5815 1.62891 7.16046 2.13277 7.16046H3.4408C3.92832 7.16046 4.32354 7.55568 4.32354 8.04321V17.4303C4.32354 17.9178 3.92832 18.313 3.4408 18.313H2.57554C2.10419 18.313 1.71599 17.9427 1.69378 17.4718L1.251 8.0848Z"></path>
      </symbol>
    </svg>

    <!-- 评论输入 -->
    <div class="comment-write">
      <img v-if="global.isLogin" :alt="global.user.userName" :src="global.user.photo" class="comment-write-avatar" />
      <div class="comment-write-content">
        <el-input
          @click="() => {if (!global.isLogin) {if(showLoginDialog) showLoginDialog()}}"
          v-model="textarea"
          :rows="3"
          resize="none"
          type="textarea"
          maxlength="512"
          :placeholder="global.isLogin ? '讨论应以学习和精进为目的。请勿发布不友善或者负能量的内容，与人为善，比聪明更重要！' : '请先登录后再评论'"
        />
        <div class="comment-write-action">
          <el-button type="primary" @click="commentSubmit" :disabled="textarea.length === 0 || isCommenting">
            发表评论
          </el-button>
        </div>
      </div>
    </div>

    <!-- 评论列表 -->
    <div class="comment-list">
      <div class="comment-list-header" v-if="comments && comments.length > 0">
        <h4>全部 <em>{{comments.length}}</em> 条评论</h4>
      </div>
      <div class="comment-list-item" v-for="(comment, id) in comments" :key="id">
        <CommentItem :comment="comment" :article="article"></CommentItem>
      </div>
      <div class="comment-list-empty" v-if="!comments || comments.length === 0">
        <p>暂无评论，快来抢沙发吧~</p>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import type { ArticleDetailResponse } from '@/http/ResponseTypes/ArticleDetailResponseType'
import CommentItem from '@/components/comment/CommentItem.vue'
import { useGlobalStore } from '@/stores/global'
import { inject, ref } from 'vue'
import { doPost } from '@/http/BackendRequests'
import { COMMENT_SUBMIT_URL } from '@/http/URL'
import { messageTip } from '@/util/utils'
import type { CommonResponse } from '@/http/ResponseTypes/CommonResponseType'
import type { ArticleCommentType } from '@/http/ResponseTypes/CommentType/ArticleCommentType'
import type { ArticleType } from '@/http/ResponseTypes/ArticleType/ArticleType'

const globalStore = useGlobalStore()
const global = globalStore.global
const showLoginDialog = inject<() => void>('loginDialogClicked')
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
  }).then((response) => {
    messageTip('评论成功', 'success')
    textarea.value = ''
    if (updateArticleComment) {
      updateArticleComment(response.data.result)
    }
  }).catch(() => {
    messageTip('评论失败', 'error')
  })
}

const props = defineProps<{
  hotComment: ArticleCommentType,
  comments: ArticleCommentType[],
  article: ArticleType,
}>()

const textarea = ref('')
</script>

<style scoped>
.comment-section {
  background: var(--pai-bg-white-fff, #ffffff);
  border-radius: 16px;
  padding: 1.5rem 2rem;
  margin-top: 1.5rem;
  box-shadow: 0 1px 3px rgba(26, 29, 39, 0.04);
}

/* ── Write ── */
.comment-write {
  display: flex;
  gap: 0.75rem;
  margin-bottom: 1.5rem;
}

.comment-write-avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  object-fit: cover;
  flex-shrink: 0;
}

.comment-write-content {
  flex: 1;
  min-width: 0;
}

.comment-write-content :deep(.el-textarea__inner) {
  border-radius: 12px;
  border: 1.5px solid var(--pai-border-color-1, #d6dae6);
  box-shadow: none;
  transition: border-color 0.2s ease, box-shadow 0.2s ease;
  font-size: 0.85rem;
  padding: 0.75rem;
}

.comment-write-content :deep(.el-textarea__inner:focus) {
  border-color: var(--pai-brand-1-normal);
  box-shadow: 0 0 0 3px rgba(45, 124, 246, 0.1);
}

.comment-write-action {
  display: flex;
  justify-content: flex-end;
  margin-top: 0.5rem;
}

.comment-write-action :deep(.el-button) {
  border-radius: 8px;
  font-weight: 500;
}

/* ── Header ── */
.comment-list-header h4 {
  font-size: 0.95rem;
  font-weight: 600;
  color: var(--pai-color-3-black, #1e2029);
  margin: 0 0 1rem;
  padding-bottom: 0.75rem;
  border-bottom: 1px solid var(--pai-bg-light-2, #eef1f7);
}

.comment-list-header em {
  font-style: normal;
  color: var(--pai-brand-1-normal);
}

/* ── Items ── */
.comment-list-item {
  padding: 0.75rem 0;
  border-bottom: 1px solid var(--pai-bg-light-2, #eef1f7);
}

.comment-list-item:last-child {
  border-bottom: none;
}

/* ── Empty ── */
.comment-list-empty {
  text-align: center;
  padding: 2rem 0;
}

.comment-list-empty p {
  font-size: 0.85rem;
  color: var(--pai-color-999-gray, #8c8f9c);
  margin: 0;
}

/* ── Responsive ── */
@media (max-width: 768px) {
  .comment-section {
    padding: 1rem;
  }
  .comment-write {
    flex-direction: column;
  }
}
</style>
