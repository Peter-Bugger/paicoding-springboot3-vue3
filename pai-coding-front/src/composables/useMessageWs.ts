import Stomp from 'stompjs'
import { useMessageStore } from '@/stores/message'
import { WS_URL } from '@/http/URL'
import { getCookie } from '@/util/utils'
import { markConversationRead } from '@/http/MessageRequests'
import type { WsMsgPush, WsNewMessagePayload } from '@/http/ResponseTypes/MsgTypes'

/**
 * 私信 WebSocket 连接管理 (单例模式)
 * - 跨页面持久连接，不依赖组件生命周期
 * - 在 App.vue 登录后初始化
 * - 使用 Pinia store 同步连接状态
 * - 断线自动重连（递增延迟）
 */

let stompClient: Stomp.Client | null = null
let _connected = false
let _connecting = false
let _disposed = false
let _reconnectTimer: ReturnType<typeof setTimeout> | null = null
let _reconnectAttempts = 0
const MAX_RECONNECT_ATTEMPTS = 10
const RECONNECT_BASE_DELAY = 3000 // 基础重连延迟 3 秒

/**
 * 建立 STOMP WebSocket 连接
 * 订阅 /user/msg/new 和 /user/msg/read
 */
export function connectMessageWs() {
  const session = getCookie('f-session')
  if (_connected || _connecting || !session) {
    if (!session) {
      console.info('[MessageWS] connect skipped: no session cookie')
    }
    return
  }

  // disconnectMessageWs() 会标记 disposed；重新连接时需要复位
  _disposed = false
  _connecting = true

  try {
    console.info('[MessageWS] connecting', { session, _connected, _connecting, _disposed })
    const socket = new WebSocket(`${WS_URL}/msg/${session}`)
    const client = Stomp.over(socket)

    // 禁用 STOMP 心跳调试日志，避免控制台刷屏
    client.debug = () => {}

    client.connect(
      {},
      () => {
        if (_disposed) {
          console.info('[MessageWS] connect succeeded but disposed=true; disconnecting')
          _connecting = false
          try { client.disconnect(() => {}) } catch (_) { /* ignore */ }
          return
        }

        console.info('[MessageWS] connected')
        _connected = true
        _connecting = false
        _reconnectAttempts = 0
        stompClient = client

        const store = useMessageStore()
        store.setWsConnected(true)

        // 订阅新消息推送
        client.subscribe('/user/msg/new', (message: Stomp.Message) => {
          try {
            const push = JSON.parse(message.body) as WsMsgPush
            if (push.type === 'NEW_MESSAGE') {
              const payload = push.payload as WsNewMessagePayload
              console.info('[MessageWS] new message', {
                conversationId: payload.conversationId,
                messageId: payload.messageId,
                fromUserId: payload.fromUserId
              })
              const store = useMessageStore()
              const convId = Number(payload.conversationId)
              const messageId = Number(payload.messageId)
              const fromUserId = Number(payload.fromUserId)

              // 判断是否是当前正在查看的会话
              const isCurrentConversation = convId === store.currentConversationId

              if (isCurrentConversation) {
                // 当前会话：不增加未读数，直接标记已读
                store.handleNewMessage({
                  conversationId: convId,
                  content: payload.content,
                  fromUserId,
                  createTime: payload.createTime
                })
                // 自动调用后端已读接口
                markConversationRead(convId)
                  .then(() => {
                    store.resetUnread(convId)
                  })
                  .catch((e: unknown) => {
                    console.error('[MessageWS] auto mark read failed:', e)
                  })
              } else {
                store.incrementUnread()
                store.handleNewMessage({
                  conversationId: convId,
                  content: payload.content,
                  fromUserId,
                  createTime: payload.createTime
                })
              }

              if (!convId || Number.isNaN(convId)) {
                console.warn('[MessageWS] invalid conversationId in payload', payload)
              }

              console.info('[MessageWS] deliver to views', {
                convId,
                messageId,
                fromUserId
              })
              // 构建完整 MessageItem 供会话页实时追加
              store.setLatestWsMessage({
                messageId: Number(payload.messageId),
                conversationId: convId,
                fromUserId: Number(payload.fromUserId),
                fromUserName: payload.fromUserName || '',
                fromUserPhoto: payload.fromUserPhoto || '',
                messageType: payload.messageType || 'TEXT',
                content: payload.content,
                referencedMsgId: null,
                attachment: null,
                status: 'DELIVERED',
                createTime: typeof payload.createTime === 'string'
                  ? payload.createTime
                  : new Date().toISOString()
              })
              console.info('[MessageWS] latestWsMessage set', { convId })
            }
          } catch (e) {
            console.error('[MessageWS] Failed to parse new message:', e)
          }
        })

        // 订阅已读状态推送
        client.subscribe('/user/msg/read', (message: Stomp.Message) => {
          try {
            const push = JSON.parse(message.body) as WsMsgPush
            if (push.type === 'READ_STATUS') {
              // 后端可能会把 Long 序列化成字符串，统一转 number 避免会话页过滤不命中
              const payload = push.payload as any
              console.info('[MessageWS] read status', {
                conversationId: Number(payload?.conversationId),
                messageId: Number(payload?.messageId),
                readByUserId: Number(payload?.readByUserId)
              })
            }
          } catch (e) {
            console.error('[MessageWS] Failed to parse read status:', e)
          }
        })
      },
      (error: unknown) => {
        console.error('[MessageWS] Connection failed:', error)
        _connected = false
        _connecting = false
        stompClient = null
        try {
          const store = useMessageStore()
          store.setWsConnected(false)
        } catch (_) { /* ignore */ }
        // 连接失败：尝试重连
        scheduleReconnect()
      }
    )

    socket.onclose = (event: CloseEvent) => {
      console.info('[MessageWS] socket closed', { code: event.code, reason: event.reason, _disposed })
      _connected = false
      _connecting = false
      stompClient = null
      try {
        const store = useMessageStore()
        store.setWsConnected(false)
      } catch (_) { /* ignore */ }

      // 非主动断开时尝试重连
      if (!_disposed) {
        scheduleReconnect()
      }
    }
  } catch (e) {
    console.error('[MessageWS] Failed to create connection:', e)
    _connected = false
    _connecting = false
    // 创建连接失败：尝试重连
    scheduleReconnect()
  }
}

/**
 * 排期重连（递增延迟）
 */
function scheduleReconnect() {
  if (_disposed || _reconnectTimer) return

  if (_reconnectAttempts >= MAX_RECONNECT_ATTEMPTS) {
    console.warn('[MessageWS] max reconnect attempts reached, giving up')
    return
  }

  _reconnectAttempts++
  const delay = RECONNECT_BASE_DELAY * Math.min(_reconnectAttempts, 5)
  console.info(`[MessageWS] scheduling reconnect attempt ${_reconnectAttempts} in ${delay}ms`)

  _reconnectTimer = setTimeout(() => {
    _reconnectTimer = null
    console.info('[MessageWS] attempting reconnect')
    connectMessageWs()
  }, delay)
}

/**
 * 断开 STOMP WebSocket 连接
 */
export function disconnectMessageWs() {
  console.info('[MessageWS] disconnect requested')
  _disposed = true

  // 清除重连定时器
  if (_reconnectTimer) {
    clearTimeout(_reconnectTimer)
    _reconnectTimer = null
  }
  _reconnectAttempts = 0

  if (stompClient) {
    try {
      stompClient.disconnect(() => {
        console.info('[MessageWS] disconnected')
        _connected = false
        stompClient = null
        try {
          const store = useMessageStore()
          store.setWsConnected(false)
        } catch (_) { /* ignore */ }
      })
    } catch (e) {
      console.error('[MessageWS] Disconnect error:', e)
      _connected = false
      stompClient = null
    }
  }
  _connected = false
  _connecting = false
}

/**
 * 当前连接状态
 */
export function isMessageWsConnected(): boolean {
  return _connected
}
