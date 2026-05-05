import './assets/main.css'
import '@fontsource/jetbrains-mono'
import '@fontsource/noto-sans-sc'
import '@fontsource/zcool-qingke-huangyou'

import { createApp, defineComponent, h } from 'vue'
import { createPinia } from 'pinia'
import ElementPlus  from 'element-plus'
import zhCn from 'element-plus/es/locale/lang/zh-cn'
// 导入css样式
import 'element-plus/dist/index.css'
import 'element-plus/theme-chalk/dark/css-vars.css'
import "./index.css"

import App from './App.vue'
import router from './router'
import { useThemeStore } from '@/stores/theme'


// markdown插件

// 创建一个 H2 组件映射到原生 h2 标签
const H2 = defineComponent({
  setup(_, { slots }) {
    return () => h('h2', slots.default ? slots.default() : [])
  }
})
const app = createApp(App)

// eslint-disable-next-line vue/no-reserved-component-names,vue/multi-word-component-names
app.component('H2', H2)

app.use(createPinia())

// 初始化主题 Store（应用 localStorage 中的主题设置）
useThemeStore()
app.use(router)

// ....
app.use(ElementPlus, {
  locale: zhCn
}).mount('#app')
