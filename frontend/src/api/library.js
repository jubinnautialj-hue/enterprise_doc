import request from '@/utils/request'

export function listPublicLibraries(params) {
  return request({
    url: '/library/public',
    method: 'get',
    params
  })
}

export function listPersonalLibraries(params) {
  return request({
    url: '/library/personal',
    method: 'get',
    params
  })
}

export function getLibrary(id) {
  return request({
    url: '/library/' + id,
    method: 'get'
  })
}

export function createLibrary(data) {
  return request({
    url: '/library',
    method: 'post',
    data
  })
}

export function updateLibrary(id, data) {
  return request({
    url: '/library/' + id,
    method: 'put',
    data
  })
}

export function deleteLibrary(id) {
  return request({
    url: '/library/' + id,
    method: 'delete'
  })
}
