<template>
  <div class="hidden-when-screen-small ml-2 mr-2 flex-col flex photos">

    <el-card class="side-qrcode-card">
      <template #header>作者联系方式</template>
      <template #default>
        <div class="qrcode-item">
          <p class="qrcode-label">微信</p>
          <el-image
            class="qrcode-img"
            src="https://xuyifei-oss.oss-cn-beijing.aliyuncs.com/commons/WechatARCode.png"
            fit="contain"
          ></el-image>
        </div>

        <el-divider></el-divider>

        <div class="qrcode-item">
          <p class="qrcode-label">QQ</p>
          <el-image
            class="qrcode-img"
            src="https://xuyifei-oss.oss-cn-beijing.aliyuncs.com/commons/QQARCode.png"
            fit="contain"
          ></el-image>
        </div>
      </template>
    </el-card>
  </div>
</template>

<script setup lang="ts">

</script>

<style scoped>
.side-qrcode-card {
  border-radius: 12px;
}

.side-qrcode-card :deep(.el-card__header) {
  font-weight: 700;
  font-size: 0.9rem;
  color: var(--pai-color-3-black, #1e2029);
  border-bottom: 1px solid var(--pai-bg-light-2, #eef1f7);
  padding: 0.75rem 1rem;
}

.qrcode-item {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.qrcode-label {
  font-size: 0.8rem;
  color: var(--pai-color-4-gray, #484d5e);
  margin: 0 0 0.5rem;
}

.qrcode-img {
  width: 100%;
  max-width: 180px;
  border-radius: 8px;
}
</style>
