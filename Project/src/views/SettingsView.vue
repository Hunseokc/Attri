<template>
  <div class="page-container">
    <div class="content-box">
      <h2>설정</h2>
      <p>계정 설정을 관리하는 페이지입니다. (준비 중)</p>
      <button @click="$router.push('/')" class="back-btn">홈으로 돌아가기</button>
    </div>
  </div>
</template>

<style scoped>
.page-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background-color: #121212;
  color: #fff;
}
.content-box {
  background: #242424;
  padding: 40px;
  border-radius: 12px;
  text-align: center;
}
h2 {
  margin-bottom: 20px;
}
.back-btn {
  margin-top: 20px;
  padding: 10px 20px;
  background-color: #0095f6;
  color: white;
  border: none;
  border-radius: 8px;
  cursor: pointer;
}
.back-btn:hover {
  background-color: #1877f2;
}
</style>
