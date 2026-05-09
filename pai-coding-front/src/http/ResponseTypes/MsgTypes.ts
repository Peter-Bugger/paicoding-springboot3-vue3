// 会话列表中的目标用户信息
export interface SimpleUserInfo {
  userId: number
  userName: string
  photo: string
}

// 会话中的最后一条消息摘要
export interface LastMessage {
  content: string
  createTime: string
  status: string
  fromUserId: number
}

// 会话列表单项
export interface ConversationItem {
  conversationId: number
  conversationType: string
  targetUser: SimpleUserInfo
  lastMessage: LastMessage
  unreadCount: number
  isTop: number
  createTime: string
}

// 历史消息
export interface MessageItem {
  messageId: number
  conversationId: number
  fromUserId: number
  fromUserName: string
  fromUserPhoto: string
  messageType: string
  content: string
  referencedMsgId: number | null
  attachment: unknown | null
  status: string
  createTime: string
}

// WebSocket 新消息推送载荷
export interface WsNewMessagePayload {
  conversationId: number
  messageId: number
  fromUserId: number
  fromUserName: string
  fromUserPhoto: string
  content: string
  messageType: string
  createTime: string
}

// WebSocket 已读状态推送载荷
export interface WsReadStatusPayload {
  conversationId: number
  messageId: number
  readByUserId: number
  readTime: string
}

// WebSocket 通用推送消息体
export interface WsMsgPush {
  type: 'NEW_MESSAGE' | 'READ_STATUS'
  payload: WsNewMessagePayload | WsReadStatusPayload
}

// 发送消息请求
export interface SendMsgReq {
  toUserId: number
  content: string
}

// 发送消息响应
export interface SendMsgRes {
  conversationId: number
  messageId: number
}

// 发起会话请求
export interface StartConvReq {
  toUserId: number
}

// 发起会话响应
export interface StartConvRes {
  conversationId: number
}

// 未读总数响应
export interface UnreadCountRes {
  totalUnread: number
}

// 分页响应包装
export interface PageListVo<T> {
  list: T[]
  hasMore: boolean
}
