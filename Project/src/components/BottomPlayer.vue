<template>
  <footer class="bottom-player">
    <div class="now-playing">
      <p>재생 중: {{ currentSong }}</p>
    </div>
    <div class="controls">
      <button @click="$emit('toggle-play')" class="play-btn">
        {{ isPlaying ? '⏸️ 일시정지' : '▶️ 재생' }}
      </button>
    </div>
  </footer>
</template>

<script setup>
// 1. 부모로부터 내려받을 데이터(Read-Only) 정의
defineProps({
  isPlaying: Boolean,
  currentSong: String
});

// 2. 부모에게 전달할 이벤트 이름 정의
defineEmits(['toggle-play']);
</script>

<style scoped>
.bottom-player {
  background-color: #181818;
  border-top: 1px solid #282828;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 24px;
  height: 100%;
}
.play-btn {
  padding: 10px 20px;
  border-radius: 20px;
  border: none;
  background-color: #fff;
  color: #000;
  font-weight: bold;
  cursor: pointer;
}
</style>