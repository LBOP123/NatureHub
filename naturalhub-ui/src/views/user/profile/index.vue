<template>
  <div class="user-profile-container">
    <!-- 顶部个人信息卡片 -->
    <el-card class="profile-card" shadow="hover" v-loading="!infoLoaded">
      <div class="profile-main" v-show="infoLoaded">
        <!-- 头像：有图则显示，无则显示文字头像 -->
        <el-avatar v-if="avatarUrl" :size="72" :src="avatarUrl"/>
        <div v-else class="avatar-text" :style="{ background: avatarBgColor }">{{ avatarInitial }}</div>
        <div class="profile-info">
          <div class="profile-name-row">
            <span class="nickname">{{ userInfo.userName || userInfo.name }}</span>
            <el-tag size="small" class="identity-tag" :class="'identity-' + identityType">{{ identityLabel }}</el-tag>
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
            <!-- 观察记录 -->
            <el-tab-pane label="观察记录" name="records">
              <div class="list-container">
                <content-card
                  v-for="record in recordList"
                  :key="record.recordId"
                  :item="record"
                  content-type="record"
                  @click="goToRecordDetail(record)"
                />
                <el-empty v-if="recordList.length === 0" description="暂无已分享的观察记录"/>
              </div>
              <pagination
                v-show="recordTotal > 0"
                :total="recordTotal"
                :page.sync="recordQuery.pageNum"
                :limit.sync="recordQuery.pageSize"
                @pagination="loadMyRecords"
              />
            </el-tab-pane>

            <!-- 野外调查 -->
            <el-tab-pane label="野外调查" name="surveys">
              <div class="list-container">
                <content-card
                  v-for="survey in surveyList"
                  :key="survey.surveyId"
                  :item="survey"
                  content-type="survey"
                  @click="goToSurveyDetail(survey)"
                />
                <el-empty v-if="surveyList.length === 0" description="暂无已分享的调查记录"/>
              </div>
              <pagination
                v-show="surveyTotal > 0"
                :total="surveyTotal"
                :page.sync="surveyQuery.pageNum"
                :limit.sync="surveyQuery.pageSize"
                @pagination="loadMySurveys"
              />
            </el-tab-pane>

            <!-- 物种鉴定 -->
            <el-tab-pane label="物种鉴定" name="identifications">
              <div class="list-container">
                <content-card
                  v-for="item in identificationList"
                  :key="item.identificationId"
                  :item="item"
                  content-type="identification"
                  @click="goToIdentificationDetail(item)"
                />
                <el-empty v-if="identificationList.length === 0" description="暂无已分享的鉴定记录"/>
              </div>
              <pagination
                v-show="identificationTotal > 0"
                :total="identificationTotal"
                :page.sync="identificationQuery.pageNum"
                :limit.sync="identificationQuery.pageSize"
                @pagination="loadMyIdentifications"
              />
            </el-tab-pane>
          </el-tabs>
        </el-tab-pane>

        <!-- 3D展馆 -->
        <el-tab-pane label="3D展馆" name="gallery">
          <div v-loading="galleryLoading" class="gallery-tab">
            <div v-if="isSelf" class="gallery-manage-hint">
              <i class="el-icon-info"></i>
              管理哪些模型公开展示：
              <el-button type="text" @click="$router.push('/user/gallery')">前往3D展馆管理</el-button>
            </div>
            <div v-if="galleryList.length === 0 && !galleryLoading" class="gallery-empty">
              <span style="font-size:40px">🗿</span>
              <p>{{ isSelf ? '还没有公开展示的3D模型，去展馆管理中设置公开吧' : '该用户暂未公开任何3D模型' }}</p>
            </div>
            <div v-else class="gallery-masonry">
              <div
                v-for="item in galleryList"
                :key="item.id"
                class="gallery-item"
                @mouseenter="onGalleryCardEnter(item.id)"
                @mouseleave="onGalleryCardLeave(item.id)"
                @click="openGalleryPreview(item)"
              >
                <div class="gallery-thumb">
                  <model-viewer
                    v-if="item.modelUrlGlbQiniu || item.modelUrlGlb"
                    :key="item.id + '-' + (hoverRotateMap[item.id] ? 'spin' : 'idle')"
                    :src="item.modelUrlGlbQiniu || item.modelUrlGlb"
                    :auto-rotate="hoverRotateMap[item.id] ? true : null"
                    :auto-rotate-delay="hoverRotateMap[item.id] ? 0 : null"
                    :rotation-per-second="hoverRotateMap[item.id] ? '360deg' : null"
                    camera-controls
                    disable-zoom
                    disable-pan
                    interaction-prompt="none"
                    shadow-intensity="1"
                    background-color="#e0e0e0"
                    class="gallery-card-model-viewer"
                  />
                  <img
                    v-else-if="item.thumbnailQiniu || item.thumbnailUrl"
                    :src="item.thumbnailQiniu || item.thumbnailUrl"
                    class="gallery-thumb-img"
                  />
                  <div v-else class="gallery-thumb-placeholder">
                    <i class="el-icon-box"></i>
                  </div>
                  <div class="gallery-thumb-hover">
                    <i class="el-icon-view"></i>
                    <span>查看3D模型</span>
                  </div>
                </div>
                <div class="gallery-item-meta">
                  <div class="meta-line" :title="'模型：' + (item.taskName || ('模型 #' + item.id))">
                    模型：{{ item.taskName || ('模型 #' + item.id) }}
                  </div>
                  <div class="meta-line time-line">
                    时间：{{ formatTime(item.createTime) || '—' }}
                  </div>
                </div>
              </div>
            </div>
          </div>
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
                <img v-if="avatarUrl" :src="avatarUrl" class="avatar"/>
                <div v-else class="avatar-text avatar-text-sm" :style="{ background: avatarBgColor }">{{
                    avatarInitial
                  }}
                </div>
              </el-upload>
              <div class="avatar-tip">支持 JPG/PNG，大小不超过 2MB</div>
            </el-form-item>
            <el-form-item label="昵称" prop="nickName">
              <el-input v-model="profileForm.nickName" placeholder="请输入昵称"/>
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
              <el-input v-model="profileForm.phonenumber" placeholder="请输入手机号码"/>
            </el-form-item>
            <el-form-item label="邮箱" prop="email">
              <el-input v-model="profileForm.email" placeholder="请输入邮箱"/>
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

    <!-- 3D展馆模型预览弹窗 -->
    <el-dialog
      :visible.sync="galleryPreviewVisible"
      :title="galleryPreviewTask ? (galleryPreviewTask.taskName || '模型 #' + galleryPreviewTask.id) : '3D模型预览'"
      width="760px"
      append-to-body
      @closed="galleryPreviewTask = null; galleryModelLoading = false"
    >
      <div v-if="galleryPreviewTask" class="gallery-preview-body">
        <div class="gallery-preview-viewer" ref="galleryViewerWrap">
          <model-viewer
            :src="galleryPreviewTask.modelUrlGlbQiniu || galleryPreviewTask.modelUrlGlb"
            alt="3D模型" camera-controls shadow-intensity="1"
            background-color="#e0e0e0" enable-pan auto-rotate
            class="gallery-model-viewer"
            @load="galleryModelLoading = false"
            @error="galleryModelLoading = false"
          />
          <div v-if="galleryModelLoading" class="gallery-preview-loading">
            <i class="el-icon-loading"></i> <span>模型加载中...</span>
          </div>
        </div>
        <div style="display:flex;gap:12px;justify-content:center;margin-top:14px;">
          <el-button type="primary" icon="el-icon-full-screen" @click="galleryFullscreen">全屏查看</el-button>
          <el-button icon="el-icon-download" @click="galleryDownload(galleryPreviewTask)">下载 GLB</el-button>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import {listRecord, listPublicRecord} from '@/api/user/record'
import {getTopic} from '@/api/user/community'
import {listSurvey, listPublicSurvey} from '@/api/user/survey'
import {listIdentification, listPublicIdentification} from '@/api/user/identification'
import {getUserProfile, updateUserProfile, uploadAvatar} from '@/api/system/user'
import {getPublicProfile} from '@/api/user/profile'
import {listMyMark3dTasks, getPublicMark3dGallery} from '@/api/user/mark3d'
import ContentCard from './ContentCard'

export default {
  name: 'UserProfile',
  components: {ContentCard},
  data() {
    return {
      userInfo: {userId: null, userName: '', nickName: '', name: '', avatar: '', intro: '', createTime: ''},
      identityLabel: '探索者',
      identityType: 'info',
      userTypeOptions: [],
      mainTab: 'overview',
      activeTab: 'records',
      isSelf: true,
      recordQuery: {pageNum: 1, pageSize: 10},
      recordList: [],
      recordTotal: 0,
      surveyQuery: {pageNum: 1, pageSize: 10},
      surveyList: [],
      surveyTotal: 0,
      identificationQuery: {pageNum: 1, pageSize: 10},
      identificationList: [],
      identificationTotal: 0,
      diaryQuery: {pageNum: 1, pageSize: 10, visibility: '1'},
      diaryList: [],
      diaryTotal: 0,
      profileForm: {nickName: '', intro: '', phonenumber: '', email: ''},
      profileRules: {
        nickName: [{required: true, message: '昵称不能为空', trigger: 'blur'}],
        email: [{type: 'email', message: '邮箱格式不正确', trigger: 'blur'}]
      },
      profileSaving: false,
      infoLoaded: false,
      // 自然足迹只展示审核通过且已分享到社群的数据
      footprintFilter: {auditStatus: 2, isShared: 1},
      // 3D展馆
      galleryList: [],
      galleryLoading: false,
      galleryPreviewVisible: false,
      galleryPreviewTask: null,
      galleryModelLoading: false,
      hoverRotateMap: {},
      hoverRotateTimer: null
    }
  },
  computed: {
    // 头像地址（不动）
    avatarUrl() {
      const avatar = this.userInfo.avatar
      if (!avatar) return ''
      if (avatar.startsWith('http://') || avatar.startsWith('https://')) return avatar
      if (avatar.startsWith('/dev-api') || avatar.startsWith('/prod-api')) return avatar
      const base = process.env.VUE_APP_BASE_API || ''
      return base + avatar
    },
    // ✅ 修复：头像首字母（解决空白）
    avatarInitial() {
      const name = (this.userInfo.userName || '').trim()
      if (!name) return '?'
      return name.charAt(0).toUpperCase()
    },
    avatarBgColor() {
      const colors = [
        '#43a06b', '#2e7d9a', '#8e6bbf', '#c0640a',
        '#c0392b', '#1a6b8a', '#6d8c3e', '#7b4f9e',
        '#1a8c6b', '#e67e22'
      ]
      const name = (this.userInfo.userName || '').trim()
      if (!name) return colors[0]
      let hash = 0
      for (let i = 0; i < name.length; i++) hash += name.charCodeAt(i)
      return colors[hash % colors.length]
    }
  },
  watch: {
    '$route.query.userId'(newUserId) {
      this.infoLoaded = false
      this.initUserInfo(newUserId)
    },
    mainTab(tab) {
      if (tab === 'gallery' && this.galleryList.length === 0) {
        this.loadGallery()
      }
    }
  },
  created() {
    this.getDicts('nh_user_type').then(res => {
      this.userTypeOptions = res.data
    })
    const userId = this.$route.query.userId
    this.initUserInfo(userId)
    this.loadModelViewer()
  },
  beforeDestroy() {
    clearTimeout(this.hoverRotateTimer)
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
        this.applyIdentityLabel(this.$store.getters.userType)
        this.infoLoaded = true

        this.recordQuery.userId = targetUserId
        this.surveyQuery.userId = targetUserId
        this.identificationQuery.userId = targetUserId

        this.loadMyRecords()
        this.loadMySurveys()
        this.loadMyIdentifications()
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
          this.applyIdentityLabel(data.userType)
          this.infoLoaded = true
          this.recordQuery.userId = targetUserId
          this.surveyQuery.userId = targetUserId
          this.identificationQuery.userId = targetUserId

          this.loadMyRecords()
          this.loadMySurveys()
          this.loadMyIdentifications()
        })
      }
    },
    applyIdentityLabel(userType) {
      const t = String(userType ?? '1')
      const typeMap = {'0': 'admin', '1': 'explorer', '2': 'identifier'}
      const labelMap = {'0': '管理员', '1': '探索者', '2': '鉴定者'}
      this.identityType = typeMap[t] || 'explorer'
      this.identityLabel = labelMap[t] || '探索者'
    },
    loadMyRecords() {
      const params = {...this.recordQuery, ...this.footprintFilter}
      const fn = this.isSelf ? listRecord : listPublicRecord
      if (!this.isSelf) params.userId = this.recordQuery.userId
      fn(params).then(res => {
        this.recordList = res.rows || []
        this.recordTotal = res.total || 0
        this.enrichWithTopicStats(this.recordList)
      })
    },
    loadMySurveys() {
      const params = {...this.surveyQuery, ...this.footprintFilter}
      const fn = this.isSelf ? listSurvey : listPublicSurvey
      fn(params).then(res => {
        this.surveyList = res.rows || []
        this.surveyTotal = res.total || 0
        this.enrichWithTopicStats(this.surveyList)
      })
    },
    loadMyIdentifications() {
      const params = {...this.identificationQuery, ...this.footprintFilter}
      const fn = this.isSelf ? listIdentification : listPublicIdentification
      fn(params).then(res => {
        this.identificationList = res.rows || []
        this.identificationTotal = res.total || 0
        this.enrichWithTopicStats(this.identificationList)
      })
    },
    // 批量从关联话题中补充统计字段
    enrichWithTopicStats(list) {
      if (!list || list.length === 0) return
      const promises = list.map(item => {
        const tid = item.topicId || item.sharedTopicId
        if (!tid) return Promise.resolve()
        return getTopic(tid).then(res => {
          const topic = res.data || res
          if (!topic) return
          this.$set(item, 'viewCount', topic.viewCount || 0)
          this.$set(item, 'likeCount', topic.likeCount || 0)
          this.$set(item, 'commentCount', topic.commentCount || 0)
          this.$set(item, 'collectCount', topic.collectCount || 0)
          this.$set(item, 'isTop', topic.isTop || '0')
          this.$set(item, 'isEssence', topic.isEssence || '0')
        }).catch(() => {})
      })
      // 全部请求完毕后按置顶>精华>普通排序
      Promise.all(promises).then(() => {
        list.sort((a, b) => {
          const aTop = a.isTop === '1' ? 2 : 0
          const bTop = b.isTop === '1' ? 2 : 0
          const aEss = a.isEssence === '1' ? 1 : 0
          const bEss = b.isEssence === '1' ? 1 : 0
          return (bTop + bEss) - (aTop + aEss)
        })
      })
    },
    goToRecordDetail(record) {
      if (record.topicId) {
        this.$router.push({path: '/user/community/detail', query: {id: record.topicId}})
      } else {
        this.$router.push({path: '/user/observation/detail', query: {id: record.recordId}})
      }
    },
    goToSurveyDetail(survey) {
      if (survey.topicId) {
        this.$router.push({path: '/user/community/detail', query: {id: survey.topicId}})
      } else {
        this.$router.push({path: '/user/survey/detail', query: {id: survey.surveyId}})
      }
    },
    goToIdentificationDetail(item) {
      if (item.topicId) {
        this.$router.push({path: '/user/community/detail', query: {id: item.topicId}})
      } else {
        this.$router.push({path: '/user/identification/detail', query: {id: item.identificationId}})
      }
    },
    goToDiaryDetail(diary) {
      this.$router.push({path: '/user/diary/detail', query: {id: diary.diaryId}})
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
        // 只补充 store 中缺失的字段，避免覆盖已有值造成闪烁
        if (!this.userInfo.nickName) this.userInfo.nickName = data.nickName || ''
        if (!this.userInfo.avatar) this.userInfo.avatar = data.avatar || ''
        if (!this.userInfo.createTime) this.userInfo.createTime = data.createTime || ''
        this.userInfo.intro = data.remark || this.userInfo.intro
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
          // 同步更新 store，使导航栏头像实时刷新
          this.$store.commit('SET_AVATAR', url)
        }
        this.$message.success('头像已更新')
      })
    },
    // ===== 3D展馆方法 =====
    loadModelViewer() {
      if (typeof customElements !== 'undefined' && !customElements.get('model-viewer')) {
        const script = document.createElement('script')
        script.type = 'module'
        script.src = 'https://unpkg.com/@google/model-viewer/dist/model-viewer.min.js'
        document.head.appendChild(script)
      }
    },
    loadGallery() {
      this.galleryLoading = true
      const userId = this.userInfo.userId
      const fn = this.isSelf
        ? listMyMark3dTasks({taskStatus: '3', pageNum: 1, pageSize: 100})
        : getPublicMark3dGallery(userId)
      const promise = this.isSelf
        ? listMyMark3dTasks({taskStatus: '3', pageNum: 1, pageSize: 100})
        : getPublicMark3dGallery(userId)
      promise.then(res => {
        if (this.isSelf) {
          // listMyMark3dTasks 返回分页格式，需过滤 is_public=1
          this.galleryList = (res.rows || []).filter(t => t.isPublic === 1)
        } else {
          this.galleryList = res.data || []
        }
      }).finally(() => {
        this.galleryLoading = false
      })
    },
    openGalleryPreview(item) {
      this.galleryPreviewTask = item
      this.galleryModelLoading = true
      this.galleryPreviewVisible = true
    },
    onGalleryCardEnter(id) {
      this.$set(this.hoverRotateMap, id, true)
      clearTimeout(this.hoverRotateTimer)
      this.hoverRotateTimer = setTimeout(() => {
        this.$set(this.hoverRotateMap, id, false)
      }, 2200)
    },
    onGalleryCardLeave(id) {
      this.$set(this.hoverRotateMap, id, false)
    },
    galleryFullscreen() {
      const viewer = this.$el.querySelector('.gallery-model-viewer')
      const el = viewer || this.$refs.galleryViewerWrap
      if (!el) return
      if (el.requestFullscreen) el.requestFullscreen()
      else if (el.webkitRequestFullscreen) el.webkitRequestFullscreen()
    },
    galleryDownload(item) {
      const url = item.modelUrlGlbQiniu || item.modelUrlGlb
      if (!url) return
      const a = document.createElement('a')
      a.href = url
      a.download = (item.taskName || 'model') + '.glb'
      a.target = '_blank'
      a.click()
    },
    formatTime(t) {
      if (!t) return ''
      return new Date(t).toLocaleString('zh-CN', {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit',
        hour: '2-digit',
        minute: '2-digit'
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

.avatar-text {
  width: 72px;
  height: 72px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 28px;
  font-weight: 700;
  color: #fff;
  flex-shrink: 0;
  user-select: none;
  letter-spacing: 1px;
}

.avatar-text-sm {
  width: 80px;
  height: 80px;
  font-size: 30px;
  border-radius: 50%;
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
  font-weight: 600;
  letter-spacing: 0.5px;
  border: none;

  // 管理员：深邃蓝
  &.identity-admin {
    background: rgba(30, 60, 114, 0.12);
    color: #1e3c72;
    box-shadow: inset 0 0 0 1px rgba(30, 60, 114, 0.3);
  }

  // 探索者：自然绿（主题色）
  &.identity-explorer {
    background: rgba(67, 160, 71, 0.12);
    color: #2e7d32;
    box-shadow: inset 0 0 0 1px rgba(67, 160, 71, 0.3);
  }

  // 鉴定者：琥珀橙
  &.identity-identifier {
    background: rgba(230, 126, 34, 0.12);
    color: #c0640a;
    box-shadow: inset 0 0 0 1px rgba(230, 126, 34, 0.35);
  }
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

// ===== 3D展馆样式 =====
.gallery-tab {
  padding: 4px 0;
}

.gallery-manage-hint {
  font-size: 13px;
  color: #909399;
  margin-bottom: 16px;
  display: flex;
  align-items: center;
  gap: 6px;
  background: #f4f4f5;
  padding: 8px 14px;
  border-radius: 6px;
}

.gallery-empty {
  text-align: center;
  padding: 48px 20px;
  color: #909399;

  p {
    margin: 10px 0 0;
    font-size: 14px;
  }
}

.gallery-masonry {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 16px;
}

@media (max-width: 1200px) {
  .gallery-masonry {
    grid-template-columns: repeat(3, minmax(0, 1fr));
  }
}

@media (max-width: 900px) {
  .gallery-masonry {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 600px) {
  .gallery-masonry {
    grid-template-columns: 1fr;
  }
}

.gallery-item {
  cursor: pointer;
  border-radius: 10px;
  overflow: hidden;
  background: #fff;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.07);
  transition: all 0.3s ease;

  &:hover {
    transform: translateY(-4px);
    box-shadow: 0 8px 24px rgba(0, 0, 0, 0.13);

    .gallery-thumb-hover {
      opacity: 1;
    }

    .gallery-thumb-img {
      transform: scale(1.06);
    }
  }

  .gallery-item-meta {
    padding: 10px 12px 12px;
    background: #fff;

    .meta-line {
      font-size: 12px;
      color: #606266;
      line-height: 1.6;
      white-space: nowrap;
      overflow: hidden;
      text-overflow: ellipsis;
    }

    .time-line {
      color: #909399;
    }
  }
}

.gallery-thumb {
  position: relative;
  width: 100%;
  padding-top: 80%;
  background: #e0e0e0;;
  overflow: hidden;

  .gallery-thumb-img {
    position: absolute;
    inset: 0;
    width: 100%;
    height: 100%;
    object-fit: cover;
    transition: transform 0.3s ease;
  }

  .gallery-card-model-viewer {
    position: absolute;
    inset: 0;
    width: 100%;
    height: 100%;
    display: block;
    pointer-events: none;
  }

  .gallery-thumb-placeholder {
    position: absolute;
    inset: 0;
    display: flex;
    align-items: center;
    justify-content: center;

    i {
      font-size: 36px;
      color: #4a5568;
    }
  }

  .gallery-thumb-hover {
    position: absolute;
    inset: 0;
    background: rgba(0, 0, 0, 0.52);
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    opacity: 0;
    transition: opacity 0.3s ease;
    color: #fff;
    gap: 5px;

    i {
      font-size: 24px;
    }

    span {
      font-size: 12px;
    }
  }
}

.gallery-item-name {
  padding: 8px 10px;
  font-size: 13px;
  font-weight: 500;
  color: #e0e0e0;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.gallery-preview-body {
  .gallery-preview-viewer {
    position: relative;
    background: #e0e0e0;
    border-radius: 8px;
    overflow: hidden;

    .gallery-model-viewer {
      width: 100%;
      height: 420px;
      display: block;
    }
  }

  .gallery-preview-loading {
    position: absolute;
    inset: 0;
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 10px;
    background: rgba(26, 31, 46, 0.85);
    color: #fff;
    font-size: 14px;

    i {
      font-size: 28px;
    }
  }
}

// 全屏时铺满
:-webkit-full-screen .gallery-preview-viewer {
  position: fixed !important;
  inset: 0 !important;
  width: 100vw !important;
  height: 100vh !important;
  border-radius: 0 !important;
  z-index: 9999;
}

:fullscreen .gallery-preview-viewer {
  position: fixed !important;
  inset: 0 !important;
  width: 100vw !important;
  height: 100vh !important;
  border-radius: 0 !important;
  z-index: 9999;
}

:-webkit-full-screen .gallery-model-viewer {
  width: 100vw !important;
  height: 100vh !important;
}

:fullscreen .gallery-model-viewer {
  width: 100vw !important;
  height: 100vh !important;
}
</style>

