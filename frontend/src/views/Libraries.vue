<template>
  <div class="libraries-page">
    <div class="page-header">
      <div class="header-left">
        <h2>全部文库</h2>
        <span class="count">共 {{ pagination.total }} 个文库</span>
      </div>
      <div class="header-right">
        <el-input
          v-model="keyword"
          placeholder="搜索文库..."
          style="width: 240px"
          prefix-icon="Search"
          clearable
          @keyup.enter="loadLibraries"
          @clear="loadLibraries"
        />
        <el-dropdown @command="handleNewDocument">
          <el-button type="primary">
            <el-icon><Plus /></el-icon>
            新增
            <el-icon class="el-icon--right"><ArrowDown /></el-icon>
          </el-button>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="word">
                <el-icon><Document /></el-icon>
                Word文档
              </el-dropdown-item>
              <el-dropdown-item command="excel">
                <el-icon><Grid /></el-icon>
                Excel表格
              </el-dropdown-item>
              <el-dropdown-item command="ppt">
                <el-icon><Picture /></el-icon>
                PPT演示
              </el-dropdown-item>
              <el-dropdown-item command="csv">
                <el-icon><Tickets /></el-icon>
                CSV表格
              </el-dropdown-item>
              <el-dropdown-item command="markdown">
                <el-icon><Notebook /></el-icon>
                Markdown文档
              </el-dropdown-item>
              <el-dropdown-item command="flowchart">
                <el-icon><Share /></el-icon>
                流程图
              </el-dropdown-item>
              <el-dropdown-item command="mindmap">
                <el-icon><Connection /></el-icon>
                脑图
              </el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
        <el-button @click="showImportDialog = true">
          <el-icon><Upload /></el-icon>
          导入文档
        </el-button>
        <el-button type="primary" @click="showCreateDialog = true">
          <el-icon><Plus /></el-icon>
          新建文库
        </el-button>
      </div>
    </div>

    <div class="library-list" v-loading="loading">
      <el-empty v-if="!loading && libraries.length === 0" description="暂无文库，点击上方按钮创建" />
      <div v-else class="library-grid">
        <div
          v-for="library in libraries"
          :key="library.id"
          class="library-card"
          @click="goToLibrary(library)"
        >
          <div class="card-cover" :style="{ background: getCoverColor(library.id) }">
            <el-icon :size="48"><Collection /></el-icon>
          </div>
          <div class="card-content">
            <h3 class="card-title" :title="library.name">{{ library.name }}</h3>
            <p class="card-desc" :title="library.description">
              {{ library.description || '暂无描述' }}
            </p>
            <div class="card-footer">
              <span class="owner">
                <el-icon><User /></el-icon>
                {{ library.ownerName }}
              </span>
              <span class="time">{{ formatDate(library.createTime) }}</span>
            </div>
          </div>
        </div>
      </div>
    </div>

    <el-pagination
      v-if="pagination.total > 0"
      class="pagination"
      v-model:current-page="pagination.current"
      v-model:page-size="pagination.size"
      :total="pagination.total"
      :page-sizes="[10, 20, 50]"
      layout="total, sizes, prev, pager, next"
      @current-change="loadLibraries"
      @size-change="loadLibraries"
    />

    <el-dialog
      v-model="showCreateDialog"
      title="新建文库"
      width="480px"
    >
      <el-form :model="createForm" label-position="top">
        <el-form-item label="文库名称" required>
          <el-input v-model="createForm.name" placeholder="请输入文库名称" />
        </el-form-item>
        <el-form-item label="文库描述">
          <el-input
            v-model="createForm.description"
            type="textarea"
            :rows="3"
            placeholder="请输入文库描述"
          />
        </el-form-item>
        <el-form-item label="文库类型">
          <el-radio-group v-model="createForm.type">
            <el-radio :value="0">公共文库</el-radio>
            <el-radio :value="1">个人文库</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showCreateDialog = false">取消</el-button>
        <el-button type="primary" :loading="creating" @click="handleCreate">创建</el-button>
      </template>
    </el-dialog>

    <el-dialog
      v-model="showImportDialog"
      title="导入文档"
      width="560px"
    >
      <el-form :model="importForm" label-position="top">
        <el-form-item label="目标文库" required>
          <el-select v-model="importForm.libraryId" placeholder="请选择要导入到的文库" style="width: 100%">
            <el-option
              v-for="lib in allLibraries"
              :key="lib.id"
              :label="lib.name"
              :value="lib.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="目标文件夹">
          <el-select v-model="importForm.folderId" placeholder="根目录（可选）" style="width: 100%">
            <el-option label="根目录" :value="null" />
            <el-option
              v-for="folder in folderOptions"
              :key="folder.id"
              :label="folder.name"
              :value="folder.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="选择文件" required>
          <el-upload
            ref="uploadRef"
            action="#"
            :auto-upload="false"
            :multiple="true"
            :on-change="handleFileChange"
            :on-remove="handleFileRemove"
            :limit="20"
            accept=".doc,.docx,.xls,.xlsx,.ppt,.pptx,.pdf,.csv,.md,.txt,.jpg,.jpeg,.png,.gif,.zip,.rar"
          >
            <el-button type="primary">
              <el-icon><Upload /></el-icon>
              选择文件
            </el-button>
            <template #tip>
              <div class="el-upload__tip">
                支持 doc, docx, xls, xlsx, ppt, pptx, pdf, csv, md, txt, 图片, 压缩包等格式，最多20个文件
              </div>
            </template>
          </el-upload>
          <div v-if="importFiles.length > 0" style="margin-top: 12px">
            <div v-for="(file, index) in importFiles" :key="index" class="file-item">
              <el-icon><Document /></el-icon>
              <span class="file-name">{{ file.name }}</span>
              <span class="file-size">({{ formatFileSize(file.size) }})</span>
            </div>
          </div>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showImportDialog = false">取消</el-button>
        <el-button type="primary" :loading="importing" @click="handleImport">导入</el-button>
      </template>
    </el-dialog>

    <el-dialog
      v-model="showNewDocDialog"
      :title="'新建' + newDocTypeLabel"
      width="480px"
    >
      <el-form :model="newDocForm" label-position="top">
        <el-form-item label="文档名称" required>
          <el-input v-model="newDocForm.name" :placeholder="'请输入' + newDocTypeLabel + '名称'" />
        </el-form-item>
        <el-form-item label="目标文库" required>
          <el-select v-model="newDocForm.libraryId" placeholder="请选择目标文库" style="width: 100%">
            <el-option
              v-for="lib in allLibraries"
              :key="lib.id"
              :label="lib.name"
              :value="lib.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="目标文件夹">
          <el-select v-model="newDocForm.folderId" placeholder="根目录（可选）" style="width: 100%">
            <el-option label="根目录" :value="null" />
            <el-option
              v-for="folder in newDocFolderOptions"
              :key="folder.id"
              :label="folder.name"
              :value="folder.id"
            />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showNewDocDialog = false">取消</el-button>
        <el-button type="primary" :loading="creatingDoc" @click="handleCreateDocument">创建</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { listPublicLibraries, createLibrary, getLibrary } from '@/api/library'
import { importDocuments, createEmptyDocument } from '@/api/document'

const router = useRouter()

const loading = ref(false)
const creating = ref(false)
const importing = ref(false)
const creatingDoc = ref(false)
const keyword = ref('')
const showCreateDialog = ref(false)
const showImportDialog = ref(false)
const showNewDocDialog = ref(false)
const libraries = ref([])
const allLibraries = ref([])
const folderOptions = ref([])
const newDocFolderOptions = ref([])
const uploadRef = ref(null)
const importFiles = ref([])

const pagination = reactive({
  current: 1,
  size: 12,
  total: 0
})

const createForm = reactive({
  name: '',
  description: '',
  type: 0
})

const importForm = reactive({
  libraryId: null,
  folderId: null
})

const newDocType = ref('')
const newDocForm = reactive({
  name: '',
  libraryId: null,
  folderId: null
})

const newDocTypeLabel = ref('')

const coverColors = [
  '#409eff', '#67c23a', '#e6a23c', '#f56c6c', '#909399',
  '#9b59b6', '#34495e', '#1abc9c', '#e91e63', '#00bcd4'
]

const getCoverColor = (id) => {
  return coverColors[id % coverColors.length]
}

const formatDate = (date) => {
  if (!date) return ''
  return new Date(date).toLocaleDateString('zh-CN')
}

const formatFileSize = (bytes) => {
  if (bytes === 0) return '0 B'
  const k = 1024
  const sizes = ['B', 'KB', 'MB', 'GB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  return (bytes / Math.pow(k, i)).toFixed(2) + ' ' + sizes[i]
}

const loadLibraries = async () => {
  loading.value = true
  try {
    const res = await listPublicLibraries({
      current: 1,
      size: 1000,
      keyword: ''
    })
    allLibraries.value = res.data.records || []
    
    const pageRes = await listPublicLibraries({
      current: pagination.current,
      size: pagination.size,
      keyword: keyword.value
    })
    libraries.value = pageRes.data.records || []
    pagination.total = pageRes.data.total || 0
  } catch (error) {
    console.error(error)
  } finally {
    loading.value = false
  }
}

const loadFolders = async (libraryId, optionsRef) => {
  if (!libraryId) {
    optionsRef.value = []
    return
  }
  try {
    const res = await getLibrary(libraryId)
    const folders = res.data.folders || []
    optionsRef.value = folders.map(f => ({
      id: f.id,
      name: f.name
    }))
  } catch (error) {
    console.error(error)
    optionsRef.value = []
  }
}

watch(() => importForm.libraryId, (val) => {
  importForm.folderId = null
  loadFolders(val, folderOptions)
})

watch(() => newDocForm.libraryId, (val) => {
  newDocForm.folderId = null
  loadFolders(val, newDocFolderOptions)
})

const goToLibrary = (library) => {
  router.push(`/library/${library.id}`)
}

const handleCreate = async () => {
  if (!createForm.name.trim()) {
    ElMessage.warning('请输入文库名称')
    return
  }

  creating.value = true
  try {
    await createLibrary(createForm)
    ElMessage.success('创建成功')
    showCreateDialog.value = false
    createForm.name = ''
    createForm.description = ''
    createForm.type = 0
    loadLibraries()
  } catch (error) {
    console.error(error)
  } finally {
    creating.value = false
  }
}

const handleNewDocument = (type) => {
  if (allLibraries.value.length === 0) {
    ElMessage.warning('请先创建文库')
    return
  }
  newDocType.value = type
  switch (type) {
    case 'word':
      newDocTypeLabel.value = 'Word文档'
      break
    case 'excel':
      newDocTypeLabel.value = 'Excel表格'
      break
    case 'ppt':
      newDocTypeLabel.value = 'PPT演示'
      break
    case 'csv':
      newDocTypeLabel.value = 'CSV表格'
      break
    case 'markdown':
      newDocTypeLabel.value = 'Markdown文档'
      break
    case 'flowchart':
      newDocTypeLabel.value = '流程图'
      break
    case 'mindmap':
      newDocTypeLabel.value = '脑图'
      break
  }
  newDocForm.name = ''
  newDocForm.libraryId = allLibraries.value.length > 0 ? allLibraries.value[0].id : null
  newDocForm.folderId = null
  loadFolders(newDocForm.libraryId, newDocFolderOptions)
  showNewDocDialog.value = true
}

const handleCreateDocument = async () => {
  if (!newDocForm.name.trim()) {
    ElMessage.warning('请输入文档名称')
    return
  }
  if (!newDocForm.libraryId) {
    ElMessage.warning('请选择目标文库')
    return
  }

  creatingDoc.value = true
  try {
    const res = await createEmptyDocument({
      libraryId: newDocForm.libraryId,
      folderId: newDocForm.folderId,
      name: newDocForm.name,
      docType: newDocType.value
    })
    ElMessage.success('创建成功')
    showNewDocDialog.value = false
    router.push('/editor/' + res.data.id)
  } catch (error) {
    console.error(error)
    ElMessage.error('创建失败')
  } finally {
    creatingDoc.value = false
  }
}

const handleFileChange = (file, fileList) => {
  importFiles.value = fileList
}

const handleFileRemove = (file, fileList) => {
  importFiles.value = fileList
}

const handleImport = async () => {
  if (!importForm.libraryId) {
    ElMessage.warning('请选择目标文库')
    return
  }
  if (importFiles.value.length === 0) {
    ElMessage.warning('请选择要导入的文件')
    return
  }

  importing.value = true
  try {
    const formData = new FormData()
    formData.append('libraryId', importForm.libraryId)
    if (importForm.folderId) {
      formData.append('folderId', importForm.folderId)
    }
    importFiles.value.forEach(file => {
      formData.append('files', file.raw)
    })
    
    await importDocuments(formData)
    ElMessage.success('导入成功')
    showImportDialog.value = false
    importFiles.value = []
    importForm.libraryId = null
    importForm.folderId = null
    if (uploadRef.value) {
      uploadRef.value.clearFiles()
    }
  } catch (error) {
    console.error(error)
    ElMessage.error('导入失败')
  } finally {
    importing.value = false
  }
}

onMounted(() => {
  loadLibraries()
})
</script>

<style scoped>
.libraries-page {
  padding: 0;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.header-left {
  display: flex;
  align-items: baseline;
  gap: 16px;
}

.header-left h2 {
  margin: 0;
  font-size: 20px;
  color: #303133;
}

.count {
  color: #909399;
  font-size: 14px;
}

.header-right {
  display: flex;
  gap: 12px;
}

.library-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(240px, 1fr));
  gap: 20px;
}

.library-card {
  background: white;
  border-radius: 8px;
  overflow: hidden;
  cursor: pointer;
  transition: all 0.3s;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
}

.library-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
}

.card-cover {
  height: 140px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
}

.card-content {
  padding: 16px;
}

.card-title {
  margin: 0 0 8px;
  font-size: 16px;
  color: #303133;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.card-desc {
  margin: 0 0 12px;
  font-size: 13px;
  color: #909399;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  min-height: 36px;
}

.card-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 12px;
  color: #c0c4cc;
  padding-top: 12px;
  border-top: 1px solid #f5f7fa;
}

.owner {
  display: flex;
  align-items: center;
  gap: 4px;
}

.pagination {
  margin-top: 24px;
  display: flex;
  justify-content: center;
}

.file-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 12px;
  background: #f5f7fa;
  border-radius: 4px;
  margin-bottom: 8px;
}

.file-name {
  flex: 1;
  color: #303133;
  font-size: 14px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.file-size {
  color: #909399;
  font-size: 12px;
  flex-shrink: 0;
}
</style>
