<template>
  <div class="permission-manage">
    <div class="page-header">
      <div class="header-left">
        <h2>权限管理</h2>
      </div>
      <div class="header-right">
        <el-button type="primary" @click="addPermission(null)">
          <el-icon><Plus /></el-icon>
          新增根权限
        </el-button>
      </div>
    </div>

    <div class="table-container">
      <el-table
        :data="permissionTree"
        v-loading="loading"
        row-key="id"
        default-expand-all
        :tree-props="{ children: 'children' }"
        stripe
      >
        <el-table-column prop="name" label="权限名称" width="180" />
        <el-table-column prop="code" label="权限编码" width="200" />
        <el-table-column prop="type" label="类型" width="100">
          <template #default="{ row }">
            <el-tag :type="row.type === 0 ? 'primary' : row.type === 1 ? 'success' : 'warning'">
              {{ row.type === 0 ? '菜单' : row.type === 1 ? '按钮' : '数据' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="path" label="路径" width="180" />
        <el-table-column prop="icon" label="图标" width="100" />
        <el-table-column prop="sort" label="排序" width="80" />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'">
              {{ row.status === 1 ? '正常' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="220">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="addPermission(row)">添加子权限</el-button>
            <el-button link type="primary" size="small" @click="editPermission(row)">编辑</el-button>
            <el-button link type="danger" size="small" @click="deletePermission(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <el-dialog v-model="showDialog" :title="isEdit ? '编辑权限' : (parentPermission ? '新增子权限' : '新增根权限')" width="500px">
      <el-form :model="permissionForm" label-position="top">
        <el-form-item label="权限名称" required>
          <el-input v-model="permissionForm.name" placeholder="请输入权限名称" />
        </el-form-item>
        <el-form-item label="权限编码" required>
          <el-input v-model="permissionForm.code" placeholder="请输入权限编码" :disabled="isEdit" />
        </el-form-item>
        <el-form-item label="类型">
          <el-radio-group v-model="permissionForm.type">
            <el-radio :value="0">菜单</el-radio>
            <el-radio :value="1">按钮</el-radio>
            <el-radio :value="2">数据</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="路径">
          <el-input v-model="permissionForm.path" placeholder="路由路径（菜单类型填写）" />
        </el-form-item>
        <el-form-item label="组件">
          <el-input v-model="permissionForm.component" placeholder="组件路径（菜单类型填写）" />
        </el-form-item>
        <el-form-item label="图标">
          <el-input v-model="permissionForm.icon" placeholder="图标名称（菜单类型填写）" />
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="permissionForm.sort" :min="0" :step="1" />
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="permissionForm.status">
            <el-radio :value="1">正常</el-radio>
            <el-radio :value="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showDialog = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  listPermissionTree,
  createPermission,
  updatePermission,
  deletePermission as deletePermissionApi
} from '@/api/admin'

const loading = ref(false)
const submitting = ref(false)
const showDialog = ref(false)
const isEdit = ref(false)
const parentPermission = ref(null)
const permissionTree = ref([])

const permissionForm = reactive({
  id: null,
  name: '',
  code: '',
  type: 0,
  parentId: 0,
  path: '',
  component: '',
  icon: '',
  sort: 0,
  status: 1
})

const loadPermissions = async () => {
  loading.value = true
  try {
    const res = await listPermissionTree()
    permissionTree.value = res.data || []
  } catch (error) {
    console.error(error)
  } finally {
    loading.value = false
  }
}

const addPermission = (parent) => {
  isEdit.value = false
  parentPermission.value = parent
  Object.assign(permissionForm, {
    id: null,
    name: '',
    code: '',
    type: parent ? 1 : 0,
    parentId: parent ? parent.id : 0,
    path: '',
    component: '',
    icon: '',
    sort: 0,
    status: 1
  })
  showDialog.value = true
}

const editPermission = (row) => {
  isEdit.value = true
  parentPermission.value = null
  Object.assign(permissionForm, {
    id: row.id,
    name: row.name,
    code: row.code,
    type: row.type,
    parentId: row.parentId,
    path: row.path,
    component: row.component,
    icon: row.icon,
    sort: row.sort,
    status: row.status
  })
  showDialog.value = true
}

const deletePermission = async (row) => {
  try {
    await ElMessageBox.confirm(`确定要删除权限 ${row.name} 吗？`, '提示', {
      type: 'warning'
    })
    await deletePermissionApi(row.id)
    ElMessage.success('删除成功')
    loadPermissions()
  } catch (error) {
    if (error !== 'cancel') console.error(error)
  }
}

const handleSubmit = async () => {
  if (!permissionForm.name.trim()) {
    ElMessage.warning('请输入权限名称')
    return
  }
  if (!permissionForm.code.trim()) {
    ElMessage.warning('请输入权限编码')
    return
  }

  submitting.value = true
  try {
    if (isEdit.value) {
      await updatePermission(permissionForm.id, permissionForm)
      ElMessage.success('更新成功')
    } else {
      await createPermission(permissionForm)
      ElMessage.success('创建成功')
    }
    showDialog.value = false
    loadPermissions()
  } catch (error) {
    console.error(error)
  } finally {
    submitting.value = false
  }
}

onMounted(() => {
  loadPermissions()
})
</script>

<style scoped>
.permission-manage {
  padding: 0;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.header-left h2 {
  margin: 0;
  font-size: 20px;
  color: #303133;
}

.table-container {
  background: white;
  border-radius: 8px;
  padding: 20px;
}
</style>
