import Stomp from 'stompjs'
import { useMessageStore } from '@/stores/message'
import { WS_URL } from '@/http/URL'
import { getCookie } from '@/util/utils'
import type { WsMsgPush, WsNewMessagePayload } from '@/http/ResponseTypes/MsgTypes'

/**
 * 私信 WebSocket 连接管理 (单例模式)
 * - 跨页面持久连接，不依赖组件生命周期
 * - 在 App.vue 登录后初始化
 * - 使用 Pinia store 同步连接状态
 */

let stompClient: Stomp.Client | null = null
let _connected = false
let _connecting = false
let _disposed = false

/**
 * 建立 STOMP WebSocket 连接
 * 订阅 /user/msg/new 和 /user/msg/read
 */
export function connectMessageWs() {
  const session = getCookie('f-session')
  if (_connected || _connecting || !session) return

  _connecting = true

  try {
    const socket = new WebSocket(`${WS_URL}/msg/${session}`)
    const client = Stomp.over(socket)

    client.connect(
      {},
      () => {
        if (_disposed) {
          try { client.disconnect(() => {}) } catch (_) { /* ignore */ }
          return
        }

        _connected = true
        _connecting = false
        stompClient = client

        const store = useMessageStore()
        store.setWsConnected(true)

        // 订阅新消息推送
        client.subscribe('/user/msg/new', (message: Stomp.Message) => {
          try {
            const push = JSON.parse(message.body) as WsMsgPush
            if (push.type === 'NEW_MESSAGE') {
              const payload = push.payload as WsNewMessagePayload
              const store = useMessageStore()
              store.incrementUnread()
              store.handleNewMessage({
                conversationId: payload.conversationId,
                content: payload.content,
                fromUserId: payload.fromUserId,
                createTime: payload.createTime
              })
              // 构建完整 MessageItem 供 ConversationView 实时追加
              store.setLatestWsMessage({
                messageId: payload.messageId,
                conversationId: payload.conversationId,
                fromUserId: payload.fromUserId,
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
              // 已读状态预留处理
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
      }
    )

    socket.onclose = () => {
      if (!_disposed) {
        _connected = false
        _connecting = false
        stompClient = null
        try {
          const store = useMessageStore()
          store.setWsConnected(false)
        } catch (_) { /* ignore */ }
      }
    }
  } catch (e) {
    console.error('[MessageWS] Failed to create connection:', e)
    _connected = false
    _connecting = false
  }
}

/**
 * 断开 STOMP WebSocket 连接
 */
export function disconnectMessageWs() {
  _disposed = true
  if (stompClient) {
    try {
      stompClient.disconnect(() => {
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
