<template>
  <div class="bio-recognition-container">
    <el-card class="header-card" shadow="never">
      <div class="header-content">
        <div class="header-icon">🔍</div>
        <div class="header-text">
          <h2>AI生物识别</h2>
          <p>上传图片，智能识别动物、植物与生物信息</p>
        </div>
      </div>
    </el-card>

    <el-card class="upload-card" shadow="hover">
      <div slot="header">
        <i class="el-icon-upload"></i>
        <span>上传生物图片</span>
      </div>

      <el-upload
        class="upload-area"
        drag
        :action="uploadUrl"
        :headers="uploadHeaders"
        :before-upload="beforeUpload"
        :on-success="handleSuccess"
        :on-error="handleError"
        :show-file-list="false"
        accept="image/*"
        :disabled="loading"
      >
        <div v-if="!imageUrl" class="upload-content">
          <i class="el-icon-upload upload-icon"></i>
          <div class="upload-text">点击或将图片拖拽到此处<em>上传</em></div>
          <div class="upload-tip">支持 jpg/png/bmp 格式，大小不超过4MB</div>
        </div>
        <div v-else class="preview-content">
          <!-- 图片加载中动画 -->
          <div v-show="imageLoading" class="image-loading">
            <i class="el-icon-loading"></i>
            <span>图片加载中...</span>
          </div>
          <!-- 预览图片 -->
          <img
            :src="imageUrl"
            class="preview-image"
            v-show="!imageLoading"
            @load="handleImageLoad"
            @error="handleImageError"
          />
          <div class="preview-mask">
            <el-button type="primary" size="small" @click.stop="reUpload">
              <i class="el-icon-refresh-left"></i> 重新上传
            </el-button>
          </div>
        </div>
      </el-upload>

      <div class="action-buttons" v-if="imageUrl && !loading">
        <el-button
          type="primary"
          size="large"
          @click="recognizeImage"
          :loading="recognizing"
          icon="el-icon-search"
        >
          开始识别
        </el-button>
        <el-button
          size="large"
          @click="clearImage"
          icon="el-icon-delete"
        >
          清空图片
        </el-button>
      </div>

      <div v-if="loading" class="loading-overlay">
        <i class="el-icon-loading"></i>
        <p>图片上传中...</p>
      </div>

      <div v-if="recognizing" class="recognizing-overlay">
        <div class="recognizing-animation">
          <i class="el-icon-loading"></i>
          <div class="recognizing-text">
            <p class="main-text">AI识别中...</p>
            <p class="sub-text">正在分析物种信息，请稍候</p>
          </div>
        </div>
      </div>
    </el-card>

    <el-card v-if="result" class="result-card" shadow="hover">
      <div slot="header" class="result-header">
        <span>识别结果</span>
        <el-tag :type="getTypeTag(result.type)" size="medium">
          {{ getTypeText(result.type) }}
        </el-tag>
      </div>

      <div v-for="(item, index) in result.results" :key="index" class="result-item">
        <el-divider v-if="index > 0"></el-divider>

        <div class="result-main">
          <div class="result-name">
            <h3>{{ item.name }}</h3>
            <el-tag v-if="index === 0" type="success" size="small">最佳匹配</el-tag>
          </div>

          <div class="result-score">
            <span class="score-label">置信度</span>
            <el-progress
              :percentage="Math.round(item.score * 100)"
              :color="getScoreColor(item.score)"
              :stroke-width="12"
            ></el-progress>
          </div>
        </div>

        <div class="result-info">
          <el-descriptions :column="2" border size="small">
            <el-descriptions-item label="类别">
              {{ item.category }}
            </el-descriptions-item>
            <el-descriptions-item label="置信度">
              {{ (item.score * 100).toFixed(2) }}%
            </el-descriptions-item>
          </el-descriptions>

          <div v-if="item.baikeInfo" class="baike-info">
            <div class="baike-header">
              <i class="el-icon-document"></i>
              <span>百科信息</span>
            </div>
            <div v-if="item.baikeInfo.description" class="baike-description">
              {{ item.baikeInfo.description }}
            </div>
            <div class="baike-actions">
              <el-button
                v-if="item.baikeInfo.baikeUrl"
                type="text"
                @click="openBaike(item.baikeInfo.baikeUrl)"
                icon="el-icon-link"
              >
                查看百科
              </el-button>
              <el-button
                v-if="item.baikeInfo.imageUrl"
                type="text"
                @click="viewBaikeImage(item.baikeInfo.imageUrl)"
                icon="el-icon-picture"
              >
                查看百科图片
              </el-button>
            </div>
          </div>
        </div>

        <div v-if="index === 0" class="result-actions">
          <el-button
            type="primary"
            @click="createObservation(item)"
            icon="el-icon-plus"
          >
            生成观察记录
          </el-button>
          <el-button
            @click="shareResult(item)"
            icon="el-icon-share"
          >
            分享结果
          </el-button>
        </div>
      </div>
    </el-card>

    <el-card v-if="!result" class="tips-card" shadow="never">
      <div slot="header">
        <i class="el-icon-info"></i>
        <span>使用小贴士</span>
      </div>
      <ul class="tips-list">
        <li>清晰：拍摄时尽量让生物主体清晰可见</li>
        <li>完整：尽量拍摄生物整体特征，提高识别率</li>
        <li>光线：在自然光充足的环境下拍摄最佳</li>
        <li>大小：单张图片不超过4MB，支持jpg/png/bmp</li>
        <li>科普：识别结果包含百科知识，便于快速学习</li>
      </ul>
    </el-card>

    <el-dialog
      :visible.sync="previewVisible"
      width="60%"
      append-to-body
      center
    >
      <div style="text-align: center; padding: 10px 0;">
        <img
          :src="previewImageUrl"
          style="width: 80%; height: auto; object-fit: contain;"
          referrerpolicy="no-referrer"
        />
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { bioRecognize } from '@/api/common/bio'
import { getToken } from '@/utils/auth'

export default {
  name: 'BioRecognition',
  data() {
    return {
      uploadUrl: process.env.VUE_APP_BASE_API + '/common/upload',
      uploadHeaders: {
        Authorization: 'Bearer ' + getToken()
      },
      loading: false,
      recognizing: false,
      imageUrl: '',
      imageLoading: false,
      result: null,
      previewVisible: false,
      previewImageUrl: ''
    }
  },
  methods: {
    beforeUpload(file) {
      const isImage = file.type.startsWith('image/')
      if (!isImage) {
        this.$message.error('只能上传图片文件！')
        return false
      }
      const isLt4M = file.size / 1024 / 1024 < 4
      if (!isLt4M) {
        this.$message.error('图片大小不能超过 4MB！')
        return false
      }
      this.loading = true
      this.result = null
      return true
    },

    handleSuccess(response, file) {
      this.loading = false
      console.log('上传响应:', response)

      if (response.code === 200) {
        this.imageLoading = true
        this.imageUrl = response.url
        if (this.imageUrl) {
          this.$message.success('上传成功，点击"开始识别"')
          console.log('图片预览URL:', this.imageUrl)
        } else {
          this.$message.error('上传成功，但未获取到图片地址')
          console.error('返回数据异常:', response)
        }
      } else {
        this.$message.error(response.msg || '上传失败')
      }
    },

    handleError(error) {
      this.loading = false
      this.$message.error('上传失败：' + error.message)
    },

    // 图片加载完成
    handleImageLoad() {
      this.imageLoading = false
    },

    // 图片加载失败
    handleImageError() {
      this.imageLoading = false
      this.$message.error('图片加载失败，请重新上传')
    },

    recognizeImage() {
      if (!this.imageUrl) {
        this.$message.warning('请先上传图片')
        return
      }

      console.log('开始识别，图片URL:', this.imageUrl)

      this.recognizing = true
      this.result = null

      bioRecognize({ url: this.imageUrl }).then(response => {
        this.recognizing = false
        if (response.code === 200) {
          this.result = response.data
          this.$message.success('识别完成')
          this.$nextTick(() => {
            const resultCard = document.querySelector('.result-card')
            if (resultCard) {
              resultCard.scrollIntoView({ behavior: 'smooth', block: 'start' })
            }
          })
        } else {
          this.$message.error(response.msg || '识别失败')
        }
      }).catch(error => {
        this.recognizing = false
        console.error('识别异常:', error)
        this.$message.error('识别失败：' + error.message)
      })
    },

    reUpload() {
      this.imageUrl = ''
      this.imageLoading = false
      this.result = null
    },

    clearImage() {
      this.$confirm('确定要清空当前图片吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        this.imageUrl = ''
        this.imageLoading = false
        this.result = null
        this.$message.success('已清空')
      }).catch(() => {})
    },

    createObservation(item) {
      this.$router.push({
        path: '/user/observation/upload',
        query: {
          speciesName: item.name,
          speciesType: this.result.type === 'plant' ? 'plant' :
            this.result.type === 'animal' ? 'animal' : 'other',
          description: item.baikeInfo ? item.baikeInfo.description : '',
          imageUrl: this.imageUrl
        }
      })
    },

    shareResult(item) {
      this.$message.info('分享功能开发中...')
    },

    openBaike(url) {
      window.open(url, '_blank')
    },

    viewBaikeImage(url) {
      console.log("百科图片URL:", url);
      this.previewImageUrl = url
      this.previewVisible = true
    },

    getTypeText(type) {
      const typeMap = { plant: '植物', animal: '动物', other: '其他生物' }
      return typeMap[type] || type
    },

    getTypeTag(type) {
      const tagMap = { plant: 'success', animal: 'warning', other: 'info' }
      return tagMap[type] || 'info'
    },

    getScoreColor(score) {
      if (score >= 0.8) return '#67C23A'
      if (score >= 0.6) return '#E6A23C'
      return '#F56C6C'
    }
  }
}
</script>

<style lang="scss" scoped>
.bio-recognition-container {
  padding: 20px;
  max-width: 1200px;
  margin: 0 auto;
}
.header-card {
  margin-bottom: 20px;
  .header-content {
    display: flex;
    align-items: center;
    .header-icon {
      font-size: 48px;
      margin-right: 20px;
    }
    .header-text {
      h2 {
        margin: 0 0 8px 0;
        color: #303133;
        font-size: 24px;
      }
      p {
        margin: 0;
        color: #909399;
        font-size: 14px;
      }
    }
  }
}
.upload-card {
  margin-bottom: 20px;
  position: relative;
  .upload-area {
    ::v-deep .el-upload { width: 100%; }
    ::v-deep .el-upload-dragger {
      width: 100%;
      height: 400px;
      display: flex;
      align-items: center;
      justify-content: center;
    }
  }
  .upload-content {
    text-align: center;
    .upload-icon {
      font-size: 80px;
      color: #C0C4CC;
      margin-bottom: 20px;
    }
    .upload-text {
      font-size: 16px;
      color: #606266;
      margin-bottom: 10px;
      em {
        color: #409EFF;
        font-style: normal;
      }
    }
    .upload-tip {
      font-size: 12px;
      color: #909399;
    }
  }
  .preview-content {
    width: 100%;
    height: 100%;
    position: relative;
    .preview-image {
      width: 100%;
      height: 100%;
      object-fit: contain;
    }
    .preview-mask {
      position: absolute;
      top: 0;
      left: 0;
      right: 0;
      bottom: 0;
      background: rgba(0, 0, 0, 0.5);
      display: flex;
      align-items: center;
      justify-content: center;
      opacity: 0;
      transition: opacity 0.3s;
      &:hover { opacity: 1; }
    }
  }
  .action-buttons {
    margin-top: 20px;
    text-align: center;
    .el-button { margin: 0 10px; }
  }
  .loading-overlay, .recognizing-overlay {
    position: absolute;
    top: 60px;
    left: 0;
    right: 0;
    bottom: 0;
    background: rgba(255, 255, 255, 0.95);
    display: flex;
    align-items: center;
    justify-content: center;
    flex-direction: column;
    z-index: 10;
    i {
      font-size: 48px;
      color: #409EFF;
      margin-bottom: 15px;
    }
    p {
      color: #606266;
      font-size: 14px;
    }
  }
  .recognizing-overlay {
    .recognizing-animation {
      text-align: center;
      i {
        font-size: 64px;
        animation: rotate 1.5s linear infinite;
      }
      .recognizing-text {
        margin-top: 20px;
        .main-text {
          font-size: 18px;
          color: #303133;
          font-weight: bold;
          margin-bottom: 8px;
        }
        .sub-text {
          font-size: 14px;
          color: #909399;
        }
      }
    }
  }
}

// 图片加载中样式
.image-loading {
  width: 100%;
  height: 400px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-direction: column;
  color: #409EFF;
  font-size: 16px;
  i {
    font-size: 40px;
    margin-bottom: 15px;
  }
}

@keyframes rotate {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}
.result-card {
  margin-bottom: 20px;
  .result-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }
  .result-item {
    padding: 20px 0;
    .result-main {
      margin-bottom: 20px;
      .result-name {
        display: flex;
        align-items: center;
        margin-bottom: 15px;
        h3 {
          margin: 0 10px 0 0;
          color: #303133;
          font-size: 24px;
        }
      }
      .result-score {
        .score-label {
          display: inline-block;
          margin-bottom: 8px;
          color: #606266;
          font-size: 14px;
        }
      }
    }
    .result-info {
      margin-bottom: 20px;
      .baike-info {
        margin-top: 20px;
        padding: 15px;
        background: #F5F7FA;
        border-radius: 4px;
        .baike-header {
          display: flex;
          align-items: center;
          margin-bottom: 10px;
          color: #409EFF;
          font-weight: bold;
          i { margin-right: 5px; }
        }
        .baike-description {
          color: #606266;
          line-height: 1.8;
          margin-bottom: 15px;
        }
        .baike-actions {
          .el-button { margin-right: 10px; }
        }
      }
    }
    .result-actions {
      text-align: center;
      padding-top: 20px;
      border-top: 1px solid #EBEEF5;
      .el-button { margin: 0 10px; }
    }
  }
}
.tips-card {
  .tips-list {
    list-style: none;
    padding: 0;
    margin: 0;
    li {
      padding: 12px 0;
      color: #606266;
      line-height: 1.6;
      border-bottom: 1px dashed #EBEEF5;
      &:last-child { border-bottom: none; }
    }
  }
}
@media screen and (max-width: 768px) {
  .bio-recognition-container { padding: 10px; }
  .upload-card {
    .upload-area {
      ::v-deep .el-upload-dragger { height: 300px; }
    }
  }
  .result-item {
    .result-main {
      .result-name h3 { font-size: 20px; }
    }
    .result-actions {
      .el-button {
        display: block;
        width: 100%;
        margin: 10px 0 !important;
      }
    }
  }
}
</style>
