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
            <li :class="{ active: currentMenu === 'all' }" @click="loadFeeds('all')">
              <Home class="icon" />
              <span class="menu-text">Home</span>
            </li>

            <li :class="{ active: currentMenu === 'following' }" @click="loadFeeds('following')">
              <Users class="icon" />
              <span class="menu-text">Following</span>
            </li>

            <li :class="{ active: currentMenu === 'bookmark' }" @click="loadFeeds('bookmark')">
              <Bookmark class="icon" />
              <span class="menu-text">My Board</span>
            </li>

            <li :class="{ active: currentMenu === 'messages' }" @click="openMessages">
              <MessageSquare class="icon" />
              <span class="menu-text">Messages</span>
              <span v-if="totalUnreadCount > 0" class="unread-badge">{{ totalUnreadCount }}</span>
            </li>
            <li :class="{ active: currentMenu === 'profile' }" @click="loadFeeds('profile')">
              <User class="icon" />
              <span class="menu-text">Profile</span>
            </li>
            <!-- 💡 플러스 아이콘을 버튼 대신 심플하게 유지 -->
            <button class="nav-btn create-btn" @click="openCreateModal">
              <Plus class="icon" />
              <span class="menu-text">만들기</span>
            </button>
          </ul>
        </nav>
        
        <!-- 더 보기 메뉴 버튼과 팝업 -->
        <div class="more-menu-container" ref="moreMenuContainer">
          <!-- 팝업 메뉴 -->
          <div v-if="showMoreMenu" class="more-popup">
            <ul>
              <li @click="goTo('/settings')"><Settings class="menu-icon"/> 설정</li>
              <li><Activity class="menu-icon"/> 내 활동</li>
              <li><Bookmark class="menu-icon"/> 저장됨</li>
              <li><Moon class="menu-icon"/> 모드 전환</li>
              <li @click="goTo('/report')"><MessageCircle class="menu-icon"/> 문제 신고</li>
              <hr />
              <li @click="switchAccount">계정 전환</li>
              <li @click="logout">로그아웃</li>
            </ul>
          </div>
          <!-- 버튼 -->
          <button class="nav-btn more-btn" @click="showMoreMenu = !showMoreMenu">
            <Menu class="icon" />
            <span class="menu-text">더 보기</span>
          </button>
        </div>
      </aside>
    </div>

    <main class="main-content">
      <div v-if="currentMenu !== 'profile' && currentMenu !== 'messages'" class="default-feed-content">
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
            @click="openDetail(feed)"
            @mouseenter="playVideo($event, feed)"
            @mouseleave="stopVideo($event, feed)"
          >
            <!-- 비디오 호버 재생을 위한 비디오 요소 -->
            <video
              v-if="feed.videoUrl"
              class="pin-video"
              :src="'http://localhost:8080' + feed.videoUrl"
              muted
              loop
              playsinline
              preload="none"
              style="display: none;"
            ></video>

            <img
              v-if="feed.imageUrl"
              :src="'http://localhost:8080' + feed.imageUrl"
              class="pin-image"
              alt="Artwork"
            />

            <div class="card-actions">
              <button
                class="action-btn save-btn"
                :class="{ 'is-saved': feed.isBookmarked }"
                @click.stop="toggleBookmark(feed)"
              >
                {{ feed.isBookmarked ? '저장됨' : '저장' }}
              </button>
            </div>
          </div>
      </div>
      </div>
      </div>
      
      <!-- ==== 메시지(DM) 뷰 ==== -->
      <div v-else-if="currentMenu === 'messages'" class="messages-page">
        <div class="dm-container">
          <!-- 좌측: 채팅방 목록 -->
          <div class="dm-sidebar">
            <div class="dm-sidebar-header">
              <h2>메시지</h2>
              <button class="dm-new-chat-btn" @click="showNewChatModal = true">
                <Plus class="icon-sm" />
              </button>
            </div>

            <div class="dm-room-list">
              <div v-if="chatRooms.length === 0" class="dm-empty">
                <MessageSquare class="dm-empty-icon" />
                <p>아직 대화가 없습니다</p>
                <button class="dm-start-btn" @click="showNewChatModal = true">새 대화 시작</button>
              </div>

              <div
                v-for="room in chatRooms"
                :key="room.roomId"
                class="dm-room-item"
                :class="{ active: selectedRoom && selectedRoom.roomId === room.roomId }"
                @click="selectRoom(room)"
              >
                <div class="dm-room-avatar">
                  <img
                    v-if="room.otherProfileImageUrl"
                    :src="'http://localhost:8080' + room.otherProfileImageUrl"
                    alt="avatar"
                  />
                  <div v-else class="dm-avatar-placeholder">
                    {{ (room.otherNickname || room.otherUsername).charAt(0).toUpperCase() }}
                  </div>
                </div>
                <div class="dm-room-info">
                  <span class="dm-room-name">{{ room.otherNickname || room.otherUsername }}</span>
                  <span class="dm-room-preview">{{ room.lastMessage || '대화를 시작하세요' }}</span>
                </div>
                <div class="dm-room-meta">
                  <span class="dm-room-time">{{ formatChatTime(room.lastMessageTime) }}</span>
                  <span v-if="room.unreadCount > 0" class="dm-unread-count">{{ room.unreadCount }}</span>
                </div>
              </div>
            </div>
          </div>

          <!-- 우측: 대화 내용 -->
          <div class="dm-chat-area">
            <div v-if="!selectedRoom" class="dm-no-selection">
              <MessageSquare class="dm-no-selection-icon" />
              <h3>내 메시지</h3>
              <p>친구에게 메시지를 보내보세요</p>
              <button class="dm-start-btn" @click="showNewChatModal = true">메시지 보내기</button>
            </div>

            <template v-else>
              <!-- 채팅 헤더 -->
              <div class="dm-chat-header">
                <div class="dm-chat-header-avatar">
                  <img
                    v-if="selectedRoom.otherProfileImageUrl"
                    :src="'http://localhost:8080' + selectedRoom.otherProfileImageUrl"
                    alt="avatar"
                  />
                  <div v-else class="dm-avatar-placeholder sm">
                    {{ (selectedRoom.otherNickname || selectedRoom.otherUsername).charAt(0).toUpperCase() }}
                  </div>
                </div>
                <span class="dm-chat-header-name">{{ selectedRoom.otherNickname || selectedRoom.otherUsername }}</span>
              </div>

              <!-- 메시지 목록 -->
              <div class="dm-messages" ref="dmMessagesContainer">
                <div
                  v-for="msg in chatMessages"
                  :key="msg.id"
                  class="dm-msg"
                  :class="{ 'dm-msg-mine': msg.sender === currentUsername }"
                >
                  <div class="dm-msg-bubble">
                    {{ msg.content }}
                  </div>
                  <span class="dm-msg-time">{{ formatChatTime(msg.createdAt) }}</span>
                </div>
              </div>

              <!-- 메시지 입력 -->
              <div class="dm-input-area">
                <input
                  v-model="newMessage"
                  type="text"
                  placeholder="메시지 입력..."
                  @keyup.enter="sendChatMessage"
                  class="dm-input"
                />
                <button class="dm-send-btn" @click="sendChatMessage" :disabled="!newMessage.trim()">
                  <Send class="icon-sm" />
                </button>
              </div>
            </template>
          </div>
        </div>
      </div>
      <!-- ==== 메시지(DM) 뷰 끝 ==== -->

      <!-- 새 대화 시작 모달 -->
      <div v-if="showNewChatModal" class="modal-overlay" @click.self="showNewChatModal = false">
        <div class="dm-new-chat-modal">
          <div class="dm-modal-header">
            <h3>새로운 메시지</h3>
            <button class="dm-modal-close" @click="showNewChatModal = false">&times;</button>
          </div>
          <div class="dm-modal-search">
            <input
              v-model="userSearchKeyword"
              type="text"
              placeholder="유저 이름으로 검색..."
              @input="searchUsersForChat"
              class="dm-search-input"
            />
          </div>
          <div class="dm-modal-results">
            <div
              v-for="user in userSearchResults"
              :key="user.username"
              class="dm-search-result-item"
              @click="startChatWith(user)"
            >
              <div class="dm-room-avatar">
                <img
                  v-if="user.profileImageUrl"
                  :src="'http://localhost:8080' + user.profileImageUrl"
                  alt="avatar"
                />
                <div v-else class="dm-avatar-placeholder">
                  {{ (user.nickname || user.username).charAt(0).toUpperCase() }}
                </div>
              </div>
              <div class="dm-search-result-info">
                <span class="dm-search-result-name">{{ user.nickname || user.username }}</span>
                <span class="dm-search-result-username">@{{ user.username }}</span>
              </div>
            </div>
            <p v-if="userSearchKeyword && userSearchResults.length === 0" class="dm-no-results">
              검색 결과가 없습니다
            </p>
          </div>
        </div>
      </div>

      <!-- ==== 프로필 뷰 콘텐츠 ==== -->
      <div v-else-if="currentMenu === 'profile'" class="profile-page-content">
        <!-- 1. 프로필 헤더 정보 영역 -->
        <header class="profile-header-centered">
          <div class="profile-avatar-container large-center" @click="triggerDirectProfileImageUpload">
            <input type="file" ref="directProfileImageInput" @change="handleDirectProfileImageUpload" accept="image/*" style="display: none;" />
            <img 
              v-if="userProfile.profileImageUrl" 
              :src="'http://localhost:8080' + userProfile.profileImageUrl" 
              alt="Profile Avatar" 
              class="profile-avatar-img"
            />
            <div v-else class="avatar-placeholder-large">
              <User class="icon-xlarge" />
            </div>
          </div>
          
          <div class="profile-info-section-centered">
            <div class="profile-info-top-centered">
              <h2 class="username-title">{{ userProfile.username || 'Loading...' }} <Settings class="icon-small settings-icon" /></h2>
            </div>

            <div class="profile-nickname-centered">
              <p>{{ userProfile.nickname || userProfile.username }}</p>
              <p class="profile-tag" v-if="userProfile.tag">{{ userProfile.tag }}</p>
            </div>
            
            <div class="profile-stats-centered">
              <span>게시물 <strong>{{ userProfile.postCount }}</strong></span>
              <span>팔로워 <strong>{{ userProfile.followerCount }}</strong></span>
              <span>팔로우 <strong>{{ userProfile.followingCount }}</strong></span>
            </div>

            <div class="profile-actions-centered">
              <button class="action-btn wide" @click="openEditModal">프로필 편집</button>
              <button class="action-btn wide">보관함 보기</button>
            </div>
          </div>

          <!-- 스토리 하이라이트 영역 (New 버튼) -->
          <div class="profile-highlights">
            <div class="highlight-item">
              <div class="highlight-circle">
                <Plus class="icon-large" />
              </div>
              <span class="highlight-text">New</span>
            </div>
          </div>
        </header>

        <!-- 2. 프로필 탭 (아이콘만 표시) -->
        <nav class="profile-tabs-centered">
          <button 
            class="tab-btn-icon" 
            :class="{ active: activeTab === 'posts' }"
            @click="changeTab('posts')"
          >
            <Grid class="tab-icon-large" />
          </button>
          <button 
            class="tab-btn-icon" 
            :class="{ active: activeTab === 'interactions' }"
            @click="changeTab('interactions')"
          >
            <Bookmark class="tab-icon-large" />
          </button>
          <button 
            class="tab-btn-icon" 
            :class="{ active: activeTab === 'collabs' }"
            @click="changeTab('collabs')"
          >
            <UserSquare class="tab-icon-large" />
          </button>
        </nav>

        <!-- 3. 선택된 탭의 피드 그리드 -->
        <div class="feeds-grid">
          <div 
            v-for="feed in currentProfileFeeds" 
            :key="feed.id" 
            class="feed-thumbnail-wrapper"
            @click="openDetail(feed)"
          >
            <video
              v-if="feed.videoUrl"
              :src="'http://localhost:8080' + feed.videoUrl"
              class="feed-thumbnail"
              muted loop
              @mouseover="$event.target.play()"
              @mouseout="$event.target.pause()"
            ></video>
            <img
              v-else-if="feed.imageUrl"
              :src="'http://localhost:8080' + feed.imageUrl"
              alt="Feed Image"
              class="feed-thumbnail"
            />
            <div v-else class="feed-thumbnail placeholder-thumbnail">
              {{ feed.title }}
            </div>
          </div>
        </div>
        
        <!-- 글이 없을 때 표시할 UI -->
        <div v-if="currentProfileFeeds.length === 0" class="empty-state-modern">
          <div class="empty-icon-circle-large">
            <Camera class="empty-camera-icon" />
          </div>
          <h2>사진 공유</h2>
          <p>사진을 공유하면 회원님의 프로필에 표시됩니다.</p>
          <a href="#" class="share-link">첫 사진 공유하기</a>
        </div>
      </div>
      <!-- ==== 프로필 뷰 콘텐츠 끝 ==== -->
    </main>
  </div>

  <!-- 프로필 편집 모달 -->
  <div v-if="showEditModal" class="modal-overlay" @click.self="showEditModal = false">
    <div class="edit-modal-content">
      <h3>프로필 편집</h3>
      <div class="edit-form-group">
        <label>닉네임</label>
        <input type="text" v-model="editForm.nickname" placeholder="새 닉네임 입력" class="edit-input" />
      </div>
      <div class="edit-form-group">
        <label>소개 (태그)</label>
        <input type="text" v-model="editForm.tag" placeholder="소개 문구 입력" class="edit-input" />
      </div>
      <div class="edit-form-group">
        <label>프로필 사진 변경</label>
        <input type="file" @change="handleProfileFileChange" accept="image/*" class="file-input" />
      </div>
      <div class="edit-modal-actions">
        <button class="save-btn" @click="saveProfile">저장</button>
        <button class="cancel-btn" @click="showEditModal = false">취소</button>
      </div>
    </div>
  </div>

  <div v-if="showModal" class="modal-overlay" @click.self="closeCreateModal">
    <div class="modal-content new-modal" :class="{'step2-modal': modalStep === 2}">
      
      <!-- Step 1: 미디어 선택 -->
      <div v-if="modalStep === 1" class="modal-step-1">
        <div class="modal-header-centered">
          <h2>새 게시물 만들기</h2>
        </div>
        <div class="drag-drop-area" @click="triggerFileInput">
          <div class="icon-group">
            <svg viewBox="0 0 24 24" class="media-icon"><path d="M19 3H5c-1.1 0-2 .9-2 2v14c0 1.1.9 2 2 2h14c1.1 0 2-.9 2-2V5c0-1.1-.9-2-2-2zm0 16H5V5h14v14zm-5.04-6.71l-2.75 3.54-1.96-2.36L6.5 17h11l-3.54-4.71z" fill="currentColor"/></svg>
            <svg viewBox="0 0 24 24" class="media-icon video-icon"><path d="M17 10.5V7c0-.55-.45-1-1-1H4c-.55 0-1 .45-1 1v10c0 .55.45 1 1 1h12c.55 0 1-.45 1-1v-3.5l4 4v-11l-4 4z" fill="currentColor"/></svg>
          </div>
          <h3>사진과 동영상을 여기에 끌어다 놓으세요</h3>
          <button class="select-file-btn">컴퓨터에서 선택</button>
          <input type="file" ref="fileInput" @change="handleFileChange" accept="image/*,video/*" style="display: none;" />
        </div>
      </div>

      <!-- Step 2: 상세 정보 입력 -->
      <div v-if="modalStep === 2" class="modal-step-2">
        <div class="modal-header-nav">
          <button class="back-btn" @click="modalStep = 1">←</button>
          <h2>새 게시물 만들기</h2>
          <button class="share-btn" @click="createNewFeed">공유하기</button>
        </div>
        
        <div class="modal-body-split">
          <!-- 좌측: 미디어 미리보기 -->
          <div class="media-preview-pane">
            <video v-if="selectedFile && selectedFile.type.startsWith('video/')" :src="imagePreview" autoplay loop muted class="preview-media"></video>
            <img v-else :src="imagePreview" class="preview-media" />
          </div>

          <!-- 우측: 입력 및 설정 -->
          <div class="details-pane">
            <div class="user-header">
              <div class="avatar-placeholder"><User class="icon-small" /></div>
              <span class="username">{{ currentUsername }}</span>
            </div>

            <textarea 
              v-model="formData.content" 
              placeholder="문구를 입력하세요..." 
              class="caption-input"
              maxlength="2200"
            ></textarea>
            <div class="char-count">{{ formData.content.length || 0 }}/2200</div>

            <div class="form-row">
              <input type="text" v-model="formData.tag" placeholder="태그 추가 (예: Art)" class="minimal-input" />
            </div>

            <div class="advanced-settings">
              <div class="settings-header" @click="showAdvancedSettings = !showAdvancedSettings">
                <span>고급 설정</span>
                <span class="chevron">{{ showAdvancedSettings ? '▲' : '▼' }}</span>
              </div>
              
              <div v-if="showAdvancedSettings" class="settings-body">
                <div class="setting-item">
                  <div class="setting-text">
                    <h4>이 게시물의 좋아요 수 및 조회수 숨기기</h4>
                    <p>이 게시물의 총 좋아요 및 조회수는 회원님만 볼 수 있습니다.</p>
                  </div>
                  <label class="toggle-switch">
                    <input type="checkbox" v-model="formData.hideCounts">
                    <span class="slider round"></span>
                  </label>
                </div>

                <div class="setting-item">
                  <div class="setting-text">
                    <h4>댓글 기능 해제</h4>
                    <p>나중에 게시물 상단의 메뉴에서 이 설정을 변경할 수 있습니다.</p>
                  </div>
                  <label class="toggle-switch">
                    <input type="checkbox" v-model="formData.disableComments">
                    <span class="slider round"></span>
                  </label>
                </div>

                <div class="setting-item">
                  <div class="setting-text">
                    <h4>공동 작업자 추가 (협업 여부)</h4>
                  </div>
                  <label class="toggle-switch">
                    <input type="checkbox" v-model="formData.isCollab">
                    <span class="slider round"></span>
                  </label>
                </div>
              </div>
            </div>

          </div>
        </div>
      </div>

    </div>
  </div>

  <!-- 상세 보기 모달 -->
  <div v-if="showDetailModal && selectedFeed" class="modal-overlay" style="background: rgba(0,0,0,0.85);" @click.self="closeDetail">
    <div class="detail-modal-content pinterest-style">
      
      <!-- 상단 네비게이션 바 -->
      <div class="pinterest-top-bar">
        <button class="back-btn-top" @click="closeDetail"><ArrowLeft class="icon-medium" /></button>
        <div class="top-bar-actions">
          <button class="icon-btn-text" :class="{ 'is-liked': selectedFeed.isLiked }" @click="toggleLike(selectedFeed)">
            <Heart class="icon-medium" :fill="selectedFeed.isLiked ? '#ff2a5f' : 'none'" :color="selectedFeed.isLiked ? '#ff2a5f' : '#fff'" />
            <span class="count-text" v-if="selectedFeed.likeCount > 0">{{ selectedFeed.likeCount }}</span>
          </button>
          <button class="icon-btn-text" @click="focusCommentInput"><MessageCircle class="icon-medium" /></button>
          <button class="icon-btn-text" @click="startChatWithCreator(selectedFeed)" title="메시지 보내기"><Send class="icon-medium" /></button> <!-- Direct Message -->
          <button class="icon-btn-text" @click="showMoreOptionsModal = true"><MoreHorizontal class="icon-medium" /></button> <!-- More -->
          <div class="profile-dropdown-btn">
            프로필 <ChevronDown class="icon-small" />
          </div>
          <button class="save-btn-red" :class="{ 'is-saved': selectedFeed.isBookmarked }" @click="toggleBookmark(selectedFeed)">
            {{ selectedFeed.isBookmarked ? '저장됨' : '저장' }}
          </button>
        </div>
      </div>

      <!-- 메인 콘텐츠 영역 -->
      <div class="pinterest-split-layout">
        <!-- 좌측: 미디어 영역 -->
        <div class="pinterest-media-pane">
          <video
            v-if="selectedFeed.videoUrl"
            :src="'http://localhost:8080' + selectedFeed.videoUrl"
            controls
            autoplay
            class="media-full-pinterest"
          ></video>
          <img
            v-else-if="selectedFeed.imageUrl"
            :src="'http://localhost:8080' + selectedFeed.imageUrl"
            alt="Artwork Full"
            class="media-full-pinterest"
          />
          <div v-else class="no-image">No Image</div>
        </div>

        <!-- 우측: 정보 및 댓글 영역 -->
        <div class="pinterest-info-pane">
          <div class="pinterest-scrollable-content">
            <div class="pinterest-author">
              <div class="avatar-placeholder-medium"><User class="icon-small" /></div>
              <div class="author-info" style="display:flex; flex-direction:column; gap:4px; margin-left: 12px; flex:1;">
                <span class="username" style="font-size: 16px;"><strong>{{ selectedFeed.creator }}</strong></span>
              </div>
              <button 
                v-if="currentUsername && selectedFeed.creator !== currentUsername"
                class="follow-btn" 
                :class="{ 'is-following': selectedFeed.isFollowing }"
                @click="toggleFollowCreator"
              >
                {{ selectedFeed.isFollowing ? '팔로잉' : '팔로우' }}
              </button>
            </div>

            <!-- 설명 (Caption) -->
            <div class="pinterest-description" v-if="selectedFeed.content">
              <h4>설명</h4>
              <p class="description-text">{{ selectedFeed.content }}</p>
              <div class="see-more">더 보기</div>
            </div>

            <!-- 댓글 헤더 -->
            <div class="pinterest-comments-header">
              <h4>댓글 {{ comments.length + (selectedFeed.content ? 1 : 0) }}개</h4>
              <ChevronDown class="icon-small chevron" />
            </div>

            <!-- 댓글 목록 -->
            <div class="comments-section-pinterest">
              <div class="comment-item-pinterest" v-for="comment in comments" :key="comment.id">
                <div class="avatar-placeholder-small"><User class="icon-small" /></div>
                
                <div class="comment-content-pinterest" style="flex: 1;">
                  <div class="comment-header-row">
                    <span class="comment-author"><strong>{{ comment.username }}</strong></span>
                    
                    <!-- 수정/삭제 버튼 -->
                    <div class="comment-actions" v-if="isAdmin || comment.username === currentUsername">
                      <button class="action-text-btn" @click="startEditComment(comment)" v-if="comment.username === currentUsername && editingCommentId !== comment.id">수정</button>
                      <button class="action-text-btn" @click="saveEditComment(comment.id)" v-if="comment.username === currentUsername && editingCommentId === comment.id">저장</button>
                      <button class="action-text-btn delete" @click="deleteComment(comment.id)">삭제</button>
                      <button class="action-text-btn cancel" @click="editingCommentId = null" v-if="comment.username === currentUsername && editingCommentId === comment.id">취소</button>
                    </div>
                  </div>

                  <span class="comment-text" v-if="editingCommentId !== comment.id">{{ comment.content }}</span>
                  <input v-else type="text" v-model="editingCommentContent" @keyup.enter="saveEditComment(comment.id)" class="edit-comment-input" />
                </div>
              </div>
            </div>
          </div>

          <!-- 댓글 입력창 -->
          <div class="pinterest-comment-input-area" v-if="!selectedFeed.disableComments">
            <input 
              type="text" 
              v-model="newComment" 
              placeholder="댓글 추가" 
              @keyup.enter="submitComment" 
              class="comment-input-box"
              ref="commentInputBoxRef"
            />
            <div class="input-icons">
              <Smile class="icon-small icon-btn-hover" />
              <Sticker class="icon-small icon-btn-hover" />
              <Image class="icon-small icon-btn-hover" />
            </div>
          </div>
          <div class="comment-input-area disabled-comments" v-else>
            댓글 기능이 해제되었습니다.
          </div>
        </div>
      </div>
      
      <!-- 더보기(점 3개) 옵션 팝업 -->
      <div class="more-options-overlay" v-if="showMoreOptionsModal" @click.self="showMoreOptionsModal = false">
        <div class="more-options-modal">
          <button class="more-option-btn text-red" @click="handleReport">신고</button>
          <button class="more-option-btn" @click="copyLink">링크 복사</button>
          <button class="more-option-btn" @click="handleAccountInfo">이 계정 정보</button>
          <button class="more-option-btn text-red" v-if="isAdmin || (selectedFeed && selectedFeed.creator === currentUsername)" @click="handleDeleteFeed">삭제</button>
          <button class="more-option-btn" @click="showMoreOptionsModal = false">취소</button>
        </div>
      </div>

    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import api from '../api/axios' // 커스텀 axios 인터셉터 임포트
import {
  Home,
  Users,
  Bookmark,
  MessageSquare,
  User,
  Plus,
  Sparkles,
  Search,
  Menu,
  Settings,
  Activity,
  Moon,
  MessageCircle,
  Heart,
  Send,
  Grid,
  Camera,
  UserSquare,
  ArrowLeft,
  Upload,
  MoreHorizontal,
  ChevronDown,
  Smile,
  Image,
  Sticker
} from 'lucide-vue-next'
import { useRouter } from 'vue-router'

const router = useRouter()

// 더 보기 메뉴 상태
const showMoreMenu = ref(false)

// 메뉴 외부 클릭 시 닫기
import { onUnmounted } from 'vue'
const moreMenuContainer = ref(null)
const handleClickOutside = (event) => {
  if (moreMenuContainer.value && !moreMenuContainer.value.contains(event.target)) {
    showMoreMenu.value = false
  }
}
onMounted(() => {
  document.addEventListener('click', handleClickOutside)
})
onUnmounted(() => {
  document.removeEventListener('click', handleClickOutside)
  stopChatPolling()
})

const goTo = (path) => {
  showMoreMenu.value = false
  router.push(path)
}

const logout = async () => {
  try {
    await api.post('/users/logout')
  } catch (error) {
    console.error('Logout error on server', error)
  }
  // Clear local storage and redirect
  localStorage.removeItem('accessToken')
  localStorage.removeItem('refreshToken')
  router.push('/login')
}

const switchAccount = () => {
  // 계정 전환도 일단 로그아웃 후 로그인 페이지로 이동하도록 처리
  localStorage.removeItem('accessToken')
  localStorage.removeItem('refreshToken')
  router.push('/login')
}

// 피드 초기값 빈 배열
const mixedFeeds = ref([])

// 글쓰기 모달 상태 관리
const showModal = ref(false)
const modalStep = ref(1) // 1: 미디어 선택, 2: 상세 입력
const showAdvancedSettings = ref(false)
const fileInput = ref(null)

const triggerFileInput = () => {
  if (fileInput.value) {
    fileInput.value.click()
  }
}

const openCreateModal = () => {
  showModal.value = true
  modalStep.value = 1
}

const closeCreateModal = () => {
  showModal.value = false
  modalStep.value = 1
  resetForm()
}

const currentUsername = ref('AtriinUser') // 나중에 fetchProfile에서 실제 유저명으로 덮어씀

const resetForm = () => {
  formData.value = { content: '', creator: currentUsername.value, type: 'art', tag: '', isCollab: false, hideCounts: false, disableComments: false }
  selectedFile.value = null
  selectedThumbnail.value = null
  imagePreview.value = null
}

// 파일 보관용 변수 추가
const selectedFile = ref(null)
const selectedThumbnail = ref(null)
const imagePreview = ref(null)

const extractVideoThumbnail = (file) => {
  const video = document.createElement('video')
  video.src = URL.createObjectURL(file)
  video.crossOrigin = 'anonymous'
  
  video.addEventListener('loadeddata', () => {
    video.currentTime = 1 // 1초 지점 캡처 (영상이 1초 미만이어도 브라우저가 마지막 프레임 처리함)
  })
  
  video.addEventListener('seeked', () => {
    const canvas = document.createElement('canvas')
    canvas.width = video.videoWidth
    canvas.height = video.videoHeight
    const ctx = canvas.getContext('2d')
    ctx.drawImage(video, 0, 0, canvas.width, canvas.height)
    
    canvas.toBlob((blob) => {
      selectedThumbnail.value = blob
      imagePreview.value = URL.createObjectURL(blob)
    }, 'image/jpeg', 0.8)
  })
  video.load()
}

// 파일 선택 시 처리 로직
const handleFileChange = (event) => {
  const file = event.target.files[0]
  if (file) {
    selectedFile.value = file
    imagePreview.value = URL.createObjectURL(file)
    
    if (file.type.startsWith('video/')) {
      extractVideoThumbnail(file)
    } else {
      selectedThumbnail.value = null
    }
    
    modalStep.value = 2 // 파일 선택 시 Step 2로 이동
  }
}

// 사용자가 입력할 폼 데이터 보관함
const formData = ref({
  content: '',
  creator: currentUsername.value,
  type: 'art', // 기본값
  tag: '',
  isCollab: false,
  hideCounts: false,
  disableComments: false,
})

// 상세 보기 모달 상태 관리
const showDetailModal = ref(false)
const selectedFeed = ref(null) // 클릭한 카드의 모든 정보(객체)를 통째로 보관

// 더보기 옵션 모달 상태 및 핸들러
const showMoreOptionsModal = ref(false)

const handleReport = () => {
  showAlert('신고가 접수되었습니다.')
  showMoreOptionsModal.value = false
}

const copyLink = () => {
  const link = `${window.location.origin}/feed/${selectedFeed.value?.id}`
  navigator.clipboard.writeText(link).then(() => {
    showAlert('링크가 복사되었습니다.')
  }).catch(() => {
    showAlert('링크 복사에 실패했습니다.')
  })
  showMoreOptionsModal.value = false
}

const handleAccountInfo = () => {
  showAlert('계정 정보 기능은 준비 중입니다.')
  showMoreOptionsModal.value = false
}

const handleDeleteFeed = async () => {
  if (!confirm('정말 이 게시물을 삭제하시겠습니까?')) return
  try {
    await api.delete(`/feeds/${selectedFeed.value.id}?username=${currentUsername.value}`)
    showMoreOptionsModal.value = false
    closeDetail()
    await loadFeeds(currentMenu.value)
  } catch (error) {
    console.error('게시물 삭제 실패', error)
    showAlert('게시물을 삭제하지 못했습니다.')
  }
}

// 댓글 상태 관리
const comments = ref([])
const newComment = ref('')
const commentInputBoxRef = ref(null)

const showAlert = (msg) => alert(msg)
const focusCommentInput = () => {
  if (commentInputBoxRef.value) {
    commentInputBoxRef.value.focus()
  }
}

const editingCommentId = ref(null)
const editingCommentContent = ref('')

const startEditComment = (comment) => {
  editingCommentId.value = comment.id
  editingCommentContent.value = comment.content
}

const saveEditComment = async (commentId) => {
  if (!editingCommentContent.value.trim()) return
  try {
    await api.put(`/feeds/${selectedFeed.value.id}/comments/${commentId}`, {
      username: currentUsername.value,
      content: editingCommentContent.value
    })
    editingCommentId.value = null
    editingCommentContent.value = ''
    await fetchComments(selectedFeed.value.id)
  } catch (error) {
    console.error('댓글 수정 실패', error)
    alert('댓글을 수정하지 못했습니다.')
  }
}

const deleteComment = async (commentId) => {
  if (!confirm('댓글을 삭제하시겠습니까?')) return
  try {
    await api.delete(`/feeds/${selectedFeed.value.id}/comments/${commentId}?username=${currentUsername.value}`)
    await fetchComments(selectedFeed.value.id)
  } catch (error) {
    console.error('댓글 삭제 실패', error)
    alert('댓글을 삭제하지 못했습니다.')
  }
}

const fetchComments = async (feedId) => {
  try {
    const res = await api.get(`/feeds/${feedId}/comments`)
    comments.value = res.data
  } catch (err) {
    console.error('댓글을 불러오는데 실패했습니다.', err)
  }
}

const submitComment = async () => {
  if (!newComment.value.trim() || !selectedFeed.value) return
  
  try {
    await api.post(`/feeds/${selectedFeed.value.id}/comments`, {
      username: currentUsername.value,
      content: newComment.value
    })
    newComment.value = ''
    await fetchComments(selectedFeed.value.id)
  } catch (err) {
    console.error('댓글 작성에 실패했습니다.', err)
    alert('댓글 작성에 실패했습니다.')
  }
}

// 카드 클릭 시 실행될 함수
const openDetail = async (feed) => {
  selectedFeed.value = feed
  showDetailModal.value = true
  comments.value = [] // 열 때 초기화
  await fetchComments(feed.id)

  if (currentUsername.value && feed.creator !== currentUsername.value) {
    try {
      const res = await api.get(`/users/${feed.creator}/follow/status`)
      selectedFeed.value.isFollowing = res.data.isFollowing
    } catch (e) {
      console.error('팔로우 상태 조회 실패', e)
    }
  }
}

const toggleFollowCreator = async () => {
  if (!selectedFeed.value || !currentUsername.value) return
  try {
    const res = await api.post(`/users/${selectedFeed.value.creator}/follow`)
    selectedFeed.value.isFollowing = res.data.isFollowing
  } catch (error) {
    console.error('팔로우 처리 실패', error)
    if (error.response?.status === 401) {
      alert('로그인이 필요한 기능입니다.')
    }
  }
}

// 닫기 함수
const closeDetail = () => {
  showDetailModal.value = false
  selectedFeed.value = null
  comments.value = []
}

// 호버 시 비디오 재생
const playVideo = (event, feed) => {
  if (feed.videoUrl) {
    const card = event.currentTarget
    const video = card.querySelector('.pin-video')
    const img = card.querySelector('.pin-image')
    if (video) {
      video.style.display = 'block'
      if (img) img.style.display = 'none'
      video.play().catch(e => console.error("자동재생 실패:", e))
    }
  }
}

// 마우스가 벗어날 때 비디오 멈춤
const stopVideo = (event, feed) => {
  if (feed.videoUrl) {
    const card = event.currentTarget
    const video = card.querySelector('.pin-video')
    const img = card.querySelector('.pin-image')
    if (video) {
      video.pause()
      video.currentTime = 0
      video.style.display = 'none'
      if (img) img.style.display = 'block'
    }
  }
}

// 상호작용(좋아요, 북마크) 로직

// 좋아요 토글 함수
const toggleLike = async (feed) => {
  try {
    const response = await api.post(
      `/feeds/${feed.id}/like?username=${currentUsername.value}`
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
    const response = await api.post(
      `/feeds/${feed.id}/bookmark?username=${currentUsername.value}`
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
    // 1. 프로필 정보를 미리 로드하여 isAdmin과 currentUsername 세팅
    await fetchProfile()
    
    // 2. 전체 피드 목록 조회
    const response = await api.get('/feeds')
    mixedFeeds.value = response.data
  } catch (error) {
    console.error('서버 연결 실패', error)
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
  stopChatPolling() // 다른 메뉴로 이동 시 채팅 polling 중지

  try {
    if (menuType === 'profile') {
      fetchProfile()
      fetchTabFeeds(activeTab.value)
      return
    }

    let url = '/feeds'

    // 카테고리 필터링일 때
    if (menuType !== 'all' && menuType !== 'bookmark' && menuType !== 'following') {
      url = `/feeds?type=${menuType}`
    }
    // 북마크 모아보기일 때
    else if (menuType === 'bookmark') {
      url = `/feeds/bookmarks?username=${currentUsername.value}`
    }
    // 팔로잉 피드일 때
    else if (menuType === 'following') {
      url = `/feeds/following?username=${currentUsername.value}`
    }

    const response = await api.get(url)
    mixedFeeds.value = response.data
  } catch (error) {
    console.error('데이터 로드 실패:', error)
  }
}

// ==== 프로필 관련 로직 시작 ====
const userProfile = ref({
  username: '',
  nickname: '',
  profileImageUrl: '',
  tag: '',
  postCount: 0,
  followerCount: 0,
  followingCount: 0,
  isAdmin: false
})
const isAdmin = ref(false)
const activeTab = ref('posts') // 'posts', 'interactions', 'collabs'
const postsFeeds = ref([])
const interactionsFeeds = ref([])
const collabsFeeds = ref([])

const showEditModal = ref(false)
const editForm = ref({ nickname: '', tag: '' })
const profileSelectedFile = ref(null)

const directProfileImageInput = ref(null)

const triggerDirectProfileImageUpload = () => {
  if (directProfileImageInput.value) {
    directProfileImageInput.value.click()
  }
}

const handleDirectProfileImageUpload = async (e) => {
  const file = e.target.files[0]
  if (file) {
    try {
      const formData = new FormData()
      formData.append('profileImage', file)
      const res = await api.put('/users/profile', formData, {
        headers: { 'Content-Type': 'multipart/form-data' }
      })
      userProfile.value = res.data
    } catch (error) {
      console.error('프로필 사진 변경 실패', error)
      alert('프로필 사진을 업데이트하지 못했습니다.')
    }
  }
}

const currentProfileFeeds = computed(() => {
  if (activeTab.value === 'posts') return postsFeeds.value
  if (activeTab.value === 'interactions') return interactionsFeeds.value
  if (activeTab.value === 'collabs') return collabsFeeds.value
  return []
})

const fetchProfile = async () => {
  try {
    const res = await api.get('/users/profile')
    userProfile.value = res.data
    isAdmin.value = res.data.isAdmin
    if (res.data.username) {
      currentUsername.value = res.data.username
    }
  } catch (error) {
    console.error('프로필 로드 실패', error)
  }
}

const fetchTabFeeds = async (tab) => {
  try {
    if (tab === 'posts') {
      const res = await api.get(`/feeds/user?username=${currentUsername.value}`)
      postsFeeds.value = res.data
    } else if (tab === 'interactions') {
      // 보관, 좋아요, 댓글 단 게시물 병합
      const [bookmarks, liked, commented] = await Promise.all([
        api.get(`/feeds/bookmarks?username=${currentUsername.value}`).catch(()=>({data:[]})),
        api.get(`/feeds/liked?username=${currentUsername.value}`).catch(()=>({data:[]})),
        api.get(`/feeds/commented?username=${currentUsername.value}`).catch(()=>({data:[]}))
      ])
      const combined = [...bookmarks.data, ...liked.data, ...commented.data]
      const uniqueFeeds = combined.reduce((acc, current) => {
        const x = acc.find(item => item.id === current.id);
        if (!x) return acc.concat([current]);
        return acc;
      }, []);
      // 최신순 정렬 (ID 기준 내림차순)
      uniqueFeeds.sort((a, b) => b.id - a.id)
      interactionsFeeds.value = uniqueFeeds
    } else if (tab === 'collabs') {
      // 나중에 추가될 협업 이력
      collabsFeeds.value = []
    }
  } catch (error) {
    console.error(`피드 로드 실패 (${tab})`, error)
  }
}

const changeTab = (tab) => {
  activeTab.value = tab
  fetchTabFeeds(tab)
}

const openEditModal = () => {
  editForm.value.nickname = userProfile.value.nickname || ''
  editForm.value.tag = userProfile.value.tag || ''
  profileSelectedFile.value = null
  showEditModal.value = true
}

const handleProfileFileChange = (e) => {
  const file = e.target.files[0]
  if (file) {
    profileSelectedFile.value = file
  }
}

const saveProfile = async () => {
  try {
    const formData = new FormData()
    if (editForm.value.nickname) {
      formData.append('nickname', editForm.value.nickname)
    }
    if (editForm.value.tag) {
      formData.append('tag', editForm.value.tag)
    }
    if (profileSelectedFile.value) {
      formData.append('profileImage', profileSelectedFile.value)
    }
    
    const res = await api.put('/users/profile', formData, {
      headers: { 'Content-Type': 'multipart/form-data' }
    })
    userProfile.value = res.data
    showEditModal.value = false
  } catch (error) {
    console.error('프로필 저장 실패', error)
    alert('프로필 업데이트에 실패했습니다.')
  }
}
// ==== 프로필 관련 로직 끝 ====

// ==== DM 채팅 관련 로직 시작 ====
const chatRooms = ref([])
const selectedRoom = ref(null)
const chatMessages = ref([])
const newMessage = ref('')
const showNewChatModal = ref(false)
const userSearchKeyword = ref('')
const userSearchResults = ref([])
const totalUnreadCount = ref(0)
const dmMessagesContainer = ref(null)
let chatPollingTimer = null

const openMessages = () => {
  currentMenu.value = 'messages'
  loadChatRooms()
  // 5초 간격 polling 시작
  stopChatPolling()
  chatPollingTimer = setInterval(() => {
    loadChatRooms()
    if (selectedRoom.value) {
      loadChatMessages(selectedRoom.value.roomId)
    }
  }, 5000)
}

const stopChatPolling = () => {
  if (chatPollingTimer) {
    clearInterval(chatPollingTimer)
    chatPollingTimer = null
  }
}

const loadChatRooms = async () => {
  try {
    const res = await api.get('/chat/rooms')
    chatRooms.value = res.data
    totalUnreadCount.value = res.data.reduce((sum, r) => sum + r.unreadCount, 0)
  } catch (e) {
    console.error('채팅방 목록 로드 실패', e)
  }
}

const selectRoom = async (room) => {
  selectedRoom.value = room
  await loadChatMessages(room.roomId)
  // 읽음 처리 후 목록 갱신
  loadChatRooms()
}

const loadChatMessages = async (roomId) => {
  try {
    const res = await api.get(`/chat/rooms/${roomId}/messages`)
    chatMessages.value = res.data
    // 스크롤을 맨 아래로
    setTimeout(() => {
      if (dmMessagesContainer.value) {
        dmMessagesContainer.value.scrollTop = dmMessagesContainer.value.scrollHeight
      }
    }, 50)
  } catch (e) {
    console.error('메시지 로드 실패', e)
  }
}

const sendChatMessage = async () => {
  if (!newMessage.value.trim() || !selectedRoom.value) return
  try {
    await api.post(`/chat/rooms/${selectedRoom.value.roomId}/messages`, {
      content: newMessage.value.trim()
    })
    newMessage.value = ''
    await loadChatMessages(selectedRoom.value.roomId)
    loadChatRooms()
  } catch (e) {
    console.error('메시지 전송 실패', e)
  }
}

let searchTimeout = null
const searchUsersForChat = () => {
  clearTimeout(searchTimeout)
  if (!userSearchKeyword.value.trim()) {
    userSearchResults.value = []
    return
  }
  searchTimeout = setTimeout(async () => {
    try {
      const res = await api.get(`/chat/users/search?keyword=${userSearchKeyword.value}`)
      userSearchResults.value = res.data
    } catch (e) {
      console.error('유저 검색 실패', e)
    }
  }, 300)
}

const startChatWith = async (user) => {
  try {
    const res = await api.post(`/chat/rooms?otherUsername=${user.username}`)
    showNewChatModal.value = false
    userSearchKeyword.value = ''
    userSearchResults.value = []
    await loadChatRooms()
    // 생성된 방 선택
    selectedRoom.value = res.data
    await loadChatMessages(res.data.roomId)
  } catch (e) {
    console.error('채팅방 생성 실패', e)
    alert(e.response?.data || '채팅방 생성에 실패했습니다.')
  }
}

const startChatWithCreator = async (feed) => {
  if (!feed || !feed.creator) return
  if (feed.creator === currentUsername.value) {
    alert('자신에게는 메시지를 보낼 수 없습니다.')
    return
  }
  
  // 모달 닫기
  closeDetail()
  
  // 채팅 시작
  await startChatWith({ username: feed.creator })
  
  // 메시지 탭으로 전환
  if (currentMenu.value !== 'messages') {
    openMessages()
  }
}

const formatChatTime = (timeStr) => {
  if (!timeStr) return ''
  const date = new Date(timeStr)
  const now = new Date()
  const diff = now - date

  if (diff < 60000) return '방금'
  if (diff < 3600000) return Math.floor(diff / 60000) + '분 전'
  if (diff < 86400000) return Math.floor(diff / 3600000) + '시간 전'
  if (diff < 604800000) return Math.floor(diff / 86400000) + '일 전'

  return date.toLocaleDateString('ko-KR', { month: 'short', day: 'numeric' })
}
// ==== DM 채팅 관련 로직 끝 ====

// 💡 페이지가 처음 켜질 때는 전체(all) 데이터를 불러옵니다.
onMounted(() => {
  loadFeeds('all')
})

// 서버로 데이터 전송 (POST)
const createNewFeed = async () => {
  const randomHeight = Math.floor(Math.random() * 200) + 200

  // JSON 대신 FormData 객체에 데이터 쌓아서 보내기
  const payload = new FormData()
  payload.append('content', formData.value.content || '')
  payload.append('creator', formData.value.creator)
  payload.append('type', formData.value.type)
  payload.append('tag', formData.value.tag || 'Daily')
  payload.append('height', randomHeight)
  payload.append('isCollab', formData.value.isCollab)
  payload.append('hideCounts', formData.value.hideCounts)
  payload.append('disableComments', formData.value.disableComments)

  if (selectedFile.value) {
    payload.append('file', selectedFile.value) // 원본 파일
  }
  if (selectedThumbnail.value) {
    payload.append('thumbnail', selectedThumbnail.value, 'thumbnail.jpg') // 추출된 썸네일
  }

  try {
    const response = await api.post('/feeds', payload, {
      headers: { 'Content-Type': 'multipart/form-data' }, // 헤더 명시
    })

    mixedFeeds.value.unshift(response.data)

    // 초기화
    closeCreateModal()
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

.default-feed-content {
  display: flex;
  flex-direction: column;
  flex: 1;
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
  column-count: 5;
  column-gap: 12px;
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
  margin-bottom: 12px;
  break-inside: avoid;
  position: relative;
  overflow: hidden;
  display: flex;
  align-items: flex-end;
  padding: 0; /* Remove padding to let image fill entirely */
  cursor: pointer;
}
.pin-card::after {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-color: rgba(0, 0, 0, 0.4);
  opacity: 0;
  transition: opacity 0.2s ease-in-out;
  pointer-events: none;
  z-index: 1;
}
.pin-card:hover::after {
  opacity: 1;
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
  padding: 0;
}
.pin-card:hover .card-actions {
  opacity: 1;
}
.save-btn {
  background-color: #e60023; /* Pinterest Red */
  color: white;
  border: none;
  padding: 10px 18px;
  border-radius: 24px;
  font-weight: 700;
  font-size: 15px;
  cursor: pointer;
}
.save-btn:hover {
  background-color: #b5001c;
}

/* =========================================
   더 보기 메뉴 CSS
   ========================================= */
.more-menu-container {
  margin-top: auto; /* 사이드바 하단에 붙이기 */
  position: relative;
  width: 100%;
}
.more-btn {
  display: flex;
  align-items: center;
  justify-content: flex-start;
  width: calc(100% - 24px);
  margin: 0 12px 20px 12px;
  padding: 12px 16px;
  background-color: transparent;
  color: #a0a0a0;
  border: none;
  border-radius: 12px;
  font-weight: 600;
  font-size: 16px;
  cursor: pointer;
  transition: all 0.2s;
}
.more-btn:hover {
  background-color: #242424;
  color: #ffffff;
}
.more-popup {
  position: absolute;
  bottom: 70px;
  left: 12px;
  width: 250px;
  background-color: #262626;
  border-radius: 16px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.5);
  overflow: hidden;
  z-index: 100;
}
.more-popup ul {
  list-style: none;
  padding: 8px;
  margin: 0;
  display: flex;
  flex-direction: column;
}
.more-popup li {
  padding: 12px 16px;
  color: #fff;
  font-size: 14px;
  display: flex;
  align-items: center;
  gap: 12px;
  cursor: pointer;
  border-radius: 8px;
  transition: background-color 0.2s;
}
.more-popup li:hover {
  background-color: #363636;
}
.more-popup hr {
  border: none;
  border-top: 1px solid #363636;
  margin: 4px 0;
}
.menu-icon {
  width: 18px;
  height: 18px;
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
  background: rgba(0, 0, 0, 0.65);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 999;
}

.modal-content.new-modal {
  background: #262626;
  border-radius: 12px;
  overflow: hidden;
  display: flex;
  flex-direction: column;
  box-shadow: 0 20px 60px rgba(0,0,0,0.5);
  transition: width 0.3s ease, height 0.3s ease;
  width: 500px;
  height: 540px;
}

.modal-content.step2-modal {
  width: 850px;
  height: 550px;
}

.modal-header-centered {
  text-align: center;
  padding: 12px;
  border-bottom: 1px solid #363636;
}
.modal-header-centered h2 {
  font-size: 16px;
  font-weight: 600;
  margin: 0;
  color: #f5f5f5;
}

.modal-header-nav {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 16px;
  border-bottom: 1px solid #363636;
}
.modal-header-nav h2 {
  font-size: 16px;
  font-weight: 600;
  margin: 0;
  color: #f5f5f5;
}
.back-btn {
  background: none;
  border: none;
  color: #f5f5f5;
  font-size: 20px;
  cursor: pointer;
}
.share-btn {
  background: none;
  border: none;
  color: #0095f6;
  font-weight: 600;
  font-size: 14px;
  cursor: pointer;
}
.share-btn:hover {
  color: #1877f2;
}

/* Step 1 */
.modal-step-1 {
  display: flex;
  flex-direction: column;
  height: 100%;
}
.drag-drop-area {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 16px;
  cursor: pointer;
}
.icon-group {
  position: relative;
  display: flex;
  justify-content: center;
}
.media-icon {
  width: 70px;
  height: 70px;
  color: #f5f5f5;
  opacity: 0.8;
}
.video-icon {
  position: absolute;
  right: -20px;
  bottom: -10px;
  width: 40px;
  height: 40px;
  color: #f5f5f5;
  background: #262626;
  border-radius: 8px;
  padding: 2px;
}
.drag-drop-area h3 {
  font-size: 20px;
  font-weight: 400;
  margin: 10px 0;
}
.select-file-btn {
  background-color: #0095f6;
  color: #fff;
  border: none;
  border-radius: 8px;
  padding: 8px 16px;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
}
.select-file-btn:hover {
  background-color: #1877f2;
}

/* Step 2 */
.modal-step-2 {
  display: flex;
  flex-direction: column;
  height: 100%;
}
.modal-body-split {
  display: flex;
  flex: 1;
  height: calc(100% - 49px);
}
.media-preview-pane {
  width: 500px;
  background: #000;
  display: flex;
  align-items: center;
  justify-content: center;
  border-right: 1px solid #363636;
}
.preview-media {
  max-width: 100%;
  max-height: 100%;
  object-fit: contain;
}

.details-pane {
  flex: 1;
  display: flex;
  flex-direction: column;
  background: #262626;
  overflow-y: auto;
}
.user-header {
  display: flex;
  align-items: center;
  padding: 16px;
  gap: 12px;
}
.avatar-placeholder {
  width: 28px;
  height: 28px;
  background: #444;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
}
.icon-small {
  width: 16px;
  height: 16px;
  color: #fff;
}
.username {
  font-weight: 600;
  font-size: 14px;
}

.caption-input {
  width: 100%;
  height: 160px;
  background: transparent;
  border: none;
  color: #f5f5f5;
  padding: 0 16px;
  resize: none;
  font-size: 15px;
  outline: none;
}
.char-count {
  text-align: right;
  padding: 0 16px 12px;
  font-size: 12px;
  color: #a8a8a8;
  border-bottom: 1px solid #363636;
}

.form-row {
  padding: 12px 16px;
  border-bottom: 1px solid #363636;
}
.minimal-input {
  width: 100%;
  background: transparent;
  border: none;
  color: #f5f5f5;
  font-size: 15px;
  outline: none;
}

/* Advanced Settings Toggle */
.advanced-settings {
  display: flex;
  flex-direction: column;
}
.settings-header {
  padding: 14px 16px;
  display: flex;
  justify-content: space-between;
  cursor: pointer;
  font-size: 15px;
  color: #f5f5f5;
  border-bottom: 1px solid #363636;
}
.settings-body {
  background: #262626;
  display: flex;
  flex-direction: column;
}
.setting-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px;
  border-bottom: 1px solid #363636;
}
.setting-text h4 {
  margin: 0 0 4px 0;
  font-size: 15px;
  font-weight: 500;
}
.setting-text p {
  margin: 0;
  font-size: 12px;
  color: #a8a8a8;
  line-height: 1.4;
  max-width: 250px;
}

/* Toggle Switch CSS */
.toggle-switch {
  position: relative;
  display: inline-block;
  width: 44px;
  height: 24px;
}
.toggle-switch input {
  opacity: 0;
  width: 0;
  height: 0;
}
.slider {
  position: absolute;
  cursor: pointer;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: #555;
  transition: .4s;
}
.slider:before {
  position: absolute;
  content: "";
  height: 18px;
  width: 18px;
  left: 3px;
  bottom: 3px;
  background-color: white;
  transition: .4s;
}
input:checked + .slider {
  background-color: #0095f6;
}
input:checked + .slider:before {
  transform: translateX(20px);
}
.slider.round {
  border-radius: 24px;
}
.slider.round:before {
  border-radius: 50%;
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

.pin-image,
.pin-video {
  width: 100%;
  height: auto;
  display: block;
  object-fit: cover;
  z-index: 0;
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

/* 왼쪽 이미지/비디오 영역 */
.detail-media-pane {
  flex: 6; /* 60% 차지 */
  background: #000;
  display: flex;
  align-items: center;
  justify-content: center;
  max-height: 90vh;
}
.media-full {
  width: 100%;
  height: 100%;
  object-fit: contain;
}
.no-image {
  color: #555;
  font-size: 18px;
}

/* 오른쪽 정보 및 댓글 영역 */
.detail-info-pane {
  flex: 4; /* 40% 차지 */
  background: #000; /* 블랙 배경 */
  display: flex;
  flex-direction: column;
  border-left: 1px solid #262626; /* 인스타그램 특유의 구분선 */
}

/* 1. 헤더 (작성자 프로필) */
.detail-info-header {
  padding: 14px 16px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  border-bottom: 1px solid #262626;
}
.user-info {
  display: flex;
  align-items: center;
  gap: 12px;
}
.more-options-btn {
  background: none;
  border: none;
  color: #fff;
  font-size: 20px;
  cursor: pointer;
  padding: 0;
  line-height: 1;
}

/* 2. 본문 및 댓글 영역 (스크롤) */
.comments-section {
  flex: 1;
  overflow-y: auto;
  padding: 16px;
}
.comments-section::-webkit-scrollbar {
  display: none; /* 스크롤바 숨김 */
}
.comment-item {
  display: flex;
  gap: 12px;
  margin-bottom: 16px;
}
.caption-item {
  margin-bottom: 24px;
}
.comment-content {
  display: flex;
  flex-direction: column;
  font-size: 14px;
  line-height: 1.4;
}
.comment-author {
  color: #fff;
  margin-right: 6px;
}
.comment-text {
  color: #f5f5f5;
  word-break: break-word;
}
.comment-meta {
  margin-top: 8px;
  font-size: 12px;
  color: #a8a8a8;
  display: flex;
  gap: 12px;
}
.comment-date {
  color: #a8a8a8;
}

/* 3. 액션 바 (좋아요, 공유 등) */
.detail-action-bar {
  padding: 12px 16px;
  border-top: 1px solid #262626;
}
.action-icons {
  display: flex;
  justify-content: space-between;
  margin-bottom: 8px;
}
.left-icons {
  display: flex;
  gap: 16px;
}
.right-icons {
  display: flex;
}
.icon-btn {
  background: none;
  border: none;
  padding: 0;
  cursor: pointer;
  color: #fff;
  transition: opacity 0.2s;
}
.icon-btn:hover {
  opacity: 0.7;
}
.likes-count {
  font-size: 14px;
  font-weight: 600;
  margin-bottom: 4px;
}
.post-date {
  font-size: 12px;
  color: #a8a8a8;
}

/* 4. 댓글 입력창 */
.comment-input-area {
  padding: 12px 16px;
  border-top: 1px solid #262626;
  display: flex;
  align-items: center;
}
.comment-input {
  flex: 1;
  background: none;
  border: none;
  color: #fff;
  font-size: 14px;
  outline: none;
}
.comment-input::placeholder {
  color: #a8a8a8;
}
.post-comment-btn {
  background: none;
  border: none;
  color: #0095f6;
  font-weight: 600;
  font-size: 14px;
  cursor: pointer;
  padding: 0 0 0 12px;
}
.post-comment-btn:disabled {
  color: #00376b;
  cursor: default;
}
.disabled-comments {
  color: #a8a8a8;
  font-size: 14px;
  justify-content: center;
}
/* ==== 프로필 뷰 스타일 ==== */
.profile-page-content {
  width: 100%;
  max-width: 935px;
  margin: 0 auto;
}
.profile-header {
  display: flex;
  gap: 60px;
  margin-bottom: 44px;
}
.profile-avatar-container {
  position: relative;
  width: 150px;
  height: 150px;
  border-radius: 50%;
  overflow: hidden;
  cursor: pointer;
  background-color: #262626;
  flex-shrink: 0;
}
.profile-avatar-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}
.avatar-placeholder-large {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #737373;
  position: relative;
}
.camera-icon-overlay {
  position: absolute;
  top: 10px;
  right: 10px;
  background: rgba(0,0,0,0.5);
  border-radius: 50%;
  padding: 4px;
}
.profile-info-section {
  display: flex;
  flex-direction: column;
  justify-content: center;
}
.profile-info-top {
  display: flex;
  align-items: center;
  gap: 20px;
  margin-bottom: 20px;
}
.username-title {
  font-size: 20px;
  font-weight: 400;
  margin: 0;
  display: flex;
  align-items: center;
  gap: 10px;
}
.profile-actions {
  display: flex;
  gap: 10px;
  margin-top: 15px;
}
.profile-stats {
  display: flex;
  gap: 40px;
  margin-bottom: 20px;
  font-size: 16px;
}
.profile-nickname {
  font-size: 14px;
  font-weight: 600;
}
.profile-tabs {
  display: flex;
  justify-content: center;
  gap: 60px;
  border-top: 1px solid #262626;
  margin-bottom: 20px;
}
.tab-btn {
  background: none;
  border: none;
  border-top: 1px solid transparent;
  color: #8e8e8e;
  padding: 15px 0;
  font-size: 12px;
  font-weight: 600;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 6px;
  text-transform: uppercase;
}
.tab-btn.active {
  color: #f5f5f5;
  border-top: 1px solid #f5f5f5;
}
.feeds-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 5px;
}
.feed-thumbnail-wrapper {
  position: relative;
  width: 100%;
  padding-bottom: 100%;
  background-color: #262626;
  overflow: hidden;
  cursor: pointer;
}
.feed-thumbnail {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  object-fit: cover;
}
.placeholder-thumbnail {
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  color: #8e8e8e;
  padding: 10px;
  text-align: center;
  word-break: break-all;
}
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  margin-top: 60px;
  color: #f5f5f5;
}
.empty-icon-circle {
  width: 60px;
  height: 60px;
  border: 2px solid #f5f5f5;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 20px;
  font-size: 30px;
}
.edit-modal-content {
  background: #262626;
  border-radius: 12px;
  padding: 24px;
  width: 400px;
}
.edit-form-group {
  margin-bottom: 20px;
  display: flex;
  flex-direction: column;
  gap: 8px;
}
.edit-input, .file-input {
  padding: 10px;
  border-radius: 6px;
  border: 1px solid #363636;
  background: #121212;
  color: #fff;
}
.edit-modal-actions {
  display: flex;
  justify-content: space-between;
  margin-top: 30px;
}

/* ==== 모던 프로필 센터 정렬 스타일 ==== */
.profile-header-centered {
  display: flex;
  flex-direction: column;
  align-items: center;
  margin-top: 40px;
  margin-bottom: 20px;
}
.profile-avatar-container.large-center {
  width: 150px;
  height: 150px;
  margin-bottom: 24px;
}
.profile-info-section-centered {
  display: flex;
  flex-direction: column;
  align-items: center;
  width: 100%;
}
.profile-info-top-centered {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 8px;
}
.username-title {
  font-size: 24px;
  font-weight: 500;
  display: flex;
  align-items: center;
  gap: 8px;
}
.profile-nickname-centered {
  font-size: 14px;
  color: #a8a8a8;
  margin-bottom: 20px;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
}
.profile-tag {
  color: #f5f5f5;
  font-weight: 400;
}
.profile-stats-centered {
  display: flex;
  gap: 30px;
  margin-bottom: 24px;
  font-size: 16px;
}
.profile-actions-centered {
  display: flex;
  gap: 10px;
  margin-bottom: 40px;
}
.action-btn.wide {
  padding: 8px 30px;
  background-color: #363636;
  color: #fff;
  border-radius: 8px;
  font-weight: 600;
  font-size: 14px;
}
.action-btn.wide:hover {
  background-color: #262626;
}
.profile-highlights {
  display: flex;
  gap: 15px;
  margin-bottom: 40px;
  width: 100%;
  justify-content: flex-start;
  padding-left: 20px; /* Aligns with grid */
}
.highlight-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  cursor: pointer;
}
.highlight-circle {
  width: 70px;
  height: 70px;
  border-radius: 50%;
  border: 1px solid #363636;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #a8a8a8;
}
.highlight-circle:hover {
  color: #fff;
  border-color: #fff;
}
.highlight-text {
  font-size: 12px;
  color: #f5f5f5;
  font-weight: 500;
}
.profile-tabs-centered {
  display: flex;
  justify-content: center;
  border-top: 1px solid #262626;
  gap: 100px;
}
.tab-btn-icon {
  background: none;
  border: none;
  border-top: 1px solid transparent;
  color: #8e8e8e;
  padding: 16px 0;
  cursor: pointer;
  display: flex;
  align-items: center;
}
.tab-btn-icon.active {
  color: #f5f5f5;
  border-top: 1px solid #f5f5f5;
}
.tab-icon-large {
  width: 24px;
  height: 24px;
}
.empty-state-modern {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 100px 0;
  color: #f5f5f5;
}
.empty-icon-circle-large {
  width: 80px;
  height: 80px;
  border-radius: 50%;
  border: 2px solid #f5f5f5;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 24px;
}
.empty-camera-icon {
  width: 40px;
  height: 40px;
}
.empty-state-modern h2 {
  font-size: 28px;
  font-weight: 800;
  margin-bottom: 12px;
}
.empty-state-modern p {
  font-size: 14px;
  color: #a8a8a8;
  margin-bottom: 20px;
}
.share-link {
  color: #0095f6;
  text-decoration: none;
  font-weight: 600;
  font-size: 14px;
}
.share-link:hover {
  color: #fff;
}
/* ==== 핀터레스트 스타일 풀스크린 모달 CSS ==== */
.detail-modal-content.pinterest-style {
  position: relative;
  background: #1e1e1e;
  width: 95vw;
  height: 95vh;
  border-radius: 20px;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.5);
}

.pinterest-top-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 24px;
  background: #1e1e1e;
  border-bottom: 1px solid #333;
}

.back-btn-top {
  background: none;
  border: none;
  color: #fff;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
}

.icon-medium {
  width: 24px;
  height: 24px;
}

.top-bar-actions {
  display: flex;
  align-items: center;
  gap: 16px;
}

.icon-btn-text {
  background: none;
  border: none;
  color: #fff;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 6px;
  font-weight: 600;
  font-size: 14px;
}

.icon-btn-text:hover {
  opacity: 0.8;
}

.profile-dropdown-btn {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
}

.save-btn-red {
  background: #E60023;
  color: white;
  border: none;
  border-radius: 24px;
  padding: 10px 18px;
  font-weight: 700;
  font-size: 14px;
  cursor: pointer;
  transition: background 0.2s;
}

.save-btn-red:hover {
  background: #ad081b;
}
.save-btn-red.is-saved {
  background: #000;
  color: #fff;
}

.pinterest-split-layout {
  display: flex;
  flex: 1;
  overflow: hidden;
  background: #1e1e1e;
}

.pinterest-media-pane {
  flex: 6;
  background: #000;
  display: flex;
  align-items: center;
  justify-content: center;
}

.media-full-pinterest {
  width: 100%;
  height: 100%;
  object-fit: cover; /* 꽉 차게 변경 */
}

.pinterest-info-pane {
  flex: 4;
  display: flex;
  flex-direction: column;
  background: #1e1e1e;
  border-left: 1px solid #333;
}

.pinterest-scrollable-content {
  flex: 1;
  overflow-y: auto;
  padding: 24px;
}

.pinterest-scrollable-content::-webkit-scrollbar {
  display: none;
}

.pinterest-author {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 24px;
}

.avatar-placeholder-medium {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background-color: #363636;
  display: flex;
  align-items: center;
  justify-content: center;
}

.pinterest-description {
  margin-bottom: 24px;
}

.pinterest-description h4 {
  font-size: 16px;
  margin-bottom: 8px;
  font-weight: 600;
}

.description-text {
  font-size: 14px;
  line-height: 1.5;
  color: #dcdcdc;
  margin-bottom: 8px;
}

.see-more {
  font-size: 14px;
  font-weight: 600;
  color: #fff;
  cursor: pointer;
}

.pinterest-comments-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.pinterest-comments-header h4 {
  font-size: 16px;
  font-weight: 600;
}

.comments-section-pinterest {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.comment-item-pinterest {
  display: flex;
  gap: 12px;
}

.avatar-placeholder-small {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background-color: #555;
  display: flex;
  align-items: center;
  justify-content: center;
}

.comment-content-pinterest {
  display: flex;
  flex-direction: column;
  background: #2a2a2a;
  padding: 10px 14px;
  border-radius: 16px;
  max-width: 85%;
}

.comment-author {
  font-size: 13px;
  font-weight: 600;
  margin-bottom: 4px;
}

.comment-text {
  font-size: 14px;
  line-height: 1.4;
}

.pinterest-comment-input-area {
  padding: 16px 24px;
  border-top: 1px solid #333;
  display: flex;
  align-items: center;
  position: relative;
}

.comment-input-box {
  width: 100%;
  background: #333;
  border: none;
  border-radius: 24px;
  padding: 12px 100px 12px 16px;
  color: #fff;
  font-size: 14px;
}

.comment-input-box:focus {
  outline: none;
  background: #444;
}

.input-icons {
  position: absolute;
  right: 36px;
  display: flex;
  gap: 12px;
}

.icon-btn-hover {
  cursor: pointer;
  color: #a8a8a8;
}

.icon-btn-hover:hover {
  color: #fff;
}

.comment-header-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 4px;
}

.comment-actions {
  display: flex;
  gap: 8px;
}

.action-text-btn {
  background: none;
  border: none;
  font-size: 11px;
  font-weight: 600;
  cursor: pointer;
  color: #a8a8a8;
  padding: 0;
}

.action-text-btn:hover {
  color: #fff;
}

.action-text-btn.delete:hover {
  color: #E60023;
}

.edit-comment-input {
  width: 100%;
  background: #1e1e1e;
  border: 1px solid #444;
  border-radius: 4px;
  color: #fff;
  padding: 4px 8px;
  font-size: 13px;
  margin-top: 4px;
}

/* 더보기(옵션) 모달 스타일 */
.more-options-overlay {
  position: absolute;
  top: 0; left: 0; right: 0; bottom: 0;
  background-color: rgba(0, 0, 0, 0.6);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1000;
  border-radius: 16px;
}

.more-options-modal {
  background-color: #262626;
  border-radius: 12px;
  width: 400px;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  box-shadow: 0 4px 12px rgba(0,0,0,0.5);
}

.more-option-btn {
  background: none;
  border: none;
  border-bottom: 1px solid #363636;
  color: #fff;
  padding: 16px 0;
  font-size: 14px;
  font-weight: 400;
  cursor: pointer;
  width: 100%;
  text-align: center;
  transition: background-color 0.2s;
}

.more-option-btn:last-child {
  border-bottom: none;
}

.more-option-btn:hover {
  background-color: #363636;
}

.more-option-btn.text-red {
  color: #ed4956;
  font-weight: 700;
}

/* =========================================
   DM 채팅 CSS
   ========================================= */
.messages-page {
  width: 100%;
  height: 100vh;
  display: flex;
}
.dm-container {
  display: flex;
  width: 100%;
  height: 100%;
  background-color: #000;
}

/* 좌측 사이드바 - 채팅방 목록 */
.dm-sidebar {
  width: 380px;
  min-width: 380px;
  border-right: 1px solid #262626;
  display: flex;
  flex-direction: column;
  background-color: #000;
}
.dm-sidebar-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 20px 24px;
  border-bottom: 1px solid #262626;
}
.dm-sidebar-header h2 {
  font-size: 20px;
  font-weight: 700;
  color: #fafafa;
  margin: 0;
}
.dm-new-chat-btn {
  background: none;
  border: none;
  color: #fafafa;
  cursor: pointer;
  padding: 8px;
  border-radius: 50%;
  transition: background-color 0.2s;
}
.dm-new-chat-btn:hover {
  background-color: #262626;
}
.icon-sm {
  width: 20px;
  height: 20px;
}

/* 채팅방 목록 */
.dm-room-list {
  flex: 1;
  overflow-y: auto;
}
.dm-empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 20px;
  color: #8e8e8e;
}
.dm-empty-icon {
  width: 64px;
  height: 64px;
  margin-bottom: 16px;
  opacity: 0.4;
}
.dm-start-btn {
  margin-top: 16px;
  padding: 10px 24px;
  background-color: #0095f6;
  color: white;
  border: none;
  border-radius: 8px;
  font-weight: 600;
  font-size: 14px;
  cursor: pointer;
  transition: background-color 0.2s;
}
.dm-start-btn:hover {
  background-color: #1877f2;
}

.dm-room-item {
  display: flex;
  align-items: center;
  padding: 12px 24px;
  cursor: pointer;
  transition: background-color 0.15s;
  gap: 12px;
}
.dm-room-item:hover {
  background-color: #1a1a1a;
}
.dm-room-item.active {
  background-color: #262626;
}
.dm-room-avatar {
  flex-shrink: 0;
}
.dm-room-avatar img {
  width: 52px;
  height: 52px;
  border-radius: 50%;
  object-fit: cover;
}
.dm-avatar-placeholder {
  width: 52px;
  height: 52px;
  border-radius: 50%;
  background: linear-gradient(135deg, #833AB4, #FD1D1D, #F77737);
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-weight: 700;
  font-size: 20px;
}
.dm-avatar-placeholder.sm {
  width: 36px;
  height: 36px;
  font-size: 14px;
}
.dm-room-info {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 4px;
}
.dm-room-name {
  color: #fafafa;
  font-size: 15px;
  font-weight: 600;
}
.dm-room-preview {
  color: #8e8e8e;
  font-size: 13px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.dm-room-meta {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 6px;
  flex-shrink: 0;
}
.dm-room-time {
  color: #8e8e8e;
  font-size: 12px;
}
.dm-unread-count {
  background-color: #0095f6;
  color: white;
  font-size: 11px;
  font-weight: 700;
  min-width: 20px;
  height: 20px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 0 6px;
}

/* 우측 채팅 영역 */
.dm-chat-area {
  flex: 1;
  display: flex;
  flex-direction: column;
  background-color: #000;
}
.dm-no-selection {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #8e8e8e;
}
.dm-no-selection-icon {
  width: 80px;
  height: 80px;
  margin-bottom: 16px;
  opacity: 0.3;
}
.dm-no-selection h3 {
  color: #fafafa;
  font-size: 22px;
  margin: 0 0 8px 0;
}
.dm-no-selection p {
  color: #8e8e8e;
  margin: 0;
}

/* 채팅 헤더 */
.dm-chat-header {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 16px 24px;
  border-bottom: 1px solid #262626;
}
.dm-chat-header-avatar img {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  object-fit: cover;
}
.dm-chat-header-name {
  color: #fafafa;
  font-size: 16px;
  font-weight: 700;
}

/* 메시지 목록 */
.dm-messages {
  flex: 1;
  overflow-y: auto;
  padding: 20px 24px;
  display: flex;
  flex-direction: column;
  gap: 8px;
}
.dm-msg {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  max-width: 65%;
}
.dm-msg-mine {
  align-self: flex-end;
  align-items: flex-end;
}
.dm-msg-bubble {
  padding: 10px 16px;
  border-radius: 22px;
  font-size: 14px;
  line-height: 1.5;
  word-break: break-word;
  background-color: #262626;
  color: #fafafa;
}
.dm-msg-mine .dm-msg-bubble {
  background-color: #3797f0;
  color: white;
}
.dm-msg-time {
  font-size: 11px;
  color: #8e8e8e;
  margin-top: 4px;
  padding: 0 8px;
}

/* 메시지 입력 영역 */
.dm-input-area {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 16px 24px;
  border-top: 1px solid #262626;
}
.dm-input {
  flex: 1;
  padding: 12px 18px;
  border-radius: 22px;
  border: 1px solid #363636;
  background-color: #1a1a1a;
  color: #fafafa;
  font-size: 14px;
  outline: none;
  transition: border-color 0.2s;
}
.dm-input:focus {
  border-color: #555;
}
.dm-input::placeholder {
  color: #8e8e8e;
}
.dm-send-btn {
  background-color: #0095f6;
  color: white;
  border: none;
  padding: 10px;
  border-radius: 50%;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: background-color 0.2s;
}
.dm-send-btn:hover {
  background-color: #1877f2;
}
.dm-send-btn:disabled {
  opacity: 0.3;
  cursor: not-allowed;
}

/* 새 대화 모달 */
.dm-new-chat-modal {
  background-color: #262626;
  border-radius: 16px;
  width: 420px;
  max-height: 500px;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}
.dm-modal-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 20px;
  border-bottom: 1px solid #363636;
}
.dm-modal-header h3 {
  margin: 0;
  color: #fafafa;
  font-size: 16px;
  font-weight: 700;
}
.dm-modal-close {
  background: none;
  border: none;
  color: #fafafa;
  font-size: 24px;
  cursor: pointer;
  padding: 0;
  line-height: 1;
}
.dm-modal-search {
  padding: 12px 20px;
}
.dm-search-input {
  width: 100%;
  padding: 10px 16px;
  border-radius: 8px;
  border: 1px solid #363636;
  background-color: #1a1a1a;
  color: #fafafa;
  font-size: 14px;
  outline: none;
  box-sizing: border-box;
}
.dm-search-input:focus {
  border-color: #555;
}
.dm-search-input::placeholder {
  color: #8e8e8e;
}
.dm-modal-results {
  flex: 1;
  overflow-y: auto;
  padding: 8px 0;
}
.dm-search-result-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 10px 20px;
  cursor: pointer;
  transition: background-color 0.15s;
}
.dm-search-result-item:hover {
  background-color: #363636;
}
.dm-search-result-info {
  display: flex;
  flex-direction: column;
  gap: 2px;
}
.dm-search-result-name {
  color: #fafafa;
  font-size: 14px;
  font-weight: 600;
}
.dm-search-result-username {
  color: #8e8e8e;
  font-size: 13px;
}
.dm-no-results {
  text-align: center;
  color: #8e8e8e;
  padding: 20px;
}

/* 사이드바 unread badge */
.unread-badge {
  background-color: #ed4956;
  color: white;
  font-size: 11px;
  font-weight: 700;
  min-width: 18px;
  height: 18px;
  border-radius: 9px;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 0 5px;
  margin-left: auto;
}

/* Follow Button */
.follow-btn {
  background-color: #ff2a5f;
  color: white;
  border: none;
  border-radius: 20px;
  padding: 8px 16px;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s ease;
}
.follow-btn:hover {
  background-color: #e02453;
}
.follow-btn.is-following {
  background-color: #333;
  color: #fff;
}
.follow-btn.is-following:hover {
  background-color: #444;
}
</style>
