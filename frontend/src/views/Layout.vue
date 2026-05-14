<template>
  <el-container class="layout-container">
    <el-aside width="220px" class="sidebar">
      <div class="logo">
        <el-icon :size="28"><Document /></el-icon>
        <span>企业文档</span>
      </div>

      <el-menu
        :default-active="activeMenu"
        router
        class="menu"
        background-color="#304156"
        text-color="#bfcbd9"
        active-text-color="#409eff"
      >
        <el-menu-item index="/libraries">
          <el-icon><Collection /></el-icon>
          <span>全部文库</span>
        </el-menu-item>
        <el-menu-item index="/personal">
          <el-icon><UserFilled /></el-icon>
          <span>个人文库</span>
        </el-menu-item>
        <el-sub-menu index="admin">
          <template #title>
            <el-icon><Setting /></el-icon>
            <span>系统管理</span>
          </template>
          <el-menu-item index="/admin/users">
            <el-icon><User /></el-icon>
            <span>用户管理</span>
          </el-menu-item>
          <el-menu-item index="/admin/roles">
            <el-icon><UserFilled /></el-icon>
            <span>角色管理</span>
          </el-menu-item>
          <el-menu-item index="/admin/permissions">
            <el-icon><Key /></el-icon>
            <span>权限管理</span>
          </el-menu-item>
        </el-sub-menu>
      </el-menu>
    </el-aside>

    <el-container>
      <el-header class="header">
        <div class="header-left">
          <el-breadcrumb separator="/">
            <el-breadcrumb-item v-for="(item, index) in breadcrumbs" :key="index">
              <template v-if="index === breadcrumbs.length - 1">
                {{ item.name }}
              </template>
              <router-link v-else :to="item.path">{{ item.name }}</router-link>
            </el-breadcrumb-item>
          </el-breadcrumb>
        </div>

        <div class="header-center">
          <el-input
            v-model="searchKeyword"
            placeholder="搜索文档（按名称或内容）"
            style="width: 400px"
            prefix-icon="Search"
            clearable
            @keyup.enter="handleSearch"
            @clear="handleSearch"
          >
            <template #append>
              <el-button :icon="Search" @click="handleSearch" />
            </template>
          </el-input>
        </div>

        <div class="header-right">
          <el-dropdown @command="handleCommand">
            <span class="user-info">
              <el-avatar :size="32" icon="UserFilled" />
              <span class="username">{{ user?.nickname || user?.username }}</span>
              <el-icon><ArrowDown /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="logout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>

      <el-main class="main">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { computed, ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const user = ref(userStore.user)
const searchKeyword = ref('')

const activeMenu = computed(() => {
  if (route.path.startsWith('/admin/users')) {
    return '/admin/users'
  }
  if (route.path.startsWith('/admin/roles')) {
    return '/admin/roles'
  }
  if (route.path.startsWith('/admin/permissions')) {
    return '/admin/permissions'
  }
  if (route.path.startsWith('/personal') || route.path === '/personal') {
    return '/personal'
  }
  return '/libraries'
})

const breadcrumbs = computed(() => {
  const crumbs = [{ name: '首页', path: '/' }]
  
  if (route.path === '/libraries') {
    crumbs.push({ name: '全部文库' })
  } else if (route.path === '/personal') {
    crumbs.push({ name: '个人文库' })
  } else if (route.path.startsWith('/library/')) {
    const libId = route.params.id
    crumbs.push({ name: '文库详情' })
  } else if (route.path === '/search') {
    crumbs.push({ name: '搜索结果' })
  } else if (route.path.startsWith('/admin/')) {
    crumbs.push({ name: '系统管理' })
    if (route.path === '/admin/users') {
      crumbs.push({ name: '用户管理' })
    } else if (route.path === '/admin/roles') {
      crumbs.push({ name: '角色管理' })
    } else if (route.path === '/admin/permissions') {
      crumbs.push({ name: '权限管理' })
    }
  }
  
  return crumbs
})

const handleCommand = (command) => {
  if (command === 'logout') {
    userStore.logout()
    router.push('/login')
  }
}

const handleSearch = () => {
  if (searchKeyword.value && searchKeyword.value.trim()) {
    router.push({
      path: '/search',
      query: { keyword: searchKeyword.value.trim() }
    })
  }
}

onMounted(async () => {
  if (!user.value && userStore.token) {
    try {
      await userStore.fetchCurrentUser()
      user.value = userStore.user
    } catch (error) {
      console.error(error)
    }
  }
})
</script>

<style scoped>
.layout-container {
  height: 100vh;
}

.sidebar {
  background: #304156;
  display: flex;
  flex-direction: column;
}

.logo {
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  color: white;
  font-size: 18px;
  font-weight: bold;
  border-bottom: 1px solid #1f2d3d;
}

.menu {
  border-right: none;
  flex: 1;
}

.header {
  background: white;
  display: flex;
  align-items: center;
  gap: 20px;
  border-bottom: 1px solid #e4e7ed;
  padding: 0 20px;
}

.header-left {
  flex: 0 0 auto;
}

.header-center {
  flex: 1;
  display: flex;
  justify-content: center;
}

.header-right {
  flex: 0 0 auto;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
}

.username {
  max-width: 120px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.main {
  background: #f5f7fa;
  padding: 20px;
}
</style>
