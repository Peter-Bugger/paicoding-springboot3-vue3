<template>
  <HeaderBar />
  <div class="msg-page" :class="{ 'msg-page--mobile-chat': isMobile && selectedId }">
    <!-- 左侧：会话列表（桌面端常显，移动端无选中会话时显示） -->
    <div class="msg-list-panel" v-show="!isMobile || !selectedId">
      <div class="msg-list-header">
        <h2 class="msg-list-title">私信</h2>
      </div>

      <div v-if="loading" class="msg-list-status">
        <el-icon class="is-loading" :size="24"><Loading /></el-icon>
      </div>

      <div v-else-if="conversations.length === 0" class="msg-list-status">
        <el-empty description="暂无私信" />
      </div>

      <div v-else class="msg-list-items">
        <ConversationItem
          v-for="conv in conversations"
          :key="conv.conversationId"
          :item="conv"
          :class="{ 'conv-item--active': conv.conversationId === selectedId }"
          @click="selectConversation(conv.conversationId)"
        />
      </div>

      <div v-if="hasMore && conversations.length > 0" class="msg-list-more">
        <el-button text :loading="loadingMore" @click="loadMore">
          {{ loadingMore ? '加载中...' : '加载更多' }}
        </el-button>
      </div>
    </div>

    <!-- 右侧：聊天面板（桌面端） / 全屏聊天（移动端） -->
    <div class="msg-chat-panel" v-if="selectedId">
      <MessageHeader
        :target-user="targetUser"
        @back="goBack"
        @user-click="goToUserPage"
        @clear="handleClear"
      />

      <div class="msg-chat-messages" ref="messagesRef" @scroll="onChatScroll">
        <div v-if="loadingMessages" class="msg-chat-status">
          <el-icon class="is-loading" :size="20"><Loading /></el-icon>
        </div>

        <div v-else-if="hasMoreMessages" class="msg-chat-load-more">
          <el-button text :loading="loadingMoreMessages" @click="loadOlderMessages">
            {{ loadingMoreMessages ? '加载中...' : '加载更多消息' }}
          </el-button>
        </div>

        <div v-if="messages.length === 0 && !loadingMessages" class="msg-chat-empty-hint">
          <span>暂无消息，发送第一条消息吧</span>
        </div>

        <MessageBubble
          v-for="msg in messages"
          :key="msg.messageId"
          :message="msg"
          :is-own="msg.fromUserId === currentUserId"
        />
      </div>

      <MessageInput
        :loading="sending"
        :disabled="!global.isLogin"
        @send="handleSend"
      />
    </div>

    <!-- 桌面端无选中会话时的占位提示 -->
    <div class="msg-chat-placeholder" v-if="!selectedId && !isMobile">
      <div class="msg-chat-placeholder-inner">
        <svg width="64" height="64" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.2" stroke-linecap="round" class="msg-chat-placeholder-icon">
          <path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"/>
        </svg>
        <span>选择一条私信开始聊天</span>
      </div>
    </div>
  </div>
  <Footer v-if="!selectedId || !isMobile" />
  <LoginDialog :clicked="loginDialogClicked" />
</template>

<script setup lang="ts">
import HeaderBar from '@/components/layout/HeaderBar.vue'
import Footer from '@/components/layout/Footer.vue'
import LoginDialog from '@/components/dialog/LoginDialog.vue'
import ConversationItem from '@/components/message/ConversationItem.vue'
import MessageHeader from '@/components/message/MessageHeader.vue'
import MessageBubble from '@/components/message/MessageBubble.vue'
import MessageInput from '@/components/message/MessageInput.vue'
import { useGlobalStore } from '@/stores/global'
import { useMessageStore } from '@/stores/message'
import { onMounted, ref, provide, nextTick, watch, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import {
  fetchConversations,
  fetchMessages,
  sendMessage,
  markConversationRead,
  clearConversation,
  fetchUnreadCount
} from '@/http/MessageRequests'
import type { MessageItem, SimpleUserInfo } from '@/http/ResponseTypes/MsgTypes'
import { ElMessageBox } from 'element-plus'
import { Loading } from '@element-plus/icons-vue'
import { messageTip } from '@/util/utils'

const globalStore = useGlobalStore()
const messageStore = useMessageStore()
const route = useRoute()
const router = useRouter()

const global = globalStore.global
const currentUserId = computed(() => Number(global.user?.userId || 0))

// ========== 响应式断点 ==========
const isMobile = ref(window.innerWidth < 768)

function onResize() {
  isMobile.value = window.innerWidth < 768
}
window.addEventListener('resize', onResize)

// ========== 当前选中的会话 ==========
const selectedId = computed(() => {
  const id = Number(route.params.conversationId)
  return id > 0 ? id : 0
})

// ========== 会话列表（响应式绑定 store） ==========
const conversations = computed(() => messageStore.conversations)
const loading = ref(true)
const loadingMore = ref(false)
const hasMore = ref(true)
const currentPage = ref(1)

/**
 * 从后端同步未读总数，消除客户端增量计数与后端权威数据之间的漂移
 */
async function syncUnreadFromBackend() {
  try {
    const res = await fetchUnreadCount()
    messageStore.setUnreadTotal(res.data.result.totalUnread)
  } catch (e) {
    console.error('[MessageListView] Failed to sync unread count:', e)
  }
}

async function loadConversations() {
  loading.value = true
  try {
    const res = await fetchConversations(1, 20)
    const data = res.data.result
    messageStore.setConversations(data.list || [])
    hasMore.value = data.hasMore || false
    currentPage.value = 1
    // 从后端同步权威未读总数，消除客户端增量计数的漂移
    await syncUnreadFromBackend()
  } catch (e) {
    console.error('Failed to load conversations:', e)
    messageTip('加载失败', 'error')
  } finally {
    loading.value = false
  }
}

async function loadMore() {
  if (loadingMore.value || !hasMore.value) return
  loadingMore.value = true
  try {
    const nextPage = currentPage.value + 1
    const res = await fetchConversations(nextPage, 20)
    const data = res.data.result
    messageStore.appendConversations(data.list || [])
    hasMore.value = data.hasMore || false
    currentPage.value = nextPage
  } catch (e) {
    console.error('Failed to load more:', e)
    messageTip('加载失败', 'error')
  } finally {
    loadingMore.value = false
  }
}

function selectConversation(conversationId: number) {
  router.push(`/messages/${conversationId}`)
}

// ========== 会话切换时的已读标记状态保护 ==========
// 标记正在执行的 markRead 数量（支持并发），防止 _needsRefresh 触发的
// loadConversations() 在已读标记尚未完成时覆盖掉刚清零的 unreadCount
const markReadActiveCount = ref(0)
// 在 markRead 执行期间被跳过的刷新请求，待所有 markRead 完成后执行
let pendingRefresh: (() => Promise<void>) | null = null

// ========== 聊天面板状态 ==========
const messagesRef = ref<HTMLElement | null>(null)
const messages = ref<MessageItem[]>([])
const loadingMessages = ref(false)
const loadingMoreMessages = ref(false)
const sending = ref(false)
const hasMoreMessages = ref(true)
const chatPage = ref(1)
const autoScroll = ref(true)

// 后端 API 返回的对方用户信息（来自 listMessages 响应的 targetUser 字段）
const apiResolvedTargetUser = ref<SimpleUserInfo | null>(null)

// 目标用户信息（多级兜底：store会话列表 → pendingTargetUserId → API返回 → 消息推断）
const targetUser = computed<SimpleUserInfo>(() => {
  const curId = currentUserId.value
  const conv = conversations.value.find(c => c.conversationId === selectedId.value)
  if (conv?.targetUser && conv.targetUser.userId > 0 && conv.targetUser.userId !== curId) {
    return conv.targetUser
  }
  if (messageStore.pendingTargetUserId > 0 && messageStore.pendingTargetUserId !== curId) {
    return {
      userId: messageStore.pendingTargetUserId,
      userName: '用户',
      photo: 'https://static.developers.pub/static/img/logo.b2ff606.jpeg'
    }
  }
  // 后端 API 直接返回的 targetUser（最可靠的非 store 来源）
  if (apiResolvedTargetUser.value && apiResolvedTargetUser.value.userId > 0 && apiResolvedTargetUser.value.userId !== curId) {
    return apiResolvedTargetUser.value
  }
  // 兜底：从已加载消息中推断对方
  const otherMsg = messages.value.find(m => m.fromUserId !== curId)
  if (otherMsg) {
    return {
      userId: otherMsg.fromUserId,
      userName: otherMsg.fromUserName || '用户',
      photo: otherMsg.fromUserPhoto || 'https://static.developers.pub/static/img/logo.b2ff606.jpeg'
    }
  }
  return {
    userId: 0,
    userName: '用户',
    photo: 'https://static.developers.pub/static/img/logo.b2ff606.jpeg'
  }
})

// ========== 聊天面板方法 ==========

/** 取消上一次进行中的消息加载请求，防止快速切换时旧数据覆盖新数据 */
let abortLoadMessages: AbortController | null = null

/**
 * 加载指定会话的消息，接受显式 convId 以避免快速切换时
 * selectedId 已被后续导航覆盖导致数据错乱
 */
async function loadMessagesForId(convId: number) {
  if (!convId) return
  // 取消上一次进行中的请求
  if (abortLoadMessages) {
    abortLoadMessages.abort()
  }
  abortLoadMessages = new AbortController()
  const signal = abortLoadMessages.signal

  loadingMessages.value = true
  try {
    const res = await fetchMessages(convId, 1, 20, signal)
    const data = res.data.result
    messages.value = data.list || []
    hasMoreMessages.value = data.hasMore || false
    chatPage.value = 1
    // 提取后端返回的对方用户信息（解决前端 targetUser 兜底解析失败的问题）
    if (data.targetUser && data.targetUser.userId > 0) {
      apiResolvedTargetUser.value = {
        userId: Number(data.targetUser.userId),
        userName: data.targetUser.userName || '用户',
        photo: data.targetUser.photo || 'https://static.developers.pub/static/img/logo.b2ff606.jpeg'
      }
    }
    await scrollToBottom()
  } catch (e: unknown) {
    if (e instanceof DOMException && e.name === 'AbortError') return
    console.error('Failed to load messages:', e)
    messageTip('加载消息失败', 'error')
  } finally {
    if (abortLoadMessages?.signal === signal) {
      abortLoadMessages = null
    }
    loadingMessages.value = false
  }
}

async function loadOlderMessages() {
  if (loadingMoreMessages.value || !hasMoreMessages.value || !selectedId.value) return
  loadingMoreMessages.value = true
  try {
    const nextPage = chatPage.value + 1
    const res = await fetchMessages(selectedId.value, nextPage, 20)
    const data = res.data.result
    messages.value.unshift(...(data.list || []))
    hasMoreMessages.value = data.hasMore || false
    chatPage.value = nextPage
  } catch (e) {
    console.error('Failed to load older messages:', e)
    messageTip('加载失败', 'error')
  } finally {
    loadingMoreMessages.value = false
  }
}

/**
 * 标记指定会话为已读，接受显式 convId 以避免快速切换时
 * selectedId 已被后续导航覆盖导致误操作
 */
async function markReadForId(convId: number) {
  if (!convId) return
  markReadActiveCount.value++
  try {
    await markConversationRead(convId)
    messageStore.resetUnread(convId)
  } catch (e) {
    console.error('Failed to mark as read:', e)
  } finally {
    markReadActiveCount.value--
    // 所有并发的 markRead 都完成后，执行被跳过的刷新请求
    if (markReadActiveCount.value === 0 && pendingRefresh) {
      const refresh = pendingRefresh
      pendingRefresh = null
      refresh()
    }
  }
}

async function handleSend(content: string) {
  if (!global.isLogin) {
    loginDialogClicked.value = true
    return
  }

  const toUserId = targetUser.value.userId
  if (!toUserId || toUserId === currentUserId.value) {
    messageTip('无法确定接收用户', 'error')
    return
  }

  // 乐观更新
  const tempMsg: MessageItem = {
    messageId: -Date.now(),
    conversationId: selectedId.value,
    fromUserId: currentUserId.value,
    fromUserName: global.user?.userName || '我',
    fromUserPhoto: global.user?.photo || '',
    messageType: 'TEXT',
    content,
    referencedMsgId: null,
    attachment: null,
    status: 'SENDING',
    createTime: new Date().toISOString()
  }
  messages.value.push(tempMsg)
  await scrollToBottom()

  sending.value = true
  try {
    const res = await sendMessage({ toUserId, content })
    const newConversationId = res.data.result.conversationId
    const sentMsg = messages.value.find(m => m.messageId === tempMsg.messageId)
    if (sentMsg) {
      sentMsg.messageId = res.data.result.messageId
      sentMsg.status = 'SENT'
      if (sentMsg.conversationId !== newConversationId) {
        sentMsg.conversationId = newConversationId
      }
    }
    // 更新会话列表：将当前会话置顶并更新最后一条消息摘要
    messageStore.handleOwnSentMessage(
      newConversationId,
      content,
      tempMsg.createTime
    )
  } catch (e) {
    console.error('Failed to send message:', e)
    messageTip('发送失败', 'error')
    const failedMsg = messages.value.find(m => m.messageId === tempMsg.messageId)
    if (failedMsg) {
      failedMsg.status = 'FAILED'
    }
  } finally {
    sending.value = false
  }
}

function onChatScroll() {
  const el = messagesRef.value
  if (!el) return
  const distanceFromBottom = el.scrollHeight - el.scrollTop - el.clientHeight
  autoScroll.value = distanceFromBottom < 60
}

async function scrollToBottom() {
  await nextTick()
  const el = messagesRef.value
  if (el) {
    el.scrollTo({ top: el.scrollHeight, behavior: 'smooth' })
  }
}

function goBack() {
  router.push('/messages')
}

function goToUserPage() {
  if (targetUser.value.userId > 0) {
    router.push(`/user/${targetUser.value.userId}`)
  }
}

async function handleClear() {
  try {
    await ElMessageBox.confirm(
      '确定要清除该会话吗？清除后聊天记录将被隐藏，对方不受影响。',
      '清除会话',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    await clearConversation(selectedId.value)
    messageStore.removeConversation(selectedId.value)
    messageTip('会话已清除', 'success')
    router.push('/messages')
  } catch (e) {
    if (e !== 'cancel') {
      console.error('Failed to clear conversation:', e)
      messageTip('清除失败', 'error')
    }
  }
}

// ========== 响应 WebSocket 推送 ==========

// 监听选中会话的实时消息
watch(() => messageStore.latestWsMessage, (msg) => {
  if (!msg || msg.conversationId !== selectedId.value) return
  if (messages.value.some(m => m.messageId === msg.messageId)) return
  messages.value.push(msg)
  if (autoScroll.value) scrollToBottom()
})

// 监听 WebSocket 触发的新会话刷新标记
// 如果 markRead 正在执行，则暂缓刷新，避免覆盖刚清零的 unreadCount
watch(() => messageStore._needsRefresh, (needs) => {
  if (!needs) return
  messageStore.consumeRefreshFlag()
  if (markReadActiveCount.value > 0) {
    // 暂缓刷新，待 markRead 完成后执行
    pendingRefresh = () => loadConversations()
    return
  }
  loadConversations()
})

// ========== 生命周期 ==========

onMounted(async () => {
  if (!global.isLogin) {
    loginDialogClicked.value = true
    loading.value = false
    return
  }
  messageStore.setCurrentUserId(currentUserId.value)
  await loadConversations()
  // 如果 URL 带有 conversationId，加载对应聊天
  if (selectedId.value) {
    const convId = selectedId.value
    messageStore.setCurrentConversationId(convId)
    await loadMessagesForId(convId)
    await markReadForId(convId)
  }
})

// 选中会话变化时加载消息
// 捕获会话ID避免快速切换时 markRead 落到了错误的会话上
watch(selectedId, async (newId) => {
  if (!newId || !global.isLogin) {
    if (!newId) messageStore.setCurrentConversationId(0)
    return
  }
  const capturedId = newId
  messageStore.setCurrentConversationId(capturedId)
  messages.value = []
  chatPage.value = 1
  hasMoreMessages.value = true
  loadingMessages.value = true
  await loadMessagesForId(capturedId)
  await markReadForId(capturedId)
})

// 登录弹窗
const loginDialogClicked = ref(false)
function changeClicked() {
  loginDialogClicked.value = !loginDialogClicked.value
}
provide('loginDialogClicked', changeClicked)
</script>

<style scoped>
.msg-page {
  background: var(--pai-bg-light-1, #f4f6fa);
  min-height: calc(100vh - var(--header-height, 60px));
  padding-top: calc(var(--header-height, 60px));
  display: flex;
}

/* ===== 会话列表面板 ===== */
.msg-list-panel {
  width: 360px;
  min-width: 360px;
  border-right: 1px solid var(--pai-border-color-1, #eef1f7);
  background: var(--pai-bg-white-fff, #ffffff);
  display: flex;
  flex-direction: column;
  overflow-y: auto;
  height: calc(100vh - var(--header-height, 60px));
}

.msg-list-header {
  padding: 1rem 1.25rem;
  border-bottom: 1px solid var(--pai-border-color-1, #eef1f7);
  flex-shrink: 0;
}

.msg-list-title {
  font-size: 1.15rem;
  font-weight: 700;
  margin: 0;
  color: var(--pai-color-3-black, #1e2029);
}

.msg-list-status {
  flex: 1;
  display: flex;
  justify-content: center;
  align-items: center;
}

.msg-list-items {
  flex: 1;
  overflow-y: auto;
}

.conv-item--active {
  background: var(--pai-brand-7-light, rgba(45, 124, 246, 0.08));
  border-left: 3px solid var(--pai-brand-1-normal);
}

.msg-list-more {
  display: flex;
  justify-content: center;
  padding: 0.75rem;
  flex-shrink: 0;
}

/* ===== 聊天面板 ===== */
.msg-chat-panel {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-width: 0;
  background: var(--pai-bg-white-fff, #ffffff);
  height: calc(100vh - var(--header-height, 60px));
}

.msg-chat-messages {
  flex: 1;
  overflow-y: auto;
  padding: 0.75rem 0;
  display: flex;
  flex-direction: column;
  gap: 0.15rem;
}

.msg-chat-status {
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 2rem 0;
}

.msg-chat-load-more {
  display: flex;
  justify-content: center;
  padding: 0.5rem 0;
}

.msg-chat-empty-hint {
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 3rem 1rem;
  color: var(--pai-color-999-gray, #8c8f9c);
  font-size: 0.85rem;
}

/* ===== 桌面端占位提示 ===== */
.msg-chat-placeholder {
  flex: 1;
  display: flex;
  justify-content: center;
  align-items: center;
  background: var(--pai-bg-white-fff, #ffffff);
}

.msg-chat-placeholder-inner {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 1rem;
  color: var(--pai-color-999-gray, #8c8f9c);
  font-size: 0.9rem;
}

.msg-chat-placeholder-icon {
  color: var(--pai-border-color-3, #c5c9d6);
}

/* ===== 移动端适配 ===== */
@media (max-width: 767px) {
  .msg-page {
    flex-direction: column;
  }

  .msg-list-panel {
    width: 100%;
    min-width: unset;
    border-right: none;
  }

  .msg-chat-panel {
    width: 100%;
    position: fixed;
    inset: 0;
    z-index: 100;
    height: 100vh;
    padding-top: 0;
  }

  .msg-page--mobile-chat {
    /* hide page-level padding on mobile chat */
    padding-top: 0;
    min-height: 100vh;
  }

  .msg-chat-placeholder {
    display: none;
  }
}
</style>
