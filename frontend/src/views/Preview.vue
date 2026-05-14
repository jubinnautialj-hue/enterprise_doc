<template>
  <div class="preview-page">
    <div class="preview-header">
      <div class="header-left">
        <el-button link @click="goBack">
          <el-icon><ArrowLeft /></el-icon>
          返回
        </el-button>
        <div class="file-info" v-loading="loading">
          <component :is="getFileIcon(doc?.fileType)" class="file-icon" :class="doc?.fileType" :size="32" />
          <div>
            <h3>{{ doc?.name }}</h3>
            <p>{{ formatSize(doc?.fileSize) }} · {{ doc?.viewCount || 0 }} 次浏览</p>
          </div>
        </div>
      </div>
      <div class="header-right">
        <el-button v-if="canEdit" type="success" @click="goToEditor">
          <el-icon><Edit /></el-icon>
          在线编辑
        </el-button>
        <el-button type="primary" @click="downloadDoc">
          <el-icon><Download /></el-icon>
          下载
        </el-button>
      </div>
    </div>

    <div class="preview-content">
      <div v-loading="loading" class="preview-wrapper">
        <template v-if="doc">
          <div v-if="isImage" class="image-preview">
            <img :src="doc.fileUrl" :alt="doc.name" />
          </div>

          <div v-else-if="isPdf" class="pdf-preview">
            <iframe :src="doc.fileUrl" frameborder="0" width="100%" height="100%"></iframe>
          </div>

          <div v-else-if="isMarkdown" class="markdown-preview">
            <div class="markdown-content" v-html="renderedMarkdown"></div>
          </div>

          <div v-else-if="isText" class="text-preview">
            <pre>{{ textContent || '文本类型文件，需要后端转换才能预览' }}</pre>
          </div>

          <div v-else-if="isVideo" class="video-preview">
            <video :src="doc.fileUrl" controls width="100%" height="100%"></video>
          </div>

          <div v-else-if="isAudio" class="audio-preview">
            <div class="audio-placeholder">
              <el-icon :size="80"><Headset /></el-icon>
              <p>{{ doc.name }}</p>
              <audio :src="doc.fileUrl" controls style="margin-top: 20px;"></audio>
            </div>
          </div>

          <div v-else class="unsupported-preview">
            <el-icon :size="80" color="#909399"><Document /></el-icon>
            <h3>该文件类型暂不支持在线预览</h3>
            <p>支持在线预览：PDF、图片、Markdown、纯文本、视频、音频</p>
            <p>Word、Excel、PPT 等格式需要下载后使用对应软件打开</p>
            <el-button type="primary" @click="downloadDoc" style="margin-top: 20px;">
              <el-icon><Download /></el-icon>
              下载文件
            </el-button>
          </div>
        </template>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { marked } from 'marked'
import DOMPurify from 'dompurify'
import { getDocument } from '@/api/document'

const route = useRoute()
const router = useRouter()

const loading = ref(false)
const doc = ref(null)
const textContent = ref('')

const docId = computed(() => route.params.id)

const canEdit = computed(() => {
  if (!doc.value) return false
  const type = doc.value.fileType
  const textTypes = ['markdown', 'csv', 'flowchart', 'mindmap']
  const officeTypes = ['word', 'excel', 'ppt']
  return textTypes.includes(type) || officeTypes.includes(type)
})

const isImage = computed(() => doc.value?.fileType === 'image')
const isPdf = computed(() => doc.value?.fileType === 'pdf')
const isMarkdown = computed(() => doc.value?.fileType === 'markdown')
const isText = computed(() => doc.value?.fileType === 'text' || doc.value?.fileType === 'csv')
const isVideo = computed(() => doc.value?.fileType === 'video')
const isAudio = computed(() => doc.value?.fileType === 'audio')

const renderedMarkdown = computed(() => {
  if (isMarkdown.value && textContent.value) {
    return DOMPurify.sanitize(marked.parse(textContent.value))
  }
  return ''
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

const goBack = () => {
  router.back()
}

const downloadDoc = () => {
  window.open(`/api/document/${docId.value}/download`, '_blank')
}

const goToEditor = () => {
  router.push(`/editor/${docId.value}`)
}

const loadDoc = async () => {
  loading.value = true
  try {
    const res = await getDocument(docId.value)
    doc.value = res.data

    if (isMarkdown.value) {
      try {
        const response = await fetch(res.data.fileUrl)
        if (response.ok) {
          textContent.value = await response.text()
        }
      } catch (e) {
        console.error('Failed to load markdown:', e)
      }
    }
  } catch (error) {
    console.error(error)
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadDoc()
})
</script>

<style scoped>
.preview-page {
  height: 100vh;
  display: flex;
  flex-direction: column;
  background: #f5f7fa;
}

.preview-header {
  background: white;
  padding: 12px 24px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  border-bottom: 1px solid #e4e7ed;
  flex-shrink: 0;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 16px;
}

.file-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.file-icon {
  font-size: 32px;
  color: #909399;
}

.file-icon.word { color: #2b579a; }
.file-icon.excel { color: #217346; }
.file-icon.ppt { color: #d24726; }
.file-icon.pdf { color: #ff0000; }
.file-icon.image { color: #409eff; }

.file-info h3 {
  margin: 0;
  font-size: 16px;
  color: #303133;
}

.file-info p {
  margin: 4px 0 0;
  font-size: 12px;
  color: #909399;
}

.preview-content {
  flex: 1;
  overflow: auto;
  padding: 24px;
}

.preview-wrapper {
  background: white;
  border-radius: 8px;
  min-height: 100%;
  padding: 20px;
}

.image-preview {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 400px;
}

.image-preview img {
  max-width: 100%;
  max-height: 80vh;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.pdf-preview {
  height: calc(100vh - 200px);
  min-height: 600px;
}

.markdown-preview {
  max-width: 900px;
  margin: 0 auto;
}

.markdown-content {
  font-size: 15px;
  line-height: 1.8;
  color: #303133;
}

.markdown-content :deep(h1),
.markdown-content :deep(h2),
.markdown-content :deep(h3) {
  margin-top: 24px;
  margin-bottom: 16px;
  color: #1f2d3d;
}

.markdown-content :deep(code) {
  background: #f5f7fa;
  padding: 2px 6px;
  border-radius: 4px;
  font-size: 13px;
}

.markdown-content :deep(pre) {
  background: #1e1e1e;
  color: #d4d4d4;
  padding: 16px;
  border-radius: 8px;
  overflow-x: auto;
}

.markdown-content :deep(pre code) {
  background: none;
  padding: 0;
  color: inherit;
}

.markdown-content :deep(blockquote) {
  border-left: 4px solid #409eff;
  padding-left: 16px;
  color: #606266;
  margin: 16px 0;
}

.markdown-content :deep(table) {
  width: 100%;
  border-collapse: collapse;
  margin: 16px 0;
}

.markdown-content :deep(th),
.markdown-content :deep(td) {
  border: 1px solid #ebeef5;
  padding: 8px 12px;
  text-align: left;
}

.markdown-content :deep(th) {
  background: #f5f7fa;
}

.text-preview {
  background: #1e1e1e;
  color: #d4d4d4;
  padding: 20px;
  border-radius: 8px;
  overflow: auto;
  font-family: 'Consolas', 'Monaco', monospace;
  font-size: 14px;
  line-height: 1.6;
  min-height: 400px;
}

.text-preview pre {
  margin: 0;
  white-space: pre-wrap;
  word-wrap: break-word;
}

.video-preview {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 400px;
}

.audio-preview {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 300px;
}

.audio-placeholder {
  text-align: center;
  color: #909399;
}

.unsupported-preview {
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  min-height: 400px;
  color: #606266;
  text-align: center;
}

.unsupported-preview h3 {
  margin: 20px 0 12px;
  color: #303133;
}

.unsupported-preview p {
  margin: 4px 0;
  font-size: 14px;
}
</style>
