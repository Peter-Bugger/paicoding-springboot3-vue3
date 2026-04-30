<template>
  <section class="hero-section" :style="{ '--hero-accent': backgroundColor }">
    <div class="hero-grid">
      <!-- 左侧：首篇推荐文章（大封面） -->
      <a
        v-if="topArticles.length > 0"
        :href="'/article/detail/' + topArticles[0].articleId"
        class="hero-featured"
      >
        <div class="hero-featured-img-wrap">
          <img
            :src="topArticles[0].cover"
            :id="'cover0'"
            @load="setColor(0)"
            class="hero-featured-img"
          />
          <div class="hero-featured-gradient"></div>
          <div class="hero-featured-badge">精选</div>
        </div>
        <div class="hero-featured-body">
          <div class="hero-featured-tags">
            <span v-for="(tag, index) in topArticles[0].tags.slice(0, 2)" :key="index" class="hero-featured-tag">
              {{tag.tag}}
            </span>
          </div>
          <h2 class="hero-featured-title">{{topArticles[0].title}}</h2>
          <p class="hero-featured-desc">{{topArticles[0].summary}}</p>
          <div class="hero-featured-meta">
            <span class="hero-featured-author">
              <span class="hero-featured-author-dot"></span>
              {{topArticles[0].authorName}}
            </span>
            <span class="hero-featured-divider"></span>
            <span class="hero-featured-date">{{format(new Date(Number(topArticles[0].createTime)), "yyyy/MM/dd")}}</span>
          </div>
        </div>
      </a>

      <!-- 右侧：推荐文章列表 -->
      <div class="hero-side" v-if="topArticles.length > 1">
        <div class="hero-side-header">
          <span class="hero-side-heading-bar"></span>
          <h3 class="hero-side-heading">推荐阅读</h3>
        </div>
        <div class="hero-side-items">
          <a
            v-for="(article, index) in topArticles.slice(1, 4)"
            :key="article.articleId"
            :href="'/article/detail/' + article.articleId"
            class="hero-side-item"
            :style="{ animationDelay: (index * 0.08) + 's' }"
          >
            <div class="hero-side-item-num">{{String(index + 1).padStart(2, '0')}}</div>
            <div class="hero-side-item-content">
              <h4 class="hero-side-item-title">{{article.title}}</h4>
              <div class="hero-side-item-meta">
                <span>{{article.authorName}}</span>
                <span class="hero-side-item-meta-dot">·</span>
                <span>{{format(new Date(Number(article.createTime)), "MM/dd")}}</span>
              </div>
            </div>
          </a>
        </div>
      </div>
    </div>
  </section>
</template>

<script setup lang="ts">
import { format } from 'date-fns'
import { ref } from 'vue'
import Vibrant from 'node-vibrant/lib/bundle'
import type { ArticleType } from '@/http/ResponseTypes/ArticleType/ArticleType'

defineProps<{
  topArticles: ArticleType[]
}>()

const backgroundColor = ref<string>('#f0e8e0')

function setColor(index: number) {
  if(index !== 0) return
  const img = document.getElementById(`cover${index}`) as HTMLImageElement
  if(img){
    if (img.complete) {
      applyColor(img)
    } else {
      img.addEventListener("load", function () {
        applyColor(img)
      })
    }
  }
}

function applyColor(img: HTMLImageElement){
  Vibrant.from(img)
    .getPalette()
    .then((palette: any) => {
      let rgb = palette.Vibrant?.getHex()
      backgroundColor.value = rgb || '#f0e8e0'
    })
}
</script>

<style scoped>
.hero-section {
  margin-bottom: 2.5rem;
  border-radius: 20px;
  overflow: hidden;
  animation: heroReveal 0.7s ease-out both;
}

.hero-grid {
  display: grid;
  grid-template-columns: 1.6fr 1fr;
  min-height: 400px;
  border-radius: 20px;
  overflow: hidden;
  background: #fff;
  box-shadow: 0 2px 16px rgba(0,0,0,0.04);
}

/* ═══════════ Featured (Left) ═══════════ */
.hero-featured {
  position: relative;
  display: flex;
  flex-direction: column;
  text-decoration: none;
  overflow: hidden;
  background: #faf6f1;
}

.hero-featured-img-wrap {
  position: relative;
  width: 100%;
  height: 240px;
  overflow: hidden;
}

.hero-featured-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.7s cubic-bezier(0.2, 0, 0, 1);
}

.hero-featured:hover .hero-featured-img {
  transform: scale(1.04);
}

.hero-featured-gradient {
  position: absolute;
  inset: 0;
  background: linear-gradient(180deg, transparent 30%, rgba(0,0,0,0.5) 100%);
}

.hero-featured-badge {
  position: absolute;
  top: 1rem;
  left: 1rem;
  padding: 0.2rem 0.7rem;
  font-size: 0.7rem;
  font-weight: 600;
  color: #fff;
  background: var(--pai-brand-1-normal);
  border-radius: 6px;
  letter-spacing: 0.05em;
  backdrop-filter: blur(4px);
  box-shadow: 0 2px 8px rgba(255, 105, 0, 0.3);
}

.hero-featured-body {
  padding: 1.5rem 1.75rem 1.75rem;
  flex: 1;
  display: flex;
  flex-direction: column;
}

.hero-featured-tags {
  display: flex;
  gap: 0.4rem;
  margin-bottom: 0.7rem;
}

.hero-featured-tag {
  display: inline-block;
  padding: 0.15rem 0.65rem;
  font-size: 0.7rem;
  font-weight: 600;
  color: var(--pai-brand-1-normal);
  background: var(--pai-brand-7-light);
  border-radius: 4px;
  letter-spacing: 0.02em;
  line-height: 1.6;
}

.hero-featured-title {
  font-family: "Noto Serif SC", "Source Han Serif SC", serif;
  font-size: 1.45rem;
  font-weight: 700;
  line-height: 1.4;
  color: #1a1a1a;
  margin: 0 0 0.5rem;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  transition: color 0.3s ease;
}

.hero-featured:hover .hero-featured-title {
  color: var(--pai-brand-1-normal);
}

.hero-featured-desc {
  font-size: 0.85rem;
  color: #88817a;
  line-height: 1.65;
  margin: 0 0 1rem;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  flex: 1;
}

.hero-featured-meta {
  display: flex;
  align-items: center;
  gap: 0.6rem;
  font-size: 0.8rem;
  color: #99938c;
}

.hero-featured-author {
  display: flex;
  align-items: center;
  gap: 0.4rem;
  font-weight: 500;
  color: #666;
}

.hero-featured-author-dot {
  width: 5px;
  height: 5px;
  border-radius: 50%;
  background: var(--pai-brand-1-normal);
}

.hero-featured-divider {
  width: 1px;
  height: 12px;
  background: #ddd6ce;
}

/* ═══════════ Side (Right) ═══════════ */
.hero-side {
  display: flex;
  flex-direction: column;
  padding: 1.75rem 1.5rem;
  background: #fff;
  border-left: 1px solid #f0ebe5;
}

.hero-side-header {
  display: flex;
  align-items: center;
  gap: 0.6rem;
  margin-bottom: 0.5rem;
  padding-bottom: 1rem;
  border-bottom: 1px solid #f5f0ea;
}

.hero-side-heading-bar {
  width: 3px;
  height: 16px;
  border-radius: 2px;
  background: var(--pai-brand-1-normal);
}

.hero-side-heading {
  font-size: 0.85rem;
  font-weight: 700;
  color: #2c2c2c;
  margin: 0;
  letter-spacing: 0.03em;
}

.hero-side-items {
  display: flex;
  flex-direction: column;
  flex: 1;
}

.hero-side-item {
  display: flex;
  align-items: flex-start;
  gap: 0.9rem;
  padding: 0.9rem 0;
  text-decoration: none;
  border-bottom: 1px solid #f8f3ed;
  opacity: 0;
  animation: slideUp 0.4s ease-out forwards;
  transition: all 0.3s ease;
}

.hero-side-item:last-child {
  border-bottom: none;
}

.hero-side-item:hover {
  padding-left: 0.5rem;
  padding-right: 0.5rem;
  margin: 0 -0.5rem;
  background: rgba(255, 105, 0, 0.03);
  border-radius: 8px;
  border-bottom-color: transparent;
}

.hero-side-item-num {
  font-family: "Noto Serif SC", serif;
  font-size: 1.3rem;
  font-weight: 700;
  color: #ddd6ce;
  line-height: 1.3;
  min-width: 1.8rem;
  transition: color 0.3s ease;
}

.hero-side-item:hover .hero-side-item-num {
  color: var(--pai-brand-1-normal);
}

.hero-side-item-content {
  flex: 1;
  min-width: 0;
}

.hero-side-item-title {
  font-size: 0.88rem;
  font-weight: 600;
  color: #2c2c2c;
  line-height: 1.45;
  margin: 0 0 0.35rem;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  transition: color 0.25s ease;
}

.hero-side-item:hover .hero-side-item-title {
  color: var(--pai-brand-1-normal);
}

.hero-side-item-meta {
  display: flex;
  align-items: center;
  gap: 0.35rem;
  font-size: 0.75rem;
  color: #bbb3ab;
}

.hero-side-item-meta-dot {
  color: #ddd6ce;
}

/* ═══════════ Animations ═══════════ */
@keyframes heroReveal {
  from { opacity: 0; transform: translateY(12px); }
  to { opacity: 1; transform: translateY(0); }
}

@keyframes slideUp {
  from { opacity: 0; transform: translateY(8px); }
  to { opacity: 1; transform: translateY(0); }
}

/* ═══════════ Responsive ═══════════ */
@media (max-width: 768px) {
  .hero-grid {
    grid-template-columns: 1fr;
  }

  .hero-featured-img-wrap {
    height: 200px;
  }

  .hero-side {
    border-left: none;
    border-top: 1px solid #f0ebe5;
    padding: 1.25rem;
  }

  .hero-featured-body {
    padding: 1.25rem;
  }

  .hero-featured-title {
    font-size: 1.25rem;
  }
}
</style>
