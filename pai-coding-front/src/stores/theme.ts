import { ref, watch } from 'vue'
import { defineStore } from 'pinia'

export type ThemeMode = 'light' | 'dark' | 'auto'

export const useThemeStore = defineStore('theme', () => {
  const saved = localStorage.getItem('pai-theme-mode') as ThemeMode | null
  const mode = ref<ThemeMode>(saved || 'light')

  const isDark = ref(false)

  const applyTheme = (newMode: ThemeMode) => {
    const html = document.documentElement
    if (newMode === 'dark') {
      html.classList.add('dark')
      isDark.value = true
    } else if (newMode === 'light') {
      html.classList.remove('dark')
      isDark.value = false
    } else {
      const prefersDark = window.matchMedia('(prefers-color-scheme: dark)').matches
      if (prefersDark) {
        html.classList.add('dark')
      } else {
        html.classList.remove('dark')
      }
      isDark.value = prefersDark
    }
  }

  const toggle = () => {
    mode.value = mode.value === 'light' ? 'dark' : 'light'
  }

  const setMode = (newMode: ThemeMode) => {
    mode.value = newMode
  }

  // Initialize
  applyTheme(mode.value)

  // Watch for system preference changes in auto mode
  const mq = window.matchMedia('(prefers-color-scheme: dark)')
  mq.addEventListener('change', () => {
    if (mode.value === 'auto') {
      applyTheme('auto')
    }
  })

  watch(mode, (newMode) => {
    localStorage.setItem('pai-theme-mode', newMode)
    applyTheme(newMode)
  })

  return { mode, isDark, toggle, setMode }
})
