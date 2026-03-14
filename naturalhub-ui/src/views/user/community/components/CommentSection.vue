<template>
  <div class="comment-section">
    <h3 class="section-title"><i class="el-icon-chat-dot-round"></i> 评论 ({{ commentList.length }})</h3>
    <div class="comment-input">
      <el-input v-model="inputContent" type="textarea" :rows="3" placeholder="发表你的看法..." maxlength="500" show-word-limit />
      <el-button type="primary" style="margin-top:10px" @click="handleAdd">发表评论</el-button>
    </div>
    <div class="comment-list">
      <el-empty v-if="commentList.length === 0" description="暂无评论，快来发表第一条！" :image-size="80" />
      <div v-for="comment in commentList" :key="comment.commentId" class="comment-item">
        <el-avatar :size="36" icon="el-icon-user-solid" style="cursor:pointer;flex-shrink:0"
          @click="$emit('go-profile', comment.userId)" />
        <div class="comment-body">
          <div class="comment-meta">
            <span class="c-name" @click="$emit('go-profile', comment.userId)">{{ comment.userName }}</span>
            <span class="c-time">{{ parseTime(comment.createTime, '{y}-{m}-{d} {h}:{i}') }}</span>
          </div>
          <p class="comment-text">{{ comment.content }}</p>
          <div class="comment-actions">
            <span :class="{ active: comment.isLiked }" @click="$emit('like-comment', comment)">
              <i class="el-icon-thumb"></i> {{ comment.likeCount || 0 }}
            </span>
            <span @click="handleReply(comment)"><i class="el-icon-chat-dot-round"></i> 回复</span>
            <span @click="$emit('report-comment', comment)"><i class="el-icon-warning"></i> 举报</span>
            <span v-if="comment.userId === currentUserId" class="c-danger" @click="$emit('delete-comment', comment)">
              <i class="el-icon-delete"></i> 删除
            </span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'CommentSection',
  props: {
    commentList: { type: Array, default: () => [] },
    currentUserId: { type: Number, default: null }
  },
  data() {
    return { inputContent: '' }
  },
  methods: {
    handleAdd() {
      if (!this.inputContent.trim()) { this.$message.warning('请输入评论内容'); return }
      this.$emit('add-comment', this.inputContent)
      this.inputContent = ''
    },
    handleReply(comment) {
      this.inputContent = '@' + comment.userName + ' '
    }
  }
}
</script>

<style lang="scss" scoped>
.comment-section { border-top: 2px solid #ebeef5; padding-top: 20px; }
.section-title { font-size: 15px; font-weight: 600; color: #303133; margin: 0 0 16px 0; padding-bottom: 8px; border-bottom: 1px solid #ebeef5; }
.comment-input { margin-bottom: 24px; }
.comment-item { display: flex; gap: 12px; padding: 16px 0; border-bottom: 1px solid #f2f2f2; }
.comment-body { flex: 1; }
.comment-meta { display: flex; align-items: center; gap: 10px; }
.c-name { font-weight: 600; cursor: pointer; &:hover { color: #409EFF; } }
.c-time { font-size: 12px; color: #c0c4cc; }
.comment-text { font-size: 14px; color: #4a4a5a; line-height: 1.7; margin: 8px 0; }
.comment-actions {
  display: flex; gap: 16px;
  span { font-size: 12px; color: #909399; cursor: pointer;
    &:hover { color: #409EFF; } &.active { color: #409EFF; } &.c-danger:hover { color: #F56C6C; }
  }
}
</style>
