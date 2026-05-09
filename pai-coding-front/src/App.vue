<template>
  <router-view />
</template>

<script setup lang="ts">
import { watch } from 'vue'
import { useGlobalStore } from '@/stores/global'
import { useMessageStore } from '@/stores/message'
import { connectMessageWs, disconnectMessageWs } from '@/composables/useMessageWs'

const globalStore = useGlobalStore()
const messageStore = useMessageStore()

// 全局同步：从 global 信息同步私信未读数
watch(
  () => globalStore.global.privateMsgNum,
  (val) => {
    if (val != null && val >= 0) {
      messageStore.setUnreadTotal(val)
    }
  },
  { immediate: true }
)

// 登录状态变化：连接/断开私信 WebSocket
watch(
  () => globalStore.global.isLogin,
  (isLogin) => {
    if (isLogin) {
      // 已登录：连接 WebSocket
      connectMessageWs()
    } else {
      // 未登录：断开 WebSocket
      disconnectMessageWs()
    }
  },
  { immediate: true }
)
</script>
