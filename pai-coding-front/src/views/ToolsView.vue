<template>
  <HeaderBar />

  <div class="tools-page">
    <div class="tools-container">
      <el-menu
        :default-active="defaultActiveTab"
        class="tools-menu"
        :collapse="isCollapse"
        router
      >
        <el-sub-menu index="1">
          <template #title>
            <el-icon><Location /></el-icon>
            <span>常用工具</span>
          </template>
          <el-menu-item-group>
            <template #title><span>文件处理</span></template>
            <el-menu-item index="excel">Excel表格处理</el-menu-item>
          </el-menu-item-group>
        </el-sub-menu>
      </el-menu>

      <div class="tools-content">
        <router-view></router-view>
      </div>
    </div>
  </div>

  <Footer></Footer>
</template>

<script setup lang="ts">
import HeaderBar from '@/components/layout/HeaderBar.vue'
import { useGlobalStore } from '@/stores/global'
import { onMounted, watchEffect } from 'vue'
import { doGet } from '@/http/BackendRequests'
import type { CommonResponse } from '@/http/ResponseTypes/CommonResponseType'
import { GLOBAL_INFO_URL } from '@/http/URL'
import Footer from '@/components/layout/Footer.vue'
import { ref } from 'vue'
import { Location } from '@element-plus/icons-vue'
import { useRoute } from 'vue-router'
import { messageTip } from '@/util/utils'

const globalStore = useGlobalStore()
const route = useRoute()

const path = route.path
const defaultActiveTab = ref(path.split('/')[2] || 'excel')
const isCollapse = ref(false)

const updateMenuStatus = () => {
  isCollapse.value = window.innerWidth < 769
}

watchEffect(() => { updateMenuStatus() })
window.addEventListener('resize', updateMenuStatus)

onMounted(() => {
  doGet<CommonResponse>(GLOBAL_INFO_URL, {})
    .then((response) => {
      if (response.data) {
        globalStore.setGlobal(response.data.global)
        if (!globalStore.global.isLogin) {
          messageTip('请先登录', 'warning')
          setTimeout(() => { window.location.href = '/' }, 1000)
        }
      }
    })
    .catch((error) => { console.error(error) })
})
</script>

<style scoped>
.tools-page {
  background: var(--pai-bg-light-1, #f4f6fa);
  min-height: calc(100vh - var(--header-height, 60px));
  padding-top: calc(var(--header-height, 60px));
}

.tools-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 1.25rem;
  display: flex;
  gap: 1.5rem;
  align-items: flex-start;
}

.tools-menu {
  width: 200px;
  flex-shrink: 0;
  border-radius: 12px;
  background: var(--pai-bg-white-fff, #ffffff);
  box-shadow: 0 1px 3px rgba(26, 29, 39, 0.04);
  border: none;
}

.tools-menu:not(.el-menu--collapse) {
  width: 200px;
}

.tools-content {
  flex: 1;
  min-width: 0;
}

@media (max-width: 768px) {
  .tools-container {
    padding: 0.75rem;
    flex-direction: column;
  }
}
</style>
