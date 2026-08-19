<template>
  <div class="login-container">
    <div class="login-card">
      <div class="logo-area">
        <Sparkles class="icon logo-icon" />
        <h1 class="logo-text">SQUARE</h1>
      </div>
      
      <h2 class="title">{{ isLogin ? 'Welcome Back' : 'Join SQUARE' }}</h2>
      <p class="subtitle">{{ isLogin ? 'Please log in to continue' : 'Create an account to start sharing' }}</p>
      
      <form @submit.prevent="handleSubmit" class="login-form">
        <div v-if="!isLogin" class="input-group">
          <label>Username</label>
          <div class="input-wrapper">
            <User class="input-icon" />
            <input type="text" v-model="formData.username" placeholder="Username" required />
          </div>
        </div>

        <div class="input-group">
          <label>Email</label>
          <div class="input-wrapper">
            <Mail class="input-icon" />
            <input type="email" v-model="formData.email" placeholder="you@example.com" required />
          </div>
        </div>

        <div class="input-group">
          <label>Password</label>
          <div class="input-wrapper">
            <Lock class="input-icon" />
            <input type="password" v-model="formData.password" placeholder="••••••••" required />
          </div>
        </div>

        <div v-if="errorMessage" class="error-message">
          {{ errorMessage }}
        </div>

        <button type="submit" class="submit-btn" :disabled="isLoading">
          <span v-if="isLoading" class="spinner"></span>
          <span v-else>{{ isLogin ? 'Log In' : 'Sign Up' }}</span>
        </button>
      </form>

      <div class="toggle-mode">
        <p>
          {{ isLogin ? "Don't have an account?" : "Already have an account?" }}
          <a href="#" @click.prevent="toggleMode">{{ isLogin ? 'Sign up' : 'Log in' }}</a>
        </p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { Sparkles, Mail, Lock, User } from 'lucide-vue-next'
import api from '../api/axios'

const router = useRouter()
const isLogin = ref(true)
const isLoading = ref(false)
const errorMessage = ref('')

const formData = ref({
  username: '',
  email: '',
  password: '',
})

const toggleMode = () => {
  isLogin.value = !isLogin.value
  errorMessage.value = ''
  formData.value = { username: '', email: '', password: '' }
}

const handleSubmit = async () => {
  isLoading.value = true
  errorMessage.value = ''

  try {
    if (isLogin.value) {
      // 로그인 처리
      const response = await api.post('/users/login', {
        email: formData.value.email,
        password: formData.value.password,
      })
      
      const { accessToken, refreshToken } = response.data
      
      // 로컬 스토리지에 토큰 및 정보 저장
      localStorage.setItem('accessToken', accessToken)
      localStorage.setItem('refreshToken', refreshToken)
      localStorage.setItem('userEmail', formData.value.email)
      
      // 메인 화면으로 이동
      router.push('/')
    } else {
      // 회원가입 처리
      await api.post('/users/signup', {
        username: formData.value.username,
        email: formData.value.email,
        password: formData.value.password,
      })
      
      alert('회원가입이 완료되었습니다. 로그인해주세요.')
      toggleMode() // 로그인 모드로 전환
    }
  } catch (error) {
    if (error.response && error.response.data) {
      errorMessage.value = typeof error.response.data === 'string' 
        ? error.response.data 
        : error.response.data.message || '인증에 실패했습니다.'
    } else {
      errorMessage.value = '서버와 통신할 수 없습니다.'
    }
  } finally {
    isLoading.value = false
  }
}
</script>

<style scoped>
.login-container {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 100vh;
  background: radial-gradient(circle at top right, #1a1a2e 0%, #0f0f0f 100%);
  position: relative;
  overflow: hidden;
}

/* Background animated glow effects */
.login-container::before,
.login-container::after {
  content: '';
  position: absolute;
  width: 400px;
  height: 400px;
  border-radius: 50%;
  filter: blur(100px);
  opacity: 0.15;
  z-index: 0;
}

.login-container::before {
  top: -100px;
  left: -100px;
  background: #ff2a5f;
  animation: float 8s ease-in-out infinite;
}

.login-container::after {
  bottom: -100px;
  right: -100px;
  background: #4a00e0;
  animation: float 10s ease-in-out infinite reverse;
}

@keyframes float {
  0% { transform: translate(0, 0); }
  50% { transform: translate(30px, 30px); }
  100% { transform: translate(0, 0); }
}

.login-card {
  position: relative;
  z-index: 1;
  width: 100%;
  max-width: 400px;
  padding: 48px;
  background: rgba(25, 25, 25, 0.4);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border: 1px solid rgba(255, 255, 255, 0.05);
  border-radius: 24px;
  box-shadow: 0 25px 50px -12px rgba(0, 0, 0, 0.5);
}

.logo-area {
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 32px;
}

.logo-icon {
  color: #fff;
  width: 28px;
  height: 28px;
}

.logo-text {
  font-size: 24px;
  font-weight: 900;
  letter-spacing: 3px;
  margin: 0 0 0 12px;
  color: #fff;
}

.title {
  font-size: 28px;
  font-weight: 700;
  margin: 0 0 8px 0;
  color: #fff;
  text-align: center;
}

.subtitle {
  font-size: 14px;
  color: #a0a0a0;
  margin: 0 0 32px 0;
  text-align: center;
}

.login-form {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.input-group {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.input-group label {
  font-size: 13px;
  font-weight: 600;
  color: #d0d0d0;
  padding-left: 4px;
}

.input-wrapper {
  position: relative;
  display: flex;
  align-items: center;
}

.input-icon {
  position: absolute;
  left: 16px;
  color: #666;
  width: 20px;
  height: 20px;
  transition: color 0.3s;
}

.input-wrapper input {
  width: 100%;
  padding: 14px 16px 14px 48px;
  background: rgba(0, 0, 0, 0.2);
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 12px;
  color: #fff;
  font-size: 15px;
  transition: all 0.3s ease;
}

.input-wrapper input:focus {
  outline: none;
  border-color: #ff2a5f;
  background: rgba(0, 0, 0, 0.4);
}

.input-wrapper input:focus + .input-icon {
  color: #ff2a5f;
}

.error-message {
  padding: 12px;
  background: rgba(255, 42, 95, 0.1);
  border-left: 4px solid #ff2a5f;
  border-radius: 8px;
  color: #ff2a5f;
  font-size: 14px;
  text-align: left;
}

.submit-btn {
  margin-top: 12px;
  padding: 16px;
  border: none;
  border-radius: 12px;
  background: linear-gradient(135deg, #ff2a5f 0%, #ff5e3a 100%);
  color: #fff;
  font-size: 16px;
  font-weight: 700;
  cursor: pointer;
  transition: transform 0.2s, box-shadow 0.2s;
  display: flex;
  justify-content: center;
  align-items: center;
  height: 52px;
}

.submit-btn:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 10px 20px rgba(255, 42, 95, 0.3);
}

.submit-btn:active:not(:disabled) {
  transform: translateY(0);
}

.submit-btn:disabled {
  opacity: 0.7;
  cursor: not-allowed;
}

.spinner {
  width: 20px;
  height: 20px;
  border: 3px solid rgba(255,255,255,0.3);
  border-radius: 50%;
  border-top-color: #fff;
  animation: spin 1s ease-in-out infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

.toggle-mode {
  margin-top: 24px;
  text-align: center;
  font-size: 14px;
  color: #a0a0a0;
}

.toggle-mode a {
  color: #fff;
  font-weight: 600;
  text-decoration: none;
  margin-left: 4px;
  transition: color 0.2s;
}

.toggle-mode a:hover {
  color: #ff2a5f;
}
</style>
