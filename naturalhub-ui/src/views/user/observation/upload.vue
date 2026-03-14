<template>
  <div class="observation-upload-container">
    <el-card class="upload-card">
      <div slot="header" class="card-header">
        <i class="el-icon-camera-solid"></i>
        <span>新增观察记录</span>
      </div>

      <el-form ref="observationForm" :model="form" :rules="rules" label-width="100px" class="observation-form">
        <!-- 基本信息 -->
        <el-divider content-position="left">基本信息</el-divider>
        
        <el-form-item label="标题" prop="title">
          <el-input 
            v-model="form.title" 
            placeholder="请输入观察记录标题" 
            maxlength="100"
            show-word-limit
          />
        </el-form-item>

        <el-form-item label="观察时间" prop="observationTime">
          <el-date-picker
            v-model="form.observationTime"
            type="datetime"
            placeholder="选择观察时间"
            value-format="yyyy-MM-dd HH:mm:ss"
            :picker-options="pickerOptions"
            style="width: 100%"
          />
        </el-form-item>

        <el-form-item label="观察地点" prop="location">
          <el-input 
            v-model="form.location" 
            placeholder="请输入观察地点（如：北京市海淀区颐和园）"
            maxlength="200"
            show-word-limit
          />
        </el-form-item>

        <el-form-item label="经纬度" prop="coordinates">
          <el-row :gutter="10">
            <el-col :span="11">
              <el-input 
                v-model="form.latitude" 
                placeholder="纬度"
                @blur="validateCoordinates"
              >
                <template slot="prepend">纬度</template>
              </el-input>
            </el-col>
            <el-col :span="2" class="text-center">-</el-col>
            <el-col :span="11">
              <el-input 
                v-model="form.longitude" 
                placeholder="经度"
                @blur="validateCoordinates"
              >
                <template slot="prepend">经度</template>
              </el-input>
            </el-col>
          </el-row>
          <div class="form-tip">
            <el-button type="text" size="small" @click="getCurrentLocation">
              <i class="el-icon-location"></i> 获取当前位置
            </el-button>
          </div>
        </el-form-item>

        <!-- 物种信息 -->
        <el-divider content-position="left">物种信息</el-divider>

        <el-form-item label="物种类型" prop="speciesType">
          <el-select v-model="form.speciesType" placeholder="请选择物种类型" style="width: 100%">
            <el-option v-for="dict in speciesTypeOptions" :key="dict.dictValue" :label="dict.dictLabel" :value="parseInt(dict.dictValue)" />
          </el-select>
        </el-form-item>

        <el-form-item label="物种名称" prop="speciesName">
          <el-input 
            v-model="form.speciesName" 
            placeholder="请输入物种名称（中文名或学名）"
            maxlength="100"
            show-word-limit
          />
        </el-form-item>

        <el-form-item label="生境备注" prop="habitat">
          <el-input
            v-model="form.habitat"
            type="textarea"
            :rows="3"
            placeholder="请描述观察时的生境环境（如：林下、水边、草地等）"
            maxlength="500"
            show-word-limit
          />
        </el-form-item>

        <!-- 多媒体上传 -->
        <el-divider content-position="left">多媒体资料</el-divider>

        <el-form-item label="图片上传" prop="images">
          <el-upload
            ref="imageUpload"
            :action="uploadUrl"
            :headers="uploadHeaders"
            list-type="picture-card"
            :on-preview="handlePicturePreview"
            :on-remove="handleImageRemove"
            :on-success="handleImageSuccess"
            :before-upload="beforeImageUpload"
            :file-list="imageList"
            :limit="9"
            accept="image/*"
          >
            <i class="el-icon-plus"></i>
            <div slot="tip" class="el-upload__tip">
              支持jpg/png格式，单张不超过10MB，最多上传9张
            </div>
          </el-upload>
        </el-form-item>

        <el-form-item label="视频上传" prop="videos">
          <el-upload
            ref="videoUpload"
            :action="uploadUrl"
            :headers="uploadHeaders"
            list-type="picture-card"
            :on-remove="handleVideoRemove"
            :on-success="handleVideoSuccess"
            :before-upload="beforeVideoUpload"
            :file-list="videoList"
            :limit="3"
            accept="video/*"
          >
            <i class="el-icon-video-camera"></i>
            <div slot="tip" class="el-upload__tip">
              支持mp4/avi/mov格式，单个不超过100MB，最多上传3个
            </div>
          </el-upload>
        </el-form-item>

        <!-- 观察描述 -->
        <el-divider content-position="left">观察描述</el-divider>

        <el-form-item label="详细描述" prop="description">
          <el-input
            v-model="form.description"
            type="textarea"
            :rows="6"
            placeholder="请详细描述您的观察内容，包括形态特征、行为特点、数量等信息"
            maxlength="2000"
            show-word-limit
          />
        </el-form-item>

        <!-- 操作按钮 -->
        <el-form-item>
          <el-button type="primary" @click="submitForm" :loading="submitLoading">
            <i class="el-icon-check"></i> 保存记录
          </el-button>
          <el-button type="success" @click="submitForReview" :loading="submitLoading">
            <i class="el-icon-s-promotion"></i> 提交审核
          </el-button>
          <el-button @click="resetForm">
            <i class="el-icon-refresh-left"></i> 重置
          </el-button>
          <el-button @click="goBack">
            <i class="el-icon-back"></i> 返回
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 图片预览对话框 -->
    <el-dialog :visible.sync="previewVisible" width="800px" append-to-body>
      <img :src="previewImageUrl" style="width: 100%" />
    </el-dialog>
  </div>
</template>

<script>
import { addRecord, updateRecord, getRecord, submitForReview as submitReviewApi } from '@/api/user/record'
import { getToken } from '@/utils/auth'

export default {
  name: 'ObservationUpload',
  data() {
    // 自定义验证规则
    const validateCoordinates = (rule, value, callback) => {
      if (this.form.latitude && this.form.longitude) {
        const lat = parseFloat(this.form.latitude)
        const lng = parseFloat(this.form.longitude)
        if (isNaN(lat) || lat < -90 || lat > 90) {
          callback(new Error('纬度范围应在-90到90之间'))
        } else if (isNaN(lng) || lng < -180 || lng > 180) {
          callback(new Error('经度范围应在-180到180之间'))
        } else {
          callback()
        }
      } else {
        callback()
      }
    }

    return {
      // 表单数据
      form: {
        recordId: null,
        title: '',
        observationTime: '',
        location: '',
        latitude: '',
        longitude: '',
        speciesType: '',
        speciesName: '',
        habitat: '',
        description: '',
        images: [],
        videos: []
      },
      // 表单验证规则
      rules: {
        title: [
          { required: true, message: '请输入标题', trigger: 'blur' },
          { min: 2, max: 100, message: '标题长度在2到100个字符', trigger: 'blur' }
        ],
        observationTime: [
          { required: true, message: '请选择观察时间', trigger: 'change' }
        ],
        location: [
          { required: true, message: '请输入观察地点', trigger: 'blur' },
          { min: 2, max: 200, message: '地点长度在2到200个字符', trigger: 'blur' }
        ],
        coordinates: [
          { validator: validateCoordinates, trigger: 'blur' }
        ],
        speciesType: [
          { required: true, message: '请选择物种类型', trigger: 'change' }
        ],
        speciesName: [
          { required: true, message: '请输入物种名称', trigger: 'blur' }
        ],
        description: [
          { required: true, message: '请输入观察描述', trigger: 'blur' },
          { min: 10, max: 2000, message: '描述长度在10到2000个字符', trigger: 'blur' }
        ]
      },
      // 上传相关
      uploadUrl: process.env.VUE_APP_BASE_API + '/common/upload',
      uploadHeaders: {
        Authorization: 'Bearer ' + getToken()
      },
      imageList: [],
      videoList: [],
      previewVisible: false,
      previewImageUrl: '',
      submitLoading: false,
      speciesTypeOptions: [],
      // 日期选择器配置
      pickerOptions: {
        disabledDate(time) {
          return time.getTime() > Date.now()
        }
      }
    }
  },
  created() {
    this.getDicts('nh_species_type').then(res => { this.speciesTypeOptions = res.data || [] })
    // 如果是编辑模式，加载数据
    const recordId = this.$route.params.recordId
    if (recordId) {
      this.loadRecordData(recordId)
    }
  },
  methods: {
    /** 加载记录数据 */
    loadRecordData(recordId) {
      getRecord(recordId).then(response => {
        const data = response.data
        this.form = {
          recordId: data.recordId,
          title: data.title,
          observationTime: data.observationTime,
          location: data.location,
          latitude: data.latitude,
          longitude: data.longitude,
          speciesType: data.speciesType,
          speciesName: data.speciesName,
          habitat: data.habitat,
          description: data.description
        }
        // 加载图片列表
        if (data.images) {
          this.imageList = JSON.parse(data.images).map((url, index) => ({
            name: 'image_' + index,
            url: url
          }))
          this.form.images = JSON.parse(data.images)
        }
        // 加载视频列表
        if (data.videos) {
          this.videoList = JSON.parse(data.videos).map((url, index) => ({
            name: 'video_' + index,
            url: url
          }))
          this.form.videos = JSON.parse(data.videos)
        }
      })
    },

    /** 获取当前位置 */
    getCurrentLocation() {
      if (navigator.geolocation) {
        this.$message.info('正在获取位置信息...')
        navigator.geolocation.getCurrentPosition(
          position => {
            this.form.latitude = position.coords.latitude.toFixed(6)
            this.form.longitude = position.coords.longitude.toFixed(6)
            this.$message.success('位置获取成功')
          },
          error => {
            this.$message.error('位置获取失败：' + error.message)
          }
        )
      } else {
        this.$message.error('您的浏览器不支持地理定位')
      }
    },

    /** 验证坐标 */
    validateCoordinates() {
      this.$refs.observationForm.validateField('coordinates')
    },

    /** 图片上传前校验 */
    beforeImageUpload(file) {
      const isImage = file.type.startsWith('image/')
      const isLt10M = file.size / 1024 / 1024 < 10

      if (!isImage) {
        this.$message.error('只能上传图片文件!')
        return false
      }
      if (!isLt10M) {
        this.$message.error('图片大小不能超过 10MB!')
        return false
      }
      return true
    },

    /** 图片上传成功 */
    handleImageSuccess(response, file, fileList) {
      if (response.code === 200) {
        this.form.images.push(response.url)
        this.$message.success('图片上传成功')
      } else {
        this.$message.error(response.msg || '图片上传失败')
      }
    },

    /** 图片移除 */
    handleImageRemove(file, fileList) {
      const url = file.response ? file.response.url : file.url
      const index = this.form.images.indexOf(url)
      if (index > -1) {
        this.form.images.splice(index, 1)
      }
    },

    /** 图片预览 */
    handlePicturePreview(file) {
      this.previewImageUrl = file.url
      this.previewVisible = true
    },

    /** 视频上传前校验 */
    beforeVideoUpload(file) {
      const isVideo = file.type.startsWith('video/')
      const isLt100M = file.size / 1024 / 1024 < 100

      if (!isVideo) {
        this.$message.error('只能上传视频文件!')
        return false
      }
      if (!isLt100M) {
        this.$message.error('视频大小不能超过 100MB!')
        return false
      }
      return true
    },

    /** 视频上传成功 */
    handleVideoSuccess(response, file, fileList) {
      if (response.code === 200) {
        this.form.videos.push(response.url)
        this.$message.success('视频上传成功')
      } else {
        this.$message.error(response.msg || '视频上传失败')
      }
    },

    /** 视频移除 */
    handleVideoRemove(file, fileList) {
      const url = file.response ? file.response.url : file.url
      const index = this.form.videos.indexOf(url)
      if (index > -1) {
        this.form.videos.splice(index, 1)
      }
    },

    /** 提交表单 */
    submitForm() {
      this.$refs.observationForm.validate(valid => {
        if (valid) {
          this.submitLoading = true
          const submitData = {
            ...this.form,
            images: JSON.stringify(this.form.images),
            videos: JSON.stringify(this.form.videos)
          }

          const request = this.form.recordId ? updateRecord(submitData) : addRecord(submitData)
          
          request.then(response => {
            this.$message.success(this.form.recordId ? '修改成功' : '保存成功')
            this.$router.push('/user/observation/list')
          }).catch(() => {
            this.submitLoading = false
          }).finally(() => {
            this.submitLoading = false
          })
        }
      })
    },

    /** 提交审核 */
    submitForReview() {
      this.$refs.observationForm.validate(valid => {
        if (valid) {
          this.$confirm('提交审核后将无法修改，是否继续?', '提示', {
            confirmButtonText: '确定',
            cancelButtonText: '取消',
            type: 'warning'
          }).then(() => {
            this.submitLoading = true
            const submitData = {
              ...this.form,
              images: JSON.stringify(this.form.images),
              videos: JSON.stringify(this.form.videos)
            }

            const request = this.form.recordId ? updateRecord(submitData) : addRecord(submitData)
            
            request.then(response => {
              // 获取记录ID（新增时从response.data获取，编辑时使用form.recordId）
              const recordId = this.form.recordId || (response.data && response.data.recordId)
              if (recordId) {
                // 保存成功后提交审核
                return submitReviewApi(recordId)
              } else {
                throw new Error('无法获取记录ID')
              }
            }).then(() => {
              this.$message.success('提交审核成功，请等待管理员审核')
              this.$router.push('/user/observation/list')
            }).catch(() => {
              this.submitLoading = false
            }).finally(() => {
              this.submitLoading = false
            })
          })
        }
      })
    },

    /** 重置表单 */
    resetForm() {
      this.$refs.observationForm.resetFields()
      this.imageList = []
      this.videoList = []
      this.form.images = []
      this.form.videos = []
    },

    /** 返回 */
    goBack() {
      this.$router.go(-1)
    }
  }
}
</script>

<style lang="scss" scoped>
.observation-upload-container {
  padding: 20px;
  
  .upload-card {
    max-width: 1200px;
    margin: 0 auto;
    
    .card-header {
      font-size: 18px;
      font-weight: bold;
      
      i {
        margin-right: 8px;
        color: #67C23A;
      }
    }
  }
  
  .observation-form {
    .el-divider {
      margin: 30px 0 20px;
      
      ::v-deep .el-divider__text {
        font-weight: bold;
        color: #409EFF;
      }
    }
    
    .form-tip {
      font-size: 12px;
      color: #909399;
      margin-top: 5px;
    }
    
    .text-center {
      text-align: center;
      line-height: 40px;
    }
  }
  
  ::v-deep .el-upload-list--picture-card .el-upload-list__item {
    width: 120px;
    height: 120px;
  }
  
  ::v-deep .el-upload--picture-card {
    width: 120px;
    height: 120px;
    line-height: 120px;
  }
}

// 移动端适配
@media screen and (max-width: 768px) {
  .observation-upload-container {
    padding: 10px;
    
    .observation-form {
      ::v-deep .el-form-item__label {
        width: 80px !important;
      }
      
      ::v-deep .el-form-item__content {
        margin-left: 80px !important;
      }
    }
    
    ::v-deep .el-upload-list--picture-card .el-upload-list__item {
      width: 100px;
      height: 100px;
    }
    
    ::v-deep .el-upload--picture-card {
      width: 100px;
      height: 100px;
      line-height: 100px;
    }
  }
}
</style>
