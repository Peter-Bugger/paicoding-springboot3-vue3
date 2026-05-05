<template>
  <HeaderBar></HeaderBar>
  <div class="chat-page">
    <div class="chat-container">
      <div class="chat-main">
        <div class="chat-header">
          <div class="chat-header-info">
            <div class="chat-header-title">
              <div v-if="!global.isLogin || !global.user" class="chat-header-login-prompt">
                点击登录，体验编程汇智能对话
              </div>
              <div class="chat-header-user" v-else>
                <span class="chat-header-username">{{global.user.userName}}</span>
                <span class="chat-header-status">试用中</span>
              </div>
            </div>
            <div class="chat-header-sub">
              与派聪明的 <span class="chat-header-cnt">{{chatUsedCnt}}/{{chatMaxCnt}}</span> 条对话
              <span class="chat-header-hint">（以天为单位，无限期重置）</span>
            </div>
          </div>
          <div class="chat-header-model">
            <el-select
              class="chat-model-select"
              @change="chatTypeChange"
              v-model="chatType"
              placeholder="选择对话模型"
            >
              <el-option :value="AiTypeEnum.XUN_FEI_AI" label="讯飞星火" />
            </el-select>
          </div>
        </div>

        <div class="chat-messages" ref="chatContent" id="chat-content">
          <div class="chat-msg" v-for="(msg, id) in msgRecords[chatType]" :key="id">
            <div v-if="msg.msgType == 'question'" class="chat-msg-row chat-msg-row--user">
              <div class="chat-msg-bubble chat-msg-bubble--user">{{msg.question}}</div>
              <el-avatar :size="35" :src="global.user.photo" class="chat-msg-avatar"></el-avatar>
            </div>
            <div v-if="msg.msgType == 'answer'" class="chat-msg-row chat-msg-row--ai">
              <el-avatar :size="35" class="chat-msg-avatar" src="https://xuyifei-oss.oss-cn-beijing.aliyuncs.com/tech-pai/images/avatar/llm-avatar1.png"></el-avatar>
              <div class="chat-msg-bubble chat-msg-bubble--ai"><MdPreview :model-value="msg.answer" /></div>
            </div>
            <div v-if="msg.msgType == 'history'" class="chat-msg-divider">
              <span class="chat-msg-divider-text">历史消息</span>
            </div>
          </div>
          <div v-if="aiLoading" class="chat-msg-row chat-msg-row--ai">
            <el-avatar :size="35" class="chat-msg-avatar" src="https://xuyifei-oss.oss-cn-beijing.aliyuncs.com/tech-pai/images/avatar/llm-avatar1.png"></el-avatar>
            <div class="chat-msg-bubble chat-msg-bubble--ai">
              <el-icon :size="20" class="is-loading"><Loading /></el-icon>
            </div>
          </div>
        </div>

        <div class="chat-input-area" id="chat-textarea">
          <textarea
            v-model="chatText"
            id="input-field"
            class="chat-input-field"
            rows="3"
            :placeholder="!global.isLogin || !global.user.userId || chatTextAreaDisabled ? '你好，快登录和我对线吧' : '可按回车发送'"
            :disabled="!global.isLogin || !global.user.userId || chatTextAreaDisabled"
          ></textarea>
          <button
            @click="sendMsg"
            id="send-btn"
            class="chat-send-btn"
            :disabled="!global.isLogin || !global.user.userId || chatBtnDisabled"
          >
            <svg class="chat-send-icon" viewBox="0 0 16 16" fill="none">
              <path d="M1.333 4.71 6.67 6l1.67 6.67L12.67 0 1.333 4.71Z" fill="currentColor" />
              <path d="M8.003 6.117 10 8" stroke="currentColor" stroke-width="1.333" />
            </svg>
            <span>{{ !global.isLogin || !global.user.userId ? '等待登录' : '发送' }}</span>
          </button>
        </div>
      </div>
    </div>
    <Footer></Footer>
  </div>
  <LoginDialog :clicked="loginDialogClicked"></LoginDialog>
</template>

<script setup lang="ts">
import HeaderBar from '@/components/layout/HeaderBar.vue'
import Footer from '@/components/layout/Footer.vue'
import LoginDialog from '@/components/dialog/LoginDialog.vue'
import { useGlobalStore } from '@/stores/global'
import { nextTick, onMounted, provide, ref } from 'vue'
import { doGet } from '@/http/BackendRequests'
import type { CommonResponse } from '@/http/ResponseTypes/CommonResponseType'
import { GLOBAL_INFO_URL, WS_URL } from '@/http/URL'
import { getCookie, messageTip } from '@/util/utils'
import Stomp from 'stompjs'
import { Loading } from '@element-plus/icons-vue'
import { MdPreview } from 'md-editor-v3'
import type { WebSocketRecordsType } from '@/http/ResponseTypes/ChatType/WebSocketRecordsType'
import type { WebSocketResponseType } from '@/http/ResponseTypes/WebSocketResponseType'
import { type AiTypeConstants, AiTypeEnum } from '@/constants/AiTypeEnumConstants'

const globalStore = useGlobalStore()
const global = globalStore.global

const chatUsedCnt = ref(0)
const chatMaxCnt = ref(0)
const chatText = ref('')
const chatBtnDisabled = ref(true)
const chatTextAreaDisabled = ref(true)
const chatContent = ref<HTMLElement | null>(null)
const aiLoading = ref(false)

const session = getCookie('f-session')
const chatType = ref<AiTypeConstants>('XUN_FEI_AI')
let stompClient: Stomp.Client | null = null

const msgRecords = ref<Record<AiTypeConstants, WebSocketRecordsType[]>>({
  XUN_FEI_AI: [],
  CHAT_GPT_3_5: [],
  PAI_AI: []
})

const chatTypeChange = () => {
  if (global.isLogin) {
    disconnect()
    initWs()
  }
}

const initWs = () => {
  msgRecords.value[chatType.value] = []
  const aiType = chatType.value
  const socket = new WebSocket(`${WS_URL}/gpt/${session}/${aiType}`)
  stompClient = Stomp.over(socket)
  stompClient.connect({}, () => {
    chatBtnDisabled.value = false
    chatTextAreaDisabled.value = false
    chatText.value = ''

    stompClient?.subscribe('/user/chat/rsp', (message: Stomp.Message) => {
      const res = JSON.parse(message.body)
      chatUsedCnt.value = res.usedCnt
      chatMaxCnt.value = res.maxCnt
      const data: WebSocketResponseType[] = res.records
      if (data.length > 1) {
        for (let i = data.length - 1; i >= 0; i--) {
          addClientMsg(data[i])
          if (i === 0) {
            msgRecords.value[chatType.value].push({ msgType: 'history' })
          }
          appendServerMessage(data[i])
        }
        scrollToBottom()
      } else {
        appendServerMessage(data[0])
      }
      if (data[data.length - 1]?.answerType !== 'STREAM') {
        chatBtnDisabled.value = false
      }
    })
  })
  socket.onclose = disconnect
}

const disconnect = () => {
  if (stompClient !== null) {
    stompClient.disconnect(() => {})
  }
  stompClient = null
  chatTextAreaDisabled.value = true
  chatBtnDisabled.value = false
}

const appendServerMessage = (answer: WebSocketResponseType) => {
  const content = answer.answer
  const answerType = answer.answerType
  const chatId = answer.chatUid
  let appendLastChat = false
  aiLoading.value = false

  if ('JSON' === answerType) {
    const parsed = JSON.parse(content)
    if (parsed.length === 1) {
      msgRecords.value[chatType.value].push({
        msgType: 'answer',
        answer: parsed[0].message.content,
        answerTime: answer.answerTime,
        chatUid: chatId
      })
      appendLastChat = true
    }
  } else if ('STREAM' === answerType || 'STREAM_END' === answerType) {
    const lastIndex = msgRecords.value[chatType.value].findLastIndex(
      (msg) => msg.msgType === 'answer' && msg.chatUid === chatId
    )
    if (lastIndex !== -1) {
      msgRecords.value[chatType.value][lastIndex].answer = content
      appendLastChat = true
    }
  }

  if (!appendLastChat) {
    msgRecords.value[chatType.value].push({
      msgType: 'answer',
      answer: content,
      answerTime: answer.answerTime,
      chatUid: chatId
    })
  }
  scrollToBottom()
}

const addClientMsg = (data: WebSocketResponseType) => {
  msgRecords.value[chatType.value].push({
    msgType: 'question',
    question: data.question,
    questionTime: data.questionTime
  })
  scrollToBottom()
}

const scrollToBottom = () => {
  nextTick(() => {
    if (chatContent.value) {
      chatContent.value.scrollTo({
        top: chatContent.value.scrollHeight,
        behavior: 'smooth'
      })
    }
  })
}

const doSend = () => {
  const qa = chatText.value
  if (qa.length > 512) {
    messageTip('提问长度请不要超过512字符哦~', 'info')
    return
  }
  stompClient?.send('/app/chat/' + session, { 's-uid': session }, qa)
  chatText.value = ''
  msgRecords.value[chatType.value].push({ msgType: 'question', question: qa })
  aiLoading.value = true
  chatBtnDisabled.value = true
}

const sendMsg = () => {
  if (stompClient == null) {
    initWs()
  } else {
    if (chatText.value === '') {
      messageTip('请输入内容', 'info')
    } else {
      doSend()
    }
  }
}

onMounted(async () => {
  await doGet<CommonResponse>(GLOBAL_INFO_URL, {})
    .then((res) => {
      globalStore.setGlobal(res.data.global)
    })
  if (global.isLogin) {
    initWs()
  } else {
    messageTip('请先登录', 'info')
  }
})

const changeClicked = () => {
  loginDialogClicked.value = !loginDialogClicked.value
}

provide('loginDialogClicked', changeClicked)
const loginDialogClicked = ref(false)
</script>

<style scoped>
.chat-page {
  background: var(--pai-bg-light-1, #f4f6fa);
  min-height: calc(100vh - var(--header-height, 60px));
  padding-top: calc(var(--header-height, 60px));
  display: flex;
  flex-direction: column;
}

.chat-container {
  flex: 1;
  max-width: 900px;
  margin: 0 auto;
  padding: 1.25rem;
  width: 100%;
  display: flex;
  flex-direction: column;
}

.chat-main {
  flex: 1;
  display: flex;
  flex-direction: column;
  background: var(--pai-bg-white-fff, #ffffff);
  border-radius: 16px;
  box-shadow: 0 1px 3px rgba(26, 29, 39, 0.04);
  overflow: hidden;
}

.chat-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  padding: 1.25rem 1.5rem;
  border-bottom: 1px solid var(--pai-bg-light-2, #eef1f7);
}

.chat-header-info {
  flex: 1;
}

.chat-header-login-prompt {
  font-size: 1rem;
  font-weight: 600;
  color: var(--pai-color-4-gray, #484d5e);
}

.chat-header-user {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.chat-header-username {
  font-size: 1rem;
  font-weight: 700;
  color: var(--pai-color-3-black, #1e2029);
}

.chat-header-status {
  font-size: 0.72rem;
  color: var(--pai-color-999-gray, #8c8f9c);
  background: var(--pai-bg-light-2, #eef1f7);
  padding: 0.15rem 0.5rem;
  border-radius: 4px;
}

.chat-header-sub {
  font-size: 0.8rem;
  color: var(--pai-color-999-gray, #8c8f9c);
  margin-top: 0.3rem;
}

.chat-header-cnt {
  font-family: 'JetBrains Mono', monospace;
  font-weight: 700;
  color: var(--pai-brand-1-normal);
}

.chat-header-hint {
  font-size: 0.72rem;
  color: var(--pai-color-5-gray, #d0d3dd);
}

.chat-model-select {
  width: 140px;
}

.chat-messages {
  flex: 1;
  overflow-y: auto;
  padding: 1rem 1.5rem;
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.chat-msg-row {
  display: flex;
  gap: 0.6rem;
  align-items: flex-start;
}

.chat-msg-row--user {
  justify-content: flex-end;
}

.chat-msg-row--ai {
  justify-content: flex-start;
}

.chat-msg-avatar {
  flex-shrink: 0;
}

.chat-msg-bubble {
  max-width: 70%;
  padding: 0.6rem 1rem;
  border-radius: 12px;
  font-size: 0.88rem;
  line-height: 1.5;
}

.chat-msg-bubble--user {
  background: var(--pai-brand-7-light, rgba(45, 124, 246, 0.12));
  color: var(--pai-color-3-black, #1e2029);
  border-bottom-right-radius: 4px;
}

.chat-msg-bubble--ai {
  background: var(--pai-bg-light-1, #f4f6fa);
  color: var(--pai-color-3-black, #1e2029);
  border-bottom-left-radius: 4px;
}

.chat-msg-divider {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  padding: 0.5rem 0;
}

.chat-msg-divider::before,
.chat-msg-divider::after {
  content: '';
  flex: 1;
  height: 1px;
  background: var(--pai-bg-light-2, #eef1f7);
}

.chat-msg-divider-text {
  font-size: 0.75rem;
  color: var(--pai-color-5-gray, #d0d3dd);
  white-space: nowrap;
}

.chat-input-area {
  display: flex;
  gap: 0.75rem;
  padding: 1rem 1.5rem 1.25rem;
  border-top: 1px solid var(--pai-bg-light-2, #eef1f7);
  align-items: flex-end;
}

.chat-input-field {
  flex: 1;
  padding: 0.6rem 0.9rem;
  border: 1.5px solid var(--pai-border-color-1, #d6dae6);
  border-radius: 10px;
  font-size: 0.88rem;
  font-family: inherit;
  resize: none;
  outline: none;
  transition: border-color 0.2s, box-shadow 0.2s;
  background: var(--pai-bg-light-1, #f4f6fa);
  color: var(--pai-color-3-black, #1e2029);
}

.chat-input-field:focus {
  border-color: var(--pai-brand-1-normal);
  box-shadow: 0 0 0 3px rgba(45, 124, 246, 0.1);
}

.chat-input-field::placeholder {
  color: var(--pai-color-5-gray, #d0d3dd);
}

.chat-send-btn {
  display: inline-flex;
  align-items: center;
  gap: 0.4rem;
  padding: 0.6rem 1.2rem;
  background: var(--pai-brand-1-normal);
  color: #fff;
  border: none;
  border-radius: 10px;
  font-size: 0.85rem;
  font-weight: 600;
  cursor: pointer;
  transition: background 0.2s, opacity 0.2s;
  white-space: nowrap;
}

.chat-send-btn:hover:not(:disabled) {
  background: var(--pai-brand-2-hover, #4a8ff7);
}

.chat-send-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.chat-send-icon {
  width: 16px;
  height: 16px;
}

@media (max-width: 768px) {
  .chat-container {
    padding: 0.75rem;
  }
  .chat-header {
    flex-direction: column;
    gap: 0.75rem;
  }
  .chat-msg-bubble {
    max-width: 85%;
  }
}
</style>
