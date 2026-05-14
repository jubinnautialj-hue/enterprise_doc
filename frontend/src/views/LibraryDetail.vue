<template>
  <div class="library-detail">
    <div class="library-header" v-loading="libraryLoading">
      <div class="library-info">
        <div class="library-icon" :style="{ background: getCoverColor(library?.id) }">
          <el-icon :size="32"><Collection /></el-icon>
        </div>
        <div class="library-meta">
          <h2>{{ library?.name }}</h2>
          <p>{{ library?.description || '暂无描述' }}</p>
          <div class="meta-footer">
            <span>创建者：{{ library?.ownerName }}</span>
            <span>创建时间：{{ formatDate(library?.createTime) }}</span>
          </div>
        </div>
      </div>
      <div class="library-actions">
        <el-button type="primary" @click="uploadDoc">
          <el-icon><Upload /></el-icon>
          上传文档
        </el-button>
        <el-button @click="createFolder">
          <el-icon><FolderAdd /></el-icon>
          新建文件夹
        </el-button>
      </div>
    </div>

    <div class="content-container">
      <div class="sidebar">
        <div class="sidebar-header">
          <span>文件目录</span>
        </div>
        <div class="tree-container">
          <el-tree
            :data="folderTree"
            :props="{ label: 'name', children: 'children' }"
            :expand-on-click-node="false"
            :default-expanded-keys="expandedKeys"
            @node-click="handleFolderClick"
            node-key="id"
          >
            <template #default="{ data }">
              <span class="tree-node" :class="{ active: currentFolderId === data.id }">
                <el-icon><Folder /></el-icon>
                <span class="tree-label">{{ data.name }}</span>
              </span>
            </template>
          </el-tree>
        </div>
      </div>

      <div class="main-content">
        <div class="path-bar">
          <el-breadcrumb separator="/">
            <el-breadcrumb-item>
              <span class="path-root" @click="goToRoot">
                <el-icon><HomeFilled /></el-icon>
                根目录
              </span>
            </el-breadcrumb-item>
            <el-breadcrumb-item
              v-for="(item, index) in breadcrumbFolders"
              :key="index"
            >
              <span v-if="index === breadcrumbFolders.length - 1">{{ item.name }}</span>
              <span v-else @click="navigateToFolder(item)" class="path-link">{{ item.name }}</span>
            </el-breadcrumb-item>
          </el-breadcrumb>
        </div>

        <div class="file-list" v-loading="loading">
          <div v-if="!loading && folders.length === 0 && documents.length === 0" class="empty-tip">
            <el-empty description="当前目录为空，点击上方按钮上传文档或创建文件夹" />
          </div>

          <div v-else class="file-grid">
            <div
              v-for="folder in folders"
              :key="'f_' + folder.id"
              class="file-card folder-card"
              @click="openFolder(folder)"
            >
              <div class="file-icon">
                <el-icon :size="48" color="#409eff"><Folder /></el-icon>
              </div>
              <div class="file-name" :title="folder.name">{{ folder.name }}</div>
              <div class="file-actions">
                <el-button link type="primary" size="small" @click.stop="renameFolder(folder)">重命名</el-button>
                <el-button link type="danger" size="small" @click.stop="deleteFolder(folder)">删除</el-button>
              </div>
            </div>

            <div
              v-for="doc in documents"
              :key="'d_' + doc.id"
              class="file-card"
              @click="previewDocument(doc)"
            >
              <div class="file-icon" :class="doc.fileType">
                <component :is="getFileIcon(doc.fileType)" :size="48" />
              </div>
              <div class="file-name" :title="doc.name">{{ doc.name }}</div>
              <div class="file-meta">
                <span>{{ formatSize(doc.fileSize) }}</span>
                <span>{{ doc.viewCount || 0 }} 次浏览</span>
              </div>
              <div class="file-actions">
                <el-button link type="primary" size="small" @click.stop="previewDocument(doc)">预览</el-button>
                <el-button 
                  v-if="canOnlineEdit(doc)" 
                  link 
                  type="success" 
                  size="small" 
                  @click.stop="editDocument(doc)"
                >
                  <el-icon><Edit /></el-icon>
                  编辑
                </el-button>
                <el-button link type="primary" size="small" @click.stop="downloadDocument(doc)">下载</el-button>
                <el-button link type="warning" size="small" @click.stop="shareDocument(doc)">分享</el-button>
                <el-dropdown @click.stop>
                  <el-button link type="primary" size="small">更多</el-button>
                  <template #dropdown>
                    <el-dropdown-menu>
                      <el-dropdown-item @click.stop="renameDocument(doc)">重命名</el-dropdown-item>
                      <el-dropdown-item @click.stop="openMoveDialog(doc)">移动</el-dropdown-item>
                      <el-dropdown-item @click.stop="deleteDocument(doc)" divided>删除</el-dropdown-item>
                    </el-dropdown-menu>
                  </template>
                </el-dropdown>
              </div>
            </div>
          </div>

          <el-pagination
            v-if="docPagination.total > 0"
            class="pagination"
            v-model:current-page="docPagination.current"
            v-model:page-size="docPagination.size"
            :total="docPagination.total"
            :page-sizes="[12, 24, 48]"
            layout="total, sizes, prev, pager, next"
            @current-change="loadDocuments"
            @size-change="loadDocuments"
          />
        </div>
      </div>
    </div>

    <el-dialog v-model="showUploadDialog" title="上传文档" width="500px">
      <el-upload
        ref="uploadRef"
        drag
        :auto-upload="false"
        :multiple="true"
        :limit="10"
        :file-list="fileList"
        :on-change="handleFileChange"
        :on-remove="handleFileRemove"
      >
        <el-icon class="el-icon--upload"><UploadFilled /></el-icon>
        <div class="el-upload__text">将文件拖到此处，或<em>点击选择</em></div>
        <template #tip>
          <div class="el-upload__tip">
            支持 word、excel、pdf、ppt、csv、markdown、图片等格式，单个文件不超过 100MB
          </div>
        </template>
      </el-upload>
      <template #footer>
        <el-button @click="showUploadDialog = false">取消</el-button>
        <el-button type="primary" :loading="uploading" @click="handleUpload">上传</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="showFolderDialog" :title="folderDialogTitle" width="400px">
      <el-input v-model="folderName" placeholder="请输入文件夹名称" @keyup.enter="handleCreateFolder" />
      <template #footer>
        <el-button @click="showFolderDialog = false">取消</el-button>
        <el-button type="primary" @click="handleCreateFolder">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="showShareDialog" title="分享文档" width="450px">
      <div class="share-info" v-if="currentShare">
        <el-alert type="success" :closable="false">
          <template #title>
            <div>分享链接：{{ currentShare.shareUrl }}</div>
            <div style="margin-top: 8px; font-size: 12px;">
              {{ currentShare.password ? '访问密码：' + currentShare.password : '无密码访问' }}
              {{ currentShare.expireType === 0 ? '· 永久有效' : '· 临时有效' }}
            </div>
          </template>
        </el-alert>
        <div class="share-actions" style="margin-top: 16px;">
          <el-button type="primary" @click="copyShareLink">复制链接</el-button>
          <el-button type="danger" @click="cancelShare">取消分享</el-button>
        </div>
      </div>
      <div v-else>
        <el-form label-position="top">
          <el-form-item label="访问密码">
            <el-input v-model="shareForm.password" placeholder="留空则无需密码" />
          </el-form-item>
          <el-form-item label="有效期">
            <el-radio-group v-model="shareForm.expireType">
              <el-radio :value="0">永久</el-radio>
              <el-radio :value="1">1天</el-radio>
              <el-radio :value="2">7天</el-radio>
              <el-radio :value="3">30天</el-radio>
            </el-radio-group>
          </el-form-item>
        </el-form>
      </div>
      <template #footer>
        <el-button v-if="!currentShare" @click="showShareDialog = false">取消</el-button>
        <el-button v-if="!currentShare" type="primary" @click="createShare">生成分享链接</el-button>
        <el-button v-else @click="showShareDialog = false">关闭</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="showMoveDialog" title="移动到" width="400px">
      <el-tree
        :data="folderTree"
        :props="{ label: 'name', children: 'children' }"
        node-key="id"
        :default-expand-all="true"
        v-model:current-node-key="moveTargetFolderId"
        highlight-current
      >
        <template #default="{ data }">
          <span>
            <el-icon><Folder /></el-icon>
            {{ data.name }}
          </span>
        </template>
      </el-tree>
      <template #footer>
        <el-button @click="showMoveDialog = false">取消</el-button>
        <el-button type="primary" @click="handleMove">移动</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getLibrary } from '@/api/library'
import {
  getFoldersByLibrary,
  createFolder as createFolderApi,
  deleteFolder as deleteFolderApi,
  renameFolder as renameFolderApi
} from '@/api/folder'
import {
  listDocuments as listDocsApi,
  uploadDocument as uploadDocApi,
  deleteDocument as deleteDocApi,
  renameDocument as renameDocApi,
  moveDocument as moveDocApi,
  createShare as createShareApi,
  cancelShare as cancelShareApi,
  checkOnlyOfficeSupport
} from '@/api/document'

const route = useRoute()
const router = useRouter()

const libraryId = computed(() => Number(route.params.id))

const libraryLoading = ref(false)
const loading = ref(false)
const uploading = ref(false)
const library = ref(null)

const showUploadDialog = ref(false)
const showFolderDialog = ref(false)
const showShareDialog = ref(false)
const showMoveDialog = ref(false)
const folderDialogTitle = ref('新建文件夹')
const currentEditFolder = ref(null)
const folderName = ref('')
const currentMoveDoc = ref(null)
const moveTargetFolderId = ref(0)
const currentShareDoc = ref(null)
const currentShare = ref(null)

const fileList = ref([])
const uploadRef = ref(null)

const folderTree = ref([{ id: 0, name: '根目录', parentId: 0, children: [] }])
const expandedKeys = ref([0])
const currentFolderId = ref(0)
const currentFolderPath = ref([])
const folders = ref([])
const documents = ref([])

const shareForm = reactive({
  password: '',
  expireType: 0
})

const docPagination = reactive({
  current: 1,
  size: 24,
  total: 0
})

const coverColors = [
  '#409eff', '#67c23a', '#e6a23c', '#f56c6c', '#909399',
  '#9b59b6', '#34495e', '#1abc9c', '#e91e63', '#00bcd4'
]

const getCoverColor = (id) => {
  if (!id) return coverColors[0]
  return coverColors[id % coverColors.length]
}

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

const formatDate = (date) => {
  if (!date) return ''
  return new Date(date).toLocaleDateString('zh-CN')
}

const formatSize = (bytes) => {
  if (!bytes) return '0 B'
  const k = 1024
  const sizes = ['B', 'KB', 'MB', 'GB', 'TB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  return (bytes / Math.pow(k, i)).toFixed(1) + ' ' + sizes[i]
}

const breadcrumbFolders = computed(() => {
  return currentFolderPath.value
})

const loadLibrary = async () => {
  libraryLoading.value = true
  try {
    const res = await getLibrary(libraryId.value)
    library.value = res.data
  } catch (error) {
    console.error(error)
  } finally {
    libraryLoading.value = false
  }
}

const loadFolders = async () => {
  try {
    const res = await getFoldersByLibrary(libraryId.value)
    const folders = res.data || []
    folderTree.value = [{ id: 0, name: '根目录', parentId: 0, children: folders }]
  } catch (error) {
    console.error(error)
  }
}

const loadCurrentFolderItems = async () => {
  loading.value = true
  try {
    const [foldersRes, docsRes] = await Promise.all([
      getFoldersByLibrary(libraryId.value).catch(() => ({ data: [] })),
      listDocsApi({
        libraryId: libraryId.value,
        folderId: currentFolderId.value,
        current: docPagination.current,
        size: docPagination.size
      })
    ])

    const allFolders = foldersRes.data || []
    folders.value = allFolders.filter(f => f.parentId === currentFolderId.value)

    documents.value = docsRes.data.records || []
    docPagination.total = docsRes.data.total || 0
  } catch (error) {
    console.error(error)
  } finally {
    loading.value = false
  }
}

const loadDocuments = async () => {
  loading.value = true
  try {
    const res = await listDocsApi({
      libraryId: libraryId.value,
      folderId: currentFolderId.value,
      current: docPagination.current,
      size: docPagination.size
    })
    documents.value = res.data.records || []
    docPagination.total = res.data.total || 0
  } catch (error) {
    console.error(error)
  } finally {
    loading.value = false
  }
}

const goToRoot = () => {
  currentFolderId.value = 0
  currentFolderPath.value = []
  docPagination.current = 1
  loadCurrentFolderItems()
}

const handleFolderClick = (data) => {
  navigateToFolder(data)
}

const navigateToFolder = (folder) => {
  if (folder.id === 0) {
    goToRoot()
  } else {
    currentFolderId.value = folder.id
    buildFolderPath(folder)
    docPagination.current = 1
    loadCurrentFolderItems()
  }
}

const buildFolderPath = (targetFolder) => {
  const allFolders = flattenFolders(folderTree.value)
  const path = []
  let current = targetFolder

  while (current && current.id !== 0) {
    path.unshift(current)
    current = allFolders.find(f => f.id === current.parentId)
  }
  currentFolderPath.value = path
}

const flattenFolders = (nodes, result = []) => {
  for (const node of nodes) {
    result.push(node)
    if (node.children) {
      flattenFolders(node.children, result)
    }
  }
  return result
}

const openFolder = (folder) => {
  navigateToFolder(folder)
}

const createFolder = () => {
  folderDialogTitle.value = '新建文件夹'
  currentEditFolder.value = null
  folderName.value = ''
  showFolderDialog.value = true
}

const renameFolder = (folder) => {
  folderDialogTitle.value = '重命名文件夹'
  currentEditFolder.value = folder
  folderName.value = folder.name
  showFolderDialog.value = true
}

const handleCreateFolder = async () => {
  if (!folderName.value.trim()) {
    ElMessage.warning('请输入文件夹名称')
    return
  }

  try {
    if (currentEditFolder.value) {
      await renameFolderApi(currentEditFolder.value.id, folderName.value)
      ElMessage.success('重命名成功')
    } else {
      await createFolderApi({
        libraryId: libraryId.value,
        parentId: currentFolderId.value || 0,
        name: folderName.value
      })
      ElMessage.success('创建成功')
    }
    showFolderDialog.value = false
    loadFolders()
    loadCurrentFolderItems()
  } catch (error) {
    console.error(error)
  }
}

const deleteFolder = async (folder) => {
  try {
    await ElMessageBox.confirm('确定要删除该文件夹吗？文件夹内的内容也会被删除', '提示', {
      type: 'warning'
    })
    await deleteFolderApi(folder.id)
    ElMessage.success('删除成功')
    loadFolders()
    loadCurrentFolderItems()
  } catch (error) {
    if (error !== 'cancel') console.error(error)
  }
}

const uploadDoc = () => {
  fileList.value = []
  showUploadDialog.value = true
}

const handleFileChange = (file, fileList) => {
  fileList.value = fileList
}

const handleFileRemove = (file, fileList) => {
  fileList.value = fileList
}

const handleUpload = async () => {
  if (fileList.value.length === 0) {
    ElMessage.warning('请选择要上传的文件')
    return
  }

  uploading.value = true
  try {
    for (const file of fileList.value) {
      const formData = new FormData()
      formData.append('libraryId', libraryId.value)
      if (currentFolderId.value > 0) {
        formData.append('folderId', currentFolderId.value)
      }
      formData.append('file', file.raw)

      await uploadDocApi(formData)
    }
    ElMessage.success('上传成功')
    showUploadDialog.value = false
    fileList.value = []
    loadCurrentFolderItems()
  } catch (error) {
    console.error(error)
  } finally {
    uploading.value = false
  }
}

const canOnlineEdit = (doc) => {
  if (!doc) return false
  const type = doc.fileType
  const textTypes = ['markdown', 'csv', 'flowchart', 'mindmap']
  const officeTypes = ['word', 'excel', 'ppt']
  return textTypes.includes(type) || officeTypes.includes(type)
}

const previewDocument = (doc) => {
  router.push(`/preview/${doc.id}`)
}

const editDocument = (doc) => {
  router.push(`/editor/${doc.id}`)
}

const downloadDocument = (doc) => {
  window.open(`/api/document/${doc.id}/download`, '_blank')
}

const renameDocument = async (doc) => {
  try {
    const { value } = await ElMessageBox.prompt('请输入新名称', '重命名', {
      inputValue: doc.name,
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      inputPattern: /\S/,
      inputErrorMessage: '名称不能为空'
    })
    await renameDocApi(doc.id, value)
    ElMessage.success('重命名成功')
    loadCurrentFolderItems()
  } catch (error) {
    if (error !== 'cancel') console.error(error)
  }
}

const openMoveDialog = (doc) => {
  currentMoveDoc.value = doc
  moveTargetFolderId.value = 0
  showMoveDialog.value = true
}

const handleMove = async () => {
  try {
    await moveDocApi(currentMoveDoc.value.id, moveTargetFolderId.value || null)
    ElMessage.success('移动成功')
    showMoveDialog.value = false
    loadCurrentFolderItems()
  } catch (error) {
    console.error(error)
  }
}

const deleteDocument = async (doc) => {
  try {
    await ElMessageBox.confirm('确定要删除该文档吗？', '提示', { type: 'warning' })
    await deleteDocApi(doc.id)
    ElMessage.success('删除成功')
    loadCurrentFolderItems()
  } catch (error) {
    if (error !== 'cancel') console.error(error)
  }
}

const shareDocument = (doc) => {
  currentShareDoc.value = doc
  currentShare.value = null
  shareForm.password = ''
  shareForm.expireType = 0
  showShareDialog.value = true
}

const createShare = async () => {
  try {
    const res = await createShareApi({
      shareType: 2,
      targetId: currentShareDoc.value.id,
      password: shareForm.password,
      expireType: shareForm.expireType
    })
    currentShare.value = res.data
  } catch (error) {
    console.error(error)
  }
}

const copyShareLink = async () => {
  const fullUrl = window.location.origin + currentShare.value.shareUrl
  try {
    await navigator.clipboard.writeText(fullUrl)
    ElMessage.success('链接已复制到剪贴板')
  } catch {
    ElMessage.warning('复制失败，请手动复制')
  }
}

const cancelShare = async () => {
  try {
    await ElMessageBox.confirm('确定要取消分享吗？', '提示', { type: 'warning' })
    await cancelShareApi(currentShare.value.id)
    ElMessage.success('已取消分享')
    currentShare.value = null
  } catch (error) {
    if (error !== 'cancel') console.error(error)
  }
}

onMounted(() => {
  loadLibrary()
  loadFolders()
  loadCurrentFolderItems()
})
</script>

<style scoped>
.library-detail {
  padding: 0;
}

.library-header {
  background: white;
  padding: 20px 24px;
  border-radius: 8px;
  margin-bottom: 20px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.library-info {
  display: flex;
  gap: 16px;
}

.library-icon {
  width: 72px;
  height: 72px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
}

.library-meta h2 {
  margin: 0 0 8px;
  font-size: 20px;
  color: #303133;
}

.library-meta p {
  margin: 0 0 8px;
  color: #606266;
  font-size: 14px;
}

.meta-footer {
  font-size: 12px;
  color: #909399;
  display: flex;
  gap: 20px;
}

.library-actions {
  display: flex;
  gap: 12px;
}

.content-container {
  display: flex;
  gap: 20px;
  height: calc(100vh - 260px);
}

.sidebar {
  width: 220px;
  background: white;
  border-radius: 8px;
  overflow: hidden;
  display: flex;
  flex-direction: column;
}

.sidebar-header {
  padding: 16px;
  font-weight: 600;
  border-bottom: 1px solid #ebeef5;
}

.tree-container {
  flex: 1;
  overflow: auto;
  padding: 8px;
}

.tree-node {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 4px;
  border-radius: 4px;
  cursor: pointer;
}

.tree-node.active {
  background: #ecf5ff;
  color: #409eff;
}

.tree-label {
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.main-content {
  flex: 1;
  background: white;
  border-radius: 8px;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.path-bar {
  padding: 16px 20px;
  border-bottom: 1px solid #ebeef5;
}

.path-root, .path-link {
  cursor: pointer;
}

.path-root:hover, .path-link:hover {
  color: #409eff;
}

.file-list {
  flex: 1;
  overflow: auto;
  padding: 20px;
}

.file-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(160px, 1fr));
  gap: 16px;
}

.file-card {
  background: #fafafa;
  border: 1px solid #ebeef5;
  border-radius: 8px;
  padding: 16px 12px;
  cursor: pointer;
  transition: all 0.2s;
  text-align: center;
}

.file-card:hover {
  border-color: #409eff;
  background: #f5f7fa;
}

.file-icon {
  height: 72px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #909399;
}

.file-icon.word { color: #2b579a; }
.file-icon.excel { color: #217346; }
.file-icon.ppt { color: #d24726; }
.file-icon.pdf { color: #ff0000; }
.file-icon.image { color: #409eff; }

.file-name {
  font-size: 13px;
  color: #303133;
  margin: 8px 0;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  min-height: 36px;
}

.file-meta {
  font-size: 11px;
  color: #909399;
  display: flex;
  justify-content: space-between;
  margin-bottom: 8px;
}

.file-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 4px;
  justify-content: center;
}

.empty-tip {
  height: 300px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}

.share-info {
  margin-bottom: 16px;
}

.share-actions {
  display: flex;
  gap: 12px;
}
</style>
