<template>
  <div class="community-detail-container">
    <el-card v-loading="loading" class="detail-card">
      <div slot="header" class="detail-header">
        <el-button icon="el-icon-back" size="small" @click="goBack">返回社群</el-button>
        <div class="header-actions">
          <el-button v-if="isOwner" type="primary" icon="el-icon-edit" size="small" @click="handleEdit">编辑</el-button>
          <el-button v-if="isOwner" type="danger" icon="el-icon-delete" size="small" @click="handleDelete">删除</el-button>
          <el-button icon="el-icon-warning" size="small" @click="openReport(null)">举报</el-button>
        </div>
      </div>

      <div class="detail-title-section">
        <div class="title-badges">
          <el-tag v-if="topic.isTop === '1'" type="danger" size="small" effect="dark">置顶</el-tag>
          <el-tag v-if="topic.isEssence === '1'" type="warning" size="small" effect="dark">精华</el-tag>
<!--          <dict-tag :options="categoryTypeOptions" :value="getCategoryName(topic.category)" />-->
          <dict-tag :options="categoryTypeOptions" :value="topic.category" />
          <el-tag v-if="topic.sourceType === 1" type="success" size="small">观察记录</el-tag>
          <el-tag v-else-if="topic.sourceType === 2" type="warning" size="small">物种鉴定</el-tag>
          <el-tag v-else-if="topic.sourceType === 3" type="primary" size="small">野外调查</el-tag>
        </div>
        <h1 class="detail-title">{{ topic.title }}</h1>
        <div class="detail-meta">
          <span class="meta-item"><i class="el-icon-user"></i> {{ topic.userName }}</span>
          <span class="meta-item"><i class="el-icon-time"></i> {{ parseTime(topic.createTime) }}</span>
          <span class="meta-item"><i class="el-icon-view"></i> {{ topic.viewCount || 0 }} 浏览</span>
          <span class="meta-item"><i class="el-icon-chat-dot-round"></i> {{ topic.commentCount || 0 }} 评论</span>
        </div>
      </div>

      <div v-if="hasSource" class="source-panel">
        <div v-loading="sourceLoading" class="source-inner">
          <div class="source-images">
            <el-carousel v-if="sourceImages.length > 1" :interval="4000" height="340px" indicator-position="outside">
              <el-carousel-item v-for="(img, idx) in sourceImages" :key="idx">
                <el-image :src="img" fit="cover" style="width:100%;height:100%" :preview-src-list="sourceImages" />
              </el-carousel-item>
            </el-carousel>
            <el-image v-else-if="sourceImages.length === 1" :src="sourceImages[0]" fit="cover"
              style="width:100%;height:340px;display:block" :preview-src-list="sourceImages" />
            <div v-else class="no-image"><i class="el-icon-picture-outline"></i><p>暂无图片</p></div>
          </div>
          <div class="source-info">
            <source-observation v-if="topic.sourceType === 1 && sourceData" :data="sourceData" />
            <source-identification v-else-if="topic.sourceType === 2 && sourceData" :data="sourceData" />
            <source-survey v-else-if="topic.sourceType === 3 && sourceData" :data="sourceData" />
            <el-alert v-else-if="!sourceLoading && !sourceData" type="warning" show-icon :closable="false"
              title="原始记录数据暂不可访问" description="您仍可查看话题内容和评论" />
            <div v-if="hasCustomContent" class="detail-section">
              <h3 class="section-title"><i class="el-icon-document"></i> 帖子内容</h3>
              <div class="section-content">{{ topic.content }}</div>
            </div>
          </div>
        </div>
      </div>

      <div v-if="!hasSource && topic.content" class="original-content">
        <div class="section-content">{{ topic.content }}</div>
      </div>

      <div v-if="topic.tags" class="tag-section">
        <el-tag v-for="tag in topic.tags.split(',')" :key="tag" size="small" type="info" effect="plain"
          style="margin-right:8px"># {{ tag.trim() }}</el-tag>
      </div>

      <action-bar
        :is-liked="!!topic.isLiked" :like-count="topic.likeCount"
        :is-collected="!!topic.isCollected" :collect-count="topic.collectCount"
        @like="handleLike" @collect="handleCollect"
      />

      <comment-section
        :comment-list="commentList" :current-user-id="currentUserId"
        @add-comment="handleAddComment" @like-comment="handleLikeComment"
        @delete-comment="handleDeleteComment" @report-comment="openReport"
        @go-profile="goToUserProfile"
      />
    </el-card>

    <report-dialog :visible.sync="reportVisible" :form="reportForm" @submit="submitReport" />
  </div>
</template>
<script>
import {
  getTopic, delTopic,
  likeTopic, unlikeTopic,
  collectTopic, uncollectTopic,
  listComment, addComment, delComment,
  likeComment, unlikeComment,
  getSourceDetail
} from '@/api/user/community'
import { listAllCategory } from '@/api/community/category'
import { addReport } from '@/api/community/report'
import { getDicts } from '@/api/system/dict/data'
import SourceObservation   from './components/SourceObservation'
import SourceIdentification from './components/SourceIdentification'
import SourceSurvey        from './components/SourceSurvey'
import ActionBar           from './components/ActionBar'
import CommentSection      from './components/CommentSection'
import ReportDialog        from './components/ReportDialog'
import DictTag from '@/components/DictTag'

const SYSTEM_CONTENTS = ['来自观察记录的分享', '来自野外调查记录的分享', '来自物种鉴定求助的分享']

export default {
  name: 'CommunityDetail',
  components: { SourceObservation, SourceIdentification, SourceSurvey, ActionBar, CommentSection, ReportDialog, DictTag },
  data() {
    return {
      loading: false,
      topic: {},
      categoryList: [],
      commentList: [],
      currentUserId: this.$store.getters.userId,
      sourceLoading: false,
      sourceData: null,
      reportVisible: false,
      categoryTypeOptions: [],
      reportForm: {
        reportType: '0', targetId: null, targetTitle: null,
        targetUserId: null, targetUserName: null,
        reporterId: null, reporterName: null,
        reason: null, description: null
      }
    }
  },
  computed: {
    isOwner() {
      return this.topic.userId && this.topic.userId === this.currentUserId
    },
    hasSource() {
      const t = this.topic.sourceType
      return t && t !== 0 && t !== null && t !== ''
    },
    hasCustomContent() {
      return this.topic.content && !SYSTEM_CONTENTS.includes(this.topic.content)
    },
    sourceImages() {
      if (!this.sourceData) return []
      const raw = this.sourceData.images
      if (!raw) return []
      try {
        let arr = raw
        // 如果是字符串，尝试解析为 JSON
        if (typeof raw === 'string') {
          // 先尝试作为 JSON 数组解析
          try {
            arr = JSON.parse(raw)
          } catch (e) {
            // 如果不是 JSON 数组，则作为单个 URL 处理
            arr = [raw]
          }
        }
        if (!Array.isArray(arr)) {
          // 如果不是数组，包装成数组
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
        console.error('Failed to parse images:', raw, e)
        return []
      }
    }
  },
  watch: {
    '$route.query.id'(newId) {
      if (newId) {
        this.sourceData = null
        this.topic = {}
        this.commentList = []
        this.loadTopic(newId)
      }
    }
  },
  created() {
    this.getCategoryList()
    this.getDicts('nh_community_category_type').then(res => { this.categoryTypeOptions = res.data || [] })
    const topicId = this.$route.query.id
    if (topicId) this.loadTopic(topicId)
  },
  methods: {
    getCategoryList() {
      listAllCategory().then(r => { this.categoryList = r.data })
    },
    getDicts(dictType) {
      return getDicts(dictType)
    },
    loadTopic(id) {
      this.loading = true
      getTopic(id).then(r => {
        this.topic = r.data
        this.loadComments(id)
        if (this.hasSource && this.topic.sourceId) this.loadSourceDetail()
      }).finally(() => { this.loading = false })
    },
    loadSourceDetail() {
      this.sourceLoading = true
      this.sourceData = null
      getSourceDetail(this.topic.sourceType, this.topic.topicId)
        .then(r => { this.sourceData = r.data })
        .catch(() => { this.sourceData = null })
        .finally(() => { this.sourceLoading = false })
    },
    loadComments(id) {
      listComment({ topicId: id, pageNum: 1, pageSize: 100 }).then(r => { this.commentList = r.rows })
    },
    getCategoryName(val) {
      const item = this.communityCategoryOptions.find(d => d.dictValue == val);
      return item ? item.dictLabel : "未分类";
    },
    goToUserProfile(uid) {
      if (!uid) return
      this.$router.push({ path: '/user/myProfile', query: { userId: uid } })
    },
    handleEdit() {
      this.$router.push({ path: '/user/community/create', query: { id: this.topic.topicId } })
    },
    handleDelete() {
      this.$modal.confirm('确定删除此话题？')
        .then(() => delTopic(this.topic.topicId))
        .then(() => { this.$message.success('删除成功'); this.$router.push('/user/community/list') })
        .catch(() => {})
    },
    handleLike() {
      if (this.topic.isLiked) {
        unlikeTopic(this.topic.topicId).then(() => { this.topic.isLiked = false; this.topic.likeCount-- })
      } else {
        likeTopic(this.topic.topicId).then(() => { this.topic.isLiked = true; this.topic.likeCount++ })
      }
    },
    handleCollect() {
      if (this.topic.isCollected) {
        uncollectTopic(this.topic.topicId).then(() => { this.topic.isCollected = false; this.topic.collectCount-- })
      } else {
        collectTopic(this.topic.topicId).then(() => { this.topic.isCollected = true; this.topic.collectCount++ })
      }
    },
    handleAddComment(content) {
      addComment({ topicId: this.topic.topicId, content, parentId: 0 }).then(() => {
        this.$message.success('评论成功')
        this.loadComments(this.topic.topicId)
        this.topic.commentCount = (this.topic.commentCount || 0) + 1
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
      this.$modal.confirm('确定删除此评论？')
        .then(() => delComment(comment.commentId))
        .then(() => { this.$message.success('删除成功'); this.loadComments(this.topic.topicId); this.topic.commentCount-- })
        .catch(() => {})
    },
    openReport(comment) {
      if (comment) {
        this.reportForm = {
          reportType: '1', targetId: comment.commentId,
          targetTitle: comment.content.substring(0, 50),
          targetUserId: comment.userId, targetUserName: comment.userName,
          reporterId: this.$store.getters.userId, reporterName: this.$store.getters.name,
          reason: null, description: null
        }
      } else {
        this.reportForm = {
          reportType: '0', targetId: this.topic.topicId, targetTitle: this.topic.title,
          targetUserId: this.topic.userId, targetUserName: this.topic.userName,
          reporterId: this.$store.getters.userId, reporterName: this.$store.getters.name,
          reason: null, description: null
        }
      }
      this.reportVisible = true
    },
    submitReport() {
      addReport(this.reportForm).then(() => {
        this.$message.success('举报成功')
        this.reportVisible = false
      })
    }
  }
}
</script>

<style lang="scss" scoped>
.community-detail-container { padding: 20px; max-width: 2000px; margin: 0 auto; }
.detail-card { border-radius: 8px; }
.detail-header { display: flex; justify-content: space-between; align-items: center; }
.header-actions { display: flex; gap: 8px; }
.detail-title-section { margin-bottom: 24px; }
.title-badges { display: flex; flex-wrap: wrap; gap: 6px; margin-bottom: 12px; }
.detail-title { font-size: 24px; font-weight: 700; color: #1a1a2e; margin: 0 0 12px 0; line-height: 1.4; }
.detail-meta { display: flex; flex-wrap: wrap; gap: 16px; }
.meta-item { display: flex; align-items: center; gap: 4px; font-size: 13px; color: #909399; }
.source-panel { margin-bottom: 24px; border: 1px solid #ebeef5; border-radius: 8px; overflow: hidden; }
.source-inner { display: flex; align-items: flex-start; }
.source-images { width: 45%; min-width: 260px; height: 340px; flex-shrink: 0; background: #f5f7fa; overflow: hidden; }
.source-images ::v-deep .el-image, .source-images ::v-deep img { width: 100% !important; height: 100% !important; object-fit: cover !important; }
.source-images ::v-deep .el-carousel, .source-images ::v-deep .el-carousel__container, .source-images ::v-deep .el-carousel__item { height: 100% !important; }
.no-image { display: flex; flex-direction: column; align-items: center; justify-content: center; height: 100%; color: #c0c4cc; }
.source-info { flex: 1; padding: 20px; display: flex; flex-direction: column; gap: 14px; overflow-y: auto; max-height: 400px; }
.source-link-row { padding-top: 4px; }
.original-content { margin-bottom: 20px; padding: 16px; background: #f9fafb; border-radius: 8px; }
.detail-section { margin-bottom: 16px; }
.section-title { font-size: 15px; font-weight: 600; color: #303133; margin: 0 0 12px 0; padding-bottom: 8px; border-bottom: 1px solid #ebeef5; }
.section-content { font-size: 14px; color: #4a4a5a; line-height: 1.8; padding: 12px 16px; background: #f9fafb; border-radius: 6px; }
.tag-section { margin-bottom: 16px; }
@media (max-width: 768px) {
  .source-inner { flex-direction: column; }
  .source-images { width: 100%; }
  .source-info { max-height: none; }
}
</style>
