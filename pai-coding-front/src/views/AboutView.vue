<template>
  <HeaderBar></HeaderBar>
  <div class="about-page">
    <div class="about-container">
      <div class="about-content">
        <section class="about-section">
          <h4 class="about-section-title">关于作者</h4>
          <el-divider></el-divider>

          <el-card class="about-card">
            <template #header>基本信息</template>
            <div class="about-info-grid">
              <span class="about-info-item"><b>作者：</b>卜俊龙</span>
              <span class="about-info-item"><b>性别：</b>♂</span>
              <span class="about-info-item"><b>年龄：</b>23</span>
              <span class="about-info-item"><b>邮箱：</b>3080782635@qq.com</span>
              <span class="about-info-item"><b>籍贯：</b>四川省遂宁市</span>
            </div>
          </el-card>

          <el-card class="about-card">
            <template #header>教育背景</template>
            <div class="about-edu">
              <h3 class="about-edu-title">专科：2020.09 - 2023.07</h3>
              <div class="about-info-grid">
                <span class="about-info-item"><b>学校：</b>成都纺织高等专科学校</span>
                <span class="about-info-item"><b>学院：</b>计算机学院</span>
                <span class="about-info-item"><b>专业：</b>软件技术</span>
              </div>
            </div>
            <div class="about-edu">
              <h3 class="about-edu-title">本科：2023.09 - 2025.06</h3>
              <div class="about-info-grid">
                <span class="about-info-item"><b>学校：</b>四川大学锦江学院</span>
                <span class="about-info-item"><b>学院：</b>计算机学院</span>
                <span class="about-info-item"><b>专业：</b>软件工程</span>
              </div>
            </div>
          </el-card>

<!--          <el-card class="about-card">-->
<!--            <template #header>获奖 / 荣誉</template>-->
<!--            <div class="about-sub-section">-->
<!--              <h3 class="about-edu-title">所获奖项</h3>-->
<!--              <ul class="about-list">-->
<!--                <li>2020-2021 学年度本专科生 <b>国家奖学金</b></li>-->
<!--                <li>2021-2022 学年度本专科生 <b>国家奖学金</b></li>-->
<!--                <li>美国大学生数学建模竞赛 F（Finalist）奖（特等奖提名）</li>-->
<!--                <li>蓝桥杯省赛二等奖</li>-->
<!--              </ul>-->
<!--            </div>-->
<!--            <div class="about-sub-section">-->
<!--              <h3 class="about-edu-title">所获荣誉</h3>-->
<!--              <ul class="about-list">-->
<!--                <li>南京大学硕士学业一等奖学金</li>-->
<!--                <li>华中科技大学 2023 届 <b>优秀毕业生</b></li>-->
<!--                <li>华中科技大学荣誉学士学位</li>-->
<!--              </ul>-->
<!--            </div>-->
<!--          </el-card>-->

          <el-card class="about-card">
            <template #header>技能专长</template>
            <ul class="about-list">
              <li><b>Java 基础：</b>熟悉 Java 基础知识，如集合框架、I/O流、反射机制等，运用常用的设计模式，如工厂模式、代理模式、单例模式等。</li>
              <li><b>JUC：</b>熟悉 Java多线程，Java 并发编程，集合框架，对线程池、JUC、AQS、ThreadLocal 有详细理解。</li>
              <li><b>MySQL：</b>熟练掌握MySQL有一定的SQL调优经验【查询语句优化、慢sqI排查、索引优化】对锁机制、事务特性有一定的理解。</li>
              <li><b>Redis：</b>练使用Redis及其底层原理【底层数据结构、数据持久化机制、分布式锁实现及过期淘汰策略】，多种Redis应用场景实践，如利用Set数据结构实现UV统计，NX简易实现分布式锁等。</li>
              <li><b>框架：</b>熟练使用 Spring、Mybatis、Mybatis-Plus，MybatisX 插件等，可通过需求快速构建项目。熟练掌握Spring Boot、Spring Cloud 技术栈。</li>
              <li><b>JVM：</b>初步理解 JVM 原理【类加载机制、内存结构、垃圾回收机制等】</li>
              <li><b>MQ：</b>了解消息队列 MQ，有使用 RabbitMQ 经验</li>
              <li><b>其他：</b>有使用前端框架经验【Vue】，了解前后端分离开发模式；掌握版本控制工具 Git、Docker 的基本操作，熟悉Linux系统，熟练使用Docker、Tomcat、Nginx。可独立进行项目部署，问题排查。熟悉常见的服务器安全策略，有编写shell脚本及使用开源项目实现多服务器性能监控的实操。</li>
            </ul>
          </el-card>

          <el-card class="about-card">
            <template #header>实习经历</template>
            <el-collapse>
              <el-collapse-item title="成都灵尧科技有限公司 -兴鲸云职业教育平台">
                <div class="about-collapse-content">
                  <p><b>实习时间：</b>2024.07 - 2025.03</p>
                  <p><b>工作核心：</b>参与项目中新功能开发及BUG修复，主要负责学、练、考、评中的考与评两个阶段内容</p>
                  <ul>
                    <li>针对考试提交、实时判分等高并发场景，基于 RabbitMQ 实现 <b>异步任务队列解耦及削峰</b>，将判题服务响应时间从3秒优化至500毫秒内。</li>
                    <li>集成EasyExcel实现海量题库数据导入导出，结合<b>Redis缓存预加载与分片处理</b>，使百万级数据导入耗时从4分钟降至2分钟。</li>
                    <li>采用 <b>Redis 分布式锁（Redisson 框架）</b>保障分布式环境下答题提交的事务一致性，结合数据库乐观锁降低并发冲突率85%。</li>
                    <li>通过先写MySQL，再删除 Redis的方案来保证高并发场景下的缓存一致性。并通过 Redis 的 zset数据结构和幂等策略，实现计数和用户活跃排行榜。</li>

                  </ul>
                </div>
              </el-collapse-item>
            </el-collapse>
          </el-card>

          <el-card class="about-card">
            <template #header>项目经验</template>
            <el-collapse>
              <el-collapse-item title="编程汇：前后端分离的社区项目  -基于技术派的二次开发（纯 vibing coding 实践）">
                <div class="about-collapse-content">
                  <p><b>技术栈：</b>SpringBoot、MyBatis-Plus、MySQL、Redis、RabbitMQ、Docker</p>
                  <p><b>项目描述：</b>编程汇是一个前后端分离的社区项目、包括前端 PC 和管理后台，用于帮助开发者成长的社区，面向互联网技术人的内容分享平台</p>
                  <p><b>责任描述：</b>作为核心研发人员，主要负责登录认证、消息通知以及文章模块的开发工作</p>
                  <p><b>核心技术：</b></p>
<!--                  <ul>-->
<!--                    <li>将用户相关动态消息的处理机制进行异步优化，在高负载情况下的<b>最大响应时间降低 33%</b></li>-->
<!--                    <li>通过 <b>缓存 + 会话 session</b> 实现计数统计与在线人数实时展示，使系统的<b>平均响应速度提升了 20%</b></li>-->
<!--                    <li>基于 ThreadLocal 封装线程隔离的全局上下文，减少用户信息的数据库查询次数</li>-->
<!--                    <li>针对热点文章进行<b>动态缓存</b>，将访问高频文章的响应时长从 1000ms 提升至 700ms，QPS 提升 25%</li>-->
<!--                    <li>通过分析系统的响应报告，进行简单的 JVM 参数调整，提升了系统的响应性能</li>-->
<!--                    <li>通过验证码和前端保持半长链接映射关系，当用户扫码关注公众号并输入验证码后，发起回调，识别用户信息并找到对应半长链接，实现系统自动登录</li>-->
<!--                  </ul>-->
<!--                  <p><b>项目链接：</b><a href="http://www.xuyifei.site">就是本项目</a></p>-->
                </div>
              </el-collapse-item>
            </el-collapse>
          </el-card>
        </section>
      </div>
      <SideImage></SideImage>
    </div>
    <Footer></Footer>
  </div>
  <LoginDialog :clicked="loginDialogClicked"></LoginDialog>
</template>

<script setup lang="ts">
import HeaderBar from '@/components/layout/HeaderBar.vue'
import Footer from '@/components/layout/Footer.vue'
import SideImage from '@/components/layout/SideImage.vue'
import LoginDialog from '@/components/dialog/LoginDialog.vue'
import { onMounted, provide, ref } from 'vue'
import { useGlobalStore } from '@/stores/global'
import { doGet } from '@/http/BackendRequests'
import type { CommonResponse } from '@/http/ResponseTypes/CommonResponseType'
import { GLOBAL_INFO_URL } from '@/http/URL'

const changeClicked = () => {
  loginDialogClicked.value = !loginDialogClicked.value
}
provide('loginDialogClicked', changeClicked)
const loginDialogClicked = ref(false)

const globalStore = useGlobalStore()
onMounted(async () => {
  await doGet<CommonResponse>(GLOBAL_INFO_URL, {})
    .then((res) => {
      globalStore.setGlobal(res.data.global)
    })
})
</script>

<style scoped>
.about-page {
  background: var(--pai-bg-light-1, #f4f6fa);
  min-height: calc(100vh - var(--header-height, 60px));
  padding-top: calc(var(--header-height, 60px));
}

.about-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 1.25rem;
  display: flex;
  gap: 1.5rem;
  align-items: flex-start;
}

.about-content {
  flex: 1;
  min-width: 0;
}

.about-section {
  max-width: 900px;
}

.about-section-title {
  font-size: 1.1rem;
  font-weight: 700;
  color: var(--pai-color-3-black, #1e2029);
  margin: 0;
}

.about-card {
  margin-bottom: 1rem;
  border-radius: 12px;
}

.about-card :deep(.el-card__header) {
  font-weight: 700;
  font-size: 0.9rem;
  color: var(--pai-color-3-black, #1e2029);
  border-bottom: 1px solid var(--pai-bg-light-2, #eef1f7);
  padding: 0.75rem 1rem;
}

.about-info-grid {
  display: flex;
  flex-wrap: wrap;
  gap: 0.5rem 1.5rem;
}

.about-info-item {
  font-size: 0.85rem;
  color: var(--pai-color-4-gray, #484d5e);
}

.about-edu {
  margin-bottom: 1rem;
}

.about-edu:last-child {
  margin-bottom: 0;
}

.about-edu-title {
  font-size: 0.9rem;
  font-weight: 600;
  color: var(--pai-brand-1-normal);
  margin: 0 0 0.5rem;
}

.about-sub-section {
  margin-bottom: 1rem;
}

.about-list {
  list-style: none;
  padding: 0;
  margin: 0;
  display: flex;
  flex-direction: column;
  gap: 0.4rem;
}

.about-list li {
  font-size: 0.85rem;
  color: var(--pai-color-4-gray, #484d5e);
  padding: 0.25rem 0;
  padding-left: 0.75rem;
  border-left: 3px solid var(--pai-brand-7-light, rgba(45, 124, 246, 0.3));
}

.about-collapse-content {
  font-size: 0.85rem;
  color: var(--pai-color-4-gray, #484d5e);
  line-height: 1.6;
}

.about-collapse-content p {
  margin: 0.4rem 0;
}

.about-collapse-content ul {
  list-style: none;
  padding: 0;
  margin: 0.4rem 0;
}

.about-collapse-content ul li {
  padding: 0.15rem 0 0.15rem 0.75rem;
  border-left: 3px solid var(--pai-brand-7-light, rgba(45, 124, 246, 0.3));
  margin-bottom: 0.25rem;
}

.about-collapse-content a {
  color: var(--pai-brand-1-normal);
  text-decoration: none;
}

@media (max-width: 768px) {
  .about-container {
    padding: 0.75rem;
    flex-direction: column;
  }
}
</style>
