<template>
  <section class="user-card">
    <div class="user-card-header">
      <div class="user-card-author">
        <a :href="'/user/' + user.userId" class="user-card-avatar-link">
          <div class="user-card-avatar" :style="{ backgroundImage: 'url(' + user.photo + ')' }"></div>
        </a>
        <div class="user-card-info">
          <a :href="'/user/' + user.userId" class="user-card-name" :title="user.userName">
            {{ user.userName }}
          </a>
          <span class="user-card-join">已加入 {{ user.joinDayCount }} 天</span>
        </div>
      </div>
    </div>

    <div class="user-card-actions">
      <el-button
        @click="follow"
        :disabled="followBtnDisabled"
        class="user-card-btn"
        :class="{ 'is-followed': userFollowed }"
        v-if="global.isLogin && global.user.userId != user.userId"
      >
        {{ userFollowed ? '取消关注' : '关注' }}
      </el-button>
      <el-button type="primary" class="user-card-btn" @click="router.push('/column')">
        教程
      </el-button>
    </div>

    <ul class="user-card-stats">
      <li class="user-card-stat">
        <a :href="'/user/' + user.userId" class="user-card-stat-link">
          <span class="user-card-stat-title">文章</span>
          <span class="user-card-stat-num">{{ user.articleCount }}</span>
        </a>
      </li>
      <li class="user-card-stat-divider"></li>
      <li class="user-card-stat">
        <a :href="'/user/' + user.userId" class="user-card-stat-link">
          <span class="user-card-stat-title">点赞</span>
          <span class="user-card-stat-num">{{ user.praiseCount }}</span>
        </a>
      </li>
      <li class="user-card-stat-divider"></li>
      <li class="user-card-stat">
        <a :href="'/user/' + user.userId" class="user-card-stat-link">
          <span class="user-card-stat-title">收藏</span>
          <span class="user-card-stat-num">{{ user.collectionCount }}</span>
        </a>
      </li>
      <li class="user-card-stat-divider"></li>
      <li class="user-card-stat">
        <a :href="'/user/' + user.userId" class="user-card-stat-link">
          <span class="user-card-stat-title">粉丝</span>
          <span class="user-card-stat-num">{{ user.fansCount }}</span>
        </a>
      </li>
    </ul>
  </section>
</template>

<script setup lang="ts">
import type { CommonResponse, GlobalResponse } from '@/http/ResponseTypes/CommonResponseType'
import type { UserStatisticInfo } from '@/http/ResponseTypes/UserInfoType/UserStatisticInfoType'
import { useRouter } from 'vue-router'
import { ref } from 'vue'
import { doPost } from '@/http/BackendRequests'
import { USER_FOLLOW_URL } from '@/http/URL'

const router = useRouter()
const props = defineProps<{
  user: UserStatisticInfo
  global: GlobalResponse
}>()

const followBtnDisabled = ref(false)
const userFollowed = ref(props.user.followed)

const follow = () => {
  followBtnDisabled.value = true
  doPost<CommonResponse>(USER_FOLLOW_URL, {
    userId: props.user.userId,
    followed: !userFollowed.value
  })
    .then(() => {
      userFollowed.value = !userFollowed.value
    })
    .catch((err) => {
      console.error('UserCard error:', err)
    })
    .finally(() => {
      followBtnDisabled.value = false
    })
}
</script>

<style scoped>
.user-card {
  background: var(--pai-bg-white-fff, #ffffff);
  border-radius: 16px;
  padding: 1.5rem 1.75rem;
  box-shadow: 0 1px 3px rgba(26, 29, 39, 0.04);
}

.user-card-header {
  margin-bottom: 1rem;
}

.user-card-author {
  display: flex;
  align-items: center;
  gap: 0.75rem;
}

.user-card-avatar-link {
  text-decoration: none;
}

.user-card-avatar {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  background-size: cover;
  background-position: center;
  background-color: var(--pai-bg-light-2, #eef1f7);
}

.user-card-info {
  display: flex;
  flex-direction: column;
  gap: 0.15rem;
}

.user-card-name {
  font-size: 1rem;
  font-weight: 700;
  color: var(--pai-color-3-black, #1e2029);
  text-decoration: none;
  transition: color 0.2s;
}

.user-card-name:hover {
  color: var(--pai-brand-1-normal);
}

.user-card-join {
  font-size: 0.75rem;
  color: var(--pai-color-999-gray, #8c8f9c);
}

.user-card-actions {
  display: flex;
  gap: 0.5rem;
  margin-bottom: 1rem;
}

.user-card-btn {
  flex: 1;
  font-size: 0.8rem;
  border-radius: 8px;
}

.user-card-btn.is-followed {
  border-color: var(--pai-border-color-1, #d6dae6);
  color: var(--pai-color-4-gray, #484d5e);
}

.user-card-stats {
  display: flex;
  align-items: center;
  justify-content: space-around;
  list-style: none;
  margin: 0;
  padding: 0.75rem 0 0;
  border-top: 1px solid var(--pai-bg-light-2, #eef1f7);
}

.user-card-stat-link {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 0.2rem;
  text-decoration: none;
}

.user-card-stat-title {
  font-size: 0.72rem;
  color: var(--pai-color-999-gray, #8c8f9c);
}

.user-card-stat-num {
  font-family: 'JetBrains Mono', monospace;
  font-size: 1rem;
  font-weight: 700;
  color: var(--pai-color-3-black, #1e2029);
}

.user-card-stat-divider {
  width: 1px;
  height: 32px;
  background: var(--pai-bg-light-2, #eef1f7);
}
</style>
