<template>
  <div class="community-container">
    <!-- 顶部分类导航 -->
    <div class="category-tabs">
    <el-radio-group v-model="queryParams.category" @change="handleQuery" size="medium">
      <el-radio-button label="">全部话题</el-radio-button>
      <el-radio-button
        v-for="item in categoryList"
        :key="item.categoryCode"
        :label="item.categoryCode"
      >
        {{ item.categoryIcon }} {{ item.categoryName }}
      </el-radio-button>
    </el-radio-group>

    <el-button type="primary" icon="el-icon-edit" @click="handleCreate">发布话题</el-button>
  </div>

    <!-- 精华/置顶筛选 -->
    <div class="filter-bar">
      <el-checkbox-group v-model="filterOptions" @change="handleQuery">
        <el-checkbox label="essence">只看精华</el-checkbox>
        <el-checkbox label="top">只看置顶</el-checkbox>
      </el-checkbox-group>

      <el-input
        v-model="queryParams.title"
        placeholder="搜索话题标题"
        clearable
        style="width: 300px;"
        @keyup.enter.native="handleQuery"
      >
        <el-button slot="append" icon="el-icon-search" @click="handleQuery"></el-button>
      </el-input>
    </div>

    <!-- 话题列表 -->
    <div class="topic-list">
      <el-card
        v-for="topic in topicList"
        :key="topic.topicId"
        class="topic-card"
        shadow="hover"
        @click.native="handleView(topic)"
      >
        <div class="topic-header">
          <div class="topic-badges">
            <el-tag v-if="topic.isTop === '1'" type="danger" size="mini" effect="dark">置顶</el-tag>
            <el-tag v-if="topic.isEssence === '1'" type="warning" size="mini" effect="dark">精华</el-tag>
            <el-tag :type="getCategoryType(topic.category)" size="mini">{{ getCategoryName(topic.category) }}</el-tag>
          </div>
          <div class="topic-author">
            <el-avatar
              :size="32"
              icon="el-icon-user-solid"
              class="topic-author-avatar"
              @click.stop="goToUserProfile(topic.userId)"
            ></el-avatar>
            <span class="author-name" @click.stop="goToUserProfile(topic.userId)">{{ topic.userName }}</span>
            <span class="topic-time">{{ parseTime(topic.createTime, '{y}-{m}-{d} {h}:{i}') }}</span>
          </div>
        </div>

        <h3 class="topic-title">{{ topic.title }}</h3>

        <div class="topic-content">
          <p>{{ truncate(topic.content, 200) }}</p>
        </div>

        <div class="topic-tags" v-if="topic.tags">
          <el-tag
            v-for="tag in topic.tags.split(',')"
            :key="tag"
            size="mini"
            type="info"
            effect="plain"
          >
            # {{ tag }}
          </el-tag>
        </div>

        <div class="topic-stats">
          <span><i class="el-icon-view"></i> {{ topic.viewCount }}</span>
          <span
            :class="{ active: topic.isLiked }"
            @click.stop="handleLike(topic)"
          >
            <i class="el-icon-thumb"></i> {{ topic.likeCount }}
          </span>
          <span><i class="el-icon-chat-dot-round"></i> {{ topic.commentCount }}</span>
          <span
            :class="{ active: topic.isCollected }"
            @click.stop="handleCollect(topic)"
          >
            <i class="el-icon-star-off"></i> {{ topic.collectCount }}
          </span>
        </div>
      </el-card>

      <el-empty v-if="topicList.length === 0" description="暂无话题，快来发布第一个吧！"></el-empty>
    </div>

    <!-- 分页 -->
    <pagination
      v-show="total > 0"
      :total="total"
      :page.sync="queryParams.pageNum"
      :limit.sync="queryParams.pageSize"
      @pagination="getList"
    />

    <!-- 发布/编辑话题对话框 -->
    <el-dialog
      :title="dialogTitle"
      :visible.sync="dialogVisible"
      width="800px"
      :close-on-click-modal="false"
    >
      <el-form ref="form" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="话题分类" prop="category">
          <el-select v-model="form.category" placeholder="请选择话题分类">
            <el-option
              v-for="item in categoryList"
              :key="item.categoryCode"
              :label="item.categoryIcon + ' ' + item.categoryName"
              :value="item.categoryCode"
            >
              <span style="font-size: 18px;">{{ item.categoryIcon }}</span>
              <span style="margin-left: 10px;">{{ item.categoryName }}</span>
            </el-option>
          </el-select>
        </el-form-item>

        <el-form-item label="话题标题" prop="title">
          <el-input v-model="form.title" placeholder="请输入话题标题" maxlength="200" show-word-limit />
        </el-form-item>

        <el-form-item label="话题内容" prop="content">
          <el-input
            v-model="form.content"
            type="textarea"
            :rows="10"
            placeholder="分享你的观察、经验或问题..."
            maxlength="5000"
            show-word-limit
          />
        </el-form-item>

        <el-form-item label="标签">
          <el-input
            v-model="form.tags"
            placeholder="多个标签用逗号分隔，如：鸟类,观察,北京"
          />
        </el-form-item>
      </el-form>

      <div slot="footer" class="dialog-footer">
        <el-button @click="dialogVisible = false">取 消</el-button>
        <el-button type="primary" @click="submitForm">发 布</el-button>
      </div>
    </el-dialog>

    <!-- 话题详情对话框 -->
    <el-dialog
      title="话题详情"
      :visible.sync="detailVisible"
      width="900px"
      top="5vh"
    >
      <div class="topic-detail" v-if="currentTopic">
        <div class="detail-header">
          <div class="detail-badges">
            <el-tag v-if="currentTopic.isTop === '1'" type="danger" size="small">置顶</el-tag>
            <el-tag v-if="currentTopic.isEssence === '1'" type="warning" size="small">精华</el-tag>
            <el-tag :type="getCategoryType(currentTopic.category)" size="small">
              {{ getCategoryName(currentTopic.category) }}
            </el-tag>
          </div>
          <h2>{{ currentTopic.title }}</h2>
          <div class="detail-author">
            <el-avatar
              :size="40"
              icon="el-icon-user-solid"
              class="topic-author-avatar"
              @click="goToUserProfile(currentTopic.userId)"
            ></el-avatar>
            <div class="author-info" @click="goToUserProfile(currentTopic.userId)">
              <div class="author-name">{{ currentTopic.userName }}</div>
              <div class="author-time">{{ parseTime(currentTopic.createTime) }}</div>
            </div>
          </div>
        </div>

        <div class="detail-content">
          <p class="content-text">{{ currentTopic.content }}</p>
        </div>

        <div class="detail-tags" v-if="currentTopic.tags">
          <el-tag
            v-for="tag in currentTopic.tags.split(',')"
            :key="tag"
            style="margin-right: 8px;"
          >
            # {{ tag }}
          </el-tag>
        </div>

        <div class="detail-actions">
          <el-button
            :type="currentTopic.isLiked ? 'primary' : ''"
            :icon="currentTopic.isLiked ? 'el-icon-thumb' : 'el-icon-thumb'"
            @click="handleLike(currentTopic)"
          >
            {{ currentTopic.isLiked ? '已点赞' : '点赞' }} ({{ currentTopic.likeCount }})
          </el-button>
          <el-button
            :type="currentTopic.isCollected ? 'warning' : ''"
            :icon="currentTopic.isCollected ? 'el-icon-star-on' : 'el-icon-star-off'"
            @click="handleCollect(currentTopic)"
          >
            {{ currentTopic.isCollected ? '已收藏' : '收藏' }} ({{ currentTopic.collectCount }})
          </el-button>
          <el-button icon="el-icon-view">浏览 {{ currentTopic.viewCount }}</el-button>
          <el-button icon="el-icon-warning" @click="handleReportTopic(currentTopic)">举报</el-button>
        </div>

        <!-- 评论区 -->
        <div class="comment-section">
          <div class="comment-header">
            <h3>评论 ({{ commentList.length }})</h3>
          </div>

          <div class="comment-input">
            <el-input
              v-model="commentContent"
              type="textarea"
              :rows="3"
              placeholder="发表你的看法..."
              maxlength="500"
              show-word-limit
            />
            <el-button type="primary" @click="handleAddComment" style="margin-top: 10px;">发表评论</el-button>
          </div>

          <div class="comment-list">
            <div v-for="comment in commentList" :key="comment.commentId" class="comment-item">
              <el-avatar
                :size="36"
                icon="el-icon-user-solid"
                class="comment-avatar"
                @click="goToUserProfile(comment.userId)"
              ></el-avatar>
              <div class="comment-content">
                <div class="comment-author">
                  <span class="name" @click="goToUserProfile(comment.userId)">{{ comment.userName }}</span>
                  <span class="time">{{ parseTime(comment.createTime, '{y}-{m}-{d} {h}:{i}') }}</span>
                </div>
                <div class="comment-text">{{ comment.content }}</div>
                <div class="comment-actions">
                  <span
                    :class="{ active: comment.isLiked }"
                    @click="handleLikeComment(comment)"
                  >
                    <i class="el-icon-thumb"></i> {{ comment.likeCount || 0 }}
                  </span>
                  <span @click="handleReply(comment)">
                    <i class="el-icon-chat-dot-round"></i> 回复
                  </span>
                  <span @click="handleReportComment(comment)">
                    <i class="el-icon-warning"></i> 举报
                  </span>
                  <span
                    v-if="comment.userId === currentUserId"
                    @click="handleDeleteComment(comment)"
                    style="color: #f56c6c;"
                  >
                    <i class="el-icon-delete"></i> 删除
                  </span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </el-dialog>

    <!-- 举报对话框 -->
    <el-dialog title="举报" :visible.sync="reportVisible" width="500px" append-to-body>
      <el-form ref="reportForm" :model="reportForm" :rules="reportRules" label-width="80px">
        <el-form-item label="举报类型">
          <el-tag>{{ reportForm.reportType === '0' ? '话题' : '评论' }}</el-tag>
        </el-form-item>
        <el-form-item label="举报原因" prop="reason">
          <el-select v-model="reportForm.reason" placeholder="请选择举报原因">
            <el-option label="垃圾广告" value="0" />
            <el-option label="辱骂攻击" value="1" />
            <el-option label="违法违规" value="2" />
            <el-option label="虚假信息" value="3" />
            <el-option label="其他" value="4" />
          </el-select>
        </el-form-item>
        <el-form-item label="详细描述" prop="description">
          <el-input
            v-model="reportForm.description"
            type="textarea"
            :rows="4"
            placeholder="请详细描述举报原因"
          />
        </el-form-item>
      </el-form>
      <div slot="footer">
        <el-button @click="reportVisible = false">取 消</el-button>
        <el-button type="primary" @click="submitReport">提 交</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import {
  listTopic, getTopic, addTopic, updateTopic, delTopic,
  likeTopic, unlikeTopic, collectTopic, uncollectTopic,
  listComment, addComment, delComment, likeComment, unlikeComment
} from '@/api/user/community'
import { listAllCategory } from '@/api/community/category'
import { addReport } from '@/api/community/report'

export default {
  name: 'UserCommunity',
  data() {
    return {
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        category: '',
        title: null,
        auditStatus: '1' // 只显示已审核通过的话题
      },
      filterOptions: [],
      topicList: [],
      categoryList: [], // 板块列表
      total: 0,
      dialogVisible: false,
      dialogTitle: '',
      detailVisible: false,
      form: {},
      currentTopic: null,
      commentList: [],
      commentContent: '',
      currentUserId: this.$store.getters.userId,
      reportVisible: false,
      reportForm: {
        reportType: '0',
        targetId: null,
        targetTitle: null,
        targetUserId: null,
        targetUserName: null,
        reporterId: null,
        reporterName: null,
        reason: null,
        description: null
      },
      rules: {
        category: [
          { required: true, message: '请选择话题分类', trigger: 'change' }
        ],
        title: [
          { required: true, message: '话题标题不能为空', trigger: 'blur' },
          { min: 5, max: 200, message: '标题长度在 5 到 200 个字符', trigger: 'blur' }
        ],
        content: [
          { required: true, message: '话题内容不能为空', trigger: 'blur' },
          { min: 10, message: '内容至少10个字符', trigger: 'blur' }
        ]
      },
      reportRules: {
        reason: [
          { required: true, message: '请选择举报原因', trigger: 'change' }
        ],
        description: [
          { required: true, message: '请填写详细描述', trigger: 'blur' },
          { min: 10, message: '描述至少10个字符', trigger: 'blur' }
        ]
      }
    }
  },
  created() {
    this.getList()
    this.getCategoryList()
  },
  methods: {
    getCategoryList() {
      listAllCategory().then(response => {
        this.categoryList = response.data
      })
    },
    goToUserProfile(userId) {
      if (!userId) {
        return
      }
      this.$router.push({
        path: '/user/myProfile',
        query: { userId }
      })
    },
    truncate(value, length) {
      if (!value) return ''
      if (value.length <= length) return value
      return value.substring(0, length) + '...'
    },
    getCategoryName(categoryCode) {
      if (!this.categoryList || this.categoryList.length === 0) {
        return '加载中...';
      }
      const item = this.categoryList.find(c => c.categoryCode === categoryCode);
      return item ? item.categoryName : '未分类';
    },
    getCategoryType(category) {
      const map = {
        'species_science': 'success',
        'field_explore': 'primary',
        'identify_help': 'warning'
      }
      return map[category] || ''
    },
    getList() {
      const params = { ...this.queryParams }
      if (this.filterOptions.includes('essence')) {
        params.isEssence = '1'
      }
      if (this.filterOptions.includes('top')) {
        params.isTop = '1'
      }
      listTopic(params).then(response => {
        this.topicList = response.rows
        this.total = response.total
      })
    },
    handleQuery() {
      this.queryParams.pageNum = 1
      this.getList()
    },
    handleCreate() {
      this.reset()
      this.dialogVisible = true
      this.dialogTitle = '发布话题'
    },
    handleView(topic) {
      getTopic(topic.topicId).then(response => {
        this.currentTopic = response.data
        this.detailVisible = true
        this.loadComments(topic.topicId)
      })
    },
    loadComments(topicId) {
      listComment({ topicId: topicId, pageNum: 1, pageSize: 100 }).then(response => {
        this.commentList = response.rows
      })
    },
    submitForm() {
      this.$refs['form'].validate(valid => {
        if (valid) {
          if (this.form.topicId) {
            updateTopic(this.form).then(response => {
              this.$modal.msgSuccess('修改成功')
              this.dialogVisible = false
              this.getList()
            })
          } else {
            addTopic(this.form).then(response => {
              this.$modal.msgSuccess('话题已提交，等待管理员审核后显示')
              this.dialogVisible = false
              // 不刷新列表，因为待审核话题不显示
            })
          }
        }
      })
    },
    handleLike(topic) {
      if (topic.isLiked) {
        unlikeTopic(topic.topicId).then(() => {
          topic.isLiked = false
          topic.likeCount--
          this.$message.success('已取消点赞')
        })
      } else {
        likeTopic(topic.topicId).then(() => {
          topic.isLiked = true
          topic.likeCount++
          this.$message.success('点赞成功')
        })
      }
    },
    handleCollect(topic) {
      if (topic.isCollected) {
        uncollectTopic(topic.topicId).then(() => {
          topic.isCollected = false
          topic.collectCount--
          this.$message.success('已取消收藏')
        })
      } else {
        collectTopic(topic.topicId).then(() => {
          topic.isCollected = true
          topic.collectCount++
          this.$message.success('收藏成功')
        })
      }
    },
    handleAddComment() {
      if (!this.commentContent.trim()) {
        this.$message.warning('请输入评论内容')
        return
      }
      const data = {
        topicId: this.currentTopic.topicId,
        content: this.commentContent,
        parentId: 0
      }
      addComment(data).then(() => {
        this.$message.success('评论成功')
        this.commentContent = ''
        this.loadComments(this.currentTopic.topicId)
        this.currentTopic.commentCount++
      })
    },
    handleLikeComment(comment) {
      if (comment.isLiked) {
        unlikeComment(comment.commentId).then(() => {
          comment.isLiked = false
          comment.likeCount--
        })
      } else {
        likeComment(comment.commentId).then(() => {
          comment.isLiked = true
          comment.likeCount = (comment.likeCount || 0) + 1
        })
      }
    },
    handleDeleteComment(comment) {
      this.$modal.confirm('确定要删除这条评论吗？').then(() => {
        return delComment(comment.commentId)
      }).then(() => {
        this.$message.success('删除成功')
        this.loadComments(this.currentTopic.topicId)
        this.currentTopic.commentCount--
      }).catch(() => {})
    },
    handleReply(comment) {
      this.commentContent = '@' + comment.userName + ' '
    },
    handleReportTopic(topic) {
      this.reportForm = {
        reportType: '0',
        targetId: topic.topicId,
        targetTitle: topic.title,
        targetUserId: topic.userId,
        targetUserName: topic.userName,
        reporterId: this.$store.getters.userId,
        reporterName: this.$store.getters.name,
        reason: null,
        description: null
      }
      this.reportVisible = true
    },
    handleReportComment(comment) {
      this.reportForm = {
        reportType: '1',
        targetId: comment.commentId,
        targetTitle: comment.content.substring(0, 50),
        targetUserId: comment.userId,
        targetUserName: comment.userName,
        reporterId: this.$store.getters.userId,
        reporterName: this.$store.getters.name,
        reason: null,
        description: null
      }
      this.reportVisible = true
    },
    submitReport() {
      this.$refs['reportForm'].validate(valid => {
        if (valid) {
          addReport(this.reportForm).then(() => {
            this.$message.success('举报成功，我们会尽快处理')
            this.reportVisible = false
          })
        }
      })
    },
    reset() {
      this.form = {
        topicId: null,
        category: 'species_science',
        title: null,
        content: null,
        tags: null
      }
      this.resetForm('form')
    }
  }
}
</script>

<style lang="scss" scoped>
.community-container {
  padding: 20px;
  max-width: 1200px;
  margin: 0 auto;
}

.category-tabs {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding: 20px;
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.06);
}

.filter-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding: 15px 20px;
  background: white;
  border-radius: 8px;
}

.topic-list {
  display: grid;
  gap: 16px;
}

.topic-card {
  cursor: pointer;
  transition: all 0.3s ease;

  &:hover {
    transform: translateY(-4px);
    box-shadow: 0 8px 24px rgba(0,0,0,0.12);
  }

  .topic-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 12px;

    .topic-badges {
      display: flex;
      gap: 8px;
    }

    .topic-author {
      display: flex;
      align-items: center;
      gap: 10px;

      .topic-author-avatar {
        cursor: pointer;
      }

      .author-name {
        font-weight: 500;
        color: #606266;
        cursor: pointer;
      }

      .topic-time {
        font-size: 13px;
        color: #909399;
      }
    }
  }

  .topic-title {
    font-size: 20px;
    font-weight: 600;
    color: #2c3e50;
    margin: 0 0 12px 0;
    line-height: 1.4;
  }

  .topic-content {
    margin-bottom: 12px;

    p {
      color: #606266;
      line-height: 1.6;
      margin: 0;
    }
  }

  .topic-tags {
    margin-bottom: 12px;

    .el-tag {
      margin-right: 8px;
    }
  }

  .topic-stats {
    display: flex;
    gap: 24px;
    padding-top: 12px;
    border-top: 1px solid #f0f0f0;

    span {
      display: flex;
      align-items: center;
      gap: 4px;
      color: #909399;
      font-size: 14px;
      cursor: pointer;
      transition: color 0.3s;

      &:hover {
        color: #409EFF;
      }

      &.active {
        color: #409EFF;
      }

      i {
        font-size: 16px;
      }
    }
  }
}

.topic-detail {
  .detail-header {
    margin-bottom: 24px;
    padding-bottom: 20px;
    border-bottom: 2px solid #f0f0f0;

    .detail-badges {
      display: flex;
      gap: 8px;
      margin-bottom: 12px;
    }

    h2 {
      font-size: 24px;
      font-weight: 600;
      color: #2c3e50;
      margin: 0 0 16px 0;
      line-height: 1.4;
    }

    .detail-author {
      display: flex;
      align-items: center;
      gap: 12px;

      .topic-author-avatar {
        cursor: pointer;
      }

      .author-info {
        cursor: pointer;

        .author-name {
          font-weight: 500;
          color: #2c3e50;
          margin-bottom: 4px;
        }

        .author-time {
          font-size: 13px;
          color: #909399;
        }
      }
    }
  }

  .detail-content {
    margin-bottom: 24px;

    .content-text {
      color: #606266;
      line-height: 1.8;
      white-space: pre-wrap;
      font-size: 15px;
    }
  }

  .detail-tags {
    margin-bottom: 24px;
  }

  .detail-actions {
    display: flex;
    gap: 12px;
    padding: 20px 0;
    border-top: 1px solid #f0f0f0;
    border-bottom: 1px solid #f0f0f0;
  }
}

.comment-section {
  margin-top: 32px;

  .comment-header {
    margin-bottom: 20px;

    h3 {
      font-size: 18px;
      font-weight: 600;
      color: #2c3e50;
      margin: 0;
    }
  }

  .comment-input {
    margin-bottom: 24px;
  }

  .comment-list {
    .comment-item {
      display: flex;
      gap: 12px;
      padding: 16px 0;
      border-bottom: 1px solid #f0f0f0;

      &:last-child {
        border-bottom: none;
      }

      .comment-content {
        flex: 1;

        .comment-author {
          display: flex;
          align-items: center;
          gap: 12px;
          margin-bottom: 8px;

          .name {
            font-weight: 500;
            color: #2c3e50;
          }

          .time {
            font-size: 13px;
            color: #909399;
          }
        }

        .comment-text {
          color: #606266;
          line-height: 1.6;
          margin-bottom: 8px;
        }

        .comment-actions {
          display: flex;
          gap: 16px;

          span {
            display: flex;
            align-items: center;
            gap: 4px;
            color: #909399;
            font-size: 13px;
            cursor: pointer;
            transition: color 0.3s;

            &:hover {
              color: #409EFF;
            }

            &.active {
              color: #409EFF;
            }
          }
        }
      }
    }
  }
}

@media (max-width: 768px) {
  .category-tabs {
    flex-direction: column;
    gap: 12px;

    .el-radio-group {
      width: 100%;
    }
  }

  .filter-bar {
    flex-direction: column;
    gap: 12px;

    .el-input {
      width: 100% !important;
    }
  }
}
</style>
