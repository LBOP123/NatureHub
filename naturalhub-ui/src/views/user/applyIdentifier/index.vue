<template>
  <div class="apply-identifier-page">

    <!-- 管理员提示 -->
    <div v-if="isAdmin" class="status-card">
      <div class="status-icon admin-icon">
        <i class="el-icon-warning"></i>
      </div>
      <div class="status-info">
        <h2>无需申请</h2>
        <p class="status-desc pending-desc">管理员账号已具备最高权限，无需申请鉴定者身份。</p>
      </div>
    </div>

    <!-- 已是鉴定者提示 -->
    <div v-else-if="alreadyIdentifier" class="status-card">
      <div class="status-icon success">
        <i class="el-icon-circle-check"></i>
      </div>
      <div class="status-info">
        <h2>已是鉴定者</h2>
        <p class="status-desc success-desc">🎉 您已是鉴定者，可以为社群中的物种鉴定求助提供专业解答。</p>
      </div>
    </div>

    <!-- 状态展示卡片（已申请过时显示） -->
    <div v-else-if="existingApp" class="status-card">
      <div class="status-icon" :class="statusClass">
        <i :class="statusIcon"></i>
      </div>
      <div class="status-info">
        <h2>{{ statusTitle }}</h2>
        <p class="status-sub">申请时间：{{ parseTime(existingApp.createTime, '{y}-{m}-{d} {h}:{i}') }}</p>
        <template v-if="existingApp.status === 1">
          <p class="status-desc success-desc">🎉 恭喜！您已成为鉴定者，可以为社群中的物种鉴定求助提供专业解答。</p>
        </template>
        <template v-else-if="existingApp.status === 0">
          <p class="status-desc pending-desc">您的申请正在审核中，管理员将在 1-3 个工作日内处理，请耐心等待。</p>
        </template>
        <template v-else-if="existingApp.status === 2">
          <p class="status-desc reject-desc">
            <strong>拒绝原因：</strong>{{ existingApp.rejectReason || '暂无说明' }}
          </p>
          <el-button type="primary" class="reapply-btn" @click="handleReapply">重新申请</el-button>
        </template>
      </div>
    </div>

    <!-- 申请表单（首次申请 或 被拒绝后重新申请） -->
    <div v-if="showForm" class="form-card">
      <div class="form-header">
        <div class="form-icon">🔬</div>
        <h2>申请成为鉴定者</h2>
        <p>鉴定者可以为社群中的物种鉴定求助提供专业解答，帮助更多自然爱好者识别物种。</p>
      </div>

      <!-- 角色说明 -->
      <div class="role-intro">
        <div class="intro-item">
          <i class="el-icon-check"></i>
          <span>为物种鉴定求助提交专业回答</span>
        </div>
        <div class="intro-item">
          <i class="el-icon-check"></i>
          <span>获得鉴定者专属标识</span>
        </div>
        <div class="intro-item">
          <i class="el-icon-check"></i>
          <span>参与社群专家互动</span>
        </div>
      </div>

      <el-form
        ref="applyForm"
        :model="form"
        :rules="rules"
        label-position="top"
        class="apply-form"
      >
        <el-form-item label="真实姓名" prop="realName">
          <el-input
            v-model="form.realName"
            placeholder="请填写真实姓名"
            prefix-icon="el-icon-user"
          />
        </el-form-item>

        <el-form-item label="专业背景 / 擅长领域" prop="expertise">
          <el-input
            v-model="form.expertise"
            type="textarea"
            :rows="3"
            placeholder="例如：植物学硕士、昆虫爱好者10年、鸟类观察者…"
            maxlength="500"
            show-word-limit
          />
        </el-form-item>

        <el-form-item label="鉴定经验描述" prop="experience">
          <el-input
            v-model="form.experience"
            type="textarea"
            :rows="4"
            placeholder="请描述您在物种识别/鉴定方面的经历，例如参与过的项目、发表过的文章、鉴定数量等"
            maxlength="1000"
            show-word-limit
          />
        </el-form-item>

        <el-form-item label="个人简介" prop="bio">
          <el-input
            v-model="form.bio"
            type="textarea"
            :rows="3"
            placeholder="简单介绍自己（选填）"
            maxlength="500"
            show-word-limit
          />
        </el-form-item>

        <el-form-item label="资质证明（选填）">
          <p class="field-hint">可上传相关证书、论文截图等图片，最多 3 张</p>
          <el-upload
            :action="uploadUrl"
            :headers="uploadHeaders"
            list-type="picture-card"
            :limit="3"
            :file-list="qualificationFiles"
            :on-success="handleUploadSuccess"
            :on-remove="handleUploadRemove"
            accept="image/*"
          >
            <i class="el-icon-plus"></i>
          </el-upload>
        </el-form-item>

        <el-form-item>
          <el-button
            type="primary"
            :loading="submitting"
            class="submit-btn"
            @click="handleSubmit"
          >
            {{ submitting ? '提交中...' : '提交申请' }}
          </el-button>
          <el-button @click="$router.back()">取消</el-button>
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>

<script>
import { getMyApplication, submitApplication } from '@/api/user/identifier'
import { getToken } from '@/utils/auth'

export default {
  name: 'ApplyIdentifier',
  data() {
    return {
      loading: true,
      submitting: false,
      isAdmin: false,           // 管理员，不可申请
      alreadyIdentifier: false, // 已是鉴定者（userType='2'）
      existingApp: null,
      showForm: false,
      form: {
        realName: '',
        expertise: '',
        experience: '',
        bio: '',
        qualification: ''
      },
      qualificationFiles: [],
      rules: {
        realName: [
          { required: true, message: '请填写真实姓名', trigger: 'blur' },
          { min: 2, max: 20, message: '姓名长度在 2 到 20 个字符', trigger: 'blur' }
        ],
        expertise: [
          { required: true, message: '请描述您的专业背景或擅长领域', trigger: 'blur' },
          { min: 10, message: '至少填写 10 个字符', trigger: 'blur' }
        ],
        experience: [
          { required: true, message: '请描述您的鉴定经验', trigger: 'blur' },
          { min: 20, message: '至少填写 20 个字符', trigger: 'blur' }
        ]
      }
    }
  },
  computed: {
    uploadUrl() {
      return process.env.VUE_APP_BASE_API + '/common/upload'
    },
    uploadHeaders() {
      return { Authorization: 'Bearer ' + getToken() }
    },
    statusClass() {
      if (!this.existingApp) return ''
      const map = { 0: 'pending', 1: 'success', 2: 'rejected' }
      return map[this.existingApp.status] || ''
    },
    statusIcon() {
      if (!this.existingApp) return ''
      const map = { 0: 'el-icon-time', 1: 'el-icon-circle-check', 2: 'el-icon-circle-close' }
      return map[this.existingApp.status] || 'el-icon-time'
    },
    statusTitle() {
      if (!this.existingApp) return ''
      const map = { 0: '申请审核中', 1: '已成为鉴定者', 2: '申请已被拒绝' }
      return map[this.existingApp.status] || ''
    }
  },
  created() {
    this.loadStatus()
  },
  methods: {
    loadStatus() {
      this.loading = true
      // 先从 store 判断用户身份，管理员/已是鉴定者直接拦截
      const userType = String(this.$store.getters.userType ?? '1')
      if (userType === '0') {
        this.isAdmin = true
        this.loading = false
        return
      }
      if (userType === '2') {
        this.alreadyIdentifier = true
        this.loading = false
        return
      }
      // 探索者：查询申请记录
      getMyApplication().then(res => {
        const app = res.data
        if (!app) {
          this.existingApp = null
          this.showForm = true
        } else {
          this.existingApp = app
          this.showForm = false
        }
      }).catch(() => {
        this.showForm = true
      }).finally(() => {
        this.loading = false
      })
    },
    handleReapply() {
      this.showForm = true
      this.$nextTick(() => {
        const el = this.$el.querySelector('.form-card')
        if (el) el.scrollIntoView({ behavior: 'smooth' })
      })
    },
    handleUploadSuccess(response, file) {
      if (response.code === 200) {
        this.qualificationFiles.push({ name: file.name, url: response.fileName })
        this.form.qualification = this.qualificationFiles.map(f => f.url).join(',')
      } else {
        this.$message.error('图片上传失败：' + response.msg)
      }
    },
    handleUploadRemove(file) {
      this.qualificationFiles = this.qualificationFiles.filter(f => f.uid !== file.uid)
      this.form.qualification = this.qualificationFiles.map(f => f.url).join(',')
    },
    handleSubmit() {
      this.$refs.applyForm.validate(valid => {
        if (!valid) return
        this.submitting = true
        submitApplication(this.form).then(() => {
          this.$message.success('申请提交成功！管理员将在 1-3 个工作日内审核')
          this.showForm = false
          this.loadStatus()
        }).catch(() => {
          this.submitting = false
        }).finally(() => {
          this.submitting = false
        })
      })
    }
  }
}
</script>

<style lang="scss" scoped>
.apply-identifier-page {
  max-width: 760px;
  margin: 0 auto;
  padding: 24px 0 60px;
  display: flex;
  flex-direction: column;
  gap: 24px;
}

/* ── 状态卡片 ── */
.status-card {
  display: flex;
  align-items: flex-start;
  gap: 24px;
  background: #fff;
  border-radius: 16px;
  padding: 32px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);

  .status-icon {
    width: 64px;
    height: 64px;
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    flex-shrink: 0;

    i {
      font-size: 32px;
      color: #fff;
    }

    &.admin-icon { background: linear-gradient(135deg, #909399, #606266); }
    &.pending    { background: linear-gradient(135deg, #f39c12, #e67e22); }
    &.success    { background: linear-gradient(135deg, #27ae60, #2ecc71); }
    &.rejected   { background: linear-gradient(135deg, #e74c3c, #c0392b); }
  }

  .status-info {
    flex: 1;

    h2 {
      margin: 0 0 8px;
      font-size: 20px;
      font-weight: 600;
      color: #2c3e50;
    }

    .status-sub {
      color: #909399;
      font-size: 13px;
      margin: 0 0 12px;
    }

    .status-desc {
      font-size: 14px;
      line-height: 1.7;
      margin: 0;
      &.success-desc { color: #27ae60; }
      &.pending-desc { color: #e67e22; }
      &.reject-desc  { color: #e74c3c; }
    }

    .reapply-btn {
      margin-top: 16px;
    }
  }
}

/* ── 表单卡片 ── */
.form-card {
  background: #fff;
  border-radius: 16px;
  padding: 40px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
}

.form-header {
  text-align: center;
  margin-bottom: 32px;

  .form-icon {
    font-size: 52px;
    margin-bottom: 12px;
  }

  h2 {
    font-size: 24px;
    font-weight: 700;
    color: #2c3e50;
    margin: 0 0 10px;
  }

  p {
    color: #909399;
    font-size: 14px;
    margin: 0;
    line-height: 1.6;
  }
}

.role-intro {
  display: flex;
  flex-direction: column;
  gap: 10px;
  background: linear-gradient(135deg, rgba(67, 206, 162, 0.08), rgba(24, 90, 157, 0.06));
  border: 1px solid rgba(67, 206, 162, 0.3);
  border-radius: 12px;
  padding: 20px 24px;
  margin-bottom: 32px;

  .intro-item {
    display: flex;
    align-items: center;
    gap: 10px;
    font-size: 14px;
    color: #2c3e50;

    i {
      color: #43cea2;
      font-size: 16px;
      font-weight: bold;
    }
  }
}

.apply-form {
  ::v-deep .el-form-item__label {
    font-weight: 600;
    color: #2c3e50;
    padding-bottom: 6px;
  }

  ::v-deep .el-input__inner,
  ::v-deep .el-textarea__inner {
    border-radius: 10px;
    border-color: #e4e7ed;
    transition: border-color 0.3s;

    &:focus {
      border-color: #43cea2;
      box-shadow: 0 0 0 2px rgba(67, 206, 162, 0.15);
    }
  }

  .field-hint {
    font-size: 12px;
    color: #909399;
    margin: 0 0 10px;
  }

  .submit-btn {
    background: linear-gradient(135deg, #43cea2, #185a9d);
    border: none;
    padding: 12px 40px;
    font-size: 15px;
    border-radius: 10px;
    transition: all 0.3s;

    &:hover {
      transform: translateY(-2px);
      box-shadow: 0 8px 20px rgba(67, 206, 162, 0.35);
    }
  }
}

@media (max-width: 768px) {
  .apply-identifier-page {
    padding: 16px 0 40px;
  }

  .status-card {
    flex-direction: column;
    align-items: center;
    text-align: center;
    padding: 24px;
  }

  .form-card {
    padding: 24px 20px;
  }
}
</style>
