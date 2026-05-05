<template>
  <div class="followed-card">
    <div class="followed-card-body">
      <div class="followed-card-user">
        <el-avatar size="default" class="followed-card-avatar">
          <img :src="user.avatar" />
        </el-avatar>
        <span class="followed-card-name">{{ user.userName }}</span>
      </div>
      <span class="followed-card-action" v-if="global.user.id == route.params['userId']">
        <el-button
          @click="follow"
          :disabled="btnDisabled"
          size="small"
          class="followed-card-btn"
          :class="{ 'is-followed': userFollowed }"
          round
        >
          {{ userFollowed ? '取消关注' : '关注' }}
        </el-button>
      </span>
    </div>
  </div>
</template>

<script setup lang="ts">
import type { FollowUserInfoType } from '@/http/ResponseTypes/UserInfoType/FollowUserInfoType'
import { doPost } from '@/http/BackendRequests'
import type { CommonResponse } from '@/http/ResponseTypes/CommonResponseType'
import { USER_FOLLOW_URL } from '@/http/URL'
import { useGlobalStore } from '@/stores/global'
import { useRoute } from 'vue-router'
import { ref } from 'vue'

const globalStore = useGlobalStore()
const global = globalStore.global

const props = defineProps<{
  user: FollowUserInfoType
}>()

const route = useRoute()
const userFollowed = ref(props.user.followed)
const btnDisabled = ref(false)

const follow = () => {
  btnDisabled.value = true
  doPost<CommonResponse>(USER_FOLLOW_URL, {
    userId: props.user.userId,
    followed: !userFollowed.value
  })
    .then(() => {
      userFollowed.value = !userFollowed.value
    })
    .catch((err) => {
      console.log(err)
    })
    .finally(() => {
      btnDisabled.value = false
    })
}
</script>

<style scoped>
.followed-card {
  background: var(--pai-bg-white-fff, #ffffff);
  border-radius: 12px;
  padding: 0.75rem 1rem;
  box-shadow: 0 1px 3px rgba(26, 29, 39, 0.04);
  transition: box-shadow 0.2s;
}

.followed-card:hover {
  box-shadow: 0 4px 12px rgba(26, 29, 39, 0.06);
}

.followed-card-body {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 0.75rem;
}

.followed-card-user {
  display: flex;
  align-items: center;
  gap: 0.6rem;
  min-width: 0;
}

.followed-card-avatar {
  flex-shrink: 0;
}

.followed-card-name {
  font-size: 0.9rem;
  font-weight: 600;
  color: var(--pai-color-4-gray, #484d5e);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.followed-card-action {
  flex-shrink: 0;
}

.followed-card-btn {
  font-size: 0.75rem;
}

.followed-card-btn.is-followed {
  border-color: var(--pai-border-color-1, #d6dae6);
  color: var(--pai-color-999-gray, #8c8f9c);
}
</style>
