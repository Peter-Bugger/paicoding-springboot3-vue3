<template>
  <HeaderBar />
  <div class="column-page">
    <div class="column-page-wrap">
      <div class="column-page-body">
        <div class="column-page-header">
          <h1 class="column-page-title">专栏</h1>
          <p class="column-page-subtitle">系统化学习，循序渐进</p>
        </div>
        <ColumnList :columns="vo.columnPage.records"></ColumnList>
      </div>
      <div class="column-page-side">
        <ColumnSideBar :sidebar-items="vo.sideBarItems"></ColumnSideBar>
      </div>
    </div>
    <Footer></Footer>
  </div>
  <LoginDialog :clicked="loginDialogClicked"></LoginDialog>
</template>

<script setup lang="ts">
import { useGlobalStore } from '@/stores/global'
const globalStore = useGlobalStore()
import { setTitle } from '@/util/utils'
import { onMounted, provide, reactive, ref } from 'vue'
import { type CommonResponse } from '@/http/ResponseTypes/CommonResponseType'
import { doGet } from '@/http/BackendRequests'
import { COLUMN_LIST_URL } from '@/http/URL'
import { type ColumnListVoTypeResponse, defaultColumnVoResponse } from '@/http/ResponseTypes/ColumnType/ColumnListVoType'
import HeaderBar from '@/components/layout/HeaderBar.vue'
import Footer from '@/components/layout/Footer.vue'
import ColumnList from '@/views/column/ColumnList.vue'
import ColumnSideBar from '@/components/column/ColumnSideBar.vue'
import LoginDialog from '@/components/dialog/LoginDialog.vue'

let vo = reactive<ColumnListVoTypeResponse>({...defaultColumnVoResponse})

onMounted(() => {
  setTitle("专栏首页")
  doGet<CommonResponse>(COLUMN_LIST_URL, {})
    .then((response) => {
      if(response.data){
        globalStore.setGlobal(response.data.global)
        // @ts-ignore
        Object.assign(vo, response.data.result)
      }
    })
    .catch((error) => {
      console.log(error)
    })
})

const changeClicked = () => {
  loginDialogClicked.value = !loginDialogClicked.value
}

provide('loginDialogClicked', changeClicked)
const loginDialogClicked = ref(false)
</script>

<style scoped>
.column-page {
  background: var(--pai-bg-light-1, #f4f6fa);
  min-height: 100vh;
  padding-top: calc(var(--header-height, 60px) + 0.5rem);
}

.column-page-wrap {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 1.25rem;
  display: grid;
  grid-template-columns: 1fr 300px;
  gap: 1.5rem;
  align-items: start;
}

.column-page-body {
  min-width: 0;
}

.column-page-header {
  margin-bottom: 1.5rem;
}

.column-page-title {
  font-family: 'JetBrains Mono', monospace;
  font-size: 1.5rem;
  font-weight: 700;
  color: var(--pai-color-3-black, #1e2029);
  margin: 0 0 0.25rem;
}

.column-page-subtitle {
  font-size: 0.85rem;
  color: var(--pai-color-999-gray, #8c8f9c);
  margin: 0;
}

.column-page-side {
  position: sticky;
  top: calc(var(--header-height, 60px) + 1rem);
}

@media (max-width: 768px) {
  .column-page-wrap {
    grid-template-columns: 1fr;
  }
  .column-page-side {
    display: none;
  }
}
</style>
