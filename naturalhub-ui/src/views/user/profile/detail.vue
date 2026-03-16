<template>
  <div class="detail-container">
    <el-card v-loading="loading" class="detail-card">
      <div slot="header" class="detail-header">
        <el-button icon="el-icon-back" size="small" @click="goBack">返回</el-button>
        <div class="header-actions">
          <el-button v-if="isOwner && canEdit" type="primary" icon="el-icon-edit" size="small" @click="handleEdit">编辑</el-button>
          <el-button v-if="isOwner" type="danger" icon="el-icon-delete" size="small" @click="handleDelete">删除</el-button>
        </div>
      </div>

      <div class="detail-title-section">
        <div class="title-badges">
          <el-tag v-if="contentType === 'record'" type="success" size="small">观察记录</el-tag>
          <el-tag v-else-if="contentType === 'survey'" type="primary" size="small">野外调查</el-tag>
          <el-tag v-else-if="contentType === 'identification'" type="warning" size="small">物种鉴定</el-tag>
          <el-tag :type="getAuditStatusType(data.auditStatus)" size="small">{{ getAuditStatusText(data.auditStatus) }}</el-tag>
        </div>
        <h1 class="detail-title">{{ data.title }}</h1>
        <div class="detail-meta">
          <span class="meta-item"><i class="el-icon-user"></i> {{ data.userName }}</span>
          <span class="meta-item"><i class="el-icon-time"></i> {{ parseTime(data.createTime) }}</span>
          <span class="meta-item"><i class="el-icon-view"></i> {{ data.viewCount || 0 }} 浏览</span>
          <span class="meta-item"><i class="el-icon-chat-dot-round"></i> {{ data.commentCount || 0 }} 评论</span>
        </div>
      </div>

      <!-- 使用社群的组件显示详细信息 -->
      <div v-if="hasImages" class="source-panel">
        <div class="source-images">
          <el-carousel v-if="images.length > 1" :interval="4000" height="340px" indicator-position="outside">
            <el-carousel-item v-for="(img, idx) in images" :key="idx">
              <el-image :src="img" fit="cover" style="width:100%;height:100%" :preview-src-list="images" />
            </el-carousel-item>
          </el-carousel>
          <el-image v-else-if="images.length === 1" :src="images[0]" fit="cover" style="width:100%;height:340px;display:block" :preview-src-list="images" />
        </div>
        <div class="source-info">
          <source-observation v-if="contentType === 'record'" :data="data" />
          <source-survey v-else-if="contentType === 'survey'" :data="data" />
          <source-identification v-else-if="contentType === 'identification'" :data="data" />
        </div>
      </div>

      <!-- 点赞和收藏 -->
      <action-bar
        :is-liked="!!data.isLiked"
        :like-count="data.likeCount"
        :is-collected="!!data.isCollected"
        :collect-count="data.collectCount"
        @like="handleLike"
        @collect="handleCollect"
      />

      <!-- 评论区 -->
      <comment-section
        :comment-list="commentList"
        :current-user-id="currentUserId"
        @add-comment="handleAddComment"
        @like-comment="handleLikeComment"
        @delete-comment="handleDeleteComment"
      />
    </el-card>
  </div>
</template>

<script>
import { listRecord, delRecord } from '@/api/user/record'
import { listSurvey, delSurvey } from '@/api/user/survey'
import { listIdentification, delIdentification } from '@/api/user/identification'
import { listComment, addComment, delComment, likeComment, unlikeComment } from '@/api/user/community'
import SourceObservation from '@/views/user/community/components/SourceObservation'
import SourceSurvey from '@/views/user/community/components/SourceSurvey'
import SourceIdentification from '@/views/user/community/components/SourceIdentification'
import ActionBar from '@/views/user/community/components/ActionBar'
import CommentSection from '@/views/user/community/components/CommentSection'

export default {
  name: 'DetailPage',
  components: {
    SourceObservation,
    SourceSurvey,
    SourceIdentification,
    ActionBar,
    CommentSection
  },
  data() {
    return {
      loading: false,
      contentType: '', // 'record' | 'survey' | 'identification'
      contentId: null,
      data: {},
      commentList: [],
      currentUserId: this.$store.getters.userId,
      auditStatusOptions: []
    }
  },
  computed: {
    isOwner() {
      return this.data.userId && this.data.userId === this.currentUserId
    },
    canEdit() {
      return this.data.auditStatus === 0 || this.data.auditStatus === 3
    },
    hasImages() {
      return this.images && this.images.length > 0
    },
    images() {
      if (!this.data.images) return []
      try {
        let arr = this.data.images
        if (typeof arr === 'string') {
          arr = JSON.parse(arr)
        }
        if (!Array.isArray(arr)) {
          arr = [arr]
        }
        const base = process.env.VUE_APP_BASE_API || ''
        return arr.filter(Boolean).map(url => {
          if (typeof url !== 'string') return null
          if (url.startsWith('http://') || url.startsWith('https://')) return url
          if (url.startsWith('/dev-api') || url.startsWith('/prod-api')) return url
          return base + url
        }).filter(Boolean)
      } catch (e) {
        return []
      }
    }
  },
  created() {
    this.contentType = this.$route.query.type
    this.contentId = this.$route.query.id
    this.loadData()
    this.getDicts('nh_audit_status').then(res => {
      this.auditStatusOptions = res.data || []
    })
  },
  methods: {
    loadData() {
      this.loading = true
      let promise
      if (this.contentType === 'record') {
        promise = listRecord({ pageNum: 1, pageSize: 1, recordId: this.contentId })
      } else if (this.contentType === 'survey') {
        promise = listSurvey({ pageNum: 1, pageSize: 1, surveyId: this.contentId })
      } else if (this.contentType === 'identification') {
        promise = listIdentification({ pageNum: 1, pageSize: 1, identificationId: this.contentId })
      }

      if (promise) {
        promise.then(res => {
          this.data = res.rows && res.rows[0] ? res.rows[0] : {}
          this.loadComments()
        }).finally(() => {
          this.loading = false
        })
      }
    },
    loadComments() {
      const sourceId = this.contentType === 'record' ? this.data.recordId : 
                       this.contentType === 'survey' ? this.data.surveyId : 
                       this.data.identificationId
      listComment({ sourceId, sourceType: this.getSourceType(), pageNum: 1, pageSize: 100 }).then(res => {
        this.commentList = res.rows || []
      })
    },
    getSourceType() {
      if (this.contentType === 'record') return 1
      if (this.contentType === 'survey') return 3
      if (this.contentType === 'identification') return 2
      return 0
    },
    getAuditStatusText(status) {
      const item = this.auditStatusOptions.find(d => d.dictValue == status)
      return item ? item.dictLabel : ""
    },
    getAuditStatusType(status) {
      switch (status) {
        case 0: return ""
        case 1: return "warning"
        case 2: return "success"
        case 3: return "danger"
        default: return ""
      }
    },
    goBack() {
      this.$router.go(-1)
    },
    handleEdit() {
      if (this.contentType === 'record') {
        this.$router.push({ path: '/user/observation/upload', query: { id: this.contentId } })
      } else if (this.contentType === 'survey') {
        this.$router.push({ path: '/user/survey/upload', query: { id: this.contentId } })
      } else if (this.contentType === 'identification') {
        this.$router.push({ path: '/user/identification/upload', query: { id: this.contentId } })
      }
    },
    handleDelete() {
      this.$modal.confirm('确定删除此记录？').then(() => {
        let promise
        if (this.contentType === 'record') {
          promise = delRecord(this.contentId)
        } else if (this.contentType === 'survey') {
          promise = delSurvey(this.contentId)
        } else if (this.contentType === 'identification') {
          promise = delIdentification(this.contentId)
        }
        return promise
      }).then(() => {
        this.$message.success('删除成功')
        this.$router.go(-1)
      })
    },
    handleLike() {
      // 实现点赞逻辑
      if (this.data.isLiked) {
        this.data.isLiked = false
        this.data.likeCount--
      } else {
        this.data.isLiked = true
        this.data.likeCount++
      }
    },
    handleCollect() {
      // 实现收藏逻辑
      if (this.data.isCollected) {
        this.data.isCollected = false
        this.data.collectCount--
      } else {
        this.data.isCollected = true
        this.data.collectCount++
      }
    },
    handleAddComment(content) {
      addComment({ topicId: this.contentId, content, parentId: 0 }).then(() => {
        this.$message.success('评论成功')
        this.loadComments()
        this.data.commentCount = (this.data.commentCount || 0) + 1
      })
    },
    handleLikeComment(comment) {
      if (comment.isLiked) {
        unlikeComment(comment.commentId).then(() => { comment.isLiked = false; comment.likeCount-- })
      } else {
        likeComment(comment.commentId).then(() => { comment.isLiked = true; comment.likeCount = (comment.likeCount || 0) + 1 })
      }
    },
    handleDeleteComment(comment) {
      this.$modal.confirm('确定删除此评论？').then(() => delComment(comment.commentId)).then(() => {
        this.$message.success('删除成功')
        this.loadComments()
        this.data.commentCount--
      })
    }
  }
}
</script>

<style lang="scss" scoped>
.detail-container {
  padding: 20px;
  max-width: 2000px;
  margin: 0 auto;
}

.detail-card {
  border-radius: 8px;
}

.detail-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-actions {
  display: flex;
  gap: 8px;
}

.detail-title-section {
  margin-bottom: 24px;
}

.title-badges {
  display: flex;
  gap: 8px;
  margin-bottom: 12px;
}

.detail-title {
  font-size: 28px;
  font-weight: 600;
  color: #333;
  margin: 0 0 12px 0;
}

.detail-meta {
  display: flex;
  gap: 24px;
  font-size: 14px;
  color: #909399;

  .meta-item {
    display: inline-flex;
    align-items: center;
    gap: 4px;
  }
}

.source-panel {
  display: flex;
  gap: 20px;
  margin-bottom: 24px;

  .source-images {
    flex: 0 0 400px;
  }

  .source-info {
    flex: 1;
  }
}
</style>
