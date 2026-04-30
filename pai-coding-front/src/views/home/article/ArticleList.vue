<template>
  <div class="article-grid">
    <div
      v-for="(article, index) in articles"
      :key="article.articleId"
      class="article-grid-item"
      :style="{ animationDelay: (index * 0.05) + 's' }"
    >
      <ArticleCard :article="article"></ArticleCard>
    </div>
  </div>
</template>

<script setup lang="ts">

import ArticleCard from '@/components/article/ArticleCard.vue'
import type { ArticleType } from '@/http/ResponseTypes/ArticleType/ArticleType'

defineProps<{
  articles: ArticleType[]
}>()

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
  animation: cardFadeIn 0.5s ease-out forwards;
  break-inside: avoid;
  margin-bottom: 1.5rem;
}

@keyframes cardFadeIn {
  from { opacity: 0; transform: translateY(12px); }
  to { opacity: 1; transform: translateY(0); }
}
</style>
