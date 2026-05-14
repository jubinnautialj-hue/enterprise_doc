import request from '@/utils/request'

export function listUsers(params) {
  return request({
    url: '/admin/users',
    method: 'get',
    params
  })
}

export function getUser(id) {
  return request({
    url: '/admin/users/' + id,
    method: 'get'
  })
}

export function createUser(data) {
  return request({
    url: '/admin/users',
    method: 'post',
    data
  })
}

export function updateUser(id, data) {
  return request({
    url: '/admin/users/' + id,
    method: 'put',
    data
  })
}

export function deleteUser(id) {
  return request({
    url: '/admin/users/' + id,
    method: 'delete'
  })
}

export function resetUserPassword(id, password) {
  return request({
    url: `/admin/users/${id}/reset-password`,
    method: 'post',
    data: { password }
  })
}

export function assignUserRoles(id, roleIds) {
  return request({
    url: `/admin/users/${id}/roles`,
    method: 'post',
    data: { roleIds }
  })
}

export function listRoles(params) {
  return request({
    url: '/admin/roles',
    method: 'get',
    params
  })
}

export function listAllRoles() {
  return request({
    url: '/admin/roles/all',
    method: 'get'
  })
}

export function getRole(id) {
  return request({
    url: '/admin/roles/' + id,
    method: 'get'
  })
}

export function createRole(data) {
  return request({
    url: '/admin/roles',
    method: 'post',
    data
  })
}

export function updateRole(id, data) {
  return request({
    url: '/admin/roles/' + id,
    method: 'put',
    data
  })
}

export function deleteRole(id) {
  return request({
    url: '/admin/roles/' + id,
    method: 'delete'
  })
}

export function assignRolePermissions(id, permissionIds) {
  return request({
    url: `/admin/roles/${id}/permissions`,
    method: 'post',
    data: { permissionIds }
  })
}

export function listPermissionTree() {
  return request({
    url: '/admin/permissions/tree',
    method: 'get'
  })
}

export function createPermission(data) {
  return request({
    url: '/admin/permissions',
    method: 'post',
    data
  })
}

export function updatePermission(id, data) {
  return request({
    url: '/admin/permissions/' + id,
    method: 'put',
    data
  })
}

export function deletePermission(id) {
  return request({
    url: '/admin/permissions/' + id,
    method: 'delete'
  })
}

export function getDirectoryPermissions(targetType, targetId) {
  return request({
    url: '/directory-permissions',
    method: 'get',
    params: { targetType, targetId }
  })
}

export function addDirectoryPermission(data) {
  return request({
    url: '/directory-permissions',
    method: 'post',
    data
  })
}

export function updateDirectoryPermission(id, permissionType) {
  return request({
    url: '/directory-permissions/' + id,
    method: 'put',
    data: { permissionType }
  })
}

export function deleteDirectoryPermission(id) {
  return request({
    url: '/directory-permissions/' + id,
    method: 'delete'
  })
}

export function checkDirectoryPermission(targetType, targetId) {
  return request({
    url: '/directory-permissions/check',
    method: 'get',
    params: { targetType, targetId }
  })
}
