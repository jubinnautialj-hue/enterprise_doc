import request from '@/utils/request'

export function listDocuments(params) {
  return request({
    url: '/document/list',
    method: 'get',
    params
  })
}

export function getDocument(id) {
  return request({
    url: '/document/' + id,
    method: 'get'
  })
}

export function uploadDocument(data, onProgress) {
  return request({
    url: '/document/upload',
    method: 'post',
    data,
    headers: {
      'Content-Type': 'multipart/form-data'
    },
    onUploadProgress: onProgress
  })
}

export function updateDocument(id, data, onProgress) {
  return request({
    url: `/document/${id}/update`,
    method: 'post',
    data,
    headers: {
      'Content-Type': 'multipart/form-data'
    },
    onUploadProgress: onProgress
  })
}

export function deleteDocument(id) {
  return request({
    url: '/document/' + id,
    method: 'delete'
  })
}

export function renameDocument(id, name) {
  return request({
    url: `/document/${id}/rename`,
    method: 'put',
    params: { name }
  })
}

export function moveDocument(id, folderId) {
  return request({
    url: `/document/${id}/move`,
    method: 'put',
    params: { folderId }
  })
}

export function createShare(data) {
  return request({
    url: '/share',
    method: 'post',
    data
  })
}

export function cancelShare(id) {
  return request({
    url: '/share/' + id,
    method: 'delete'
  })
}

export function verifyShare(data) {
  return request({
    url: '/share/verify',
    method: 'post',
    data
  })
}

export function getShareDocument(shareCode) {
  return request({
    url: '/share/document/' + shareCode,
    method: 'get'
  })
}
