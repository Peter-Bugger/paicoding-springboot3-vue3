<template>
  <article class="article-card" :class="{ 'article-card--column': article.articleType === ArticleTypeNumberEnum.COLUMN }">
    <a
      class="article-card-link"
      rel="noreferrer"
      @click="clickArticle"
    ></a>
    <div class="article-card-inner">
      <!-- 封面图 -->
      <div class="article-card-cover" v-if="article.cover">
        <span
          class="article-card-cover-img"
          :style="{'background-image': 'url(' + article.cover + ')'}"
        ></span>
        <div class="article-card-cover-overlay"></div>
      </div>

      <!-- 内容区 -->
      <div class="article-card-body">
        <!-- 标签 + 日期 -->
        <div class="article-card-meta-row">
          <div class="article-card-tags" v-if="article.tags && article.tags.length > 0">
            <span class="article-card-tag" v-for="tag in article.tags.slice(0, 2)" :key="tag.tagId">
              {{tag.tag}}
            </span>
            <span v-if="article.tags.length > 3" class="article-card-tag-more">+{{article.tags.length - 3}}</span>
          </div>
        </div>

        <!-- 标题 -->
        <h3 class="article-card-title">
          <span v-if="article.toppingStat === 1 && router.currentRoute.value.path === '/'" class="article-card-pin">置顶</span>
          {{article.title}}
        </h3>

        <!-- 摘要 -->
        <p class="article-card-summary">{{article.summary}}</p>

        <!-- 底部元信息 -->
        <div class="article-card-footer">
          <div class="article-card-author">
            <span class="article-card-avatar" :style="{'background-image': 'url(' + article.authorAvatar + ')'}"></span>
            <a class="article-card-author-name" :href="'/user/' + article.author" @click.stop>{{article.authorName}}</a>
          </div>
          <div class="article-card-stats">
            <span class="article-card-stat">
              <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"/><circle cx="12" cy="12" r="3"/></svg>
              <span>{{article.count.readCount}}</span>
            </span>
            <span class="article-card-stat">
              <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"/></svg>
              <span>{{article.count.commentCount}}</span>
            </span>
            <span class="article-card-stat article-card-stat--like">
              <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 9V5a3 3 0 0 0-3-3l-4 9v11h11.28a2 2 0 0 0 2-1.7l1.38-9a2 2 0 0 0-2-2.3H14zM7 22H4a2 2 0 0 1-2-2v-7a2 2 0 0 1 2-2h3"/></svg>
              <span>{{article.count.praiseCount}}</span>
            </span>
          </div>
        </div>
      </div>
    </div>
  </article>
</template>

<script setup lang="ts">

import { format } from 'date-fns';
import { useRouter } from 'vue-router'
import type { ArticleType } from '@/http/ResponseTypes/ArticleType/ArticleType'
import {ArticleTypeNumberEnum} from "@/constants/ArticleTypeEnumConstants";
import {doGet} from "@/http/BackendRequests";
import type {CommonResponse} from "@/http/ResponseTypes/CommonResponseType";
import {ARTICLE_COLUMN_RELATION_URL} from "@/http/URL";
import {messageTip} from "@/util/utils";

const router = useRouter()

const props = defineProps<{
  article: ArticleType
}>()

const clickArticle = () =>{
  console.log(props.article.articleType)
  if(props.article.articleType === ArticleTypeNumberEnum.COLUMN){
    doGet<CommonResponse>(`${ARTICLE_COLUMN_RELATION_URL}/${props.article.articleId}`, {}).then(res=>{
      router.push(`/column/${res.data.result.columnId}/${res.data.result.section}`)
    }).catch(err=>{
      messageTip("获取专栏信息失败", "error")
      console.log(err)
    })
  }else{
    router.push('/article/detail/'+props.article.articleId)
  }
}

</script>

<style scoped>
.article-card {
  position: relative;
  background: #fff;
  border-radius: 16px;
  overflow: hidden;
  transition: all 0.4s cubic-bezier(0.2, 0, 0, 1);
  box-shadow: 0 1px 3px rgba(0,0,0,0.04), 0 1px 2px rgba(0,0,0,0.03);
  animation: cardFadeIn 0.5s ease-out both;
}

.article-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 12px 32px rgba(0,0,0,0.07), 0 3px 8px rgba(0,0,0,0.03);
}

.article-card-link {
  position: absolute;
  inset: 0;
  z-index: 1;
  cursor: pointer;
}

.article-card-inner {
  display: flex;
  flex-direction: column;
}

/* ── Cover ── */
.article-card-cover {
  position: relative;
  width: 100%;
  height: 190px;
  overflow: hidden;
}

.article-card-cover-img {
  display: block;
  width: 100%;
  height: 100%;
  background-size: cover;
  background-position: center;
  background-repeat: no-repeat;
  transition: transform 0.6s cubic-bezier(0.2, 0, 0, 1);
}

.article-card-cover-overlay {
  position: absolute;
  inset: 0;
  background: linear-gradient(180deg, transparent 50%, rgba(0,0,0,0.06) 100%);
  pointer-events: none;
}

.article-card:hover .article-card-cover-img {
  transform: scale(1.05);
}

/* ── Body ── */
.article-card-body {
  padding: 1.25rem 1.25rem 1rem;
  display: flex;
  flex-direction: column;
  flex: 1;
}

/* ── Meta Row ── */
.article-card-meta-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 0.5rem;
}

.article-card-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 0.35rem;
}

.article-card-tag {
  display: inline-block;
  padding: 0.1rem 0.55rem;
  font-size: 0.7rem;
  font-weight: 500;
  color: var(--pai-brand-1-normal);
  background: var(--pai-brand-7-light);
  border-radius: 4px;
  letter-spacing: 0.02em;
  line-height: 1.5;
}

.article-card-tag-more {
  font-size: 0.7rem;
  color: #bbb;
  line-height: 1.8;
}

/* ── Title ── */
.article-card-title {
  font-family: "Noto Serif SC", "Source Han Serif SC", serif;
  font-size: 1.05rem;
  font-weight: 700;
  line-height: 1.5;
  color: #1a1a1a;
  margin: 0 0 0.45rem;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  transition: color 0.3s ease;
}

.article-card:hover .article-card-title {
  color: var(--pai-brand-1-normal);
}

.article-card-pin {
  display: inline-block;
  font-family: -apple-system, sans-serif;
  font-size: 0.65rem;
  font-weight: 700;
  color: #fff;
  background: var(--pai-brand-6-mq);
  padding: 1px 6px;
  border-radius: 4px;
  margin-right: 6px;
  vertical-align: middle;
}

/* ── Summary ── */
.article-card-summary {
  font-size: 0.85rem;
  color: #88817a;
  line-height: 1.65;
  margin: 0 0 0.85rem;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

/* ── Footer ── */
.article-card-footer {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  padding-top: 0.75rem;
  border-top: 1px solid #f3efe9;
  flex-wrap: wrap;
}

.article-card-author {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.article-card-avatar {
  display: block;
  width: 26px;
  height: 26px;
  border-radius: 50%;
  background-size: cover;
  background-position: center;
  background-color: #f0f0f0;
  flex-shrink: 0;
  box-shadow: 0 0 0 1.5px rgba(255,255,255,0.8);
}

.article-card-author-name {
  font-size: 0.8rem;
  font-weight: 500;
  color: #666;
  text-decoration: none;
  position: relative;
  z-index: 2;
  transition: color 0.25s ease;
}

.article-card-author-name:hover {
  color: var(--pai-brand-1-normal);
}

.article-card-stats {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  margin-left: auto;
}

.article-card-stat {
  display: inline-flex;
  align-items: center;
  gap: 0.3rem;
  font-size: 0.75rem;
  color: #c4bdb5;
  transition: color 0.25s ease;
}

.article-card-stat svg {
  opacity: 0.5;
  transition: opacity 0.25s ease;
}

.article-card:hover .article-card-stat {
  color: #b0a89e;
}

/* ── Responsive ── */
@media (max-width: 768px) {
  .article-card-cover {
    height: 160px;
  }
  .article-card-body {
    padding: 1rem 1rem 0.85rem;
  }
  .article-card-title {
    font-size: 1rem;
  }
  .article-card-footer {
    gap: 0.5rem;
  }
}
</style>
