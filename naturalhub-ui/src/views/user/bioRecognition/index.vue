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

    <!-- 主体布局：左侧历史记录 + 右侧上传 -->
    <div class="main-layout">
      <!-- 左侧：历史记录 -->
      <div class="history-panel">
        <el-card shadow="hover" class="history-card">
          <div slot="header" class="history-header">
            <span>
              <i class="el-icon-document-copy"></i>
              识别历史
            </span>
            <el-button
              type="text"
              size="small"
              @click="loadHistory"
              :loading="historyLoading"
              icon="el-icon-refresh"
            >
              刷新
            </el-button>
          </div>

          <div v-if="historyList.length === 0" class="empty-state">
            <i class="el-icon-document"></i>
            <p>暂无识别记录</p>
          </div>

          <div v-else class="history-list">
            <div
              v-for="item in historyList"
              :key="item.id"
              class="history-item"
              :class="{ active: selectedHistory && selectedHistory.id === item.id }"
              @click="selectHistory(item)"
            >
              <div class="history-item-image">
                <img :src="item.imageUrl" :alt="item.recognitionResult" />
              </div>
              <div class="history-item-info">
                <div class="history-item-name">{{ item.recognitionResult }}</div>
                <div class="history-item-type">
                  <el-tag :type="getTypeTag(item.recognitionType)" size="mini">
                    {{ getTypeText(item.recognitionType) }}
                  </el-tag>
                </div>
                <div class="history-item-time">{{ formatTime(item.createTime) }}</div>
              </div>
            </div>
          </div>

          <!-- 分页 -->
          <div v-if="historyTotal > 0" class="history-pagination">
            <el-pagination
              :current-page="historyPage"
              :page-size="historyPageSize"
              :total="historyTotal"
              layout="prev, pager, next"
              @current-change="handleHistoryPageChange"
            ></el-pagination>
          </div>
        </el-card>
      </div>

      <!-- 右侧：上传区域 -->
      <div class="upload-panel">
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
      </div>
    </div>

    <!-- 下方：识别结果或历史详情 -->
    <div class="result-panel">

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
        </div>
      </el-card>

      <el-card v-else-if="selectedHistory" class="result-card" shadow="hover">
        <div slot="header" class="result-header">
          <span>历史记录详情</span>
          <el-tag :type="getTypeTag(selectedHistory.recognitionType)" size="medium">
            {{ getTypeText(selectedHistory.recognitionType) }}
          </el-tag>
        </div>

        <div class="history-detail">
          <div class="detail-image">
            <img :src="selectedHistory.imageUrl" :alt="selectedHistory.recognitionResult" />
          </div>

          <div class="detail-info">
            <div class="detail-name">
              <h3>{{ selectedHistory.recognitionResult }}</h3>
              <el-tag type="success" size="small">{{ getTypeText(selectedHistory.recognitionType) }}</el-tag>
            </div>

            <div class="detail-score" v-if="selectedHistory.confidence">
              <span class="score-label">置信度</span>
              <el-progress
                :percentage="Math.round(selectedHistory.confidence * 100)"
                :color="getScoreColor(selectedHistory.confidence / 100)"
                :stroke-width="12"
              ></el-progress>
            </div>

            <el-descriptions :column="1" border size="small" class="detail-descriptions">
              <el-descriptions-item label="识别时间">
                {{ formatTime(selectedHistory.createTime) }}
              </el-descriptions-item>
              <el-descriptions-item label="识别状态">
                <el-tag :type="selectedHistory.status === 1 ? 'success' : 'danger'">
                  {{ selectedHistory.status === 1 ? '成功' : '失败' }}
                </el-tag>
              </el-descriptions-item>
            </el-descriptions>

            <div v-if="selectedHistory.baikeInfo" class="baike-info">
              <div class="baike-header">
                <i class="el-icon-document"></i>
                <span>百科信息</span>
              </div>
              <div class="baike-description">
                {{ parseBaikeDescription(selectedHistory.baikeInfo) }}
              </div>
              <div class="baike-actions">
                <el-button
                  v-if="parseBaikeUrl(selectedHistory.baikeInfo)"
                  type="primary"
                  size="small"
                  @click="openBaike(parseBaikeUrl(selectedHistory.baikeInfo))"
                  icon="el-icon-link"
                >
                  查看百科
                </el-button>
                <el-button
                  v-if="parseBaikeImageUrl(selectedHistory.baikeInfo)"
                  type="success"
                  size="small"
                  @click="viewBaikeImage(parseBaikeImageUrl(selectedHistory.baikeInfo))"
                  icon="el-icon-picture"
                >
                  查看百科图片
                </el-button>
              </div>
            </div>
          </div>
        </div>
      </el-card>

      <el-card v-else class="tips-card" shadow="never">
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
    </div>

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
import { bioRecognize, getBioRecognitionHistory, getBioRecognitionDetail } from '@/api/common/bio'
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
      previewImageUrl: '',
      // 历史记录相关
      historyList: [],
      historyLoading: false,
      historyPage: 1,
      historyPageSize: 10,
      historyTotal: 0,
      selectedHistory: null
    }
  },
  mounted() {
    this.loadHistory()
  },
  methods: {
    // ===== 历史记录相关方法 =====
    loadHistory() {
      this.historyLoading = true
      getBioRecognitionHistory({
        pageNum: this.historyPage,
        pageSize: this.historyPageSize
      }).then(response => {
        this.historyLoading = false
        if (response.code === 200) {
          // 修复：API返回的数据在 response.data 中
          const data = response.data || {}
          this.historyList = data.rows || []
          this.historyTotal = data.total || 0
          console.log('历史记录加载成功:', this.historyList.length, '条')
        } else {
          this.$message.error(response.msg || '加载历史记录失败')
        }
      }).catch(error => {
        this.historyLoading = false
        console.error('加载历史记录异常:', error)
        this.$message.error('加载历史记录失败')
      })
    },

    handleHistoryPageChange(page) {
      this.historyPage = page
      this.loadHistory()
    },

    selectHistory(item) {
      this.selectedHistory = item
      this.result = null
      this.imageUrl = ''
    },

    createObservationFromHistory() {
      if (!this.selectedHistory) return
      this.$router.push({
        path: '/user/observation/upload',
        query: {
          speciesName: this.selectedHistory.recognitionResult,
          speciesType: this.selectedHistory.recognitionType === 1 ? 'plant' :
            this.selectedHistory.recognitionType === 2 ? 'animal' : 'other',
          imageUrl: this.selectedHistory.imageUrl
        }
      })
    },

    shareHistoryResult() {
      this.$message.info('分享功能开发中...')
    },

    formatTime(time) {
      if (!time) return ''
      const date = new Date(time)
      return date.toLocaleString('zh-CN', {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit',
        hour: '2-digit',
        minute: '2-digit'
      })
    },

    // ===== 上传识别相关方法 =====
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
      this.selectedHistory = null
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

    handleImageLoad() {
      this.imageLoading = false
    },

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
      this.selectedHistory = null

      bioRecognize({ url: this.imageUrl }).then(response => {
        this.recognizing = false
        if (response.code === 200) {
          this.result = response.data
          this.$message.success('识别完成')
          // 刷新历史记录
          this.historyPage = 1
          this.loadHistory()
          this.$nextTick(() => {
            const resultPanel = document.querySelector('.result-panel')
            if (resultPanel) {
              resultPanel.scrollIntoView({ behavior: 'smooth', block: 'start' })
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
      const typeMap = {
        plant: '植物',
        animal: '动物',
        other: '其他生物',
        1: '植物',
        2: '动物',
        3: '其他生物'
      }
      return typeMap[type] || type
    },

    getTypeTag(type) {
      const tagMap = {
        plant: 'success',
        animal: 'warning',
        other: 'info',
        1: 'success',
        2: 'warning',
        3: 'info'
      }
      return tagMap[type] || 'info'
    },

    getScoreColor(score) {
      if (score >= 0.8) return '#67C23A'
      if (score >= 0.6) return '#E6A23C'
      return '#F56C6C'
    },

    // 解析百科信息JSON
    parseBaikeInfo(baikeInfoStr) {
      try {
        if (typeof baikeInfoStr === 'string') {
          return JSON.parse(baikeInfoStr)
        }
        return baikeInfoStr
      } catch (e) {
        console.error('解析百科信息失败:', e)
        return {}
      }
    },

    parseBaikeDescription(baikeInfoStr) {
      const info = this.parseBaikeInfo(baikeInfoStr)
      return info.description || '暂无描述'
    },

    parseBaikeUrl(baikeInfoStr) {
      const info = this.parseBaikeInfo(baikeInfoStr)
      return info.baikeUrl || ''
    },

    parseBaikeImageUrl(baikeInfoStr) {
      const info = this.parseBaikeInfo(baikeInfoStr)
      return info.imageUrl || ''
    }
  }
}
</script>

<style lang="scss" scoped>
.bio-recognition-container {
  padding: 20px;
  max-width: 1400px;
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

// 主体布局：左右分栏
.main-layout {
  display: grid;
  grid-template-columns: 300px 1fr;
  gap: 20px;
  margin-bottom: 20px;

  .history-panel {
    .history-card {
      height: 100%;
      display: flex;
      flex-direction: column;

      .history-header {
        display: flex;
        justify-content: space-between;
        align-items: center;
        width: 100%;
      }

      .empty-state {
        text-align: center;
        padding: 40px 20px;
        color: #909399;
        i {
          font-size: 48px;
          margin-bottom: 10px;
          display: block;
        }
        p {
          margin: 0;
        }
      }

      .history-list {
        flex: 1;
        overflow-y: auto;
        max-height: 350px;
        padding-right: 5px;

        &::-webkit-scrollbar {
          width: 6px;
        }

        &::-webkit-scrollbar-track {
          background: #F5F7FA;
          border-radius: 3px;
        }

        &::-webkit-scrollbar-thumb {
          background: #C0C4CC;
          border-radius: 3px;

          &:hover {
            background: #909399;
          }
        }

        .history-item {
          display: flex;
          gap: 10px;
          padding: 10px;
          margin-bottom: 8px;
          border: 1px solid #EBEEF5;
          border-radius: 4px;
          cursor: pointer;
          transition: all 0.3s;

          &:hover {
            background: #F5F7FA;
            border-color: #409EFF;
          }

          &.active {
            background: #E6F7FF;
            border-color: #409EFF;
            box-shadow: 0 0 0 2px rgba(64, 158, 255, 0.2);
          }

          .history-item-image {
            width: 50px;
            height: 50px;
            flex-shrink: 0;
            border-radius: 4px;
            overflow: hidden;
            background: #F5F7FA;

            img {
              width: 100%;
              height: 100%;
              object-fit: cover;
            }
          }

          .history-item-info {
            flex: 1;
            min-width: 0;
            display: flex;
            flex-direction: column;
            justify-content: space-between;

            .history-item-name {
              font-size: 13px;
              font-weight: bold;
              color: #303133;
              white-space: nowrap;
              overflow: hidden;
              text-overflow: ellipsis;
            }

            .history-item-type {
              margin: 3px 0;
            }

            .history-item-time {
              font-size: 11px;
              color: #909399;
            }
          }
        }
      }

      .history-pagination {
        margin-top: 10px;
        text-align: center;
        padding-top: 10px;
        border-top: 1px solid #EBEEF5;

        ::v-deep .el-pagination {
          justify-content: center;
        }
      }
    }
  }

  .upload-panel {
    .upload-card {
      .upload-area {
        ::v-deep .el-upload {
          width: 100%;
        }
        ::v-deep .el-upload-dragger {
          width: 100%;
          height: 420px;
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
          &:hover {
            opacity: 1;
          }
        }
      }

      .action-buttons {
        margin-top: 20px;
        text-align: center;
        .el-button {
          margin: 0 10px;
        }
      }

      .loading-overlay,
      .recognizing-overlay {
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
  from {
    transform: rotate(0deg);
  }
  to {
    transform: rotate(360deg);
  }
}

// 结果面板
.result-panel {
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

            i {
              margin-right: 5px;
            }
          }

          .baike-description {
            color: #606266;
            line-height: 1.8;
            margin-bottom: 15px;
          }

          .baike-actions {
            .el-button {
              margin-right: 10px;
            }
          }
        }
      }

      .result-actions {
        text-align: center;
        padding-top: 20px;
        border-top: 1px solid #EBEEF5;

        .el-button {
          margin: 0 10px;
        }
      }
    }

    // 历史记录详情样式
    .history-detail {
      display: grid;
      grid-template-columns: 280px 1fr;
      gap: 30px;

      .detail-image {
        border-radius: 4px;
        overflow: hidden;
        background: #F5F7FA;
        height: 280px;
        flex-shrink: 0;

        img {
          width: 100%;
          height: 100%;
          display: block;
          object-fit: cover;
        }
      }

      .detail-info {
        .detail-name {
          display: flex;
          align-items: center;
          margin-bottom: 20px;

          h3 {
            margin: 0 10px 0 0;
            color: #303133;
            font-size: 24px;
          }
        }

        .detail-score {
          margin-bottom: 20px;

          .score-label {
            display: inline-block;
            margin-bottom: 8px;
            color: #606266;
            font-size: 14px;
          }
        }

        .detail-descriptions {
          margin-bottom: 20px;
        }

        .baike-info {
          margin: 20px 0;
          padding: 15px;
          background: #F5F7FA;
          border-radius: 4px;
          max-height: 300px;
          overflow-y: auto;

          &::-webkit-scrollbar {
            width: 6px;
          }

          &::-webkit-scrollbar-track {
            background: #EBEEF5;
            border-radius: 3px;
          }

          &::-webkit-scrollbar-thumb {
            background: #C0C4CC;
            border-radius: 3px;

            &:hover {
              background: #909399;
            }
          }

          .baike-header {
            display: flex;
            align-items: center;
            margin-bottom: 10px;
            color: #409EFF;
            font-weight: bold;

            i {
              margin-right: 5px;
            }
          }

          .baike-description {
            color: #606266;
            line-height: 1.8;
            font-size: 13px;
            margin-bottom: 15px;
            text-align: justify;
          }

          .baike-actions {
            display: flex;
            gap: 10px;
            padding-top: 10px;
            border-top: 1px solid #DCDFE6;

            .el-button {
              flex: 1;
              margin: 0;
              font-size: 12px;
            }
          }
        }

        .detail-actions {
          text-align: center;
          padding-top: 20px;
          border-top: 1px solid #EBEEF5;

          .el-button {
            margin: 0 10px;
          }
        }
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

        &:last-child {
          border-bottom: none;
        }
      }
    }
  }
}

// 响应式设计
@media screen and (max-width: 1024px) {
  .main-layout {
    grid-template-columns: 250px 1fr;
    gap: 15px;

    .history-panel {
      .history-card {
        .history-list {
          max-height: 500px;
        }
      }
    }

    .upload-panel {
      .upload-card {
        .upload-area {
          ::v-deep .el-upload-dragger {
            height: 350px;
          }
        }
      }
    }
  }

  .result-panel {
    .result-card {
      .history-detail {
        grid-template-columns: 250px 1fr;
        gap: 20px;
      }
    }
  }
}

@media screen and (max-width: 768px) {
  .bio-recognition-container {
    padding: 10px;
  }

  .main-layout {
    grid-template-columns: 1fr;
    gap: 15px;

    .history-panel {
      .history-card {
        .history-list {
          max-height: 300px;
        }
      }
    }

    .upload-panel {
      .upload-card {
        .upload-area {
          ::v-deep .el-upload-dragger {
            height: 300px;
          }
        }

        .action-buttons {
          .el-button {
            display: block;
            width: 100%;
            margin: 10px 0 !important;
          }
        }
      }
    }
  }

  .result-panel {
    .result-card {
      .result-item {
        .result-main {
          .result-name h3 {
            font-size: 20px;
          }
        }

        .result-actions {
          .el-button {
            display: block;
            width: 100%;
            margin: 10px 0 !important;
          }
        }
      }

      .history-detail {
        grid-template-columns: 1fr;
        gap: 15px;

        .detail-image {
          max-width: 100%;
        }

        .detail-info {
          .detail-actions {
            .el-button {
              display: block;
              width: 100%;
              margin: 10px 0 !important;
            }
          }
        }
      }
    }
  }
}
</style>
