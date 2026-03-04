<template>
  <div class="user-profile-container">
    <!-- 顶部个人信息卡片 -->
    <el-card class="profile-card" shadow="hover">
      <div class="profile-main">
        <el-avatar :size="72" :src="userInfo.avatar" icon="el-icon-user-solid" />
        <div class="profile-info">
          <div class="profile-name-row">
            <span class="nickname">{{ userInfo.nickName || userInfo.userName || userInfo.name }}</span>
            <el-tag type="success" size="small" class="identity-tag">{{ identityLabel }}</el-tag>
          </div>
          <div class="intro">
            {{ userInfo.intro || '热爱自然，乐于分享观察与发现。' }}
          </div>
          <div class="meta">
            <span>
              <i class="el-icon-time"></i>
              注册时间：
              <span>{{ userInfo.createTime ? parseTime(userInfo.createTime, '{y}-{m}-{d}') : '——' }}</span>
            </span>
          </div>
        </div>
      </div>
    </el-card>

    <!-- 内容区域：导航 + 我的话题 / 日志 / 资料设置 -->
    <el-card class="content-card" shadow="never">
      <el-tabs v-model="mainTab">
        <el-tab-pane label="自然足迹" name="overview">
          <el-tabs v-model="activeTab">
            <!-- 我的话题：只显示审核通过 -->
            <el-tab-pane label="话题" name="topics">
              <div class="topic-list">
                <div
                  v-for="topic in topicList"
                  :key="topic.topicId"
                  class="topic-item"
                  @click="openTopicDetail(topic)"
                >
                  <div class="topic-header">
                    <div class="topic-title-area">
                      <h3 class="topic-title">{{ topic.title }}</h3>
                      <div class="topic-badges">
                        <el-tag
                          v-if="topic.isTop === '1'"
                          type="danger"
                          size="mini"
                        >置顶</el-tag>
                        <el-tag
                          v-if="topic.isEssence === '1'"
                          type="warning"
                          size="mini"
                        >精华</el-tag>
                      </div>
                    </div>
                    <div class="topic-meta">
                      <span><i class="el-icon-time"></i> {{ parseTime(topic.createTime, '{y}-{m}-{d} {h}:{i}') }}</span>
                      <span><i class="el-icon-collection-tag"></i> {{ getCategoryName(topic.category) }}</span>
                    </div>
                  </div>

                  <div class="topic-content">
                    <p>{{ truncate(topic.content, 160) }}</p>
                  </div>

                  <div class="topic-footer">
                    <div class="topic-stats">
                      <span><i class="el-icon-view"></i> {{ topic.viewCount || 0 }}</span>
                      <span><i class="el-icon-chat-dot-round"></i> {{ topic.commentCount || 0 }}</span>
                      <span><i class="el-icon-thumb"></i> {{ topic.likeCount || 0 }}</span>
                    </div>
                  </div>
                </div>

                <el-empty
                  v-if="topicList.length === 0"
                  description="你还没有发布过已审核通过的话题。"
                />
              </div>

              <pagination
                v-show="topicTotal > 0"
                :total="topicTotal"
                :page.sync="topicQuery.pageNum"
                :limit.sync="topicQuery.pageSize"
                @pagination="loadMyTopics"
              />
            </el-tab-pane>

            <!-- 我的公开观察日志 -->
            <el-tab-pane label="日志" name="diaries">
              <div class="diary-list">
                <div
                  v-for="diary in diaryList"
                  :key="diary.diaryId"
                  class="diary-item"
                  @click="openDiaryDetail(diary)"
                >
                  <div class="diary-header">
                    <div class="diary-title-row">
                      <h3 class="diary-title">{{ diary.title }}</h3>
                      <el-tag
                        v-if="diary.isArchived === '1'"
                        type="warning"
                        size="mini"
                      >已归档</el-tag>
                    </div>
                    <div class="diary-meta">
                      <span><i class="el-icon-time"></i> {{ parseTime(diary.observationDate, '{y}-{m}-{d}') }}</span>
                      <span v-if="diary.location"><i class="el-icon-location"></i> {{ diary.location }}</span>
                    </div>
                  </div>

                  <div class="diary-content">
                    <p>{{ truncate(diary.content, 160) }}</p>
                  </div>

                  <div class="diary-tags" v-if="diary.tags">
                    <el-tag
                      v-for="tag in diary.tags.split(',')"
                      :key="tag"
                      size="mini"
                      type="info"
                    >
                      {{ tag }}
                    </el-tag>
                  </div>
                </div>

                <el-empty
                  v-if="diaryList.length === 0"
                  description="暂无公开日志，你可以在日志中将可见性设置为公开。"
                />
              </div>

              <pagination
                v-show="diaryTotal > 0"
                :total="diaryTotal"
                :page.sync="diaryQuery.pageNum"
                :limit.sync="diaryQuery.pageSize"
                @pagination="loadMyPublicDiaries"
              />
            </el-tab-pane>
          </el-tabs>
        </el-tab-pane>

        <!-- 个人资料设置，仅本人可见 -->
        <el-tab-pane v-if="isSelf" label="个人资料" name="profile">
          <el-form
            ref="profileForm"
            :model="profileForm"
            :rules="profileRules"
            label-width="90px"
            class="profile-form"
          >
            <el-form-item label="头像">
              <el-upload
                class="avatar-uploader"
                action=""
                :show-file-list="false"
                :auto-upload="false"
                :on-change="handleAvatarChange"
              >
                <img v-if="userInfo.avatar" :src="userInfo.avatar" class="avatar" />
                <i v-else class="el-icon-plus avatar-uploader-icon"></i>
              </el-upload>
              <div class="avatar-tip">支持 JPG/PNG，大小不超过 2MB</div>
            </el-form-item>
            <el-form-item label="昵称" prop="nickName">
              <el-input v-model="profileForm.nickName" placeholder="请输入昵称" />
            </el-form-item>
            <el-form-item label="个性签名" prop="intro">
              <el-input
                v-model="profileForm.intro"
                type="textarea"
                :rows="3"
                placeholder="简单介绍一下你与自然的故事"
              />
            </el-form-item>
            <el-form-item label="手机号码" prop="phonenumber">
              <el-input v-model="profileForm.phonenumber" placeholder="请输入手机号码" />
            </el-form-item>
            <el-form-item label="邮箱" prop="email">
              <el-input v-model="profileForm.email" placeholder="请输入邮箱" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" :loading="profileSaving" @click="submitProfile">
                保存修改
              </el-button>
              <el-button @click="resetProfile">重置</el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>
      </el-tabs>
    </el-card>

    <!-- 话题详情（含评论） -->
    <el-dialog
      title="话题详情"
      :visible.sync="topicDetailVisible"
      width="900px"
      top="5vh"
    >
      <div v-if="currentTopic" class="topic-detail">
        <div class="detail-header">
          <div class="detail-badges">
            <el-tag
              :type="getAuditStatusTagType(currentTopic.auditStatus)"
              size="small"
            >
              {{ getAuditStatusLabel(currentTopic.auditStatus) }}
            </el-tag>
            <el-tag
              v-if="currentTopic.isTop === '1'"
              type="danger"
              size="small"
            >置顶</el-tag>
            <el-tag
              v-if="currentTopic.isEssence === '1'"
              type="warning"
              size="small"
            >精华</el-tag>
          </div>
          <h2 class="detail-title">{{ currentTopic.title }}</h2>
          <div class="detail-meta">
            <span><i class="el-icon-time"></i> {{ parseTime(currentTopic.createTime, '{y}-{m}-{d} {h}:{i}') }}</span>
            <span><i class="el-icon-collection-tag"></i> {{ getCategoryName(currentTopic.category) }}</span>
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
            <el-button
              type="primary"
              @click="handleAddComment"
              style="margin-top: 10px;"
            >
              发表评论
            </el-button>
          </div>

          <div class="comment-list">
            <div
              v-for="comment in commentList"
              :key="comment.commentId"
              class="comment-item"
            >
              <el-avatar
                :size="36"
                icon="el-icon-user-solid"
                class="comment-avatar"
                @click="goToUserProfile(comment.userId)"
              />
              <div class="comment-content">
                <div class="comment-author">
                  <span
                    class="name"
                    @click="goToUserProfile(comment.userId)"
                  >
                    {{ comment.userName }}
                  </span>
                  <span class="time">
                    {{ parseTime(comment.createTime, '{y}-{m}-{d} {h}:{i}') }}
                  </span>
                </div>
                <div class="comment-text">{{ comment.content }}</div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </el-dialog>

    <!-- 日志详情 -->
    <el-dialog
      title="观察日志详情"
      :visible.sync="diaryDetailVisible"
      width="800px"
    >
      <div v-if="currentDiary" class="diary-detail">
        <div class="detail-header">
          <h2>{{ currentDiary.title }}</h2>
          <div class="detail-badges">
            <el-tag type="success" size="small">公开</el-tag>
            <el-tag
              v-if="currentDiary.isArchived === '1'"
              type="warning"
              size="small"
            >
              已归档
            </el-tag>
          </div>
        </div>

        <div class="detail-meta">
          <div class="meta-item">
            <span class="meta-label">观察日期：</span>
            <span>{{ parseTime(currentDiary.observationDate, '{y}-{m}-{d} {h}:{i}') }}</span>
          </div>
          <div class="meta-item" v-if="currentDiary.location">
            <span class="meta-label">观察地点：</span>
            <span>{{ currentDiary.location }}</span>
          </div>
        </div>

        <div class="detail-section">
          <h4>日志内容</h4>
          <p class="content-text">{{ currentDiary.content }}</p>
        </div>

        <div class="detail-section" v-if="currentDiary.tags">
          <h4>标签</h4>
          <el-tag
            v-for="tag in currentDiary.tags.split(',')"
            :key="tag"
            style="margin-right: 8px;"
          >
            {{ tag }}
          </el-tag>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import {
  myTopicList,
  getTopic,
  listComment,
  addComment
} from '@/api/user/community'
import { listDiary, getDiary } from '@/api/user/diary'
import { listAllCategory } from '@/api/community/category'
import { getUserProfile, updateUserProfile, uploadAvatar } from '@/api/system/user'
import { getPublicProfile } from '@/api/user/profile'

export default {
  name: 'UserProfile',
  data() {
    return {
      userInfo: {
        userId: null,
        userName: '',
        nickName: '',
        name: '',
        avatar: '',
        intro: '',
        createTime: ''
      },
      identityLabel: '探索者',
      mainTab: 'overview',
      activeTab: 'topics',
      isSelf: true,
      categoryList: [],
      // 我的话题
      topicQuery: {
        pageNum: 1,
        pageSize: 10,
        auditStatus: '1'
      },
      topicList: [],
      topicTotal: 0,
      // 我的公开日志
      diaryQuery: {
        pageNum: 1,
        pageSize: 10,
        visibility: '1'
      },
      diaryList: [],
      diaryTotal: 0,
      // 详情 & 评论
      topicDetailVisible: false,
      currentTopic: null,
      commentList: [],
      commentContent: '',
      diaryDetailVisible: false,
      currentDiary: null,
      // 个人资料编辑
      profileForm: {
        nickName: '',
        intro: '',
        phonenumber: '',
        email: ''
      },
      profileRules: {
        nickName: [
          { required: true, message: '昵称不能为空', trigger: 'blur' }
        ],
        email: [
          { type: 'email', message: '邮箱格式不正确', trigger: 'blur' }
        ]
      },
      profileSaving: false
    }
  },
  created() {
    // 获取传递过来的评论者ID
    const userId = this.$route.query.userId;

    this.initUserInfo(userId)
    this.loadCategories()
  },
  methods: {
    initUserInfo(routeUserId) {
      const storeUser = this.$store.getters.user || {}
      const currentUserId = this.$store.getters.userId

      // 判断：是否查看自己
      this.isSelf = !routeUserId || String(routeUserId) === String(currentUserId)

      if (this.isSelf) {
        // 查看自己 → 用当前登录用户ID
        const targetUserId = currentUserId
        this.userInfo = {
          userId: targetUserId,
          userName: storeUser.userName || this.$store.getters.name,
          nickName: storeUser.nickName,
          name: this.$store.getters.name,
          avatar: this.$store.getters.avatar,
          intro: storeUser.remark || storeUser.intro,
          createTime: storeUser.createTime
        }

        const userType = parseInt(storeUser.userType || 1);
        if (userType === 0) this.identityLabel = "管理员";
        else if (userType === 2) this.identityLabel = "鉴定者";
        else this.identityLabel = "探索者";

        this.topicQuery.userId = targetUserId
        this.diaryQuery.userId = targetUserId

        this.loadMyTopics()
        this.loadMyPublicDiaries()
        this.loadProfileDetail()
      } else {
        // 查看别人 → 用路由传过来的 userId
        const targetUserId = routeUserId
        getPublicProfile(targetUserId).then(res => {
          const data = res.data || {}
          this.userInfo = {
            userId: data.userId,
            userName: data.userName,
            nickName: data.nickName,
            name: data.nickName || data.userName,
            avatar: data.avatar,
            intro: data.remark,
            createTime: data.createTime
          }

          const userType = parseInt(data.userType || 1);
          if (userType === 0) this.identityLabel = "管理员";
          else if (userType === 2) this.identityLabel = "鉴定者";
          else this.identityLabel = "探索者";

          this.topicQuery.userId = targetUserId
          this.diaryQuery.userId = targetUserId

          this.loadMyTopics()
          this.loadMyPublicDiaries()
        })
      }
    },
    loadCategories() {
      listAllCategory().then(res => {
        this.categoryList = res.data || []
      })
    },
    loadMyTopics() {
      myTopicList(this.topicQuery).then(res => {
        this.topicList = res.rows || []
        this.topicTotal = res.total || 0
      })
    },
    loadMyPublicDiaries() {
      listDiary(this.diaryQuery).then(res => {
        this.diaryList = res.rows || []
        this.diaryTotal = res.total || 0
      })
    },
    truncate(value, length) {
      if (!value) return ''
      if (value.length <= length) return value
      return value.substring(0, length) + '...'
    },
    getCategoryName(categoryCode) {
      if (!this.categoryList || this.categoryList.length === 0) {
        return '未分类'
      }
      const item = this.categoryList.find(c => c.categoryCode === categoryCode)
      return item ? item.categoryName : '未分类'
    },
    getAuditStatusLabel(status) {
      if (status === '0' || status === 0) return '待审核'
      if (status === '1' || status === 1) return '已通过'
      if (status === '2' || status === 2) return '未通过'
      return '未知状态'
    },
    getAuditStatusTagType(status) {
      if (status === '0' || status === 0) return 'info'
      if (status === '1' || status === 1) return 'success'
      if (status === '2' || status === 2) return 'danger'
      return ''
    },
    openTopicDetail(topic) {
      getTopic(topic.topicId).then(res => {
        this.currentTopic = res.data
        this.topicDetailVisible = true
        this.loadComments(topic.topicId)
      })
    },
    loadComments(topicId) {
      listComment({
        topicId,
        pageNum: 1,
        pageSize: 100
      }).then(res => {
        this.commentList = res.rows || []
      })
    },
    handleAddComment() {
      if (!this.commentContent || !this.commentContent.trim()) {
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
      })
    },
    openDiaryDetail(diary) {
      getDiary(diary.diaryId).then(res => {
        this.currentDiary = res.data
        this.diaryDetailVisible = true
      })
    },
    loadProfileDetail() {
      getUserProfile().then(res => {
        const data = res.data || {}
        this.profileForm = {
          nickName: data.nickName || '',
          intro: data.remark || '',
          phonenumber: data.phonenumber || '',
          email: data.email || ''
        }
        // 顶部展示的用户信息同步为后台返回的真实数据
        this.userInfo.userName = data.userName || this.userInfo.userName
        this.userInfo.nickName = data.nickName || this.userInfo.nickName
        this.userInfo.avatar = data.avatar || this.userInfo.avatar
        this.userInfo.createTime = data.createTime || this.userInfo.createTime
      })
    },
    submitProfile() {
      this.$refs.profileForm.validate(valid => {
        if (!valid) return
        this.profileSaving = true
        const payload = {
          nickName: this.profileForm.nickName,
          remark: this.profileForm.intro,
          phonenumber: this.profileForm.phonenumber,
          email: this.profileForm.email
        }
        updateUserProfile(payload).then(() => {
          this.$message.success('资料已更新')
          this.profileSaving = false
          // 顶部展示信息同步
          this.userInfo.nickName = this.profileForm.nickName
          this.userInfo.intro = this.profileForm.intro
        }).catch(() => {
          this.profileSaving = false
        })
      })
    },
    resetProfile() {
      this.loadProfileDetail()
    },
    handleAvatarChange(file) {
      const raw = file.raw || file
      if (!raw) return
      const isImage = raw.type === 'image/jpeg' || raw.type === 'image/png'
      if (!isImage) {
        this.$message.error('头像只能是 JPG/PNG 格式')
        return
      }
      const isLt2M = raw.size / 1024 / 1024 < 2
      if (!isLt2M) {
        this.$message.error('头像大小不能超过 2MB')
        return
      }
      const formData = new FormData()
      formData.append('avatarfile', raw)
      uploadAvatar(formData).then(res => {
        const url = res.imgUrl || res.data || res.url
        if (url) {
          this.userInfo.avatar = url
          // 如果项目里有更新头像的 action，可以在这里同步到全局 store
          if (this.$store.dispatch) {
            this.$store.dispatch('user/setAvatar', url).catch(() => {})
          }
        }
        this.$message.success('头像已更新')
      })
    },
    goToUserProfile(userId) {
      if (!userId) return
      this.$router.push({
        path: '/user/myProfile',
        query: { userId }
      })
    }
  }
}
</script>

<style lang="scss" scoped>
.user-profile-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 24px 20px 40px;
}

.profile-card {
  margin-bottom: 20px;
}

.profile-main {
  display: flex;
  align-items: center;
}

.profile-info {
  margin-left: 20px;
  flex: 1;
}

.profile-name-row {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 6px;
}

.nickname {
  font-size: 22px;
  font-weight: 600;
  color: #2c3e50;
}

.identity-tag {
  border-radius: 999px;
}

.username {
  font-size: 14px;
  color: #909399;
  margin-bottom: 8px;
}

.intro {
  font-size: 14px;
  color: #606266;
  margin-bottom: 10px;
}

.meta {
  font-size: 13px;
  color: #909399;

  span {
    display: inline-flex;
    align-items: center;
    gap: 4px;
  }
}

.content-card {
  margin-top: 10px;
}

.profile-form {
  max-width: 520px;
}

.avatar-uploader {
  display: inline-block;
}

.avatar-uploader-icon,
.avatar {
  width: 80px;
  height: 80px;
  border-radius: 50%;
  display: inline-block;
}

.avatar-uploader-icon {
  font-size: 28px;
  color: #8c939d;
  border: 1px dashed #d9d9d9;
  line-height: 80px;
  text-align: center;
}

.avatar-tip {
  font-size: 12px;
  color: #909399;
  margin-top: 6px;
}

.list-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.list-title {
  font-size: 16px;
  font-weight: 500;
  color: #2c3e50;
}

.topic-list,
.diary-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
  margin-bottom: 16px;
}

.topic-item,
.diary-item {
  cursor: pointer;
  padding: 16px 18px;
  border-radius: 8px;
  background: #ffffff;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.04);
  border: 1px solid #f0f0f0;
  transition: all 0.2s ease;

  &:hover {
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
    transform: translateY(-2px);
  }
}

.topic-header {
  margin-bottom: 10px;
}

.topic-title-area {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 10px;
  margin-bottom: 6px;
}

.topic-title {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
  color: #2c3e50;
}

.topic-badges {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}

.topic-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  font-size: 13px;
  color: #909399;

  span {
    display: inline-flex;
    align-items: center;
    gap: 4px;
  }
}

.topic-content {
  margin-top: 8px;
  margin-bottom: 8px;

  p {
    margin: 0;
    color: #606266;
    line-height: 1.6;
  }
}

.topic-footer {
  margin-top: 8px;
  padding-top: 8px;
  border-top: 1px solid #f0f0f0;
}

.topic-stats {
  display: flex;
  gap: 16px;
  font-size: 13px;
  color: #909399;

  span {
    display: inline-flex;
    align-items: center;
    gap: 4px;
  }
}

.diary-header {
  margin-bottom: 8px;
}

.diary-title-row {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 6px;
}

.diary-title {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
  color: #2c3e50;
}

.diary-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  font-size: 13px;
  color: #909399;

  span {
    display: inline-flex;
    align-items: center;
    gap: 4px;
  }
}

.diary-content {
  margin-top: 8px;
  margin-bottom: 8px;

  p {
    margin: 0;
    color: #606266;
    line-height: 1.6;
  }
}

.diary-tags {
  margin-top: 6px;

  .el-tag {
    margin-right: 6px;
  }
}

.topic-detail .detail-header,
.diary-detail .detail-header {
  margin-bottom: 16px;
  padding-bottom: 12px;
  border-bottom: 1px solid #f0f0f0;
}

.detail-badges {
  display: flex;
  gap: 8px;
  margin-bottom: 8px;
}

.detail-title {
  margin: 0 0 8px 0;
  font-size: 22px;
  color: #2c3e50;
}

.detail-meta {
  font-size: 13px;
  color: #909399;

  span {
    margin-right: 12px;
  }
}

.detail-content {
  margin-bottom: 16px;

  .content-text {
    color: #606266;
    line-height: 1.8;
    white-space: pre-wrap;
  }
}

.comment-section {
  margin-top: 24px;
}

.comment-header {
  margin-bottom: 12px;

  h3 {
    margin: 0;
    font-size: 16px;
    font-weight: 600;
    color: #2c3e50;
  }
}

.comment-input {
  margin-bottom: 16px;
}

.comment-list {
  .comment-item {
    display: flex;
    gap: 12px;
    padding: 10px 0;
    border-bottom: 1px solid #f5f5f5;
  }
}

.comment-avatar {
  cursor: pointer;
}

.comment-content {
  flex: 1;
}

.comment-author {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 4px;
}

.comment-author .name {
  font-weight: 500;
  color: #2c3e50;
  cursor: pointer;
}

.comment-author .time {
  font-size: 12px;
  color: #909399;
}

.comment-text {
  font-size: 14px;
  color: #606266;
  line-height: 1.6;
}

.detail-section {
  margin-top: 16px;

  h4 {
    margin: 0 0 8px 0;
    font-size: 15px;
    color: #2c3e50;
  }

  .content-text {
    color: #606266;
    line-height: 1.8;
    white-space: pre-wrap;
  }
}

@media (max-width: 768px) {
  .profile-main {
    flex-direction: column;
    align-items: flex-start;
  }

  .profile-info {
    margin-left: 0;
    margin-top: 12px;
  }
}
</style>

