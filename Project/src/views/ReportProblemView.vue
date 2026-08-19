<template>
  <div class="page-container">
    <div class="content-box">
      <h2>문제 신고</h2>
      <p>앱을 사용하면서 발생한 문제를 신고해주세요. (준비 중)</p>
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
  background-color: #ff2a5f;
  color: white;
  border: none;
  border-radius: 8px;
  cursor: pointer;
}
.back-btn:hover {
  background-color: #e0003b;
}
</style>
