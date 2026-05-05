<template>
  <HeaderBar></HeaderBar>
  <div class="tag-articles-page">
    <div class="tag-articles-container">
      <div class="tag-articles-empty">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" class="tag-articles-empty-icon">
          <path d="M9.143 17.082a24.248 24.248 0 0 0 3.844.148m-3.844-.148a23.856 23.856 0 0 1-5.455-1.31 8.964 8.964 0 0 0 2.3-5.542m3.155 6.852a3 3 0 0 0 5.667 1.97m1.965-2.277L21 21m-4.225-4.225a23.81 23.81 0 0 0 3.536-1.003A8.967 8.967 0 0 1 18 9.75V9A6 6 0 0 0 6.53 6.53m10.245 10.245L6.53 6.53M3 3l3.53 3.53" />
        </svg>
        <p class="tag-articles-empty-text">标签文章列表</p>
      </div>
    </div>
  </div>
  <LoginDialog :clicked="loginDialogClicked"></LoginDialog>
</template>

<script setup lang="ts">
import HeaderBar from '@/components/layout/HeaderBar.vue'
import LoginDialog from '@/components/dialog/LoginDialog.vue'
import { provide, ref } from 'vue'

const changeClicked = () => {
  loginDialogClicked.value = !loginDialogClicked.value
}

provide('loginDialogClicked', changeClicked)
const loginDialogClicked = ref(false)
</script>

<style scoped>
.tag-articles-page {
  background: var(--pai-bg-light-1, #f4f6fa);
  min-height: calc(100vh - var(--header-height, 60px));
  padding-top: calc(var(--header-height, 60px));
}

.tag-articles-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 2rem 1.25rem;
}

.tag-articles-empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 4rem 2rem;
  background: var(--pai-bg-white-fff, #ffffff);
  border-radius: 16px;
  box-shadow: 0 1px 3px rgba(26, 29, 39, 0.04);
}

.tag-articles-empty-icon {
  width: 48px;
  height: 48px;
  color: var(--pai-color-5-gray, #d0d3dd);
  margin-bottom: 1rem;
}

.tag-articles-empty-text {
  font-size: 1rem;
  color: var(--pai-color-999-gray, #8c8f9c);
  margin: 0;
}
</style>
