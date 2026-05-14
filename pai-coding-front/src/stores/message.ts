import { defineStore } from 'pinia'
import { MESSAGE_STORE } from '@/constants/StoreConstants'
import type { ConversationItem, MessageItem } from '@/http/ResponseTypes/MsgTypes'

export const useMessageStore = defineStore(MESSAGE_STORE, {
  state: () => ({
    /** 私信未读总数 */
    unreadTotal: 0,
    /** 会话列表 */
    conversations: [] as ConversationItem[],
    /** 当前打开的会话ID */
    currentConversationId: 0,
    /** WebSocket 是否已连接 */
    wsConnected: false,
    /**
     * 发起会话时的目标用户ID。
     * 用于 ConversationView 在 store 未加载会话列表时仍能获取 toUserId 发送消息。
     */
    pendingTargetUserId: 0,
    /**
     * WebSocket 推送的最新消息（用于 ConversationView 实时追加）
     */
    latestWsMessage: null as MessageItem | null,
    /** 是否有新会话需要刷新列表（WebSocket 收到陌生会话消息时标记） */
    _needsRefresh: false
  }),

  actions: {
    /** 设置未读总数 */
    setUnreadTotal(count: number) {
      this.unreadTotal = count
      this.updatePageTitle()
    },

    /** 设置当前打开的会话ID */
    setCurrentConversationId(id: number) {
      this.currentConversationId = id
    },

    /** 递增未读数 */
    incrementUnread() {
      this.unreadTotal++
      this.updatePageTitle()
    },

    /** 重置未读数（进入会话页后） */
    resetUnread(conversationId: number) {
      const conv = this.conversations.find(c => c.conversationId === conversationId)
      if (conv) {
        conv.unreadCount = 0
      }
      this.updatePageTitle()
    },

    /** 设置会话列表 */
    setConversations(list: ConversationItem[]) {
      this.conversations = list
    },

    /** 追加更多会话（分页加载） */
    appendConversations(list: ConversationItem[]) {
      this.conversations.push(...list)
    },

    /** WebSocket 新消息处理：更新对应会话项，不存在则标记需要刷新 */
    handleNewMessage(payload: {
      conversationId: number
      content: string
      fromUserId: number
      createTime: string
    }) {
      const conv = this.conversations.find(c => c.conversationId === payload.conversationId)
      if (conv) {
        conv.lastMessage.content = payload.content
        conv.lastMessage.createTime = payload.createTime
        conv.lastMessage.fromUserId = payload.fromUserId
        // 当前正在查看的会话不增加未读数
        if (payload.conversationId !== this.currentConversationId) {
          conv.unreadCount++
        }
        const index = this.conversations.indexOf(conv)
        if (index > 0) {
          this.conversations.splice(index, 1)
          this.conversations.unshift(conv)
        }
      } else {
        // 新会话（首次收到对方消息），标记需要刷新列表
        this._needsRefresh = true
      }
    },

    /** 获取并清除刷新标记 */
    consumeRefreshFlag(): boolean {
      const flag = this._needsRefresh
      this._needsRefresh = false
      return flag
    },

    /** 设置 WebSocket 连接状态 */
    setWsConnected(connected: boolean) {
      this.wsConnected = connected
    },

    /** 设置 WebSocket 推送的最新消息（供 ConversationView 消费） */
    setLatestWsMessage(msg: MessageItem | null) {
      this.latestWsMessage = msg
    },

    /** 设置发起会话时的目标用户ID */
    setPendingTargetUserId(userId: number) {
      this.pendingTargetUserId = userId
    },

    /** 从会话列表中移除指定会话 */
    removeConversation(id: number) {
      this.conversations = this.conversations.filter(c => c.conversationId !== id)
      this.updatePageTitle()
    },

    /** 更新页面标题未读徽标 */
    updatePageTitle() {
      if (this.unreadTotal > 0) {
        document.title = `(${this.unreadTotal}) 派编程`
      } else {
        document.title = '派编程'
      }
    }
  }
})
