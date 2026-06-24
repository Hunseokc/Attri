// src/api/index.js
import axios from 'axios';

const api = axios.create({
  baseURL: 'http://localhost:8080', // Spring Boot 로컬 서버 주소
  withCredentials: true, // Spring Boot의 allowCredentials(true)와 세트
  headers: {
    'Content-Type': 'application/json',
  },
});

export default api;