import request from '@/utils/request'

export function getFoldersByLibrary(libraryId) {
  return request({
    url: '/folder/library/' + libraryId,
    method: 'get'
  })
}

export function getFoldersByParent(libraryId, parentId) {
  return request({
    url: `/folder/library/${libraryId}/parent/${parentId || 0}`,
    method: 'get'
  })
}

export function createFolder(data) {
  return request({
    url: '/folder',
    method: 'post',
    data
  })
}

export function deleteFolder(id) {
  return request({
    url: '/folder/' + id,
    method: 'delete'
  })
}

export function renameFolder(id, name) {
  return request({
    url: `/folder/${id}/rename`,
    method: 'put',
    params: { name }
  })
}
