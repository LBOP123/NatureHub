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

    <!-- 内容区域 -->
    <el-card class="content-card" shadow="never">
      <el-tabs v-model="mainTab">
        <el-tab-pane label="自然足迹" name="overview">
          <el-tabs v-model="activeTab">
            <!-- 分享的观察记录 -->
            <el-tab-pane label="分享的观察记录" name="records">
              <div class="list-container">
                <content-card
                  v-for="record in recordList"
                  :key="record.recordId"
                  :item="record"
                  content-type="record"
                  @click="goToRecordDetail(record)"
                />
                <el-empty v-if="recordList.length === 0" description="暂无已分享的观察记录" />
              </div>
              <pagination
                v-show="recordTotal > 0"
                :total="recordTotal"
                :page.sync="recordQuery.pageNum"
                :limit.sync="recordQuery.pageSize"
                @pagination="loadMyRecords"
              />
            </el-tab-pane>

            <!-- 分享的野外调查 -->
            <el-tab-pane label="分享的野外调查" name="surveys">
              <div class="list-container">
                <content-card
                  v-for="survey in surveyList"
                  :key="survey.surveyId"
                  :item="survey"
                  content-type="survey"
                  @click="goToSurveyDetail(survey)"
                />
                <el-empty v-if="surveyList.length === 0" description="暂无已分享的调查记录" />
              </div>
              <pagination
                v-show="surveyTotal > 0"
                :total="surveyTotal"
                :page.sync="surveyQuery.pageNum"
                :limit.sync="surveyQuery.pageSize"
                @pagination="loadMySurveys"
              />
            </el-tab-pane>

            <!-- 分享的物种鉴定 -->
            <el-tab-pane label="分享的物种鉴定" name="identifications">
              <div class="list-container">
                <content-card
                  v-for="item in identificationList"
                  :key="item.identificationId"
                  :item="item"
                  content-type="identification"
                  @click="goToIdentificationDetail(item)"
                />
                <el-empty v-if="identificationList.length === 0" description="暂无已分享的鉴定记录" />
              </div>
              <pagination
                v-show="identificationTotal > 0"
                :total="identificationTotal"
                :page.sync="identificationQuery.pageNum"
                :limit.sync="identificationQuery.pageSize"
                @pagination="loadMyIdentifications"
              />
            </el-tab-pane>

            <!-- 观察日志 -->
            <el-tab-pane label="观察日志" name="diaries">
              <div class="list-container">
                <content-card
                  v-for="diary in diaryList"
                  :key="diary.diaryId"
                  :item="diary"
                  content-type="record"
                  @click="goToDiaryDetail(diary)"
                />
                <el-empty v-if="diaryList.length === 0" description="暂无公开日志" />
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

        <!-- 个人资料设置 -->
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
  </div>
</template>

<script>
import { listDiary } from '@/api/user/diary'
import { listRecord } from '@/api/user/record'
import { listSurvey } from '@/api/user/survey'
import { listIdentification } from '@/api/user/identification'
import { getUserProfile, updateUserProfile, uploadAvatar } from '@/api/system/user'
import { getPublicProfile } from '@/api/user/profile'
import ContentCard from './ContentCard'

export default {
  name: 'UserProfile',
  components: { ContentCard },
  data() {
    return {
      userInfo: { userId: null, userName: '', nickName: '', name: '', avatar: '', intro: '', createTime: '' },
      identityLabel: '探索者',
      mainTab: 'overview',
      activeTab: 'records',
      isSelf: true,
      recordQuery: { pageNum: 1, pageSize: 10 },
      recordList: [],
      recordTotal: 0,
      surveyQuery: { pageNum: 1, pageSize: 10 },
      surveyList: [],
      surveyTotal: 0,
      identificationQuery: { pageNum: 1, pageSize: 10 },
      identificationList: [],
      identificationTotal: 0,
      diaryQuery: { pageNum: 1, pageSize: 10, visibility: '1' },
      diaryList: [],
      diaryTotal: 0,
      profileForm: { nickName: '', intro: '', phonenumber: '', email: '' },
      profileRules: {
        nickName: [{ required: true, message: '昵称不能为空', trigger: 'blur' }],
        email: [{ type: 'email', message: '邮箱格式不正确', trigger: 'blur' }]
      },
      profileSaving: false
    }
  },
  created() {
    const userId = this.$route.query.userId
    this.initUserInfo(userId)
  },
  methods: {
    initUserInfo(routeUserId) {
      const storeUser = this.$store.getters.user || {}
      const currentUserId = this.$store.getters.userId
      this.isSelf = !routeUserId || String(routeUserId) === String(currentUserId)

      if (this.isSelf) {
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
        const userType = parseInt(storeUser.userType || 1)
        if (userType === 0) this.identityLabel = '管理员'
        else if (userType === 2) this.identityLabel = '鉴定者'
        else this.identityLabel = '探索者'

        this.recordQuery.userId = targetUserId
        this.surveyQuery.userId = targetUserId
        this.identificationQuery.userId = targetUserId
        this.diaryQuery.userId = targetUserId

        this.loadMyRecords()
        this.loadMySurveys()
        this.loadMyIdentifications()
        this.loadMyPublicDiaries()
        this.loadProfileDetail()
      } else {
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
          const userType = parseInt(data.userType || 1)
          if (userType === 0) this.identityLabel = '管理员'
          else if (userType === 2) this.identityLabel = '鉴定者'
          else this.identityLabel = '探索者'

          this.recordQuery.userId = targetUserId
          this.surveyQuery.userId = targetUserId
          this.identificationQuery.userId = targetUserId
          this.diaryQuery.userId = targetUserId

          this.loadMyRecords()
          this.loadMySurveys()
          this.loadMyIdentifications()
          this.loadMyPublicDiaries()
        })
      }
    },
    loadMyRecords() {
      const params = { ...this.recordQuery, isShared: 1 }
      listRecord(params).then(res => {
        this.recordList = res.rows || []
        this.recordTotal = res.total || 0
      })
    },
    loadMySurveys() {
      const params = { ...this.surveyQuery, isShared: 1 }
      listSurvey(params).then(res => {
        this.surveyList = res.rows || []
        this.surveyTotal = res.total || 0
      })
    },
    loadMyIdentifications() {
      const params = { ...this.identificationQuery, isShared: 1 }
      listIdentification(params).then(res => {
        this.identificationList = res.rows || []
        this.identificationTotal = res.total || 0
      })
    },
    loadMyPublicDiaries() {
      listDiary(this.diaryQuery).then(res => {
        this.diaryList = res.rows || []
        this.diaryTotal = res.total || 0
      })
    },
    goToRecordDetail(record) {
      if (record.topicId) {
        this.$router.push({ path: '/user/community/detail', query: { id: record.topicId } })
      } else {
        this.$router.push({ path: '/user/observation/detail', query: { id: record.recordId } })
      }
    },
    goToSurveyDetail(survey) {
      if (survey.topicId) {
        this.$router.push({ path: '/user/community/detail', query: { id: survey.topicId } })
      } else {
        this.$router.push({ path: '/user/survey/detail', query: { id: survey.surveyId } })
      }
    },
    goToIdentificationDetail(item) {
      if (item.topicId) {
        this.$router.push({ path: '/user/community/detail', query: { id: item.topicId } })
      } else {
        this.$router.push({ path: '/user/identification/detail', query: { id: item.identificationId } })
      }
    },
    goToDiaryDetail(diary) {
      this.$router.push({ path: '/user/diary/detail', query: { id: diary.diaryId } })
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
          if (this.$store.dispatch) {
            this.$store.dispatch('user/setAvatar', url).catch(() => {})
          }
        }
        this.$message.success('头像已更新')
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

.list-container {
  display: grid;
  gap: 16px;
  margin-bottom: 16px;
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

