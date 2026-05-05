<template>
  <div v-if="user.records.length > 0" class="followed-list">
    <div v-for="userItem in user.records" :key="userItem.userId" class="followed-list-item">
      <UserFollowedCard :user="userItem" />
    </div>
  </div>
  <div v-else class="followed-empty">
    暂时为空~
  </div>
</template>

<script setup lang="ts">
import type { BasicPageType } from '@/http/ResponseTypes/PageType/BasicPageType'
import type { FollowUserInfoType } from '@/http/ResponseTypes/UserInfoType/FollowUserInfoType'
import UserFollowedCard from '@/components/user/UserFollowedCard.vue'

defineProps<{
  user: BasicPageType<FollowUserInfoType>
}>()
</script>

<style scoped>
.followed-list {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.followed-list-item {
  /* wrapper for card spacing */
}

.followed-empty {
  text-align: center;
  font-size: 0.88rem;
  color: var(--pai-color-999-gray, #8c8f9c);
  padding: 2rem 0;
}
</style>
