<template>
  <div class="action-bar">
    <el-button :type="isLiked ? 'primary' : 'default'" icon="el-icon-thumb" @click="$emit('like')">
      {{ isLiked ? '已点赞' : '点赞' }} ({{ likeCount || 0 }})
    </el-button>
    <el-button :type="isCollected ? 'warning' : 'default'"
      :icon="isCollected ? 'el-icon-star-on' : 'el-icon-star-off'" @click="$emit('collect')">
      {{ isCollected ? '已收藏' : '收藏' }} ({{ collectCount || 0 }})
    </el-button>
  </div>
</template>

<script>
export default {
  name: 'ActionBar',
  props: {
    isLiked:      { type: Boolean, default: false },
    likeCount:    { type: Number,  default: 0 },
    isCollected:  { type: Boolean, default: false },
    collectCount: { type: Number,  default: 0 }
  }
}
</script>

<style lang="scss" scoped>
.action-bar {
  display: flex;
  gap: 12px;
  padding: 16px 0;
  border-top: 1px solid #ebeef5;
  margin-bottom: 24px;
}
</style>
