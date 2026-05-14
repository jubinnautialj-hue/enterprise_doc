<template>
  <div class="onlyoffice-editor">
    <div id="onlyoffice-editor-container" ref="editorContainer" class="editor-container"></div>
    <div v-if="loading" class="loading-overlay">
      <el-spinner size="40" />
      <div class="loading-text">加载编辑器中...</div>
    </div>
    <div v-if="error" class="error-overlay">
      <el-alert :title="error" type="error" show-icon :closable="false" />
      <el-button type="primary" @click="retry" style="margin-top: 16px;">重试</el-button>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { getOnlyOfficeConfig } from '@/api/document'

const props = defineProps({
  documentId: {
    type: [String, Number],
    required: true
  },
  canEdit: {
    type: Boolean,
    default: true
  }
})

const emit = defineEmits(['ready', 'documentReady', 'documentStateChange'])

const editorContainer = ref(null)
const loading = ref(true)
const error = ref('')
const docEditor = ref(null)
let scriptLoaded = false

const loadScript = (url) => {
  return new Promise((resolve, reject) => {
    if (window.DocsAPI) {
      resolve()
      return
    }

    const script = document.createElement('script')
    script.type = 'text/javascript'
    script.src = url
    script.onload = () => {
      scriptLoaded = true
      resolve()
    }
    script.onerror = () => {
      reject(new Error('Failed to load OnlyOffice API script'))
    }
    document.head.appendChild(script)
  })
}

const initEditor = async () => {
  loading.value = true
  error.value = ''

  try {
    const res = await getOnlyOfficeConfig(props.documentId, props.canEdit)
    const { config, apiUrl } = res.data

    await loadScript(apiUrl)

    if (editorContainer.value) {
      editorContainer.value.innerHTML = ''
    }

    if (window.DocsAPI) {
      const editorConfig = {
        ...config,
        events: {
          'onReady': onEditorReady,
          'onDocumentReady': onDocumentReady,
          'onDocumentStateChange': onDocumentStateChange,
          'onError': onError
        }
      }

      docEditor.value = new window.DocsAPI.DocEditor('onlyoffice-editor-container', editorConfig)
    }
  } catch (err) {
    console.error('Failed to initialize OnlyOffice editor:', err)
    error.value = err.message || '初始化编辑器失败，请检查OnlyOffice服务是否正常运行'
    loading.value = false
  }
}

const onEditorReady = () => {
  loading.value = false
  emit('ready')
}

const onDocumentReady = () => {
  emit('documentReady')
}

const onDocumentStateChange = (event) => {
  emit('documentStateChange', event.data)
}

const onError = (event) => {
  console.error('OnlyOffice error:', event.data)
  error.value = event.data?.message || '编辑器发生错误'
}

const retry = () => {
  error.value = ''
  initEditor()
}

const download = () => {
  if (docEditor.value) {
    docEditor.value.downloadAs()
  }
}

const save = () => {
  if (docEditor.value) {
    docEditor.value.save()
    ElMessage.success('已触发保存')
  }
}

watch(() => props.documentId, (newId, oldId) => {
  if (newId !== oldId) {
    initEditor()
  }
})

onMounted(() => {
  initEditor()
})

onBeforeUnmount(() => {
  if (docEditor.value && docEditor.value.destroyEditor) {
    try {
      docEditor.value.destroyEditor()
    } catch (e) {
      console.log('Destroy editor error:', e)
    }
  }
})

defineExpose({
  download,
  save,
  retry
})
</script>

<style scoped>
.onlyoffice-editor {
  width: 100%;
  height: 100%;
  position: relative;
}

.editor-container {
  width: 100%;
  height: 100%;
  min-height: 500px;
}

.loading-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  background: rgba(255, 255, 255, 0.9);
  z-index: 10;
}

.loading-text {
  margin-top: 16px;
  color: #909399;
}

.error-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  background: rgba(255, 255, 255, 0.9);
  z-index: 10;
  padding: 24px;
}
</style>
