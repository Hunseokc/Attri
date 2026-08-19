import axios from 'axios'
import router from '../router'

const api = axios.create({
  baseURL: 'http://localhost:8080/api',
  headers: {
    'Content-Type': 'application/json',
  },
})

// 요청(Request) 인터셉터: 토큰을 헤더에 자동 주입
api.interceptors.request.use(
  (config) => {
    const accessToken = localStorage.getItem('accessToken')
    if (accessToken) {
      config.headers.Authorization = `Bearer ${accessToken}`
    }
    return config
  },
  (error) => {
    return Promise.reject(error)
  },
)

// 응답(Response) 인터셉터: 401 에러 시 리프레시 토큰으로 자동 갱신
api.interceptors.response.use(
  (response) => {
    return response
  },
  async (error) => {
    const originalRequest = error.config

    if (error.response && error.response.status === 401 && !originalRequest._retry) {
      originalRequest._retry = true
      const refreshToken = localStorage.getItem('refreshToken')
      const email = localStorage.getItem('userEmail') // 저장해둔 이메일 필요

      if (refreshToken && email) {
        try {
          // 토큰 갱신 API 호출
          const res = await axios.post('http://localhost:8080/api/users/refresh', {
            email,
            refreshToken,
          })

          const newAccessToken = res.data.accessToken
          const newRefreshToken = res.data.refreshToken

          localStorage.setItem('accessToken', newAccessToken)
          localStorage.setItem('refreshToken', newRefreshToken)

          // 원래 하려던 요청에 새 토큰을 넣어 재시도
          originalRequest.headers.Authorization = `Bearer ${newAccessToken}`
          return api(originalRequest)
        } catch (refreshError) {
          // 리프레시 토큰도 만료되었거나 유효하지 않으면 로그아웃 처리
          console.error('리프레시 토큰 만료', refreshError)
          localStorage.removeItem('accessToken')
          localStorage.removeItem('refreshToken')
          localStorage.removeItem('userEmail')
          router.push('/login')
        }
      } else {
        // 토큰이 없으면 로그인 페이지로 이동
        router.push('/login')
      }
    }
    return Promise.reject(error)
  },
)

export default api
