<template>
  <div class="search-page">
    <div class="page-header">
      <h2>搜索结果</h2>
      <span class="keyword">关键词："{{ keyword }}"</span>
      <span class="count" v-if="documents.length > 0">找到 {{ documents.length }} 个文档</span>
    </div>

    <div class="search-results" v-loading="loading">
      <el-empty v-if="!loading && documents.length === 0" description="未找到相关文档" />
      
      <div v-else class="result-list">
        <div
          v-for="doc in documents"
          :key="doc.id"
          class="result-item"
          @click="previewDocument(doc)"
        >
          <div class="result-icon" :class="doc.fileType">
            <component :is="getFileIcon(doc.fileType)" :size="32" />
          </div>
          <div class="result-content">
            <h3 class="result-title">{{ doc.name }}</h3>
            <p class="result-meta">
              <span><el-icon><User /></el-icon> {{ doc.createUserName }}</span>
              <span><el-icon><Document /></el-icon> {{ doc.fileType }}</span>
              <span>{{ formatSize(doc.fileSize) }}</span>
              <span>{{ formatDate(doc.createTime) }}</span>
            </p>
            <div class="result-actions">
              <el-button link type="primary" size="small" @click.stop="previewDocument(doc)">预览</el-button>
              <el-button link type="success" size="small" @click.stop="downloadDocument(doc)">下载</el-button>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { searchDocuments } from '@/api/document'

const route = useRoute()
const router = useRouter()

const loading = ref(false)
const keyword = ref('')
const documents = ref([])

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

const formatDate = (date) => {
  if (!date) return ''
  return new Date(date).toLocaleDateString('zh-CN')
}

const loadResults = async () => {
  keyword.value = route.query.keyword || ''
  if (!keyword.value) return

  loading.value = true
  try {
    const res = await searchDocuments({
      keyword: keyword.value,
      current: 1,
      size: 100
    })
    documents.value = res.data || []
  } catch (error) {
    console.error(error)
  } finally {
    loading.value = false
  }
}

const previewDocument = (doc) => {
  router.push(`/preview/${doc.id}`)
}

const downloadDocument = (doc) => {
  window.open(`/api/document/${doc.id}/download`, '_blank')
}

watch(() => route.query.keyword, () => {
  loadResults()
})

onMounted(() => {
  loadResults()
})
</script>

<style scoped>
.search-page {
  padding: 0;
}

.page-header {
  margin-bottom: 20px;
  display: flex;
  align-items: baseline;
  gap: 16px;
}

.page-header h2 {
  margin: 0;
  font-size: 20px;
  color: #303133;
}

.keyword {
  color: #606266;
}

.count {
  color: #909399;
  font-size: 14px;
}

.search-results {
  background: white;
  border-radius: 8px;
  padding: 20px;
}

.result-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.result-item {
  display: flex;
  gap: 16px;
  padding: 16px;
  border: 1px solid #ebeef5;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s;
}

.result-item:hover {
  border-color: #409eff;
  background: #f5f7fa;
}

.result-icon {
  width: 64px;
  height: 64px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f5f7fa;
  border-radius: 8px;
  color: #909399;
  flex-shrink: 0;
}

.result-icon.word { color: #2b579a; }
.result-icon.excel { color: #217346; }
.result-icon.ppt { color: #d24726; }
.result-icon.pdf { color: #ff0000; }

.result-content {
  flex: 1;
  min-width: 0;
}

.result-title {
  margin: 0 0 8px;
  font-size: 16px;
  color: #303133;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.result-meta {
  margin: 0 0 8px;
  font-size: 13px;
  color: #909399;
  display: flex;
  gap: 16px;
  align-items: center;
  flex-wrap: wrap;
}

.result-meta span {
  display: flex;
  align-items: center;
  gap: 4px;
}

.result-actions {
  display: flex;
  gap: 8px;
}
</style>
