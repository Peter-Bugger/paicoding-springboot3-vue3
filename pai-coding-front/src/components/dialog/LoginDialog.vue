<template>
  <el-dialog v-model="loginModal" :width="'660px'" :show-close="false" :close-on-click-modal="true" class="login-dialog">
    <div class="login-dialog-inner">
      <!-- Left: Login Form -->
      <div class="login-form-section">
        <div class="login-form-header">
          <h3 class="login-form-title">欢迎回来</h3>
          <p class="login-form-subtitle">登录技术派，畅享更多权益</p>
        </div>
        <el-form ref="formRef" :model="dynamicValidateForm" label-position="top" class="login-form">
          <el-form-item prop="username" label="用户名" :rules="[{ required: true, message: '用户名不能为空', trigger: 'blur' }]">
            <el-input v-model="dynamicValidateForm.username" placeholder="请输入用户名" class="login-input" />
          </el-form-item>
          <el-form-item prop="password" label="密码" :rules="[{ required: true, message: '密码不能为空', trigger: 'blur' }]">
            <el-input v-model="dynamicValidateForm.password" type="password" placeholder="请输入密码" class="login-input" show-password />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="submitForm(formRef)" class="login-submit-btn">登 录</el-button>
          </el-form-item>
        </el-form>
        <div class="login-divider">
          <span class="login-divider-text">其他方式</span>
        </div>
        <div class="login-oauth">
          <span class="login-oauth-text">微信扫码登录</span>
        </div>
      </div>

      <!-- Right: QR Code -->
      <div class="login-qr-section">
        <div class="login-qr-card">
          <div class="login-qr-header">微信扫码登录</div>
          <div class="login-qr-code-wrap">
            <img class="login-qr-image" width="150" src="https://xuyifei-oss.oss-cn-beijing.aliyuncs.com/tech-pai/images/%E5%85%AC%E4%BC%97%E5%8F%B7qrcode.jpg" alt="QR Code" />
          </div>
          <div class="login-qr-info">
            <span class="login-qr-code-label">验证码 <strong class="login-qr-code-value">{{ code }}</strong></span>
            <div class="login-qr-status">
              <span class="login-qr-state">{{ state }}</span>
              <a class="login-qr-refresh" @click="refreshCode">刷新</a>
            </div>
          </div>
        </div>
        <p class="login-agreement">
          登录即同意 <a class="login-agreement-link">用户协议</a> 和 <a class="login-agreement-link">隐私政策</a>
        </p>
      </div>
    </div>
  </el-dialog>
</template>

<script setup lang="ts">
import { reactive, ref, watch } from 'vue'
import type { FormInstance } from 'element-plus'
import { doGet, doPost, mockLogin2XML, mockLoginXML } from '@/http/BackendRequests'
import type { CommonResponse, GlobalResponse } from '@/http/ResponseTypes/CommonResponseType'
import { BASE_URL, LOGIN_USER_NAME_URL } from '@/http/URL'
import { getCookie, messageTip, refreshPage, setAuthToken } from '@/util/utils'
import { MESSAGE_TYPE } from '@/constants/MessageTipEnumConstant'
import { COOKIE_DEVICE_ID } from '@/constants/CookieConstants'
import { useGlobalStore } from '@/stores/global'
const globalStore = useGlobalStore()
const global = globalStore.global

const props = defineProps<{
  clicked: boolean,
}>()

const loginModal = ref(false)
let init = false

watch(() => props.clicked, () => {
  loginModal.value = true
  if (!init) {
    buildConnect()
    init = true
  }
})

const formRef = ref<FormInstance>()

const dynamicValidateForm = reactive<{
  username: string,
  password: string
}>({
  username: '',
  password: '',
})

const submitForm = (formEl: FormInstance | undefined) => {
  if (!formEl) return
  formEl.validate((valid) => {
    if (valid) {
      doPost<CommonResponse>(LOGIN_USER_NAME_URL, {
        username: dynamicValidateForm.username,
        password: dynamicValidateForm.password
      })
        .then((response) => {
          if (response.data.status.code === 0) {
            messageTip("登录成功", MESSAGE_TYPE.SUCCESS)
            setAuthToken(response.data.result.token)
            refreshPage()
          }
        })
        .catch((error) => {
          console.error(error)
        })
    } else {
      messageTip("请按要求填写用户名密码", MESSAGE_TYPE.ERROR)
    }
  })
}

const resetForm = (formEl: FormInstance | undefined) => {
  if (!formEl) return
  formEl.resetFields()
}

// ==========模拟登录==========
const mockLogin = () => {
  mockLoginXML<CommonResponse>(code.value)
    .then((response) => {
      messageTip("登录成功", MESSAGE_TYPE.SUCCESS)
      refreshPage()
    })
    .catch((error) => {
      console.error(error)
    })
}

const mockLogin2 = () => {
  mockLogin2XML<CommonResponse>(code.value)
    .then((response) => {
      messageTip("登录成功", MESSAGE_TYPE.SUCCESS)
      refreshPage()
    })
    .catch((error) => {
      console.error(error)
    })
}

// ==========长连接==========
let sseSource: any = null;
let intHook: any = null;
let deviceId: any = null;
const code = ref('')
const state = ref('有效期五分钟 👉')
let fetchCodeCnt = 0

function buildConnect() {
  if (sseSource != null) {
    try { sseSource.close() } catch (e) { console.log("关闭上次的连接", e) }
    try { window.clearInterval(intHook) } catch (e) { /* empty */ }
  }

  if (!deviceId) {
    deviceId = getCookie(COOKIE_DEVICE_ID);
  }
  const subscribeUrl = BASE_URL + "/subscribe?deviceId=" + deviceId;
  const source = new EventSource(subscribeUrl);
  sseSource = source;

  source.onmessage = function (event) {
    let text = event.data.replaceAll("\"", "").trim();
    let newCode;
    if (text.startsWith('refresh#')) {
      newCode = text.substring(8).trim();
      code.value = newCode
      state.value = '已刷新 '
    } else if (text === 'scan') {
      state.value = '已扫描 '
    } else if (text.startsWith('login#')) {
      document.cookie = text.substring(6);
      source.close();
      refreshPage();
    } else if (text.startsWith("init#")) {
      newCode = text.substring(5).trim();
      code.value = newCode
    }

    if (newCode != null) {
      try { window.clearInterval(intHook) } catch (e) { /* empty */ }
    }
  };

  source.onopen = function () {
    deviceId = getCookie("f-device");
  }

  source.onerror = function () {
    state.value = '连接中断,请刷新重连'
    buildConnect();
  };

  fetchCodeCnt = 0;
  intHook = setInterval(() => fetchCode(), 1000);
}

function fetchCode() {
  if (deviceId) {
    if (++fetchCodeCnt > 5) {
      try { window.clearInterval(intHook) } catch (e) { /* empty */ }
      return;
    }

    doGet('/login/fetch?deviceId=' + deviceId, {}, 'text')
      .then((response) => {
        if (response.data && response.data !== 'fail') {
          // @ts-ignore
          code.value = response.data
          try { window.clearInterval(intHook) } catch (e) { /* empty */ }
        }
      })
      .catch((error) => { console.error(error) })
  }
}

function refreshCode() {
  doGet('/login/refresh?deviceId=' + deviceId, {}, 'json')
    .then((response) => {
      // @ts-ignore
      const validationCode = response.data['result']['code']
      // @ts-ignore
      const reconnect = response.data['result']['reconnect']
      if (reconnect) {
        buildConnect()
        state.value = '已刷新'
      } else if (validationCode) {
        if (code.value !== validationCode) {
          code.value = validationCode
          state.value = '已刷新'
        }
      }
    })
    .catch((error) => { console.error(error) })
}
</script>

<style scoped>
.login-dialog :deep(.el-dialog) {
  border-radius: 16px;
  overflow: hidden;
  box-shadow: 0 20px 60px rgba(26, 29, 39, 0.15), 0 4px 16px rgba(26, 29, 39, 0.08);
  transition: all 0.3s ease;
}

.login-dialog :deep(.el-dialog__body) {
  padding: 0;
}

.login-dialog-inner {
  display: flex;
  min-height: 420px;
}

/* Left: Form Section */
.login-form-section {
  flex: 1;
  padding: 2.5rem 2rem;
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.login-form-header {
  margin-bottom: 1.5rem;
}

.login-form-title {
  font-size: 1.4rem;
  font-weight: 700;
  color: var(--pai-color-3-black, #1e2029);
  margin: 0 0 0.3rem;
  font-family: 'JetBrains Mono', monospace;
  letter-spacing: -0.02em;
}

.login-form-subtitle {
  font-size: 0.82rem;
  color: var(--pai-color-3-gray, #6b7084);
  margin: 0;
}

.login-form {
  max-width: 320px;
}

.login-form :deep(.el-form-item__label) {
  font-size: 0.78rem;
  font-weight: 500;
  color: var(--pai-color-4-gray, #484d5e);
  padding-bottom: 4px;
}

.login-input :deep(.el-input__wrapper) {
  border-radius: 10px;
  border: 1.5px solid var(--pai-border-color-1, #d6dae6);
  box-shadow: none;
  padding: 4px 12px;
  transition: border-color 0.2s ease, box-shadow 0.2s ease;
  background: var(--pai-bg-light-1, #f4f6fa);
}

.login-input :deep(.el-input__wrapper):hover {
  border-color: var(--pai-brand-1-normal, #2d7cf6);
}

.login-input :deep(.el-input__wrapper.is-focus) {
  border-color: var(--pai-brand-1-normal, #2d7cf6);
  box-shadow: 0 0 0 3px rgba(45, 124, 246, 0.1);
  background: #fff;
}

.login-input :deep(.el-input__inner) {
  font-size: 0.85rem;
  color: var(--pai-color-3-black, #1e2029);
}

.login-submit-btn {
  width: 100%;
  padding: 0.7rem;
  font-size: 0.9rem;
  font-weight: 600;
  border-radius: 10px;
  border: none;
  background: linear-gradient(135deg, #2d7cf6, #1a5fc7);
  color: #fff;
  transition: all 0.25s ease;
  letter-spacing: 0.06em;
  margin-top: 0.5rem;
}

.login-submit-btn:hover {
  background: linear-gradient(135deg, #4a8ff7, #2d7cf6);
  transform: translateY(-1px);
  box-shadow: 0 4px 14px rgba(45, 124, 246, 0.35);
}

.login-divider {
  display: flex;
  align-items: center;
  margin: 1.2rem 0 0.8rem;
}

.login-divider::before,
.login-divider::after {
  content: '';
  flex: 1;
  height: 1px;
  background: var(--pai-border-color-1, #d6dae6);
}

.login-divider-text {
  padding: 0 0.75rem;
  font-size: 0.72rem;
  color: var(--pai-color-999-gray, #8c8f9c);
  white-space: nowrap;
}

.login-oauth {
  text-align: center;
}

.login-oauth-text {
  font-size: 0.78rem;
  color: var(--pai-brand-1-normal, #2d7cf6);
  cursor: pointer;
  transition: opacity 0.2s;
}

.login-oauth-text:hover {
  opacity: 0.8;
}

/* Right: QR Section */
.login-qr-section {
  width: 240px;
  background: var(--pai-bg-light-1, #f4f6fa);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 2rem 1.5rem;
  gap: 1rem;
}

.login-qr-card {
  text-align: center;
}

.login-qr-header {
  font-size: 0.85rem;
  font-weight: 600;
  color: var(--pai-color-3-black, #1e2029);
  margin-bottom: 1rem;
}

.login-qr-code-wrap {
  background: #fff;
  padding: 0.6rem;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(26, 29, 39, 0.06);
  display: inline-block;
}

.login-qr-image {
  display: block;
  border-radius: 8px;
}

.login-qr-info {
  margin-top: 0.75rem;
}

.login-qr-code-label {
  font-size: 0.75rem;
  color: var(--pai-color-3-gray, #6b7084);
}

.login-qr-code-value {
  color: var(--pai-brand-1-normal, #2d7cf6);
  font-family: 'JetBrains Mono', monospace;
  font-size: 0.9rem;
}

.login-qr-status {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 0.4rem;
  margin-top: 0.3rem;
}

.login-qr-state {
  font-size: 0.7rem;
  color: var(--pai-color-999-gray, #8c8f9c);
}

.login-qr-refresh {
  font-size: 0.7rem;
  color: var(--pai-brand-1-normal, #2d7cf6);
  cursor: pointer;
  text-decoration: none;
}

.login-qr-refresh:hover {
  text-decoration: underline;
}

.login-agreement {
  font-size: 0.65rem;
  color: var(--pai-color-999-gray, #8c8f9c);
  text-align: center;
  margin: 0;
  line-height: 1.5;
}

.login-agreement-link {
  color: var(--pai-brand-1-normal, #2d7cf6);
  cursor: pointer;
  text-decoration: none;
}

.login-agreement-link:hover {
  text-decoration: underline;
}

/* Dialog overlay animation */
.login-dialog :deep(.el-overlay) {
  background: rgba(15, 17, 26, 0.5);
  backdrop-filter: blur(4px);
}

.login-dialog :deep(.el-overlay-dialog) {
  display: flex;
  align-items: center;
  justify-content: center;
}
</style>
