<template>
  <div class="editor-page">
    <div class="editor-header">
      <div class="header-left">
        <el-button @click="goBack">
          <el-icon><ArrowLeft /></el-icon>
          返回
        </el-button>
        <div class="document-info">
          <h3>{{ document?.name }}</h3>
          <span class="file-type">{{ getFileTypeName(document?.fileType) }}</span>
          <span v-if="isOnlyOffice" class="onlyoffice-badge">OnlyOffice</span>
        </div>
      </div>
      <div class="header-right">
        <el-button @click="handlePreview">
          <el-icon><View /></el-icon>
          预览
        </el-button>
        <el-button type="primary" @click="handleSave" :loading="saving" :disabled="!isEditable">
          <el-icon><DocumentAdd /></el-icon>
          保存
        </el-button>
      </div>
    </div>

    <div class="editor-content">
      <div v-if="isOnlyOffice" class="onlyoffice-wrapper">
        <OnlyOfficeEditor
          ref="onlyofficeEditorRef"
          :document-id="docId"
          :can-edit="isEditable"
          @ready="onEditorReady"
        />
      </div>

      <div v-else v-loading="loading">
        <el-alert v-if="!isTextType && document" type="warning" :closable="false" style="margin: 20px;">
          <template #title>
            <p>该文档类型（{{ getFileTypeName(document?.fileType) }}）不支持在线编辑。</p>
            <p style="margin-top: 8px;">请下载后使用专业软件编辑，然后重新上传更新版本。</p>
            <el-button type="primary" @click="handleDownload" style="margin-top: 12px;">
              <el-icon><Download /></el-icon>
              下载文档
            </el-button>
          </template>
        </el-alert>

        <div v-if="isTextType" class="editor-container">
          <div class="editor-toolbar" v-if="document?.fileType === 'markdown'">
            <el-button-group>
              <el-button size="small" @click="insertFormat('**', '**')">粗体</el-button>
              <el-button size="small" @click="insertFormat('*', '*')">斜体</el-button>
              <el-button size="small" @click="insertFormat('`', '`')">代码</el-button>
              <el-button size="small" @click="insertHeading">标题</el-button>
              <el-button size="small" @click="insertFormat('- ', '')">列表</el-button>
              <el-button size="small" @click="insertLink">链接</el-button>
              <el-button size="small" @click="insertFormat('\n```\n', '\n```\n')">代码块</el-button>
            </el-button-group>
          </div>

          <div class="editor-wrapper" v-if="document?.fileType === 'markdown'">
            <div class="editor-section">
              <div class="section-title">编辑</div>
              <el-input
                ref="editorRef"
                v-model="content"
                type="textarea"
                :rows="30"
                placeholder="请输入内容..."
                @input="onContentChange"
                class="markdown-editor"
              />
            </div>
            <div class="preview-section">
              <div class="section-title">预览</div>
              <div class="markdown-preview" v-html="renderedContent"></div>
            </div>
          </div>

          <div v-else class="single-editor">
            <el-input
              ref="editorRef"
              v-model="content"
              type="textarea"
              :rows="35"
              placeholder="请输入内容..."
              @input="onContentChange"
              class="plain-editor"
            />
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch, nextTick } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { marked } from 'marked'
import DOMPurify from 'dompurify'
import OnlyOfficeEditor from '@/components/OnlyOfficeEditor.vue'
import { getDocument, getDocumentContent, saveDocumentContent, checkOnlyOfficeSupport } from '@/api/document'

const route = useRoute()
const router = useRouter()

const document = ref(null)
const content = ref('')
const originalContent = ref('')
const loading = ref(false)
const saving = ref(false)
const editorRef = ref(null)
const onlyofficeEditorRef = ref(null)
const isOnlyOffice = ref(false)
const onlyofficeSupported = ref(false)

const docId = computed(() => route.params.id)

const isTextType = computed(() => {
  const type = document.value?.fileType
  return type === 'markdown' || type === 'csv' || type === 'flowchart' || type === 'mindmap'
})

const isEditable = computed(() => {
  return isOnlyOffice.value || isTextType.value
})

const renderedContent = computed(() => {
  if (!isTextType.value || document.value?.fileType !== 'markdown') return ''
  try {
    const html = marked(content.value || '', { breaks: true, gfm: true })
    return DOMPurify.sanitize(html)
  } catch (e) {
    return content.value || ''
  }
})

const getFileTypeName = (type) => {
  const names = {
    word: 'Word文档',
    excel: 'Excel表格',
    ppt: 'PPT演示',
    pdf: 'PDF文档',
    csv: 'CSV表格',
    markdown: 'Markdown文档',
    image: '图片',
    video: '视频',
    audio: '音频',
    zip: '压缩包',
    flowchart: '流程图',
    mindmap: '脑图',
    txt: '文本文档',
    other: '其他'
  }
  return names[type] || type || '未知'
}

const checkSupport = async () => {
  try {
    const res = await checkOnlyOfficeSupport(docId.value)
    onlyofficeSupported.value = res.data?.supported || false
  } catch (e) {
    console.error('Check OnlyOffice support failed:', e)
    onlyofficeSupported.value = false
  }
}

const loadDocument = async () => {
  loading.value = true
  isOnlyOffice.value = false

  try {
    await checkSupport()

    const docRes = await getDocument(docId.value)
    document.value = docRes.data

    if (onlyofficeSupported.value) {
      isOnlyOffice.value = true
      loading.value = false
    } else if (isTextType.value) {
      const contentRes = await getDocumentContent(docId.value)
      content.value = contentRes.data
      originalContent.value = content.value
      loading.value = false
    } else {
      loading.value = false
    }
  } catch (error) {
    console.error(error)
    ElMessage.error('加载文档失败')
    loading.value = false
  }
}

const onEditorReady = () => {
  loading.value = false
}

const handleSave = async () => {
  if (isOnlyOffice.value) {
    if (onlyofficeEditorRef.value) {
      onlyofficeEditorRef.value.save()
    }
    return
  }

  if (!isTextType.value) return

  saving.value = true
  try {
    await saveDocumentContent(docId.value, content.value)
    originalContent.value = content.value
    ElMessage.success('保存成功')
  } catch (error) {
    console.error(error)
    ElMessage.error('保存失败')
  } finally {
    saving.value = false
  }
}

const handlePreview = () => {
  router.push('/preview/' + docId.value)
}

const handleDownload = () => {
  window.open('/api/document/' + docId.value + '/download', '_blank')
}

const goBack = () => {
  if (!isOnlyOffice.value && content.value !== originalContent.value) {
    ElMessage.warning('文档内容已修改，请先保存')
    return
  }
  router.back()
}

const onContentChange = () => {
}

const insertFormat = (before, after) => {
  const textarea = editorRef.value?.textarea
  if (!textarea) return

  const start = textarea.selectionStart
  const end = textarea.selectionEnd
  const selectedText = content.value.substring(start, end)
  const newText = before + selectedText + after

  content.value = content.value.substring(0, start) + newText + content.value.substring(end)

  nextTick(() => {
    textarea.focus()
    textarea.setSelectionRange(start + before.length, start + before.length + selectedText.length)
  })
}

const insertHeading = () => {
  insertFormat('## ', '')
}

const insertLink = () => {
  insertFormat('[链接文字](', ')')
}

onMounted(() => {
  loadDocument()
})

watch(() => route.params.id, () => {
  loadDocument()
})
</script>

<style scoped>
.editor-page {
  height: 100vh;
  display: flex;
  flex-direction: column;
  background: #f5f7fa;
}

.editor-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 20px;
  background: #fff;
  border-bottom: 1px solid #e4e7ed;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.02);
  flex-shrink: 0;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 16px;
}

.document-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.document-info h3 {
  margin: 0;
  font-size: 16px;
  color: #303133;
}

.file-type {
  font-size: 12px;
  color: #909399;
  background: #f4f4f5;
  padding: 2px 8px;
  border-radius: 4px;
}

.onlyoffice-badge {
  font-size: 12px;
  color: #fff;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 2px 10px;
  border-radius: 4px;
  font-weight: 500;
}

.header-right {
  display: flex;
  gap: 8px;
}

.editor-content {
  flex: 1;
  overflow: hidden;
  display: flex;
  flex-direction: column;
}

.onlyoffice-wrapper {
  flex: 1;
  width: 100%;
  height: 100%;
}

.editor-container {
  height: 100%;
  display: flex;
  flex-direction: column;
}

.editor-toolbar {
  padding: 12px 20px;
  background: #fff;
  border-bottom: 1px solid #e4e7ed;
}

.editor-wrapper {
  flex: 1;
  display: flex;
  min-height: 0;
}

.editor-section,
.preview-section {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-width: 0;
}

.section-title {
  padding: 10px 20px;
  font-size: 13px;
  font-weight: 500;
  color: #606266;
  background: #fafafa;
  border-bottom: 1px solid #e4e7ed;
}

.markdown-editor,
.plain-editor {
  flex: 1;
  resize: none;
}

.markdown-editor :deep(textarea),
.plain-editor :deep(textarea) {
  height: 100% !important;
  min-height: 400px;
  font-family: 'Monaco', 'Menlo', 'Ubuntu Mono', monospace;
  font-size: 14px;
  line-height: 1.6;
  padding: 20px;
  box-sizing: border-box;
}

.preview-section {
  border-left: 1px solid #e4e7ed;
}

.markdown-preview {
  flex: 1;
  padding: 20px;
  background: #fff;
  overflow: auto;
  line-height: 1.8;
}

.markdown-preview :deep(h1),
.markdown-preview :deep(h2),
.markdown-preview :deep(h3) {
  margin-top: 24px;
  margin-bottom: 16px;
  color: #303133;
}

.markdown-preview :deep(h1) {
  font-size: 24px;
  padding-bottom: 10px;
  border-bottom: 1px solid #e4e7ed;
}

.markdown-preview :deep(h2) {
  font-size: 20px;
}

.markdown-preview :deep(h3) {
  font-size: 16px;
}

.markdown-preview :deep(p) {
  margin-bottom: 16px;
  color: #606266;
}

.markdown-preview :deep(code) {
  background: #f4f4f5;
  padding: 2px 6px;
  border-radius: 4px;
  font-size: 13px;
  color: #e96900;
}

.markdown-preview :deep(pre) {
  background: #1e1e1e;
  padding: 16px;
  border-radius: 6px;
  overflow-x: auto;
}

.markdown-preview :deep(pre code) {
  background: transparent;
  color: #d4d4d4;
  padding: 0;
}

.markdown-preview :deep(blockquote) {
  border-left: 4px solid #409eff;
  padding-left: 16px;
  margin: 16px 0;
  color: #909399;
}

.markdown-preview :deep(ul),
.markdown-preview :deep(ol) {
  padding-left: 24px;
  margin-bottom: 16px;
}

.markdown-preview :deep(a) {
  color: #409eff;
  text-decoration: none;
}

.markdown-preview :deep(a:hover) {
  text-decoration: underline;
}

.markdown-preview :deep(table) {
  border-collapse: collapse;
  width: 100%;
  margin-bottom: 16px;
}

.markdown-preview :deep(th),
.markdown-preview :deep(td) {
  border: 1px solid #e4e7ed;
  padding: 8px 12px;
  text-align: left;
}

.markdown-preview :deep(th) {
  background: #fafafa;
  font-weight: 500;
}

.single-editor {
  padding: 20px;
  height: 100%;
  box-sizing: border-box;
}
</style>
