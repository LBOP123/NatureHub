<template>
  <div class="placeholder-page">
    <div class="placeholder-content">
      <div class="icon">🚧</div>
      <h2>功能开发中</h2>
      <p>该功能正在努力开发中，敬请期待！</p>
      <el-button type="primary" @click="$router.back()">返回上一页</el-button>
    </div>
  </div>
</template>

<script>
export default {
  name: 'PlaceholderPage'
}
</script>

<style lang="scss" scoped>
.placeholder-page {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 60vh;
  
  .placeholder-content {
    text-align: center;
    
    .icon {
      font-size: 80px;
      margin-bottom: 24px;
    }
    
    h2 {
      font-size: 28px;
      color: #2c3e50;
      margin: 0 0 16px 0;
    }
    
    p {
      font-size: 16px;
      color: #7f8c8d;
      margin: 0 0 32px 0;
    }
  }
}
</style>
