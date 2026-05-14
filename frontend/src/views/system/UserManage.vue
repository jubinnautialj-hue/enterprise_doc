<template>
  <div class="user-manage">
    <div class="page-header">
      <div class="header-left">
        <h2>用户管理</h2>
      </div>
      <div class="header-right">
        <el-input
          v-model="keyword"
          placeholder="搜索用户名、昵称、邮箱"
          style="width: 240px"
          prefix-icon="Search"
          clearable
          @keyup.enter="loadUsers"
          @clear="loadUsers"
        />
        <el-button type="primary" @click="showCreateDialog = true">
          <el-icon><Plus /></el-icon>
          新增用户
        </el-button>
      </div>
    </div>

    <div class="table-container">
      <el-table :data="users" v-loading="loading" stripe>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="username" label="用户名" width="120" />
        <el-table-column prop="nickname" label="昵称" width="120" />
        <el-table-column prop="email" label="邮箱" width="180" />
        <el-table-column prop="phone" label="手机号" width="130" />
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
            <el-button link type="primary" size="small" @click="editUser(row)">编辑</el-button>
            <el-button link type="primary" size="small" @click="assignRoles(row)">分配角色</el-button>
            <el-button link type="warning" size="small" @click="resetPassword(row)">重置密码</el-button>
            <el-button link type="danger" size="small" @click="deleteUser(row)">删除</el-button>
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
        @current-change="loadUsers"
        @size-change="loadUsers"
      />
    </div>

    <el-dialog v-model="showCreateDialog" :title="isEdit ? '编辑用户' : '新增用户'" width="500px">
      <el-form :model="userForm" label-position="top">
        <el-form-item label="用户名" required>
          <el-input v-model="userForm.username" placeholder="请输入用户名" :disabled="isEdit" />
        </el-form-item>
        <el-form-item label="密码" v-if="!isEdit" required>
          <el-input v-model="userForm.password" type="password" placeholder="请输入密码" show-password />
        </el-form-item>
        <el-form-item label="昵称">
          <el-input v-model="userForm.nickname" placeholder="请输入昵称" />
        </el-form-item>
        <el-form-item label="邮箱">
          <el-input v-model="userForm.email" placeholder="请输入邮箱" />
        </el-form-item>
        <el-form-item label="手机号">
          <el-input v-model="userForm.phone" placeholder="请输入手机号" />
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="userForm.status">
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

    <el-dialog v-model="showRoleDialog" :title="`分配角色 - ${currentUser?.nickname || currentUser?.username}`" width="400px">
      <el-checkbox-group v-model="selectedRoleIds">
        <el-checkbox v-for="role in allRoles" :key="role.id" :value="role.id">
          {{ role.name }} ({{ role.code }})
        </el-checkbox>
      </el-checkbox-group>
      <template #footer>
        <el-button @click="showRoleDialog = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleAssignRoles">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  listUsers,
  createUser,
  updateUser,
  deleteUser as deleteUserApi,
  resetUserPassword,
  assignUserRoles,
  listAllRoles
} from '@/api/admin'

const loading = ref(false)
const submitting = ref(false)
const keyword = ref('')
const showCreateDialog = ref(false)
const showRoleDialog = ref(false)
const isEdit = ref(false)
const currentUser = ref(null)
const users = ref([])
const allRoles = ref([])
const selectedRoleIds = ref([])

const pagination = reactive({
  current: 1,
  size: 10,
  total: 0
})

const userForm = reactive({
  id: null,
  username: '',
  password: '',
  nickname: '',
  email: '',
  phone: '',
  status: 1
})

const formatDate = (date) => {
  if (!date) return ''
  return new Date(date).toLocaleString('zh-CN')
}

const loadUsers = async () => {
  loading.value = true
  try {
    const res = await listUsers({
      current: pagination.current,
      size: pagination.size,
      keyword: keyword.value
    })
    users.value = res.data.records || []
    pagination.total = res.data.total || 0
  } catch (error) {
    console.error(error)
  } finally {
    loading.value = false
  }
}

const loadAllRoles = async () => {
  try {
    const res = await listAllRoles()
    allRoles.value = res.data || []
  } catch (error) {
    console.error(error)
  }
}

const editUser = (row) => {
  isEdit.value = true
  userForm.id = row.id
  userForm.username = row.username
  userForm.nickname = row.nickname
  userForm.email = row.email
  userForm.phone = row.phone
  userForm.status = row.status
  showCreateDialog.value = true
}

const deleteUser = async (row) => {
  try {
    await ElMessageBox.confirm(`确定要删除用户 ${row.username} 吗？`, '提示', {
      type: 'warning'
    })
    await deleteUserApi(row.id)
    ElMessage.success('删除成功')
    loadUsers()
  } catch (error) {
    if (error !== 'cancel') console.error(error)
  }
}

const resetPassword = async (row) => {
  try {
    const { value } = await ElMessageBox.prompt('请输入新密码', '重置密码', {
      inputType: 'password',
      inputPattern: /\S/,
      inputErrorMessage: '密码不能为空'
    })
    await resetUserPassword(row.id, value)
    ElMessage.success('密码重置成功')
  } catch (error) {
    if (error !== 'cancel') console.error(error)
  }
}

const assignRoles = async (row) => {
  currentUser.value = row
  selectedRoleIds.value = []
  try {
    const res = await fetch(`${import.meta.env.VITE_API_BASE || ''}/api/admin/users/${row.id}/roles`, {
      headers: { 'Authorization': `Bearer ${localStorage.getItem('token')}` }
    })
    const data = await res.json()
    if (data.code === 200) {
      selectedRoleIds.value = (data.data || []).map(r => r.id)
    }
  } catch (error) {
    console.error(error)
  }
  showRoleDialog.value = true
}

const handleAssignRoles = async () => {
  submitting.value = true
  try {
    await assignUserRoles(currentUser.value.id, selectedRoleIds.value)
    ElMessage.success('角色分配成功')
    showRoleDialog.value = false
  } catch (error) {
    console.error(error)
  } finally {
    submitting.value = false
  }
}

const handleSubmit = async () => {
  if (!userForm.username.trim()) {
    ElMessage.warning('请输入用户名')
    return
  }
  if (!isEdit.value && !userForm.password) {
    ElMessage.warning('请输入密码')
    return
  }

  submitting.value = true
  try {
    if (isEdit.value) {
      await updateUser(userForm.id, {
        nickname: userForm.nickname,
        email: userForm.email,
        phone: userForm.phone,
        status: userForm.status
      })
      ElMessage.success('更新成功')
    } else {
      await createUser(userForm)
      ElMessage.success('创建成功')
    }
    showCreateDialog.value = false
    loadUsers()
  } catch (error) {
    console.error(error)
  } finally {
    submitting.value = false
  }
}

showCreateDialog.value = false
isEdit.value = false
Object.assign(userForm, {
  id: null,
  username: '',
  password: '',
  nickname: '',
  email: '',
  phone: '',
  status: 1
})

onMounted(() => {
  loadUsers()
  loadAllRoles()
})
</script>

<style scoped>
.user-manage {
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
