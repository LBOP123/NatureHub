<template>
  <div class="comment-section">
    <h3 class="section-title"><i class="el-icon-chat-dot-round"></i> 评论 ({{ commentList.length }})</h3>

    <!-- 主评论输入框 -->
    <div class="comment-input">
      <el-input v-model="inputContent" type="textarea" :rows="3" placeholder="发表你的看法..." maxlength="500" show-word-limit />
      <el-button type="primary" style="margin-top:10px" @click="handleAdd">发表评论</el-button>
    </div>

    <!-- 评论列表 -->
    <div class="comment-list">
      <el-empty v-if="rootComments.length === 0" description="暂无评论，快来发表第一条！" :image-size="80" />

      <div v-for="comment in rootComments" :key="comment.commentId" class="comment-item">
        <!-- 头像 -->
        <el-avatar
          v-if="comment.avatar"
          :size="38"
          :src="getAvatarUrl(comment.avatar)"
          style="cursor:pointer;flex-shrink:0"
          @click="$emit('go-profile', comment.userId)"
        />
        <div
          v-else
          class="avatar-circle"
          :style="{ background: getAvatarColor(comment.userId) }"
          @click="$emit('go-profile', comment.userId)"
        >{{ getInitial(comment.userName) }}</div>

        <div class="comment-body">
          <!-- 评论元信息 -->
          <div class="comment-meta">
            <span class="c-name" @click="$emit('go-profile', comment.userId)">{{ comment.userName }}</span>
            <span v-if="comment.userType === '2'" class="identifier-tag">鉴定者</span>
            <span class="c-time">{{ parseTime(comment.createTime, '{y}-{m}-{d} {h}:{i}') }}</span>
          </div>
          <p class="comment-text">{{ comment.content }}</p>

          <!-- 操作栏 -->
          <div class="comment-actions">
            <span :class="{ active: comment.isLiked }" @click="$emit('like-comment', comment)">
              <i class="el-icon-thumb"></i> {{ comment.likeCount || 0 }}
            </span>
            <span @click="toggleReplyBox(comment.commentId)">
              <i class="el-icon-chat-dot-round"></i> 回复
              <span v-if="getReplies(comment.commentId).length > 0" class="reply-count">({{ getReplies(comment.commentId).length }})</span>
            </span>
            <span @click="$emit('report-comment', comment)"><i class="el-icon-warning"></i> 举报</span>
            <span v-if="comment.userId === currentUserId" class="c-danger" @click="$emit('delete-comment', comment)">
              <i class="el-icon-delete"></i> 删除
            </span>
          </div>

          <!-- 子回复列表 -->
          <div v-if="getReplies(comment.commentId).length > 0" class="replies-wrapper">
            <div v-for="reply in getReplies(comment.commentId)" :key="reply.commentId" class="reply-item">
              <el-avatar
                v-if="reply.avatar"
                :size="28"
                :src="getAvatarUrl(reply.avatar)"
                style="cursor:pointer;flex-shrink:0"
                @click="$emit('go-profile', reply.userId)"
              />
              <div
                v-else
                class="avatar-circle avatar-circle-sm"
                :style="{ background: getAvatarColor(reply.userId) }"
                @click="$emit('go-profile', reply.userId)"
              >{{ getInitial(reply.userName) }}</div>

              <div class="reply-body">
                <div class="comment-meta">
                  <span class="c-name" @click="$emit('go-profile', reply.userId)">{{ reply.userName }}</span>
                  <span v-if="reply.userType === '2'" class="identifier-tag">鉴定者</span>
                  <span v-if="reply.replyToName" class="reply-to">回复 <span class="reply-to-name" @click="$emit('go-profile', reply.replyToUserId)">@{{ reply.replyToName }}</span></span>
                  <span class="c-time">{{ parseTime(reply.createTime, '{y}-{m}-{d} {h}:{i}') }}</span>
                </div>
                <p class="comment-text">{{ reply.content }}</p>
                <div class="comment-actions">
                  <span :class="{ active: reply.isLiked }" @click="$emit('like-comment', reply)">
                    <i class="el-icon-thumb"></i> {{ reply.likeCount || 0 }}
                  </span>
                  <span @click="toggleReplyBox(comment.commentId, reply)"><i class="el-icon-chat-dot-round"></i> 回复</span>
                  <span @click="$emit('report-comment', reply)"><i class="el-icon-warning"></i> 举报</span>
                  <span v-if="reply.userId === currentUserId" class="c-danger" @click="$emit('delete-comment', reply)">
                    <i class="el-icon-delete"></i> 删除
                  </span>
                </div>
              </div>
            </div>
          </div>

          <!-- 内联回复输入框 -->
          <transition name="reply-slide">
            <div v-if="activeReplyId === comment.commentId" class="reply-input-box">
              <span v-if="replyTarget" class="reply-hint">回复 <b>@{{ replyTarget.userName }}</b></span>
              <el-input
                v-model="replyContent"
                type="textarea"
                :rows="2"
                :placeholder="replyTarget ? '回复 @' + replyTarget.userName + '...' : '回复评论...' "
                maxlength="500"
                show-word-limit
                size="small"
              />
              <div class="reply-input-actions">
                <el-button size="mini" @click="cancelReply">取消</el-button>
                <el-button size="mini" type="primary" @click="submitReply(comment)">发送</el-button>
              </div>
            </div>
          </transition>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
const AVATAR_COLORS = [
  '#43a86b', '#2e7d9a', '#8e6bbf', '#c0640a',
  '#c0392b', '#1a6b8a', '#6d8c3e', '#7b4f9e'
]

export default {
  name: 'CommentSection',
  props: {
    commentList: { type: Array, default: () => [] },
    currentUserId: { type: Number, default: null }
  },
  data() {
    return {
      inputContent: '',
      activeReplyId: null,   // 当前展开回复框的根评论 commentId
      replyTarget: null,     // 正在回复的具体评论对象（可能是子回复）
      replyContent: ''
    }
  },
  computed: {
    // 只取顶层评论（parentId 为 0 / null / undefined）
    rootComments() {
      return this.commentList.filter(c => !c.parentId || c.parentId === 0)
    }
  },
  methods: {
    // 获取某条根评论的所有子回复
    getReplies(parentId) {
      return this.commentList.filter(c => c.parentId && c.parentId === parentId)
    },
    // 切换/打开回复框
    toggleReplyBox(rootCommentId, targetComment) {
      if (this.activeReplyId === rootCommentId && !targetComment) {
        // 点同一根评论的回复 -> 关闭
        this.cancelReply()
        return
      }
      this.activeReplyId = rootCommentId
      this.replyTarget = targetComment || null
      this.replyContent = ''
    },
    cancelReply() {
      this.activeReplyId = null
      this.replyTarget = null
      this.replyContent = ''
    },
    submitReply(rootComment) {
      if (!this.replyContent.trim()) {
        this.$message.warning('请输入回复内容')
        return
      }
      // 构建内容：如果是回复某个子评论，前缀 @名字
      let content = this.replyContent
      if (this.replyTarget) {
        content = '@' + this.replyTarget.userName + ' ' + content
      }
      this.$emit('add-reply', {
        content,
        parentId: rootComment.commentId,
        replyToUserId: this.replyTarget ? this.replyTarget.userId : null
      })
      this.cancelReply()
    },
    handleAdd() {
      if (!this.inputContent.trim()) { this.$message.warning('请输入评论内容'); return }
      this.$emit('add-comment', this.inputContent)
      this.inputContent = ''
    },
    getInitial(name) {
      if (!name) return '?'
      return name.trim().charAt(0).toUpperCase()
    },
    getAvatarColor(userId) {
      if (!userId) return AVATAR_COLORS[0]
      return AVATAR_COLORS[userId % AVATAR_COLORS.length]
    },
    getAvatarUrl(avatar) {
      if (!avatar) return ''
      if (avatar.startsWith('http://') || avatar.startsWith('https://')) return avatar
      const base = process.env.VUE_APP_BASE_API || ''
      return base + avatar
    }
  }
}
</script>

<style lang="scss" scoped>
.comment-section { border-top: 2px solid #ebeef5; padding-top: 20px; }
.section-title { font-size: 15px; font-weight: 600; color: #303133; margin: 0 0 16px 0; padding-bottom: 8px; border-bottom: 1px solid #ebeef5; }
.comment-input { margin-bottom: 24px; }

.comment-item {
  display: flex;
  gap: 12px;
  padding: 16px 0;
  border-bottom: 1px solid #f2f2f2;
}

.avatar-circle {
  width: 38px;
  height: 38px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 15px;
  font-weight: 700;
  color: #fff;
  cursor: pointer;
  flex-shrink: 0;
  user-select: none;
  transition: opacity 0.2s;
  &:hover { opacity: 0.85; }
}

.avatar-circle-sm {
  width: 28px;
  height: 28px;
  font-size: 12px;
}

.comment-body { flex: 1; min-width: 0; }
.comment-meta { display: flex; align-items: center; gap: 8px; flex-wrap: wrap; margin-bottom: 4px; }
.c-name { font-weight: 600; cursor: pointer; &:hover { color: #409EFF; } }
.c-time { font-size: 12px; color: #c0c4cc; }

.reply-to {
  font-size: 12px;
  color: #909399;
  .reply-to-name {
    color: #409EFF;
    cursor: pointer;
    font-weight: 500;
    &:hover { text-decoration: underline; }
  }
}

.identifier-tag {
  display: inline-flex;
  align-items: center;
  font-size: 11px;
  font-weight: 600;
  color: #c0640a;
  background: rgba(230, 126, 34, 0.10);
  border: 1px solid rgba(230, 126, 34, 0.30);
  border-radius: 999px;
  padding: 1px 7px;
  line-height: 18px;
}

.comment-text { font-size: 14px; color: #4a4a5a; line-height: 1.7; margin: 6px 0 8px 0; }

.comment-actions {
  display: flex;
  gap: 16px;
  span {
    font-size: 12px;
    color: #909399;
    cursor: pointer;
    &:hover { color: #409EFF; }
    &.active { color: #409EFF; }
    &.c-danger:hover { color: #F56C6C; }
  }
  .reply-count { color: #409EFF; margin-left: 2px; }
}

/* 子回复区域 */
.replies-wrapper {
  margin-top: 10px;
  background: #f7f8fa;
  border-radius: 8px;
  padding: 4px 12px;
}

.reply-item {
  display: flex;
  gap: 10px;
  padding: 10px 0;
  border-bottom: 1px solid #eee;
  &:last-child { border-bottom: none; }
}

.reply-body { flex: 1; min-width: 0; }

/* 内联回复输入框 */
.reply-input-box {
  margin-top: 12px;
  background: #f7f8fa;
  border-radius: 8px;
  padding: 12px;
}

.reply-hint {
  display: block;
  font-size: 12px;
  color: #909399;
  margin-bottom: 6px;
  b { color: #409EFF; }
}

.reply-input-actions {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
  margin-top: 8px;
}

/* 回复框展开动画 */
.reply-slide-enter-active, .reply-slide-leave-active {
  transition: all 0.22s ease;
  overflow: hidden;
}
.reply-slide-enter, .reply-slide-leave-to {
  opacity: 0;
  max-height: 0;
  padding-top: 0;
  padding-bottom: 0;
}
.reply-slide-enter-to, .reply-slide-leave {
  opacity: 1;
  max-height: 200px;
}
</style>
