<template>
  <footer class="app-footer">
    <div class="footer-inner">
      <div class="footer-info">
        <a class="footer-brand" href="/">
          <span class="footer-logo-text">编程汇</span>
        </a>
        <p class="footer-desc">一个基于 Spring Boot 3 + Vue 3 的现代化社区平台</p>
      </div>
      <div class="footer-meta">
        <div class="footer-stats">
          <span class="footer-stat">总PV {{ global.siteStatisticInfo.pv }}</span>
          <span class="footer-stat-divider">·</span>
          <span class="footer-stat">总UV {{ global.siteStatisticInfo.uv }}</span>
          <span class="footer-stat-divider">·</span>
          <span class="footer-stat">{{ global.onlineCnt }} 人在线</span>
        </div>
        <div class="footer-copyright">
          <a class="text-reset" href="https://beian.miit.gov.cn" target="_blank">
            {{ global.siteInfo.websiteRecord }}
          </a>
          <span class="footer-stat-divider">·</span>
          <a class="text-reset" href="http://xuyifei.site" target="_blank">
            小灰灰 &copy; 2024-{{ format(new Date(), 'yyyy') }} 编程汇
          </a>
        </div>
      </div>
    </div>
  </footer>
</template>

<script setup lang="ts">
import { defineOptions } from 'vue'
import { format } from 'date-fns'
import { useGlobalStore } from '@/stores/global'

defineOptions({ name: 'AppFooter' })

const globalStore = useGlobalStore()
const global = globalStore.global
</script>

<style scoped>
.app-footer {
  width: 100%;
  background: var(--pai-bg-dark-1, #1a1d27);
  color: var(--pai-color-999-gray, #8c8f9c);
  padding: 2rem 0;
  margin-top: 3rem;
}

.footer-inner {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 1.25rem;
  display: flex;
  flex-direction: column;
  gap: 1.25rem;
}

.footer-info {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 0.5rem;
}

.footer-brand {
  text-decoration: none;
}

.footer-logo-text {
  font-family: 'ZCOOL QingKe HuangYou', cursive;
  font-size: 1.5rem;
  color: var(--pai-color-fff-normal, #e1e3eb);
  letter-spacing: 0.05em;
}

.footer-desc {
  font-size: 0.8rem;
  color: var(--pai-color-999-gray, #8c8f9c);
  margin: 0;
}

.footer-meta {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 0.5rem;
  font-size: 0.75rem;
}

.footer-stats {
  display: flex;
  align-items: center;
  gap: 0.35rem;
  flex-wrap: wrap;
  justify-content: center;
}

.footer-stat {
  color: var(--pai-color-3-gray, #6b7084);
}

.footer-stat-divider {
  color: var(--pai-color-5-gray, #3a3d4a);
  opacity: 0.4;
}

.footer-copyright {
  display: flex;
  align-items: center;
  gap: 0.35rem;
  flex-wrap: wrap;
  justify-content: center;
}

.footer-copyright a {
  color: var(--pai-color-3-gray, #6b7084);
  text-decoration: none;
  transition: color 0.2s ease;
}

.footer-copyright a:hover {
  color: var(--pai-brand-1-normal);
}

@media (max-width: 768px) {
  .app-footer {
    padding: 1.5rem 0;
  }
  .footer-inner {
    padding: 0 1rem;
    gap: 1rem;
  }
}
</style>
