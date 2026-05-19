import { doGet, doPost, doPut, doDelete } from '@/http/BackendRequests'
import type { CommonResponse } from '@/http/ResponseTypes/CommonResponseType'
import type {
  SendMsgReq,
  SendMsgRes,
  StartConvReq,
  StartConvRes,
  ConversationItem,
  MessageItem,
  MessagePageListVo,
  UnreadCountRes,
  PageListVo
} from '@/http/ResponseTypes/MsgTypes'
import {
  MSG_SEND_URL,
  MSG_START_URL,
  MSG_CONVERSATIONS_URL,
  MSG_MESSAGES_URL,
  MSG_READ_URL,
  MSG_UNREAD_COUNT_URL,
  MSG_CLEAR_CONVERSATION_URL
} from '@/http/URL'

/**
 * 发送私信
 */
export function sendMessage(data: SendMsgReq) {
  return doPost<CommonResponse<SendMsgRes>>(MSG_SEND_URL, data as unknown as Record<string, unknown>)
}

/**
 * 创建或获取一对一会话
 */
export function startConversation(data: StartConvReq) {
  return doPost<CommonResponse<StartConvRes>>(MSG_START_URL, data as unknown as Record<string, unknown>)
}

/**
 * 获取会话列表（分页）
 */
export function fetchConversations(page: number = 1, pageSize: number = 20) {
  return doGet<CommonResponse<PageListVo<ConversationItem>>>(MSG_CONVERSATIONS_URL, { page, pageSize })
}

/**
 * 获取会话历史消息（分页）
 * @param signal 用于取消请求的 AbortSignal，快速切换会话时中断旧请求
 */
export function fetchMessages(conversationId: number, page: number = 1, pageSize: number = 20, signal?: AbortSignal) {
  return doGet<CommonResponse<MessagePageListVo>>(MSG_MESSAGES_URL + `/${conversationId}`, { page, pageSize }, undefined, signal)
}

/**
 * 标记会话为已读
 */
export function markConversationRead(conversationId: number) {
  return doPut<CommonResponse<Record<string, boolean>>>(MSG_READ_URL + `/${conversationId}`, {})
}

/**
 * 获取未读消息总数
 */
export function fetchUnreadCount() {
  return doGet<CommonResponse<UnreadCountRes>>(MSG_UNREAD_COUNT_URL, {})
}

/**
 * 清除会话(当前用户侧)
 */
export function clearConversation(conversationId: number) {
  return doDelete<CommonResponse<Record<string, boolean>>>(MSG_CLEAR_CONVERSATION_URL + `/${conversationId}`, {})
}
