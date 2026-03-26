<template>
  <div class="community-detail-container">
    <el-card v-loading="loading" class="detail-card">
      <div slot="header" class="detail-header">
        <el-button icon="el-icon-back" size="small" @click="goBack">返回社群</el-button>
        <div class="header-actions">
          <el-button v-if="isOwner" type="primary" icon="el-icon-edit" size="small" @click="handleEdit">编辑</el-button>
          <el-button v-if="isOwner" type="danger" icon="el-icon-delete" size="small" @click="handleDelete">删除
          </el-button>
          <el-button icon="el-icon-warning" size="small" @click="openReport(null)">举报</el-button>
        </div>
      </div>

      <div class="detail-title-section">
        <div class="title-badges">
          <el-tag v-if="topic.isTop === '1'" type="danger" size="small" effect="dark">置顶</el-tag>
          <el-tag v-if="topic.isEssence === '1'" type="warning" size="small" effect="dark">精华</el-tag>
          <!--          <dict-tag :options="categoryTypeOptions" :value="getCategoryName(topic.category)" />-->
          <!--          <dict-tag :options="categoryTypeOptions" :value="topic.category" />-->
          <el-tag v-if="topic.sourceType" size="small" :type="(sourceTypeOptions.find(d => d.dictValue == topic.sourceType) || {}).listClass || 'success'">
            {{ (sourceTypeOptions.find(d => d.dictValue == topic.sourceType) || {}).dictLabel || topic.sourceType }}
          </el-tag>
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
            <div v-if="sourceImages.length > 0" class="image-viewer">
              <!-- 主图区域（占满，缩略图叠在上面） -->
              <div class="main-image">
                <transition name="img-fade">
                  <el-image
                    :key="activeImageIdx"
                    :src="sourceImages[activeImageIdx]"
                    fit="cover"
                    class="main-img-el"
                    :preview-src-list="sourceImages"
                  />
                </transition>
                <!-- 多图时显示计数角标 -->
                <span v-if="sourceImages.length > 1" class="image-counter">
                  {{ activeImageIdx + 1 }} / {{ sourceImages.length }}
                </span>
                <!-- 缩略图横向条，叠在主图底部 -->
                <div v-if="sourceImages.length > 1" class="thumb-strip">
                  <div
                    v-for="(img, idx) in sourceImages"
                    :key="idx"
                    :class="['thumb-item', { active: activeImageIdx === idx }]"
                    @click.stop="activeImageIdx = idx"
                  >
                    <el-image :src="img" fit="cover" style="width:100%;height:100%"/>
                  </div>
                </div>
              </div>
            </div>
            <div v-else class="no-image"><i class="el-icon-picture-outline"></i>
              <p>暂无图片</p></div>
          </div>
          <div class="source-info">
            <source-observation v-if="topic.sourceType === 1 && sourceData" :data="sourceData"/>
            <source-identification v-else-if="topic.sourceType === 2 && sourceData" :data="sourceData" mode="panel" @vote-updated="loadSourceDetail"/>
            <source-survey v-else-if="topic.sourceType === 3 && sourceData" :data="sourceData"/>
            <el-alert v-else-if="!sourceLoading && !sourceData" type="warning" show-icon :closable="false"
                      title="原始记录数据暂不可访问" description="您仍可查看话题内容和评论"/>
          </div>
        </div>
      </div>

      <div v-if="!hasSource && topic.content" class="original-content">
        <div class="section-content">{{ topic.content }}</div>
      </div>

      <div v-if="topic.tags" class="tag-section">
        <el-tag v-for="tag in topic.tags.split(',')" :key="tag" size="small" type="info" effect="plain"
                style="margin-right:8px"># {{ tag.trim() }}
        </el-tag>
      </div>

      <action-bar
        :is-liked="!!topic.isLiked" :like-count="topic.likeCount"
        :is-collected="!!topic.isCollected" :collect-count="topic.collectCount"
        @like="handleLike" @collect="handleCollect"
      />

      <!-- 物种鉴定专属：AI识别结果 + 社群投票 + 回答（组件内部处理） -->
      <div v-if="topic.sourceType === 2 && sourceData" class="identification-extra-wrap">
        <source-identification :data="sourceData" mode="full" @vote-updated="loadSourceDetail"/>
      </div>

      <comment-section
        v-if="topic.sourceType !== 2"
        :comment-list="commentList" :current-user-id="currentUserId"
        @add-comment="handleAddComment" @add-reply="handleAddReply"
        @like-comment="handleLikeComment"
        @delete-comment="handleDeleteComment" @report-comment="openReport"
        @go-profile="goToUserProfile"
      />
    </el-card>

    <report-dialog :visible.sync="reportVisible" :form="reportForm" @submit="submitReport"/>
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
import {listAllCategory} from '@/api/community/category'
import {addReport} from '@/api/community/report'
import {getDicts} from '@/api/system/dict/data'
import SourceObservation from './components/SourceObservation'
import SourceIdentification from './components/SourceIdentification'
import SourceSurvey from './components/SourceSurvey'
import ActionBar from './components/ActionBar'
import CommentSection from './components/CommentSection'
import ReportDialog from './components/ReportDialog'
import DictTag from '@/components/DictTag'

const SYSTEM_CONTENTS = ['来自观察记录的分享', '来自野外调查记录的分享', '来自物种鉴定求助的分享']

export default {
  name: 'CommunityDetail',
  components: {SourceObservation, SourceIdentification, SourceSurvey, ActionBar, CommentSection, ReportDialog, DictTag},
  data() {
    return {
      loading: false,
      topic: {},
      categoryList: [],
      commentList: [],
      currentUserId: this.$store.getters.userId,
      sourceLoading: false,
      sourceData: null,
      activeImageIdx: 0,
      reportVisible: false,
      categoryTypeOptions: [],
      sourceTypeOptions: [],
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
        // 如果是字符串，尝试解析
        if (typeof raw === 'string') {
          // 先尝试作为 JSON 数组解析
          if (raw.startsWith('[')) {
            try {
              arr = JSON.parse(raw)
            } catch (e) {
              // JSON 解析失败，尝试逗号分割
              arr = raw.split(',').map(s => s.trim()).filter(Boolean)
            }
          } else {
            // 不是 JSON，直接用逗号分割
            arr = raw.split(',').map(s => s.trim()).filter(Boolean)
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
    this.getDicts('nh_community_category_type').then(res => {
      this.categoryTypeOptions = res.data || []
    })
    this.getDicts('nh_community_source_type').then(res => {
      this.sourceTypeOptions = res.data || []
    })
    const topicId = this.$route.query.id
    if (topicId) this.loadTopic(topicId)
  },
  methods: {
    getCategoryList() {
      listAllCategory().then(r => {
        this.categoryList = r.data
      })
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
      }).finally(() => {
        this.loading = false
      })
    },
    loadSourceDetail() {
      this.sourceLoading = true
      this.sourceData = null
      this.activeImageIdx = 0
      getSourceDetail(this.topic.sourceType, this.topic.sourceId)
        .then(r => {
          this.sourceData = r.data
        })
        .catch(() => {
          this.sourceData = null
        })
        .finally(() => {
          this.sourceLoading = false
        })
    },
    loadComments(id) {
      listComment({topicId: id, pageNum: 1, pageSize: 100}).then(r => {
        this.commentList = r.rows
      })
    },
    getCategoryName(val) {
      const item = this.communityCategoryOptions.find(d => d.dictValue == val);
      return item ? item.dictLabel : "未分类";
    },
    goBack() {
      this.$router.go(-1)
    },
    goToUserProfile(uid) {
      if (!uid) return
      this.$router.push({path: '/user/myProfile', query: {userId: uid}})
    },
    handleEdit() {
      this.$router.push({path: '/user/community/create', query: {id: this.topic.topicId}})
    },
    handleDelete() {
      this.$modal.confirm('确定删除此话题？')
        .then(() => delTopic(this.topic.topicId))
        .then(() => {
          this.$message.success('删除成功');
          this.$router.push('/user/community/list')
        })
        .catch(() => {
        })
    },
    handleLike() {
      if (this.topic.isLiked) {
        unlikeTopic(this.topic.topicId).then(() => {
          this.topic.isLiked = false;
          this.topic.likeCount--
        })
      } else {
        likeTopic(this.topic.topicId).then(() => {
          this.topic.isLiked = true;
          this.topic.likeCount++
        })
      }
    },
    handleCollect() {
      if (this.topic.isCollected) {
        uncollectTopic(this.topic.topicId).then(() => {
          this.topic.isCollected = false;
          this.topic.collectCount--
        })
      } else {
        collectTopic(this.topic.topicId).then(() => {
          this.topic.isCollected = true;
          this.topic.collectCount++
        })
      }
    },
    handleAddComment(content) {
      addComment({topicId: this.topic.topicId, content, parentId: 0}).then(() => {
        this.$message.success('评论成功')
        this.loadComments(this.topic.topicId)
        this.topic.commentCount = (this.topic.commentCount || 0) + 1
      })
    },
    handleAddReply({content, parentId, replyToUserId}) {
      addComment({topicId: this.topic.topicId, content, parentId, replyToUserId}).then(() => {
        this.$message.success('回复成功')
        this.loadComments(this.topic.topicId)
        this.topic.commentCount = (this.topic.commentCount || 0) + 1
      })
    },
    handleLikeComment(comment) {
      if (comment.isLiked) {
        unlikeComment(comment.commentId).then(() => {
          comment.isLiked = false;
          comment.likeCount--
        })
      } else {
        likeComment(comment.commentId).then(() => {
          comment.isLiked = true;
          comment.likeCount = (comment.likeCount || 0) + 1
        })
      }
    },
    handleDeleteComment(comment) {
      this.$modal.confirm('确定删除此评论？')
        .then(() => delComment(comment.commentId))
        .then(() => {
          this.$message.success('删除成功');
          this.loadComments(this.topic.topicId);
          this.topic.commentCount--
        })
        .catch(() => {
        })
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
.community-detail-container {
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
  flex-wrap: wrap;
  gap: 6px;
  margin-bottom: 12px;
}

.detail-title {
  font-size: 24px;
  font-weight: 700;
  color: #1a1a2e;
  margin: 0 0 12px 0;
  line-height: 1.4;
}

.detail-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 16px;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 13px;
  color: #909399;
}

.source-panel {
  margin-bottom: 24px;
  border: 1px solid #ebeef5;
  border-radius: 8px;
  overflow: hidden;
}

.source-inner {
  display: flex;
  align-items: flex-start;
}

.source-images {
  width: 45%;
  min-width: 260px;
  flex-shrink: 0;
  overflow: hidden;
  display: flex;
  flex-direction: column;
}

.image-viewer {
  height: 100%;
  margin-top: 15px;
}

.main-image {
  position: relative;
  width: 100%;
  height: 100%;
  min-height: 350px;
  overflow: hidden;
  background: #1a1a1a;
}

.image-counter {
  position: absolute;
  top: 10px;
  right: 10px;
  background: rgba(0, 0, 0, 0.50);
  color: #fff;
  font-size: 11px;
  padding: 2px 9px;
  border-radius: 999px;
  z-index: 10;
  pointer-events: none;
  backdrop-filter: blur(4px);
}

.thumb-strip {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  padding: 10px 10px 8px;
  overflow-x: auto;
  background: linear-gradient(to top, rgba(0, 0, 0, 0.60) 0%, transparent 100%);
  z-index: 10;
  scrollbar-width: thin;
  scrollbar-color: rgba(255, 255, 255, 0.4) transparent;

  &::-webkit-scrollbar {
    height: 3px;
  }

  &::-webkit-scrollbar-thumb {
    background: rgba(255, 255, 255, 0.4);
    border-radius: 2px;
  }
}

.thumb-item {
  width: 52px;
  height: 52px;
  flex-shrink: 0;
  border-radius: 5px;
  overflow: hidden;
  cursor: pointer;
  border: 2px solid rgba(255, 255, 255, 0.3);
  transition: border-color 0.2s, transform 0.2s;
  opacity: 0.75;

  &.active {
    border-color: #fff;
    opacity: 1;
    transform: scale(1.08);
  }

  &:hover {
    opacity: 1;
    border-color: rgba(255, 255, 255, 0.8);
  }
}

/* 主图切换淡入淡出动画 */
.main-img-el {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  display: block;
}

/* 主图切换淡入淡出动画 */
.img-fade-enter-active {
  transition: opacity 0.28s ease;
}

.img-fade-leave-active {
  transition: opacity 0.28s ease;
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
}

.img-fade-enter {
  opacity: 0;
}

.img-fade-leave-to {
  opacity: 0;
}

.source-images ::v-deep .el-image, .source-images ::v-deep img {
  width: 100% !important;
  height: 100% !important;
  object-fit: cover !important;
}

.main-image ::v-deep .el-image {
  width: 100% !important;
  height: 100% !important;
}

.no-image {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100%;
  color: #c0c4cc;
}

.source-info {
  flex: 1;
  padding: 20px;
  display: flex;
  flex-direction: column;
  gap: 14px;
  overflow-y: auto;
  max-height: 400px;
}

.source-link-row {
  padding-top: 4px;
}

.original-content {
  margin-bottom: 20px;
  padding: 16px;
  background: #f9fafb;
  border-radius: 8px;
}

.detail-section {
  margin-bottom: 16px;
}

.section-title {
  font-size: 15px;
  font-weight: 600;
  color: #303133;
  margin: 0 0 12px 0;
  padding-bottom: 8px;
  border-bottom: 1px solid #ebeef5;
}

.section-content {
  font-size: 14px;
  color: #4a4a5a;
  line-height: 1.8;
  padding: 12px 16px;
  background: #f9fafb;
  border-radius: 6px;
}

.tag-section {
  margin-bottom: 16px;
}

@media (max-width: 768px) {
  .source-inner {
    flex-direction: column;
  }
  .source-images {
    width: 100%;
  }
  .source-info {
    max-height: none;
  }
}

/* 物种鉴定：AI识别 + 投票区块 */
.identification-extra-section {
  margin-top: 20px;

  .extra-section-title {
    font-size: 16px;
    font-weight: 600;
    color: #303133;
    margin: 0 0 12px 0;
    padding-left: 10px;
    border-left: 4px solid #409EFF;
    display: flex;
    align-items: center;
    gap: 8px;

    i {
      color: #409EFF;
    }
  }

  .ai-result-card {
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);

    ::v-deep .el-card__body {
      padding: 16px;
    }

    .ai-content {
      .ai-main {
        display: flex;
        align-items: center;
        gap: 12px;
        margin-bottom: 10px;

        .ai-species {
          font-size: 20px;
          font-weight: bold;
          margin: 0;
          color: white;
        }
      }

      .ai-desc {
        color: rgba(255, 255, 255, 0.95);
        font-size: 14px;
        line-height: 1.6;
        margin: 10px 0;
      }

      .ai-actions {
        display: flex;
        align-items: center;
        margin-top: 14px;

        .ai-link {
          color: white;
          text-decoration: none;
          display: inline-flex;
          align-items: center;
          gap: 6px;
          padding: 7px 16px;
          background: rgba(255, 255, 255, 0.2);
          border-radius: 20px;
          font-size: 13px;
          transition: background 0.2s;

          &:hover {
            background: rgba(255, 255, 255, 0.32);
          }
        }

        ::v-deep .el-button--text {
          color: white !important;
        }
      }
    }
  }

  .vote-result-card {
    ::v-deep .el-card__body {
      padding: 16px 20px;
    }

    .vote-body {
      display: flex;
      flex-direction: column;
      gap: 14px;
      margin-bottom: 14px;

      .vote-row {
        display: flex;
        align-items: center;
        gap: 10px;

        .vote-label {
          font-size: 13px;
          font-weight: 600;
          width: 42px;
          flex-shrink: 0;

          &.agree-label {
            color: #67c23a;
          }

          &.disagree-label {
            color: #f56c6c;
          }
        }

        .vote-score {
          font-size: 15px;
          font-weight: bold;
          width: 52px;
          text-align: right;
          flex-shrink: 0;

          &.agree-score {
            color: #67c23a;
          }

          &.disagree-score {
            color: #f56c6c;
          }
        }

        .vote-bar-wrap {
          flex: 1;
          height: 10px;
          background: #e4e7ed;
          border-radius: 5px;
          overflow: hidden;

          .vote-bar {
            height: 100%;
            border-radius: 5px;
            transition: width 0.5s ease;

            &.agree-bar {
              background: linear-gradient(90deg, #67c23a, #95d475);
            }

            &.disagree-bar {
              background: linear-gradient(90deg, #f56c6c, #f89898);
            }
          }
        }

        .vote-max {
          font-size: 12px;
          color: #909399;
          flex-shrink: 0;
          width: 28px;
        }
      }
    }

    .vote-footer {
      display: flex;
      align-items: center;
      padding-top: 12px;
      border-top: 1px solid #ebeef5;
      gap: 8px;

      .vote-hint {
        margin-left: auto;
        font-size: 12px;
        color: #909399;
      }
    }
  }
}
</style>
