<template>
  <HeaderBar />
  <div class="conv-page">
    <div class="conv-container">
      <MessageHeader
        :target-user="targetUser"
        @back="goBack"
        @user-click="goToUserPage"
        @clear="handleClear"
      />

      <div class="conv-messages" ref="messagesRef" @scroll="onScroll">
        <div v-if="loadingMessages" class="conv-loading">
          <el-icon class="is-loading" :size="20"><Loading /></el-icon>
        </div>

        <div v-else-if="hasMoreMessages" class="conv-load-more">
          <el-button text :loading="loadingMore" @click="loadOlderMessages">
            {{ loadingMore ? '加载中...' : '加载更多消息' }}
          </el-button>
        </div>

        <div v-if="messages.length === 0 && !loadingMessages" class="conv-empty">
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
  </div>
  <LoginDialog :clicked="loginDialogClicked" />
</template>

<script setup lang="ts">
import HeaderBar from '@/components/layout/HeaderBar.vue'
import LoginDialog from '@/components/dialog/LoginDialog.vue'
import MessageHeader from '@/components/message/MessageHeader.vue'
import MessageBubble from '@/components/message/MessageBubble.vue'
import MessageInput from '@/components/message/MessageInput.vue'
import { useGlobalStore } from '@/stores/global'
import { useMessageStore } from '@/stores/message'
import { onMounted, ref, provide, nextTick, watch, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { fetchMessages, sendMessage, markConversationRead, clearConversation } from '@/http/MessageRequests'
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

const conversationId = computed(() => Number(route.params.conversationId))
const messagesRef = ref<HTMLElement | null>(null)

const messages = ref<MessageItem[]>([])
const loadingMessages = ref(true)
const loadingMore = ref(false)
const sending = ref(false)
const hasMoreMessages = ref(true)
const currentPage = ref(1)
const autoScroll = ref(true)

// 后端 API 返回的对方用户信息（来自 listMessages 响应的 targetUser 字段）
const apiResolvedTargetUser = ref<SimpleUserInfo | null>(null)

// 目标用户信息（多级兜底：store会话列表 → pendingTargetUserId → API返回 → 消息推断）
const targetUser = computed<SimpleUserInfo>(() => {
  const curId = currentUserId.value
  const conv = messageStore.conversations.find(c => c.conversationId === conversationId.value)
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

onMounted(async () => {
  if (!global.isLogin) {
    loginDialogClicked.value = true
    return
  }
  await loadMessages()
  await markRead()
})

watch(conversationId, async () => {
  if (!global.isLogin) return
  messages.value = []
  currentPage.value = 1
  hasMoreMessages.value = true
  loadingMessages.value = true
  await loadMessages()
  await markRead()
})

// 监听 WebSocket 推送的新消息，实时追加到当前会话
watch(() => messageStore.latestWsMessage, (msg) => {
  if (!msg) return
  const curConvId = conversationId.value
  if (msg.conversationId !== curConvId) {
    console.info('[ConversationView] ignore ws msg (not current conversation)', {
      curConvId,
      msgConvId: msg.conversationId,
      msgId: msg.messageId
    })
    return
  }
  // 避免重复追加（通过 messageId 去重）
  if (messages.value.some(m => m.messageId === msg.messageId)) return
  console.info('[ConversationView] append ws msg', { curConvId, msgId: msg.messageId })
  messages.value.push(msg)
  if (autoScroll.value) scrollToBottom()
})

async function loadMessages() {
  loadingMessages.value = true
  try {
    const res = await fetchMessages(conversationId.value, 1, 20)
    const data = res.data.result
    messages.value = data.list || []
    hasMoreMessages.value = data.hasMore || false
    currentPage.value = 1
    // 提取后端返回的对方用户信息（解决前端 targetUser 兜底解析失败的问题）
    if (data.targetUser && data.targetUser.userId > 0) {
      apiResolvedTargetUser.value = {
        userId: Number(data.targetUser.userId),
        userName: data.targetUser.userName || '用户',
        photo: data.targetUser.photo || 'https://static.developers.pub/static/img/logo.b2ff606.jpeg'
      }
    }
    await scrollToBottom()
  } catch (e) {
    console.error('Failed to load messages:', e)
    messageTip('加载消息失败', 'error')
  } finally {
    loadingMessages.value = false
  }
}

async function loadOlderMessages() {
  if (loadingMore.value || !hasMoreMessages.value) return
  loadingMore.value = true
  try {
    const nextPage = currentPage.value + 1
    const res = await fetchMessages(conversationId.value, nextPage, 20)
    const data = res.data.result
    const newItems = data.list || []
    messages.value.unshift(...newItems)
    hasMoreMessages.value = data.hasMore || false
    currentPage.value = nextPage
  } catch (e) {
    console.error('Failed to load older messages:', e)
    messageTip('加载失败', 'error')
  } finally {
    loadingMore.value = false
  }
}

async function markRead() {
  try {
    await markConversationRead(conversationId.value)
    messageStore.resetUnread(conversationId.value)
  } catch (e) {
    console.error('Failed to mark as read:', e)
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

  // 先追加本地消息（乐观更新）
  const tempMsg: MessageItem = {
    messageId: -Date.now(),
    conversationId: conversationId.value,
    fromUserId: currentUserId.value,
    fromUserName: global.user?.userName || '我',
    fromUserPhoto: global.user?.photo || '',
    messageType: 'TEXT',
    content: content,
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
    // 更新本地消息ID
    const sentMsg = messages.value.find(m => m.messageId === tempMsg.messageId)
    if (sentMsg) {
      sentMsg.messageId = res.data.result.messageId
      sentMsg.status = 'SENT'
      // [已修复] 仅当 conversationId 变化时更新（首次发送/路由变更时）
      if (sentMsg.conversationId !== res.data.result.conversationId) {
        sentMsg.conversationId = res.data.result.conversationId
      }
    }
  } catch (e) {
    console.error('Failed to send message:', e)
    messageTip('发送失败', 'error')
    // 标记发送失败
    const failedMsg = messages.value.find(m => m.messageId === tempMsg.messageId)
    if (failedMsg) {
      failedMsg.status = 'FAILED'  // [已修复] 发送失败标记为 FAILED
    }
  } finally {
    sending.value = false
  }
}

function onScroll() {
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
    await clearConversation(conversationId.value)
    messageStore.removeConversation(conversationId.value)
    messageTip('会话已清除', 'success')
    router.push('/messages')
  } catch (e) {
    // 用户取消或 API 异常
    if (e !== 'cancel') {
      console.error('Failed to clear conversation:', e)
      messageTip('清除失败', 'error')
    }
  }
}

// Login dialog
const loginDialogClicked = ref(false)
function changeClicked() {
  loginDialogClicked.value = !loginDialogClicked.value
}
provide('loginDialogClicked', changeClicked)
</script>

<style scoped>
.conv-page {
  background: var(--pai-bg-light-1, #f4f6fa);
  min-height: calc(100vh - var(--header-height, 60px));
  padding-top: calc(var(--header-height, 60px));
  display: flex;
  flex-direction: column;
}

.conv-container {
  flex: 1;
  max-width: 640px;
  margin: 1.25rem auto;
  width: 100%;
  background: var(--pai-bg-white-fff, #ffffff);
  border-radius: 16px;
  box-shadow: 0 1px 3px rgba(26, 29, 39, 0.04);
  overflow: hidden;
  display: flex;
  flex-direction: column;
}

.conv-messages {
  flex: 1;
  overflow-y: auto;
  padding: 0.75rem 0;
  display: flex;
  flex-direction: column;
  gap: 0.15rem;
}

.conv-loading {
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 2rem 0;
}

.conv-load-more {
  display: flex;
  justify-content: center;
  padding: 0.5rem 0;
}

.conv-empty {
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 3rem 1rem;
  color: var(--pai-color-999-gray, #8c8f9c);
  font-size: 0.85rem;
}

@media (max-width: 768px) {
  .conv-container {
    margin: 0;
    border-radius: 0;
    min-height: calc(100vh - var(--header-height, 60px));
  }
}
</style>
