<template>
  <div class="survey-create-container">
    <el-card class="create-card">
      <div slot="header" class="card-header">
        <i class="el-icon-map-location"></i>
        <span>{{ form.surveyId ? '编辑' : '创建' }}野外调查记录</span>
      </div>

      <el-form ref="surveyForm" :model="form" :rules="rules" label-width="100px" class="survey-form">
        <el-divider content-position="left">基本信息</el-divider>

        <el-form-item label="调查标题" prop="title">
          <el-input v-model="form.title" placeholder="请输入调查标题" maxlength="200" show-word-limit />
        </el-form-item>

        <el-form-item label="调查日期" prop="surveyDate">
          <el-date-picker v-model="form.surveyDate" type="date" placeholder="选择调查日期"
            value-format="yyyy-MM-dd" :picker-options="pickerOptions" style="width:100%" />
        </el-form-item>

        <el-form-item label="调查时间">
          <el-row :gutter="10">
            <el-col :span="12">
              <el-date-picker v-model="form.startTime" type="datetime" placeholder="开始时间"
                value-format="yyyy-MM-dd HH:mm:ss" :picker-options="pickerOptions" style="width:100%" />
            </el-col>
            <el-col :span="12">
              <el-date-picker v-model="form.endTime" type="datetime" placeholder="结束时间"
                value-format="yyyy-MM-dd HH:mm:ss" :picker-options="pickerOptions" style="width:100%" />
            </el-col>
          </el-row>
        </el-form-item>

        <el-form-item label="调查地点" prop="location">
          <el-input v-model="form.location" placeholder="请输入调查地点" maxlength="200" show-word-limit />
        </el-form-item>

        <el-divider content-position="left">调查方法</el-divider>

        <!-- 【改造点】生境类型：value 改为数字 -->
        <el-form-item label="生境类型" prop="habitatType">
          <el-select v-model="form.habitatType" placeholder="请选择生境类型" style="width:100%">
            <el-option v-for="dict in habitatTypeOptions" :key="dict.dictValue"
              :label="dict.dictLabel" :value="parseInt(dict.dictValue)" />
          </el-select>
        </el-form-item>

        <!-- 【改造点】调查方法：value 改为数字 -->
        <el-form-item label="调查方法" prop="surveyMethod">
          <el-select v-model="form.surveyMethod" placeholder="请选择调查方法" style="width:100%">
            <el-option v-for="dict in surveyMethodOptions" :key="dict.dictValue"
              :label="dict.dictLabel" :value="parseInt(dict.dictValue)" />
          </el-select>
        </el-form-item>

        <el-form-item label="天气情况">
          <el-row :gutter="10">
            <el-col :span="16">
              <el-input v-model="form.weather" placeholder="请输入天气情况，如：晴、多云、小雨">
                <template slot="prepend"><i class="el-icon-sunny"></i></template>
              </el-input>
            </el-col>
            <el-col :span="8">
              <el-input v-model="form.temperature" placeholder="温度">
                <template slot="append">℃</template>
              </el-input>
            </el-col>
          </el-row>
        </el-form-item>

        <el-form-item label="团队成员">
          <el-input v-model="form.teamMembers" placeholder="请输入团队成员，多个成员用逗号分隔" maxlength="500" show-word-limit>
            <template slot="prepend"><i class="el-icon-user"></i></template>
          </el-input>
        </el-form-item>

        <el-form-item label="发现物种数">
          <el-input-number v-model="form.speciesCount" :min="0" :max="9999"
            controls-position="right" style="width:200px" />
          <span style="margin-left:10px;font-size:12px;color:#909399">种</span>
        </el-form-item>

        <el-divider content-position="left">调查内容</el-divider>

        <el-form-item label="调查描述" prop="description">
          <el-input v-model="form.description" type="textarea" :rows="5"
            placeholder="请描述调查的目的、过程、方法等详细信息" maxlength="2000" show-word-limit />
        </el-form-item>

        <el-form-item label="主要发现">
          <el-input v-model="form.findings" type="textarea" :rows="4"
            placeholder="请描述调查的主要发现和结论" maxlength="1000" show-word-limit />
        </el-form-item>

        <el-divider content-position="left">多媒体资料</el-divider>

        <el-form-item label="图片上传">
          <el-upload ref="imageUpload" :action="uploadUrl" :headers="uploadHeaders"
            list-type="picture-card" :on-preview="handlePicturePreview"
            :on-remove="handleImageRemove" :on-success="handleImageSuccess"
            :before-upload="beforeImageUpload" :file-list="imageList" :limit="9" accept="image/*">
            <i class="el-icon-plus"></i>
            <div slot="tip" class="el-upload__tip">支持jpg/png格式，单张不超过10MB，最多上传9张</div>
          </el-upload>
        </el-form-item>

        <el-form-item label="备注">
          <el-input v-model="form.remark" type="textarea" :rows="2"
            placeholder="其他补充信息" maxlength="500" show-word-limit />
        </el-form-item>

        <el-form-item>
          <el-button type="primary" @click="submitForReview" :loading="submitLoading">
            <i class="el-icon-s-promotion"></i> 提交审核
          </el-button>
          <el-button type="info" @click="saveDraft" :loading="submitLoading">
            <i class="el-icon-document"></i> 保存草稿
          </el-button>
          <el-button @click="resetForm"><i class="el-icon-refresh-left"></i> 重置</el-button>
          <el-button @click="goBack"><i class="el-icon-back"></i> 返回</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-dialog :visible.sync="previewVisible" width="800px" append-to-body>
      <img :src="previewImageUrl" style="width:100%" />
    </el-dialog>
  </div>
</template>

<script>
import { getSurvey, addSurvey, updateSurvey } from '@/api/user/survey'
import { getToken } from '@/utils/auth'

export default {
  name: 'SurveyCreate',
  data() {
    return {
      form: {
        surveyId: null, title: '', surveyDate: '', startTime: '', endTime: '',
        location: '', habitatType: null, surveyMethod: null,
        weather: '', temperature: '', teamMembers: '', speciesCount: 0,
        description: '', findings: '', images: [], remark: '', auditStatus: 0
      },
      rules: {
        title:       [{ required: true, message: '请输入调查标题', trigger: 'blur' }, { min: 5, max: 200, message: '标题长度在5到200个字符', trigger: 'blur' }],
        surveyDate:  [{ required: true, message: '请选择调查日期', trigger: 'change' }],
        location:    [{ required: true, message: '请输入调查地点', trigger: 'blur' }],
        description: [{ required: true, message: '请输入调查描述', trigger: 'blur' }, { min: 10, max: 2000, message: '描述长度在10到2000个字符', trigger: 'blur' }]
      },
      uploadUrl: process.env.VUE_APP_BASE_API + '/common/upload',
      uploadHeaders: { Authorization: 'Bearer ' + getToken() },
      imageList: [],
      previewVisible: false,
      previewImageUrl: '',
      submitLoading: false,
      pickerOptions: { disabledDate(time) { return time.getTime() > Date.now() } },
      // 【改造点】字典数据
      habitatTypeOptions: [],
      surveyMethodOptions: []
    }
  },
  created() {
    // 【改造点】加载字典
    this.getDicts('nh_habitat_type').then(res => { this.habitatTypeOptions = res.data })
    this.getDicts('nh_survey_method').then(res => { this.surveyMethodOptions = res.data })
    const surveyId = this.$route.query.id || this.$route.params.surveyId
    if (surveyId) { this.loadData(surveyId) }
  },
  methods: {
    loadData(surveyId) {
      getSurvey(surveyId).then(response => {
        const data = response.data
        this.form = {
          surveyId: data.surveyId, title: data.title, surveyDate: data.surveyDate,
          startTime: data.startTime, endTime: data.endTime, location: data.location,
          // 【改造点】habitatType / surveyMethod 直接赋数字
          habitatType: data.habitatType, surveyMethod: data.surveyMethod,
          weather: data.weather, temperature: data.temperature,
          teamMembers: data.teamMembers, speciesCount: data.speciesCount || 0,
          description: data.description, findings: data.findings,
          remark: data.remark, auditStatus: data.auditStatus, images: []
        }
        if (data.images) {
          const images = data.images.split(',')
          this.imageList = images.map((url, index) => ({ name: 'image_' + index, url }))
          this.form.images = images
        }
      })
    },
    beforeImageUpload(file) {
      const isImage = file.type.startsWith('image/')
      const isLt10M = file.size / 1024 / 1024 < 10
      if (!isImage) { this.$message.error('只能上传图片文件!'); return false }
      if (!isLt10M) { this.$message.error('图片大小不能超过 10MB!'); return false }
      return true
    },
    handleImageSuccess(response) {
      if (response.code === 200) {
        this.form.images.push(response.url)
        this.$message.success('图片上传成功')
      } else {
        this.$message.error(response.msg || '图片上传失败')
      }
    },
    handleImageRemove(file) {
      const url = file.response ? file.response.url : file.url
      const index = this.form.images.indexOf(url)
      if (index > -1) { this.form.images.splice(index, 1) }
    },
    handlePicturePreview(file) { this.previewImageUrl = file.url; this.previewVisible = true },
    saveDraft() {
      this.submitLoading = true
      // 【改造点】auditStatus 数字：0=草稿
      this.form.auditStatus = 0
      const submitData = { ...this.form, images: this.form.images.join(',') }
      const request = this.form.surveyId ? updateSurvey(submitData) : addSurvey(submitData)
      request.then(() => {
        this.$message.success('保存草稿成功')
        this.$router.push('/user/survey/list')
      }).finally(() => { this.submitLoading = false })
    },
    submitForReview() {
      this.$refs.surveyForm.validate(valid => {
        if (valid) {
          this.$confirm('提交审核后将无法修改，是否继续?', '提示', { type: 'warning' }).then(() => {
            this.submitLoading = true
            // 【改造点】auditStatus 数字：1=待审核
            this.form.auditStatus = 1
            const submitData = { ...this.form, images: this.form.images.join(',') }
            const request = this.form.surveyId ? updateSurvey(submitData) : addSurvey(submitData)
            request.then(() => {
              this.$message.success('提交审核成功，请等待管理员审核')
              this.$router.push('/user/survey/list')
            }).finally(() => { this.submitLoading = false })
          })
        }
      })
    },
    resetForm() { this.$refs.surveyForm.resetFields(); this.imageList = []; this.form.images = [] },
    goBack() { this.$router.go(-1) }
  }
}
</script>

<style lang="scss" scoped>
.survey-create-container {
  padding: 20px;
  .create-card {
    max-width: 1200px; margin: 0 auto;
    .card-header { font-size: 18px; font-weight: bold; i { margin-right: 8px; color: #67C23A; } }
  }
  .survey-form {
    .el-divider { margin: 30px 0 20px; ::v-deep .el-divider__text { font-weight: bold; color: #409EFF; } }
  }
  ::v-deep .el-upload-list--picture-card .el-upload-list__item { width: 120px; height: 120px; }
  ::v-deep .el-upload--picture-card { width: 120px; height: 120px; line-height: 120px; }
}
@media screen and (max-width: 768px) {
  .survey-create-container {
    padding: 10px;
    ::v-deep .el-upload-list--picture-card .el-upload-list__item { width: 100px; height: 100px; }
    ::v-deep .el-upload--picture-card { width: 100px; height: 100px; line-height: 100px; }
  }
}
</style>
