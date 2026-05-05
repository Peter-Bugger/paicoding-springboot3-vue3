<template>
  <div ref="gridRef" class="article-grid">
    <div
      v-for="(article, index) in articles"
      :key="article.articleId"
      class="article-grid-item"
      :class="{ 'article-grid-item--visible': visible }"
      :style="{ animationDelay: visible ? (index * 0.05) + 's' : '0s' }"
    >
      <ArticleCard :article="article"></ArticleCard>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onBeforeUnmount } from 'vue'
import ArticleCard from '@/components/article/ArticleCard.vue'
import type { ArticleType } from '@/http/ResponseTypes/ArticleType/ArticleType'

defineProps<{
  articles: ArticleType[]
}>()

const gridRef = ref<HTMLElement | null>(null)
const visible = ref(false)
let observer: IntersectionObserver | null = null

onMounted(() => {
  if (!gridRef.value) return
  observer = new IntersectionObserver(
    ([entry]) => {
      if (entry.isIntersecting) {
        visible.value = true
        observer?.unobserve(entry.target)
      }
    },
    { threshold: 0.05 }
  )
  observer.observe(gridRef.value)
})

onBeforeUnmount(() => {
  observer?.disconnect()
})
</script>

<style scoped>
.article-grid {
  column-count: 1;
  column-gap: 1.5rem;
}

@media (min-width: 768px) {
  .article-grid {
    column-count: 2;
  }
}

.article-grid-item {
  opacity: 0;
  transform: translateY(12px);
  transition: none;
  break-inside: avoid;
  margin-bottom: 1.5rem;
}

.article-grid-item--visible {
  animation: cardFadeIn 0.5s ease-out forwards;
}

@keyframes cardFadeIn {
  from { opacity: 0; transform: translateY(12px); }
  to { opacity: 1; transform: translateY(0); }
}
</style>
