<template>
  <HeaderBar></HeaderBar>

  <div class="user-page">
    <UserHomeInfo :vo="userInfo"></UserHomeInfo>
    <div class="user-wrap">
      <div class="user-content">
        <div class="user-body">
          <UserHomeNavBar></UserHomeNavBar>
        </div>
        <div class="user-sidebar hidden-when-screen-small">
          <UserAchievement :user="userInfo"></UserAchievement>
          <UserHistory :user="userInfo"></UserHistory>
        </div>
      </div>
    </div>
  </div>
  <Footer></Footer>
</template>

<script setup lang="ts">
import HeaderBar from '@/components/layout/HeaderBar.vue'
import Footer from '@/components/layout/Footer.vue'
import { onMounted, ref } from 'vue'
import { doGet } from '@/http/BackendRequests'
import type { CommonResponse } from '@/http/ResponseTypes/CommonResponseType'
import { USER_INFO_URL } from '@/http/URL'
import { useRoute } from 'vue-router'
import { useGlobalStore } from '@/stores/global'
import { defaultUserHomeInfo, type UserHomeInfoResponseType } from '@/http/ResponseTypes/UserHomeInfoResponseType'
import UserHomeInfo from '@/views/user/UserHomeInfo.vue'
import UserHomeNavBar from '@/views/user/UserHomeNavBar.vue'
import UserAchievement from '@/views/user/UserAchievement.vue'
import UserHistory from '@/views/user/UserHistory.vue'

const globalStore = useGlobalStore()

const route = useRoute()

const userInfo = ref<UserHomeInfoResponseType>({...defaultUserHomeInfo})
onMounted(() => {
  doGet<CommonResponse>(USER_INFO_URL, {
    userId: route.params.userId
  })
    .then((res) => {
      globalStore.setGlobal(res.data.global)
      Object.assign(userInfo.value, res.data.result)
    })
    .catch((err) => {
      console.log(err)
    })
})
</script>

<style scoped>
.user-page {
  min-height: calc(100vh - var(--footer-height) - var(--header-height));
  background: var(--pai-bg-light-1, #f4f6fa);
  padding-top: calc(var(--header-height, 60px));
}

.user-wrap {
  max-width: 1200px;
  margin: 0 auto;
  padding: 1.25rem;
}

.user-content {
  display: flex;
  gap: 1.5rem;
  align-items: flex-start;
}

.user-body {
  flex: 1;
  min-width: 0;
}

.user-sidebar {
  width: 300px;
  flex-shrink: 0;
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

@media (max-width: 768px) {
  .user-wrap {
    padding: 0.75rem;
  }
}
</style>
