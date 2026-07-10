<template>
  <div class="app-container">
    <div class="sidebar-spacer">
      <aside class="sidebar">
        <div class="logo">
          <Sparkles class="icon logo-icon" />
          <h1 class="menu-text">SQUARE</h1>
        </div>

        <nav class="nav-container">
          <ul class="nav-links">
            <li class="active">
              <Home class="icon" />
              <span class="menu-text">Home</span>
            </li>
            <li>
              <Compass class="icon" />
              <span class="menu-text">Explore</span>
            </li>
            <li>
              <Bookmark class="icon" />
              <span class="menu-text">My Board</span>
            </li>
            <li>
              <MessageSquare class="icon" />
              <span class="menu-text">Messages</span>
            </li>
            <li>
              <User class="icon" />
              <span class="menu-text">Profile</span>
            </li>
          </ul>
        </nav>

        <button class="create-btn" @click="showModal = true">
          <Plus class="icon" />
          <span class="menu-text">Create</span>
        </button>
      </aside>
    </div>

    <main class="main-content">
      <header class="search-header">
        <div class="search-wrapper">
          <Search class="search-icon" />
          <input type="text" placeholder="무엇을 찾고 있나요? (음악, 일러스트, 시티팝...)" />
        </div>
      </header>

      <div class="scrollable-content">
        <div class="masonry-grid">
          <div
            v-for="feed in mixedFeeds"
            :key="feed.id"
            class="pin-card"
            :style="{ height: feed.height + 'px' }"
            @click="openDetail(feed)"
          >
            <img
              v-if="feed.imageUrl"
              :src="'http://localhost:8080' + feed.imageUrl"
              class="pin-image"
              alt="Artwork"
            />

            <div class="media-badge">{{ getBadge(feed.type) }}</div>
            <div v-if="feed.isCollab" class="collab-badge">🤝</div>

            <div class="pin-content">
              <h3>{{ feed.title }}</h3>
              <p class="tag">{{ feed.tag }}</p>
            </div>

            <div class="card-actions">
              <button
                class="action-btn save-btn"
                :class="{ 'is-saved': feed.isBookmarked }"
                @click.stop="toggleBookmark(feed)"
              >
                {{ feed.isBookmarked ? '📌 저장됨' : '📌 저장' }}
              </button>
            </div>
          </div>
        </div>
      </div>
    </main>
  </div>
  <div v-if="showModal" class="modal-overlay" @click.self="showModal = false">
    <div class="modal-content">
      <h2>✨ Create New Feed</h2>

      <div class="input-group">
        <label>이미지 첨부 (선택)</label>
        <input type="file" @change="handleFileChange" accept="image/*" />
        <img v-if="imagePreview" :src="imagePreview" class="preview-img" />
      </div>

      <div class="input-group">
        <label>작품 제목</label>
        <input type="text" v-model="formData.title" placeholder="예: 60s Psychedelic Vibe" />
      </div>

      <div class="input-group">
        <label>창작자 (닉네임)</label>
        <input type="text" v-model="formData.creator" placeholder="예: AtriinUser" />
      </div>

      <div class="input-group">
        <label>카테고리</label>
        <select v-model="formData.type">
          <option value="art">🎨 Art (일러스트, 디자인)</option>
          <option value="music">🎵 Music (비트, 믹싱)</option>
          <option value="video">🎬 Video (뮤비, 스케치)</option>
        </select>
      </div>

      <div class="input-group">
        <label>대표 태그 (띄어쓰기 없이)</label>
        <input type="text" v-model="formData.tag" placeholder="예: Illustration" />
      </div>

      <div class="input-group checkbox-group">
        <label>
          <input type="checkbox" v-model="formData.isCollab" />
          🤝 이 작업물로 협업할 사람을 찾습니다! (Collab 모드)
        </label>
      </div>

      <div class="modal-actions">
        <button class="cancel-btn" @click="showModal = false">취소</button>
        <button class="submit-btn" @click="createNewFeed">게시하기</button>
      </div>
    </div>
  </div>
  <div v-if="showDetailModal && selectedFeed" class="modal-overlay" @click.self="closeDetail">
    <div class="detail-modal-content">
      <button class="close-btn" @click="closeDetail">✕</button>

      <div class="detail-layout">
        <div class="detail-image-area">
          <img
            v-if="selectedFeed.imageUrl"
            :src="'http://localhost:8080' + selectedFeed.imageUrl"
            alt="Artwork Full"
          />
          <div v-else class="no-image">No Image</div>
        </div>

        <div class="detail-info-area">
          <div class="detail-header">
            <span class="media-badge">{{ getBadge(selectedFeed.type) }}</span>
            <div v-if="selectedFeed.isCollab" class="collab-badge">🤝 협업 모집중</div>
          </div>

          <h2 class="detail-title">{{ selectedFeed.title }}</h2>
          <p class="detail-creator">
            by <strong>{{ selectedFeed.creator }}</strong>
          </p>
          <p class="detail-tag">{{ selectedFeed.tag }}</p>

          <div class="detail-actions">
            <button
              class="detail-btn like-btn"
              :class="{ 'is-liked': selectedFeed.isLiked }"
              @click="toggleLike(selectedFeed)"
            >
              {{ selectedFeed.isLiked ? '❤️ 꽉 찬 하트' : '🤍 빈 하트' }}
              <span v-if="selectedFeed.likeCount > 0">({{ selectedFeed.likeCount }})</span>
            </button>

            <button
              class="detail-btn save-btn"
              :class="{ 'is-saved': selectedFeed.isBookmarked }"
              @click="toggleBookmark(selectedFeed)"
            >
              {{ selectedFeed.isBookmarked ? '📌 저장 취소' : '📌 북마크에 저장' }}
              <span v-if="selectedFeed.bookmarkCount > 0">({{ selectedFeed.bookmarkCount }})</span>
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import axios from 'axios' // axios 라이브러리 임포트
import {
  Home,
  Compass,
  Bookmark,
  MessageSquare,
  User,
  Plus,
  Sparkles,
  Search,
} from 'lucide-vue-next'

// 피드 초기값 빈 배열
const mixedFeeds = ref([])

// 글쓰기 모달 상태 관리
const showModal = ref(false)

// 파일 선택 시 미리보기 이미지 생성
const handleFileChange = (event) => {
  const file = event.target.files[0]
  if (file) {
    selectedFile.value = file
    imagePreview.value = URL.createObjectURL(file)
  }
}

// 사용자가 입력할 폼 데이터 보관함
const formData = ref({
  title: '',
  creator: '',
  type: 'art', // 기본값
  tag: '',
  isCollab: false,
})

// 상세 보기 모달 상태 관리
const showDetailModal = ref(false)
const selectedFeed = ref(null) // 클릭한 카드의 모든 정보(객체)를 통째로 보관

// 파일 보관용 변수 추가
const selectedFile = ref(null)
const imagePreview = ref(null)

// 카드 클릭 시 실행될 함수
const openDetail = (feed) => {
  selectedFeed.value = feed
  showDetailModal.value = true
}

// 닫기 함수
const closeDetail = () => {
  showDetailModal.value = false
  selectedFeed.value = null
}

// 상호작용(좋아요, 북마크) 로직
const currentUsername = 'AtriinUser' // 임시 회원(로그인 기능 구현 전까지 사용)

// 좋아요 토글 함수
const toggleLike = async (feed) => {
  try {
    const response = await axios.post(
      `http://localhost:8080/api/feeds/${feed.id}/like?username=${currentUsername}`,
    )
    // 서버에서 응답받은 상태(true/false)와 총 개수로 화면을 즉시 업데이트합니다.
    feed.isLiked = response.data.toggled
    feed.likeCount = response.data.totalCount
  } catch (error) {
    console.error('좋아요 처리 실패:', error)
  }
}

// 북마크 토글 함수
const toggleBookmark = async (feed) => {
  try {
    const response = await axios.post(
      `http://localhost:8080/api/feeds/${feed.id}/bookmark?username=${currentUsername}`,
    )
    feed.isBookmarked = response.data.toggled
    feed.bookmarkCount = response.data.totalCount
  } catch (error) {
    console.error('북마크 처리 실패:', error)
  }
}

// 화면이 처음 렌더링될 때(onMounted) 백엔드 API 호출
onMounted(async () => {
  try {
    // Spring Boot 서버 주소로 GET 요청을 보냅니다.
    const response = await axios.get('http://localhost:8080/api/feeds')

    // 성공적으로 받아온 JSON 데이터를 mixedFeeds에 덮어씌웁니다.
    mixedFeeds.value = response.data
    console.log('데이터 수신 성공:', response.data)
  } catch (error) {
    console.error('서버와 연결할 수 없습니다. CORS 설정이나 서버 실행 여부를 확인하세요.', error)
    alert('데이터를 불러오는데 실패했습니다.')
  }
})

const getBadge = (type) => {
  if (type === 'music') return '🎵 Music'
  if (type === 'video') return '🎬 Video'
  if (type === 'art') return '🎨 Art'
  return '✨'
}

// 현재 선택된 메뉴가 무엇인지 기억하는 변수
const currentMenu = ref('all')

// 💡 통합 데이터 로드 함수
const loadFeeds = async (menuType) => {
  currentMenu.value = menuType // 클릭한 메뉴(art, music 등)로 상태 변경

  try {
    let url = 'http://localhost:8080/api/feeds'

    // 카테고리 필터링일 때
    if (menuType !== 'all' && menuType !== 'bookmark') {
      url = `http://localhost:8080/api/feeds?type=${menuType}`
    }
    // 북마크 모아보기일 때
    else if (menuType === 'bookmark') {
      url = `http://localhost:8080/api/feeds/bookmarks?username=${currentUsername}`
    }

    // 서버에서 필터링된 데이터를 받아와서 화면 배열(mixedFeeds)을 통째로 갈아끼움
    const response = await axios.get(url)
    mixedFeeds.value = response.data
  } catch (error) {
    console.error('데이터 로드 실패:', error)
  }
}

// 💡 페이지가 처음 켜질 때는 전체(all) 데이터를 불러옵니다.
onMounted(() => {
  loadFeeds('all')
})

// 서버로 데이터 전송 (POST)
const createNewFeed = async () => {
  if (!formData.value.title || !formData.value.creator || !formData.value.tag) {
    alert('모든 항목을 입력해 주세요!')
    return
  }

  const randomHeight = Math.floor(Math.random() * 200) + 200

  // JSON 대신 FormData 객체에 데이터 쌓아서 보내기
  const payload = new FormData()
  payload.append('title', formData.value.title)
  payload.append('creator', formData.value.creator)
  payload.append('type', formData.value.type)
  payload.append('tag', formData.value.tag)
  payload.append('height', randomHeight)
  payload.append('isCollab', formData.value.isCollab)

  if (selectedFile.value) {
    payload.append('file', selectedFile.value) // 파일 탑재!
  }

  try {
    const response = await axios.post('http://localhost:8080/api/feeds', payload, {
      headers: { 'Content-Type': 'multipart/form-data' }, // 헤더 명시
    })

    mixedFeeds.value.unshift(response.data)

    // 초기화
    showModal.value = false
    formData.value = { title: '', creator: '', type: 'art', tag: '', isCollab: false }
    selectedFile.value = null
    imagePreview.value = null
  } catch (error) {
    console.error('저장 실패:', error)
    alert('글 작성에 실패했습니다.')
  }
}
</script>

<style scoped>
/* 전체 레이아웃 */
.app-container {
  display: flex; /* Grid에서 Flex로 변경하여 밀림 방지 */
  height: 100vh;
  background-color: #0f0f0f;
  color: #ffffff;
  overflow: hidden;
}

/* =========================================
   확장형 사이드바 (Expandable Sidebar)
   ========================================= */
.sidebar-spacer {
  width: 80px;
  flex-shrink: 0;
  position: relative;
  z-index: 50;
}

.sidebar {
  position: absolute;
  top: 0;
  left: 0;
  width: 80px;
  height: 100%;
  background-color: #161616;
  border-right: 1px solid #2a2a2a;
  display: flex;
  flex-direction: column;
  padding: 32px 0; /* 가로 패딩을 없애고 자식 요소에서 위치 제어 */
  overflow: hidden;
  white-space: nowrap;
  transition: width 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.sidebar:hover {
  width: 240px;
  box-shadow: 10px 0 30px rgba(0, 0, 0, 0.6);
}

.icon {
  width: 24px;
  height: 24px;
  flex-shrink: 0;
}

/* 💡 해결의 핵심: 글자의 공간 차지 완벽 차단 */
.menu-text {
  opacity: 0;
  width: 0; /* 대기 상태일 때 너비를 0으로 만들어 레이아웃에서 지움 */
  overflow: hidden;
  transition: all 0.3s;
  pointer-events: none;
}
.sidebar:hover .menu-text {
  opacity: 1;
  width: auto; /* 호버 시 글자 너비 복구 */
  margin-left: 16px;
}

/* 1. 로고 영역 */
.logo {
  display: flex;
  align-items: center;
  padding-left: 28px; /* 80px의 정중앙 배치 (패딩28 + 아이콘24 + 여백28 = 80) */
  margin-bottom: 48px;
  transition: padding 0.3s;
}
.sidebar:hover .logo {
  padding-left: 20px;
}
.logo-icon {
  color: #fff;
}
.logo h1 {
  font-size: 20px;
  font-weight: 900;
  letter-spacing: 2px;
  margin: 0;
}

/* 2. 네비게이션 리스트 */
.nav-container {
  width: 100%;
}
.nav-links {
  list-style: none;
  padding: 0;
  margin: 0;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

/* 💡 중앙 정렬(center) 대신 왼쪽 정렬(flex-start) + 마진/패딩으로 아이콘 위치 고정 */
.nav-links li {
  display: flex;
  align-items: center;
  justify-content: flex-start; /* 무조건 왼쪽 정렬 */
  width: calc(100% - 24px); /* 좌우 12px씩 여백 확보 */
  margin: 0 12px;
  padding: 12px 16px; /* 왼쪽 마진12 + 패딩16 = 28px (로고와 동일하게 아이콘 정중앙 위치) */
  border-radius: 12px;
  font-size: 16px;
  font-weight: 600;
  color: #a0a0a0;
  cursor: pointer;
  transition: all 0.2s;
}
.sidebar:hover .nav-links li {
  padding-left: 12px; /* 호버 시 약간 왼쪽으로 당겨서 여백 밸런스 조정 */
}
.nav-links li:hover,
.nav-links li.active {
  background-color: #242424;
  color: #ffffff;
}

/* 3. 만들기 버튼 */
.create-btn {
  display: flex;
  align-items: center;
  justify-content: center; /* 내부에 글자(width:0)가 없으므로 center 사용 가능 */
  width: 48px;
  height: 48px;
  margin: auto auto 32px auto;
  background-color: #ffffff;
  color: #000000;
  border: none;
  border-radius: 24px;
  font-weight: bold;
  font-size: 16px;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  padding: 0;
}
.sidebar:hover .create-btn {
  width: calc(100% - 32px); /* 240px 안에서 양옆 16px씩 여백 */
  justify-content: flex-start;
  padding-left: 16px;
  border-radius: 12px;
}
.create-btn .menu-text {
  display: none;
}
.sidebar:hover .create-btn .menu-text {
  display: inline-block;
}

/* =========================================
   메인 컨텐츠 영역 (기존 유지)
   ========================================= */
.main-content {
  flex: 1; /* 남은 영역 모두 차지 */
  display: flex;
  flex-direction: column;
  overflow: hidden;
}
.search-header {
  padding: 24px 40px;
  display: flex;
  justify-content: center;
  background-color: rgba(15, 15, 15, 0.9);
  backdrop-filter: blur(8px);
  position: sticky;
  top: 0;
  z-index: 10;
}
.search-wrapper {
  display: flex;
  align-items: center;
  background-color: #242424;
  border-radius: 30px;
  padding: 14px 24px;
  width: 100%;
  max-width: 700px;
  transition: background-color 0.2s;
}
.search-wrapper:focus-within {
  background-color: #333333;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.3);
}
.search-icon {
  color: #a0a0a0;
  width: 20px;
  height: 20px;
}
.search-wrapper input {
  background: transparent;
  border: none;
  color: white;
  outline: none;
  margin-left: 12px;
  width: 100%;
  font-size: 16px;
}

.scrollable-content {
  padding: 24px 40px;
  overflow-y: auto;
  flex: 1;
}
.masonry-grid {
  column-count: 4;
  column-gap: 24px;
}
@media (max-width: 1400px) {
  .masonry-grid {
    column-count: 3;
  }
}
@media (max-width: 1000px) {
  .masonry-grid {
    column-count: 2;
  }
}

.pin-card {
  background-color: #1f1f1f;
  border-radius: 20px;
  margin-bottom: 24px;
  break-inside: avoid;
  position: relative;
  overflow: hidden;
  display: flex;
  align-items: flex-end;
  padding: 20px;
  cursor: pointer;
  background-image: linear-gradient(to bottom, transparent 50%, rgba(0, 0, 0, 0.8) 100%);
}
.media-badge {
  position: absolute;
  top: 16px;
  left: 16px;
  background-color: rgba(0, 0, 0, 0.6);
  padding: 6px 12px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: bold;
  backdrop-filter: blur(4px);
}
.collab-badge {
  position: absolute;
  top: 16px;
  right: 16px;
  background-color: #ff3b30;
  color: white;
  padding: 6px 12px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: bold;
  box-shadow: 0 4px 10px rgba(255, 59, 48, 0.3);
}
.pin-content {
  position: relative;
  z-index: 2;
  width: 100%;
}
.pin-content h3 {
  margin: 0 0 6px 0;
  font-size: 16px;
  line-height: 1.4;
}
.pin-content .tag {
  margin: 0;
  font-size: 13px;
  color: #aaa;
}
.card-actions {
  position: absolute;
  top: 12px; /* 카드 맨 위에서 살짝 띄움 */
  right: 12px; /* 카드 오른쪽에서 살짝 띄움 */
  z-index: 2;
  opacity: 0; /* 💡 평소에는 투명하게 숨김 */
  transition: opacity 0.2s ease-in-out; /* 부드럽게 나타나는 애니메이션 */
  display: flex;
  justify-content: flex-end;
  align-items: flex-start;
  padding: 16px;
}
.pin-card:hover .card-actions {
  opacity: 1;
}
.save-btn {
  background-color: #ff2a5f;
  color: white;
  border: none;
  padding: 10px 18px;
  border-radius: 24px;
  font-weight: bold;
  cursor: pointer;
}
.save-btn:hover {
  background-color: #e0003b;
}

/* =========================================
   Modal CSS
   ========================================= */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100vw;
  height: 100vh;
  background: rgba(0, 0, 0, 0.7);
  backdrop-filter: blur(5px);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 999;
}

.modal-content {
  background: #1e1e1e;
  border: 1px solid #333;
  padding: 32px;
  border-radius: 16px;
  width: 400px;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.5);
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.modal-content h2 {
  margin: 0 0 8px 0;
  font-size: 20px;
  color: #fff;
}

.input-group {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.input-group label {
  font-size: 13px;
  color: #aaa;
}

.input-group input[type='text'],
.input-group select {
  padding: 12px;
  border-radius: 8px;
  border: 1px solid #333;
  background: #121212;
  color: #fff;
  font-size: 14px;
}

.input-group input:focus,
.input-group select:focus {
  outline: none;
  border-color: #555;
}

.checkbox-group {
  flex-direction: row;
  align-items: center;
  margin-top: 8px;
}

.checkbox-group label {
  color: #ddd;
  font-size: 14px;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 8px;
}

.modal-actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  margin-top: 16px;
}

.cancel-btn,
.submit-btn {
  padding: 10px 20px;
  border-radius: 8px;
  font-weight: bold;
  cursor: pointer;
  border: none;
}

.cancel-btn {
  background: transparent;
  color: #aaa;
}
.cancel-btn:hover {
  color: #fff;
}

.submit-btn {
  background: #fff;
  color: #000;
}
.submit-btn:hover {
  background: #e0e0e0;
}

/* 이미지 미리보기 및 카드 내부 이미지 스타일 */
.preview-img {
  width: 100%;
  height: 150px;
  object-fit: cover;
  border-radius: 8px;
  margin-top: 8px;
}

.feed-image {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  object-fit: cover;
  z-index: 1; /* 글자보다 뒤에 배치 */
}

/* 이미지 위에 텍스트가 잘 보이도록 반투명 그라데이션 배경 처리 */
.feed-content {
  position: relative;
  z-index: 2;
  padding: 16px;
  background: linear-gradient(to top, rgba(0, 0, 0, 0.8), transparent);
  height: 100%;
  display: flex;
  flex-direction: column;
  justify-content: flex-end;
}

.pin-image {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  object-fit: cover; /* 찌그러지지 않고 카드에 꽉 차게 */
  z-index: 0; /* 가장 뒤로 보내기 */
}

/* 💡 기존 요소들을 이미지 앞으로 당겨오기 */
.media-badge,
.collab-badge,
.pin-content {
  position: absolute; /* 공중에 띄우기 */
  bottom: 0; /* 카드의 맨 아래에 고정 */
  left: 0;
  width: 100%;
  box-sizing: border-box; /* 💡 패딩 때문에 카드를 뚫고 나가는 현상 방지 */
  z-index: 1;
  background: linear-gradient(to top, rgba(0, 0, 0, 0.9) 0%, transparent 100%);
  padding: 10px 16px 16px 16px; /* 위쪽 패딩을 넉넉히 주어 자연스러운 그라데이션 형성 */
  display: flex;
  flex-direction: column;
  gap: 4px; /* 제목과 태그 사이의 간격 */
}
/* 글자 여백(margin) 초기화로 깔끔한 정렬 맞추기 */
.pin-content h3 {
  margin: 0;
  color: #fff;
  font-size: 16px;
  font-weight: 600;
}

.pin-content .tag {
  margin: 0;
  color: #ccc;
  font-size: 13px;
}
/* =========================================
   Detail Modal CSS
   ========================================= */
.detail-modal-content {
  position: relative;
  background: #1e1e1e;
  border-radius: 24px;
  width: 900px; /* 글쓰기 모달보다 훨씬 넓게 설정 */
  max-width: 90vw;
  max-height: 90vh;
  box-shadow: 0 20px 50px rgba(0, 0, 0, 0.8);
  display: flex;
  overflow: hidden; /* 모서리 둥글게 깎기 */
}

/* 닫기 버튼 (우측 상단 둥둥 띄우기) */
.close-btn {
  position: absolute;
  top: 16px;
  right: 16px;
  background: rgba(0, 0, 0, 0.5);
  color: #fff;
  border: none;
  border-radius: 50%;
  width: 40px;
  height: 40px;
  font-size: 20px;
  cursor: pointer;
  z-index: 10;
  transition: background 0.2s;
}
.close-btn:hover {
  background: rgba(255, 255, 255, 0.2);
}

/* 좌우 분할 레이아웃 */
.detail-layout {
  display: flex;
  width: 100%;
  height: 100%;
}

/* 왼쪽 이미지 영역 */
.detail-image-area {
  flex: 6; /* 비율 6 */
  background: #000;
  display: flex;
  align-items: center;
  justify-content: center;
  max-height: 90vh;
}
.detail-image-area img {
  width: 100%;
  height: 100%;
  object-fit: contain; /* 이미지가 잘리지 않고 전체가 다 보이게 */
}
.no-image {
  color: #555;
  font-size: 18px;
}

/* 오른쪽 정보 영역 */
.detail-info-area {
  flex: 4; /* 비율 4 */
  padding: 40px;
  display: flex;
  flex-direction: column;
  background: #121212;
}

.detail-header {
  display: flex;
  gap: 10px;
  margin-bottom: 24px;
  /* 배지들이 absolute로 날아가지 않도록 위치 초기화 */
  position: relative;
}
.detail-header .media-badge,
.detail-header .collab-badge {
  position: relative;
  top: auto;
  left: auto;
}

.detail-title {
  margin: 0 0 16px 0;
  font-size: 32px;
  font-weight: 700;
  line-height: 1.3;
}

.detail-creator {
  margin: 0 0 8px 0;
  font-size: 16px;
  color: #aaa;
}
.detail-creator strong {
  color: #fff;
}

.detail-tag {
  margin: 0 0 32px 0;
  font-size: 14px;
  color: #888;
}

/* 하단 버튼들 */
.detail-actions {
  margin-top: auto; /* 정보창의 맨 아래로 버튼들을 밀어냄 */
  display: flex;
  gap: 12px;
}
.detail-btn {
  flex: 1;
  padding: 16px;
  border-radius: 12px;
  border: none;
  font-size: 16px;
  font-weight: bold;
  cursor: pointer;
  transition: opacity 0.2s;
}
.detail-btn:hover {
  opacity: 0.8;
}

/* =========================================
   상호작용 버튼 Active(활성화) 상태 CSS
   ========================================= */

/* 그리드 카드 내 저장 버튼 활성화 상태 */
.action-btn.save-btn.is-saved {
  background-color: #000;
  color: #fff;
  border: 1px solid #333;
}

/* 상세 모달 내 버튼 비활성화(기본) 상태 */
.detail-btn.like-btn {
  background: #222;
  color: #ccc;
}
.detail-btn.save-btn {
  background: #222;
  color: #ccc;
}

/* 상세 모달 내 버튼 활성화(눌림) 상태 */
.detail-btn.like-btn.is-liked {
  background: #ff2a5f; /* 핑크/레드 톤 */
  color: #fff;
}
.detail-btn.save-btn.is-saved {
  background: #0066ff; /* 블루 톤 (핀터레스트 레드로 변경해도 좋습니다) */
  color: #fff;
}

.like-btn {
  background: #333;
  color: #fff;
}
.save-btn {
  background: #e60023;
  color: #fff;
} /* 핀터레스트 레드 컬러 */
</style>
