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
              <span class="about-info-item"><b>作者：</b>许逸飞</span>
              <span class="about-info-item"><b>性别：</b>♂</span>
              <span class="about-info-item"><b>年龄：</b>23</span>
              <span class="about-info-item"><b>邮箱：</b>857998989@qq.com</span>
              <span class="about-info-item"><b>籍贯：</b>河北</span>
            </div>
          </el-card>

          <el-card class="about-card">
            <template #header>教育背景</template>
            <div class="about-edu">
              <h3 class="about-edu-title">本科：2019.09 - 2023.06</h3>
              <div class="about-info-grid">
                <span class="about-info-item"><b>学校：</b>华中科技大学</span>
                <span class="about-info-item"><b>学院：</b>电子信息与通信学院</span>
                <span class="about-info-item"><b>专业：</b>电子信息工程</span>
              </div>
            </div>
            <div class="about-edu">
              <h3 class="about-edu-title">研究生：2023.06 至今</h3>
              <div class="about-info-grid">
                <span class="about-info-item"><b>学校：</b>南京大学</span>
                <span class="about-info-item"><b>学院：</b>计算机学院</span>
                <span class="about-info-item"><b>专业：</b>计算机科学与技术</span>
              </div>
            </div>
          </el-card>

          <el-card class="about-card">
            <template #header>获奖 / 荣誉</template>
            <div class="about-sub-section">
              <h3 class="about-edu-title">所获奖项</h3>
              <ul class="about-list">
                <li>2020-2021 学年度本专科生 <b>国家奖学金</b></li>
                <li>2021-2022 学年度本专科生 <b>国家奖学金</b></li>
                <li>美国大学生数学建模竞赛 F（Finalist）奖（特等奖提名）</li>
                <li>蓝桥杯省赛二等奖</li>
              </ul>
            </div>
            <div class="about-sub-section">
              <h3 class="about-edu-title">所获荣誉</h3>
              <ul class="about-list">
                <li>南京大学硕士学业一等奖学金</li>
                <li>华中科技大学 2023 届 <b>优秀毕业生</b></li>
                <li>华中科技大学荣誉学士学位</li>
              </ul>
            </div>
          </el-card>

          <el-card class="about-card">
            <template #header>技能专长</template>
            <ul class="about-list">
              <li><b>Java 基础：</b>熟悉 Java 基础知识，具备良好编码能力，能熟练应用常见的设计模式；熟练使用集合等基础框架</li>
              <li><b>MySQL：</b>熟练掌握 MySQL，对锁机制、索引有深入了解</li>
              <li><b>Redis：</b>熟练使用 Redis 及其底层原理【底层数据结构、数据持久化机制、多种分布式集群实现方式】</li>
              <li><b>框架：</b>会使用 SpringMVC、SpringBoot、MyBatis 等框架</li>
              <li><b>JVM：</b>初步理解 JVM 原理【类加载机制、内存结构、垃圾回收机制等】</li>
              <li><b>JUC：</b>熟悉 Java 多线程与并发底层原理【volatile、synchronized、ReentrantLock】</li>
              <li><b>MQ：</b>了解消息队列 MQ，有使用 RabbitMQ 经验</li>
              <li><b>其他：</b>有使用前端框架经验【Vue、React】，了解前后端分离开发模式；掌握版本控制工具 Git、Docker 的基本操作</li>
            </ul>
          </el-card>

          <el-card class="about-card">
            <template #header>实习经历</template>
            <el-collapse>
              <el-collapse-item title="小红书 - 引擎架构 平台化开发">
                <div class="about-collapse-content">
                  <p><b>实习时间：</b>2024.12 - 2025.03</p>
                  <p><b>工作核心：</b>搭建数据生产与消费侧的通用任务平台，为开发人员提供任务作业的提交与管理服务。主要技术栈为 SpringBoot、MySQL，使用 CI/CD 流程集成部署</p>
                  <ul>
                    <li>参与设计用于生成任务作业的 DSL 配置与图化开发流程，使开发者能通过简易的配置生成任务作业，为每日近 100 亿的离线数据与 50 亿量级的实时消息提供高效可靠的数据的同步与索引构建服务</li>
                    <li>负责开发作业的监控模块，通过监控流批作业的运行状态，维护日均 2000+ 大数据作业的运行状态，并在出现异常时及时告警</li>
                    <li>升级作业的配置版本管理功能，为开发人员提供多配置的回滚及对比功能</li>
                  </ul>
                </div>
              </el-collapse-item>
            </el-collapse>
          </el-card>

          <el-card class="about-card">
            <template #header>项目经验</template>
            <el-collapse>
              <el-collapse-item title="编程汇：前后端分离的社区项目、包括前端 PC 和管理后台">
                <div class="about-collapse-content">
                  <p><b>技术栈：</b>SpringBoot、MyBatis-Plus、MySQL、Redis、RabbitMQ、Docker</p>
                  <p><b>项目描述：</b>编程汇是一个前后端分离的社区项目、包括前端 PC 和管理后台，用于帮助开发者成长的社区，面向互联网技术人的内容分享平台</p>
                  <p><b>责任描述：</b>作为核心研发人员，主要负责登录认证、消息通知以及文章模块的开发工作</p>
                  <p><b>核心技术：</b></p>
                  <ul>
                    <li>将用户相关动态消息的处理机制进行异步优化，在高负载情况下的<b>最大响应时间降低 33%</b></li>
                    <li>通过 <b>缓存 + 会话 session</b> 实现计数统计与在线人数实时展示，使系统的<b>平均响应速度提升了 20%</b></li>
                    <li>基于 ThreadLocal 封装线程隔离的全局上下文，减少用户信息的数据库查询次数</li>
                    <li>针对热点文章进行<b>动态缓存</b>，将访问高频文章的响应时长从 1000ms 提升至 700ms，QPS 提升 25%</li>
                    <li>通过分析系统的响应报告，进行简单的 JVM 参数调整，提升了系统的响应性能</li>
                    <li>通过验证码和前端保持半长链接映射关系，当用户扫码关注公众号并输入验证码后，发起回调，识别用户信息并找到对应半长链接，实现系统自动登录</li>
                  </ul>
                  <p><b>项目链接：</b><a href="http://www.xuyifei.site">就是本项目</a></p>
                </div>
              </el-collapse-item>
            </el-collapse>
            <el-collapse>
              <el-collapse-item title="SimpleDB：手搓的简易版 MySQL 数据库">
                <div class="about-collapse-content">
                  <p><b>项目描述：</b>SimpleDB 致力于实现一个简易版本 MySQL 数据库（基于 InnoDB 引擎）。涵盖了数据存储、事务管理、MVCC 和索引构建等核心数据库功能，还模拟实现日志管理和事务状态查询等特性</p>
                  <p><b>责任描述：</b>独立开发</p>
                  <p><b>核心技术：</b></p>
                  <ul>
                    <li><b>日志管理：</b>引入数据库日志管理机制，通过日志保障数据一致性，实现故障恢复功能，强化数据的安全性和稳定性</li>
                    <li><b>MVCC 与锁协议：</b>基于两阶段锁（2PL）协议与 MVCC 实现事务的读提交、可重复读隔离级别，并解决了<b>版本跳跃</b>问题</li>
                    <li><b>死锁检测与预防：</b>通过维护一个依赖等待图来监控并避免系统中出现死锁</li>
                    <li><b>索引与表管理：</b>实现<b>基于 B+ 树的聚簇索引</b>，支持高效的索引查询；并构建表管理器，负责管理表结构和字段信息、解析 SQL 语句</li>
                  </ul>
                  <p><b>项目链接：</b><a href="https://github.com/XuYi-fei/SimpleDB">SimpleDB</a></p>
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
