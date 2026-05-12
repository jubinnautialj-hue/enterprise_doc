import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login.vue'),
    meta: { title: '登录', guest: true }
  },
  {
    path: '/',
    component: () => import('@/views/Layout.vue'),
    redirect: '/libraries',
    children: [
      {
        path: 'libraries',
        name: 'Libraries',
        component: () => import('@/views/Libraries.vue'),
        meta: { title: '全部文库' }
      },
      {
        path: 'personal',
        name: 'PersonalLibraries',
        component: () => import('@/views/PersonalLibraries.vue'),
        meta: { title: '个人文库' }
      },
      {
        path: 'library/:id',
        name: 'LibraryDetail',
        component: () => import('@/views/LibraryDetail.vue'),
        meta: { title: '文库详情' }
      }
    ]
  },
  {
    path: '/share/:code',
    name: 'ShareView',
    component: () => import('@/views/ShareView.vue'),
    meta: { title: '分享文档', guest: true }
  },
  {
    path: '/preview/:id',
    name: 'Preview',
    component: () => import('@/views/Preview.vue'),
    meta: { title: '文档预览' }
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  document.title = to.meta.title ? to.meta.title + ' - 企业文档管理系统' : '企业文档管理系统'

  const token = localStorage.getItem('token')

  if (to.meta.guest) {
    next()
  } else if (!token) {
    next({ path: '/login', query: { redirect: to.fullPath } })
  } else {
    next()
  }
})

export default router
