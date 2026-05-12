import { defineStore } from 'pinia'
import { ref } from 'vue'
import { login, register, getCurrentUser } from '@/api/auth'

export const useUserStore = defineStore('user', () => {
  const token = ref(localStorage.getItem('token') || '')
  const user = ref(JSON.parse(localStorage.getItem('user') || 'null'))

  const setToken = (newToken) => {
    token.value = newToken
    localStorage.setItem('token', newToken)
  }

  const setUser = (newUser) => {
    user.value = newUser
    localStorage.setItem('user', JSON.stringify(newUser))
  }

  const logout = () => {
    token.value = ''
    user.value = null
    localStorage.removeItem('token')
    localStorage.removeItem('user')
  }

  const handleLogin = async (form) => {
    const res = await login(form)
    setToken(res.data.token)
    setUser(res.data.user)
    return res.data
  }

  const handleRegister = async (form) => {
    return await register(form)
  }

  const fetchCurrentUser = async () => {
    const res = await getCurrentUser()
    setUser(res.data)
    return res.data
  }

  return {
    token,
    user,
    setToken,
    setUser,
    logout,
    handleLogin,
    handleRegister,
    fetchCurrentUser
  }
})
