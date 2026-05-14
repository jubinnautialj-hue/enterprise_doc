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

export function searchDocuments(params) {
  return request({
    url: '/document/search',
    method: 'get',
    params
  })
}

export function importDocuments(data, onProgress) {
  return request({
    url: '/document/import',
    method: 'post',
    data,
    headers: {
      'Content-Type': 'multipart/form-data'
    },
    onUploadProgress: onProgress
  })
}

export function createEmptyDocument(data) {
  return request({
    url: '/document/create-empty',
    method: 'post',
    params: data
  })
}

export function getDocumentContent(id) {
  return request({
    url: '/document/' + id + '/content',
    method: 'get'
  })
}

export function saveDocumentContent(id, content) {
  return request({
    url: '/document/' + id + '/content',
    method: 'post',
    data: { content }
  })
}

export function getOnlyOfficeConfig(id, canEdit = true) {
  return request({
    url: '/onlyoffice/config/' + id,
    method: 'get',
    params: { canEdit }
  })
}

export function checkOnlyOfficeSupport(id) {
  return request({
    url: '/onlyoffice/support/' + id,
    method: 'get'
  })
}
