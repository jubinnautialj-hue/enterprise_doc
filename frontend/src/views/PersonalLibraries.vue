<template>
  <div class="libraries-page">
    <div class="page-header">
      <div class="header-left">
        <h2>个人文库</h2>
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
        <el-button type="primary" @click="showCreateDialog = true">
          <el-icon><Plus /></el-icon>
          新建文库
        </el-button>
      </div>
    </div>

    <div class="library-list" v-loading="loading">
      <el-empty v-if="!loading && libraries.length === 0" description="暂无个人文库，点击上方按钮创建" />
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
            <div class="title-row">
              <h3 class="card-title" :title="library.name">{{ library.name }}</h3>
              <el-dropdown @click.stop>
                <el-icon class="more"><MoreFilled /></el-icon>
                <template #dropdown>
                  <el-dropdown-menu>
                    <el-dropdown-item @click.stop="editLibrary(library)">编辑</el-dropdown-item>
                    <el-dropdown-item @click.stop="deleteLibrary(library)" divided>删除</el-dropdown-item>
                  </el-dropdown-menu>
                </template>
              </el-dropdown>
            </div>
            <p class="card-desc" :title="library.description">
              {{ library.description || '暂无描述' }}
            </p>
            <div class="card-footer">
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
      v-model="showEditDialog"
      title="编辑文库"
      width="480px"
    >
      <el-form :model="editForm" label-position="top">
        <el-form-item label="文库名称" required>
          <el-input v-model="editForm.name" placeholder="请输入文库名称" />
        </el-form-item>
        <el-form-item label="文库描述">
          <el-input
            v-model="editForm.description"
            type="textarea"
            :rows="3"
            placeholder="请输入文库描述"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showEditDialog = false">取消</el-button>
        <el-button type="primary" :loading="editing" @click="handleEdit">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { listPersonalLibraries, createLibrary, updateLibrary, deleteLibrary } from '@/api/library'

const router = useRouter()

const loading = ref(false)
const creating = ref(false)
const editing = ref(false)
const keyword = ref('')
const showCreateDialog = ref(false)
const showEditDialog = ref(false)
const libraries = ref([])

const pagination = reactive({
  current: 1,
  size: 12,
  total: 0
})

const createForm = reactive({
  name: '',
  description: '',
  type: 1
})

const editForm = reactive({
  id: null,
  name: '',
  description: ''
})

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

const loadLibraries = async () => {
  loading.value = true
  try {
    const res = await listPersonalLibraries({
      current: pagination.current,
      size: pagination.size,
      keyword: keyword.value
    })
    libraries.value = res.data.records || []
    pagination.total = res.data.total || 0
  } catch (error) {
    console.error(error)
  } finally {
    loading.value = false
  }
}

const goToLibrary = (library) => {
  router.push(`/library/${library.id}`)
}

const editLibrary = (library) => {
  editForm.id = library.id
  editForm.name = library.name
  editForm.description = library.description
  showEditDialog.value = true
}

const deleteLibrary = async (library) => {
  try {
    await ElMessageBox.confirm('确定要删除该文库吗？删除后无法恢复', '提示', {
      type: 'warning'
    })
    await deleteLibrary(library.id)
    ElMessage.success('删除成功')
    loadLibraries()
  } catch (error) {
    if (error !== 'cancel') {
      console.error(error)
    }
  }
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
    createForm.type = 1
    loadLibraries()
  } catch (error) {
    console.error(error)
  } finally {
    creating.value = false
  }
}

const handleEdit = async () => {
  if (!editForm.name.trim()) {
    ElMessage.warning('请输入文库名称')
    return
  }

  editing.value = true
  try {
    await updateLibrary(editForm.id, {
      name: editForm.name,
      description: editForm.description
    })
    ElMessage.success('保存成功')
    showEditDialog.value = false
    loadLibraries()
  } catch (error) {
    console.error(error)
  } finally {
    editing.value = false
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

.title-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-title {
  flex: 1;
  margin: 0 0 8px;
  font-size: 16px;
  color: #303133;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.more {
  cursor: pointer;
  color: #909399;
  padding: 4px;
}

.more:hover {
  color: #409eff;
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

.pagination {
  margin-top: 24px;
  display: flex;
  justify-content: center;
}
</style>
