<template>
  <header
    :data-islogin="global.isLogin? 'true' : 'false'"
    class="app-header"
    :class="{ 'app-header--scrolled': scrolled }"
  >
    <div class="header-inner">
      <div class="header-left">
        <a class="header-logo" href="/">
          <img class="logo-img" src="/src/assets/static/img/logo.png" alt="编程汇" />
        </a>

        <!-- 移动端菜单 -->
        <el-dropdown :hide-on-click="false" class="mobile-menu-trigger">
          <a class="nav-link-mobile">首页</a>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item><a href="/">首页</a></el-dropdown-item>
              <el-dropdown-item><a href="/column">教程</a></el-dropdown-item>
              <el-dropdown-item><a href="/chat">LLM</a></el-dropdown-item>
              <el-dropdown-item><a href="/about">关于作者</a></el-dropdown-item>
              <el-dropdown-item><a href="/plan">更新计划</a></el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>

      <!-- 桌面端导航 -->
      <nav class="header-nav">
        <a class="header-nav-link" :class="{ active: activeTab === '/' }" href="/">首页</a>
        <a class="header-nav-link" :class="{ active: activeTab === '/column' }" href="/column">教程</a>
        <a class="header-nav-link" :class="{ active: activeTab === '/about' }" href="/about">关于作者</a>
        <a class="header-nav-link" :class="{ active: activeTab === '/chat' }" href="/chat">LLM</a>
        <a class="header-nav-link hidden-mobile" :class="{ active: activeTab === '/plan' }" href="/plan">更新计划</a>
      </nav>

      <div class="header-right">
        <!-- 主题切换 -->
        <ThemeToggle />

        <!-- 写文章/返回主页 -->
        <button
          v-if="!route.path.includes('/article/edit') && route.path !== '/article/edit/' && global.isLogin"
          class="btn-write"
          @click="writeArticle"
        >
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M12 5v14"/><path d="M5 12h14"/></svg>
          <span>写文章</span>
        </button>
        <button
          v-else-if="route.path.includes('/article/edit') || route.path === '/article/edit/'"
          class="btn-write btn-write--back"
          @click="router.push('/')"
        >
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M19 12H5"/><path d="m12 19-7-7 7-7"/></svg>
          <span>返回主页</span>
        </button>

        <!-- 未登录 -->
        <template v-if="!global.isLogin">
          <button class="btn-login" @click="loginButton">登录</button>
        </template>

        <!-- 已登录 -->
        <template v-if="global.isLogin">
          <div class="header-actions">
            <a class="header-notice" href="/notice/">
              <span v-if="global.msgNum != null && global.msgNum > 0" class="notice-badge">{{ global.msgNum }}</span>
              <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round">
                <path d="M18 8A6 6 0 0 0 6 8c0 7-3 9-3 9h18s-3-2-3-9"/>
                <path d="M13.73 21a2 2 0 0 1-3.46 0"/>
              </svg>
            </a>

            <el-dropdown :hide-on-click="false" trigger="click">
              <div class="header-avatar">
                <img
                  class="avatar-img"
                  :src="global.user.photo || 'https://static.developers.pub/static/img/logo.b2ff606.jpeg'"
                  alt="avatar"
                  loading="lazy"
                />
                <el-icon size="12"><ArrowDownBold /></el-icon>
              </div>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item><span @click="personalPage">个人主页</span></el-dropdown-item>
                  <el-dropdown-item><span @click="toolsPage">工具</span></el-dropdown-item>
                  <el-dropdown-item divided><span @click="logout">登出</span></el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>
        </template>
      </div>
    </div>
  </header>
</template>

<script setup lang="ts">
import { inject, onMounted, ref, onBeforeUnmount } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { doGet } from '@/http/BackendRequests'
import type { CommonResponse } from '@/http/ResponseTypes/CommonResponseType'
import { useGlobalStore } from '@/stores/global'
import { messageTip, refreshPage, sleep } from '@/util/utils'
import { MESSAGE_TYPE } from '@/constants/MessageTipEnumConstant'
import { ArrowDownBold } from '@element-plus/icons-vue'
import { LOGOUT_URL } from '@/http/URL'
import { defineOptions } from 'vue'
import ThemeToggle from '@/components/theme/ThemeToggle.vue'

defineOptions({ name: 'AppHeaderBar' })

const router = useRouter()
const route = useRoute()
const globalStore = useGlobalStore()
const global = globalStore.global

const activeTab = ref('/')
const scrolled = ref(false)

let scrollHandler: (() => void) | null = null

onMounted(() => {
  activeTab.value = router.currentRoute.value.path

  scrollHandler = () => {
    scrolled.value = window.scrollY > 10
  }
  window.addEventListener('scroll', scrollHandler, { passive: true })
})

onBeforeUnmount(() => {
  if (scrollHandler) window.removeEventListener('scroll', scrollHandler)
})

const writeArticle = () => {
  if (route.fullPath.includes('/article/edit')) {
    window.location.reload()
  } else {
    router.push('/article/edit')
  }
}

const showLoginDialog = inject<() => void>('loginDialogClicked')

const loginButton = () => {
  if (showLoginDialog) showLoginDialog()
  else console.error('请先登录')
}

const personalPage = () => {
  if (route.fullPath.includes('/user/' + global.user.userId)) {
    messageTip('已经在个人主页了', MESSAGE_TYPE.INFO)
    return
  }
  router.push(global.user.userId ? '/user/' + global.user.userId : '/login')
}

const toolsPage = () => {
  router.push('/tools/')
}

const logout = () => {
  doGet<CommonResponse>(LOGOUT_URL, {})
    .then((response) => {
      if (response.data.status.code === 0) {
        messageTip('退出登录成功', MESSAGE_TYPE.SUCCESS)
        sleep(1).then(() => refreshPage())
      }
    })
    .catch((error) => console.error(error))
}
</script>

<style scoped>
/* ── Header 容器 ── */
.app-header {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 1000;
  height: var(--header-height, 60px);
  background: rgba(255, 255, 255, 0.72);
  backdrop-filter: blur(12px);
  -webkit-backdrop-filter: blur(12px);
  border-bottom: 1px solid transparent;
  transition: box-shadow 0.3s ease, border-color 0.3s ease;
}

.app-header--scrolled {
  box-shadow: 0 1px 8px rgba(26, 29, 39, 0.06);
  border-bottom-color: var(--pai-border-color-1);
}

.header-inner {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 1.25rem;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 1.5rem;
}

/* ── Logo ── */
.header-left {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  flex-shrink: 0;
}

.header-logo {
  display: flex;
  align-items: center;
  text-decoration: none;
}

.logo-img {
  height: 34px;
  width: auto;
  display: block;
}

/* ── 桌面端导航 ── */
.header-nav {
  display: flex;
  align-items: center;
  gap: 0.25rem;
}

.header-nav-link {
  position: relative;
  padding: 0.4rem 0.85rem;
  font-size: 0.875rem;
  font-weight: 500;
  color: var(--pai-color-4-gray, #484d5e);
  text-decoration: none;
  border-radius: 8px;
  transition: color 0.25s ease, background 0.25s ease;
}

.header-nav-link:hover {
  color: var(--pai-brand-1-normal);
  background: var(--pai-brand-7-light);
}

.header-nav-link.active {
  color: var(--pai-brand-1-normal);
  background: var(--pai-brand-7-light);
}

.header-nav-link.active::after {
  content: '';
  position: absolute;
  bottom: 2px;
  left: 50%;
  transform: translateX(-50%);
  width: 18px;
  height: 2.5px;
  border-radius: 2px;
  background: var(--pai-brand-1-normal);
}

/* ── 右侧区域 ── */
.header-right {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  flex-shrink: 0;
}

/* ── 写文章按钮 ── */
.btn-write {
  display: inline-flex;
  align-items: center;
  gap: 0.35rem;
  padding: 0.45rem 1rem;
  font-size: 0.8rem;
  font-weight: 600;
  color: #fff;
  background: var(--pai-brand-1-normal);
  border: none;
  border-radius: 8px;
  cursor: pointer;
  transition: background 0.25s ease, transform 0.2s ease, box-shadow 0.25s ease;
  white-space: nowrap;
}

.btn-write:hover {
  background: var(--pai-brand-2-hover);
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(45, 124, 246, 0.25);
}

.btn-write:active {
  transform: translateY(0);
}

.btn-write--back {
  background: var(--pai-color-6-gray, #f0f2f6);
  color: var(--pai-color-4-gray, #484d5e);
}

.btn-write--back:hover {
  background: var(--pai-color-7-gray, #e4e7ef);
  box-shadow: none;
}

/* ── 登录按钮 ── */
.btn-login {
  padding: 0.45rem 1.25rem;
  font-size: 0.8rem;
  font-weight: 600;
  color: var(--pai-brand-1-normal);
  background: transparent;
  border: 1.5px solid var(--pai-brand-1-normal);
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.25s ease;
}

.btn-login:hover {
  color: #fff;
  background: var(--pai-brand-1-normal);
}

/* ── 已登录操作区 ── */
.header-actions {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

/* 通知铃铛 */
.header-notice {
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
  width: 36px;
  height: 36px;
  color: var(--pai-color-4-gray, #484d5e);
  border-radius: 8px;
  transition: background 0.25s ease, color 0.25s ease;
  text-decoration: none;
}

.header-notice:hover {
  background: var(--pai-brand-7-light);
  color: var(--pai-brand-1-normal);
}

.notice-badge {
  position: absolute;
  top: 2px;
  right: 2px;
  min-width: 18px;
  height: 18px;
  padding: 0 5px;
  font-size: 0.65rem;
  font-weight: 700;
  line-height: 18px;
  text-align: center;
  color: #fff;
  background: var(--pai-brand-6-mq);
  border-radius: 9px;
  pointer-events: none;
}

/* 用户头像 */
.header-avatar {
  display: flex;
  align-items: center;
  gap: 0.2rem;
  cursor: pointer;
  padding: 2px;
  border-radius: 50%;
  transition: box-shadow 0.25s ease;
}

.header-avatar:hover {
  box-shadow: 0 0 0 2px var(--pai-brand-7-light);
}

.avatar-img {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  object-fit: cover;
  display: block;
}

/* ── 移动端菜单 ── */
.mobile-menu-trigger {
  display: none;
}

.nav-link-mobile {
  color: var(--pai-color-4-gray);
  font-size: 0.85rem;
  font-weight: 500;
  text-decoration: none;
}

.hidden-mobile {
  display: inline;
}

/* ── 响应式 ── */
@media (max-width: 768px) {
  .header-inner {
    padding: 0 1rem;
  }

  .header-nav {
    display: none;
  }

  .mobile-menu-trigger {
    display: inline-flex;
  }

  .hidden-mobile {
    display: none;
  }

  .btn-write span {
    display: none;
  }

  .btn-write {
    padding: 0.45rem 0.65rem;
  }
}

/* ── 暗色模式 ── */
:root.dark .app-header {
  background: rgba(22, 24, 34, 0.82);
}

:root.dark .app-header--scrolled {
  border-bottom-color: var(--pai-border-color-1, #2a2d3a);
}

:root.dark .btn-write--back {
  background: var(--pai-color-5-gray, #3a3d4a);
  color: var(--pai-color-4-gray, #8c8f9c);
}

:root.dark .btn-write--back:hover {
  background: var(--pai-color-7-gray, #2a2d3a);
}
</style>
