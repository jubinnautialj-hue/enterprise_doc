<template>
  <div class="share-page">
    <div class="share-container">
      <div class="share-header">
        <el-icon :size="40" color="#409eff"><Share /></el-icon>
        <h1>分享文档</h1>
      </div>

      <template v-if="!verified">
        <div class="verify-box">
          <el-form v-if="needPassword" :model="verifyForm">
            <el-form-item label="请输入访问密码">
              <el-input
                v-model="verifyForm.password"
                type="password"
                placeholder="请输入密码"
                show-password
                @keyup.enter="handleVerify"
              />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" style="width: 100%" :loading="loading" @click="handleVerify">验证</el-button>
            </el-form-item>
          </el-form>
          <div v-else class="direct-verify">
            <el-button type="primary" :loading="loading" @click="handleVerify">查看文档</el-button>
          </div>
        </div>
      </template>

      <template v-else>
        <div class="document-card" @click="openDocument">
          <div class="document-icon" :class="doc?.fileType">
            <component :is="getFileIcon(doc?.fileType)" :size="48" />
          </div>
          <div class="document-info">
            <h3 class="document-name">{{ doc?.name }}</h3>
            <p class="document-meta">
              <span>{{ formatSize(doc?.fileSize) }}</span>
              <span>{{ doc?.fileType?.toUpperCase() }}</span>
              <span>创建者：{{ doc?.createUserName }}</span>
            </p>
          </div>
          <div class="document-action">
            <el-icon><View /></el-icon>
          </div>
        </div>

        <div class="share-footer">
          <el-button type="primary" @click="openDocument">
            <el-icon><View /></el-icon>
            预览文档
          </el-button>
          <el-button @click="downloadDoc">
            <el-icon><Download /></el-icon>
            下载文档
          </el-button>
        </div>
      </template>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { verifyShare, getShareDocument } from '@/api/document'

const route = useRoute()

const loading = ref(false)
const verified = ref(false)
const shareInfo = ref(null)
const doc = ref(null)
const needPassword = ref(false)

const shareCode = computed(() => route.params.code)

const verifyForm = reactive({
  shareCode: '',
  password: ''
})

const getFileIcon = (type) => {
  const icons = {
    word: 'Document',
    excel: 'Grid',
    ppt: 'Picture',
    pdf: 'Tickets',
    csv: 'Cpu',
    markdown: 'Notebook',
    text: 'Document',
    image: 'Picture',
    video: 'VideoCamera',
    audio: 'Microphone',
    archive: 'Box',
    other: 'Files'
  }
  return icons[type] || 'Files'
}

const formatSize = (bytes) => {
  if (!bytes) return '0 B'
  const k = 1024
  const sizes = ['B', 'KB', 'MB', 'GB', 'TB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  return (bytes / Math.pow(k, i)).toFixed(1) + ' ' + sizes[i]
}

const handleVerify = async () => {
  verifyForm.shareCode = shareCode.value
  loading.value = true
  try {
    const res = await verifyShare(verifyForm)
    shareInfo.value = res.data
    verified.value = true

    const docRes = await getShareDocument(shareCode.value)
    doc.value = docRes.data
  } catch (error) {
    console.error(error)
  } finally {
    loading.value = false
  }
}

const openDocument = () => {
  if (doc.value) {
    window.open(doc.value.fileUrl, '_blank')
  }
}

const downloadDoc = () => {
  if (doc.value) {
    window.open(doc.value.fileUrl + '?download=true', '_blank')
  }
}

onMounted(async () => {
  verifyForm.shareCode = shareCode.value
  
  try {
    const res = await verifyShare({ shareCode: shareCode.value, password: '' })
    shareInfo.value = res.data
    verified.value = true
    
    const docRes = await getShareDocument(shareCode.value)
    doc.value = docRes.data
    needPassword.value = false
  } catch (error) {
    if (error.response?.data?.message?.includes('密码')) {
      needPassword.value = true
    } else if (error.response?.data?.message) {
      ElMessage.error(error.response.data.message)
    }
  }
})
</script>

<style scoped>
.share-page {
  height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px;
}

.share-container {
  width: 100%;
  max-width: 520px;
  background: white;
  border-radius: 16px;
  padding: 40px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.2);
}

.share-header {
  text-align: center;
  margin-bottom: 32px;
}

.share-header h1 {
  margin: 16px 0 0;
  font-size: 24px;
  color: #303133;
}

.verify-box {
  text-align: center;
}

.direct-verify {
  margin-top: 20px;
}

.document-card {
  display: flex;
  align-items: center;
  padding: 20px;
  background: #f5f7fa;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.2s;
  margin-bottom: 24px;
}

.document-card:hover {
  background: #ecf5ff;
}

.document-icon {
  width: 80px;
  height: 80px;
  border-radius: 12px;
  background: #e4e7ed;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #909399;
  flex-shrink: 0;
}

.document-icon.word { background: #e8f0fe; color: #2b579a; }
.document-icon.excel { background: #e6f4ea; color: #217346; }
.document-icon.ppt { background: #fdece7; color: #d24726; }
.document-icon.pdf { background: #ffebee; color: #ff0000; }
.document-icon.image { background: #e8f4fd; color: #409eff; }

.document-info {
  flex: 1;
  margin-left: 20px;
  overflow: hidden;
}

.document-name {
  margin: 0 0 8px;
  font-size: 16px;
  color: #303133;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.document-meta {
  margin: 0;
  font-size: 12px;
  color: #909399;
  display: flex;
  gap: 12px;
}

.document-action {
  color: #c0c4cc;
  flex-shrink: 0;
}

.share-footer {
  display: flex;
  gap: 16px;
}

.share-footer .el-button {
  flex: 1;
}
</style>
