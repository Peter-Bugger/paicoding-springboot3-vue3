<template>
  <div class="user-profile">
    <div class="user-profile-cover">
      <div class="user-profile-content">
        <img :src="vo.userHome.photo" class="user-profile-avatar" />
        <div class="user-profile-info">
          <h1 class="user-profile-name">{{vo.userHome.userName}}</h1>
          <div class="user-profile-stats">
            <div class="user-profile-stat">
              <span class="user-profile-stat-num">{{vo.userHome.joinDayCount}}</span>
              <span class="user-profile-stat-label">加入天数</span>
            </div>
            <div class="user-profile-stat-divider"></div>
            <div class="user-profile-stat">
              <span class="user-profile-stat-num">{{vo.userHome.followCount}}</span>
              <span class="user-profile-stat-label">关注</span>
            </div>
            <div class="user-profile-stat-divider"></div>
            <div class="user-profile-stat">
              <span class="user-profile-stat-num">{{vo.userHome.fansCount}}</span>
              <span class="user-profile-stat-label">粉丝</span>
            </div>
          </div>
        </div>
      </div>
    </div>

    <div class="user-profile-meta">
      <div class="user-profile-tags">
        <span class="user-profile-tag" v-if="vo.userHome.company">
          <svg class="user-profile-tag-icon" viewBox="0 0 20 20" fill="currentColor"><path d="M2 3a1 1 0 0 1 1-1h2.153a1 1 0 0 1 .986.836l.74 4.435a1 1 0 0 1-.54 1.06l-1.548.773a11.037 11.037 0 0 0 6.105 6.105l.773-1.548a1 1 0 0 1 1.06-.54l4.435.74a1 1 0 0 1 .836.986V17a1 1 0 0 1-1 1h-2C7.82 18 2 12.18 2 5V3Z"/></svg>
          {{vo.userHome.company}}
        </span>
        <span class="user-profile-tag" v-if="vo.userHome.position">
          <svg class="user-profile-tag-icon" viewBox="0 0 20 20" fill="currentColor"><path fill-rule="evenodd" d="M6 6V5a3 3 0 0 1 3-3h2a3 3 0 0 1 3 3v1h2a2 2 0 0 1 2 2v3.57A22.952 22.952 0 0 1 10 13a22.95 22.95 0 0 1-8-1.43V8a2 2 0 0 1 2-2h2Zm2-1a1 1 0 0 1 1-1h2a1 1 0 0 1 1 1v1H8V5Zm1 5a1 1 0 0 1 1-1h.01a1 1 0 0 1 1 1v.01a1 1 0 0 1-1 1H10a1 1 0 0 1-1-1V10Zm2 0a1 1 0 0 1 1-1h.01a1 1 0 0 1 1 1v.01a1 1 0 0 1-1 1H12a1 1 0 0 1-1-1V10Z" clip-rule="evenodd"/></svg>
          {{vo.userHome.position}}
        </span>
        <span class="user-profile-tag" v-if="vo.userHome.region">
          <svg class="user-profile-tag-icon" viewBox="0 0 20 20" fill="currentColor"><path fill-rule="evenodd" d="M9.69 18.933l.003.001C9.89 19.02 10 19 10 19s.11.02.308-.066l.002-.001.006-.003.018-.008a5.741 5.741 0 0 0 .281-.14c.186-.096.446-.24.757-.433.62-.384 1.445-.966 2.274-1.765C15.302 14.988 17 12.493 17 9A7 7 0 1 0 3 9c0 3.492 1.698 5.988 3.355 7.584a13.731 13.731 0 0 0 2.273 1.765 11.842 11.842 0 0 0 .976.544l.062.029.018.008.006.003ZM10 11.25a2.25 2.25 0 1 0 0-4.5 2.25 2.25 0 0 0 0 4.5Z" clip-rule="evenodd"/></svg>
          IP属地：{{vo.userHome.region}}
        </span>
      </div>

      <div class="user-profile-bio" v-if="global.isLogin && global.user.id == vo.userHome.userId">
        <div class="user-profile-bio-text" @click="editInfoDialogVisible = true">
          <span>{{vo.userHome.profile || '点击添加简介，让大家认识你吧'}}</span>
          <el-icon class="user-profile-bio-edit"><Edit /></el-icon>
        </div>
        <div class="user-profile-bio-meta">
          <span>个人资料完善度：{{vo.userHome.infoPercent}}%</span>
          <span class="user-profile-bio-edit-btn" @click="editInfoDialogVisible = true">去编辑 ›</span>
        </div>
      </div>

      <div class="user-profile-actions" v-if="global.isLogin && !isOwnProfile">
        <el-button type="primary" size="small" @click="sendPrivateMsg">
          发私信
        </el-button>
      </div>
    </div>

    <el-dialog
      :model-value="editInfoDialogVisible"
      @close="editInfoDialogVisible = false"
      width="560px"
      class="user-edit-dialog"
    >
      <template #header>
        <span class="user-edit-dialog-title">编辑个人资料</span>
      </template>

      <template #default>
        <div class="user-edit-dialog-body">
          <div class="user-edit-dialog-form">
            <el-form
              ref="userInfoFormRef"
              :model="userInfoForm"
              :size="userInfoFormSize"
              label-width="auto"
              status-icon
              :rules="userInfoFormRules"
            >
              <el-form-item prop="userName" label="用户名">
                <el-input v-model="userInfoForm.userName" maxlength="40" placeholder="用户名" />
              </el-form-item>
              <el-form-item prop="company" label="公司">
                <el-input v-model="userInfoForm.company" maxlength="40" placeholder="所属公司" />
              </el-form-item>
              <el-form-item prop="position" label="职位">
                <el-input v-model="userInfoForm.position" maxlength="40" placeholder="职位" />
              </el-form-item>
              <el-form-item prop="description" label="简介">
                <el-input v-model="userInfoForm.description" maxlength="40" placeholder="自我简介" />
              </el-form-item>
            </el-form>
          </div>
          <div class="user-edit-dialog-avatar">
            <el-avatar size="large" class="user-edit-avatar-preview" :src="cover" @click="uploadAvatar" />
            <span class="user-edit-avatar-label">我的头像</span>
            <span class="user-edit-avatar-hint">支持 jpg、png、jpeg<br>格式大小 2M 以内的图片</span>
          </div>
        </div>
        <input type="file" ref="fileInput" @change="handleFileUpload" style="display: none;">
      </template>

      <template #footer>
        <div class="user-edit-dialog-footer">
          <el-button @click="editInfoDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="saveUserInfo" :disabled="isSaveDisabled">保存</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import type { UserHomeInfoResponseType } from '@/http/ResponseTypes/UserHomeInfoResponseType'
import { useGlobalStore } from '@/stores/global'
import { Edit } from '@element-plus/icons-vue'
import { computed, reactive, ref, watch } from 'vue'
import type { ComponentSize, FormInstance, FormRules } from 'element-plus'
import { doFilePost, doPost } from '@/http/BackendRequests'
import type { CommonResponse } from '@/http/ResponseTypes/CommonResponseType'
import { FILE_UPLOAD_URL, USER_INFO_SAVE_URL } from '@/http/URL'
import { messageTip } from '@/util/utils'
import { useRoute, useRouter } from 'vue-router'
import { startConversation } from '@/http/MessageRequests'
import { useMessageStore } from '@/stores/message'

const globalStore = useGlobalStore()
const global = globalStore.global
const messageStore = useMessageStore()
const route = useRoute()
const router = useRouter()

const props = defineProps<{
  vo: UserHomeInfoResponseType
}>()

const userId = route.params.userId
const isOwnProfile = computed(() => global.isLogin && global.user && String(global.user.userId) === String(userId))

const editInfoDialogVisible = ref(false)

interface UserInfoForm {
  userName: string
  company: string
  position: string
  description: string
}

const userInfoFormSize = ref<ComponentSize>('default')
const userInfoFormRef = ref<FormInstance>()
const userInfoForm = reactive<UserInfoForm>({
  userName: '',
  company: '',
  position: '',
  description: ''
})

const userInfoFormRules = reactive<FormRules<UserInfoForm>>({
  userName: [
    { required: true, message: '请输入昵称', trigger: 'blur' },
    { min: 3, max: 15, message: '昵称长度位于3-15之间', trigger: 'blur' },
  ],
  company: [
    { required: true, message: '请输入公司', trigger: 'blur' },
    { min: 2, max: 15, message: '公司长度位于2-15之间', trigger: 'blur' },
  ],
  position: [
    { required: true, message: '请输入自身职位', trigger: 'blur' },
    { min: 3, max: 10, message: '职位长度位于3-10之间', trigger: 'blur' },
  ],
  description: [
    { required: true, message: '请输入自我简介', trigger: 'blur' },
    { min: 2, max: 20, message: '自我介绍长度位于4-20之间', trigger: 'blur' },
  ]
})

const cover = ref('')

watch(() => props.vo.userHome, (newVal) => {
  if (newVal) {
    if (newVal.photo != null) cover.value = newVal.photo
    userInfoForm.userName = newVal.userName || ''
    userInfoForm.company = newVal.company || ''
    userInfoForm.position = newVal.position || ''
    userInfoForm.description = newVal.profile || ''
  }
}, { immediate: true })

const fileInput = ref<HTMLInputElement | null>(null)

const onUploadFile = (file: File) => {
  const formData = new FormData()
  formData.append('image', file)

  doFilePost<CommonResponse>(FILE_UPLOAD_URL, formData)
    .then((response) => {
      messageTip('上传成功', 'success')
      cover.value = response.data.result.imagePath
    })
    .catch((error) => {
      console.error(error)
      messageTip('上传失败', 'error')
    })
}

const uploadAvatar = () => {
  fileInput.value?.click()
}

const handleFileUpload = (event: Event) => {
  const target = event.target as HTMLInputElement
  const file = target.files ? target.files[0] : null
  if (file) onUploadFile(file)
}

const isSaveDisabled = ref(false)

const saveUserInfo = async () => {
  isSaveDisabled.value = true
  if (!userInfoFormRef.value) return
  await userInfoFormRef.value.validate((valid) => {
    if (valid) {
      doPost<CommonResponse>(USER_INFO_SAVE_URL, {
        userId: userId,
        userName: userInfoForm.userName,
        company: userInfoForm.company,
        position: userInfoForm.position,
        profile: userInfoForm.description,
        photo: cover.value
      })
        .then(() => {
          messageTip('保存成功', 'success')
          editInfoDialogVisible.value = false
          window.location.reload()
        })
        .catch((error) => {
          console.error(error)
          messageTip('保存失败', 'error')
        })
        .finally(() => { isSaveDisabled.value = false })
    } else {
      isSaveDisabled.value = false
    }
  })
}

const sendPrivateMsg = async () => {
  if (!global.isLogin) {
    messageTip('请先登录', 'warning')
    return
  }
  const targetUserId = Number(userId)
  if (!targetUserId) {
    messageTip('用户ID无效', 'warning')
    return
  }
  try {
    const res = await startConversation({ toUserId: targetUserId })
    const conversationId = res.data.result.conversationId
    messageStore.setPendingTargetUserId(targetUserId)
    router.push(`/messages/${conversationId}`)
  } catch (e) {
    console.error('Failed to start conversation:', e)
    messageTip('发起私信失败', 'error')
  }
}
</script>

<style scoped>
.user-profile {
  background: var(--pai-bg-white-fff, #ffffff);
  border-radius: 0 0 20px 20px;
  box-shadow: 0 1px 3px rgba(26, 29, 39, 0.04);
  overflow: hidden;
}

.user-profile-cover {
  position: relative;
  height: 200px;
  background: linear-gradient(135deg, var(--pai-bg-dark-1, #1a1d27) 0%, var(--pai-brand-5-bak, #1a5fc7) 100%);
  display: flex;
  align-items: flex-end;
}

.user-profile-content {
  display: flex;
  align-items: flex-end;
  gap: 1.5rem;
  padding: 0 2rem 1.5rem;
  width: 100%;
  max-width: 1200px;
  margin: 0 auto;
}

.user-profile-avatar {
  width: 96px;
  height: 96px;
  border-radius: 50%;
  border: 4px solid #ffffff;
  object-fit: cover;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.2);
  flex-shrink: 0;
}

.user-profile-info {
  flex: 1;
  padding-bottom: 0.25rem;
}

.user-profile-name {
  font-size: 1.5rem;
  font-weight: 800;
  color: #ffffff;
  margin: 0 0 0.75rem;
  letter-spacing: -0.01em;
}

.user-profile-stats {
  display: flex;
  align-items: center;
  gap: 1rem;
}

.user-profile-stat {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 0.1rem;
}

.user-profile-stat-num {
  font-family: 'JetBrains Mono', monospace;
  font-size: 1.1rem;
  font-weight: 700;
  color: #ffffff;
}

.user-profile-stat-label {
  font-size: 0.72rem;
  color: rgba(255, 255, 255, 0.7);
}

.user-profile-stat-divider {
  width: 1px;
  height: 28px;
  background: rgba(255, 255, 255, 0.2);
}

.user-profile-meta {
  padding: 1rem 2rem 1.25rem;
  max-width: 1200px;
  margin: 0 auto;
  width: 100%;
}

.user-profile-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 0.5rem;
  margin-bottom: 0.75rem;
}

.user-profile-tag {
  display: inline-flex;
  align-items: center;
  gap: 0.3rem;
  font-size: 0.8rem;
  color: var(--pai-color-4-gray, #484d5e);
  background: var(--pai-bg-light-1, #f4f6fa);
  padding: 0.3rem 0.7rem;
  border-radius: 6px;
}

.user-profile-tag-icon {
  width: 14px;
  height: 14px;
  color: var(--pai-color-999-gray, #8c8f9c);
  flex-shrink: 0;
}

.user-profile-bio {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 1rem;
  flex-wrap: wrap;
}

.user-profile-bio-text {
  display: inline-flex;
  align-items: center;
  gap: 0.4rem;
  font-size: 0.88rem;
  color: var(--pai-color-3-gray, #6b7084);
  cursor: pointer;
  padding: 0.35rem 0.75rem;
  border-radius: 8px;
  transition: all 0.2s ease;
}

.user-profile-bio-text:hover {
  background: var(--pai-brand-7-light, rgba(45, 124, 246, 0.12));
  color: var(--pai-brand-1-normal);
}

.user-profile-bio-edit {
  font-size: 0.9rem;
}

.user-profile-bio-meta {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  font-size: 0.78rem;
  color: var(--pai-color-999-gray, #8c8f9c);
}

.user-profile-bio-edit-btn {
  color: var(--pai-brand-1-normal);
  font-weight: 600;
  cursor: pointer;
  transition: opacity 0.2s;
}

.user-profile-bio-edit-btn:hover {
  opacity: 0.8;
}

.user-profile-actions {
  display: flex;
  gap: 0.5rem;
  padding: 0.5rem 0 0;
}

/* Edit Dialog */
.user-edit-dialog :deep(.el-dialog__body) {
  padding: 1.25rem 1.5rem;
}

.user-edit-dialog-title {
  font-family: 'JetBrains Mono', monospace;
  font-size: 1.1rem;
  font-weight: 700;
  color: var(--pai-color-3-black, #1e2029);
}

.user-edit-dialog-body {
  display: flex;
  gap: 1.5rem;
}

.user-edit-dialog-form {
  flex: 1;
}

.user-edit-dialog-avatar {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 0.4rem;
  width: 120px;
  flex-shrink: 0;
}

.user-edit-avatar-preview {
  width: 80px;
  height: 80px;
  cursor: pointer;
  transition: opacity 0.2s;
}

.user-edit-avatar-preview:hover {
  opacity: 0.8;
}

.user-edit-avatar-label {
  font-size: 0.82rem;
  font-weight: 600;
  color: var(--pai-color-4-gray, #484d5e);
}

.user-edit-avatar-hint {
  font-size: 0.7rem;
  color: var(--pai-color-999-gray, #8c8f9c);
  text-align: center;
  line-height: 1.4;
}

.user-edit-dialog-footer {
  display: flex;
  justify-content: center;
  gap: 0.75rem;
}

@media (max-width: 768px) {
  .user-profile-cover {
    height: 140px;
  }
  .user-profile-content {
    padding: 0 1rem 1rem;
    gap: 1rem;
  }
  .user-profile-avatar {
    width: 72px;
    height: 72px;
  }
  .user-profile-name {
    font-size: 1.2rem;
  }
  .user-profile-meta {
    padding: 0.75rem 1rem 1rem;
  }
}
</style>
