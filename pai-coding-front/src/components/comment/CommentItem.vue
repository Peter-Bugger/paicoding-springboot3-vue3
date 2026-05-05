<template>
  <!-- 一级评论及其回复 -->
  <div class="comment-thread">
    <div class="comment-main">
      <a :href="'/user/' + comment.userId" target="_blank" class="comment-avatar-link">
        <img :src="comment.userPhoto" class="comment-avatar" />
      </a>
      <div class="comment-body">
        <div class="comment-head">
          <a :href="'/user/' + comment.userId" target="_blank" class="comment-name">
            {{ comment.userName + (comment.userId == article.author ? '（作者）' : '') }}
          </a>
          <span class="comment-time">{{ format(new Date(Number(comment.commentTime)), 'yyyy年MM月dd日') }}</span>
        </div>
        <div class="comment-content">{{ comment.commentContent }}</div>
        <CommentAction :comment="comment" :article="article"></CommentAction>
      </div>
    </div>

    <!-- 二级评论 -->
    <div v-if="comment.childComments && comment.childComments.length > 0" class="comment-replies">
      <div class="comment-reply" v-for="(reply, id) in comment.childComments" :key="id">
        <a :href="'/user/' + reply.userId" target="_blank" class="comment-avatar-link">
          <img :src="reply.userPhoto" class="comment-avatar comment-avatar--small" />
        </a>
        <div class="comment-body">
          <div class="comment-head">
            <a :href="'/user/' + reply.userId" target="_blank" class="comment-name">
              {{ reply.userName + (reply.userId == article.author ? '（作者）' : '') }}
            </a>
            <span class="comment-time">{{ format(new Date(Number(reply.commentTime)), 'yyyy年MM月dd日') }}</span>
          </div>
          <div class="comment-content">{{ reply.commentContent }}</div>
          <div v-if="reply.parentContent" class="comment-quote">
            <span>{{ reply.parentContent }}</span>
          </div>
          <SubCommentAction :comment="comment" :reply="reply" :article="article"></SubCommentAction>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { format } from 'date-fns'
import CommentAction from '@/components/comment/CommentAction.vue'
import SubCommentAction from '@/components/comment/SubCommentAction.vue'
import type { ArticleType } from '@/http/ResponseTypes/ArticleType/ArticleType'
import type { ArticleCommentType } from '@/http/ResponseTypes/CommentType/ArticleCommentType'

defineProps<{
  comment: ArticleCommentType;
  article: ArticleType
}>()
</script>

<style scoped>
.comment-thread {
  margin-bottom: 0;
}

.comment-main {
  display: flex;
  gap: 0.75rem;
}

.comment-avatar-link {
  flex-shrink: 0;
}

.comment-avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  object-fit: cover;
  display: block;
}

.comment-avatar--small {
  width: 32px;
  height: 32px;
}

.comment-body {
  flex: 1;
  min-width: 0;
}

.comment-head {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  margin-bottom: 0.35rem;
}

.comment-name {
  font-size: 0.85rem;
  font-weight: 600;
  color: var(--pai-color-4-gray, #484d5e);
  text-decoration: none;
}

.comment-name:hover {
  color: var(--pai-brand-1-normal);
}

.comment-time {
  font-size: 0.72rem;
  color: var(--pai-color-999-gray, #8c8f9c);
}

.comment-content {
  font-size: 0.88rem;
  color: var(--pai-color-3-black, #1e2029);
  line-height: 1.6;
  margin-bottom: 0.35rem;
}

/* ── Replies ── */
.comment-replies {
  margin-left: 3.25rem;
  margin-top: 0.75rem;
  padding: 0.5rem 0;
}

.comment-reply {
  display: flex;
  gap: 0.6rem;
  padding: 0.6rem 0;
}

.comment-reply + .comment-reply {
  border-top: 1px solid var(--pai-bg-light-2, #eef1f7);
}

/* ── Quote ── */
.comment-quote {
  background: var(--pai-bg-light-1, #f4f6fa);
  border: 1px solid var(--pai-border-color-1, #d6dae6);
  border-radius: 6px;
  padding: 0.25rem 0.75rem;
  line-height: 1.8;
  font-size: 0.8rem;
  color: var(--pai-color-3-gray, #6b7084);
  margin-top: 0.4rem;
  margin-bottom: 0.25rem;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

@media (max-width: 768px) {
  .comment-replies {
    margin-left: 1rem;
  }
}
</style>
