<template>
  <div class="role-manage">
    <div class="page-header">
      <div class="header-left">
        <h2>角色管理</h2>
      </div>
      <div class="header-right">
        <el-input
          v-model="keyword"
          placeholder="搜索角色名称、编码"
          style="width: 240px"
          prefix-icon="Search"
          clearable
          @keyup.enter="loadRoles"
          @clear="loadRoles"
        />
        <el-button type="primary" @click="showCreateDialog = true">
          <el-icon><Plus /></el-icon>
          新增角色
        </el-button>
      </div>
    </div>

    <div class="table-container">
      <el-table :data="roles" v-loading="loading" stripe>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="name" label="角色名称" width="120" />
        <el-table-column prop="code" label="角色编码" width="150" />
        <el-table-column prop="description" label="描述" />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'">
              {{ row.status === 1 ? '正常' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180">
          <template #default="{ row }">
            {{ formatDate(row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="280">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="editRole(row)">编辑</el-button>
            <el-button link type="primary" size="small" @click="assignPermissions(row)">分配权限</el-button>
            <el-button link type="danger" size="small" @click="deleteRole(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-if="pagination.total > 0"
        class="pagination"
        v-model:current-page="pagination.current"
        v-model:page-size="pagination.size"
        :total="pagination.total"
        :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next"
        @current-change="loadRoles"
        @size-change="loadRoles"
      />
    </div>

    <el-dialog v-model="showCreateDialog" :title="isEdit ? '编辑角色' : '新增角色'" width="500px">
      <el-form :model="roleForm" label-position="top">
        <el-form-item label="角色名称" required>
          <el-input v-model="roleForm.name" placeholder="请输入角色名称" />
        </el-form-item>
        <el-form-item label="角色编码" required>
          <el-input v-model="roleForm.code" placeholder="请输入角色编码" :disabled="isEdit" />
        </el-form-item>
        <el-form-item label="角色描述">
          <el-input
            v-model="roleForm.description"
            type="textarea"
            :rows="3"
            placeholder="请输入角色描述"
          />
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="roleForm.status">
            <el-radio :value="1">正常</el-radio>
            <el-radio :value="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showCreateDialog = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="showPermissionDialog" :title="`分配权限 - ${currentRole?.name}`" width="600px">
      <el-tree
        ref="permissionTreeRef"
        :data="permissionTree"
        :props="{ label: 'name', children: 'children' }"
        show-checkbox
        node-key="id"
        :default-checked-keys="checkedPermissionIds"
      />
      <template #footer>
        <el-button @click="showPermissionDialog = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleAssignPermissions">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  listRoles,
  createRole,
  updateRole,
  deleteRole as deleteRoleApi,
  assignRolePermissions,
  listPermissionTree,
  getRole
} from '@/api/admin'

const loading = ref(false)
const submitting = ref(false)
const keyword = ref('')
const showCreateDialog = ref(false)
const showPermissionDialog = ref(false)
const isEdit = ref(false)
const currentRole = ref(null)
const roles = ref([])
const permissionTree = ref([])
const checkedPermissionIds = ref([])
const permissionTreeRef = ref(null)

const pagination = reactive({
  current: 1,
  size: 10,
  total: 0
})

const roleForm = reactive({
  id: null,
  name: '',
  code: '',
  description: '',
  status: 1
})

const formatDate = (date) => {
  if (!date) return ''
  return new Date(date).toLocaleString('zh-CN')
}

const loadRoles = async () => {
  loading.value = true
  try {
    const res = await listRoles({
      current: pagination.current,
      size: pagination.size,
      keyword: keyword.value
    })
    roles.value = res.data.records || []
    pagination.total = res.data.total || 0
  } catch (error) {
    console.error(error)
  } finally {
    loading.value = false
  }
}

const loadPermissionTree = async () => {
  try {
    const res = await listPermissionTree()
    permissionTree.value = res.data || []
  } catch (error) {
    console.error(error)
  }
}

const editRole = (row) => {
  isEdit.value = true
  roleForm.id = row.id
  roleForm.name = row.name
  roleForm.code = row.code
  roleForm.description = row.description
  roleForm.status = row.status
  showCreateDialog.value = true
}

const deleteRole = async (row) => {
  try {
    await ElMessageBox.confirm(`确定要删除角色 ${row.name} 吗？`, '提示', {
      type: 'warning'
    })
    await deleteRoleApi(row.id)
    ElMessage.success('删除成功')
    loadRoles()
  } catch (error) {
    if (error !== 'cancel') console.error(error)
  }
}

const assignPermissions = async (row) => {
  currentRole.value = row
  checkedPermissionIds.value = []
  try {
    const res = await getRole(row.id)
    const permissions = res.data?.permissions || []
    checkedPermissionIds.value = permissions.map(p => p.id)
  } catch (error) {
    console.error(error)
  }
  showPermissionDialog.value = true
}

const handleAssignPermissions = async () => {
  if (permissionTreeRef.value) {
    const checked = permissionTreeRef.value.getCheckedKeys()
    const halfChecked = permissionTreeRef.value.getHalfCheckedKeys()
    const allIds = [...checked, ...halfChecked]
    
    submitting.value = true
    try {
      await assignRolePermissions(currentRole.value.id, allIds)
      ElMessage.success('权限分配成功')
      showPermissionDialog.value = false
    } catch (error) {
      console.error(error)
    } finally {
      submitting.value = false
    }
  }
}

const handleSubmit = async () => {
  if (!roleForm.name.trim()) {
    ElMessage.warning('请输入角色名称')
    return
  }
  if (!roleForm.code.trim()) {
    ElMessage.warning('请输入角色编码')
    return
  }

  submitting.value = true
  try {
    if (isEdit.value) {
      await updateRole(roleForm.id, roleForm)
      ElMessage.success('更新成功')
    } else {
      await createRole(roleForm)
      ElMessage.success('创建成功')
    }
    showCreateDialog.value = false
    loadRoles()
  } catch (error) {
    console.error(error)
  } finally {
    submitting.value = false
  }
}

showCreateDialog.value = false
isEdit.value = false
Object.assign(roleForm, {
  id: null,
  name: '',
  code: '',
  description: '',
  status: 1
})

onMounted(() => {
  loadRoles()
  loadPermissionTree()
})
</script>

<style scoped>
.role-manage {
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

.header-right {
  display: flex;
  gap: 12px;
}

.table-container {
  background: white;
  border-radius: 8px;
  padding: 20px;
}

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}
</style>
