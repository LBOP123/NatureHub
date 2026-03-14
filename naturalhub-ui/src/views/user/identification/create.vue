<template>
  <div class="identification-create-container">
    <el-card class="create-card">
      <div slot="header" class="card-header">
        <i class="el-icon-search"></i>
        <span>{{ form.identificationId ? '编辑' : '发起' }}物种鉴定求助</span>
      </div>

      <el-form ref="identificationForm" :model="form" :rules="rules" label-width="100px" class="identification-form">
        <!-- 基本信息 -->
        <el-divider content-position="left">基本信息</el-divider>
        
        <el-form-item label="标题" prop="title">
          <el-input 
            v-model="form.title" 
            placeholder="请输入求助标题，如：请帮忙鉴定这是什么植物" 
            maxlength="200"
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
            placeholder="请输入观察地点"
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

        <!-- 特征描述 -->
        <el-divider content-position="left">特征描述</el-divider>

        <el-form-item label="详细描述" prop="description">
          <el-input
            v-model="form.description"
            type="textarea"
            :rows="5"
            placeholder="请详细描述您观察到的生物特征，如颜色、形状、大小、气味等"
            maxlength="2000"
            show-word-limit
          />
        </el-form-item>

        <el-form-item label="特征描述" prop="features">
          <el-input
            v-model="form.features"
            type="textarea"
            :rows="4"
            placeholder="请描述关键特征，如叶片形状、花朵颜色、体型大小等"
            maxlength="1000"
            show-word-limit
          />
        </el-form-item>

        <el-form-item label="生境描述" prop="habitat">
          <el-input
            v-model="form.habitat"
            type="textarea"
            :rows="3"
            placeholder="请描述发现地点的环境特征，如林下、水边、草地等"
            maxlength="500"
            show-word-limit
          />
        </el-form-item>

        <!-- 图片上传 -->
        <el-divider content-position="left">图片资料</el-divider>

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
              支持jpg/png格式，单张不超过10MB，最多上传9张。建议上传多角度清晰照片以便鉴定。
            </div>
          </el-upload>
        </el-form-item>

        <el-form-item label="备注">
          <el-input
            v-model="form.remark"
            type="textarea"
            :rows="2"
            placeholder="其他补充信息"
            maxlength="500"
            show-word-limit
          />
        </el-form-item>

        <!-- 操作按钮 -->
        <el-form-item>
          <el-button type="primary" @click="submitForReview" :loading="submitLoading">
            <i class="el-icon-s-promotion"></i> 提交审核
          </el-button>
          <el-button type="info" @click="saveDraft" :loading="submitLoading">
            <i class="el-icon-document"></i> 保存草稿
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
import { getIdentification, addIdentification, updateIdentification } from "@/api/user/identification";
import { getToken } from '@/utils/auth'

export default {
  name: "IdentificationCreate",
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
      // 表单参数
      form: {
        identificationId: null,
        title: '',
        description: '',
        features: '',
        habitat: '',
        observationTime: '',
        location: '',
        latitude: '',
        longitude: '',
        images: [],
        remark: '',
        auditStatus: 0
      },
      // 表单校验
      rules: {
        title: [
          { required: true, message: '请输入标题', trigger: 'blur' },
          { min: 5, max: 200, message: '标题长度在5到200个字符', trigger: 'blur' }
        ],
        description: [
          { required: true, message: '请输入详细描述', trigger: 'blur' },
          { min: 10, max: 2000, message: '描述长度在10到2000个字符', trigger: 'blur' }
        ],
        observationTime: [
          { required: true, message: '请选择观察时间', trigger: 'change' }
        ],
        location: [
          { required: true, message: '请输入观察地点', trigger: 'blur' }
        ],
        coordinates: [
          { validator: validateCoordinates, trigger: 'blur' }
        ]
      },
      // 上传相关
      uploadUrl: process.env.VUE_APP_BASE_API + '/common/upload',
      uploadHeaders: {
        Authorization: 'Bearer ' + getToken()
      },
      imageList: [],
      previewVisible: false,
      previewImageUrl: '',
      submitLoading: false,
      // 日期选择器配置
      pickerOptions: {
        disabledDate(time) {
          return time.getTime() > Date.now()
        }
      }
    };
  },
  created() {
    const identificationId = this.$route.query.id || this.$route.params.identificationId;
    if (identificationId) {
      this.loadData(identificationId);
    }
  },
  methods: {
    /** 加载数据 */
    loadData(identificationId) {
      getIdentification(identificationId).then(response => {
        const data = response.data
        this.form = {
          identificationId: data.identificationId,
          title: data.title,
          description: data.description,
          features: data.features,
          habitat: data.habitat,
          observationTime: data.observationTime,
          location: data.location,
          latitude: data.latitude,
          longitude: data.longitude,
          remark: data.remark,
          auditStatus: data.auditStatus
        }
        // 加载图片列表
        if (data.images) {
          const images = data.images.split(',')
          this.imageList = images.map((url, index) => ({
            name: 'image_' + index,
            url: url
          }))
          this.form.images = images
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
      this.$refs.identificationForm.validateField('coordinates')
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

    /** 保存草稿 */
    saveDraft() {
      // 草稿不验证必填项，只保存
      this.submitLoading = true
      this.form.auditStatus = 0
      const submitData = {
        ...this.form,
        images: this.form.images.join(',')
      }

      const request = this.form.identificationId ? updateIdentification(submitData) : addIdentification(submitData)
      
      request.then(response => {
        this.$message.success('保存草稿成功')
        this.$router.push('/user/identification/list')
      }).catch(() => {
        this.submitLoading = false
      }).finally(() => {
        this.submitLoading = false
      })
    },

    /** 提交审核 */
    submitForReview() {
      this.$refs.identificationForm.validate(valid => {
        if (valid) {
          if (this.form.images.length === 0) {
            this.$message.warning('请至少上传一张图片以便鉴定')
            return
          }

          this.$confirm('提交审核后将无法修改，是否继续?', '提示', {
            confirmButtonText: '确定',
            cancelButtonText: '取消',
            type: 'warning'
          }).then(() => {
            this.submitLoading = true
            this.form.auditStatus = 1
            const submitData = {
              ...this.form,
              images: this.form.images.join(',')
            }

            const request = this.form.identificationId ? updateIdentification(submitData) : addIdentification(submitData)
            
            request.then(response => {
              this.$message.success('提交审核成功，请等待管理员审核')
              this.$router.push('/user/identification/list')
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
      this.$refs.identificationForm.resetFields()
      this.imageList = []
      this.form.images = []
    },

    /** 返回 */
    goBack() {
      this.$router.go(-1)
    }
  }
};
</script>

<style lang="scss" scoped>
.identification-create-container {
  padding: 20px;
  
  .create-card {
    max-width: 1200px;
    margin: 0 auto;
    
    .card-header {
      font-size: 18px;
      font-weight: bold;
      
      i {
        margin-right: 8px;
        color: #E6A23C;
      }
    }
  }
  
  .identification-form {
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
  .identification-create-container {
    padding: 10px;
    
    .identification-form {
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
